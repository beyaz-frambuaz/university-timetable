package com.shablii.timetable.constraints.validators;

import com.shablii.timetable.constraints.Date;

import javax.validation.*;
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
