DROP DATABASE IF EXISTS personal_recommender;
CREATE DATABASE personal_recommender OWNER personal;
\c personal_recommender;

CREATE TABLE track(
  id SERIAL PRIMARY KEY,
  spotify_id TEXT NOT NULL,
  name TEXT NOT NULL,
  stars SMALLINT
);
