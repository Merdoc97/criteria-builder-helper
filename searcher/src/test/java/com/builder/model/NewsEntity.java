package com.builder.model;

import com.builder.params.annotations.CriteriaField;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
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
    @GeneratedValue(generator = "news_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(catalog = "sequences", name = "news_seq",
            sequenceName = "news_seq_pk", allocationSize = 1)
    @CriteriaField
    private Integer id;

    @Column(name = "article_topic")
    private String articleTopic;

    @Column(name = "is_active")
    @CriteriaField
    private Boolean isActive;

    @Column(name = "is_parsed_today")
    private Boolean isParsedToday;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "top_name_fk", referencedColumnName = "tpname_pk", updatable = false)
    private MenuEntity menuEntity;

    @OneToMany(mappedBy = "newsEntity")
    @CriteriaField
    private List<NewsBodyEntity> bodyEntity;

    public NewsEntity(String articleTopic, Boolean isActive, Boolean isParsedToday) {
        this.articleTopic = articleTopic;
        this.isActive = isActive;
        this.isParsedToday = isParsedToday;
    }

    public NewsEntity() {
    }
}
