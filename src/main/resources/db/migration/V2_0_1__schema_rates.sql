CREATE TABLE rates (
  stars       INTEGER,
  rated_at    TIMESTAMP DEFAULT current_timestamp,
  username    VARCHAR(255) NOT NULL,
  cocktail_id VARCHAR(255) NOT NULL,
  PRIMARY KEY (cocktail_id, username)
);

ALTER TABLE rates
  ADD CONSTRAINT fk_rates_username
      FOREIGN KEY (username)
          REFERENCES users (username);

ALTER TABLE rates
  ADD CONSTRAINT fk_rates_cocktail_id
      FOREIGN KEY (cocktail_id)
          REFERENCES cocktail (id);
