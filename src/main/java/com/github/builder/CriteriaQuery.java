package com.github.builder;


import com.github.builder.params.DateQuery;
import com.github.builder.params.FieldsQuery;
import com.github.builder.params.FieldsQueryWrap;
import com.github.builder.params.OrderFields;
import com.github.builder.util.FetchModeModifier;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.persistence.EntityManager;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.builder.util.UtilClass.isNumber;

/**
 * implementation description
 * for like allowed types - integer, string,
 * for boolean types allowed only equal param , true / false
 */
@Validated
@AllArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CriteriaQuery extends FetchModeModifier implements CriteriaHelper {

    private final EntityManager entityManager;

    @Override
    public final Criteria buildCriteria(Class forClass, @Valid CriteriaRequest request) {

        if (Objects.isNull(request)) {
            log.warn("empty request {}", request);
            throw new IllegalArgumentException("request shouldn't be empty");
        }
        Session session = entityManager.unwrap(Session.class);
        session.setDefaultReadOnly(true);

        Criteria criteria = session.createCriteria(forClass);
//        change fetchMode for all entities and inner entities
        changeFetchMode(forClass, FetchMode.SELECT, criteria);


//        request for fields not entities
        Set<FieldsQuery> queryForNotEntity = new HashSet<>();
        if (Objects.nonNull(request.getConditions())) {
            queryForNotEntity.addAll(request.getConditions().stream()
                    .filter(fieldsQuery -> {
                        try {
                            return !isEntityField(forClass, fieldsQuery.getProperty());
                        } catch (NoSuchFieldException e) {
                            log.info("not correct field for request field not found {}", fieldsQuery.getProperty());
                            throw new IllegalArgumentException("wrong request search field not found:" + fieldsQuery.getProperty());
                        }
                    }).collect(Collectors.toSet()));
        }

//        requests for entities
        Set<FieldsQuery> queryForEntities = new HashSet<>();
        if (Objects.nonNull(request.getConditions())) {
            queryForEntities.addAll(request.getConditions());
            queryForEntities.removeAll(queryForNotEntity);
        }


        Set<DateQuery> dateQueries = new HashSet<>();
//        for date request not entities
        if (Objects.nonNull(request.getDateConditions())) {
            dateQueries.addAll(request.getDateConditions().stream()
                    .filter(dateQuery -> {
                        try {
                            return !isEntityField(forClass, dateQuery.getProperty());
                        } catch (NoSuchFieldException e) {
                            log.info("not correct field for request field not found {}", dateQuery.getProperty());
                            throw new IllegalArgumentException("wrong request search field not found:" + dateQuery.getProperty());
                        }
                    }).collect(Collectors.toSet()));
        }
        Set<DateQuery> dateQueriesForEntities = new HashSet<>();
        if (Objects.nonNull(request.getDateConditions())) {
            dateQueriesForEntities.addAll(request.getDateConditions());
            dateQueriesForEntities.removeAll(dateQueries);
        }
//        build for not dates
        buildForNonDate(criteria, queryForNotEntity, forClass);
        buildForDate(criteria, dateQueries, forClass);
        buildForEntities(criteria, queryForEntities, dateQueriesForEntities, forClass);

        return criteria;
    }

    @Override
    public final Criteria buildCriteria(Class forClass, CriteriaRequest request, @Valid Set<OrderFields> orderFields) {
        Criteria criteria = buildCriteria(forClass, request);
        orderFields.forEach(orderField -> {
            try {
                if (isEntityField(forClass, orderField.getOrderField())) {
                    String alias = getAliasProperty(orderField.getOrderField());
                    addOrder(criteria, orderField.getDirection(), alias);
                } else {
                    addOrder(criteria, orderField.getDirection(), orderField.getOrderField());
                }
            } catch (NoSuchFieldException e) {
                log.info("only entities field allowed for property query, param: {}", orderField.getOrderField().split("\\.")[0]);
                throw new IllegalArgumentException("only entities field allowed for property query");
            }
        });
        return criteria;
    }

    private void buildForNonDate(Criteria criteria, Set<FieldsQuery> notDate, Class forClass) {
        if (Objects.nonNull(notDate) && !notDate.isEmpty())
            notDate.forEach(fieldsQuery -> {
                try {
                    if (isEntityField(forClass, fieldsQuery.getProperty())) {
                        throw new IllegalArgumentException("not allowed entities field for property query:" + fieldsQuery.getProperty());
                    }

                    List<Criterion> criterionList = new ArrayList<>();
                    for (Object searchParam : fieldsQuery.getSearchCriteria()) {
                        criterionList.add(forNonDates(new FieldsQueryWrap(fieldsQuery.getProperty(), searchParam, fieldsQuery.getCriteriaCondition(), fieldsQuery.getMatchMode()), forClass, null));
                    }
                    Criterion[] req = criterionList.stream().toArray(Criterion[]::new);
                    criteria.add(Restrictions.or(req));

                } catch (NoSuchFieldException | ClassNotFoundException e) {
                    throw new IllegalArgumentException("wrong request search field not found: " + fieldsQuery.getProperty());
                }
            });
    }

    private void buildForDate(Criteria criteria, Set<DateQuery> dateQueries, Class forClass) {
        if (Objects.nonNull(dateQueries) && !dateQueries.isEmpty())
            dateQueries.forEach(dateQuery -> {
                try {
                    if (isEntityField(forClass, dateQuery.getProperty())) {
                        throw new IllegalArgumentException("not allowed entities field for property query: " + dateQuery.getProperty());
                    }

                    criteria.add(forDateCriterion(dateQuery));
                } catch (NoSuchFieldException e) {
                    throw new IllegalArgumentException("wrong request search field not found: " + dateQuery.getProperty());
                }

            });

    }

    /**
     * in current class properties have conventions name of entity field "." field value
     *
     * @param criteria        criteria for which will added search to relative entities
     * @param entityCriterias wrapper for query
     * @param forClass
     */
    private void buildForEntities(Criteria criteria, Set<FieldsQuery> entityCriterias, Set<DateQuery> dateQueries, Class forClass) {
//        build for not date
        Map<String, String> aliasMap = new HashMap<>();
        if (Objects.nonNull(entityCriterias)) {
            entityCriterias.forEach(fieldsQuery -> {
                try {
                    String[] fields = fieldsQuery.getProperty().split("\\.");
                    checkAndAddCriteria(aliasMap, forClass, fieldsQuery, criteria, fields);
                    List<Criterion> criterionList = new ArrayList<>();
                    for (Object searchParam : fieldsQuery.getSearchCriteria()) {
                        FieldsQueryWrap wrap = new FieldsQueryWrap(fieldsQuery.getProperty(), searchParam, fieldsQuery.getCriteriaCondition(), fieldsQuery.getMatchMode());
                        criterionList.add(forNonDates(wrap, forClass, fields[0]));
                    }
                    Criterion[] req = criterionList.stream().toArray(Criterion[]::new);
                    criteria.add(Restrictions.or(req));
                } catch (NoSuchFieldException | ClassNotFoundException e) {
                    log.warn("wrong request for search field, field not found {}", fieldsQuery);
                    throw new IllegalArgumentException("wrong request search field not found: " + fieldsQuery);
                } catch (IndexOutOfBoundsException e) {
                    log.warn("for entities query search params should be via point cut {}", fieldsQuery);
                    throw new IllegalArgumentException("for entities query search params should be via point cut:" + fieldsQuery);
                }
            });
        }
//        build for date
        if (Objects.nonNull(dateQueries)) {
            dateQueries.forEach(fieldsQuery -> {
                try {
                    String[] fields = fieldsQuery.getProperty().split("\\.");
                    checkAndAddCriteria(aliasMap, forClass, fieldsQuery, criteria, fields);
                    criteria.add(forDateCriterion(fieldsQuery));
                } catch (NoSuchFieldException e) {
                    log.info("wrong request search field not found :{}", fieldsQuery);
                    throw new IllegalArgumentException("wrong request search field not found:" + fieldsQuery);
                }
            });
        }

    }

    private void checkAndAddCriteria(Map<String, String> aliasMap, Class forClass, com.github.builder.params.Query fieldsQuery, Criteria criteria, String[] fields) throws NoSuchFieldException {
        if (!isEntityField(forClass, fields[0])) {
            log.info("only entities field allowed for property query, param: {}", fieldsQuery.getProperty().split("\\.")[0]);
            throw new IllegalArgumentException("only entities field allowed for property query:" + fields[0]);
        }

        String alias = fields[0];
//                            add alias to criteria
        if (Objects.isNull(aliasMap.get(fields[0]))) {
            aliasMap.put(fields[0], alias);
            criteria.createCriteria(fields[0], alias, JoinType.LEFT_OUTER_JOIN);
            criteria.setFetchMode(fields[0], FetchMode.SELECT);
        }
        String withAliasParam = alias.concat(".").concat(fields[1]);
//                            change to alias
        fieldsQuery.setProperty(withAliasParam);
    }

    private Criterion forNonDates(@Valid FieldsQueryWrap query, Class forClass, String path) throws NoSuchFieldException, ClassNotFoundException {
        switch (query.getCriteriaCondition()) {
            case EQUAL:
                return Restrictions.eq(query.getProperty(), query.getSearchCriteria());
            case LIKE:
                if (Objects.isNull(query.getMatchMode())) {
                    if (isNumber(forClass, query.getProperty(), path)) {
                        throw new IllegalArgumentException("like/not like not allowed for Number values current value:" + query.getSearchCriteria());
                    }
                    return Restrictions.ilike(query.getProperty(), query.getSearchCriteria());
                } else if (Objects.nonNull(query.getMatchMode())) {
                    if (isNumber(forClass, query.getProperty(), path)) {
                        throw new IllegalArgumentException("like/not like not allowed for Number values current value:" + query.getSearchCriteria());
                    }
                    return Restrictions.ilike(query.getProperty(), query.getSearchCriteria().toString(), query.getMatchMode());
                }
                break;
            case NOT_EQUAL:
                return Restrictions.ne(query.getProperty(), query.getSearchCriteria());
            case NOT_LIKE:
                if (Objects.isNull(query.getMatchMode())) {
                    if (isNumber(forClass, query.getProperty(), path)) {
                        throw new IllegalArgumentException("like/not like not allowed for Number values current value:" + query.getSearchCriteria());
                    }
                    return Restrictions.not(Restrictions.ilike(query.getProperty(), query.getSearchCriteria()));

                } else if (Objects.nonNull(query.getMatchMode())) {
                    if (isNumber(forClass, query.getProperty(), path)) {
                        throw new IllegalArgumentException("like/not like not allowed for Number values current value:" + query.getSearchCriteria());
                    }
                    return Restrictions.not(Restrictions.ilike(query.getProperty(), query.getSearchCriteria().toString(), query.getMatchMode()));
                }
                break;
            case LESS:
                return Restrictions.lt(query.getProperty(), query.getSearchCriteria());

            case MORE:
                return Restrictions.ge(query.getProperty(), query.getSearchCriteria());
            case IN:
                return Restrictions.in(query.getProperty(),query.getSearchCriteria());
        }
        throw new IllegalArgumentException("unknown condition for query");
    }

    private Criterion forDateCriterion(@Valid DateQuery dateQuery) {
        switch (dateQuery.getCriteriaCondition()) {
            case BETWEEN:
                if (Objects.isNull(dateQuery.getSecondSearchParam())) {
                    log.info("for condition BETWEEN second search param mustn't be null first param: {}, ", dateQuery.getProperty());
                    throw new IllegalArgumentException("for condition BETWEEN second search param mustn't be null");
                }
                return Restrictions.between(dateQuery.getProperty(), dateQuery.getSearchParam(), dateQuery.getSecondSearchParam());
            case LESS:
                return Restrictions.lt(dateQuery.getProperty(), dateQuery.getSearchParam());
            case EQUAL:
                return Restrictions.eq(dateQuery.getProperty(), dateQuery.getSearchParam());
//            default more
            default:
                return Restrictions.ge(dateQuery.getProperty(), dateQuery.getSearchParam());
        }
    }

    private String getAliasProperty(String searchParam) {
        String[] fields = searchParam.split("\\.");
        String alias = fields[0];
        return alias.concat(".").concat(fields[1]);
    }

    private void addOrder(Criteria criteria, Sort.Direction direction, String property) {
        switch (direction) {
            case ASC:
                criteria.addOrder(Order.asc(property));
                break;
            case DESC:
                criteria.addOrder(Order.desc(property));
                break;
        }
    }
}
