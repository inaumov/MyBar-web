CREATE TABLE roles
(
    role_name   VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    PRIMARY KEY (role_name)
);

CREATE TABLE users
(
    username VARCHAR(255) NOT NULL,
    active   BOOLEAN DEFAULT TRUE,
    email    VARCHAR(255),
    name     VARCHAR(255),
    password VARCHAR(255),
    surname  VARCHAR(255),
    PRIMARY KEY (username)
);

CREATE TABLE users_has_roles
(
    username  VARCHAR(255) NOT NULL,
    role_name VARCHAR(255) NOT NULL
);

ALTER TABLE users_has_roles
    ADD FOREIGN KEY (role_name)
        REFERENCES roles (role_name);

ALTER TABLE users_has_roles
    ADD FOREIGN KEY (username)
        REFERENCES users (username);
