package com.builder.fields_query_builder;


import com.builder.condition.CriteriaDateCondition;
import com.builder.params.DateQuery;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public final class DatesQueryBuilder {

    private static final DatesQueryBuilder QUERY = new DatesQueryBuilder();

    private DatesQueryBuilder() {
    }

    public static Builder getDateBuilder() {
        return QUERY.new Builder();
    }

    public class Builder {
        private final Set<DateQuery> dateQueries;

        public Builder() {
            this.dateQueries = new HashSet<>();
        }

        public Builder addField(String property, LocalDate searchParam, LocalDate secondSearchParam, CriteriaDateCondition criteriaCondition) {
            dateQueries.add(new DateQuery(property, searchParam, secondSearchParam, criteriaCondition));
            return this;
        }

        public DateQuery createDateQuery(String property, LocalDate searchParam, LocalDate secondSearchParam, CriteriaDateCondition criteriaCondition) {
            return new DateQuery(property, searchParam, secondSearchParam, criteriaCondition);
        }

        public Set<DateQuery> build() {
            return dateQueries;
        }
    }
}
