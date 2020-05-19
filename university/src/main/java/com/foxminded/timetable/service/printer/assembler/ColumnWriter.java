package com.foxminded.timetable.service.printer.assembler;

import lombok.Data;

import java.util.List;

@Data
public class ColumnWriter {

    private final String       title;
    private final List<String> items;
    private final int          width;

    public ColumnWriter(String title, List<String> items) {

        this.title = title;
        this.items = items;
        this.width = calculateWidth();
    }

    private int calculateWidth() {

        int widestItem = items.stream()
                .mapToInt(String::length)
                .max()
                .orElse(0);
        int columnWidth = Math.max(widestItem, title.length());
        return columnWidth == 0 ? 1 : columnWidth;
    }

}
