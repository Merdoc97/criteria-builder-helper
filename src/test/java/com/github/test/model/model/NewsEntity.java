package com.github.test.model.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

/**
 *
 */
@Entity
@Table(name = "news")
@Data
public class NewsEntity {
    @Id
    @Column(name = "news_pk")
    @GeneratedValue(generator = "news_seq",strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(catalog = "sequences", name = "news_seq",
            sequenceName = "news_seq_pk", allocationSize = 1)
    private Integer id;

    @Column(name = "article_topic")
    private String articleTopic;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "is_parsed_today")
    private Boolean isParsedToday;

    @Column(name = "top_name_fk")
    private Integer topFk;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "top_name_fk",referencedColumnName = "tpname_pk",insertable = false,updatable = false)
    private MenuEntity menuEntity;

    @JsonIgnore
    @OneToMany
    @JoinColumn(name = "news_fk",referencedColumnName = "news_pk",insertable = false,updatable = false)
    private List<NewsBodyEntity> bodyEntity;
}
