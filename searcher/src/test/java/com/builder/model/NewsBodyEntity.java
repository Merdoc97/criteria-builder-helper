package com.builder.model;

import com.builder.params.annotations.CriteriaField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDate;

/**
 *
 */
@Entity
@Table(name = "news_body")
@Data
public class NewsBodyEntity {
    @Id
    @Column(name = "article_link")
    @CriteriaField
    private String articleLink;

    @Column(name = "article_name")
    private String articleName;

    @Column(name = "article_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate articleDate;

    @Column(name = "article_body")
    private String articleBody;

    @Column(name = "news_fk")
    @CriteriaField
    private Integer newsFk;

    @ManyToOne
    @JoinColumn(name = "news_fk", insertable = false, updatable = false)
    private NewsEntity newsEntity;

}
