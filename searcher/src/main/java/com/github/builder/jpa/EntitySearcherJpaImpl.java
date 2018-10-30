package com.github.builder.jpa;

import com.github.builder.CriteriaRequest;
import com.github.builder.EntitySearcher;
import com.github.builder.params.OrderFields;
import com.github.builder.util.jpa.JpaFetchModeModifier;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Transactional(readOnly = true)
@AllArgsConstructor
public class EntitySearcherJpaImpl extends JpaFetchModeModifier implements EntitySearcher {

    private final EntityManager entityManager;
    private final PredicateCreator predicateCreator;
    private final JoinType joinType;

    @Override
    public <T> List<T> getList(Class<T> forClass, CriteriaRequest request, Set<OrderFields> orderFields) {
        if (Objects.isNull(orderFields) || orderFields.isEmpty()) {
            entityManager.createQuery(createQuery(forClass, request)).getResultList();
        }
        return entityManager.createQuery(createQueryWithSorting(forClass, request, orderFields)).getResultList();

    }

    @Override
    public <T> Page<T> getPage(int pageNumber, int pageLength, Class<T> forClass, CriteriaRequest request, Set<OrderFields> orderFields) {
        if (Objects.isNull(orderFields) || orderFields.isEmpty()) {
            TypedQuery query = entityManager.createQuery(createQuery(forClass, request));

            return getPage(pageNumber, pageLength, query, forClass);
        }
        TypedQuery query = entityManager.createQuery(createQueryWithSorting(forClass, request, orderFields));
        return getPage(pageNumber, pageLength, query, forClass);
    }

    @Override
    public <T> T findEntity(Class<T> forClass, CriteriaRequest request, Set<OrderFields> orderFields) {
        if (Objects.isNull(orderFields) || orderFields.isEmpty()) {
            TypedQuery query = entityManager.createQuery(createQuery(forClass, request));
            Page<T> result = getPage(0, 1, query, forClass);
            if (!result.getContent().isEmpty())
                return result.getContent().get(0);
        }
        TypedQuery query = entityManager.createQuery(createQuery(forClass, request));
        Page<T> result = getPage(0, 1, query, forClass);
        if (!result.getContent().isEmpty())
            return result.getContent().get(0);
        else {
            log.info("entity not found for request : {}", request.toString());
            throw new EntityNotFoundException("entity with request not found request : ".concat(request.toString()));
        }
    }

    @Override
    public <T> List getForIn(Class<T> fromClass, String entityField, CriteriaRequest request) {
        throw new NotImplementedException();
    }

    @Override
    public <T> List<Map> getFields(Class<T> forClass, CriteriaRequest request, String... entityFields) {

        throw new UnsupportedOperationException("jpa not support return map result use hibernate implementation instead of jpa implementation");

    }

    @Override
    public <T> Page<Map> getPage(int pageNumber, int pageLength, Class<T> forClass, CriteriaRequest request, Set<OrderFields> orderFields, String... entityFields) {
        throw new UnsupportedOperationException("jpa not support return map result use hibernate implementation instead of jpa implementation");
    }


    private <T> CriteriaQuery<T> createQuery(Class<T> forClass, CriteriaRequest request) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(forClass);

        Root<T> root = query.from(forClass);

        //todo fix bug not working
//        changeFetchMode(forClass,joinType,root);

        Predicate[] predicates = predicateCreator.createPredicates(request, builder, root, query);
        query.where(predicates);

        return query;
    }

    private <T> CriteriaQuery<T> createQueryWithSorting(Class<T> forClass, CriteriaRequest request, Set<OrderFields> orderFields) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(forClass);
        Root<T> root = query.from(forClass);

        //todo fix bug not working
//        changeFetchMode(forClass,joinType,root);


        Predicate[] predicates = predicateCreator.createPredicates(request, builder, root, query);
        query.where(predicates);

        List<Order> orders = orderFields
                .stream()
                .map(orderField -> predicateCreator.addOrder(builder, root, orderField.getDirection(), orderField.getOrderField())).collect(Collectors.toList());
        query.orderBy(orders);

        return query;
    }

    private long total(Class forClass) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery query = builder.createQuery(forClass);
        Root root = query.from(forClass);
        Expression expression = builder.countDistinct(root);
        query.distinct(true);
        query.select(expression);
        Long count = (Long) entityManager.createQuery(query).getSingleResult();
        return count;
    }

    private <T> Page<T> getPage(int pageNumber, int pageLength, TypedQuery criteria, Class forClass) {
        criteria.setFirstResult(pageNumber * pageLength);
        criteria.setMaxResults(pageLength);
        List<T> response = criteria.getResultList();
        Long total = total(forClass);
        return new PageImpl<>(response, new PageRequest(pageNumber, pageLength), total);
    }
}
