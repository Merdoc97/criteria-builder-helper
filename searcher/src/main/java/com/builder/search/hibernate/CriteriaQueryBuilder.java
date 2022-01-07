package com.builder.search.hibernate;

import com.builder.CriteriaRequest;
import com.builder.params.OrderFields;

import javax.persistence.criteria.CriteriaQuery;
import javax.validation.Valid;
import java.util.Set;

public interface CriteriaQueryBuilder {
    /**
     * @param forClass - root entity class for request
     * @param request  - set of fields with predictable request which can include inner entity with point syntax
     * @return - build criteria for search request
     */
    <T> CriteriaQuery<T> buildCriteria(Class<T> forClass, @Valid CriteriaRequest request);

    /**
     * @param forClass    - root entity class for request
     * @param request     - set of fields with predictable request which can include inner entity with point syntax
     * @param orderFields - sorting fields
     * @return - build criteria for search request
     */
    <T> CriteriaQuery<T> buildCriteria(Class<T> forClass, CriteriaRequest request, @Valid Set<OrderFields> orderFields);
}
