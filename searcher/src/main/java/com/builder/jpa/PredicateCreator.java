package com.builder.jpa;

import com.builder.CriteriaRequest;
import com.builder.params.FieldsQueryWrap;
import com.builder.params.OrderFields;
import org.hibernate.criterion.MatchMode;
import org.springframework.data.domain.Sort;

import javax.persistence.criteria.*;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.builder.util.UtilClass.getIdField;

public class PredicateCreator {

    public Predicate[] createPredicates(CriteriaRequest request, CriteriaBuilder criteriaBuilder, Root root, CriteriaQuery query) {
        var predicates = request.getConditions().stream()
                .flatMap(conditions -> {
                    return conditions.getSearchCriteria().stream()
                            .map(o -> criteriaBuilder.and(toPredicate(root, criteriaBuilder, new FieldsQueryWrap(conditions.getProperty(), o, conditions.getCriteriaCondition(), conditions.getMatchMode()))))
                            .collect(Collectors.toList()).stream();
                }).collect(Collectors.toList());

        Predicate[] res = new Predicate[predicates.size()];
        res = predicates.toArray(res);
        return res;
    }

    public Predicate[] createPredicates(Class forClass, CriteriaRequest request, CriteriaBuilder criteriaBuilder, Root root, CriteriaQuery query, Set<OrderFields> orderFields) {
        var predicates = createPredicates(request, criteriaBuilder, root, query);
        addOrdersAndGroups(forClass, criteriaBuilder, root, query, orderFields);
        return predicates;
    }

    private void addOrdersAndGroups(Class forClass, CriteriaBuilder criteriaBuilder, Root root, CriteriaQuery query, Set<OrderFields> fields) {
        Set<OrderFields> orderFields = fields != null ? fields : Set.of();
        var orders = orderFields
                .stream()
                .map(orderField -> addOrder(criteriaBuilder, root, orderField.getDirection(), orderField.getOrderField()))
                .collect(Collectors.toList());

        var expressions = orderFields.stream()
                .map(orderField -> getFetch(root, orderField.getOrderField()))
                .collect(Collectors.toList());
        expressions.add(root.get(getIdField(forClass)));
        query.groupBy(expressions);
        query.orderBy(orders);
    }

    private Predicate toPredicate(Root root, CriteriaBuilder builder, FieldsQueryWrap fieldsQuery) {
        switch (fieldsQuery.getCriteriaCondition()) {
            case LIKE:
                return builder.like(builder.lower(getFetch(root, fieldsQuery.getProperty()).as(String.class)), valueWithMatchMode(fieldsQuery.getMatchMode(), fieldsQuery.getSearchCriteria()));
            case EQUAL:
                return builder.and(builder.equal(getFetch(root, fieldsQuery.getProperty()), fieldsQuery.getSearchCriteria()));
            case MORE:
                return builder.and(builder.greaterThan(getFetch(root, fieldsQuery.getProperty()), String.valueOf(fieldsQuery.getSearchCriteria())));
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

    private static Path getFetch(Root root, String property) {
        String[] tmp = property.split("\\.");
        if (tmp.length > 2) {

            Join path = root.join(tmp[0]);
            path.alias(tmp[0]);
            for (int i = 1; i < tmp.length - 1; i++) {
                path = path.join(tmp[i], JoinType.LEFT);
                path.alias(tmp[i]);
            }
            return path.get(tmp[tmp.length - 1]);
        }
        if (tmp.length > 1) {
            Path res = root.join(tmp[0], JoinType.LEFT);
            res.alias(tmp[0]);
            return res.get(tmp[1]);
        }

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

    public Order addOrder(CriteriaBuilder builder, Root root, Sort.Direction direction, String property) {
        switch (direction) {
            case ASC:
                return builder.asc(getFetch(root, property));
            default:
                return builder.desc(getFetch(root, property));

        }
    }

}
