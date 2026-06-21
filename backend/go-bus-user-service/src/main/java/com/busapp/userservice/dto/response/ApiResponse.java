package com.busapp.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private Integer status;
    private String endpoint;
    private String message;
    private T data;

    public static <T> ApiResponse<T> of(String message, T data) {
        String uri = "";
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            uri = attrs.getRequest().getRequestURI();
        }
        return ApiResponse.<T>builder()
                .status(200)
                .endpoint(uri)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> of(Integer status, String message, T data) {
        String uri = "";
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            uri = attrs.getRequest().getRequestURI();
        }
        return ApiResponse.<T>builder()
                .status(status)
                .endpoint(uri)
                .message(message)
                .data(data)
                .build();
    }
}
