package com.foxminded.timetable.service.printer.assembler;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class Assembler {
    private static final String PIPE = "|";
    private static final String SPACE = " ";
    private static final String EQUALS = "=";
    private static final String PLUS = "+";
    private static final String HYPHEN = "-";

    public String assembleTable(List<ColumnWriter> columns) {
        StringBuilder builder = new StringBuilder();
        builder.append(writeBorder(columns, HYPHEN, PLUS));
        builder.append(writeHeaders(columns));
        builder.append(writeBorder(columns, EQUALS, PIPE));
        builder.append(writeEveryLine(columns));
        builder.append(writeBorder(columns, HYPHEN, PLUS));
        return builder.toString();
    }

    private String writeEveryLine(List<ColumnWriter> columns) {
        StringBuilder builder = new StringBuilder();
        int numberOfItems = columns.stream()
                .mapToInt(column -> column.getItems().size()).min().orElse(0);
        for (int i = 0; i < numberOfItems; i++) {
            final Integer item = Integer.valueOf(i);

            columns.forEach(column -> {
                String line = String.format(
                        "%2$s %1$-" + column.getWidth() + "s ",
                        column.getItems().get(item), PIPE);
                builder.append(line);
            });
            builder.append(String.format("%s%n", PIPE));
        }

        return builder.toString();
    }

    private String writeHeaders(List<ColumnWriter> columns) {
        StringBuilder builder = new StringBuilder();
        columns.stream()
                .map(column -> String.format(
                        "%2$s %1$-" + column.getWidth() + "s ",
                        column.getTitle(), PIPE))
                .forEach(builder::append);
        builder.append(String.format("%s%n", PIPE));

        return builder.toString();
    }

    private String writeBorder(List<ColumnWriter> columns, String line,
            String delimiter) {
        StringBuilder builder = new StringBuilder();
        builder.append(delimiter);

        columns.stream().forEach(column -> {
            String cell = String.format(" %-" + column.getWidth() + "s ", line)
                    .replace(SPACE, line);
            builder.append(cell + delimiter);
        });
        builder.append("\n");

        return builder.toString();
    }
}
