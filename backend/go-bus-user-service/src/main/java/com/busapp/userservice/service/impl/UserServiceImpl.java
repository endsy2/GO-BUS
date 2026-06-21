package com.busapp.userservice.service.impl;

import com.busapp.userservice.dto.request.UpdateProfileRequest;
import com.busapp.userservice.dto.request.UserRequest;
import com.busapp.userservice.dto.response.UserBasicResponse;
import com.busapp.userservice.dto.response.UserResponseDetail;
import com.busapp.userservice.exception.DuplicateResourceException;
import com.busapp.userservice.exception.ResourceNotFoundException;
import com.busapp.userservice.dto.mapper.UserMapper;
import com.busapp.userservice.model.User;
import com.busapp.userservice.model.UserSpecification;
import com.busapp.userservice.repository.UserRepository;
import com.busapp.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static net.logstash.logback.argument.StructuredArguments.kv;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;


    @Override
    public List<UserResponseDetail> getUsersByIds(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return userRepository.findAllById(userIds)
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDetail getUserById(Long id) {
        User user = userRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userMapper.toResponse(user);
    }

    @Override
    public UserBasicResponse getUserBasicById(Long id) {
        return userRepository.findBasicByIdNative(id)
                .map(this::mapToUserBasicResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    public List<UserBasicResponse> getUserBasicByIds(Set<Long> ids) {
        return userRepository.findBasicByIdsNative(ids).stream()
                .map(this::mapToUserBasicResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map Object[] from native query to UserBasicResponse
     * Order: id, userName, fullName, email, phone
     */
    private UserBasicResponse mapToUserBasicResponse(Object[] row) {
        log.debug("USER_ROW_MAP", kv("rowLength", row.length));

        // Unwrap if the result is wrapped in an extra array layer
        if (row.length == 1 && row[0] instanceof Object[]) {
            log.debug("USER_ROW_UNWRAP", kv("unwrappedLength", ((Object[]) row[0]).length));
            row = (Object[]) row[0];
        }

        if (row == null || row.length < 5) {
            throw new IllegalArgumentException("Invalid row data from native query. Length: " + (row == null ? "null" : row.length));
        }

        // Handle different numeric types that PostgreSQL might return
        Long id;
        if (row[0] instanceof Number) {
            id = ((Number) row[0]).longValue();
        } else if (row[0] instanceof Long) {
            id = (Long) row[0];
        } else if (row[0] instanceof Integer) {
            id = ((Integer) row[0]).longValue();
        } else {
            throw new IllegalArgumentException("Unexpected type for id: " + row[0].getClass());
        }

        return new UserBasicResponse(
                id,                              // id
                (String) row[1],                 // userName
                (String) row[2],                 // fullName
                (String) row[3],                 // email
                (String) row[4]                  // phone
        );
    }

    @Override
    public UserResponseDetail getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return userMapper.toResponse(user);
    }

    @Override
    public UserBasicResponse getUserByUsername(String username) {
        return userRepository.findByUserName(username)
                .map(user -> UserBasicResponse.builder()
                        .id(user.getId())
                        .userName(user.getUserName())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .build())
                .orElse(null);
    }

    @Override
    public UserResponseDetail getUserByPhone(String phone) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with phone: " + phone));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponseDetail getUserByGoogleId(String googleId) {
        User user = userRepository.findByGoogleId(googleId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with googleId: " + googleId));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponseDetail createUser(UserRequest request) {
        log.debug("USER_CREATE", kv("userName", request.getUserName()));
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        if (userRepository.existsByUserName(request.getUserName())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUserName());
        }
        User user = userMapper.toEntity(request);
        User saved = userRepository.save(user);
        log.debug("USER_CREATED", kv("userId", saved.getId()), kv("userName", saved.getUserName()));
        return userMapper.toResponse(saved);
    }


    @Override
    public UserResponseDetail updateProfile(Long userId, UpdateProfileRequest request) {
        log.debug("USER_PROFILE_UPDATE", kv("userId", userId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Update only provided fields
        if (request.getUserName() != null && !request.getUserName().isBlank()) {
            // Check if username is already taken by another user
            if (userRepository.existsByUserNameAndIdNot(request.getUserName(), userId)) {
                throw new DuplicateResourceException("Username already exists");
            }
            user.setUserName(request.getUserName());
        }
        
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        
        return userMapper.toResponse(userRepository.save(user));
    }

//    @Override
//    public UserResponseDetail updateEmployee(Long id, UpdateEmployeeRequest request) {
//        User user = userRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
//
//        // Verify user is an employee
//        if (user.getIsEmployee() == null || !user.getIsEmployee()) {
//            throw new ResourceNotFoundException("User with id " + id + " is not an employee");
//        }
//
//        // Update employee fields
//        if (request.getUserName() != null) user.setUserName(request.getUserName());
//        if (request.getFullName() != null) user.setFullName(request.getFullName());
//        if (request.getEmail() != null) user.setEmail(request.getEmail());
//        if (request.getPhone() != null) user.setPhone(request.getPhone());
//        if (request.getPassword() != null) user.setPasswordHash(request.getPassword());
//        if (request.getImage() != null) user.setImage(request.getImage());
//        if (request.getGender() != null) user.setGender(request.getGender());
//
//        return userMapper.toResponse(userRepository.save(user));
//    }

    @Override
    public void deleteUser(Long id) {
        log.debug("USER_SOFT_DELETE", kv("userId", id));
        User user=userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setIsDeleted(true);
        userRepository.save(user);
        log.debug("USER_SOFT_DELETED", kv("userId", id));
    }

    @Override
    public Page<UserResponseDetail> filterBySpec(int pageStart, int pageSize, Long userId, String email, String phone, String googleId, String userName, Boolean isEmployee,Boolean isDeleted,Boolean isActive) {
        Pageable pageable = PageRequest.of(pageStart, pageSize);
        Specification<User> spec = Specification
                .where(UserSpecification.findByUserName(userName))
                .and(UserSpecification.findById(userId))
                .and(UserSpecification.findByUserName(userName))
                .and(UserSpecification.findByEmail(email))
                .and(UserSpecification.findByPhone(phone))
                .and(UserSpecification.findByGoogleId(googleId))
                .and(UserSpecification.findByUserName(userName))
                .and(UserSpecification.findByIsEmployee(isEmployee))
                .and(UserSpecification.findByIsDeleted(isDeleted))
                .and(UserSpecification.findByIsActive(isActive))
                ;
        return userMapper.toPageResponse(userRepository.findAll(spec,pageable));
    }

    @Override
    public void updateProfileImage(Long userId, String imageObjectName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setImage(imageObjectName);
        userRepository.save(user);
        log.info("Updated profile image for user {}: {}", userId, imageObjectName);
    }

    @Override
    public String getUserProfileImage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return user.getImage();
    }
}
