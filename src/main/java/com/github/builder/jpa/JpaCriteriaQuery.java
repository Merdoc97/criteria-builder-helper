package com.github.builder.jpa;

import com.github.builder.CriteriaHelper;
import com.github.builder.CriteriaRequest;
import com.github.builder.params.OrderFields;
import com.github.builder.util.jpa.JpaFetchModeModifier;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Criteria;
import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.persistence.EntityManager;
import javax.validation.Valid;
import java.util.Set;

/**
 * current implementation on jpa criteria builder
 */
@Slf4j
@AllArgsConstructor
@Validated
@Transactional(readOnly = true)
public class JpaCriteriaQuery extends JpaFetchModeModifier implements CriteriaHelper{

    private final EntityManager entityManager;

    @Override
    public Criteria buildCriteria(Class forClass, @Valid CriteriaRequest request) {

        throw new NotYetImplementedException("current feature not implemented yet");
    }

    @Override
    public Criteria buildCriteria(Class forClass, CriteriaRequest request, @Valid Set<OrderFields> orderFields) {
        throw new NotYetImplementedException("current feature not implemented yet");
    }
}
