insert into cua.users (id, client_id, username, email, first_name, last_name, password_hash, enabled)
values ('318F2FD5-E235-4C2E-AB7E-6C949BA4CDD4', '65f023632bc46470c104b76f', 'org1-admin', 'org1-admin@mail.com', 'Org1', 'Admin',
        '{bcrypt}$2a$10$pse9zsAXkH/zOjZpfiP7X.weD6CNtVY/NR5A4mYUwbYqcYThHchRa', TRUE),
    ('318F2FD5-E235-4C2E-AB7E-6C949BA4CDD4', '65f023632bc46470c104b75f', 'org1-admin', 'org1-admin@mail.com', 'Org1', 'Admin',
        '{bcrypt}$2a$10$pse9zsAXkH/zOjZpfiP7X.weD6CNtVY/NR5A4mYUwbYqcYThHchRa', TRUE)

on conflict (id) do nothing;

INSERT INTO cua.social_login_configs (store_merchant_id, provider_id, app_id, app_secret, enabled)
VALUES ( '65f020632bc46470c104b76f', 'GOOGLE', 'google-app-id-1', 'google-secret-1',true),
       ('65f023632bc26470c104b75f', 'GOOGLE', 'google-app-id-2', 'google-secret-2',true),
       ('65f023632bc46470c104b75f', 'GOOGLE', 'google-app-id-3', 'google-secret-3',true),
       ('65f023632bc46470c104b76f', 'GOOGLE', 'g_app', 'g_secret',true),
       ( '65f023632bc46470c104b76f', 'GITHUB', 'gh_app', 'gh_secret',true),
       ( '65f023632bc46470c104b76f', 'FACEBOOK', 'fb_app', 'fb_secret',true)

ON CONFLICT (store_merchant_id, provider_id) DO NOTHING;



