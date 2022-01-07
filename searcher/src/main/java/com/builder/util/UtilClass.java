package com.builder.util;

import com.builder.exceptions.RequestFieldNotPresent;
import com.builder.params.annotations.CriteriaField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;

/**
 *
 */
@Slf4j
@SuppressWarnings({"checkstyle:UnnecessaryParentheses", "checkstyle:BooleanExpressionComplexity"})
public final class UtilClass {

    private UtilClass() {
    }

    /**
     * @param forClass
     * @param property
     * @return
     * @throws NoSuchFieldException
     */
    public static boolean isNumber(Class forClass, String property) {

        Field field = UtilClass.findField(forClass, property);
        log.info("find field {} for property {} for class {}", field, property, forClass);
        if (isNull(field)) {
            throw new RequestFieldNotPresent("field not present");
        }
        Class<?> type = field.getType();
        if (type.isAssignableFrom(Boolean.TYPE)) {
            throw new IllegalArgumentException("not allowed boolean type for like field:" + property);
        }
        return type.isAssignableFrom(Integer.class)
                || type.isAssignableFrom(Long.class)
                || type.isAssignableFrom(Double.class)
                || type.isAssignableFrom(Float.class)
                || type.isAssignableFrom(BigDecimal.class)
                || type.isAssignableFrom(Short.class);
    }


    public static boolean isEntityField(Class forClass, String property) {

        String[] fields = property.split("\\.");

        if (fields.length > 1) {
            return true;
        } else {
            Field field = UtilClass.findField(forClass, property);
            if (isNull(field)) {
                throw new RequestFieldNotPresent("field property not found : " + property + " for class " + forClass.getSimpleName());
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
                if (isCollection(tmpField)) {
                    tmpClass = getClassFromCollection(tmpField);
                } else {
                    tmpClass = tmpField.getDeclaringClass();
                }
                result = tmpField;
            }
        }

        return result;

    }

    public static Object getFieldValue(Class forClass, String entityFieldName, Object entity) throws IllegalAccessException {
        Field field = UtilClass.findField(forClass, entityFieldName);
        if (isNull(field)) {
            throw new IllegalAccessException("field not present");
        }
        field.setAccessible(true);
        return field.get(entity);
    }

    public static String getIdField(Class forClass) {
        Field[] fields = forClass.getDeclaredFields();
        return stream(fields)
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new RequestFieldNotPresent("id field not present"))
                .getName();
    }

    private static boolean isCollection(Field field) {
        return (field.getType().isAssignableFrom(List.class) || field.getType().isAssignableFrom(Set.class));
    }

    private static Class getClassFromCollection(Field field) {
        try {
            return ClassLoader.getSystemClassLoader()
                    .loadClass(((ParameterizedType) field
                            .getGenericType())
                            .getActualTypeArguments()[0].getTypeName());
        } catch (final ClassNotFoundException e) {
            throw new IllegalArgumentException("class not found");
        }
    }

    public static List<String> getCriteriaFields(Class forClass) {
        if (!isEntity(forClass)) {
            return emptyList();
        }
        var annotatedFields = stream(forClass.getDeclaredFields())
                .filter(field -> stream(field.getDeclaredAnnotations()).anyMatch(annotation -> annotation.annotationType().equals(CriteriaField.class)))
                .map(Field::getName)
                .collect(Collectors.toList());
        var entityFields = annotatedFields.stream()
                .filter(field -> isEntityField(forClass, field))
                .map(entityField -> {
                    return new Pair(entityField, getFieldClass(forClass, entityField));
                })
                .filter(pair -> pair.getAClass() != null)
                .flatMap(pair -> getCriteriaFields(pair.getAClass())
                        .stream()
                        .map(field -> pair.getField().concat(".").concat(field)))
                .collect(Collectors.toList());
        var result = annotatedFields.stream()
                .filter(field -> !isEntityField(forClass, field))
                .collect(Collectors.toList());
        result.addAll(entityFields);
        return result;


    }

    public static Class<?> getFieldClass(Class forClass, String fieldName) {
        var resultField = Optional.ofNullable(forClass)
                .stream()
                .flatMap(aClass -> Arrays.stream(aClass.getDeclaredFields()))
                .filter(field -> field.getName().equals(fieldName))
                .findFirst();
        if (resultField.isPresent() && isCollection(resultField.get())) {
            return getClassFromCollection(resultField.get());
        }
        return resultField.<Class<?>>map(Field::getType).orElse(null);
    }

    public static boolean isEntity(Class forClass) {
        if (forClass == null) {
            return false;
        }
        return stream(forClass.getAnnotations())
                .anyMatch(annotation -> annotation.annotationType().equals(Entity.class));

    }

    private static class Pair {

        private final String field;
        private final Class aClass;

        Pair(String field, Class aClass) {
            this.field = field;
            this.aClass = aClass;
        }

        public String getField() {
            return field;
        }

        public Class getAClass() {
            return aClass;
        }
    }

}
