package com.github.builder.fields_query_builder;

import com.github.builder.condition.CriteriaCondition;
import com.github.builder.params.FieldsQuery;
import org.hibernate.criterion.MatchMode;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class FieldsQueryBuilder {

    private FieldsQueryBuilder (){}

    private static final FieldsQueryBuilder QUERY = new FieldsQueryBuilder();

    public static Builder getFieldsBuilder() {
        return QUERY.new Builder();
    }

    public class Builder {
        private Set<FieldsQuery> fieldsQueries;

        public Builder() {
            this.fieldsQueries = new HashSet<>();
        }

        public Builder addField(String property, Object searchCriteria, CriteriaCondition criteriaCondition, MatchMode matchMode) {
            fieldsQueries.add(new FieldsQuery(property, Arrays.asList(searchCriteria), criteriaCondition, matchMode));
            return this;
        }
        public Builder addField(String property, List<Object> searchCriteria, CriteriaCondition criteriaCondition, MatchMode matchMode) {
            fieldsQueries.add(new FieldsQuery(property, searchCriteria, criteriaCondition, matchMode));
            return this;
        }

        public Builder addField(FieldsQuery fieldsQuery){
            fieldsQueries.add(fieldsQuery);
            return this;
        }

        public FieldsQuery createFieldQuery(String property, Object searchCriteria, CriteriaCondition criteriaCondition, MatchMode matchMode) {
            return new FieldsQuery(property, Arrays.asList(searchCriteria), criteriaCondition, matchMode);
        }

        public Set<FieldsQuery> build() {
            return fieldsQueries;
        }
    }
}
