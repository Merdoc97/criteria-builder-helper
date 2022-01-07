package com.builder.jpa;

import com.builder.CriteriaRequest;
import com.builder.EntitySearcher;
import com.builder.exceptions.NotImplementedException;
import com.builder.exceptions.RequestFieldNotPresent;
import com.builder.params.OrderFields;
import com.builder.util.UtilClass;
import com.builder.util.jpa.JpaFetchModeModifier;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.query.criteria.internal.compile.CriteriaQueryTypeQueryAdapter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Transactional(readOnly = true)
@AllArgsConstructor
@Validated
@SuppressWarnings("java:S3740")
public class EntitySearcherJpaImpl extends JpaFetchModeModifier implements EntitySearcher {

    private static final int MAX_RESULT_FOR_IN = 10000;
    private final EntityManager entityManager;
    private final PredicateCreator predicateCreator;

    @Override
    public <T> List<T> getList(Class<T> forClass, @Valid CriteriaRequest request, Set<OrderFields> orderFields) {
        return entityManager.createQuery(createQuery(forClass, request, orderFields)).getResultList();
    }

    @Override
    public <T> Page<T> getPage(int pageNumber, int pageLength, Class<T> forClass, @Valid CriteriaRequest request, Set<OrderFields> orderFields) {
        TypedQuery<T> query = entityManager.createQuery(createQuery(forClass, request, orderFields));
        return getPage(pageNumber, pageLength, query, forClass);
    }

    @Override
    public <T> T findEntity(Class<T> forClass, @Valid CriteriaRequest request, Set<OrderFields> orderFields) {
        if (Objects.isNull(orderFields) || orderFields.isEmpty()) {
            TypedQuery<T> query = entityManager.createQuery(createQuery(forClass, request, null));
            Page<T> result = getPage(0, 1, query, forClass);
            if (!result.getContent().isEmpty()) {
                return result.getContent().get(0);
            }
        }
        TypedQuery<T> query = entityManager.createQuery(createQuery(forClass, request, null));
        Page<T> result = getPage(0, 1, query, forClass);
        if (!result.getContent().isEmpty()) {
            return result.getContent().get(0);
        } else {
            log.info("entity not found for request : {}", request.toString());
            throw new EntityNotFoundException("entity with request not found request : ".concat(request.toString()));
        }
    }

    @Override
    public <T> List getForIn(Class<T> forClass, String entityField, @Valid CriteriaRequest request) {
        TypedQuery<T> query = entityManager.createQuery(createQuery(forClass, request, null));

        ScrollableResults results = ((CriteriaQueryTypeQueryAdapter) query)
                .setMaxResults(MAX_RESULT_FOR_IN)
                .setCacheable(false)
                .scroll(ScrollMode.FORWARD_ONLY);

        List<Object> objects = new ArrayList<>();
        while (results.next()) {
            Object row = results.get(0);
            try {
                objects.add(UtilClass.getFieldValue(forClass, entityField, row));
            } catch (final IllegalAccessException e) {
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
    public <T> Page<Map> getPage(int pageNumber, int pageLength, Class<T> forClass, @Valid CriteriaRequest request, Set<OrderFields> orderFields,
                                 String... entityFields) {
        throw new UnsupportedOperationException("jpa not support return map result use hibernate implementation instead of jpa implementation");
    }

    @Override
    public <T> Specification<T> createSpecification(Class<T> forClass, CriteriaRequest request, Set<OrderFields> sort) {
        return Specification.where((root, criteriaQuery, criteriaBuilder) -> {
            return criteriaBuilder.and(predicateCreator.createPredicates(forClass, request, criteriaBuilder, root, criteriaQuery, sort));
        });
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
                    .map(orderField -> predicateCreator.addOrder(builder, root, orderField.getDirection(), orderField.getOrderField()))
                    .collect(Collectors.toList());
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
