-- create and populate size table
DROP TABLE IF EXISTS size;
CREATE TABLE size(
    size INTEGER,
    relation VARCHAR(255)
);

INSERT INTO size SELECT COUNT(*), "author" FROM author;
INSERT INTO size SELECT COUNT(*), "cite" FROM cite;
INSERT INTO size SELECT COUNT(*), "conference" FROM conference;
INSERT INTO size SELECT COUNT(*), "domain" FROM domain;
INSERT INTO size SELECT COUNT(*), "domain_author" FROM domain_author;
INSERT INTO size SELECT COUNT(*), "domain_conference" FROM domain_conference;
INSERT INTO size SELECT COUNT(*), "domain_journal" FROM domain_journal;
INSERT INTO size SELECT COUNT(*), "domain_keyword" FROM domain_keyword;
INSERT INTO size SELECT COUNT(*), "domain_publication" FROM domain_publication;
INSERT INTO size SELECT COUNT(*), "journal" FROM journal;
INSERT INTO size SELECT COUNT(*), "keyword" FROM keyword;
INSERT INTO size SELECT COUNT(*), "organization" FROM organization;
INSERT INTO size SELECT COUNT(*), "publication" FROM publication;
INSERT INTO size SELECT COUNT(*), "publication_keyword" FROM publication_keyword;
INSERT INTO size SELECT COUNT(*), "writes" FROM writes;

-- create history table
DROP TABLE IF EXISTS history;
CREATE TABLE history(
    content VARCHAR(1000)
);

-- add fulltext indices (only run once)
ALTER TABLE publication ADD FULLTEXT(title);
ALTER TABLE publication ADD FULLTEXT(abstract);
ALTER TABLE publication ADD FULLTEXT(doi);
ALTER TABLE author ADD FULLTEXT(homepage);
ALTER TABLE author ADD FULLTEXT(photo);
