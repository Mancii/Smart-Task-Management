package com.task.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class PagedResponse<T> extends BaseResponse<T> {
    private final int page;
    private final int size;
    private final long totalItems;
    private final int totalPages;

    private PagedResponse(String message, T data, Page<?> page) {
        super();
        setSuccess(true);
        setMessage(message);
        setData(data);
        setMeta(new Meta(
            page.getNumber() + 1, 
            page.getSize(), 
            page.getTotalElements(), 
            page.getTotalPages()
        ));
        this.page = page.getNumber() + 1; // Convert from 0-based to 1-based
        this.size = page.getSize();
        this.totalItems = page.getTotalElements();
        this.totalPages = page.getTotalPages();
    }

    public static <T> PagedResponse<T> of(Page<?> page, T data) {
        return new PagedResponse<>("Data retrieved successfully", data, page);
    }

    public static <T> PagedResponse<T> of(Page<?> page, T data, String message) {
        return new PagedResponse<>(message, data, page);
    }
}
