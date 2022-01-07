package com.builder.params.annotations;

import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * .
 */
@Slf4j
public class FieldForNotDateValidator implements ConstraintValidator<NotDateField, Object> {

    @Override
    public void initialize(NotDateField constraintAnnotation) {

    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        return tryForLocalDate(value) & tryForLocalDateTime(value) & tryForZoneDateTimeDateTime(value);
    }

    private boolean tryForLocalDate(Object value) {
        try {
            LocalDate localDate = LocalDate.parse(value.toString());
            if (Objects.nonNull(localDate)) {
                return false;
            }
        } catch (final Exception e) {
            log.debug("Exception in {} class in {} method with exception: {}", this.getClass().getSimpleName(), "tryForLocalDate", e);
        }
        return true;
    }

    private boolean tryForLocalDateTime(Object value) {
        try {
            LocalDateTime localDate = LocalDateTime.parse(value.toString());
            if (Objects.nonNull(localDate)) {
                return false;
            }
        } catch (final Exception e) {
            log.debug("Exception in {} class in {} method with exception: {}", this.getClass().getSimpleName(), "tryForLocalDateTime", e);
        }
        return true;
    }

    private boolean tryForZoneDateTimeDateTime(Object value) {
        try {
            ZonedDateTime localDate = ZonedDateTime.parse(value.toString());
            if (Objects.nonNull(localDate)) {
                return false;
            }
        } catch (final Exception e) {
            log.debug("Exception in {} class in {} method with exception: ", this.getClass().getSimpleName(), "tryForZoneDateTimeDateTime", e);
        }
        return true;
    }

}
