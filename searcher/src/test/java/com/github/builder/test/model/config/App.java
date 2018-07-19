package com.github.builder.test.model.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration;

/**
 */
@SpringBootApplication(exclude =
        {
                DataSourceAutoConfiguration.class,
                DataSourceTransactionManagerAutoConfiguration.class,
                ErrorMvcAutoConfiguration.class
        })
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
