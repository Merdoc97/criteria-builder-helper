package com.github.builder;


import com.github.builder.params.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.*;
import org.hibernate.sql.JoinType;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import javax.persistence.*;
import javax.validation.Valid;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * implementation description
 * for like allowed types - integer, string,
 * for boolean types allowed only equal param , true / false
 */
@Validated
@AllArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CriteriaQuery implements CriteriaHelper {

    private final EntityManager entityManager;

    @Override
    public final Criteria buildCriteria(Class forClass, @Valid CriteriaRequest request) {
        if (Objects.isNull(request)) {
            log.warn("empty request {}", request);
            throw new IllegalArgumentException("request shouldn't be emty");
        }
        Session session = entityManager.unwrap(Session.class);
        session.setDefaultReadOnly(true);
        Criteria criteria = session.createCriteria(forClass);
//        request for fields not entities
        Set<FieldsQuery> queryForNotEntity = new HashSet<>();
        if (Objects.nonNull(request.getConditions())) {
            queryForNotEntity.addAll(request.getConditions().stream()
                    .filter(fieldsQuery -> {
                        try {
                            return !isEntityField(forClass, fieldsQuery.getProperty());
                        } catch (NoSuchFieldException e) {
                            log.info("not correct field for request field not found {}", fieldsQuery.getProperty());
                            throw new IllegalArgumentException("wrong request search field not found");
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
                            throw new IllegalArgumentException("wrong request search field not found");
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
                        throw new IllegalArgumentException("not allowed entities field for property query");
                    }
                    criteria.add(forNonDates(fieldsQuery, forClass, null));
                } catch (NoSuchFieldException | ClassNotFoundException e) {
                    throw new IllegalArgumentException("wrong request search field not found");
                }
            });
    }

    private void buildForDate(Criteria criteria, Set<DateQuery> dateQueries, Class forClass) {
        if (Objects.nonNull(dateQueries) && !dateQueries.isEmpty())
            dateQueries.forEach(dateQuery -> {
                try {
                    if (isEntityField(forClass, dateQuery.getProperty())) {
                        throw new IllegalArgumentException("not allowed entities field for property query");
                    }

                    criteria.add(forDateCriterion(dateQuery));
                } catch (NoSuchFieldException e) {
                    throw new IllegalArgumentException("wrong request search field not found");
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
                    criteria.add(forNonDates(fieldsQuery, forClass, fields[0]));

                } catch (NoSuchFieldException | ClassNotFoundException e) {
                    log.info("wrong request for search field, field not found {}", fieldsQuery);
                    throw new IllegalArgumentException("wrong request search field not found");
                } catch (IndexOutOfBoundsException e) {
                    log.info("for entities query search params should be via point cut {}", fieldsQuery);
                    throw new IllegalArgumentException("for entities query search params should be via point cut");
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
                    throw new IllegalArgumentException("wrong request search field not found");
                }
            });
        }

    }

    private void checkAndAddCriteria(Map<String, String> aliasMap, Class forClass, com.github.builder.params.Query fieldsQuery, Criteria criteria, String[] fields) throws NoSuchFieldException {
        if (!isEntityField(forClass, fields[0])) {
            log.info("only entities field allowed for property query, param: {}", fieldsQuery.getProperty().split("\\.")[0]);
            throw new IllegalArgumentException("only entities field allowed for property query");
        }

        String alias = fields[0] + "_1";
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

    private Criterion forNonDates(@Valid FieldsQuery query, Class forClass, String path) throws NoSuchFieldException, ClassNotFoundException {
        switch (query.getCriteriaCondition()) {
            case EQUAL:
                return Restrictions.eq(query.getProperty(), query.getSearchCriteria());
            case LIKE:
                if (Objects.isNull(query.getMatchMode())) {
                    if (isNumber(forClass, query.getProperty(), path)) {
                        return likeForInt(forClass.getDeclaredField(query.getProperty()), query.getSearchCriteria(), true, query.getMatchMode());
                    }
                    return Restrictions.ilike(query.getProperty(), query.getSearchCriteria());
                } else if (Objects.nonNull(query.getMatchMode())) {
                    if (isNumber(forClass, query.getProperty(), path)) {
                        return likeForInt(forClass.getDeclaredField(query.getProperty()), query.getSearchCriteria(), true, query.getMatchMode());
                    }
                    return Restrictions.ilike(query.getProperty(), query.getSearchCriteria().toString(), query.getMatchMode());
                }
                break;
            case NOT_EQUAL:
                return Restrictions.ne(query.getProperty(), query.getSearchCriteria());
            case NOT_LIKE:
                if (Objects.isNull(query.getMatchMode())) {
                    if (isNumber(forClass, query.getProperty(), path)) {
                        return likeForInt(forClass.getDeclaredField(query.getProperty()), query.getSearchCriteria(), false, query.getMatchMode());
                    }
                    return Restrictions.not(Restrictions.ilike(query.getProperty(), query.getSearchCriteria()));

                } else if (Objects.nonNull(query.getMatchMode())) {
                    if (isNumber(forClass, query.getProperty(), path)) {
                        return likeForInt(forClass.getDeclaredField(query.getProperty()), query.getSearchCriteria(), false, query.getMatchMode());
                    }
                    return Restrictions.not(Restrictions.ilike(query.getProperty(), query.getSearchCriteria().toString(), query.getMatchMode()));
                }
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

    /**
     * @param forClass
     * @param property
     * @param path     actual variable for entity relation
     * @return
     * @throws NoSuchFieldException
     */
    private boolean isNumber(Class forClass, String property, String path) throws NoSuchFieldException, ClassNotFoundException {
        Field field = null;

        if (Objects.nonNull(path) && path.length() > 0) {
            if (forClass.getDeclaredField(path).getType().isAssignableFrom(List.class) || forClass.getDeclaredField(path).getType().isAssignableFrom(Set.class)) {
                field = forClass.getClassLoader().
                        loadClass(((ParameterizedTypeImpl) forClass
                                .getDeclaredField(path)
                                .getGenericType())
                                .getActualTypeArguments()[0]
                                .getTypeName())
                        .getDeclaredField(property.split("\\.")[1]);
            } else {
                field = forClass.getDeclaredField(path)
//                    get class type for entity
                        .getType().getDeclaredField(property.split("\\.")[1]);
            }
        } else {
            field = forClass.getDeclaredField(property);
        }
        Class<?> type = field.getType();
        if (type.isAssignableFrom(Boolean.TYPE)) {
            throw new IllegalArgumentException("not allowed boolean type for like field:" + property);
        }
        return type.isAssignableFrom(Integer.class)
                || type.isAssignableFrom(Long.class)
                || type.isAssignableFrom(Double.class)
                || type.isAssignableFrom(Float.class);
    }

    //    is like true build for lie else for not like
    private Criterion likeForInt(Field field, Object value, boolean isLike, MatchMode matchMode) throws NoSuchFieldException {
        String operand = isLike ? "" : " not";
        return Restrictions.sqlRestriction("cast(" + field.getDeclaredAnnotation(Column.class).name() + " as text)" + operand + " like " + valueWithMatchMode(matchMode, value));

    }

    private boolean isEntityField(Class forClass, String property) throws NoSuchFieldException {
        String[] fields = property.split("\\.");
        if (fields.length == 2) {
            Field field = forClass.getDeclaredField(fields[0]);
            return field.isAnnotationPresent(OneToOne.class)
                    || field.isAnnotationPresent(ManyToMany.class)
                    || field.isAnnotationPresent(OneToMany.class)
                    || field.isAnnotationPresent(ManyToOne.class);
        } else {
            Field field = forClass.getDeclaredField(property);
            return field.isAnnotationPresent(OneToOne.class)
                    || field.isAnnotationPresent(ManyToMany.class)
                    || field.isAnnotationPresent(OneToMany.class)
                    || field.isAnnotationPresent(ManyToOne.class);

        }
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

    private String getAliasProperty(String searchParam) {
        String[] fields = searchParam.split("\\.");
        String alias = fields[0] + "_1";
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
