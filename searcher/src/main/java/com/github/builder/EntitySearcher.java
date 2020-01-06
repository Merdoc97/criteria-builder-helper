package com.github.builder;


import org.springframework.data.domain.Page;
import com.github.builder.params.OrderFields;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 */
public interface EntitySearcher {

    <T> List<T> getList(Class<T> forClass, CriteriaRequest request, Set<OrderFields> orderFields);

    <T> Page<T> getPage(int pageNumber, int pageLength, Class<T> forClass, CriteriaRequest request, Set<OrderFields> orderFields);

    <T> T findEntity(Class<T> forClass, CriteriaRequest request, Set<OrderFields> orderFields);

    /**
     *
     * @param fromClass - root class
     * @param entityField field which will be result of criteria for example list Of id for another iteration
     * @param request
     * @param <T>
     * @return
     */
    <T> List getForIn(Class<T> fromClass, String entityField, CriteriaRequest request) ;

    /**
     *
     * @param fromClass
     * @param request - request if needed
     * @param entityFields
     * @param <T> - root class from what is will be selected
     * @return - list of Map<RequestedField,Value>
     */
    <T> List<Map> getFields(Class<T> fromClass, CriteriaRequest request, String... entityFields);


    /**
     *
     * @param pageNumber
     * @param pageLength
     * @param forClass - root entity
     * @param request - search request if needed or else empty
     * @param orderFields - sort order {@link org.springframework.data.domain.Sort.Direction} for each field
     * @param entityFields
     * @param <T>
     * @return same as in method {@link #getFields(Class, CriteriaRequest, String...)} but in page view
     */
    <T> Page<Map> getPage(int pageNumber, int pageLength, Class<T> forClass, CriteriaRequest request, Set<OrderFields> orderFields,String... entityFields);


}
