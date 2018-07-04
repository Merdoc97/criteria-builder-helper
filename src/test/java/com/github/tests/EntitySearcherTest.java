package com.github.tests;

import com.github.builder.EntitySearcher;
import com.github.builder.fields_query_builder.FieldsQueryBuilder;
import com.github.builder.fields_query_builder.OrderFieldsBuilder;
import com.github.builder.util.UtilClass;
import com.github.test.model.config.TestConfig;
import com.github.test.model.model.MenuEntity;
import com.github.test.model.model.NewsBodyEntity;
import com.github.test.model.model.NewsEntity;
import com.github.test.model.model.NewsRepository;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.criterion.MatchMode;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static com.github.builder.condition.CriteriaCondition.*;
import static com.github.builder.fields_query_builder.CriteriaRequestBuilder.builder;
import static org.hibernate.criterion.MatchMode.ANYWHERE;
import static org.hibernate.criterion.MatchMode.EXACT;
import static org.springframework.data.domain.Sort.Direction.ASC;

/**

 */
@Slf4j
public class EntitySearcherTest extends TestConfig {

    @Autowired
    private EntitySearcher searcher;

    @Autowired
    private NewsRepository newsRepository;

    @Test
    public void testGetOnlyOneFields() {
        List<Integer> newsEntities = searcher.getForIn(NewsEntity.class, "id",
                builder()
                        .addFieldQuery(
                                FieldsQueryBuilder.builder()
                                        .addField("articleTopic", "java", EQUAL, EXACT)
                                        .build())
                        .build());

        Assert.assertTrue("in test present only one news topic with current condition it news with id = 1", newsEntities.size() == 1);
        Assert.assertEquals(new Integer(1), newsEntities.get(0));


    }

    @Test
    public void testGetOnlyOneFieldsSecond() {
        List<Integer> result = searcher.getForIn(NewsEntity.class, "id",
                builder()
                        .addFieldQuery(
                                FieldsQueryBuilder.builder()
                                        .addField("bodyEntity.articleName", "java", LIKE, ANYWHERE)
                                        .build())
                        .build());

        Assert.assertTrue(result.size() == 2);
        Collections.sort(result);
        Assert.assertEquals(new Integer(1), result.get(0));

    }

    /**
     * test param
     * need implement search more than one requirement for building reach search
     */

    @Test
    public void testMultipleInclude() {

        List<Integer> newsEntitiesId = searcher.getForIn(NewsEntity.class, "id",
                builder()
                        .addFieldQuery(
                                FieldsQueryBuilder.builder()
                                        .addField("bodyEntity.articleName", "java", LIKE, ANYWHERE)
                                        .build())
                        .build());

        List<MenuEntity> result = searcher.getList(MenuEntity.class,
                builder().addFieldQuery(
                        FieldsQueryBuilder.builder()
                                .addField("news.id", newsEntitiesId, LIKE, EXACT)
                                .build())
                        .build(),
                OrderFieldsBuilder.builder()
                        .addOrderField("menuName", ASC)
                        .build());
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(Integer.valueOf(1), result.get(0).getId());
    }

    @Test
    public void testNotNUll() throws ClassNotFoundException {
        List<NewsBodyEntity> result = searcher.getList(NewsBodyEntity.class,
                builder().addFieldQuery(
                        FieldsQueryBuilder.builder()
//                                .addField("news.id", "", NOT_NULL, null)
                                .addField("newsEntity.menuEntity.menuName", "general", LIKE, MatchMode.ANYWHERE)
                                .build())
                        .build(),
                OrderFieldsBuilder.builder()
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
                builder().addFieldQuery(
                        FieldsQueryBuilder.builder()
                                .addField("news.id", "", IS_NULL, null)
                                .build())
                        .build(),
                OrderFieldsBuilder.builder()
                        .addOrderField("menuName", ASC)
                        .build());
        Assert.assertTrue(result.size() == 0);

    }


}
