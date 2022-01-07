package com.builder.hibernate;


import com.builder.CriteriaHelper;
import com.builder.CriteriaRequest;
import com.builder.exceptions.RequestFieldNotPresent;
import com.builder.params.DateQuery;
import com.builder.params.FieldsQuery;
import com.builder.params.FieldsQueryWrap;
import com.builder.params.OrderFields;
import com.builder.params.Query;
import com.builder.util.FetchModeModifier;
import com.builder.util.UtilClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.annotation.Validated;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.validation.Valid;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;


/**
 * implementation description
 * for like allowed types - integer, string,
 * for boolean types allowed only equal param , true / false
 */
@Validated
@Slf4j
@Transactional(readOnly = true)
@SuppressWarnings({"checkstyle:MissingSwitchDefault", "checkstyle:CyclomaticComplexity", "java:S2259", "java:S3776"})
public class CriteriaHelperImpl extends FetchModeModifier implements CriteriaHelper {

    private final EntityManager entityManager;

    public CriteriaHelperImpl(EntityManager entityManager) {
        Assert.notNull(entityManager, "entity manager can't be null");
        this.entityManager = entityManager;
    }

    @Override
    public final <T> Criteria buildCriteria(Class<T> forClass, @Valid CriteriaRequest request) {

        if (Objects.isNull(request)) {
            throw new IllegalArgumentException("Request shouldn't be empty");
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
                    .filter(fieldsQuery ->
                            !UtilClass.isEntityField(forClass, fieldsQuery.getProperty()))
                    .collect(Collectors.toSet()));
        }

//        requests for entities
        Set<FieldsQuery> queryForEntities = new HashSet<>();
        if (Objects.nonNull(request.getConditions())) {
            queryForEntities.addAll(request.getConditions().stream()
                    .filter(fieldsQuery ->
                            UtilClass.isEntityField(forClass, fieldsQuery.getProperty()))
                    .collect(Collectors.toSet()));
        }


