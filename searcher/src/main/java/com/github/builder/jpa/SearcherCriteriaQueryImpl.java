package com.github.builder.jpa;

import com.github.builder.CriteriaRequest;
import com.github.builder.params.FieldsQuery;
import com.github.builder.params.FieldsQueryWrap;
import com.github.builder.params.OrderFields;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.criterion.MatchMode;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import javax.validation.Valid;
import java.util.Objects;
import java.util.Set;

import static com.github.builder.util.UtilClass.isEntityField;

@AllArgsConstructor
@Transactional(readOnly = true)
@Slf4j
@Validated
public class SearcherCriteriaQueryImpl implements SearcherCriteriaQuery {

    private final EntityManager entityManager;

    @Override
    public CriteriaQuery buildCriteria(Class forClass, @Valid CriteriaRequest request) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery query = builder.createQuery(forClass);
        Root root = query.from(forClass);
        query.select(root);
        //        request for fields not entities
        request.getConditions().stream()
                .filter(fieldsQuery ->
                        !isEntityField(forClass, fieldsQuery.getProperty()))
                .forEach(fieldsQuery -> {

                });

        //        requests for entities
      /*  request.getConditions().stream()
                .filter(fieldsQuery ->
                        isEntityField(forClass, fieldsQuery.getProperty()))
                .forEach(fieldsQuery -> {
                    query.where(getPredicate(root,builder,dd),true));
                });
*/

        return query;

    }

    @Override
    public CriteriaQuery buildCriteria(Class forClass, @Valid CriteriaRequest request, Set<OrderFields> orderFields) {
        return null;
    }


    private Expression<Boolean> buildPredicatesForNonEntities(Root root, CriteriaBuilder builder, FieldsQuery query) {

        return null;
    }

    private Expression<Boolean> buildPredicatesForEntities(Root root, CriteriaBuilder builder, FieldsQuery entityQuery) {
        return null;
    }

    private Predicate getPredicate(Root root, CriteriaBuilder builder, FieldsQueryWrap query, boolean isEntity) {
        switch (query.getCriteriaCondition()) {
            case EQUAL:
                return null;
            case LIKE:
                if (isEntity) {
                    String[] tmp = query.getProperty().split("\\.");
                    return builder.like(builder.lower(getJoin(root, query).get(tmp[tmp.length - 1])), valueWithMatchMode(query.getMatchMode(), query.getSearchCriteria()));
                } else {

                }
                break;
            case NOT_EQUAL:
                return null;
            case NOT_LIKE:
                return null;
            case LESS:
                return null;

            case MORE:
                return null;
            case IN:
                return null;
            case IS_NULL:
                return null;
            case NOT_NULL:
                return null;
        }
        throw new IllegalArgumentException("unknown condition for query");
    }

    private Join getJoin(Root root, FieldsQueryWrap query) {
        String[] prop = query.getProperty().split("\\.");
        Join join = (Join) root.fetch(prop[0], JoinType.LEFT);
        for (int i = 1; i < prop.length - 1; i++) {
            join.get(prop[i]);
        }
        return join;
    }


    private String valueWithMatchMode(MatchMode matchMode, Object value) {
        if (Objects.isNull(matchMode)) {
            return "'" + value + "'";
        }
        switch (matchMode) {
            case ANYWHERE:
                return "'%" + value + "%'";
            case END:
                return "'" + value + "%'";
            case EXACT:
                return "'" + value + "'";
            default:
                return "'%" + value + "'";
        }

    }

}
