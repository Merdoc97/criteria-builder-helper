package com.github.tests;

import com.github.builder.EntitySearcher;
import com.github.builder.fields_query_builder.FieldsQueryBuilder;
import com.github.builder.fields_query_builder.OrderFieldsBuilder;
import com.github.builder.jpa.SpecificationBuilder;
import com.github.builder.test.model.config.TestConfig;
import com.github.builder.test.model.model.MenuEntity;
import com.github.builder.test.model.model.NewsBodyEntity;
import com.github.builder.test.model.model.NewsEntity;
import com.github.builder.test.model.model.NewsRepository;
import com.github.builder.util.UtilClass;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.criterion.MatchMode;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.github.builder.condition.CriteriaCondition.*;
import static com.github.builder.fields_query_builder.CriteriaRequestBuilder.getRequestBuilder;
import static org.hibernate.criterion.MatchMode.ANYWHERE;
import static org.hibernate.criterion.MatchMode.EXACT;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

/**

 */
@Slf4j
public class EntitySearcherTest extends TestConfig {

    @Autowired
    private EntitySearcher searcher;

    @Autowired
    private NewsRepository newsRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void testGetOnlyOneFields() {
        List<Integer> newsEntities = searcher.getForIn(NewsEntity.class, "id",
                getRequestBuilder()
                        .addFieldQuery(
                                FieldsQueryBuilder.getFieldsBuilder()
                                        .addField("articleTopic", "java", EQUAL, EXACT)
                                        .build())
                        .build());

        Assert.assertTrue("in  present only one news topic with current condition it news with id = 1", newsEntities.size() == 1);
        Assert.assertEquals(new Integer(1), newsEntities.get(0));


    }


    @Test
    @Transactional
    public void testWithJpaCriteria() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<NewsEntity> query = builder.createQuery(NewsEntity.class);
        Root<NewsEntity> root = query.from(NewsEntity.class);
        query.distinct(true);

        query.select(root).where(builder.and(builder.like(builder.lower(((Join)root.fetch("bodyEntity",JoinType.LEFT)).get("articleName")), "%java%")));

        List<NewsEntity> result = entityManager.createQuery(query).getResultList();

