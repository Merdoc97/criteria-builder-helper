package com.builder.config;

import com.builder.EntitySearcher;
import com.builder.hibernate.EntitySearcherImpl;
import com.builder.jpa.EntitySearcherJpaImpl;
import com.builder.jpa.PredicateCreator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
