package com.foxminded.timetable.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.FORBIDDEN)
public class SessionExpiredException extends RuntimeException {

    private final Class context;

    public SessionExpiredException(Class context) {

        super();
        this.context = context;
    }

}
