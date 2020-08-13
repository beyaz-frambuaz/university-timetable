package com.foxminded.timetable.exceptions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Map;

@Getter
@ToString
@RequiredArgsConstructor
@JsonIgnoreProperties(
        { "cause", "stackTrace", "localizedMessage", "suppressed" })
public class ApiException extends RuntimeException {

    private final String message;
    private final Map<String, String> errors;

}
