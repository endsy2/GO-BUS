package com.busapp.userservice.service.impl;

import com.busapp.userservice.client.BookingClient;
import com.busapp.userservice.dto.admin.*;
import com.busapp.userservice.exception.BadRequestException;
import com.busapp.userservice.exception.ResourceNotFoundException;
import com.busapp.userservice.model.Role;
import com.busapp.userservice.model.User;
import com.busapp.userservice.model.UserWallet;
import com.busapp.userservice.model.WalletTransaction;
import com.busapp.userservice.model.enums.Gender;
import com.busapp.userservice.model.enums.TransactionStatus;
import com.busapp.userservice.model.enums.TransactionType;
import com.busapp.userservice.model.enums.WalletStatus;
import com.busapp.userservice.repository.RoleRepository;
import com.busapp.userservice.repository.UserRepository;
import com.busapp.userservice.repository.UserWalletRepository;
import com.busapp.userservice.repository.WalletTransactionRepository;
import com.busapp.userservice.service.AdminUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static net.logstash.logback.argument.StructuredArguments.kv;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository               userRepository;
    private final RoleRepository               roleRepository;
    private final UserWalletRepository         userWalletRepository;
    private final WalletTransactionRepository  walletTransactionRepository;
    private final PasswordEncoder              passwordEncoder;
    private final BookingClient                bookingClient;
    private final StringRedisTemplate          stringRedisTemplate;
    private final ObjectMapper                 objectMapper;

    // ── Redis key / TTL constants ─────────────────────────────────────────────
    private static final String BOOKING_STATS_KEY_PREFIX  = "admin:booking-stats:";
    private static final long   BOOKING_STATS_TTL_SECONDS = 300L; // 5 minutes

    // ── List / Filter ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AdminUserResponse> getUsers(AdminUserFilterRequest filter) {
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // full-text search across userName, fullName, email
            if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
                String like = "%" + filter.getSearch().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("userName")), like),
                        cb.like(cb.lower(root.get("fullName")), like),
                        cb.like(cb.lower(root.get("email")), like)
                ));
            }

            // active flag filter
            if (filter.getActive() != null) {
                predicates.add(cb.equal(root.get("active"), filter.getActive()));
            }

            // wallet status filter — left-join so users without a wallet still appear
            if (filter.getWalletStatus() != null && !filter.getWalletStatus().isBlank()) {
                try {
                    WalletStatus ws = WalletStatus.valueOf(filter.getWalletStatus().toUpperCase());
                    Join<Object, Object> walletJoin = root.join("wallet", JoinType.LEFT);
                    predicates.add(cb.equal(walletJoin.get("status"), ws));
                } catch (IllegalArgumentException ignored) {
                    // unknown status value — skip predicate
                }
            }
            if(filter.getIsEmployee()!=null){
                predicates.add(cb.equal(root.get("isEmployee"), filter.getIsEmployee()));
            }

            // createdAt range filter
            if (filter.getFromDate() != null && !filter.getFromDate().isBlank()) {
                LocalDateTime from = LocalDate.parse(filter.getFromDate()).atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (filter.getToDate() != null && !filter.getToDate().isBlank()) {
                LocalDateTime to = LocalDate.parse(filter.getToDate()).atTime(23, 59, 59);
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> page = userRepository.findAll(spec, pageable);
        List<AdminUserResponse> content = page.getContent()
                .stream().map(this::toAdminListResponse).collect(Collectors.toList());

        return PagedResponse.<AdminUserResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    // ── Get by ID ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(Long userId) {

        User user = userRepository.findDetailById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        return toAdminDetailResponse(user);
    }

    // ── Update User Info ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public AdminUserResponse updateUser(Long userId, UpdateUserRequest request) {
        log.debug("ADMIN_USER_UPDATE", kv("userId", userId));
        User user = findUser(userId);

        if (request.getUserName() != null && !request.getUserName().isBlank()) {
            if (userRepository.existsByUserNameAndIdNot(request.getUserName(), userId)) {
                throw new BadRequestException("Username already exists");
            }
            user.setUserName(request.getUserName());
        }
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getImage() != null) {
            user.setImage(request.getImage());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }

        return toAdminResponse(userRepository.save(user));
    }

    // ── Set Active ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AdminUserResponse setActive(Long userId, boolean active) {
        log.debug("ADMIN_USER_SET_ACTIVE", kv("userId", userId), kv("active", active));
        User user = findUser(userId);
        user.setActive(active);
        return toAdminResponse(userRepository.save(user));
    }

    // ── Reset Password ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AdminUserResponse resetPassword(Long userId, String newPassword) {
        // Never log the password value itself — only the action.
        log.debug("ADMIN_PASSWORD_RESET", kv("userId", userId));
        if (newPassword == null || newPassword.length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters.");
        }
        User user = findUser(userId);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        AdminUserResponse response = toAdminResponse(userRepository.save(user));
        log.debug("ADMIN_PASSWORD_RESET_DONE", kv("userId", userId));
        return response;
    }

    // ── Unlink Google ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AdminUserResponse unlinkGoogle(Long userId) {
        log.debug("ADMIN_UNLINK_GOOGLE", kv("userId", userId));
        User user = findUser(userId);
        user.setGoogleId(null);
        return toAdminResponse(userRepository.save(user));
    }

    // ── Assign Roles ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AdminUserResponse assignRoles(Long userId, List<String> roleNames) {
        log.debug("ADMIN_ASSIGN_ROLES", kv("userId", userId), kv("roles", roleNames));
        User user = findUser(userId);
        Set<Role> roles = new HashSet<>(roleRepository.findByNameIn(new HashSet<>(roleNames)));
        user.setRoles(roles);
        return toAdminResponse(userRepository.save(user));
    }

    // ── Adjust Wallet ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AdminUserResponse adjustWallet(Long userId, WalletAdjustRequest request) {
        log.debug("ADMIN_WALLET_ADJUST", kv("userId", userId), kv("operation", request.getOperation()),
                kv("amount", request.getAmount()));
        UserWallet wallet = userWalletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + userId));

        double amount = request.getAmount();
        double before = wallet.getBalance();
        double after;
        TransactionType type;

        if ("ADD".equalsIgnoreCase(request.getOperation())) {
            after = before + amount;
            type = TransactionType.TOP_UP;
        } else if ("DEDUCT".equalsIgnoreCase(request.getOperation())) {
            if (before < amount) {
                throw new BadRequestException("Insufficient wallet balance.");
            }
            after = before - amount;
            type = TransactionType.PAYMENT;
        } else {
            throw new BadRequestException("Operation must be ADD or DEDUCT.");
        }

        wallet.setBalance(after);
        wallet.setLastTransaction(LocalDateTime.now());
        userWalletRepository.save(wallet);
        log.debug("ADMIN_WALLET_ADJUSTED", kv("userId", userId), kv("balanceBefore", before), kv("balanceAfter", after));

        WalletTransaction tx = WalletTransaction.builder()
                .wallet(wallet)
                .amount(amount)
                .type(type)
                .status(TransactionStatus.COMPLETED)
                .description(request.getReason() != null ? request.getReason() : "Admin adjustment")
                .balanceBefore(before)
                .balanceAfter(after)
                .completedAt(LocalDateTime.now())
                .build();
        walletTransactionRepository.save(tx);

        return toAdminResponse(findUser(userId));
    }

    // ── Set Wallet Status ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public AdminUserResponse setWalletStatus(Long userId, String status) {
        log.debug("ADMIN_WALLET_SET_STATUS", kv("userId", userId), kv("status", status));
        UserWallet wallet = userWalletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + userId));
        try {
            wallet.setStatus(WalletStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid wallet status: " + status);
        }
        userWalletRepository.save(wallet);
        return toAdminResponse(findUser(userId));
    }

    // ── Delete User ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.debug("ADMIN_USER_DELETE", kv("userId", userId));
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found: " + userId);
        }
        userRepository.deleteById(userId);
        log.debug("ADMIN_USER_DELETED", kv("userId", userId));
    }

    // ── Redis-cached booking stats ────────────────────────────────────────────

    /**
     * Returns booking stats for the given user.
     * Checks Redis first (TTL = 5 min); on cache miss calls booking-service via Feign
     * and writes the result back to Redis.  Any Redis error is silently swallowed so
     * a Redis outage never breaks the user-detail endpoint.
     */
    private UserBookingStatsResponse getBookingStatsCached(Long userId) {
        String key = BOOKING_STATS_KEY_PREFIX + userId;

        // 1. Try Redis cache
        try {
            String cached = stringRedisTemplate.opsForValue().get(key);
            if (cached != null) {
                log.debug("[ADMIN] Booking stats cache hit for userId={}", userId);
                return objectMapper.readValue(cached, UserBookingStatsResponse.class);
            }
        } catch (Exception e) {
            log.debug("[ADMIN] Redis read failed for booking stats userId={}: {}", userId, e.getMessage());
        }

        // 2. Cache miss — call booking-service
        UserBookingStatsResponse stats = fetchBookingStats(userId);

        // 3. Write back to Redis
        try {
            stringRedisTemplate.opsForValue().set(
                    key,
                    objectMapper.writeValueAsString(stats),
                    BOOKING_STATS_TTL_SECONDS,
                    TimeUnit.SECONDS);
        } catch (Exception e) {
            log.debug("[ADMIN] Redis write failed for booking stats userId={}: {}", userId, e.getMessage());
        }

        return stats;
    }

    // ── ORM helpers ───────────────────────────────────────────────────────────

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    /**
     * Fetches lifetime booking + ticket stats from booking-service.
     * Returns a zero-filled object on any failure so the detail view stays usable
     * if booking-service is briefly unreachable.
     */
    private UserBookingStatsResponse fetchBookingStats(Long userId) {
        try {
            Map<String, Object> raw = bookingClient.getUserDetailStats(userId);
            if (raw == null) {
                return emptyBookingStats();
            }
            return UserBookingStatsResponse.builder()
                    .totalBookings(asLong(raw.get("totalBookings")))
                    .totalSpent(asBigDecimal(raw.get("totalSpent")))
                    .activeTickets(asLong(raw.get("activeTickets")))
                    .build();
        } catch (Exception e) {
            log.warn("[ADMIN] Failed to fetch booking stats for userId={}: {}", userId, e.getMessage());
            return emptyBookingStats();
        }
    }

    private UserBookingStatsResponse emptyBookingStats() {
        return UserBookingStatsResponse.builder()
                .totalBookings(0L)
                .totalSpent(BigDecimal.ZERO)
                .activeTickets(0L)
                .build();
    }

    private Long asLong(Object v) {
        if (v == null) return 0L;
        if (v instanceof Number n) return n.longValue();
        return Long.parseLong(v.toString());
    }

    private BigDecimal asBigDecimal(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(v.toString());
    }

    // ── ORM mappers ───────────────────────────────────────────────────────────

    /**
     * Full detail mapper used by {@code getUserById}.
     * Roles + permissions + wallet are pre-loaded via JOIN FETCH in
     * {@link com.busapp.userservice.repository.UserRepository#findDetailById}.
     * No lazy-load will fire here.
     */
    private AdminUserResponse toAdminDetailResponse(User user) {
        AdminUserResponse.AdminUserResponseBuilder builder = AdminUserResponse.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .image(user.getImage())
                .gender(user.getGender())
                .googleId(user.getGoogleId())
                .active(Boolean.TRUE.equals(user.getActive()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt());

        if (user.getWallet() != null) {
            UserWallet w = user.getWallet();
            builder.walletBalance(w.getBalance())
                    .walletStatus(w.getStatus().name())
                    .walletCurrency(w.getCurrency().toString());
        }

        return builder.build();
    }

    /**
     * Lightweight mapper for the paginated list endpoint.
     * Intentionally skips roles and permissions — they are not needed by the
     * admin user table and skipping them eliminates the UserRole + RolePermission
     * queries that used to fire for every user on the page.
     */
    private AdminUserResponse toAdminListResponse(User user) {
        AdminUserResponse.AdminUserResponseBuilder builder = AdminUserResponse.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .image(user.getImage())
                .gender(user.getGender())
                .googleId(user.getGoogleId())
                .active(Boolean.TRUE.equals(user.getActive()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt());

        if (user.getWallet() != null) {
            UserWallet wallet = user.getWallet();
            builder.walletBalance(wallet.getBalance())
                    .walletStatus(wallet.getStatus().name())
                    .walletCurrency(wallet.getCurrency().toString());
        }

        return builder.build();
    }

    /**
     * Mapper used by write-path single-user endpoints (update, role assignment,
     * wallet ops, etc.). The caller is responsible for loading the user via a
     * JOIN FETCH query before passing it here when roles/permissions are needed.
     */
    private AdminUserResponse toAdminResponse(User user) {
        AdminUserResponse.AdminUserResponseBuilder builder = AdminUserResponse.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .image(user.getImage())
                .gender(user.getGender())
                .googleId(user.getGoogleId())
                .active(Boolean.TRUE.equals(user.getActive()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt());

        if (user.getRoles() != null) {
            builder.roles(user.getRoles().stream()
                    .map(Role::getName).collect(Collectors.toList()));
            builder.permissions(user.getRoles().stream()
                    .flatMap(r -> r.getPermissions().stream())
                    .map(p -> p.getName())
                    .distinct()
                    .collect(Collectors.toList()));
        }

        if (user.getWallet() != null) {
            UserWallet wallet = user.getWallet();
            builder.walletBalance(wallet.getBalance())
                    .walletStatus(wallet.getStatus().name())
                    .walletCurrency(wallet.getCurrency().toString());
        }

        return builder.build();
    }
}
