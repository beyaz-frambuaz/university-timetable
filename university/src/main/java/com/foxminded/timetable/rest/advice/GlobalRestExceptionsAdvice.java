package com.foxminded.timetable.rest.advice;

import com.foxminded.timetable.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalRestExceptionsAdvice {

    @ExceptionHandler(MethodNotImplementedException.class)
    public ResponseEntity<ApiException> handleNotImplemented(
            MethodNotImplementedException e) {

        ApiException apiException =
                new ApiException(e.getMessage(), Collections.emptyMap());
        return new ResponseEntity<>(apiException, HttpStatus.NOT_IMPLEMENTED);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiException> handleConstraintViolation(
            ConstraintViolationException exception) {

        log.warn(exception.getMessage());
        Map<String, String> violations = exception.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage));
        ApiException apiException =
                new ApiException(exception.getMessage(), violations);
        return ResponseEntity.badRequest().body(apiException);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiException> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception) {

        log.warn(exception.getMessage());
        Map<String, String> errors = exception.getBindingResult()
                .getAllErrors()
                .stream()
                .collect(Collectors.toMap(
                        error -> ((FieldError) error).getField(),
                        DefaultMessageSourceResolvable::getDefaultMessage));
        ApiException apiException =
                new ApiException(exception.getMessage(), errors);
        return ResponseEntity.badRequest().body(apiException);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiException> handleApiException(
            ApiException exception) {

        log.warn(exception.toString());
        return ResponseEntity.badRequest().body(exception);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiException> handleNotFound(
            NotFoundException exception) {

        log.warn(exception.getMessage());
        Map<String, String> body =
                Collections.singletonMap("not found", exception.getMessage());
        ApiException apiException = new ApiException(
                "entity you were looking for could not be located with ID you "
                        + "provided", body);
        return new ResponseEntity<>(apiException, HttpStatus.NOT_FOUND);
    }

}
