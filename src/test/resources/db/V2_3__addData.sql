--data for DevelopersComJavaNewsDataAndSectionStrategy
INSERT INTO news_parse_rule(rule_pk, news_bd_fk, url_format, site_url, element_class, child_article_name, child_lk_rd_more, child_article_body, maxpagenumbers, isonepage)
VALUES (2,1,'http://www.developer.com/java/data/','http://www.developer.com','.post','h6','h6>a','.entry',0,TRUE );

--data for DevelopersComJavaNewsEJBComponentsStrategy
INSERT  into news_parse_rule(rule_pk, news_bd_fk, url_format, site_url, element_class, child_article_name, child_lk_rd_more, child_article_body, maxpagenumbers, isonepage)
VALUES (3,1,'http://www.developer.com/java/ejb/','http://www.developer.com','.post','h6','h6>a','.entry',0,TRUE);
--data for DevelopersComJavaNewsEnterpriseStrategy
INSERT INTO news_parse_rule(rule_pk, news_bd_fk, url_format, site_url, element_class, child_article_name, child_lk_rd_more, child_article_body, maxpagenumbers, isonepage)
    VALUES (4,1,'http://www.developer.com/java/ent/','http://www.developer.com','.post','h6','h6>a','.entry',0,TRUE);

--data for https://habrahabr.ru/search/page%d/?target_type=posts&q=java+spring&order_by=date&flow=
INSERT INTO news_parse_rule(rule_pk, news_bd_fk, url_format, site_url, element_class, child_article_name, child_lk_rd_more, child_article_body, maxpagenumbers, isonepage)
    VALUES (5,1,'https://habrahabr.ru/search/page%d/?target_type=posts&q=java+spring&order_by=date&flow=','','div.post.post_teaser.shortcuts_item','a.post__title_link','a.post__title_link','div.content.html_format',5,FALSE );

INSERT INTO news(news_pk, top_name_fk, article_topic, is_active, is_parsed_today)
    VALUES (2,1,'javaPostgres',TRUE ,FALSE );
INSERT INTO news_parse_rule(rule_pk, news_bd_fk, url_format, site_url, element_class, child_article_name, child_lk_rd_more, child_article_body, maxpagenumbers, isonepage)
    VALUES (6,2,'https://habrahabr.ru/search/page%d/?target_type=posts&order_by=date&q=java+postgres&flow=','','div.post.post_teaser.shortcuts_item','a.post__title_link','a.post__title_link','div.content.html_format',5,FALSE );