package com.travelplan.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {
    private List<T> data;
    private Pagination pagination;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pagination {
        private int page;
        private int pageSize;
        private long totalItems;
        private int totalPages;
    }

    public static <T> PaginatedResponse<T> of(List<T> data, int page, int pageSize, long totalItems) {
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        return PaginatedResponse.<T>builder()
                .data(data)
                .pagination(Pagination.builder()
                        .page(page)
                        .pageSize(pageSize)
                        .totalItems(totalItems)
                        .totalPages(totalPages)
                        .build())
                .build();
    }
}
