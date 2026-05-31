package com.borrowapp.common.exception;

import com.borrowapp.common.response.ApiResponse;
import com.borrowapp.common.response.ResponseUtil;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Advice tách riêng cho 2 exception "method not allowed" với HIGHEST_PRECEDENCE.
 * Phải tách khỏi GlobalExceptionHandler để đảm bảo Spring resolve đúng
 * (catch-all @ExceptionHandler(Exception.class) trong cùng class có thể
 * gây nhiễu khi resolve NoResourceFoundException trong Spring 6.2.x).
 *
 * Dùng signature ResponseUtil.error(HttpStatus, String) của project Tuan.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MethodNotAllowedAdvice {

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoResource(NoResourceFoundException ex) {
        return ResponseUtil.error(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex) {
        return ResponseUtil.error(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage());
    }
}
