package com.vidhuratech.jobs.jobs.dto;

import lombok.Data;
import java.util.List;

@Data
public class PageResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    // ✅ FIXED CONSTRUCTOR
    public PageResponse(List<T> content, int page, int totalPages, long totalElements) {
        this.content = content;
        this.page = page;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.size = content != null ? content.size() : 0; // optional but useful
    }

    // ✅ Default constructor (needed for flexibility)
    public PageResponse() {}
}