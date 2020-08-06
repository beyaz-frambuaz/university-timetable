package com.foxminded.timetable.exceptions;

import lombok.*;

import java.util.Map;

@Getter
@ToString
@RequiredArgsConstructor
public class ApiException extends RuntimeException {

    private final String message;
    private final Map<String, String> errors;

}
