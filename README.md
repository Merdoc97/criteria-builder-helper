# criteria-builder-helper
Current simple util allow you to build much faster dynamic query for Entity
# Example
```java
CriteriaRequest request = new CriteriaRequest();
request.setConditions(new HashSet<>(Arrays.asList(
                new FieldsQuery("articleTopic", "java", CriteriaCondition.LIKE, MatchMode.ANYWHERE),
                new FieldsQuery("isActive", true, CriteriaCondition.EQUAL, null),
                new FieldsQuery("isParsedToday", false, CriteriaCondition.EQUAL, null),
                new FieldsQuery("id", 1, CriteriaCondition.LIKE, MatchMode.START),
                new FieldsQuery("menuEntity.menuName", "general", CriteriaCondition.LIKE, MatchMode.ANYWHERE),
                new FieldsQuery("menuEntity.id", 1, CriteriaCondition.EQUAL, null),
                new FieldsQuery("bodyEntity.articleName", "Solving Java Issues", CriteriaCondition.LIKE, MatchMode.ANYWHERE),
                new FieldsQuery("bodyEntity.articleLink", "http://www.developer.com", CriteriaCondition.LIKE, MatchMode.START)
        )));
        request.setDateConditions(new HashSet<>(Arrays.asList(
                new DateQuery("bodyEntity.articleDate", LocalDate.parse("2017-03-17"), null, CriteriaDateCondition.EQUAL)
        )));       
Criteria criteria = helper.buildCriteria(NewsEntity.class, request);
```
Example explanation - sample code shows sql request for root element NewsEntity.class with conditions
after execution sql request will have form

```sql
select
        this_.news_pk as news_pk1_0_2_,
        this_.article_topic as article_2_0_2_,
        this_.is_active as is_activ3_0_2_,
        this_.is_parsed_today as is_parse4_0_2_,
        this_.top_name_fk as top_name5_0_2_,
        bodyentity2_.news_fk as news_fk5_1_4_,
        bodyentity2_.article_link as article_1_1_4_,
        bodyentity2_.article_link as article_1_1_0_,
        bodyentity2_.article_body as article_2_1_0_,
        bodyentity2_.article_date as article_3_1_0_,
        bodyentity2_.article_name as article_4_1_0_,
        bodyentity2_.news_fk as news_fk5_1_0_,
        menuentity1_.tpname_pk as tpname_p1_3_1_,
        menuentity1_.name as name2_3_1_ 
    from
        news this_ 
    left outer join
        news_body bodyentity2_ 
            on this_.news_pk=bodyentity2_.news_fk 
    left outer join
        top_name menuentity1_ 
            on this_.top_name_fk=menuentity1_.tpname_pk 
    where
        this_.article_topic ilike ? 
        and this_.is_parsed_today=? 
        and this_.is_active=? 
        and cast(news_pk as text) like '%1' 
        and menuentity1_.tpname_pk=? 
        and menuentity1_.name ilike ? 
        and bodyentity2_.article_name ilike ? 
        and bodyentity2_.article_link ilike ? 
        and bodyentity2_.article_date=?
```

it always use joinType JoinType.LEFT_OUTER_JOIN it means if you will create request for @OneToMany it returned only entity which satisfy request condition

Current library always work by "and and" principle

