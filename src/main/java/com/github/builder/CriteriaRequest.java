package com.github.builder;


import com.github.builder.params.DateQuery;
import com.github.builder.params.FieldsQuery;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import java.util.Set;

/**
 */
@Getter
@Setter
@EqualsAndHashCode
public class CriteriaRequest {
    @Valid
    private Set<FieldsQuery>conditions;
    private Set<DateQuery>dateConditions;

    public CriteriaRequest() {
    }

    public CriteriaRequest(Set<FieldsQuery> conditions, Set<DateQuery> dateConditions) {
        this.conditions = conditions;
        this.dateConditions = dateConditions;
    }
}