        Assert.assertEquals(2, result.size());
        Assert.assertEquals( 14,result.stream().findFirst().get().getBodyEntity().size());
    }

    @Test
    public void testWithSpecification(){
        List<NewsEntity> result = newsRepository.findAll(Specifications.where((root, query, builder) -> {
            query.distinct(true);
            return builder.and(builder.like(builder.lower(((Join)root.fetch("bodyEntity",JoinType.LEFT)).get("articleName")), "%java%"));
        }));
        Assert.assertEquals(2, result.size());
        Assert.assertEquals( 14,result.stream().findFirst().get().getBodyEntity().size());
    }

    @Test
    public void testWithSpecificationBuilder(){
        SpecificationBuilder builder=new SpecificationBuilder();
        Specification specification=builder.builder(getRequestBuilder()
                .addFieldQuery(
                        FieldsQueryBuilder.getFieldsBuilder()
                                .addField("bodyEntity.articleName", "java", LIKE, ANYWHERE)
                                .build())
                .build());
        List<NewsEntity> result = newsRepository.findAll(specification);

        Assert.assertEquals(2, result.size());
        Assert.assertEquals( 14,result.stream().findFirst().get().getBodyEntity().size());
    }

    @Test
    public void testGetOnlyOneFieldsSecond() {
        List<Integer> result = searcher.getForIn(NewsEntity.class, "id",
                getRequestBuilder()
                        .addFieldQuery(
                                FieldsQueryBuilder.getFieldsBuilder()
                                        .addField("bodyEntity.articleName", "java", LIKE, ANYWHERE)
                                        .build())
                        .build());

        Assert.assertTrue(result.size() == 2);
        Collections.sort(result);
        Assert.assertEquals(new Integer(1), result.get(0));

    }

    /**
     * com.github.config.test param
     * need implement search more than one requirement for building reach search
     */

    @Test
    public void testMultipleInclude() {

        List<Integer> newsEntitiesId = searcher.getForIn(NewsEntity.class, "id",
                getRequestBuilder()
                        .addFieldQuery(
                                FieldsQueryBuilder.getFieldsBuilder()
                                        .addField("bodyEntity.articleName", "java", LIKE, ANYWHERE)
                                        .build())
                        .build());

        List<MenuEntity> result = searcher.getList(MenuEntity.class,
                getRequestBuilder().addFieldQuery(
                        FieldsQueryBuilder.getFieldsBuilder()
                                .addField("news.id", newsEntitiesId, LIKE, EXACT)
                                .build())
                        .build(),
                OrderFieldsBuilder.getOrderFieldBuilder()
                        .addOrderField("menuName", ASC)
                        .build());
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(Integer.valueOf(1), result.get(0).getId());
    }


    @Test
    public void testGetFieldsWhichNeeded() {
        List<Map> result = searcher.getFields(NewsBodyEntity.class,
                getRequestBuilder().addFieldQuery(
                        FieldsQueryBuilder.getFieldsBuilder()
//                                .addField("news.id", "", NOT_NULL, null)
                                .addField("newsEntity.menuEntity.menuName", "general", LIKE, MatchMode.ANYWHERE)
                                .build())
                        .build(), "articleLink", "articleName", "newsEntity.articleTopic");
        Assert.assertTrue(result.size() > 0);
        result.stream().forEach(res -> {
            Map map = res;
            Assert.assertNotNull(map.get("articleLink"));
            Assert.assertNotNull(map.get("articleName"));
            Assert.assertNotNull(map.get("newsEntity.articleTopic"));
            Assert.assertEquals(3, map.size());
        });
    }

    @Test
    public void testGetPageMap() {
        Page<Map> result = searcher.getPage(0, 10, NewsBodyEntity.class,
                getRequestBuilder().addFieldQuery(
                        FieldsQueryBuilder.getFieldsBuilder()
//                                .addField("news.id", "", NOT_NULL, null)
                                .addField("newsEntity.menuEntity.menuName", "general", LIKE, MatchMode.ANYWHERE)
                                .build())
                        .build(),
                OrderFieldsBuilder.getOrderFieldBuilder()
                        .build(),
                "articleLink", "articleName", "newsEntity.articleTopic");
        Assert.assertTrue(result.getContent().size() > 0);
        Assert.assertEquals("44 elements in query", 5, result.getTotalPages());
        Assert.assertEquals(44, result.getTotalElements());
        Assert.assertTrue(result.isFirst());
        Assert.assertFalse(result.isLast());

        result.getContent().stream()
                .forEach(map -> {
                    Assert.assertNotNull(map.get("articleLink"));
                    Assert.assertNotNull(map.get("articleName"));
                    Assert.assertNotNull(map.get("newsEntity.articleTopic"));
                    Assert.assertEquals(3, map.size());
                });
    }

    @Test
    public void testGetMapWithSorting() {
        Page<Map> result = searcher.getPage(0, 10, NewsBodyEntity.class,
                getRequestBuilder().addFieldQuery(
                        FieldsQueryBuilder.getFieldsBuilder()
//                                .addField("news.id", "", NOT_NULL, null)
                                .addField("newsEntity.menuEntity.menuName", "general", LIKE, MatchMode.ANYWHERE)
                                .build())
                        .build(),
                OrderFieldsBuilder.getOrderFieldBuilder()
                        .addOrderField("newsEntity.articleTopic", DESC)
                        .addOrderField("articleName", ASC)
                        .build(),
                "articleLink", "articleName", "newsEntity.articleTopic");
        Assert.assertTrue(result.getContent().size() > 0);
        Assert.assertEquals("44 elements in query", 5, result.getTotalPages());
        Assert.assertEquals(44, result.getTotalElements());
        Assert.assertTrue(result.isFirst());

        Assert.assertFalse(result.isLast());

        result.getContent().stream()
                .forEach(map -> {
                    Assert.assertNotNull(map.get("articleLink"));
                    Assert.assertNotNull(map.get("articleName"));
                    Assert.assertNotNull(map.get("newsEntity.articleTopic").equals("databases"));
                    Assert.assertEquals(3, map.size());
                });
    }

    @Test
    public void testNotNUll() throws ClassNotFoundException {
        List<NewsBodyEntity> result = searcher.getList(NewsBodyEntity.class,
                getRequestBuilder().addFieldQuery(
                        FieldsQueryBuilder.getFieldsBuilder()
//                                .addField("news.id", "", NOT_NULL, null)
                                .addField("newsEntity.menuEntity.menuName", "general", LIKE, MatchMode.ANYWHERE)
                                .build())
                        .build(),
                OrderFieldsBuilder.getOrderFieldBuilder()
                        .addOrderField("newsEntity.menuEntity.menuName", ASC)
                        .build());


        Assert.assertTrue(result.size() > 0);
    }


    @Test
    public void testUtil() throws ClassNotFoundException {
        Field field = UtilClass.findField(MenuEntity.class, "news.bodyEntity.articleName");
        Assert.assertNotNull(field);
        Assert.assertEquals(field.getDeclaringClass(), NewsBodyEntity.class);
    }

    @Test
    public void testNUll() {
        List<MenuEntity> result = searcher.getList(MenuEntity.class,
                getRequestBuilder().addFieldQuery(
                        FieldsQueryBuilder.getFieldsBuilder()
                                .addField("news.id", "", IS_NULL, null)
                                .build())
                        .build(),
                OrderFieldsBuilder.getOrderFieldBuilder()
                        .addOrderField("menuName", ASC)
                        .build());
        Assert.assertTrue(result.size() == 0);

    }


}
