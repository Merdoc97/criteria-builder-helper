package com.builder.model;

import com.builder.params.annotations.CriteriaField;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.List;

/**
 *
 */

@Entity
@Table(name = "top_name")
@Data
public class MenuEntity {

    @Id
    @Column(name = "tpname_pk")
    @GeneratedValue(generator = "menu_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(catalog = "sequences", name = "menu_seq",
            sequenceName = "menu_seq_pk", allocationSize = 1)
    @CriteriaField
    private Integer id;

    @Column(name = "name", nullable = false, unique = true)
    @CriteriaField
    private String menuName;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @OneToMany
    @JoinColumn(name = "top_name_fk", referencedColumnName = "tpname_pk", updatable = false)
    @CriteriaField
    private List<NewsEntity> news;

    public MenuEntity(String menuName) {
        this.menuName = menuName;
    }

    public MenuEntity() {
    }
}
