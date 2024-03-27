INSERT INTO roles (ROLE_NAME, DESCRIPTION) VALUES ('ROLE_SUPER', 'Full-powered administrator. Audit. View reports.');
INSERT INTO roles (ROLE_NAME, DESCRIPTION) VALUES ('ROLE_ADMIN', 'User administrator role.');
INSERT INTO roles (ROLE_NAME, DESCRIPTION) VALUES ('ROLE_USER', 'Regular user role. Create/manage individual cocktails. Manage personal shelf.');

INSERT INTO users (USERNAME, PASSWORD, NAME, SURNAME, EMAIL, ACTIVE)
VALUES ('super', '$2a$10$SIEehouggTGGxNQJuSXk6OPb73phJ89xHBpZ.OT6F6FNqoGiWL.Vq', 'super', 'super', 'super@mybar.com', '1');
INSERT INTO users (USERNAME, PASSWORD, NAME, SURNAME, EMAIL, ACTIVE)
VALUES ('admin', '$2a$10$gZVn7/I2RWs5w6VjeGm/JuAJBygpv0Qa5d9Yxb/MXnY/TC4CBAvQm', 'admin', 'admin', 'admin@mybar.com', '1');

INSERT INTO user_has_roles (USERNAME, ROLE_NAME) VALUES ('super', 'ROLE_SUPER');
INSERT INTO user_has_roles (USERNAME, ROLE_NAME) VALUES ('admin', 'ROLE_ADMIN');
