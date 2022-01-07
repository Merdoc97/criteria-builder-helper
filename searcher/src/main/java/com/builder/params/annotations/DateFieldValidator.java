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

    private static final String EXCEPTION_IN_CLASS_IN_METHOD_WITH_EXCEPTION = "Exception in {} class in {} method with exception: ";

    @Override
    public void initialize(DateField constraintAnnotation) {
        throw new UnsupportedOperationException("initialize method not supported");
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (Objects.isNull(value)) {
            return true;
        }
        return tryForLocalDate(value) && tryForLocalDateTime(value) && tryForZoneDateTimeDateTime(value);
    }

    private boolean tryForLocalDate(Object value) {
        try {
            LocalDate localDate = LocalDate.parse(value.toString());
            if (Objects.nonNull(localDate)) {
                return true;
            }
        } catch (final Exception e) {
            log.debug(EXCEPTION_IN_CLASS_IN_METHOD_WITH_EXCEPTION, this.getClass().getSimpleName(), "tryForLocalDate", e);
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
            log.debug(EXCEPTION_IN_CLASS_IN_METHOD_WITH_EXCEPTION, this.getClass().getSimpleName(), "tryForLocalDateTime", e);
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
            log.debug(EXCEPTION_IN_CLASS_IN_METHOD_WITH_EXCEPTION, this.getClass().getSimpleName(), "tryForZoneDateTimeDateTime", e);
        }
        return false;
    }
}
