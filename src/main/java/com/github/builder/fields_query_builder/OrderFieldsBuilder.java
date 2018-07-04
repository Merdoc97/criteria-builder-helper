package com.github.builder.fields_query_builder;


import org.springframework.data.domain.Sort;
import com.github.builder.params.OrderFields;

import java.util.HashSet;
import java.util.Set;

/**
 */
public class OrderFieldsBuilder {
    private OrderFieldsBuilder() {
    }

    private static final OrderFieldsBuilder ORDER_FIELDS_BUILDER = new OrderFieldsBuilder();

    public static Builder getOrderFieldBuilder() {
        return ORDER_FIELDS_BUILDER.new Builder();
    }

    public class Builder {
        private Set<OrderFields> orderFields;

        public Builder() {
            this.orderFields = new HashSet<>();
        }

        public Builder addOrderField(String orderField, Sort.Direction direction) {
            orderFields.add(new OrderFields(orderField, direction));
            return this;
        }

        public Set<OrderFields> build() {
            return orderFields;
        }
    }
}
