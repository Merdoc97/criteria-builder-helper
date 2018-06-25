package com.github.tests;

import com.github.builder.CriteriaHelper;
import com.github.builder.CriteriaRequest;
import com.github.builder.EntitySearcher;
import com.github.builder.condition.CriteriaCondition;
import com.github.builder.condition.CriteriaDateCondition;
import com.github.builder.params.DateQuery;
import com.github.builder.params.FieldsQuery;
import com.github.builder.params.OrderFields;
import com.github.test.model.config.TestConfig;
import com.github.test.model.model.NewsBodyEntity;
import com.github.test.model.model.NewsEntity;
import com.github.test.model.model.NewsParseRule;
import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.exception.SQLGrammarException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static com.github.builder.condition.CriteriaCondition.*;
import static com.github.builder.fields_query_builder.CriteriaRequestBuilder.getRequestBuilder;
import static com.github.builder.fields_query_builder.FieldsQueryBuilder.getFieldsBuilder;
import static com.github.builder.fields_query_builder.OrderFieldsBuilder.getOrderFieldBuilder;
import static org.hibernate.criterion.MatchMode.ANYWHERE;
import static org.hibernate.criterion.MatchMode.EXACT;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

/**

 */
@Transactional
public class CriteriaTest extends TestConfig {

    @Autowired
    private CriteriaHelper helper;

    @Autowired
    private EntitySearcher searcher;

