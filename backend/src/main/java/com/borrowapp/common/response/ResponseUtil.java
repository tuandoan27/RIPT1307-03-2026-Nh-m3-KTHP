package com.borrowapp.common.response;

import org.springframework.data.domain.Page;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ResponseUtil {

    private ResponseUtil() {}

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("OK")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<Object> page(Page<T> page) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("items",    page.getContent());
        data.put("total",    page.getTotalElements());
        data.put("page",     page.getNumber());
        data.put("pageSize", page.getSize());
        return ApiResponse.builder()
                .success(true)
                .message("OK")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .build();
    }

    public static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .build();
    }
}
