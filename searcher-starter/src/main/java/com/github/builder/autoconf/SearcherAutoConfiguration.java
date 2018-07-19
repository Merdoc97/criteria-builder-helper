package com.github.builder.autoconf;


import com.github.builder.EntitySearcher;
import com.github.builder.hibernate.EntitySearcherImpl;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.util.Assert;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Configuration
@AutoConfigureAfter({HibernateJpaAutoConfiguration.class})
@ConditionalOnClass({LocalContainerEntityManagerFactoryBean.class, EntityManager.class})
public class SearcherAutoConfiguration {


    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public EntitySearcher searcher() {
        Assert.notNull(entityManager,"entity manager can't be null for EntitySearcher");
        return new EntitySearcherImpl(entityManager);
    }

    @Bean
    @ConditionalOnMissingBean({LocalValidatorFactoryBean.class})
    public LocalValidatorFactoryBean localValidatorFactoryBean() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        return bean;
    }
}
