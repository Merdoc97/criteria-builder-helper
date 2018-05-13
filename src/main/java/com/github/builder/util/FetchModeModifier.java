package com.github.builder.util;

import com.github.builder.exceptions.RequestFieldNotPresent;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.springframework.util.ReflectionUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.util.Arrays;

import static com.github.builder.util.UtilClass.isEntityField;
import static com.github.builder.util.UtilClass.isOneToManyEntity;

/**
 * current class changed dynamically fetch mode for entities
 */
@Slf4j
public abstract class FetchModeModifier {

    protected void changeFetchMode(Class forClass, FetchMode fetchMode, Criteria criteria) {
        Arrays.stream(forClass.getDeclaredFields())
                .forEach(field -> {
                    try {
                        if (isEntityField(forClass, field.getName())) {
//                            change all child entities to lazy instead of conflicts
                            changeChildFetchMode(forClass, field.getName(), fetchMode, criteria);
                        }
                    } catch (NoSuchFieldException | ClassNotFoundException e) {
                        log.warn("field not present in entity: {}", forClass.getSimpleName());
                        throw new IllegalArgumentException("field not present in entity:".concat(forClass.getSimpleName()));
                    }
                });

    }

    protected void changeChildFetchMode(Class entityClass, String property, FetchMode fetchMode, Criteria criteria) throws NoSuchFieldException, ClassNotFoundException {

        Class childClass = getChildClass(entityClass, property);
        Arrays.stream(childClass.getDeclaredFields())
                .forEach(field -> {
                    if (isEntityField(childClass, field.getName())) {
                        criteria.setFetchMode(property.concat(".").concat(field.getName()), fetchMode);
                    }
                });

    }

    protected Class getChildClass(Class forClass, String property) {

        try {
            if (isOneToManyEntity(forClass, property)) {
                String className = (((ParameterizedTypeImpl) forClass
                        .getDeclaredField(property)
                        .getGenericType())
                        .getActualTypeArguments()[0]
                        .getTypeName());

                return this.getClass().getClassLoader().loadClass(className);
            } else {

                String className = ReflectionUtils.findField(forClass, property).getType().getTypeName();
                return this.getClass().getClassLoader().loadClass(className);
            }
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new RequestFieldNotPresent("field not found :" + property);
        }
    }

}
