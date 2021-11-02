package com.builder.params.annotations;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 */
@Constraint(validatedBy = { DateFieldValidator.class})
@Target({FIELD,ANNOTATION_TYPE,PARAMETER})
@Retention(RUNTIME)
public @interface DateField {
    String message() default "only date fields allowed";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
