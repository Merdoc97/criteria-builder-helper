package com.builder.params.annotations;

import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 *
 */
@Slf4j
public class DateFieldValidator implements ConstraintValidator<DateField, Object> {
    @Override
    public void initialize(DateField constraintAnnotation) {

    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (Objects.isNull(value)) {
            return true;
        }
        return tryForLocalDate(value) & tryForLocalDateTime(value) & tryForZoneDateTimeDateTime(value);
    }

    private boolean tryForLocalDate(Object value) {
        try {
            LocalDate localDate = LocalDate.parse(value.toString());
            if (Objects.nonNull(localDate)) {
                return true;
            }
        } catch (final Exception e) {
            log.debug("Exception in {} class in {} method with exception: ", this.getClass().getSimpleName(), "tryForLocalDate", e);
        }
        return false;
    }

    private boolean tryForLocalDateTime(Object value) {
        try {
            LocalDateTime localDate = LocalDateTime.parse(value.toString());
            if (Objects.nonNull(localDate)) {
                return true;
            }
        } catch (final Exception e) {
            log.debug("Exception in {} class in {} method with exception: ", this.getClass().getSimpleName(), "tryForLocalDateTime", e);
        }
        return false;
    }

    private boolean tryForZoneDateTimeDateTime(Object value) {
        try {
            ZonedDateTime localDate = ZonedDateTime.parse(value.toString());
            if (Objects.nonNull(localDate)) {
                return true;
            }
        } catch (final Exception e) {
            log.debug("Exception in {} class in {} method with exception: ", this.getClass().getSimpleName(), "tryForZoneDateTimeDateTime", e);
        }
        return false;
    }
}
