package com.github.builder.params;


import com.github.builder.condition.CriteriaCondition;
import com.github.builder.params.annotations.NotDateField;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.criterion.MatchMode;

import javax.validation.constraints.NotNull;

/**
 * in current class allow to query for all params instead dates
 * if want to search by inner entity you should use comma syntax
 */
@Getter
@Setter
@EqualsAndHashCode
public class FieldsQuery {
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

    public FieldsQuery() {
    }

    public FieldsQuery(String property, Object searchCriteria, CriteriaCondition criteriaCondition, MatchMode matchMode) {
        this.property = property;
        this.searchCriteria = searchCriteria;
        this.criteriaCondition = criteriaCondition;
        this.matchMode = matchMode;
    }
}
