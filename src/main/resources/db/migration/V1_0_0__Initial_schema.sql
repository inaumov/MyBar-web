CREATE TABLE bottles
(
    id            VARCHAR(255) NOT NULL,
    brand_name    VARCHAR(255),
    image_url     VARCHAR(255),
    in_shelf      BOOLEAN DEFAULT FALSE,
    price         DECIMAL(19, 2),
    volume        DOUBLE PRECISION,
    ingredient_id INTEGER      NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE cocktails
(
    id          VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    image_url   VARCHAR(255),
    menu_id     INTEGER,
    name        VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE cocktails_to_ingredients
(
    measurement   VARCHAR(255),
    volume        DOUBLE PRECISION,
    ingredient_id INTEGER,
    cocktail_id   VARCHAR(255),
    PRIMARY KEY (cocktail_id, ingredient_id)
);

CREATE TABLE ingredients
(
    group_name VARCHAR(31) NOT NULL,
    id         INTEGER     NOT NULL,
    kind       VARCHAR(255),
    type       VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE menu
(
    id   INTEGER NOT NULL,
    name VARCHAR(255),
    PRIMARY KEY (id)
);

ALTER TABLE bottles
    ADD FOREIGN KEY (ingredient_id)
        REFERENCES ingredients (id);

ALTER TABLE cocktails
    ADD FOREIGN KEY (menu_id)
        REFERENCES menu (id);

ALTER TABLE cocktails_to_ingredients
    ADD FOREIGN KEY (ingredient_id)
        REFERENCES ingredients (id);

ALTER TABLE cocktails_to_ingredients
    ADD FOREIGN KEY (cocktail_id)
        REFERENCES cocktails (id);
