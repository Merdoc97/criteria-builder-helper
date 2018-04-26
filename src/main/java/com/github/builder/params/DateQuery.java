package com.github.builder.params;


import com.github.builder.condition.CriteriaDateCondition;
import com.github.builder.params.annotations.DateField;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 *
 */
@Getter
@Setter
public class DateQuery {

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
