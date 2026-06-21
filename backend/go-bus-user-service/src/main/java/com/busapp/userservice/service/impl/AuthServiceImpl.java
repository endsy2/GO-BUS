package com.busapp.userservice.service.impl;

import com.busapp.userservice.dto.mapper.UserMapper;
import com.busapp.userservice.dto.mapper.WalletMapper;
import com.busapp.userservice.dto.request.AuthRequest;
import com.busapp.userservice.dto.request.CreateUserWalletRequest;
import com.busapp.userservice.dto.request.RefreshTokenRequest;
import com.busapp.userservice.dto.response.AuthResponse;
import com.busapp.userservice.dto.response.RefreshTokenResponse;
import com.busapp.userservice.dto.response.RegisterResponse;
import com.busapp.userservice.dto.request.UserRequest;
import com.busapp.userservice.exception.DuplicateResourceException;
import com.busapp.userservice.exception.ResourceNotFoundException;
import com.busapp.userservice.model.Permission;
import com.busapp.userservice.model.Role;
import com.busapp.userservice.model.User;
import com.busapp.userservice.model.UserWallet;
import com.busapp.userservice.model.enums.Currency;
import com.busapp.userservice.repository.RoleRepository;
import com.busapp.userservice.repository.UserRepository;
import com.busapp.userservice.repository.UserWalletRepository;
import com.busapp.userservice.security.JwtUtil;
import com.busapp.userservice.security.RedisTokenService;
import com.busapp.userservice.service.AuthService;
import com.busapp.userservice.service.RolePermissionNameCacheService;
import com.busapp.userservice.service.WalletService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository    userRepository;
    private final JwtUtil           jwtUtil;
    private final RedisTokenService redisTokenService;
    private final PasswordEncoder   passwordEncoder;
    private final UserMapper userMapper;
    private final WalletMapper walletMapper;
    private final UserWalletRepository userWalletRepository;
    private final WalletService walletService;
    private final RoleRepository  roleRepository;
    private final RolePermissionNameCacheService rolePermissionNameCacheService;

    // ── Register ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public RegisterResponse register(UserRequest request) {
        log.info("[AUTH] Register attempt - email={}, userName={}", request.getEmail(), request.getUserName());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("[AUTH] Register failed - email already registered: {}", request.getEmail());
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        if (userRepository.existsByUserName(request.getUserName())) {
            log.warn("[AUTH] Register failed - username already taken: {}", request.getUserName());
            throw new DuplicateResourceException("Username already taken: " + request.getUserName());
        }
        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            log.warn("[AUTH] Register failed - phone already registered: {}", request.getPhone());
            throw new DuplicateResourceException("Phone already registered: " + request.getPhone());
        }

        request.setIsEmployee(false);
        request.setRoleId(2L);
        User user = userRepository.save(userMapper.toEntity(request));

        log.info("[AUTH] User registered successfully - userId={}, email={}", user.getId(), user.getEmail());
        return RegisterResponse.builder()
                .userId(user.getId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .build();
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Override
    public AuthResponse login(AuthRequest request) {
        log.info("[AUTH] Login attempt - email={}", request.getEmail());

        // Use JOIN FETCH so roles+permissions are loaded in one query —
        // avoids lazy-loading them outside a transaction in issueTokenPair().
        User user = userRepository.findByEmailWithRolesAndPermissions(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("LOGIN_FAILED", kv("email", request.getEmail()), kv("reason", "USER_NOT_FOUND"));
                    return new ResourceNotFoundException("Invalid email or password.");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("LOGIN_FAILED", kv("userId", user.getId()), kv("email", request.getEmail()),
                    kv("reason", "BAD_CREDENTIALS"));
            throw new ResourceNotFoundException("Invalid email or password.");
        }

        log.info("LOGIN_SUCCESS", kv("userId", user.getId()), kv("email", user.getEmail()));
        return issueTokenPair(user);
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    @Override
    public RefreshTokenResponse refresh(String refreshToken) {
        // 1. Validate JWT signature + expiry
        Claims claims;
        try {
            claims = jwtUtil.parseToken(refreshToken);
        } catch (JwtException e) {
            log.warn("TOKEN_VALIDATION_FAILED", kv("tokenType", "refresh"), kv("reason", "INVALID_OR_EXPIRED"));
            throw new ResourceNotFoundException("Invalid or expired refresh token.");
        }

        if (!"refresh".equals(claims.get("type"))) {
            log.warn("TOKEN_VALIDATION_FAILED", kv("reason", "WRONG_TOKEN_TYPE"));
            throw new ResourceNotFoundException("Provided token is not a refresh token.");
        }

        Long userId = Long.parseLong(claims.getSubject());

        // 2. Verify against Redis (one active refresh token per user)
        String stored = redisTokenService.getRefreshToken(userId);
        if (stored == null || !stored.equals(refreshToken)) {
            log.warn("TOKEN_VALIDATION_FAILED", kv("userId", userId), kv("tokenType", "refresh"),
                    kv("reason", "REVOKED_OR_REPLACED"));
            throw new ResourceNotFoundException("Refresh token has been revoked or replaced.");
        }

        // 3. Load user with roles+permissions in one JOIN FETCH query —
        // avoids lazy-loading them outside a transaction in issueTokenPair().
        User user = userRepository.findByIdWithRolesAndPermissions(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        log.info("[AUTH] Token refreshed successfully - userId={}", userId);
        return new RefreshTokenResponse(jwtUtil.generateAccessToken(
                userId,
                user.getEmail(),
                user.getUserName(),
                user.getRoles().stream().map(Role::getName).toList(),
                user.getRoles().stream().flatMap(role -> role.getPermissions().stream()).map(Permission::getName).toList()));
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @Override
    public void logout(String accessToken, String refreshToken) {
        // Blacklist the access token for its remaining lifetime
        if (accessToken != null && jwtUtil.isValid(accessToken)) {
            long ttl = jwtUtil.getRemainingTtlMs(accessToken);
            redisTokenService.blacklistAccessToken(accessToken, ttl);

            Long userId = jwtUtil.getUserId(accessToken);
            redisTokenService.deleteRefreshToken(userId);
            log.info("[AUTH] Logout successful - userId={}, tokenBlacklistedForMs={}", userId, ttl);
        }
        // If only refresh token is provided (access token already expired)
        else if (refreshToken != null) {
            try {
                Long userId = jwtUtil.getUserId(refreshToken);
                redisTokenService.deleteRefreshToken(userId);
                log.info("[AUTH] Logout via refresh token - userId={}", userId);
            } catch (JwtException ignored) {
                log.debug("[AUTH] Logout called with already-invalid token — nothing to revoke");
            }
        } else {
            log.debug("[AUTH] Logout called with no valid tokens — no action taken");
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private AuthResponse issueTokenPair(User user) {
        Set<Role> roles = user.getRoles() != null ? user.getRoles() : Collections.emptySet();
        
        // Use cached role and permission names to avoid expensive stream operations
        Set<Long> roleIds = roles.stream().map(Role::getId).collect(Collectors.toSet());
        Map<String, List<String>> names = rolePermissionNameCacheService.getRoleAndPermissionNames(roleIds, roles);
        
        List<String> roleNames = names.get("roleNames");
        List<String> permissionNames = names.get("permissionNames");

        String accessToken  = jwtUtil.generateAccessToken(
                user.getId(), user.getEmail(), user.getUserName(), roleNames, permissionNames);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // Store refresh token asynchronously (non-blocking)
        redisTokenService.storeRefreshToken(
                user.getId(), refreshToken, jwtUtil.getRefreshTokenExpiry());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .accessTokenExpiresIn(jwtUtil.getAccessTokenExpiry())
                .refreshTokenExpiresIn(jwtUtil.getRefreshTokenExpiry())
                .userId(user.getId())
                .email(user.getEmail())
                .userName(user.getUserName())
                .build();
    }
}
