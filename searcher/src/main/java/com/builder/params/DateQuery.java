package com.builder.params;


import com.builder.condition.CriteriaDateCondition;
import com.builder.params.annotations.DateField;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 *
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class DateQuery implements Query {

    @NotNull
    private String property;

    @NotNull
    @DateField
    private LocalDate searchParam;

    @DateField
    private LocalDate secondSearchParam;

    @NotNull
    private CriteriaDateCondition criteriaCondition;


    public DateQuery() {
    }

    public DateQuery(String property, LocalDate searchParam, LocalDate secondSearchParam, CriteriaDateCondition criteriaCondition) {
        this.property = property;
        this.searchParam = searchParam;
        this.secondSearchParam = secondSearchParam;
        this.criteriaCondition = criteriaCondition;
    }
}
