package com.github.tests;

import com.github.builder.CriteriaHelper;
import com.github.builder.CriteriaRequest;
import com.github.builder.condition.CriteriaCondition;
import com.github.builder.condition.CriteriaDateCondition;
import com.github.builder.params.DateQuery;
import com.github.builder.params.FieldsQuery;
import com.github.test.model.config.TestConfig;
import com.github.test.model.model.NewsBodyEntity;
import com.github.test.model.model.NewsEntity;
import com.github.test.model.model.NewsParseRule;
import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**

 */
@Transactional
public class TestCriteria extends TestConfig {

    @Autowired
    private CriteriaHelper helper;

    @Test
    public void simpleQueryForBuilder() {

        CriteriaRequest request = new CriteriaRequest();
        request.setConditions(new HashSet<>(Arrays.asList(
                new FieldsQuery("articleName", ".post", CriteriaCondition.LIKE, MatchMode.ANYWHERE),
                new FieldsQuery("newsId", 1, CriteriaCondition.NOT_LIKE, MatchMode.ANYWHERE),
                new FieldsQuery("newsId", 2, CriteriaCondition.LIKE, MatchMode.START))));
        Criteria criteria = helper.buildCriteria(NewsParseRule.class, request);
        List<NewsParseRule> result = criteria.list();
        Assert.assertTrue(result.size() > 0);
    }

    @Test
    public void testLikeForDates() {
        CriteriaRequest request = new CriteriaRequest();
        request.setDateConditions(new HashSet<>(Arrays.asList(
                new DateQuery("articleDate", LocalDate.parse("2017-03-16"), null, CriteriaDateCondition.MORE))));
        Criteria criteria = helper.buildCriteria(NewsBodyEntity.class, request);
        List<NewsBodyEntity> result = criteria.list();
        Assert.assertTrue(result.size() > 0);

    }

    @Test
    public void testWithEntity() {
        CriteriaRequest request = new CriteriaRequest();
        request.setConditions(new HashSet<>(Arrays.asList(
                new FieldsQuery("articleTopic", "java", CriteriaCondition.LIKE, MatchMode.ANYWHERE),
                new FieldsQuery("isActive", true, CriteriaCondition.EQUAL, null),
                new FieldsQuery("isParsedToday", false, CriteriaCondition.EQUAL, null),
                new FieldsQuery("id", 1, CriteriaCondition.LIKE, MatchMode.ANYWHERE),
                new FieldsQuery("menuEntity.menuName", "general", CriteriaCondition.LIKE, MatchMode.ANYWHERE),
                new FieldsQuery("menuEntity.id", 1, CriteriaCondition.EQUAL, null)
        )));
        Criteria criteria = helper.buildCriteria(NewsEntity.class, request);
        List<NewsEntity> result = criteria.list();
        Assert.assertEquals(1, result.size());
        result.forEach(newsEntity -> {
            Assert.assertEquals("General Topics", newsEntity.getMenuEntity().getMenuName());
            Assert.assertTrue(newsEntity.getArticleTopic().contains("java"));
        });

    }

    @Test
    public void testWithEntityAllByAllExactCriteria() {
        CriteriaRequest request = new CriteriaRequest();
        request.setConditions(new HashSet<>(Arrays.asList(
                new FieldsQuery("articleTopic", "java", CriteriaCondition.LIKE, MatchMode.ANYWHERE),
                new FieldsQuery("isActive", true, CriteriaCondition.EQUAL, null),
                new FieldsQuery("isParsedToday", false, CriteriaCondition.EQUAL, null),
                new FieldsQuery("id", 1, CriteriaCondition.EQUAL, null),
                new FieldsQuery("menuEntity.menuName", "general", CriteriaCondition.LIKE, MatchMode.ANYWHERE),
                new FieldsQuery("menuEntity.id", 1, CriteriaCondition.EQUAL, null),
                new FieldsQuery("bodyEntity.articleName", "Solving Java Issues", CriteriaCondition.LIKE, MatchMode.ANYWHERE)
        )));
        request.setDateConditions(new HashSet<>(Arrays.asList(
                new DateQuery("bodyEntity.articleDate", LocalDate.parse("2017-03-17"),null,CriteriaDateCondition.EQUAL)
        )));

        Criteria criteria = helper.buildCriteria(NewsEntity.class, request);
        List<NewsEntity> result = criteria.list();
        Assert.assertEquals(1, result.size());
        result.forEach(newsEntity -> {
            Assert.assertEquals("General Topics", newsEntity.getMenuEntity().getMenuName());
            Assert.assertTrue(newsEntity.getArticleTopic().contains("java"));
            Assert.assertEquals(1, newsEntity.getBodyEntity().size());
            Assert.assertTrue(newsEntity.getBodyEntity().get(0).getArticleName().contains("Solving Java Issues"));
            Assert.assertEquals(LocalDate.parse("2017-03-17"),newsEntity.getBodyEntity().get(0).getArticleDate());
        });

    }

    @Test
    public void testAllByAll(){

    }
    @Test
    public void testConstrain() {

        CriteriaRequest request = new CriteriaRequest();
        request.setConditions(new HashSet<>(Arrays.asList(
                new FieldsQuery("articleName", LocalDate.now(), CriteriaCondition.LIKE, MatchMode.ANYWHERE))));
        try {
            helper.buildCriteria(NewsParseRule.class, request);
        } catch (javax.validation.ConstraintViolationException e) {
            Assert.assertEquals("date field not allowed", e.getConstraintViolations().stream().findFirst().get().getMessage());
        }
    }
}
