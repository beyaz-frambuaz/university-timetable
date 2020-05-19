package com.foxminded.timetable.service.printer.assembler;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Assembler {

    private static final String PIPE   = "|";
    private static final String SPACE  = " ";
    private static final String EQUALS = "=";
    private static final String PLUS   = "+";
    private static final String HYPHEN = "-";

    public String assembleTable(List<ColumnWriter> columns) {

        return writeBorder(columns, HYPHEN, PLUS) + writeHeaders(columns)
                + writeBorder(columns, EQUALS, PIPE) + writeEveryLine(columns)
                + writeBorder(columns, HYPHEN, PLUS);
    }

    private String writeEveryLine(List<ColumnWriter> columns) {

        StringBuilder builder = new StringBuilder();
        int numberOfItems = columns.stream()
                .mapToInt(column -> column.getItems().size())
                .min()
                .orElse(0);
        for (int i = 0; i < numberOfItems; i++) {
            final int item = i;

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

        columns.forEach(column -> {
            String cell = String.format(" %-" + column.getWidth() + "s ", line)
                    .replace(SPACE, line);
            builder.append(cell).append(delimiter);
        });
        builder.append("\n");

        return builder.toString();
    }

}
