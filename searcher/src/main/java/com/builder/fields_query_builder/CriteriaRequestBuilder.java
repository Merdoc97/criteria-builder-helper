package com.builder.fields_query_builder;


import com.builder.CriteriaRequest;
import com.builder.params.DateQuery;
import com.builder.params.FieldsQuery;
import lombok.experimental.UtilityClass;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
@UtilityClass
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public final class CriteriaRequestBuilder {

    public static Builder getRequestBuilder() {
        return new Builder();
    }

    public static class Builder {
        private final Set<FieldsQuery> conditions;
        private final Set<DateQuery> dateConditions;

        public Builder() {
            this.conditions = new HashSet<>();
            this.dateConditions = new HashSet<>();
        }

        public Builder addField(FieldsQuery fieldsQuery) {
            conditions.add(fieldsQuery);
            return this;
        }

        public Builder addFieldQuery(Set<FieldsQuery> fieldsQueries) {
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