        Set<DateQuery> dateQueries = new HashSet<>();
//        for date request not entities
        if (Objects.nonNull(request.getDateConditions())) {
            dateQueries.addAll(request.getDateConditions().stream()
                    .filter(dateQuery -> !UtilClass.isEntityField(forClass, dateQuery.getProperty())).collect(Collectors.toSet()));
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
    public final <T> Criteria buildCriteria(Class<T> forClass, CriteriaRequest request, @Valid Set<OrderFields> orderFields) {
        Criteria criteria = buildCriteria(forClass, request);
        orderFields.forEach(orderField -> {

            if (UtilClass.isEntityField(forClass, orderField.getOrderField())) {
                String alias = getAliasProperty(orderField.getOrderField());
                addOrder(criteria, orderField.getDirection(), alias);
            } else {
                addOrder(criteria, orderField.getDirection(), orderField.getOrderField());
            }

        });
        return criteria;
    }

    private <T> void buildForNonDate(Criteria criteria, Set<FieldsQuery> notDate, Class<T> forClass) {
        if (Objects.nonNull(notDate) && !notDate.isEmpty()) {
            notDate.forEach(fieldsQuery -> {
                try {
                    if (UtilClass.isEntityField(forClass, fieldsQuery.getProperty())) {
                        throw new IllegalArgumentException("not allowed entities field for property query:" + fieldsQuery.getProperty());
                    }

                    List<Criterion> criterionList = new ArrayList<>();
                    for (Object searchParam : fieldsQuery.getSearchCriteria()) {
                        criterionList.add(forNonDates(
                                new FieldsQueryWrap(fieldsQuery.getProperty(), searchParam, fieldsQuery.getCriteriaCondition(), fieldsQuery.getMatchMode()),
                                forClass, fieldsQuery.getProperty()));
                    }
                    Criterion[] req = criterionList.stream().toArray(Criterion[]::new);
                    criteria.add(Restrictions.or(req));

                } catch (final NoSuchFieldException | ClassNotFoundException e) {
                    throw new IllegalArgumentException("wrong request search field not found: " + fieldsQuery.getProperty());
                }
            });
        }
    }

    private <T> void buildForDate(Criteria criteria, Set<DateQuery> dateQueries, Class<T> forClass) {
        if (Objects.nonNull(dateQueries) && !dateQueries.isEmpty()) {
            dateQueries.forEach(dateQuery -> {
                if (UtilClass.isEntityField(forClass, dateQuery.getProperty())) {
                    throw new IllegalArgumentException("not allowed entities field for property query: " + dateQuery.getProperty());
                }
                criteria.add(forDateCriterion(dateQuery, forClass));
            });
        }

    }

    /**
     * in current class properties have conventions name of entity field "." field value
     *
     * @param criteria        criteria for which will added search to relative entities
     * @param entityCriterias wrapper for query
     * @param forClass
     */
    private <T> void buildForEntities(Criteria criteria, Set<FieldsQuery> entityCriterias, Set<DateQuery> dateQueries, Class<T> forClass) {
//        build for not date
        Map<String, String> aliasMap = new HashMap<>();

        if (Objects.nonNull(entityCriterias)) {
            entityCriterias.forEach(fieldsQuery -> {
                try {
                    String[] fields = fieldsQuery.getProperty().split("\\.");
                    checkAndAddCriteria(aliasMap, fieldsQuery, criteria, fields);
                    List<Criterion> criterionList = new ArrayList<>();
                    for (Object searchParam : fieldsQuery.getSearchCriteria()) {
                        String[] tmp = fieldsQuery.getProperty().split("\\.");
                        String alias = tmp[0];
                        String path = aliasMap.get(alias);

                        FieldsQueryWrap wrap =
                                new FieldsQueryWrap(fieldsQuery.getProperty(), searchParam, fieldsQuery.getCriteriaCondition(), fieldsQuery.getMatchMode());
                        if (Objects.isNull(path)) {
                            criterionList.add(forNonDates(wrap, forClass, wrap.getProperty()));
                        } else {
//                            if path present always last value be field

                            criterionList.add(forNonDates(wrap, forClass, path.concat(".").concat(tmp[1])));
                        }
                    }
                    Criterion[] req = criterionList.stream().toArray(Criterion[]::new);
                    criteria.add(Restrictions.or(req));
                } catch (final NoSuchFieldException | ClassNotFoundException e) {
                    log.warn("wrong request for search field, field not found {}", fieldsQuery);
                    throw new IllegalArgumentException("wrong request search field not found: " + fieldsQuery);
                } catch (final IndexOutOfBoundsException e) {
                    log.warn("for entities query search params should be via point cut {}", fieldsQuery);
                    throw new IllegalArgumentException("for entities query search params must be via point cut:" + fieldsQuery);
                }
            });
        }
//        build for date
        if (Objects.nonNull(dateQueries)) {
            dateQueries.forEach(fieldsQuery -> {
                try {
                    String[] fields = fieldsQuery.getProperty().split("\\.");
                    checkAndAddCriteria(aliasMap, fieldsQuery, criteria, fields);
                    criteria.add(forDateCriterion(fieldsQuery, forClass));
                } catch (final NoSuchFieldException e) {
                    log.info("wrong request search field not found :{}", fieldsQuery);
                    throw new IllegalArgumentException("wrong request search field not found:" + fieldsQuery);
                }
            });
        }

    }


    private Map<String, String> buildAliases(String[] tmp) {
        Map<String, String> aliasMap = new TreeMap<>(Comparator.comparingInt(o -> o.split("\\.").length));
        for (int i = 0; i < tmp.length - 1; i++) {
            if (i != 0) {
                StringBuilder builder = new StringBuilder();
                for (int s = 0; s < i + 1; s++) {
                    builder.append(tmp[s]);
                    if (s != i) {
                        builder.append(".");
                    }
                }
                aliasMap.put(builder.toString(), tmp[i]);
            } else {
                aliasMap.put(tmp[i], tmp[i]);
            }
        }
        return aliasMap;
    }

    private void checkAndAddCriteria(Map<String, String> aliasMap, Query fieldsQuery, Criteria criteria, String[] fields)
            throws NoSuchFieldException {
        Map<String, String> aliases = buildAliases(fields);

        aliases.forEach((absoluteField, alias) -> {
            if (Objects.isNull(aliasMap.get(alias))) {
                aliasMap.put(alias, absoluteField);
                criteria.createCriteria(absoluteField, alias, JoinType.LEFT_OUTER_JOIN);
                criteria.setFetchMode(alias, FetchMode.SELECT);
            }
        });

        String alias = fields[fields.length - 2];
//                            add alias to criteria
        String withAliasParam = alias.concat(".").concat(fields[fields.length - 1]);
//                            change to alias
        fieldsQuery.setProperty(withAliasParam);
    }

    private <T> Criterion forNonDates(@Valid FieldsQueryWrap query, Class<T> forClass, String path) throws NoSuchFieldException, ClassNotFoundException {
        switch (query.getCriteriaCondition()) {
            case EQUAL:
                return Restrictions.eq(query.getProperty(), query.getSearchCriteria());
            case LIKE:
                if (Objects.isNull(query.getMatchMode())) {
                    if (UtilClass.isNumber(forClass, path)) {
                        return likeForInt(forClass, query.getProperty(), query.getSearchCriteria(), true, query.getMatchMode());
                    }
                    return Restrictions.ilike(query.getProperty(), query.getSearchCriteria());
                } else if (Objects.nonNull(query.getMatchMode())) {
                    if (UtilClass.isNumber(forClass, path)) {
                        return likeForInt(forClass, query.getProperty(), query.getSearchCriteria(), true, query.getMatchMode());
                    }
                    return Restrictions.ilike(query.getProperty(), query.getSearchCriteria().toString(), query.getMatchMode());
                }
                break;
            case NOT_EQUAL:
                return Restrictions.ne(query.getProperty(), query.getSearchCriteria());
            case NOT_LIKE:
                if (Objects.isNull(query.getMatchMode())) {
                    if (UtilClass.isNumber(forClass, path)) {
                        return likeForInt(forClass, query.getProperty(), query.getSearchCriteria(), false, query.getMatchMode());
                    }
                    return Restrictions.not(Restrictions.ilike(query.getProperty(), query.getSearchCriteria()));

                } else if (Objects.nonNull(query.getMatchMode())) {
                    if (UtilClass.isNumber(forClass, path)) {
                        return likeForInt(forClass, query.getProperty(), query.getSearchCriteria(), false, query.getMatchMode());
                    }
                    return Restrictions.not(Restrictions.ilike(query.getProperty(), query.getSearchCriteria().toString(), query.getMatchMode()));
                }
                break;
            case LESS:
                return Restrictions.lt(query.getProperty(), query.getSearchCriteria());

            case MORE:
                return Restrictions.ge(query.getProperty(), query.getSearchCriteria());
            case IN:
                return Restrictions.in(query.getProperty(), Arrays.asList(query.getSearchCriteria()));
            case IS_NULL:
                return Restrictions.isNull(query.getProperty());
            case NOT_NULL:
                return Restrictions.isNotNull(query.getProperty());
        }
        throw new IllegalArgumentException("unknown condition for query");
    }

    private <T> Criterion forDateCriterion(@Valid DateQuery dateQuery, Class<T> forClass) {
        switch (dateQuery.getCriteriaCondition()) {
            case BETWEEN:
                if (Objects.isNull(dateQuery.getSecondSearchParam())) {
                    log.info("for condition BETWEEN second search param mustn't be null first param: {}, ", dateQuery.getProperty());
                    throw new IllegalArgumentException("for condition BETWEEN second search param mustn't be null");
                }

                return Restrictions.between(dateQuery.getProperty(), getSameDateType(forClass, dateQuery), getSameDateTypeSecondParam(forClass, dateQuery));
            case LESS:
                return Restrictions.lt(dateQuery.getProperty(), getSameDateType(forClass, dateQuery));
            case EQUAL:
                return Restrictions.eq(dateQuery.getProperty(), getSameDateType(forClass, dateQuery));
//            default more
            default:
                return Restrictions.ge(dateQuery.getProperty(), getSameDateType(forClass, dateQuery));

        }
    }

    private <T> Object getSameDateType(Class<T> forClass, DateQuery dateQuery) {

        Field field = getField(forClass, dateQuery.getProperty());
        if (field.getType().isAssignableFrom(ZonedDateTime.class)) {
            return dateQuery.getSearchParam().atStartOfDay(ZoneId.of("UTC"));
        }
        if (field.getType().isAssignableFrom(LocalDateTime.class)) {
            return dateQuery.getSearchParam().atStartOfDay();
        } else {
            return dateQuery.getSearchParam();
        }

    }

    private <T> Object getSameDateTypeSecondParam(Class<T> forClass, DateQuery dateQuery) {

        Field field = getField(forClass, dateQuery.getProperty());
        if (field.getType().isAssignableFrom(ZonedDateTime.class)) {
            return dateQuery.getSecondSearchParam().atStartOfDay(ZoneId.of("UTC"));
        }
        if (field.getType().isAssignableFrom(LocalDateTime.class)) {
            return dateQuery.getSecondSearchParam().atStartOfDay();
        } else {
            return dateQuery.getSecondSearchParam();
        }

    }

    private <T> Field getField(Class<T> forClass, String property) {
        Field field = null;

        if (UtilClass.isEntityField(forClass, property)) {
            Class<T> childClass = getChildClass(forClass, property.split("\\.")[0]);
            field = ReflectionUtils.findField(childClass, property.split("\\.")[1]);
        } else {
            field = ReflectionUtils.findField(forClass, property);
            if (Objects.isNull(property)) {
                throw new RequestFieldNotPresent("searched field not found :" + property);
            }
        }

        return field;
    }

    //    is like true build for lie else for not like
    private <T> Criterion likeForInt(Class<T> forClass, String property, Object value, boolean isLike, MatchMode matchMode)
            throws NoSuchFieldException {
        String operand = isLike ? Strings.EMPTY : " not";
        if (UtilClass.isEntityField(forClass, property)) {
            Class<T> child = getChildClass(forClass, property.split("\\.")[0]);
            Field field = child.getDeclaredField(property.split("\\.")[1]);
            return Restrictions.sqlRestriction(
                    "cast(" + field.getDeclaredAnnotation(Column.class).name() + " as text)" + operand + " like " + valueWithMatchMode(matchMode, value));
        }
        Field field = forClass.getDeclaredField(property);
        return Restrictions.sqlRestriction(
                "cast(" + field.getDeclaredAnnotation(Column.class).name() + " as text)" + operand + " like " + valueWithMatchMode(matchMode, value));
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
