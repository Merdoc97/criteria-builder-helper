package com.builder;


import com.builder.params.DateQuery;
import com.builder.params.FieldsQuery;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.Set;

/**
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Validated
public class CriteriaRequest {
    @Valid
    private Set<FieldsQuery>conditions;
    private Set<DateQuery>dateConditions;

    public CriteriaRequest() {
    }

    public CriteriaRequest(CriteriaRequest request) {
        this.conditions=new HashSet<>();
        this.dateConditions=new HashSet<>();
        request.getConditions().forEach(fieldsQuery -> {
            conditions.add(new FieldsQuery(fieldsQuery.getProperty(),fieldsQuery.getSearchCriteria(),fieldsQuery.getCriteriaCondition(),fieldsQuery.getMatchMode()));
        });
        request.getDateConditions().forEach(dateQuery -> {
            dateConditions.add(new DateQuery(dateQuery.getProperty(),dateQuery.getSearchParam(),dateQuery.getSecondSearchParam(),dateQuery.getCriteriaCondition()));
        });
    }

    public CriteriaRequest(Set<FieldsQuery> conditions, Set<DateQuery> dateConditions) {
        this.conditions = conditions;
        this.dateConditions = dateConditions;
    }
}
