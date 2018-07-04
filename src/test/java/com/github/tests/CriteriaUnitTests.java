package com.github.tests;

import com.github.builder.hibernate.CriteriaQuery;
import com.github.test.model.config.TestConfig;
import org.junit.Assert;
import org.junit.Test;

import javax.el.MethodNotFoundException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;


public class CriteriaUnitTests extends TestConfig {

    @PersistenceContext
    private EntityManager entityManager;


    @Test
    public void testGenerateAliases() throws ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        CriteriaQuery query=new CriteriaQuery(entityManager);
        String[]tmp="news.bodyEntity.articleName".split("\\.");
        Map<String,String>res=query.buildAliases(tmp);
        Assert.assertNotNull(res);
    }

    private Method findMethod(String methodName,Class forClass) throws ClassNotFoundException {
        Class searchedClass=this.getClass().getClassLoader().loadClass(forClass.getCanonicalName());
        Optional<Method>res=Stream.of(searchedClass.getDeclaredMethods()).filter(method -> method.getName().equals(methodName)).findFirst();
        if (!res.isPresent())
            throw new MethodNotFoundException("method not found with name: "+methodName);

        return res.get();

    }
}
