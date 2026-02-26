package com.travelplan.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private T data;
    private Meta meta;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        private Instant timestamp;
        private String requestId;
    }

    public static <T> ApiResponse<T> success(T data, String requestId) {
        return ApiResponse.<T>builder()
                .data(data)
                .meta(Meta.builder()
                        .timestamp(Instant.now())
                        .requestId(requestId)
                        .build())
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .meta(Meta.builder()
                        .timestamp(Instant.now())
                        .requestId(message) // Reusing requestId for simple message for now, or could add an 'error'
                                            // field if schema allows
                        .build())
                .build();
    }
}
