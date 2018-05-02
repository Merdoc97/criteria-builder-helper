package com.github.builder.params;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;

import javax.validation.constraints.NotNull;

/**
 */
@Getter
@Setter
@EqualsAndHashCode
public class OrderFields {
    @NotNull(message = "order field can't vr null")
    private String orderField;
    @NotNull(message = "order direction can't be null")
    private Sort.Direction direction;

    public OrderFields() {
    }

    public OrderFields(String orderField, Sort.Direction direction) {
        this.orderField = orderField;
        this.direction = direction;
    }
}
