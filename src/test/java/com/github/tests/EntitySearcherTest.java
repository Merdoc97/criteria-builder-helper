package com.github.tests;

import com.github.builder.EntitySearcher;
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
import static com.github.builder.fields_query_builder.CriteriaRequestBuilder.getRequestBuilder;
import static com.github.builder.fields_query_builder.FieldsQueryBuilder.getFieldsBuilder;
import static com.github.builder.fields_query_builder.OrderFieldsBuilder.getOrderFieldBuilder;
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
                getRequestBuilder()
                        .addFieldQuery(
                                getFieldsBuilder()
                                        .addField("articleTopic", "java", EQUAL, EXACT)
                                        .build())
                        .build());

        Assert.assertTrue("in test present only one news topic with current condition it news with id = 1", newsEntities.size() == 1);
        Assert.assertEquals(new Integer(1), newsEntities.get(0));


    }

    @Test
    public void testGetOnlyOneFieldsSecond() {
        List<Integer> result = searcher.getForIn(NewsEntity.class, "id",
                getRequestBuilder()
                        .addFieldQuery(
                                getFieldsBuilder()
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
                getRequestBuilder()
                        .addFieldQuery(
                                getFieldsBuilder()
                                        .addField("bodyEntity.articleName", "java", LIKE, ANYWHERE)
                                        .build())
                        .build());

        List<MenuEntity> result = searcher.getList(MenuEntity.class,
                getRequestBuilder().addFieldQuery(
                        getFieldsBuilder()
                                .addField("news.id", newsEntitiesId, LIKE, EXACT)
                                .build())
                        .build(),
                getOrderFieldBuilder()
                        .addOrderField("menuName", ASC)
                        .build());
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(Integer.valueOf(1), result.get(0).getId());
    }

    @Test
    public void testNotNUll() throws ClassNotFoundException {
        List<MenuEntity> result = searcher.getList(MenuEntity.class,
                getRequestBuilder().addFieldQuery(
                        getFieldsBuilder()
                                .addField("news.id", "", NOT_NULL,null)
                                .addField("news.bodyEntity.articleName", "java", LIKE, MatchMode.ANYWHERE)
                                .build())
                        .build(),
                getOrderFieldBuilder()
                        .addOrderField("menuName", ASC)
                        .build());


        Assert.assertTrue(result.size()>0);
    }

    @Test
    public void testUtil() throws ClassNotFoundException {
        Field field= UtilClass.findField(MenuEntity.class,"news.bodyEntity.articleName");
        Assert.assertNotNull( field);
        Assert.assertEquals(field.getDeclaringClass(), NewsBodyEntity.class);
    }

    @Test
    public void testNUll() {
        List<MenuEntity> result = searcher.getList(MenuEntity.class,
                getRequestBuilder().addFieldQuery(
                        getFieldsBuilder()
                                .addField("news.id", "", IS_NULL,null)
                                .build())
                        .build(),
                getOrderFieldBuilder()
                        .addOrderField("menuName", ASC)
                        .build());
        Assert.assertTrue( result.size()==0);

    }

    @Test
    public void cascadeExample() {
        NewsEntity newsEntity = new NewsEntity("topic", true, false);
        MenuEntity menuEntity = new MenuEntity("parent");
        newsEntity.setMenuEntity(menuEntity);
//        cascade persist
        log.info("-----------------------start cascade persist--------------------------------");
        NewsEntity saved = newsRepository.save(newsEntity);
//        cascade update
        saved.setArticleTopic("updated");
        log.info("-----------------------start cascade update--------------------------------");
        NewsEntity updated = newsRepository.save(saved);
        Assert.assertEquals(updated.getArticleTopic(), "updated");
        Assert.assertEquals(updated.getMenuEntity().getMenuName(), "parent");
        updated.getMenuEntity().setMenuName("newUpdated");
        log.info("-----------------------start cascade update 2--------------------------------");
        NewsEntity entity = newsRepository.save(updated);
        Assert.assertEquals(entity.getMenuEntity().getMenuName(), "newUpdated");
        Assert.assertEquals(entity.getArticleTopic(), "updated");
//        cascade delete
        log.info("-----------------------start cascade delete--------------------------------");
        newsRepository.delete(entity);

    }

}
