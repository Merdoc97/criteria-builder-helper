package com.github.builder;

import com.github.builder.params.OrderFields;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;

/**
 */
public interface EntitySearcher {

    <T> List<T> getList(Class<T> forClass, CriteriaRequest request, Set<OrderFields> orderFields);

    <T> Page<T> getPage(int pageNumber, int pageLength, Class<T> forClass, CriteriaRequest request, Set<OrderFields> orderFields);

    <T> T findEntity(Class<T> forClass, CriteriaRequest request, Set<OrderFields> orderFields);

}
