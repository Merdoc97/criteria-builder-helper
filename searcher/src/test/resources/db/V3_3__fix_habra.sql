UPDATE news_parse_rule
SET element_class    = '.content-list.content-list_posts.shortcuts_items', child_lk_rd_more = '.post__title_link',
  child_article_body = '.post__body.post__body_crop', child_article_name = '.post__title'
WHERE rule_pk IN (5, 6, 7, 16);

UPDATE news_parse_rule
SET element_class    = '.content-list.shortcuts_items', child_lk_rd_more = '.post__title_link',
  child_article_body = '.post__body.post__body_crop', child_article_name = '.post__title'
WHERE rule_pk IN (9, 8, 11, 10);
