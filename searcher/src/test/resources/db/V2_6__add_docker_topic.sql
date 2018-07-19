INSERT INTO news(news_pk, top_name_fk, article_topic, is_active, is_parsed_today)
    VALUES (3,1,'docker',TRUE ,FALSE );
INSERT INTO news_parse_rule(rule_pk, news_bd_fk, url_format, site_url, element_class, child_article_name, child_lk_rd_more, child_article_body, maxpagenumbers, isonepage)
VALUES (7,3,'https://habrahabr.ru/search/page%d/?target_type=posts&order_by=date&q=docker&flow=','','div.post.post_teaser.shortcuts_item','a.post__title_link','a.post__title_link','div.content.html_format',5,FALSE );