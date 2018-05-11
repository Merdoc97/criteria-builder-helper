package com.github.builder.util;

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 */
public class UtilClass {
    private UtilClass(){}
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
            if (forClass.getDeclaredField(path).getType().isAssignableFrom(List.class) || forClass.getDeclaredField(path).getType().isAssignableFrom(Set.class)) {
                field = forClass.getClassLoader().
                        loadClass(((ParameterizedTypeImpl) forClass
                                .getDeclaredField(path)
                                .getGenericType())
                                .getActualTypeArguments()[0]
                                .getTypeName())
                        .getDeclaredField(property.split("\\.")[1]);
            } else {
                field = forClass.getDeclaredField(path)
//                    get class type for entity
                        .getType().getDeclaredField(property.split("\\.")[1]);
            }
        } else {
            field = forClass.getDeclaredField(property);
        }
        Class<?> type = field.getType();
        if (type.isAssignableFrom(Boolean.TYPE)) {
            throw new IllegalArgumentException("not allowed boolean type for like field:" + property);
        }
        boolean result= type.isAssignableFrom(Integer.class)
                || type.isAssignableFrom(Long.class)
                || type.isAssignableFrom(Double.class)
                || type.isAssignableFrom(Float.class);

       return result;
    }
}
