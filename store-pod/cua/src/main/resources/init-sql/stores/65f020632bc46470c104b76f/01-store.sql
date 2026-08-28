INSERT INTO cua.social_login_configs (store_merchant_id, provider_id, app_id, app_secret, enabled)
VALUES ('65f020632bc46470c104b76f', 'GOOGLE', 'ENC:1:default-key:AES-256-GCM:wK1wBF3994Pl18DU:O+3nIg14UfunYfgwO2AChB/CjmvIWc1ZuIDsLMiJgN4K7E4LpBqTmj73sVW/WI8w+Z6KiRKz7aqFzwPIsyEuYypYRqGRY28GMmYYM9/AycTx8oTFimOwMA==', 'ENC:1:default-key:AES-256-GCM:L4DLeZZK1FrVt2HT:shNmEE6dRRnvp2QAJOn+Dj1glyNmw1m0OXMrDngOCPSi01Y3hS2G1L9IsJ16SEGOWuV9', true),
       ('65f020632bc46470c104b76f', 'GITHUB', 'ENC:1:default-key:AES-256-GCM:2ky6qTJ7Q526PUMh:Gtagmo7cH99rrfzHjhZwRvCamcbwU8Q8/gPghV3dUMbdMk8u', 'ENC:1:default-key:AES-256-GCM:gWao1lpRxoqZqqry:/qV5PXK+AM9ETM13Qs/SFhAbgxzcGH/qftlcAPT2GGeVmcXgHWEExTv/DN6mwMNPZm8u0uhq4qA=', true),
       ('65f020632bc46470c104b76f', 'FACEBOOK', 'facebook-app-id-1', 'facebook-secret-1', true)

ON CONFLICT (store_merchant_id, provider_id) DO NOTHING;


insert into cua.users (id, client_id, username, email, first_name, last_name, password_hash, enabled)
values ('318F2FD5-E235-4C2E-AB7E-6C949BA4CDD1', '65f020632bc46470c104b76f', 'user', 'user@mail.com', 'user',
        'user',
        '{bcrypt}$2a$10$pse9zsAXkH/zOjZpfiP7X.weD6CNtVY/NR5A4mYUwbYqcYThHchRa', TRUE)

on conflict (id) do nothing;
