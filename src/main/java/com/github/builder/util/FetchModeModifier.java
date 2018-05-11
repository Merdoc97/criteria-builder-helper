package com.github.builder.util;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.lang.reflect.Field;
import java.util.Arrays;

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


            Class childClass = getChildClass(entityClass,property);
            Arrays.stream(childClass.getDeclaredFields())
                    .forEach(field -> {
                        try {
                            if (isEntityField(childClass, field.getName())) {
                                criteria.setFetchMode(property.concat(".").concat(field.getName()), fetchMode);
                            }
                        } catch (NoSuchFieldException e) {
                            log.warn("field not present in entity: {}", childClass.getSimpleName());
                            throw new IllegalArgumentException("field not present in entity:".concat(childClass.getSimpleName()));
                        }
                    });

    }

    protected Class getChildClass(Class forClass,String property) throws NoSuchFieldException, ClassNotFoundException {
        if (isOneToManyEntity(forClass, property)) {
            String className = (((ParameterizedTypeImpl) forClass
                    .getDeclaredField(property)
                    .getGenericType())
                    .getActualTypeArguments()[0]
                    .getTypeName());
            return this.getClass().getClassLoader().loadClass(className);
        }
        else {
            String className=forClass.getDeclaredField(property)
                    .getType().getTypeName();
            return this.getClass().getClassLoader().loadClass(className);
        }
    }

    protected boolean isEntityField(Class forClass, String property) throws NoSuchFieldException {
        String[] fields = property.split("\\.");
        if (fields.length == 2) {
            Field field = forClass.getDeclaredField(fields[0]);
            return field.isAnnotationPresent(OneToOne.class)
                    || field.isAnnotationPresent(ManyToMany.class)
                    || field.isAnnotationPresent(OneToMany.class)
                    || field.isAnnotationPresent(ManyToOne.class);
        } else {
            Field field = forClass.getDeclaredField(property);
            return field.isAnnotationPresent(OneToOne.class)
                    || field.isAnnotationPresent(ManyToMany.class)
                    || field.isAnnotationPresent(OneToMany.class)
                    || field.isAnnotationPresent(ManyToOne.class);

        }
    }

    protected boolean isOneToManyEntity(Class forClass, String property) throws NoSuchFieldException {
        String[] fields = property.split("\\.");
        if (fields.length == 2) {
            Field field = forClass.getDeclaredField(fields[0]);
            return field.isAnnotationPresent(OneToMany.class);
        } else {
            Field field = forClass.getDeclaredField(property);
            return field.isAnnotationPresent(OneToMany.class);
        }
    }
}
