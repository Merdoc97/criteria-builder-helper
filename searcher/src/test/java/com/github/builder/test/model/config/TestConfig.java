package com.github.builder.test.model.config;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by igor on 4/26/18.
 */
@ActiveProfiles("com.github.config.test")
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {App.class})
public class TestConfig {
}
