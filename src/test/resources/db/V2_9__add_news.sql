-- info world strategy
INSERT INTO news_parse_rule (rule_pk, news_bd_fk, url_format, site_url, element_class, child_article_name, child_lk_rd_more, child_article_body, maxpagenumbers, isonepage)
VALUES (12, 1, 'http://www.infoworld.com/category/java/', 'http://www.infoworld.com',
        '.river-well.article',
        '.post-cont>h3', '.post-cont>h3>a', '.post-cont>h4', 0, TRUE);
-- http://insightfullogic.com/ java
INSERT INTO news_parse_rule (rule_pk, news_bd_fk, url_format, site_url, element_class, child_article_name, child_lk_rd_more, child_article_body, maxpagenumbers, isonepage)
VALUES (13, 1, 'http://insightfullogic.com/', '',
        '.post-preview',
        '.post-title', 'a', '.post-subtitle', 0, TRUE);

--java world
INSERT INTO news_parse_rule (rule_pk, news_bd_fk, url_format, site_url, element_class, child_article_name, child_lk_rd_more, child_article_body, maxpagenumbers, isonepage)
VALUES (14, 1, 'http://www.javaworld.com/category/application-development/', 'http://www.javaworld.com',
        '.river-well',
        '.post-cont>h3>a', '.post-cont>h3>a', '.post-cont>h4', 0, TRUE);
-- thoughts-on-java
INSERT INTO news(news_pk, top_name_fk, article_topic, is_active, is_parsed_today)
    VALUES (8,1,'Hibernate',TRUE ,FALSE);
INSERT INTO news_parse_rule (rule_pk, news_bd_fk, url_format, site_url, element_class, child_article_name, child_lk_rd_more, child_article_body, maxpagenumbers, isonepage)
VALUES (15, 8, 'http://www.thoughts-on-java.org/page/%s/', '',
        '.content>article:not(:first-child)',
        '.entry-title>a', '.entry-title>a', '.entry-content>p', 5, FALSE );
