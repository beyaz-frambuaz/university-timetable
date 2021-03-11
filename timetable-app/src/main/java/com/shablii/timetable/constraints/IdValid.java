package com.shablii.timetable.constraints;

import com.shablii.timetable.constraints.validators.IdValidator;

import javax.validation.*;
import java.lang.annotation.*;

/**
 * Custom annotation to validate ID fields and parameters.
 * Same behavior as @Min(1), but with custom user-friendly message
 *
 * @author Taras Shablii
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IdValidator.class)
@Documented
public @interface IdValid {

    String message() default " ID cannot be less than 1";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Entity name to be prefixed to default message
     *
     * @return Entity name
     */
    String value() default "";

}
