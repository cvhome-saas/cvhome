INSERT INTO cua.social_login_configs (store_merchant_id, provider_id, app_id, app_secret, enabled)
VALUES ('65f020632bc46470c104b76f', 'GOOGLE', 'google-app-id-1', 'google-secret-1', true),
       ('65f020632bc46470c104b76f', 'GITHUB', 'githib-app-id-1', 'github-secret-1', true),
       ('65f020632bc46470c104b76f', 'FACEBOOK', 'facebook-app-id-1', 'facebook-secret-1', true)

ON CONFLICT (store_merchant_id, provider_id) DO NOTHING;


insert into cua.users (id, client_id, username, email, first_name, last_name, password_hash, enabled)
values ('318F2FD5-E235-4C2E-AB7E-6C949BA4CDD1', '65f020632bc46470c104b76f', 'user', 'user@mail.com', 'user',
        'user',
        '{bcrypt}$2a$10$pse9zsAXkH/zOjZpfiP7X.weD6CNtVY/NR5A4mYUwbYqcYThHchRa', TRUE)

on conflict (id) do nothing;
