package com.github.test.model.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.persistence.*;
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
    @GeneratedValue(generator = "menu_seq",strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(catalog = "sequences", name = "menu_seq",
            sequenceName = "menu_seq_pk", allocationSize = 1)
    private Integer id;

    @Column(name = "name",nullable = false,unique = true)
    private String menuName;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @OneToMany(mappedBy = "menuEntity")
    private List<NewsEntity>news;




}
