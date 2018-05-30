-- create and populate size table
DROP TABLE IF EXISTS size;
CREATE TABLE size(
    size INTEGER,
    relation VARCHAR(255)
);

INSERT INTO size SELECT COUNT(*), "actors" FROM actors;
INSERT INTO size SELECT COUNT(*), "roles" FROM roles;
INSERT INTO size SELECT COUNT(*), "movies_genres" FROM movies_genres;
INSERT INTO size SELECT COUNT(*), "movies_directors" FROM movies_directors;
INSERT INTO size SELECT COUNT(*), "directors" FROM directors;
INSERT INTO size SELECT COUNT(*), "directors_genres" FROM directors_genres;
INSERT INTO size SELECT COUNT(*), "movies" FROM movies;

-- create history table
DROP TABLE IF EXISTS history;
CREATE TABLE history(
    content VARCHAR(1000)
);

-- add fulltext indices (only run once)
ALTER TABLE actors ADD FULLTEXT(first_name);
ALTER TABLE actors ADD FULLTEXT(last_name);
ALTER TABLE roles ADD FULLTEXT(role);
ALTER TABLE movies_genres ADD FULLTEXT(genre);
ALTER TABLE directors ADD FULLTEXT(first_name);
ALTER TABLE directors ADD FULLTEXT(last_name);
ALTER TABLE directors_genres ADD FULLTEXT(genre);
ALTER TABLE movies ADD FULLTEXT(name);
