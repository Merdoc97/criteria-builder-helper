package com.github.builder.params.annotations;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**

 */

@Constraint(validatedBy = { FieldForNotDateValidator.class})
@Target({FIELD,ANNOTATION_TYPE,PARAMETER})
@Retention(RUNTIME)
public @interface NotDateField {
    String message() default "date field not allowed";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