    @Test
    public void simpleQueryForBuilder() {

        CriteriaRequest request = new CriteriaRequest();
        request.setConditions(new HashSet<>(Arrays.asList(
                new FieldsQuery("articleName", ".post", LIKE, ANYWHERE),
                new FieldsQuery("newsId", 1, CriteriaCondition.NOT_LIKE, ANYWHERE),
                new FieldsQuery("newsId", 2, EQUAL, MatchMode.START))));
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
                new FieldsQuery("articleTopic", "java", LIKE, ANYWHERE),
                new FieldsQuery("isActive", true, EQUAL, null),
                new FieldsQuery("isParsedToday", false, EQUAL, null),
                new FieldsQuery("id", 1, LIKE, ANYWHERE),
                new FieldsQuery("menuEntity.menuName", "general", LIKE, ANYWHERE),
                new FieldsQuery("menuEntity.id", 1, EQUAL, null)
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
    public void testLikesStart() {
        CriteriaRequest request = new CriteriaRequest();
        request.setConditions(new HashSet<>(Arrays.asList(
                new FieldsQuery("bodyEntity.articleName", "DUMP-2016:", LIKE, MatchMode.START)
        )));
        Criteria criteria = helper.buildCriteria(NewsEntity.class, request);
        List<NewsEntity> result = criteria.list();
        Assert.assertEquals(1, result.size());
        Assert.assertTrue(result.get(0).getBodyEntity().get(0).getArticleName().startsWith("DUMP-2016:"));
    }

    @Test
    public void testLikesExact() {
        CriteriaRequest request = new CriteriaRequest();
        request.setConditions(new HashSet<>(Arrays.asList(
                new FieldsQuery("articleTopic", "java", LIKE, EXACT)
        )));
        Criteria criteria = helper.buildCriteria(NewsEntity.class, request);
        List<NewsEntity> result = criteria.list();
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("java", result.get(0).getArticleTopic());
    }

    @Test
    public void testLikesEnd() {
        CriteriaRequest request = new CriteriaRequest();
        request.setConditions(new HashSet<>(Arrays.asList(
                new FieldsQuery("articleTopic", "blog", LIKE, MatchMode.END)
        )));
        Criteria criteria = helper.buildCriteria(NewsEntity.class, request);
        List<NewsEntity> result = criteria.list();
        Assert.assertEquals(4, result.size());
    }

    @Test
    public void testLessDate() {
        CriteriaRequest request = new CriteriaRequest();
        request.setDateConditions(new HashSet<>(Arrays.asList(
                new DateQuery("bodyEntity.articleDate", LocalDate.parse("2017-03-17"), null, CriteriaDateCondition.LESS)
        )));
        Criteria criteria = helper.buildCriteria(NewsEntity.class, request);

        List<NewsEntity> result = criteria.list();
        Assert.assertTrue(result.size() == 0);
    }

    @Test
    public void testBetweenDate() {
        CriteriaRequest request = new CriteriaRequest();
        request.setDateConditions(new HashSet<>(Arrays.asList(
                new DateQuery("bodyEntity.articleDate", LocalDate.parse("2017-03-16"), LocalDate.parse("2017-03-18"), CriteriaDateCondition.BETWEEN)
        )));
        Criteria criteria = helper.buildCriteria(NewsEntity.class, request);
        List<NewsEntity> result = criteria.list();
        Assert.assertTrue(result.size() > 0);
    }

    @Test(expected = SQLGrammarException.class)
    public void testInject() {
        CriteriaRequest request = new CriteriaRequest();
        request.setConditions(new HashSet<>(Arrays.asList(
                new FieldsQuery("id", ";UPDATE news SET article_topic='inject'; select 1", LIKE, MatchMode.END)
        )));
        Criteria criteria = helper.buildCriteria(NewsEntity.class, request);
        criteria.list();
    }

    @Test
    public void testWithEntityAllByAllExactCriteria() {
        CriteriaRequest request = new CriteriaRequest();
        request.setConditions(new HashSet<>(Arrays.asList(
                new FieldsQuery("articleTopic", "java", LIKE, ANYWHERE),
                new FieldsQuery("isActive", true, EQUAL, null),
                new FieldsQuery("isParsedToday", false, EQUAL, null),
                new FieldsQuery("id", 1, EQUAL, MatchMode.START),
                new FieldsQuery("menuEntity.menuName", "general", LIKE, ANYWHERE),
                new FieldsQuery("menuEntity.id", 1, EQUAL, null),
                new FieldsQuery("bodyEntity.articleName", "Solving Java Issues", LIKE, ANYWHERE),
                new FieldsQuery("bodyEntity.articleLink", "http://www.developer.com", LIKE, MatchMode.START)
        )));
        request.setDateConditions(new HashSet<>(Arrays.asList(
                new DateQuery("bodyEntity.articleDate", LocalDate.parse("2017-03-17"), null, CriteriaDateCondition.EQUAL)
        )));

        Criteria criteria = helper.buildCriteria(NewsEntity.class, request);
        List<NewsEntity> result = criteria.list();
        Assert.assertEquals(1, result.size());
        result.forEach(newsEntity -> {
            Assert.assertEquals("General Topics", newsEntity.getMenuEntity().getMenuName());
            Assert.assertTrue(newsEntity.getArticleTopic().contains("java"));
            Assert.assertEquals(1, newsEntity.getBodyEntity().size());
            Assert.assertTrue(newsEntity.getBodyEntity().get(0).getArticleName().contains("Solving Java Issues"));
            Assert.assertEquals(LocalDate.parse("2017-03-17"), newsEntity.getBodyEntity().get(0).getArticleDate());
        });

    }

    @Ignore
    @Test(expected = javax.validation.ConstraintViolationException.class)
    public void testConstrains() {
        CriteriaRequest request = new CriteriaRequest();
        request.setConditions(new HashSet<>(Arrays.asList(
                new FieldsQuery("articleTopic", "2017-03-17", LIKE, ANYWHERE)
        )));
        helper.buildCriteria(NewsEntity.class, request);
    }

    @Test
    public void testConstrainsForDates() {
        CriteriaRequest request = new CriteriaRequest();
        request.setConditions(new HashSet<>(Arrays.asList(
                new FieldsQuery("articleTopic", LocalDate.now(), LIKE, ANYWHERE)
        )));
        List<NewsEntity> result = helper.buildCriteria(NewsEntity.class, request).list();
        Assert.assertTrue(result.size() == 0);
    }

    @Test
    public void testConstrain() {

        CriteriaRequest request = new CriteriaRequest();
        request.setConditions(new HashSet<>(Arrays.asList(
                new FieldsQuery("articleName", LocalDate.now(), LIKE, ANYWHERE))));
        try {
            helper.buildCriteria(NewsParseRule.class, request);
        } catch (javax.validation.ConstraintViolationException e) {
            Assert.assertEquals("date field not allowed", e.getConstraintViolations().stream().findFirst().get().getMessage());
        }
    }

    @Test
    public void testWithSorting() {
        CriteriaRequest request = new CriteriaRequest();
        request.setConditions(new HashSet<>(Arrays.asList(
                new FieldsQuery("articleTopic", Arrays.asList("java", "docker"), LIKE, ANYWHERE),
                new FieldsQuery("isActive", true, EQUAL, null),
                new FieldsQuery("isParsedToday", false, EQUAL, null),
                new FieldsQuery("id", Arrays.asList(1, 2, 3), EQUAL, MatchMode.START),
                new FieldsQuery("menuEntity.menuName", "general", LIKE, ANYWHERE),
                new FieldsQuery("menuEntity.id", Arrays.asList(1, 3, 2, 5), EQUAL, null),
                new FieldsQuery("bodyEntity.articleName", "Solving Java Issues", LIKE, ANYWHERE),
                new FieldsQuery("bodyEntity.articleLink", "http://www.developer.com", LIKE, MatchMode.START)
        )));
        request.setDateConditions(new HashSet<>(Arrays.asList(
                new DateQuery("bodyEntity.articleDate", LocalDate.parse("2017-03-17"), null, CriteriaDateCondition.EQUAL)
        )));

        HashSet<OrderFields> orderFields = new HashSet<>(
                Arrays.asList(new OrderFields("articleTopic", ASC))
        );

        Criteria criteria = helper.buildCriteria(NewsEntity.class, request, orderFields);
        List<NewsEntity> result = criteria.list();
        Assert.assertEquals(1, result.size());
        result.forEach(newsEntity -> {
            Assert.assertEquals("General Topics", newsEntity.getMenuEntity().getMenuName());
            Assert.assertTrue(newsEntity.getArticleTopic().contains("java"));
            Assert.assertEquals(1, newsEntity.getBodyEntity().size());
            Assert.assertTrue(newsEntity.getBodyEntity().get(0).getArticleName().contains("Solving Java Issues"));
            Assert.assertEquals(LocalDate.parse("2017-03-17"), newsEntity.getBodyEntity().get(0).getArticleDate());
        });
    }

    @Test
    public void testWithMultipleSorting() {
        CriteriaRequest request = new CriteriaRequest();
        request.setConditions(new HashSet<>(Arrays.asList(
                new FieldsQuery("articleTopic", "java", LIKE, ANYWHERE),
                new FieldsQuery("isActive", true, EQUAL, null),
                new FieldsQuery("isParsedToday", false, EQUAL, null),
                new FieldsQuery("id", 1, EQUAL, MatchMode.START),
                new FieldsQuery("menuEntity.menuName", "general", LIKE, ANYWHERE),
                new FieldsQuery("menuEntity.id", 1, EQUAL, null),
                new FieldsQuery("bodyEntity.articleName", "Solving Java Issues", LIKE, ANYWHERE),
                new FieldsQuery("bodyEntity.articleLink", "http://www.developer.com", LIKE, MatchMode.START)
        )));
        request.setDateConditions(new HashSet<>(Arrays.asList(
                new DateQuery("bodyEntity.articleDate", LocalDate.parse("2017-03-17"), null, CriteriaDateCondition.EQUAL)
        )));

        HashSet<OrderFields> orderFields = new HashSet<>(
                Arrays.asList(new OrderFields("articleTopic", ASC),
                        new OrderFields("menuEntity.id", DESC),
                        new OrderFields("bodyEntity.articleName", ASC),
                        new OrderFields("id", ASC)
                )
        );

        List<NewsEntity> result = searcher.getList(NewsEntity.class, request, orderFields);
        Assert.assertEquals(1, result.size());
        result.forEach(newsEntity -> {
            Assert.assertEquals("General Topics", newsEntity.getMenuEntity().getMenuName());
            Assert.assertTrue(newsEntity.getArticleTopic().contains("java"));
            Assert.assertEquals(1, newsEntity.getBodyEntity().size());
            Assert.assertTrue(newsEntity.getBodyEntity().get(0).getArticleName().contains("Solving Java Issues"));
            Assert.assertEquals(LocalDate.parse("2017-03-17"), newsEntity.getBodyEntity().get(0).getArticleDate());
        });
    }

    @Test
    public void testWithPagination() {
        CriteriaRequest request = new CriteriaRequest();
        request.setConditions(new HashSet<>(Arrays.asList(
                new FieldsQuery("articleTopic", "java", LIKE, ANYWHERE)
        )));
        request.setDateConditions(new HashSet<>(Arrays.asList(
                new DateQuery("bodyEntity.articleDate", LocalDate.parse("2017-03-17"), null, CriteriaDateCondition.EQUAL)
        )));

        Page<NewsEntity> criteria = searcher.getPage(0, 10, NewsEntity.class, request, null);
        Assert.assertTrue(criteria.getContent().size() > 0);
        Assert.assertEquals(criteria.getTotalPages(), 2);

    }

    @Test
    public void testSearchEntity() {
        CriteriaRequest request = new CriteriaRequest();
        request.setConditions(new HashSet<>(Arrays.asList(
                new FieldsQuery("articleTopic", "java", LIKE, ANYWHERE)
        )));
        request.setDateConditions(new HashSet<>(Arrays.asList(
                new DateQuery("bodyEntity.articleDate", LocalDate.parse("2017-03-17"), null, CriteriaDateCondition.EQUAL)
        )));

        NewsEntity result = searcher.findEntity(NewsEntity.class, request, null);
        Assert.assertNotNull(result);
        Assert.assertEquals("java", result.getArticleTopic());

    }

    @Test
    public void testSearchEntityWithSorting() {

        Page<NewsBodyEntity> newsEntities = searcher.getPage(0, 10, NewsBodyEntity.class,
                getRequestBuilder().addFieldQuery(
                        getFieldsBuilder().addField("newsEntity.articleTopic", "java", EQUAL, EXACT)
                                .addField("articleName", Arrays.asList("java", "docker"), LIKE, ANYWHERE)
                                .addField("newsEntity.isActive", true, EQUAL, null)
                                .addField("newsEntity.id", 2, LESS, null)
                                .addField("newsEntity.id", 0, MORE, null)
                                .addField("articleLink", "zte", LESS, null)
                                .build())
                        .build(),
                getOrderFieldBuilder().addOrderField("articleDate", ASC)
                        .addOrderField("articleName", DESC)
                        .build());
        Assert.assertTrue(newsEntities.getContent().size() > 0);
        Assert.assertEquals(Integer.valueOf(1), newsEntities.getContent().get(0).getNewsEntity().getId());
    }

    @Test
    public void testSimplePaging() {
        Page<NewsBodyEntity> res = searcher.getPage(0, 10, NewsBodyEntity.class, getRequestBuilder().build(), null);
        Assert.assertTrue(res.getContent().size() > 0);
    }

}
