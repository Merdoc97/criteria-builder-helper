package com.builder.hibernate;

import com.builder.CriteriaHelper;
import com.builder.CriteriaRequest;
import com.builder.EntitySearcher;
import com.builder.params.OrderFields;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static org.hibernate.criterion.CriteriaSpecification.ALIAS_TO_ENTITY_MAP;

/**
 *
 */

@Slf4j
@Transactional(readOnly = true)
@Validated
public class EntitySearcherImpl implements EntitySearcher {
    private final CriteriaHelper criteriaHelper;

    public EntitySearcherImpl(EntityManager entityManager) {
        Assert.notNull(entityManager, "entity manager can't be null");
        this.criteriaHelper = new CriteriaHelperImpl(entityManager);
    }

    @Override
    public <T> List<T> getList(Class<T> forClass, @Valid CriteriaRequest request, Set<OrderFields> orderFields) {
        if (Objects.isNull(orderFields) || orderFields.isEmpty()) {
            Criteria criteria = criteriaHelper.buildCriteria(forClass, request);
            criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
            return criteria.list();
        }
        Criteria criteria = criteriaHelper.buildCriteria(forClass, request, orderFields);
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

        return criteria.list();
    }

    @Override
    public <T> Page<T> getPage(int pageNumber, int pageLength, Class<T> forClass, @Valid CriteriaRequest request, Set<OrderFields> orderFields) {

        if (Objects.isNull(orderFields) || orderFields.isEmpty()) {
            Criteria criteria = criteriaHelper.buildCriteria(forClass, request);
            return getPage(pageNumber, pageLength, criteria, null);

        }
        //        copy the same for count query
        CriteriaRequest criteriaCount = new CriteriaRequest(request);
        Criteria criteria = criteriaHelper.buildCriteria(forClass, request, orderFields);
        Criteria forCount = criteriaHelper.buildCriteria(forClass, criteriaCount);
        return getPage(pageNumber, pageLength, criteria, forCount);
    }

    @Override
    public <T> T findEntity(Class<T> forClass, @Valid CriteriaRequest request, Set<OrderFields> orderFields) {
        if (Objects.isNull(orderFields) || orderFields.isEmpty()) {
            Criteria criteria = criteriaHelper.buildCriteria(forClass, request);
            Page<T> result = getPage(0, 1, criteria, null);
            if (!result.getContent().isEmpty())
                return result.getContent().get(0);
        }
        Criteria criteria = criteriaHelper.buildCriteria(forClass, request);
        Page<T> result = getPage(0, 1, criteria, null);
        if (!result.getContent().isEmpty())
            return result.getContent().get(0);
        else {
            log.info("entity not found for request : {}", request.toString());
            throw new EntityNotFoundException("entity with request not found request : ".concat(request.toString()));
        }

    }

    @Override
    public <T> List getForIn(Class<T> fromClass, String entityField, @Valid CriteriaRequest request) {
        Criteria criteria = criteriaHelper.buildCriteria(fromClass, request);

        criteria.setProjection(
                Projections.projectionList()
                        .add(Projections.distinct(Projections.property(entityField))));

        return criteria.list();
    }

    @Override
    public <T> List<Map> getFields(Class<T> fromClass, @Valid CriteriaRequest request, String... entityFields) {
        return getForMap(fromClass, request, null, entityFields).list();
    }


    @Override
    public <T> Page<Map> getPage(int pageNumber, int pageLength, Class<T> forClass, @Valid CriteriaRequest request, Set<OrderFields> orderFields, String... entityFields) {
        if (Objects.isNull(orderFields) || orderFields.isEmpty()) {
            Criteria criteria = getForMap(forClass, request, orderFields, entityFields);
            return getPage(pageNumber, pageLength, criteria, null);

        }
        //        copy the same for count query
        CriteriaRequest criteriaCount = new CriteriaRequest(request);
        Criteria criteria = getForMap(forClass, request, orderFields, entityFields);
        Criteria forCount = criteriaHelper.buildCriteria(forClass, criteriaCount);
        return getPage(pageNumber, pageLength, criteria, forCount);
    }


    private <T> Criteria getForMap(Class<T> fromClass, CriteriaRequest request, Set<OrderFields> orderFields, String... entityFields) {

        Criteria criteria = null;
        if (Objects.isNull(orderFields) || orderFields.isEmpty()) {
            criteria = criteriaHelper.buildCriteria(fromClass, request);
        } else {
            criteria = criteriaHelper.buildCriteria(fromClass, request, orderFields);
        }
        ProjectionList projectionList = Projections.projectionList();
        /*
         * add fields to projections set
         * */
        Stream.of(entityFields)
                .forEach(entityField -> projectionList.add(Projections.property(entityField), entityField));

        criteria.setProjection(projectionList);
        criteria.setResultTransformer(ALIAS_TO_ENTITY_MAP);
        return criteria;
    }

    private <T> Page<T> getPage(int pageNumber, int pageLength, Criteria criteria, Criteria withoutSorting) {
        criteria.setFirstResult(pageNumber * pageLength);
        criteria.setMaxResults(pageLength);
        List<T> response = criteria.list();
        Long total = null;
        ProjectionList projectionList = Projections.projectionList();
        projectionList.add(Projections.rowCount());
        if (Objects.nonNull(withoutSorting)) {

            withoutSorting.setProjection(projectionList);
            withoutSorting.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
            total = (Long) withoutSorting.uniqueResult();
        }

        if (Objects.isNull(withoutSorting)) {
            criteria.setProjection(projectionList);
            criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
            total = (Long) criteria.uniqueResult();
        }

        return new PageImpl<>(response, PageRequest.of(pageNumber, pageLength), total);
    }

}
