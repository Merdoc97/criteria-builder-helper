package com.builder.util.jpa;

import com.builder.util.FetchModeModifier;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import java.util.Arrays;

import static com.builder.util.UtilClass.isEntityField;

/**
 *
 */
@Slf4j
@SuppressWarnings("java:S3740")
public abstract class JpaFetchModeModifier extends FetchModeModifier {

    protected void changeFetchMode(Class forClass, JoinType fetchMode, Root criteria) {
        Arrays.stream(forClass.getDeclaredFields())
                .forEach(field -> {
                    try {
                        if (isEntityField(forClass, field.getName())) {
                            changeChildFetchMode(forClass, field.getName(), fetchMode, criteria);
                        }
                    } catch (final NoSuchFieldException | ClassNotFoundException e) {
                        log.warn("field not present in entity: {}", forClass.getSimpleName());
                        throw new IllegalArgumentException("field not present in entity:".concat(forClass.getSimpleName()));
                    }
                });

    }

    protected void changeChildFetchMode(Class entityClass, String property, JoinType fetchMode, Root root) throws NoSuchFieldException, ClassNotFoundException {

        Class childClass = getChildClass(entityClass, property);
        Arrays.stream(childClass.getDeclaredFields())
                .forEach(field -> {
                    if (isEntityField(childClass, field.getName())) {
                        root.fetch(property.concat(".").concat(field.getName()), fetchMode);

                    }
                });

    }
}
