package com.github.builder.jpa;

import com.github.builder.CriteriaRequest;
import com.github.builder.params.OrderFields;

import javax.persistence.criteria.CriteriaQuery;
import javax.validation.Valid;
import java.util.Set;

public interface CriteriaQueryBuilder {
    /**
     * @param forClass - root entity class for request
     * @param request  - set of fields with predictable request which can include inner entity with point syntax
     * @return - build criteria for search request
     */
    CriteriaQuery buildCriteria(Class forClass, @Valid CriteriaRequest request);

    /**
     *
     * @param forClass - root entity class for request
     * @param request - set of fields with predictable request which can include inner entity with point syntax
     * @param orderFields - sorting fields
     * @return - build criteria for search request
     */
    CriteriaQuery buildCriteria(Class forClass, CriteriaRequest request, @Valid Set<OrderFields> orderFields);
}