package com.busapp.bookingservice.client;

import com.busapp.bookingservice.dto.response.ErrorResponse;
import com.busapp.bookingservice.exception.BadRequestException;
import com.busapp.bookingservice.exception.ResourceNotFoundException;
import com.busapp.bookingservice.exception.ServiceUnavailableException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * Decodes error responses from downstream services (user-service, bus-service)
 * and re-throws them as local exceptions so the GlobalExceptionHandler can
 * return a consistent error body to the caller.
 */
@Component
public class FeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus status = HttpStatus.resolve(response.status());
        String message = extractMessage(response, methodKey);

        if (status == null) {
            return new ServiceUnavailableException("Unexpected status " + response.status() + " from " + methodKey);
        }

        return switch (status) {
            case NOT_FOUND          -> new ResourceNotFoundException(message);
            case BAD_REQUEST        -> new BadRequestException(message);
            case CONFLICT,
                 UNPROCESSABLE_ENTITY -> new BadRequestException(message);
            default -> {
                if (status.is5xxServerError()) {
                    yield new ServiceUnavailableException(message);
                }
                yield defaultDecoder.decode(methodKey, response);
            }
        };
    }

    private String extractMessage(Response response, String methodKey) {
        if (response.body() == null) {
            return "Downstream service error from: " + methodKey;
        }
        try (InputStream body = response.body().asInputStream()) {
            ErrorResponse error = objectMapper.readValue(body, ErrorResponse.class);
            if (error.getMessage() != null && !error.getMessage().isBlank()) {
                return error.getMessage();
            }
            if (error.getError() != null && !error.getError().isBlank()) {
                return error.getError();
            }
        } catch (IOException ignored) {
            // body is not a known ErrorResponse shape — fall through
        }
        return "Downstream service error from: " + methodKey;
    }
}
