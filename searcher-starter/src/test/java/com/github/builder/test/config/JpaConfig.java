package com.github.builder.test.config;

import com.github.builder.CriteriaHelper;
import com.github.builder.hibernate.CriteriaHelperImpl;
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
@EnableJpaRepositories(basePackages = {"com.github"})
@Slf4j
public class JpaConfig {
    //bean validator
    @Bean
    @Primary
    public LocalValidatorFactoryBean localValidatorFactoryBean() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        return bean;
    }

    @Bean
    public CriteriaHelper helper(EntityManager entityManager){
        return new CriteriaHelperImpl(entityManager);
    }

}
