package com.github.builder;

import com.github.builder.params.OrderFields;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Projections;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 */
@AllArgsConstructor
@Slf4j
public class EntitySearcherImpl implements EntitySearcher{
    private final CriteriaHelper criteriaHelper;


    @Override
    public <T> List<T> searchByParams(Class<T> forClass, CriteriaRequest request, Set<OrderFields> orderFields) {
        if (Objects.isNull(orderFields)||orderFields.isEmpty()){
            return criteriaHelper.buildCriteria(forClass,request).list();
        }
        return criteriaHelper.buildCriteria(forClass,request,orderFields).list();
    }

    @Override
    public <T> Page<T> searchByParamsWithPaging(int pageNumber,int pageLength,Class<T> forClass, CriteriaRequest request, Set<OrderFields> orderFields) {
        if (Objects.isNull(orderFields)||orderFields.isEmpty()){
            Criteria criteria=criteriaHelper.buildCriteria(forClass,request);
            criteria.setFirstResult(pageNumber*pageLength);
            criteria.setMaxResults(pageLength);
            List<T>response=criteria.list();
            criteria.setProjection(Projections.rowCount());
            criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
            Long total= (Long) criteria.uniqueResult();
            return new PageImpl<T>(response,new PageRequest(pageNumber,pageLength),total);

        }
        Criteria criteria=criteriaHelper.buildCriteria(forClass,request,orderFields);
        criteria.setFirstResult(pageNumber*pageLength);
        criteria.setMaxResults(pageLength);
        List<T>response=criteria.list();
        criteria.setProjection(Projections.rowCount());
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        Long total= (Long) criteria.uniqueResult();
        return new PageImpl<T>(response,new PageRequest(pageNumber,pageLength),total);
    }

    @Override
    public <T> T searchEntity(Class<T> forClass, CriteriaRequest request, Set<OrderFields> orderFields) {
        return null;
    }
}
