INSERT  INTO news(news_pk, top_name_fk, article_topic, is_active, is_parsed_today)
    VALUES (9,1,'java enterprise',TRUE,FALSE );

INSERT INTO news_parse_rule(rule_pk, news_bd_fk, url_format, site_url, element_class, child_article_name, child_lk_rd_more, child_article_body, maxpagenumbers, isonepage)
    VALUES (18,9,'https://www.javacodegeeks.com/category/java/enterprise-java/page/%d/',
    '','.item-list','.post-title>a','.post-title>a','.entry>p',10,FALSE);


INSERT INTO news_parse_rule(rule_pk, news_bd_fk, url_format, site_url, element_class, child_article_name, child_lk_rd_more, child_article_body, maxpagenumbers, isonepage)
VALUES (20,1,'http://www.journaldev.com/page/%d',
        '','.content>article','.entry-header>h2','.entry-title>a','.entry-content>p',10,FALSE);