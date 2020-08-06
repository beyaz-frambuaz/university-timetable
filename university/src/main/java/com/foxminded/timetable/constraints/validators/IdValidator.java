package com.foxminded.timetable.constraints.validators;

import com.foxminded.timetable.constraints.IdValid;

import javax.validation.*;

public class IdValidator implements ConstraintValidator<IdValid, Long> {

    private String value;

    @Override
    public void initialize(IdValid annotation) {

        this.value = annotation.value();
    }

    public boolean isValid(Long id, ConstraintValidatorContext context) {

        if (id == null) {
            return true;
        }

        boolean isValid = id > 0;
        if (!isValid) {
            context.disableDefaultConstraintViolation();
            String message = context.getDefaultConstraintMessageTemplate();
            context.buildConstraintViolationWithTemplate(value + message)
                    .addConstraintViolation();
        }
        return isValid;
    }

}
