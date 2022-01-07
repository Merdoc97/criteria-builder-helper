package com.builder.model;


import com.builder.params.annotations.CriteriaField;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 *
 */
@Entity
@Table(name = "news_parse_rule")
@Data
public class NewsParseRule {
    @Id
    @Column(name = "rule_pk")
    @GeneratedValue(generator = "rule_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(catalog = "sequences", name = "rule_seq",
            sequenceName = "rule_seq_pk", allocationSize = 1)
    @CriteriaField
    private Integer id;

    @Column(name = "url_format")
    private String urlFormat;

    @Column(name = "site_url")
    private String siteUrl;

    @Column(name = "element_class")
    private String elementsClass;

    @Column(name = "child_article_name")
    private String articleName;

    @Column(name = "child_lk_rd_more")
    private String linkReadMore;

    @Column(name = "child_article_body")
    private String articleBody;

    @Column(name = "news_bd_fk")
    private Integer newsId;

    @Column(name = "maxpagenumbers")
    private Integer maxPages;

    @Column(name = "isonepage")
    @CriteriaField
    private boolean isOnePage;

    @OneToOne
    @JoinColumn(name = "news_bd_fk", referencedColumnName = "news_pk", insertable = false, updatable = false)
    @CriteriaField
    private NewsEntity newsEntity;

    public NewsParseRule() {
    }

    @Override
    public String toString() {
        return "NewsParseRule{" +
                "id=" + id +
                ", urlFormat='" + urlFormat + '\'' +
                ", siteUrl='" + siteUrl + '\'' +
                ", elementsClass='" + elementsClass + '\'' +
                ", articleName='" + articleName + '\'' +
                ", linkReadMore='" + linkReadMore + '\'' +
                ", articleBody='" + articleBody + '\'' +
                ", newsId=" + newsId +
                ", maxPages=" + maxPages +
                ", isOnePage=" + isOnePage +
                ", newsEntity=" + newsEntity +
                '}';
    }
}
