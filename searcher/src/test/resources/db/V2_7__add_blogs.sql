INSERT INTO top_name (tpname_pk, name)
VALUES (2, 'Company Blogs');
-- epam blog rule strategy
INSERT INTO news (news_pk, top_name_fk, article_topic, is_active, is_parsed_today)
VALUES (4, 2, 'Epam blog', TRUE, FALSE);
INSERT INTO news_parse_rule (rule_pk, news_bd_fk, url_format, site_url, element_class, child_article_name, child_lk_rd_more, child_article_body, maxpagenumbers, isonepage)
VALUES (8, 4, 'https://habrahabr.ru/company/epam_systems/page%d/', '', 'div.post.post_teaser.shortcuts_item',
        'a.post__title_link', 'a.post__title_link', 'div.content.html_format', 5, FALSE);
-- luxoft habra strategy
INSERT INTO news (news_pk, top_name_fk, article_topic, is_active, is_parsed_today)
VALUES (5, 2, 'Luxoft blog', TRUE, FALSE);
INSERT INTO news_parse_rule (rule_pk, news_bd_fk, url_format, site_url, element_class, child_article_name, child_lk_rd_more, child_article_body, maxpagenumbers, isonepage)
VALUES (9, 5, 'https://habrahabr.ru/company/luxoft/page%d/', '', 'div.post.post_teaser.shortcuts_item', 'a.post__title_link',
   'a.post__title_link', 'div.content.html_format', 5, FALSE);
-- jet brains
INSERT INTO news (news_pk, top_name_fk, article_topic, is_active, is_parsed_today)
VALUES (6, 2, 'JetBrains blog', TRUE, FALSE);
INSERT INTO news_parse_rule (rule_pk, news_bd_fk, url_format, site_url, element_class, child_article_name, child_lk_rd_more, child_article_body, maxpagenumbers, isonepage)
VALUES (10, 6, 'https://habrahabr.ru/company/JetBrains/page%d/', '', 'div.post.post_teaser.shortcuts_item', 'a.post__title_link',
        'a.post__title_link', 'div.content.html_format', 5, FALSE);
-- data art
INSERT INTO news (news_pk, top_name_fk, article_topic, is_active, is_parsed_today)
VALUES (7, 2, 'DataArt blog', TRUE, FALSE);
INSERT INTO news_parse_rule (rule_pk, news_bd_fk, url_format, site_url, element_class, child_article_name, child_lk_rd_more, child_article_body, maxpagenumbers, isonepage)
VALUES (11, 7, 'https://habrahabr.ru/company/dataart/page%d/', '', 'div.post.post_teaser.shortcuts_item', 'a.post__title_link',
        'a.post__title_link', 'div.content.html_format', 5, FALSE);

UPDATE top_name SET name='General Topics' WHERE tpname_pk=1;


