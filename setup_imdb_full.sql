-- create and populate size table
DROP TABLE IF EXISTS size;
CREATE TABLE size(
    size INTEGER,
    relation VARCHAR(255)
);

INSERT INTO size SELECT COUNT(*), "actors" FROM actors;
INSERT INTO size SELECT COUNT(*), "business" FROM business;
INSERT INTO size SELECT COUNT(*), "countries" FROM countries;
INSERT INTO size SELECT COUNT(*), "directors" FROM directors;
INSERT INTO size SELECT COUNT(*), "distributors" FROM distributors;
INSERT INTO size SELECT COUNT(*), "editors" FROM editors;
INSERT INTO size SELECT COUNT(*), "genres" FROM genres;
INSERT INTO size SELECT COUNT(*), "language" FROM language;
INSERT INTO size SELECT COUNT(*), "movies" FROM movies;
INSERT INTO size SELECT COUNT(*), "movies2actors" FROM movies2actors;
INSERT INTO size SELECT COUNT(*), "movies2directors" FROM movies2directors;
INSERT INTO size SELECT COUNT(*), "movies2editors" FROM movies2editors;
INSERT INTO size SELECT COUNT(*), "movies2producers" FROM movies2producers;
INSERT INTO size SELECT COUNT(*), "movies2writers" FROM movies2writers;
INSERT INTO size SELECT COUNT(*), "prodcompanies" FROM prodcompanies;
INSERT INTO size SELECT COUNT(*), "producers" FROM producers;
INSERT INTO size SELECT COUNT(*), "ratings" FROM ratings;
INSERT INTO size SELECT COUNT(*), "runningtimes" FROM runningtimes;
INSERT INTO size SELECT COUNT(*), "writers" FROM writers;

-- create history table
DROP TABLE IF EXISTS history;
CREATE TABLE history(
    content VARCHAR(1000)
);

-- add fulltext indices (only run once)
ALTER TABLE actors ADD FULLTEXT(name);
ALTER TABLE business ADD FULLTEXT(businesstext);
ALTER TABLE countries ADD FULLTEXT(country);
ALTER TABLE directors ADD FULLTEXT(name);
ALTER TABLE distributors ADD FULLTEXT(name);
ALTER TABLE editors ADD FULLTEXT(name);
ALTER TABLE genres ADD FULLTEXT(genre);
ALTER TABLE language ADD FULLTEXT(language);
ALTER TABLE movies ADD FULLTEXT(title);
ALTER TABLE movies ADD FULLTEXT(year);
ALTER TABLE movies2actors ADD FULLTEXT(as_character);
ALTER TABLE movies2directors ADD FULLTEXT(genre);
ALTER TABLE movies2editors ADD FULLTEXT(addition);
ALTER TABLE movies2producers ADD FULLTEXT(addition);
ALTER TABLE movies2writers ADD FULLTEXT(addition);
ALTER TABLE prodcompanies ADD FULLTEXT(name);
ALTER TABLE producers ADD FULLTEXT(name);
ALTER TABLE ratings ADD FULLTEXT(rank);
ALTER TABLE ratings ADD FULLTEXT(distribution);
ALTER TABLE runningtimes ADD FULLTEXT(time);
ALTER TABLE runningtimes ADD FULLTEXT(addition);
ALTER TABLE writers ADD FULLTEXT(name);
