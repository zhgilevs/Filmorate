CREATE TABLE IF NOT EXISTS USERS
(
    id       INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name     VARCHAR(100)        NOT NULL,
    login    VARCHAR(100) UNIQUE NOT NULL,
    email    VARCHAR(200) UNIQUE NOT NULL,
    birthday DATE
);

CREATE TABLE IF NOT EXISTS MPAS
(
    mpa_id      INTEGER PRIMARY KEY,
    name        VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(200)        NOT NULL
);

CREATE TABLE IF NOT EXISTS FILMS
(
    id           INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    description  VARCHAR(200),
    duration     INTEGER,
    release_date DATE         NOT NULL,
    mpa_id       INTEGER      NOT NULL,
    FOREIGN KEY (mpa_id) REFERENCES MPAS (mpa_id),
    CHECK (duration > 0)
);

CREATE TABLE IF NOT EXISTS GENRES
(
    genre_id INTEGER PRIMARY KEY,
    name     VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS FILM_GENRES
(
    genre_id INTEGER NOT NULL,
    film_id  INTEGER NOT NULL,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES FILMS (id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES GENRES (genre_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS LIKES
(
    user_id INTEGER NOT NULL,
    film_id INTEGER NOT NULL,
    PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (film_id) REFERENCES FILMS (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES USERS (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS FRIENDS
(
    user_id   INTEGER NOT NULL,
    friend_id INTEGER NOT NULL,
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES USERS (id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES USERS (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS DIRECTORS
(
    director_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name        VARCHAR,
    constraint "DIRECTORS_pk"
        primary key (DIRECTOR_ID)
);

CREATE TABLE IF NOT EXISTS FILMS_AND_DIRECTORS
(
    FILM_ID     INTEGER not null,
    DIRECTOR_ID INTEGER not null,
    constraint "FILMS_AND_DIRECTORS_DIRECTORS_DIRECTOR_ID_fk"
        foreign key (DIRECTOR_ID) references DIRECTORS,
    constraint "FILMS_AND_DIRECTORS_FILMS_ID_fk"
        foreign key (FILM_ID) references FILMS
);

CREATE TABLE IF NOT EXISTS REVIEWS
(
    review_id   INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    content     VARCHAR NOT NULL,
    is_positive BOOLEAN NOT NULL,
    user_id     INTEGER NOT NULL,
    film_id     INTEGER NOT NULL,
    useful      INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES USERS (id) ON DELETE CASCADE,
    FOREIGN KEY (film_id) REFERENCES FILMS (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS REVIEWS_LIKES
(
    user_id   INTEGER NOT NULL,
    review_id INTEGER NOT NULL,
    like_flag BOOLEAN NULL,
    PRIMARY KEY (user_id, review_id),
    FOREIGN KEY (user_id) REFERENCES USERS (id) ON DELETE CASCADE,
    FOREIGN KEY (review_id) REFERENCES REVIEWS (review_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS FEEDS
(
    id           INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    userId INTEGER NOT NULL,
    entityId INTEGER NOT NULL,
    timestamp DATE NOT NULL,
    eventType VARCHAR(15) NOT NULL,
    operation VARCHAR(15) NOT NULL,
    FOREIGN KEY (userId) REFERENCES USERS (id) ON DELETE CASCADE
);