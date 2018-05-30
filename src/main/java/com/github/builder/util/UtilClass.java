package com.github.builder.util;

import com.github.builder.exceptions.RequestFieldNotPresent;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 */
public class UtilClass {


    private UtilClass() {
    }

    /**
     * @param forClass
     * @param property
     * @param path     actual variable for entity relation
     * @return
     * @throws NoSuchFieldException
     */
    public static boolean isNumber(Class forClass, String property, String path) throws NoSuchFieldException, ClassNotFoundException {
        Field field = null;

        if (Objects.nonNull(path) && path.length() > 0) {
            if (ReflectionUtils.findField(forClass, path).getType().isAssignableFrom(List.class) || forClass.getDeclaredField(path).getType().isAssignableFrom(Set.class)) {
                field = forClass.getClassLoader().
                        loadClass(((ParameterizedTypeImpl) forClass
                                .getDeclaredField(path)
                                .getGenericType())
                                .getActualTypeArguments()[0]
                                .getTypeName())
                        .getDeclaredField(property.split("\\.")[1]);
            } else {
                field = ReflectionUtils.findField(forClass, path)
//                    get class type for entity
                        .getType().getDeclaredField(property.split("\\.")[1]);
            }
        } else {
            field = UtilClass.findField(forClass, property);
        }
        Class<?> type = field.getType();
        if (type.isAssignableFrom(Boolean.TYPE)) {
            throw new IllegalArgumentException("not allowed boolean type for like field:" + property);
        }
        boolean result = type.isAssignableFrom(Integer.class)
                || type.isAssignableFrom(Long.class)
                || type.isAssignableFrom(Double.class)
                || type.isAssignableFrom(Float.class)
                || type.isAssignableFrom(BigDecimal.class)
                || type.isAssignableFrom(Short.class);

        return result;
    }

    public static boolean isEntityField(Class forClass, String property) {

        String[] fields = property.split("\\.");

        if (fields.length > 1) {
            return true;
            /*
            Field field = UtilClass.findField(forClass, property);

            if (Objects.isNull(field)) {
                throw new RequestFieldNotPresent("field property not found : " + property);
            }
            return field.isAnnotationPresent(OneToOne.class)
                    || field.isAnnotationPresent(ManyToMany.class)
                    || field.isAnnotationPresent(OneToMany.class)
                    || field.isAnnotationPresent(ManyToOne.class);*/
        } else {
            Field field = ReflectionUtils.findField(forClass, property);
            if (Objects.isNull(field)) {
                throw new RequestFieldNotPresent("field property not found : " + property);
            }
            return field.isAnnotationPresent(OneToOne.class)
                    || field.isAnnotationPresent(ManyToMany.class)
                    || field.isAnnotationPresent(OneToMany.class)
                    || field.isAnnotationPresent(ManyToOne.class);

        }

    }


    public static boolean isOneToManyEntity(Class forClass, String property) {

        String[] fields = property.split("\\.");
        if (fields.length == 2) {
            Field field = ReflectionUtils.findField(forClass, fields[0]);
            return field.isAnnotationPresent(OneToMany.class);
        } else {
            Field field = ReflectionUtils.findField(forClass, property);
            return field.isAnnotationPresent(OneToMany.class);
        }
    }

    public static Field findField(Class forClass, String field) {
        Assert.notNull(forClass, "class can't be null");
        Assert.notNull(field, "search field can't be null");
        String[] tmp = field.split("\\.");
        Class tmpClass = forClass;
        Field result = null;
        for (String property : tmp) {
            Field tmpField = ReflectionUtils.findField(tmpClass, property);
            if (Objects.nonNull(tmpField)) {
                if (isCollection(tmpField))
                    tmpClass = getClassFromCollection(tmpField);
                else
                    tmpClass = tmpField.getDeclaringClass();
                result = tmpField;
            }
        }

        return result;

    }

    private static boolean isCollection(Field field) {
        return (field.getType().isAssignableFrom(List.class) || field.getType().isAssignableFrom(Set.class));
    }

    private static Class getClassFromCollection(Field field) {
        try {
            return ClassLoader.getSystemClassLoader()
                    .loadClass(((ParameterizedTypeImpl) field
                            .getGenericType())
                            .getActualTypeArguments()[0].getTypeName());
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("class not found");
        }
    }
}
