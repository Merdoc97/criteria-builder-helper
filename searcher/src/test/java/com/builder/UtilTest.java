package com.builder;

import com.builder.model.MenuEntity;
import com.builder.model.NewsBodyEntity;
import com.builder.model.NewsParseRule;
import com.builder.model.NewsRepository;
import com.builder.util.UtilClass;
import lombok.Data;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class UtilTest {

    @Test
    void testIsEntity() {
        assertThat(UtilClass.isEntity(NewsBodyEntity.class)).isTrue();
        assertThat(UtilClass.isEntity(NewsRepository.class)).isFalse();
        assertThat(UtilClass.isEntity(JustDto.class)).isFalse();
    }

    @Test
    void testOneToOneGetCriteriaFields() {
        Assertions.assertThat(UtilClass.getCriteriaFields(NewsParseRule.class))
                .extracting(s -> s)
                .containsAll(List.of("id", "isOnePage", "newsEntity.id", "newsEntity.isActive",
                        "newsEntity.bodyEntity.articleLink", "newsEntity.bodyEntity.newsFk"));
    }

    @Test
    void getParentCriteriaFields() {
        Assertions.assertThat(UtilClass.getCriteriaFields(MenuEntity.class))
                .extracting(s -> s)
                .containsAll(List.of("id", "menuName", "news.id", "news.isActive",
                        "news.bodyEntity.articleLink", "news.bodyEntity.newsFk"));
    }

    @Test
    void getFieldClassTest() {
        Assertions.assertThat(UtilClass.getFieldClass(MenuEntity.class, "news").getSimpleName())
                .isEqualTo("NewsEntity");
    }

    @Data
    private class JustDto {
        private String value;
    }
}
