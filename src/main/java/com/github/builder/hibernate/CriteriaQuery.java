package com.github.builder.hibernate;


import com.github.builder.CriteriaHelper;
import com.github.builder.CriteriaRequest;
import com.github.builder.exceptions.RequestFieldNotPresent;
import com.github.builder.params.*;
import com.github.builder.util.FetchModeModifier;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.annotation.Validated;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.validation.Valid;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.builder.util.UtilClass.isEntityField;
import static com.github.builder.util.UtilClass.isNumber;


/**
 * implementation description
 * for like allowed types - integer, string,
 * for boolean types allowed only equal param , true / false
 */
@AllArgsConstructor
@Validated
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
                    .filter(fieldsQuery ->
                            !isEntityField(forClass, fieldsQuery.getProperty()))
                    .collect(Collectors.toSet()));
        }

//        requests for entities
        Set<FieldsQuery> queryForEntities = new HashSet<>();
        if (Objects.nonNull(request.getConditions())) {
            queryForEntities.addAll(request.getConditions().stream()
                    .filter(fieldsQuery ->
                            isEntityField(forClass, fieldsQuery.getProperty()))
                    .collect(Collectors.toSet()));
        }


        Set<DateQuery> dateQueries = new HashSet<>();
//        for date request not entities
        if (Objects.nonNull(request.getDateConditions())) {
            dateQueries.addAll(request.getDateConditions().stream()
                    .filter(dateQuery -> !isEntityField(forClass, dateQuery.getProperty())).collect(Collectors.toSet()));
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

            if (isEntityField(forClass, orderField.getOrderField())) {
                String alias = getAliasProperty(orderField.getOrderField());
                addOrder(criteria, orderField.getDirection(), alias);
            } else {
                addOrder(criteria, orderField.getDirection(), orderField.getOrderField());
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
                        criterionList.add(forNonDates(new FieldsQueryWrap(fieldsQuery.getProperty(), searchParam, fieldsQuery.getCriteriaCondition(), fieldsQuery.getMatchMode()), forClass));
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
                if (isEntityField(forClass, dateQuery.getProperty())) {
                    throw new IllegalArgumentException("not allowed entities field for property query: " + dateQuery.getProperty());
                }
                criteria.add(forDateCriterion(dateQuery, forClass));
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
                        criterionList.add(forNonDates(wrap, forClass));
                    }
                    Criterion[] req = criterionList.stream().toArray(Criterion[]::new);
                    criteria.add(Restrictions.or(req));
                } catch (NoSuchFieldException | ClassNotFoundException e) {
                    log.warn("wrong request for search field, field not found {}", fieldsQuery);
                    throw new IllegalArgumentException("wrong request search field not found: " + fieldsQuery);
                } catch (IndexOutOfBoundsException e) {
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
                    checkAndAddCriteria(aliasMap, forClass, fieldsQuery, criteria, fields);
                    criteria.add(forDateCriterion(fieldsQuery, forClass));
                } catch (NoSuchFieldException e) {
                    log.info("wrong request search field not found :{}", fieldsQuery);
                    throw new IllegalArgumentException("wrong request search field not found:" + fieldsQuery);
                }
            });
        }

    }


    public Map<String, String> buildAliases(String[] tmp) {
        Map<String, String> aliasMap = new TreeMap<>((o1, o2) -> o1.split("\\.").length - o2.split("\\.").length);
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

    private void checkAndAddCriteria(Map<String, String> aliasMap, Class forClass, Query fieldsQuery, Criteria criteria, String[] fields) throws NoSuchFieldException {
/*

        if (!isEntityField(forClass, fields[0])) {
            log.info("only entities field allowed for property query, param: {}", fieldsQuery.getProperty().split("\\.")[0]);
            throw new IllegalArgumentException("only entities field allowed for property query:" + fields[0]);
        }
*/
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
      /*if (Objects.isNull(aliases.get(fields[fields.length-1]))) {
            aliasMap.put(fields[0], alias);
            criteria.createCriteria(fields[0], alias, JoinType.LEFT_OUTER_JOIN);
            criteria.setFetchMode(fields[0], FetchMode.SELECT);
        }*/
        String withAliasParam = alias.concat(".").concat(fields[fields.length - 1]);
//                            change to alias
        fieldsQuery.setProperty(withAliasParam);
    }

    private Criterion forNonDates(@Valid FieldsQueryWrap query, Class forClass) throws NoSuchFieldException, ClassNotFoundException {
        switch (query.getCriteriaCondition()) {
            case EQUAL:
                return Restrictions.eq(query.getProperty(), query.getSearchCriteria());
            case LIKE:
                if (Objects.isNull(query.getMatchMode())) {
                    if (isNumber(forClass, query.getProperty())) {
                        return likeForInt(forClass, query.getProperty(), query.getSearchCriteria(), true, query.getMatchMode());
                    }
                    return Restrictions.ilike(query.getProperty(), query.getSearchCriteria());
                } else if (Objects.nonNull(query.getMatchMode())) {
                    if (isNumber(forClass, query.getProperty())) {
                        return likeForInt(forClass, query.getProperty(), query.getSearchCriteria(), true, query.getMatchMode());
                    }
                    return Restrictions.ilike(query.getProperty(), query.getSearchCriteria().toString(), query.getMatchMode());
                }
                break;
            case NOT_EQUAL:
                return Restrictions.ne(query.getProperty(), query.getSearchCriteria());
            case NOT_LIKE:
                if (Objects.isNull(query.getMatchMode())) {
                    if (isNumber(forClass, query.getProperty())) {
                        return likeForInt(forClass, query.getProperty(), query.getSearchCriteria(), false, query.getMatchMode());
                    }
                    return Restrictions.not(Restrictions.ilike(query.getProperty(), query.getSearchCriteria()));

                } else if (Objects.nonNull(query.getMatchMode())) {
                    if (isNumber(forClass, query.getProperty())) {
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

    private Criterion forDateCriterion(@Valid DateQuery dateQuery, Class forClass) {
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

    private Object getSameDateType(Class forClass, DateQuery dateQuery) {

        Field field = getField(forClass, dateQuery.getProperty());
        if (field.getType().isAssignableFrom(ZonedDateTime.class))
            return dateQuery.getSearchParam().atStartOfDay(ZoneId.of("UTC"));
        if (field.getType().isAssignableFrom(LocalDateTime.class))
            return dateQuery.getSearchParam().atStartOfDay();
        else return dateQuery.getSearchParam();

    }

    private Object getSameDateTypeSecondParam(Class forClass, DateQuery dateQuery) {

        Field field = getField(forClass, dateQuery.getProperty());
        if (field.getType().isAssignableFrom(ZonedDateTime.class))
            return dateQuery.getSecondSearchParam().atStartOfDay(ZoneId.of("UTC"));
        if (field.getType().isAssignableFrom(LocalDateTime.class))
            return dateQuery.getSecondSearchParam().atStartOfDay();
        else return dateQuery.getSecondSearchParam();

    }

    private Field getField(Class forClass, String property) {
        Field field = null;

        if (isEntityField(forClass, property)) {
            Class childClass = getChildClass(forClass, property.split("\\.")[0]);
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
    private Criterion likeForInt(Class forClass, String property, Object value, boolean isLike, MatchMode matchMode) throws NoSuchFieldException, ClassNotFoundException {
        String operand = isLike ? "" : " not";
        if (isEntityField(forClass, property)) {
            Class child = getChildClass(forClass, property.split("\\.")[0]);
            Field field = child.getDeclaredField(property.split("\\.")[1]);
            return Restrictions.sqlRestriction("cast(" + field.getDeclaredAnnotation(Column.class).name() + " as text)" + operand + " like " + valueWithMatchMode(matchMode, value));
        }
        Field field = forClass.getDeclaredField(property);
        return Restrictions.sqlRestriction("cast(" + field.getDeclaredAnnotation(Column.class).name() + " as text)" + operand + " like " + valueWithMatchMode(matchMode, value));
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
