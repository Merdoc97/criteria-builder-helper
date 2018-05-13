package com.github.builder.util;

import org.springframework.util.ReflectionUtils;
import com.github.builder.exceptions.RequestFieldNotPresent;
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
            field = ReflectionUtils.findField(forClass, property);
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
        if (fields.length == 2) {

            Field field = ReflectionUtils.findField(forClass, fields[0]);
            if (Objects.isNull(field)) {
                throw new RequestFieldNotPresent("field property not found : " + property);
            }
            return field.isAnnotationPresent(OneToOne.class)
                    || field.isAnnotationPresent(ManyToMany.class)
                    || field.isAnnotationPresent(OneToMany.class)
                    || field.isAnnotationPresent(ManyToOne.class);
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
}
