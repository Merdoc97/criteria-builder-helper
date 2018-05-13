package com.github.builder;


import org.hibernate.Criteria;
import com.github.builder.params.OrderFields;

import javax.validation.Valid;
import java.util.Set;

/**
 * helper to build dynamic query for ALL TO ALL SEARCH with n+1 strategy
 * -- restrictions like doesn't work for numbers if you wan't use
 */
public interface CriteriaHelper {

    /**
     * @param forClass - root entity class for request
     * @param request  - set of fields with predictable request which can include inner entity with point syntax
     * @return - build criteria for search request
     */
    Criteria buildCriteria(Class forClass, @Valid CriteriaRequest request);

    /**
     *
     * @param forClass - root entity class for request
     * @param request - set of fields with predictable request which can include inner entity with point syntax
     * @param orderFields - sorting fields
     * @return - build criteria for search request
     */
    Criteria buildCriteria(Class forClass, CriteriaRequest request, @Valid Set<OrderFields> orderFields);
}
