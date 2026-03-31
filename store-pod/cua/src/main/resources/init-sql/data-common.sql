insert into cua.users (id, client_id, username, email, first_name, last_name, password_hash, enabled)
values ('318F2FD5-E235-4C2E-AB7E-6C949BA4CDD4', '65f023632bc46470c104b76f', 'org1-admin', 'org1-admin@mail.com', 'Org1', 'Admin',
        '{bcrypt}$2a$10$pse9zsAXkH/zOjZpfiP7X.weD6CNtVY/NR5A4mYUwbYqcYThHchRa', TRUE),
    ('318F2FD5-E235-4C2E-AB7E-6C949BA4CDD4', '65f023632bc46470c104b75f', 'org1-admin', 'org1-admin@mail.com', 'Org1', 'Admin',
        '{bcrypt}$2a$10$pse9zsAXkH/zOjZpfiP7X.weD6CNtVY/NR5A4mYUwbYqcYThHchRa', TRUE)

on conflict (id) do nothing;

INSERT INTO cua.social_login_configs (id, client_id, provider_id, app_id, app_secret, scopes)
VALUES ('00000000-0000-0000-0000-65f020632bc4', '65f020632bc46470c104b76f', 'google', 'google-app-id-1', 'google-secret-1', 'openid,profile,email'),
       ('00000000-0000-0000-0000-65f020632bc5', '65f020632bc46470c104b76f', 'facebook', 'fb-app-id-1', 'fb-secret-1', 'email,public_profile'),
       ('00000000-0000-0000-0000-65f023632bc2', '65f023632bc26470c104b75f', 'google', 'google-app-id-2', 'google-secret-2', 'openid,profile,email'),
       ('00000000-0000-0000-0000-65f023632bc4', '65f023632bc46470c104b75f', 'google', 'google-app-id-3', 'google-secret-3', 'openid,profile,email'),
       ('00000000-0000-0000-0000-65f023632bc6', '65f023632bc46470c104b76f', 'google', 'google-app-id-4', 'google-secret-4', 'openid,profile,email'),
       ('00000000-0000-0000-0000-65f023632bc7', '65f023632bc26470c104b75f', 'github', 'github-app-id-1', 'github-secret-1', 'read:user,user:email')
ON CONFLICT (id) DO NOTHING;



