-- Seed OAuth clients (note: settings are simplified and may be adjusted per SAS version)
-- admin-sdk: client_credentials with scope super_admin
-- client_secret: secret
insert into uaa.oauth2_registered_client (id, client_id, client_id_issued_at, client_secret, client_name,
                                             client_authentication_methods, authorization_grant_types, redirect_uris,
                                             post_logout_redirect_uris, scopes, client_settings, token_settings)
values ('a5c7e2c0-7e7e-8f5f-c2d2-7e7e8f5fc2d2',
        'admin-sdk', now(), '{bcrypt}$2a$10$KEyYNPGHPotegD5Ui8/yX.WzIv75INVsEzgjkD2GqQdmcBG0qke8i', 'Admin SDK',
        'client_secret_basic', 'client_credentials,refresh_token',
        NULL, NULL,
        'super_admin',
        '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-proof-key":false,"settings.client.require-authorization-consent":true}',
        '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":false,"settings.token.x509-certificate-bound-access-tokens":false,"settings.token.id-token-signature-algorithm":["org.springframework.security.oauth2.jose.jws.SignatureAlgorithm","RS256"],"settings.token.access-token-time-to-live":["java.time.Duration",86400.000000000],"settings.token.access-token-format":{"@class":"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat","value":"self-contained"},"settings.token.refresh-token-time-to-live":["java.time.Duration",86400.000000000],"settings.token.authorization-code-time-to-live":["java.time.Duration",86400.000000000],"settings.token.device-code-time-to-live":["java.time.Duration",300.000000000]}')
on conflict (id) do nothing;

-- store-pod-1@service.store-pod.internal
-- client_secret: secret
insert into uaa.oauth2_registered_client (id, client_id, client_id_issued_at, client_secret, client_name,
                                             client_authentication_methods, authorization_grant_types, redirect_uris,
                                             post_logout_redirect_uris, scopes, client_settings, token_settings)
values ('BECF0252-14DD-437A-85B8-0C8EEF1BD03F',
        'store-pod-1@service.store-pod.internal', now(), '{bcrypt}$2a$10$KEyYNPGHPotegD5Ui8/yX.WzIv75INVsEzgjkD2GqQdmcBG0qke8i', 'Store Service (Store Pod 1)',
        'client_secret_basic', 'client_credentials',
        NULL, NULL,
        'internal',
        '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-proof-key":false,"settings.client.require-authorization-consent":false,"resource":"pod-1"}',
        '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":false,"settings.token.x509-certificate-bound-access-tokens":false,"settings.token.id-token-signature-algorithm":["org.springframework.security.oauth2.jose.jws.SignatureAlgorithm","RS256"],"settings.token.access-token-time-to-live":["java.time.Duration",86400.000000000],"settings.token.access-token-format":{"@class":"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat","value":"self-contained"},"settings.token.refresh-token-time-to-live":["java.time.Duration",86400.000000000],"settings.token.authorization-code-time-to-live":["java.time.Duration",86400.000000000],"settings.token.device-code-time-to-live":["java.time.Duration",300.000000000]}')
on conflict (id) do nothing;

-- store-core@service.store-core.internal
-- client_secret: secret
insert into uaa.oauth2_registered_client (id, client_id, client_id_issued_at, client_secret, client_name,
                                             client_authentication_methods, authorization_grant_types, redirect_uris,
                                             post_logout_redirect_uris, scopes, client_settings, token_settings)
values ('608A79F2-CB4D-42CA-8BA9-2571DE69BDE8',
        'store-core@service.store-core.internal', now(), '{bcrypt}$2a$10$KEyYNPGHPotegD5Ui8/yX.WzIv75INVsEzgjkD2GqQdmcBG0qke8i', 'Store Core (Store Core)',
        'client_secret_basic', 'client_credentials',
        NULL, NULL,
        'internal,store',
        '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-proof-key":false,"settings.client.require-authorization-consent":false}',
        '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":false,"settings.token.x509-certificate-bound-access-tokens":false,"settings.token.id-token-signature-algorithm":["org.springframework.security.oauth2.jose.jws.SignatureAlgorithm","RS256"],"settings.token.access-token-time-to-live":["java.time.Duration",86400.000000000],"settings.token.access-token-format":{"@class":"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat","value":"self-contained"},"settings.token.refresh-token-time-to-live":["java.time.Duration",86400.000000000],"settings.token.authorization-code-time-to-live":["java.time.Duration",86400.000000000],"settings.token.device-code-time-to-live":["java.time.Duration",300.000000000]}')
on conflict (id) do nothing;

