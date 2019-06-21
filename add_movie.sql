DROP PROCEDURE IF EXISTS add_movie;

DELIMITER $$

CREATE PROCEDURE add_movie
(
	IN movie_id VARCHAR(10),
	movie_title VARCHAR(100),
    movie_year INT,
    movie_director VARCHAR(100),
    star VARCHAR(100),
    genre VARCHAR(32)
)
BEGIN
	DECLARE max_movie TEXT DEFAULT (SELECT max(id) from movies);
    DECLARE new_movie INT DEFAULT (CONVERT(SUBSTRING(max_movie, 3), unsigned INTEGER));
    DECLARE star_id TEXT DEFAULT NULL;
    DECLARE new_star INT DEFAULT NULL;
    DECLARE genre_id INT DEFAULT NULL;
	IF (movie_id is NULL) THEN
        SET movie_id = CONCAT("tt", new_movie+1);
    END IF;
    IF (select count(*) from stars where `name`=star) > 0 THEN
		SET star_id = (select id from stars where `name`=star limit 1);
    ELSE
		SET star_id = (select max(id) from stars);
        SET new_star = (CONVERT(SUBSTRING(star_id, 3), unsigned INTEGER));
        SET star_id = CONCAT("nm", new_star+1);
        INSERT INTO stars (id, `name`) VALUE (star_id, star);
    END IF;
    IF (select count(*) from genres where `name`=genre) > 0 THEN
		SET genre_id = (select id from genres where `name`=genre);
    ELSE
		INSERT INTO genres (`name`) VALUE (genre);
        SET genre_id = (select id from genres where `name`=genre);
	END IF;
    INSERT INTO movies (id, title, `year`, director) VALUE (movie_id, movie_title, movie_year, movie_director);
    INSERT INTO stars_in_movies (starId, movieId) VALUE (star_id, movie_id);
    INSERT INTO genres_in_movies (genreId, movieId) VALUE (genre_id, movie_id);
    INSERT INTO ratings (movieId, rating, numVotes) VALUE (movie_id, 0.0, 0);
END$$
DELIMITER ;