package com.github.test.model.model;

import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.List;

/**
 *
 */
@Entity
@Table(name = "news")
@DynamicUpdate
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

    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "top_name_fk",referencedColumnName = "tpname_pk",updatable = false)
    private MenuEntity menuEntity;

    @OneToMany(mappedBy = "newsEntity")
    private List<NewsBodyEntity> bodyEntity;

    public NewsEntity(String articleTopic, Boolean isActive, Boolean isParsedToday) {
        this.articleTopic = articleTopic;
        this.isActive = isActive;
        this.isParsedToday = isParsedToday;
    }

    public NewsEntity() {
    }
}