-- webapp: authorization_code + refresh_token with PKCE and consent
-- client_secret: secret
insert into uaa.oauth2_registered_client (id, client_id, client_id_issued_at, client_secret, client_name,
                                             client_authentication_methods, authorization_grant_types, redirect_uris,
                                             post_logout_redirect_uris, scopes, client_settings, token_settings)
values ('b6d8f3d1-8f8f-9060-d3e3-8f8f9060d3e3',
        'web-app', now(), '{bcrypt}$2a$10$KEyYNPGHPotegD5Ui8/yX.WzIv75INVsEzgjkD2GqQdmcBG0qke8i', 'Web App',
        'client_secret_basic', 'authorization_code,refresh_token',
        'http://localhost:8000/login/oauth2/code/uaa,http://gateway.com:8000/login/oauth2/code/uaa,http://seller-ui.gateway.com:8000/login/oauth2/code/uaa,https://asrevo.click/login/oauth2/code/uaa,https://www.asrevo.click/login/oauth2/code/uaa,https://seller-ui.asrevo.click/login/oauth2/code/uaa',
        'http://localhost:8000,http://gateway.com:8000,http://seller-ui.gateway.com:8000,https://asrevo.click,https://www.asrevo.click,https://seller-ui.asrevo.click',
        'openid',
        '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-proof-key":false,"settings.client.require-authorization-consent":true}',
        '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":false,"settings.token.x509-certificate-bound-access-tokens":false,"settings.token.id-token-signature-algorithm":["org.springframework.security.oauth2.jose.jws.SignatureAlgorithm","RS256"],"settings.token.access-token-time-to-live":["java.time.Duration",86400.000000000],"settings.token.access-token-format":{"@class":"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat","value":"self-contained"},"settings.token.refresh-token-time-to-live":["java.time.Duration",86400.000000000],"settings.token.authorization-code-time-to-live":["java.time.Duration",86400.000000000],"settings.token.device-code-time-to-live":["java.time.Duration",300.000000000]}')
on conflict (id) do nothing;


-- Seed roles
insert into uaa.roles (id, name)
values ('c1e3a8e6-3a3e-4b1a-8e3e-3a3e4b1a8e3e', 'SUPER_ADMIN'),
       ('d2f4b9f7-4b4f-5c2b-9f8f-4b4f5c2b9f8f', 'USER'),
       ('4CA169A8-E8AC-4874-ACAE-795BF7B27832', 'ORG_ADMIN'),
       ('58C35650-746C-48F8-84E7-78E588045194', 'STORE_ADMIN'),
       ('23BAB562-5FF0-4690-A0C2-89E2CEA6FCE8', 'STORE_MODERATOR')
on conflict (id) do nothing;

