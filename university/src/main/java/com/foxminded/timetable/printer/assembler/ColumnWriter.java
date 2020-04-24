package com.foxminded.timetable.printer.assembler;

import java.util.List;

import lombok.Data;

@Data
public class ColumnWriter {
    private final String title;
    private final List<String> items;
    private final int width;

    public ColumnWriter(String title, List<String> items) {
        this.title = title;
        this.items = items;
        this.width = calculateWidth();
    }

    private int calculateWidth() {
        int widestItem = items.stream().mapToInt(String::length).max()
                .orElse(0);
        int width = Math.max(widestItem, title.length());
        return width == 0 ? 1 : width;
    }
}
