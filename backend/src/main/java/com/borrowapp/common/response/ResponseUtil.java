// com/borrowapp/common/response/ResponseUtil.java
package com.borrowapp.common.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseUtil {

    private ResponseUtil() {}

    // 200 OK + data
    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T data) {
        return ResponseEntity.ok(
            ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build()
        );
    }

    // 200 OK không có data (ví dụ: delete, logout)
    public static <T> ResponseEntity<ApiResponse<T>> success(String message) {
        return success(message, null);
    }

    // 201 Created
    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build()
        );
    }

    // Error với HTTP status tùy chỉnh
    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(
            ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .build()
        );
    }
}