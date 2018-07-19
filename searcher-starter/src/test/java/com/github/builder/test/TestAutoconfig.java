package com.github.builder.test;

import com.github.builder.EntitySearcher;
import com.github.builder.test.config.App;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {App.class})
@SpringBootTest
public class TestAutoconfig {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testAutoConfiguration(){
        EntitySearcher searcher=applicationContext.getBean(EntitySearcher.class);
        Assert.assertNotNull(searcher);
    }
}
