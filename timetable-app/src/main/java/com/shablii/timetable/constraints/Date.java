package com.shablii.timetable.constraints;

import com.shablii.timetable.constraints.validators.DateValidator;

import javax.validation.*;
import java.lang.annotation.*;

/**
 * Custom annotation to validate Strings are NotNull and in LocalDate ISO
 * standard format "YYYY-MM-DD"
 *
 * @author Taras Shablii
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateValidator.class)
@Documented
public @interface Date {

    String message() default "Invalid date, must be in YYYY-MM-DD format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
