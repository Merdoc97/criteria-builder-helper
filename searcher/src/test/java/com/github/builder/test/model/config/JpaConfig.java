package com.github.builder.test.model.config;

import com.github.builder.CriteriaHelper;
import com.github.builder.EntitySearcher;
import com.github.builder.hibernate.CriteriaQuery;
import com.github.builder.hibernate.EntitySearcherImpl;
import com.github.builder.jpa.EntitySearcherJpaImpl;
import com.github.builder.jpa.PredicateCreator;
import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.io.IOException;

/**
 *
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"com.github"})
@Slf4j
public class JpaConfig {


    @Autowired
    private JpaProperties jpaProperties;

    @Bean(initMethod = "migrate")
    Flyway flyway() throws IOException {
        Flyway flyway = new Flyway();
        flyway.setBaselineOnMigrate(true);
        flyway.setLocations("classpath:/db/");
        flyway.setDataSource(dataSource());
        return flyway;
    }

    @Bean
    public DataSource dataSource() throws IOException {
        log.debug("init com.github.config.test dataSource");
        EmbeddedPostgres pg = EmbeddedPostgres.builder()
                .start();
        return pg.getPostgresDatabase();

    }

    @Bean
    @DependsOn("flyway")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() throws IOException {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setDatabase(Database.POSTGRESQL);
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("com.github");
        factory.setDataSource(dataSource());

        factory.setJpaPropertyMap(jpaProperties.getProperties());
        factory.afterPropertiesSet();
        return factory;
    }

    @Bean(name = "transactionManager")
    public JpaTransactionManager transactionManager() throws IOException {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return txManager;
    }

    //bean validator
    @Bean
    @Primary
    public LocalValidatorFactoryBean localValidatorFactoryBean() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        return bean;
    }

    @Bean
    public CriteriaHelper helper(EntityManager entityManager) {
        return new CriteriaQuery(entityManager);
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
