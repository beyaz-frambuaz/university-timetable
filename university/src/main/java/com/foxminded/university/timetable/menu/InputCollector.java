package com.foxminded.university.timetable.menu;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Random;
import java.util.Scanner;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InputCollector {
    private static final String NUMBER = "^\\d+$";
    private static final String DATE_PATTERN = "^\\d{4}-\\d{2}-\\d{2}$";
    private static final String[] WRONG_FORMAT = {
            "Are you sure? Input seems off",
            "You gotta be kidding me! Try again",
            "Testing my nerves? We can do this all day long", "WRONG!",
            "One of these days machines will rise "
                    + "and users like you will be the first to go...",
            "Sure, then I'm Suzy. Try again, human",
            "Getting bored with your wrong inputs, I guess I'll take a nap",
            "Oh for the love of gods! Would you just cooperate?" };
    private final Scanner scanner;

    public int requestOption(int min, int max) {
        System.out.println(String.format("Your choice (%d-%d):", min, max));
        String choice = scanner.next();
        while (!choice.matches(NUMBER) || Integer.parseInt(choice) < min
                || Integer.parseInt(choice) > max) {
            System.out.println(getWrongFormatMessage());
            choice = scanner.next();
        }
        return Integer.parseInt(choice);
    }

    public LocalDate requestDate(LocalDate start, LocalDate end) {
        System.out.println(String.format(
                "Enter date in range %s - %s (supported format YYYY-MM-DD):",
                start, end));
        String input = scanner.next();
        while (isInvalidDate(input) || LocalDate.parse(input).isBefore(start)
                || LocalDate.parse(input).isAfter(end)) {
            System.out.println(getWrongFormatMessage());
            input = scanner.next();
        }
        return LocalDate.parse(input);
    }

    private boolean isInvalidDate(String input) {
        if (!input.matches(DATE_PATTERN)) {
            return true;
        }
        try {
            LocalDate.parse(input);
            return false;
        } catch (DateTimeParseException e) {
            return true;
        }
    }

    private String getWrongFormatMessage() {
        Random random = new Random();
        return WRONG_FORMAT[random.nextInt(8)];
    }
}
