CREATE TABLE article_news
(
  art_link_pk VARCHAR(500) PRIMARY KEY NOT NULL,
  art_body    VARCHAR(500),
  art_date    DATE,
  art_name    VARCHAR(500)
);

CREATE TABLE combany_blogs
(
  art_link_pk  VARCHAR(1000) PRIMARY KEY NOT NULL,
  art_body     VARCHAR(2000),
  art_date     DATE,
  art_name     VARCHAR(1000),
  company_name VARCHAR(255)              NOT NULL
);

CREATE TABLE "top_name" (
  "tpname_pk" SERIAL,
  "name"      VARCHAR(50) NOT NULL UNIQUE DEFAULT 'not null',
  CONSTRAINT topName_pk PRIMARY KEY ("tpname_pk")
) WITH (
OIDS =FALSE
);


CREATE TABLE "blogs" (

) WITH (
OIDS =FALSE
);


CREATE TABLE "news" (
  "news_pk"         SERIAL       NOT NULL,
  "top_name_fk"     INTEGER      NOT NULL,
  "article_topic"   VARCHAR(100) NOT NULL DEFAULT 'not null',
  "is_active"       BOOLEAN      NOT NULL DEFAULT 'false',
  "is_parsed_today" BOOLEAN      NOT NULL DEFAULT 'false',
  CONSTRAINT news_pk PRIMARY KEY ("news_pk")
) WITH (
OIDS =FALSE
);


CREATE TABLE "news_body" (
  "news_fk"      INTEGER      NOT NULL,
  "article_name" VARCHAR(500) NOT NULL,
  "article_date" DATE         NOT NULL,
  "article_body" VARCHAR(500),
  "article_link" VARCHAR(500) PRIMARY KEY
);


CREATE TABLE "news_parse_rule" (
  "rule_pk"            SERIAL  NOT NULL,
  "news_bd_fk"         INTEGER NOT NULL,
  "url_format"         TEXT    NOT NULL,
  "site_url"           TEXT,
  "element_class"      TEXT    NOT NULL,
  "child_article_name" TEXT    NOT NULL,
  "child_lk_rd_more"   TEXT    NOT NULL,
  "child_article_body" TEXT    NOT NULL,
  CONSTRAINT news_parse_rule_pk PRIMARY KEY ("rule_pk")
) WITH (
OIDS =FALSE
);


ALTER TABLE "news" ADD CONSTRAINT "news_fk0" FOREIGN KEY ("top_name_fk") REFERENCES "top_name" ("tpname_pk");

ALTER TABLE "news_body" ADD CONSTRAINT "news_body_fk0" FOREIGN KEY ("news_fk") REFERENCES "news" ("news_pk");

ALTER TABLE "news_parse_rule" ADD CONSTRAINT "news_parse_rule_fk0" FOREIGN KEY ("news_bd_fk") REFERENCES "news" ("news_pk");

ALTER TABLE news_parse_rule ADD COLUMN maxPageNumbers INTEGER NOT NULL DEFAULT 0;

INSERT INTO "top_name" (tpname_pk, name) VALUES (1, 'Menu');
INSERT INTO news (news_pk, top_name_fk, article_topic, is_active, is_parsed_today)
VALUES (1, 1, 'java', TRUE, FALSE);
INSERT INTO news_parse_rule (news_bd_fk, url_format, site_url, element_class, child_article_name, child_lk_rd_more, child_article_body)
VALUES (1, 'https://habrahabr.ru/search/page%d/?target_type=posts&q=java+spring&order_by=date&flow=', NULL,
        'div.post.post_teaser.shortcuts_item', 'a.post__title_link', 'a.post__title_link', 'div.content.html_format');
