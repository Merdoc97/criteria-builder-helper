UPDATE news
SET article_topic = 'databases'
WHERE news_pk = 2;
-- databases
INSERT INTO news_parse_rule (rule_pk, news_bd_fk, url_format, site_url, element_class, child_article_name, child_lk_rd_more, child_article_body, maxpagenumbers, isonepage)
VALUES (16, 2, 'https://habrahabr.ru/search/page%d/?target_type=posts&order_by=date&q=mysql&flow=', '',
        'div.post.post_teaser.shortcuts_item', 'a.post__title_link', 'a.post__title_link', 'div.content.html_format', 5,
        FALSE);

INSERT INTO news_parse_rule (rule_pk, news_bd_fk, url_format, site_url, element_class, child_article_name, child_lk_rd_more, child_article_body, maxpagenumbers, isonepage)
VALUES (17, 2, 'https://planet.postgresql.org/', '',
        '.planetPost', '.planetPostTitle>a', '.planetPostTitle>a', '.planetPostTitle>a', 0,
        TRUE );