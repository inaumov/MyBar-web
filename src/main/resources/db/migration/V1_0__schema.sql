CREATE TABLE bottle (
    id            VARCHAR(255) NOT NULL,
    brand_name    VARCHAR(255),
    image_url     VARCHAR(255),
    in_shelf      BOOLEAN DEFAULT FALSE,
    price         DECIMAL(19, 2),
    volume        DOUBLE PRECISION,
    ingredient_id INTEGER      NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE cocktail (
    id          VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    image_url   VARCHAR(255),
    menu_id     INTEGER,
    name        VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE cocktail_to_ingredient (
    measurement   VARCHAR(255),
    volume        DOUBLE PRECISION,
    ingredient_id INTEGER,
    cocktail_id   VARCHAR(255),
    PRIMARY KEY (cocktail_id, ingredient_id)
);

CREATE TABLE ingredient (
    group_name VARCHAR(31) NOT NULL,
    id         INTEGER     NOT NULL,
    kind       VARCHAR(255),
    type       VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE menu (
    id   INTEGER NOT NULL,
    name VARCHAR(255),
    PRIMARY KEY (id)
);

ALTER TABLE bottle
    ADD CONSTRAINT fk_bottle_ingredient_id
        FOREIGN KEY (ingredient_id)
            REFERENCES ingredient (id);

ALTER TABLE cocktail
    ADD CONSTRAINT fk_cocktail_menu_id
        FOREIGN KEY (menu_id)
            REFERENCES menu (id);

ALTER TABLE cocktail_to_ingredient
    ADD CONSTRAINT fk_cocktail_to_ingredient_ingredient_id
        FOREIGN KEY (ingredient_id)
            REFERENCES ingredient (id);

ALTER TABLE cocktail_to_ingredient
    ADD CONSTRAINT fk_cocktail_to_ingredient_cocktail_id
        FOREIGN KEY (cocktail_id)
            REFERENCES cocktail (id);
