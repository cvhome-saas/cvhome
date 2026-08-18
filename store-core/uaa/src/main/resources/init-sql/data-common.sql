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
        '{bcrypt}$2a$10$pse9zsAXkH/zOjZpfiP7X.weD6CNtVY/NR5A4mYUwbYqcYThHchRa', TRUE, '{}')
on conflict (id) do nothing;

insert into uaa.user_roles (user_id, role_id)
values ('65D8419C-8765-4B8B-A15F-910DCE959931', 'c1e3a8e6-3a3e-4b1a-8e3e-3a3e4b1a8e3e')
on conflict (user_id, role_id) do nothing;

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

-- store-pod-507f1f77@service.store-pod.internal
-- client_secret: secret
insert into uaa.oauth2_registered_client (id, client_id, client_id_issued_at, client_secret, client_name,
                                          client_authentication_methods, authorization_grant_types, redirect_uris,
                                          post_logout_redirect_uris, scopes, client_settings, token_settings)
values ('BECF0252-14DD-437A-85B8-0C8EEF1BD03F',
        'store-pod-507f1f77@service.store-pod.internal', now(),
        '{bcrypt}$2a$10$KEyYNPGHPotegD5Ui8/yX.WzIv75INVsEzgjkD2GqQdmcBG0qke8i', 'Store Service (Store Pod 1)',
        'client_secret_basic', 'client_credentials',
        NULL, NULL,
        'store_pod',
        '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-proof-key":false,"settings.client.require-authorization-consent":false,"resource":"pod-507f1f77"}',
        '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":false,"settings.token.x509-certificate-bound-access-tokens":false,"settings.token.id-token-signature-algorithm":["org.springframework.security.oauth2.jose.jws.SignatureAlgorithm","RS256"],"settings.token.access-token-time-to-live":["java.time.Duration",86400.000000000],"settings.token.access-token-format":{"@class":"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat","value":"self-contained"},"settings.token.refresh-token-time-to-live":["java.time.Duration",86400.000000000],"settings.token.authorization-code-time-to-live":["java.time.Duration",86400.000000000],"settings.token.device-code-time-to-live":["java.time.Duration",300.000000000]}')
on conflict (id) do nothing;

-- store-core@service.store-core.internal
-- client_secret: secret
insert into uaa.oauth2_registered_client (id, client_id, client_id_issued_at, client_secret, client_name,
                                          client_authentication_methods, authorization_grant_types, redirect_uris,
                                          post_logout_redirect_uris, scopes, client_settings, token_settings)
values ('608A79F2-CB4D-42CA-8BA9-2571DE69BDE8',
        'store-core@service.store-core.internal', now(),
        '{bcrypt}$2a$10$KEyYNPGHPotegD5Ui8/yX.WzIv75INVsEzgjkD2GqQdmcBG0qke8i', 'Store Core (Store Core)',
        'client_secret_basic', 'client_credentials',
        NULL, NULL,
        'store_core',
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
        'http://localhost:8000/login/oauth2/code/uaa,http://gateway.com:8000/login/oauth2/code/uaa,http://seller-ui.gateway.com:8000/login/oauth2/code/uaa,http://console-ui.gateway.com:8000/login/oauth2/code/uaa',
        'http://localhost:8000,http://gateway.com:8000,http://seller-ui.gateway.com:8000,http://console-ui.gateway.com:8000',
        'openid',
        '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-proof-key":false,"settings.client.require-authorization-consent":true}',
        '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":false,"settings.token.x509-certificate-bound-access-tokens":false,"settings.token.id-token-signature-algorithm":["org.springframework.security.oauth2.jose.jws.SignatureAlgorithm","RS256"],"settings.token.access-token-time-to-live":["java.time.Duration",86400.000000000],"settings.token.access-token-format":{"@class":"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat","value":"self-contained"},"settings.token.refresh-token-time-to-live":["java.time.Duration",86400.000000000],"settings.token.authorization-code-time-to-live":["java.time.Duration",86400.000000000],"settings.token.device-code-time-to-live":["java.time.Duration",300.000000000]}')
on conflict (id) do nothing;