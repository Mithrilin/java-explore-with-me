drop table if exists users, categories;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
  email VARCHAR(254) NOT NULL UNIQUE,
  name VARCHAR(250) NOT NULL
);

CREATE TABLE IF NOT EXISTS categories (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS events (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
  annotation VARCHAR(2000) NOT NULL,
  category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
  confirmed_requests BIGINT NOT NULL,
  created_on TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  description VARCHAR(7000) NOT NULL,
  event_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
);
