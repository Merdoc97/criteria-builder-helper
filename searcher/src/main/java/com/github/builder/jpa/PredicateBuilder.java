package com.github.builder.jpa;

import com.github.builder.CriteriaRequest;
import com.github.builder.params.FieldsQuery;
import com.github.builder.params.FieldsQueryWrap;
import org.hibernate.criterion.MatchMode;
import org.springframework.data.jpa.domain.Specifications;

import javax.persistence.criteria.*;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PredicateBuilder {


    public Predicate[] builder(@Valid CriteriaRequest request,CriteriaBuilder criteriaBuilder,Root root,CriteriaQuery query) {

        List<Predicate>predicates=new ArrayList<>();

        query.distinct(true);
        request.getConditions().stream()
                .forEach(conditions -> {
                    conditions.getSearchCriteria().forEach(o -> {
                        criteriaBuilder.and(toPredicate(root, criteriaBuilder, new FieldsQueryWrap(conditions.getProperty(), o, conditions.getCriteriaCondition(), conditions.getMatchMode()))));
                    });
                });
        Predicate[]res=new Predicate[predicates.size()];
        res=predicates.toArray(res);
        return res;
    }

    private Predicate toPredicate(Root root, CriteriaBuilder builder, FieldsQueryWrap fieldsQuery) {
        switch (fieldsQuery.getCriteriaCondition()) {
            case LIKE:
                return builder.like(builder.lower(getFetch(root, fieldsQuery.getProperty()).as(String.class)), valueWithMatchMode(fieldsQuery.getMatchMode(), fieldsQuery.getSearchCriteria()));
            case EQUAL:
                return builder.and(builder.equal(getFetch(root, fieldsQuery.getProperty()), fieldsQuery.getSearchCriteria()));
            case MORE:
                return builder.and(builder.greaterThan(getFetch(root, fieldsQuery.getProperty()),  String.valueOf(fieldsQuery.getSearchCriteria())));
            case LESS:
                return builder.and(builder.lessThan(getFetch(root, fieldsQuery.getProperty()), String.valueOf(fieldsQuery.getSearchCriteria())));
            case IN:
                return builder.and(builder.in(getFetch(root, fieldsQuery.getProperty())));
            case IS_NULL:
                return builder.and(builder.isNull(getFetch(root, fieldsQuery.getProperty())));
            case NOT_LIKE:
                return builder.notLike(builder.lower(getFetch(root, fieldsQuery.getProperty()).as(String.class)), valueWithMatchMode(fieldsQuery.getMatchMode(), fieldsQuery.getSearchCriteria()));
            case NOT_NULL:
                return builder.and(builder.isNotNull(getFetch(root, fieldsQuery.getProperty())));
            case NOT_EQUAL:
                return builder.and(builder.notEqual(getFetch(root, fieldsQuery.getProperty()), fieldsQuery.getSearchCriteria()));
        }
        throw new IllegalArgumentException("unknown condition for query");
    }

    private Path getFetch(Root root, String property) {
        String[] tmp = property.split("\\.");
//        bug but for mvp it's ok
        if (tmp.length > 2) {
            Fetch path = root.fetch(tmp[0], JoinType.LEFT);
            for (int i = 1; i < tmp.length - 1; i++) {
                path = path.fetch(tmp[i], JoinType.LEFT);
            }

            return ((Path) path).get(tmp[tmp.length - 1]);
        }
        if (tmp.length>1)
        return ((Path) root.fetch(tmp[0], JoinType.LEFT)).get(tmp[1]);

        return root.get(tmp[0]);

    }

    private String valueWithMatchMode(MatchMode matchMode, Object value) {
        if (Objects.isNull(matchMode)) {
            return "" + value + "";
        }
        switch (matchMode) {
            case ANYWHERE:
                return "%".concat(String.valueOf(value)).concat("%");
            case END:
                return "" + value + "%";
            case EXACT:
                return "" + value + "";
            default:
                return "%" + value + "";
        }

    }

}
