package com.github.builder.config;

import com.github.builder.CriteriaHelper;
import com.github.builder.EntitySearcher;
import com.github.builder.hibernate.CriteriaHelperImpl;
import com.github.builder.hibernate.EntitySearcherImpl;
import com.github.builder.jpa.EntitySearcherJpaImpl;
import com.github.builder.jpa.PredicateCreator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.persistence.EntityManager;

/**
 *
 */
@Configuration
@AutoConfigureAfter(JpaRepositoriesAutoConfiguration.class)
public class JpaConfig {


    //bean validator
    @Bean
    @Primary
    public LocalValidatorFactoryBean localValidatorFactoryBean() {
        return new LocalValidatorFactoryBean();
    }


    @Bean
    public EntitySearcher searcher(EntityManager entityManager) {
        return new EntitySearcherImpl(entityManager);
    }

    @Bean
    public EntitySearcher jpaSearcher(EntityManager entityManager) {
        return new EntitySearcherJpaImpl(entityManager, new PredicateCreator());
    }
}
