package com.foxminded.timetable.service.menu;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.foxminded.timetable.service.menu.InputCollector;

@TestInstance(Lifecycle.PER_CLASS)
public class InputCollectorTest {

    private ByteArrayOutputStream byteArrayOutputStream;
    private PrintStream printStream;

    private final String[] expectedErrorMessage = {
            "Are you sure? Input seems off",
            "You gotta be kidding me! Try again",
            "Testing my nerves? We can do this all day long", "WRONG!",
            "One of these days machines will rise "
                    + "and users like you will be the first to go...",
            "Sure, then I'm Suzy. Try again, human",
            "Getting bored with your wrong inputs, I guess I'll take a nap",
            "Oh for the love of gods! Would you just cooperate?" };

    @BeforeAll
    private void setUpOutStream() {
        this.byteArrayOutputStream = new ByteArrayOutputStream();
        this.printStream = new PrintStream(byteArrayOutputStream);
        System.setOut(printStream);
    }

    @BeforeEach
    private void setUp() {
        byteArrayOutputStream.reset();
    }

    @AfterAll
    private void restoreStream() {
        System.setOut(System.out);
    }

    @ParameterizedTest
    @ValueSource(strings = { "-1\n1", "f\n02", "foo\n003", "foo bar\n4", "0\n5",
            "6\n005" })
    void requestOptionShouldOnlyAcceptInputWithinLimits(String input) {

        int minOption = 1;
        int maxOption = 5;
        int acceptedInput;

        try (Scanner scanner = new Scanner(input)) {
            scanner.useDelimiter("\\n");
            InputCollector menuBot = new InputCollector(scanner);
            acceptedInput = menuBot.requestOption(minOption, maxOption);
        }
        String actualErrorMessage = byteArrayOutputStream.toString();

        boolean firstInputWasInvalid = Arrays.stream(expectedErrorMessage)
                .anyMatch(expectedMessage -> actualErrorMessage
                        .contains(expectedMessage));

        assertThat(firstInputWasInvalid).isTrue();
        assertThat(acceptedInput).isBetween(minOption, maxOption);
    }

    @ParameterizedTest
    @ValueSource(strings = { "-1\n1", "foo\n02", "5\n003", "foo bar\n4", "0\n7",
            "6\n009" })
    void requestIdShouldOnlyAcceptInputMatchingIdList(String input) {
        
        List<Long> ids = Arrays.asList(1L, 2L, 3L, 4L, 7L, 9L);
        long acceptedInput;
        
        try (Scanner scanner = new Scanner(input)) {
            scanner.useDelimiter("\\n");
            InputCollector menuBot = new InputCollector(scanner);
            acceptedInput = menuBot.requestId(ids);
        }
        String actualErrorMessage = byteArrayOutputStream.toString();

        boolean firstInputWasInvalid = Arrays.stream(expectedErrorMessage)
                .anyMatch(expectedMessage -> actualErrorMessage
                        .contains(expectedMessage));

        assertThat(firstInputWasInvalid).isTrue();
        assertThat(acceptedInput).isIn(ids);
    }

    @ParameterizedTest
    @ValueSource(strings = { "-1\n2020-02-01", "foo\n2020-03-01",
            "5\n2020-02-23", "2020-02-30\n2020-02-29", "9999-99-99\n2020-02-13",
            "2020-01-31\n2020-02-02", "2020-03-02\n2020-02-18" })
    void requestDateShouldOnlyAcceptInputWithinRangeOfDates(String input) {
        
        LocalDate start = LocalDate.parse("2020-02-01");
        LocalDate end = LocalDate.parse("2020-03-01");
        LocalDate acceptedInput;
        
        try (Scanner scanner = new Scanner(input)) {
            scanner.useDelimiter("\\n");
            InputCollector menuBot = new InputCollector(scanner);
            acceptedInput = menuBot.requestDate(start, end);
        }
        String errorMessage = byteArrayOutputStream.toString();

        boolean firstInputWasInvalid = Arrays.stream(expectedErrorMessage)
                .anyMatch(expectedMessage -> errorMessage
                        .contains(expectedMessage));

        assertThat(firstInputWasInvalid).isTrue();
        assertThat(acceptedInput).isBetween(start, end);
    }

}
