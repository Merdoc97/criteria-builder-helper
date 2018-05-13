package com.github.tests;

import com.github.builder.EntitySearcher;
import com.github.test.model.config.TestConfig;
import com.github.test.model.model.MenuEntity;
import com.github.test.model.model.NewsEntity;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

import static com.github.builder.condition.CriteriaCondition.EQUAL;
import static com.github.builder.condition.CriteriaCondition.LIKE;
import static com.github.builder.fields_query_builder.CriteriaRequestBuilder.getRequestBuilder;
import static com.github.builder.fields_query_builder.FieldsQueryBuilder.getFieldsBuilder;
import static com.github.builder.fields_query_builder.OrderFieldsBuilder.getOrderFieldBuilder;
import static org.hibernate.criterion.MatchMode.ANYWHERE;
import static org.hibernate.criterion.MatchMode.EXACT;
import static org.springframework.data.domain.Sort.Direction.ASC;

/**

 */
public class EntitySearcherTest extends TestConfig {

    @Autowired
    private EntitySearcher searcher;

    @Test
    public void testGetOnlyOneFields() {
        List<Integer> newsEntities = searcher.getForIn(NewsEntity.class, "id",
                getRequestBuilder()
                        .addFields(
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
                        .addFields(
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
     *
     */

    @Test
    public void testMultipleInclude() {
        List<Integer> newsEntitiesId = searcher.getForIn(NewsEntity.class, "id",
                getRequestBuilder()
                        .addFields(
                                getFieldsBuilder()
                                        .addField("bodyEntity.articleName", "java", LIKE, ANYWHERE)
                                        .build())
                        .build());

        List<MenuEntity> result = searcher.getList(MenuEntity.class,
                getRequestBuilder().addFields(
                        getFieldsBuilder()
                                .addField("news.id", newsEntitiesId, LIKE, EXACT)
                                .build())
                        .build(),
                getOrderFieldBuilder()
                        .addOrderField("menuName", ASC)
                        .build());
        Assert.assertEquals(1,result.size());
        Assert.assertEquals(Integer.valueOf(1),result.get(0).getId());
    }

}
