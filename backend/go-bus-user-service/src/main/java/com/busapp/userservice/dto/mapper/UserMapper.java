package com.busapp.userservice.dto.mapper;

import com.busapp.userservice.dto.admin.PermissionResponse;
import com.busapp.userservice.dto.admin.RoleResponse;
import com.busapp.userservice.dto.request.UserRequest;
import com.busapp.userservice.dto.response.UserBasicResponse;
import com.busapp.userservice.dto.response.UserResponseDetail;
import com.busapp.userservice.model.User;
import com.busapp.userservice.repository.RoleRepository;
import com.busapp.userservice.service.RolePermissionCacheService;
import com.busapp.userservice.service.UserService;
import com.busapp.userservice.util.MinioUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final MinioUtil minioUtil;
    private final RolePermissionCacheService rolePermissionCacheService;

    public UserResponseDetail toResponse(User user) {
        UserResponseDetail response = new UserResponseDetail();
        response.setId(user.getId());
        response.setUserName(user.getUserName());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setGoogleId(user.getGoogleId());
        response.setImage(user.getImage());
        response.setGender(user.getGender());
        response.setCreatedAt(user.getCreatedAt());
        response.setIsEmployee(user.getIsEmployee());
        response.setProfilePicture(minioUtil.getPresignedUrlWithTimeout(user.getImage(), 200));
        response.setIsDeleted(user.getIsDeleted());
        response.setIsActive(user.getActive());
        response.setIsWalletExist(user.getIsWalletExist());

        // Use cached role-permission mapping for fast response
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            Set<Long> roleIds = user.getRoles().stream()
                    .map(role -> role.getId())
                    .collect(Collectors.toSet());
            response.setRoles(rolePermissionCacheService.getRolesWithPermissions(roleIds));
        }

        return response;
    }

    public UserBasicResponse toBasicResponse(User user) {
        return UserBasicResponse.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }

    public List<UserBasicResponse> toBasicResponseList(List<User> users) {
        return users.stream()
                .map(this::toBasicResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponseDetail> toListResponse(List<User> users) {
        List<UserResponseDetail> response = new ArrayList<>();
        for (User user : users) {
            response.add(toResponse(user));
        }
        return response;
    }

    public Page<UserResponseDetail> toPageResponse(Page<User> users) {
        return users.map(this::toResponse);
    }

    public User toEntity(UserRequest request) {
        return User.builder()
                .roles(request.getRoleId()!=null?roleRepository.findById(request.getRoleId()).stream().collect(Collectors.toSet()) :null)
                .userName(request.getUserName())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .active(true)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .googleId(request.getGoogleId())
                .image(request.getImage())
                .gender(request.getGender())
                .isEmployee(request.getIsEmployee())
                .build();
    }
}

