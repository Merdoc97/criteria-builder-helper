package com.builder.util;

import com.builder.exceptions.RequestFieldNotPresent;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;

/**
 * current class changed dynamically fetch mode for entities
 */
@Slf4j
@SuppressWarnings("checkstyle:UnnecessaryParentheses")
public abstract class FetchModeModifier {

    protected void changeFetchMode(Class forClass, FetchMode fetchMode, Criteria criteria) {
        Arrays.stream(forClass.getDeclaredFields())
                .forEach(field -> {
                    try {
                        if (UtilClass.isEntityField(forClass, field.getName())) {
//                            change all child entities to lazy instead of conflicts
                            changeChildFetchMode(forClass, field.getName(), fetchMode, criteria);
                        }
                    } catch (final NoSuchFieldException | ClassNotFoundException e) {
                        log.warn("field not present in entity: {}", forClass.getSimpleName());
                        throw new IllegalArgumentException("field not present in entity:".concat(forClass.getSimpleName()));
                    }
                });

    }

    protected void changeChildFetchMode(Class entityClass, String property, FetchMode fetchMode, Criteria criteria)
            throws NoSuchFieldException, ClassNotFoundException {

        Class childClass = getChildClass(entityClass, property);
        Arrays.stream(childClass.getDeclaredFields())
                .forEach(field -> {
                    if (UtilClass.isEntityField(childClass, field.getName())) {
                        criteria.setFetchMode(property.concat(".").concat(field.getName()), fetchMode);
                    }
                });

    }

    protected Class getChildClass(Class forClass, String property) {

        try {
            if (UtilClass.isOneToManyEntity(forClass, property)) {
                String className = (((ParameterizedType) forClass
                        .getDeclaredField(property)
                        .getGenericType())
                        .getActualTypeArguments()[0]
                        .getTypeName());

                return this.getClass().getClassLoader().loadClass(className);
            } else {

                String className = ReflectionUtils.findField(forClass, property).getType().getTypeName();
                return this.getClass().getClassLoader().loadClass(className);
            }
        } catch (final ClassNotFoundException | NoSuchFieldException e) {
            throw new RequestFieldNotPresent("field not found :" + property);
        }
    }

}
