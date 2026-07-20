package com.amha.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private List<T> records;
    private long current;
    private long page;
    private long size;
    private long total;

    public static <T> PageResult<T> of(List<T> records, long current, long size, long total) {
        return new PageResult<>(records, current, current, size, total);
    }
}
