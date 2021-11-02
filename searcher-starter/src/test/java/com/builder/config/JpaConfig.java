package com.builder.config;

import com.builder.CriteriaHelper;
import com.builder.hibernate.CriteriaHelperImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.persistence.EntityManager;

/**
 *
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories
@Slf4j
public class JpaConfig {
    //bean validator
    @Bean
    @Primary
    public LocalValidatorFactoryBean localValidatorFactoryBean() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    public CriteriaHelper helper(EntityManager entityManager){
        return new CriteriaHelperImpl(entityManager);
    }

}