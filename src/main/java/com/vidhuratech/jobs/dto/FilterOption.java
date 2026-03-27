package com.vidhuratech.jobs.dto;

public class FilterOption {
    private String name;
    private long count;

    public FilterOption(String name, long count) {
        this.name = name;
        this.count = count;
    }

    public String getName() { return name; }
    public long getCount() { return count; }
}