insert into uaa.users (id, username, email, first_name, last_name, password_hash, enabled, metadata)
values ('65D8419C-8765-4B8B-A15F-910DCE959931', 'super-admin', 'super-admin@mail.com', 'Super', 'Admin',
        '{bcrypt}$2a$10$pse9zsAXkH/zOjZpfiP7X.weD6CNtVY/NR5A4mYUwbYqcYThHchRa', TRUE, '{}'),
       ('318F2FD5-E235-4C2E-AB7E-6C949BA4CDD4', 'org1-admin', 'org1-admin@mail.com', 'Org1', 'Admin',
        '{bcrypt}$2a$10$pse9zsAXkH/zOjZpfiP7X.weD6CNtVY/NR5A4mYUwbYqcYThHchRa', TRUE,
        '{"org": "21f023932bc66470c104b76f"}'),
       ('60AB49A5-7F06-4B5A-BE81-9B30BB6559AE', 'org1-store1-admin', 'org1-store1-admin@mail.com', 'Store1', 'Admin',
        '{bcrypt}$2a$10$pse9zsAXkH/zOjZpfiP7X.weD6CNtVY/NR5A4mYUwbYqcYThHchRa', TRUE,
        '{"org": "21f023932bc66470c104b76f", "store": "65f023632bc46470c104b76f"}'),
       ('0C1C7C69-F504-47E2-AA5D-3348CBD1023F', 'org1-store1-moderator', 'org1-store1-moderator@mail.com', 'Store1', 'Moderator',
        '{bcrypt}$2a$10$pse9zsAXkH/zOjZpfiP7X.weD6CNtVY/NR5A4mYUwbYqcYThHchRa', TRUE,
        '{"org": "21f023932bc66470c104b76f", "store": "65f023632bc46470c104b76f"}'),
       ('E91EB99A-D3C9-4FBE-8CD3-8744E4F6CA29', 'org1-store2-admin', 'org1-store2-admin@mail.com', 'Store2', 'Admin',
        '{bcrypt}$2a$10$pse9zsAXkH/zOjZpfiP7X.weD6CNtVY/NR5A4mYUwbYqcYThHchRa', TRUE,
        '{"org": "21f023932bc66470c104b76f", "store": "65f023632bc46470c104b75f"}'),
       ('77E97256-9706-44A9-A640-52AF9C65E5EA', 'org1-store2-moderator', 'org1-store2-moderator@mail.com', 'Store2', 'Moderator',
        '{bcrypt}$2a$10$pse9zsAXkH/zOjZpfiP7X.weD6CNtVY/NR5A4mYUwbYqcYThHchRa', TRUE,
        '{"org": "21f023932bc66470c104b76f", "store": "65f023632bc46470c104b75f"}'),
       ('7B54CF3C-5510-40BF-BD0B-14C4078EDF07', 'org2-admin', 'org2-admin@mail.com', 'Org2', 'Admin',
        '{bcrypt}$2a$10$pse9zsAXkH/zOjZpfiP7X.weD6CNtVY/NR5A4mYUwbYqcYThHchRa', TRUE,
        '{"org": "352023632b046970c104b76f"}'),
       ('6C303A6A-459C-44FB-B4B2-EAB53FB2B325', 'org2-store1-admin', 'org2-store1-admin@mail.com', 'Store1', 'Admin',
        '{bcrypt}$2a$10$pse9zsAXkH/zOjZpfiP7X.weD6CNtVY/NR5A4mYUwbYqcYThHchRa', TRUE,
        '{"org": "352023632b046970c104b76f", "store": "65f020632bc46470c104b76f"}'),
       ('F900B31E-376E-4757-8EAB-501ABA2CFDD3', 'org2-store1-moderator', 'org2-store1-moderator@mail.com', 'Store1', 'Moderator',
        '{bcrypt}$2a$10$pse9zsAXkH/zOjZpfiP7X.weD6CNtVY/NR5A4mYUwbYqcYThHchRa', TRUE,
        '{"org": "352023632b046970c104b76f", "store": "65f020632bc46470c104b76f"}'),
       ('F1FA1FA2-51F3-41CD-A816-356F6816ABD4', 'org2-store2-admin', 'org2-store2-admin@mail.com', 'Store2', 'Admin',
        '{bcrypt}$2a$10$pse9zsAXkH/zOjZpfiP7X.weD6CNtVY/NR5A4mYUwbYqcYThHchRa', TRUE,
        '{"org": "352023632b046970c104b76f", "store": "65f023632bc26470c104b75f"}'),
       ('97022CD5-CC0A-467A-A99A-460B8E2745C3', 'org2-store2-moderator', 'org2-store2-moderator@mail.com', 'Store2', 'Moderator',
        '{bcrypt}$2a$10$pse9zsAXkH/zOjZpfiP7X.weD6CNtVY/NR5A4mYUwbYqcYThHchRa', TRUE,
        '{"org": "352023632b046970c104b76f", "store": "65f023632bc26470c104b75f"}')
on conflict (id) do nothing;

-- Map user roles
insert into uaa.user_roles (user_id, role_id) values
  ('65D8419C-8765-4B8B-A15F-910DCE959931','c1e3a8e6-3a3e-4b1a-8e3e-3a3e4b1a8e3e'),
  ('318F2FD5-E235-4C2E-AB7E-6C949BA4CDD4','4CA169A8-E8AC-4874-ACAE-795BF7B27832'),
  ('60AB49A5-7F06-4B5A-BE81-9B30BB6559AE','58C35650-746C-48F8-84E7-78E588045194'),
  ('0C1C7C69-F504-47E2-AA5D-3348CBD1023F','23BAB562-5FF0-4690-A0C2-89E2CEA6FCE8'),
  ('E91EB99A-D3C9-4FBE-8CD3-8744E4F6CA29','58C35650-746C-48F8-84E7-78E588045194'),
  ('77E97256-9706-44A9-A640-52AF9C65E5EA','23BAB562-5FF0-4690-A0C2-89E2CEA6FCE8'),
  ('7B54CF3C-5510-40BF-BD0B-14C4078EDF07','4CA169A8-E8AC-4874-ACAE-795BF7B27832'),
  ('6C303A6A-459C-44FB-B4B2-EAB53FB2B325','58C35650-746C-48F8-84E7-78E588045194'),
  ('F900B31E-376E-4757-8EAB-501ABA2CFDD3','23BAB562-5FF0-4690-A0C2-89E2CEA6FCE8'),
  ('F1FA1FA2-51F3-41CD-A816-356F6816ABD4','58C35650-746C-48F8-84E7-78E588045194'),
  ('97022CD5-CC0A-467A-A99A-460B8E2745C3','23BAB562-5FF0-4690-A0C2-89E2CEA6FCE8')
on conflict (user_id, role_id) do nothing;



