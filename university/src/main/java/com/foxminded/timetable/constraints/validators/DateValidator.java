package com.foxminded.timetable.constraints.validators;

import com.foxminded.timetable.constraints.Date;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class DateValidator implements ConstraintValidator<Date, String> {

    public boolean isValid(String date, ConstraintValidatorContext context) {

        try {
            if (date == null) {
                return false;
            }
            LocalDate.parse(date);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

}
