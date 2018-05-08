package com.github.builder.params;

import com.github.builder.condition.CriteriaCondition;
import com.github.builder.params.annotations.NotDateField;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.criterion.MatchMode;

import javax.validation.constraints.NotNull;

/**
 * current wrapper it representation of fieldsQuery with single searchParam
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class FieldsQueryWrap implements Query {
    /**
     * field name from entity
     */
    @NotNull
    @NotDateField
    private String property;
    /**
     * value for search
     */
    @NotNull
    @NotDateField
    private Object searchCriteria;
    /**
     * condition like,equal ...
     */
    @NotNull
    private CriteriaCondition criteriaCondition;
    /**
     * match mode for like condition
     */
    private MatchMode matchMode;

    public FieldsQueryWrap() {
    }

    public FieldsQueryWrap(String property, Object searchCriteria, CriteriaCondition criteriaCondition, MatchMode matchMode) {
        this.property = property;
        this.searchCriteria = searchCriteria;
        this.criteriaCondition = criteriaCondition;
        this.matchMode = matchMode;
    }
}
