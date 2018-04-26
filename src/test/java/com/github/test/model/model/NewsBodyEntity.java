package com.github.test.model.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
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
    private String articleLink;

    @Column(name = "article_name")
    private String articleName;

    @Column(name = "article_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate articleDate;

    @Column(name = "article_body")
    private String articleBody;

    @Column(name = "news_fk")
    private Integer newsFk;

}
