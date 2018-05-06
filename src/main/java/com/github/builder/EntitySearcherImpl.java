package com.github.builder;

import com.github.builder.params.OrderFields;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 */
@AllArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EntitySearcherImpl implements EntitySearcher {
    private final CriteriaHelper criteriaHelper;


    @Override
    public <T> List<T> searchByParams(Class<T> forClass, CriteriaRequest request, Set<OrderFields> orderFields) {
        if (Objects.isNull(orderFields) || orderFields.isEmpty()) {
            return criteriaHelper.buildCriteria(forClass, request).list();
        }
        return criteriaHelper.buildCriteria(forClass, request, orderFields).list();
    }

    @Override
    public <T> Page<T> searchByParamsWithPaging(int pageNumber, int pageLength, Class<T> forClass, CriteriaRequest request, Set<OrderFields> orderFields) {

        if (Objects.isNull(orderFields) || orderFields.isEmpty()) {
            Criteria criteria = criteriaHelper.buildCriteria(forClass, request);
            return getPage(pageNumber,pageLength,criteria,null,forClass);

        }
        //        copy the same for count query
        CriteriaRequest criteriaCount=new CriteriaRequest(request);
        Criteria criteria = criteriaHelper.buildCriteria(forClass, request, orderFields);
        Criteria forCount=criteriaHelper.buildCriteria(forClass, criteriaCount);
        return getPage(pageNumber,pageLength,criteria,forCount,forClass);
    }

    @Override
    public <T> T searchEntity(Class<T> forClass, CriteriaRequest request, Set<OrderFields> orderFields) {
        if (Objects.isNull(orderFields) || orderFields.isEmpty()) {
            Criteria criteria = criteriaHelper.buildCriteria(forClass, request);
            Page<T>result=getPage(0,1,criteria,null,forClass);
            if (!result.getContent().isEmpty())
                return result.getContent().get(0);
        }
        Criteria criteria = criteriaHelper.buildCriteria(forClass, request);
        Page<T>result=getPage(0,1,criteria,null,forClass);
        if (!result.getContent().isEmpty())
            return result.getContent().get(0);
        else {
            log.info("entity not found for request : {}",request.toString());
            throw new EntityNotFoundException("entity with request not found request : ".concat(request.toString()));
        }

    }

    private <T> Page<T> getPage(int pageNumber, int pageLength, Criteria criteria,Criteria withoutSorting,Class forClass) {
        criteria.setFirstResult(pageNumber * pageLength);
        criteria.setMaxResults(pageLength);
        List<T> response = criteria.list();
        Long total = null;
        ProjectionList projectionList = Projections.projectionList();
        projectionList.add(Projections.rowCount());
        if (Objects.nonNull(withoutSorting)){

            withoutSorting.setProjection(projectionList);
            withoutSorting.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
            total= (Long) withoutSorting.uniqueResult();
        }
        if (Objects.isNull(withoutSorting)){

            criteria.setProjection(projectionList);
            criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
            total= (Long) criteria.uniqueResult();
        }

        return new PageImpl<>(response, new PageRequest(pageNumber, pageLength), total);
    }

}
