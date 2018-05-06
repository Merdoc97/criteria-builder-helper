package com.github.builder.fields_query_builder;

import com.github.builder.CriteriaRequest;
import com.github.builder.params.DateQuery;
import com.github.builder.params.FieldsQuery;

import java.util.HashSet;
import java.util.Set;

/**
 */
public class CriteriaRequestBuilder {
    private static final CriteriaRequestBuilder BUILDER = new CriteriaRequestBuilder();

    public static Builder getRequestBuilder() {
        return BUILDER.new Builder();
    }

    private CriteriaRequestBuilder() {
    }

    public class Builder {
        private Set<FieldsQuery> conditions;
        private Set<DateQuery> dateConditions;

        public Builder() {
            this.conditions = new HashSet<>();
            this.dateConditions = new HashSet<>();
        }

        public Builder addField(FieldsQuery fieldsQuery) {
            conditions.add(fieldsQuery);
            return this;
        }

        public Builder addFields(Set<FieldsQuery> fieldsQueries) {
            conditions.addAll(fieldsQueries);
            return this;
        }

        public Builder addDateQuery(DateQuery dateQuery) {
            dateConditions.add(dateQuery);
            return this;
        }

        public Builder addDatesQuery(Set<DateQuery> dateQueries) {
            dateConditions.addAll(dateQueries);
            return this;
        }

        public CriteriaRequest build() {
            return new CriteriaRequest(conditions, dateConditions);
        }
    }

}
