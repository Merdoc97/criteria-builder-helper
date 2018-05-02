package com.github.builder.params;

import com.github.builder.condition.CriteriaCondition;
import com.github.builder.params.annotations.NotDateField;
import org.hibernate.criterion.MatchMode;

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 */
public class FieldsOrQuery {

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
    private Set<Object> searchCriteria;
    /**
     * condition like,equal ...
     */
    @NotNull
    private CriteriaCondition criteriaCondition;
    /**
     * match mode for like condition
     */
    private MatchMode matchMode;

    public FieldsOrQuery() {
    }

    public FieldsOrQuery(String property, Set<Object> searchCriteria, CriteriaCondition criteriaCondition, MatchMode matchMode) {
        this.property = property;
        this.searchCriteria = searchCriteria;
        this.criteriaCondition = criteriaCondition;
        this.matchMode = matchMode;
    }
}
