package com.github.builder.jpa;

import com.github.builder.CriteriaRequest;
import com.github.builder.EntitySearcher;
import com.github.builder.exceptions.NotImplementedException;
import com.github.builder.exceptions.RequestFieldNotPresent;
import com.github.builder.params.OrderFields;
import com.github.builder.util.UtilClass;
import com.github.builder.util.jpa.JpaFetchModeModifier;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.query.criteria.internal.compile.CriteriaQueryTypeQueryAdapter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Transactional(readOnly = true)
@AllArgsConstructor
@Validated
public class EntitySearcherJpaImpl extends JpaFetchModeModifier implements EntitySearcher {

    private final EntityManager entityManager;
    private final PredicateCreator predicateCreator;

    @Override
    public <T> List<T> getList(Class<T> forClass, @Valid CriteriaRequest request, Set<OrderFields> orderFields) {

        return entityManager.createQuery(createQuery(forClass, request, orderFields)).getResultList();
    }

    @Override
    public <T> Page<T> getPage(int pageNumber, int pageLength, Class<T> forClass, @Valid CriteriaRequest request, Set<OrderFields> orderFields) {
        TypedQuery query = entityManager.createQuery(createQuery(forClass, request, orderFields));
        return getPage(pageNumber, pageLength, query, forClass);
    }

    @Override
    public <T> T findEntity(Class<T> forClass, @Valid CriteriaRequest request, Set<OrderFields> orderFields) {
        if (Objects.isNull(orderFields) || orderFields.isEmpty()) {
            TypedQuery query = entityManager.createQuery(createQuery(forClass, request, null));
            Page<T> result = getPage(0, 1, query, forClass);
            if (!result.getContent().isEmpty())
                return result.getContent().get(0);
        }
        TypedQuery query = entityManager.createQuery(createQuery(forClass, request, null));
        Page<T> result = getPage(0, 1, query, forClass);
        if (!result.getContent().isEmpty())
            return result.getContent().get(0);
        else {
            log.info("entity not found for request : {}", request.toString());
            throw new EntityNotFoundException("entity with request not found request : ".concat(request.toString()));
        }
    }

    @Override
    public <T> List getForIn(Class<T> forClass, String entityField, @Valid CriteriaRequest request) {
        TypedQuery query = entityManager.createQuery(createQuery(forClass, request, null));

        ScrollableResults results = ((CriteriaQueryTypeQueryAdapter) query)
                .setMaxResults(10000)
                .setCacheable(false)
                .scroll(ScrollMode.FORWARD_ONLY);

        List<Object> objects = new ArrayList<>();
        while (results.next()) {
            Object row = results.get(0);
            try {
                objects.add(UtilClass.getFieldValue(forClass, entityField, row));
            } catch (IllegalAccessException e) {
                log.error("can't get field from entity please check is it field present");
                throw new RequestFieldNotPresent("can't get field from entity please check is it field present", e.getCause());
            }
        }
        results.close();
        return objects;
    }

    @Override
    public <T> List<Map> getFields(Class<T> forClass, @Valid CriteriaRequest request, String... entityFields) {
        throw new NotImplementedException();
    }

    @Override
    public <T> Page<Map> getPage(int pageNumber, int pageLength, Class<T> forClass, @Valid CriteriaRequest request, Set<OrderFields> orderFields, String... entityFields) {
        throw new UnsupportedOperationException("jpa not support return map result use hibernate implementation instead of jpa implementation");
    }


    private <T> CriteriaQuery<T> createQuery(Class<T> forClass, CriteriaRequest request, Set<OrderFields> orderFields) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(forClass);

        Root<T> root = query.from(forClass);
        Predicate[] predicates = predicateCreator.createPredicates(request, builder, root, query);
        query.where(predicates);
        if (Objects.nonNull(orderFields) && !orderFields.isEmpty()) {
            List<Order> orders = orderFields
                    .stream()
                    .map(orderField -> predicateCreator.addOrder(builder, root, orderField.getDirection(), orderField.getOrderField())).collect(Collectors.toList());
            query.orderBy(orders);
        }
        return query;
    }

    private long total(Class forClass) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery query = builder.createQuery(forClass);
        Root root = query.from(forClass);
        Expression<Long> expression = builder.countDistinct(root);
        query.distinct(true);
        query.select(expression);
        return (Long) entityManager.createQuery(query).getSingleResult();
    }

    private <T> Page<T> getPage(int pageNumber, int pageLength, TypedQuery criteria, Class forClass) {
        criteria.setFirstResult(pageNumber * pageLength);
        criteria.setMaxResults(pageLength);
        List<T> response = criteria.getResultList();
        Long total = total(forClass);
        return new PageImpl<>(response, PageRequest.of(pageNumber, pageLength), total);
    }
}
