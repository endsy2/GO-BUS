package com.busapp.userservice.exception;

import com.busapp.userservice.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("[EXCEPTION] NOT_FOUND - {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.builder()
                .endpoint(request.getRequestURI())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(
            DuplicateResourceException ex, HttpServletRequest request) {
        log.warn("[EXCEPTION] CONFLICT - {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.builder()
                .endpoint(request.getRequestURI())
                .status(HttpStatus.CONFLICT.value())
                .error("Conflict")
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            BadRequestException ex, HttpServletRequest request) {
        log.warn("[EXCEPTION] BAD_REQUEST - {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder()
                .endpoint(request.getRequestURI())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponse> handleFileStorage(
            FileStorageException ex, HttpServletRequest request) {
        log.error("[EXCEPTION] FILE_STORAGE_ERROR - {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder()
                .endpoint(request.getRequestURI())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("File Storage Error")
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("[EXCEPTION] VALIDATION_FAILED - {} {}: {}", request.getMethod(), request.getRequestURI(), message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder()
                .endpoint(request.getRequestURI())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message(message)
                .build());
    }

    @ExceptionHandler(QRGenerationException.class)
    public ResponseEntity<ErrorResponse> handleQRGeneration(
            QRGenerationException ex, HttpServletRequest request) {
        log.error("[EXCEPTION] QR_GENERATION_FAILED - {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder()
                .endpoint(request.getRequestURI())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("QR Generation Failed")
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(BakongApiException.class)
    public ResponseEntity<ErrorResponse> handleBakongApi(
            BakongApiException ex, HttpServletRequest request) {
        log.error("[EXCEPTION] BAKONG_API_ERROR - {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(ErrorResponse.builder()
                .endpoint(request.getRequestURI())
                .status(HttpStatus.BAD_GATEWAY.value())
                .error("Bakong API Error")
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(PaymentTimeoutException.class)
    public ResponseEntity<ErrorResponse> handlePaymentTimeout(
            PaymentTimeoutException ex, HttpServletRequest request) {
        log.warn("[EXCEPTION] PAYMENT_TIMEOUT - {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(ErrorResponse.builder()
                .endpoint(request.getRequestURI())
                .status(HttpStatus.REQUEST_TIMEOUT.value())
                .error("Payment Timeout")
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(TransactionCheckException.class)
    public ResponseEntity<ErrorResponse> handleTransactionCheck(
            TransactionCheckException ex, HttpServletRequest request) {
        // NOTE: was previously returning HTTP 500 with body status 404 — corrected to 500/500
        log.error("[EXCEPTION] TRANSACTION_CHECK_FAILED - {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder()
                .endpoint(request.getRequestURI())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Transaction Check Failed")
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(InvalidPaymentStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPaymentState(
            InvalidPaymentStateException ex, HttpServletRequest request) {
        log.warn("[EXCEPTION] INVALID_PAYMENT_STATE - {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.builder()
                .endpoint(request.getRequestURI())
                .status(HttpStatus.CONFLICT.value())
                .error("Invalid Payment State")
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(BakongPaymentException.class)
    public ResponseEntity<ErrorResponse> handleBakongPayment(
            BakongPaymentException ex, HttpServletRequest request) {
        log.error("[EXCEPTION] BAKONG_PAYMENT_ERROR - {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder()
                .endpoint(request.getRequestURI())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Payment Error")
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException ex, HttpServletRequest request) {
        log.warn("[EXCEPTION] FORBIDDEN - {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.builder()
                .endpoint(request.getRequestURI())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Forbidden")
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(WalletNotAuthenticatedException.class)
    public ResponseEntity<ErrorResponse> handleWalletNotAuthenticated(
            WalletNotAuthenticatedException ex, HttpServletRequest request) {
        log.warn("[EXCEPTION] WALLET_AUTH_REQUIRED - {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.builder()
                .endpoint(request.getRequestURI())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Wallet Authentication Required")
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex, HttpServletRequest request) {
        log.error("[EXCEPTION] UNHANDLED - {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder()
                .endpoint(request.getRequestURI())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message(ex.getMessage())
                .build());
    }
}
