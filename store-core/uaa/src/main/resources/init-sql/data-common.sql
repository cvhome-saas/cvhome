-- The one realm uaa serves, and the only one it ever will: its staff and service accounts are a single pool.
insert into uaa.realms (id, display_name) values ('platform', 'cvhome ID') on conflict (id) do nothing;

-- The platform realm's settings row; every column has its default.
insert into uaa.settings (realm_id) values ('platform') on conflict (realm_id) do nothing;

-- Seed roles. System roles cannot be renamed or deleted; their permissions can still be edited.
insert into uaa.roles (id, name, description, scope, system_role)
values ('c1e3a8e6-3a3e-4b1a-8e3e-3a3e4b1a8e3e', 'SUPER_ADMIN', 'The platform owner. Everything, everywhere.', 'REALM', true),
       ('d2f4b9f7-4b4f-5c2b-9f8f-4b4f5c2b9f8f', 'USER', 'A signed-in account with no administrative rights.', 'REALM', true),
       ('4CA169A8-E8AC-4874-ACAE-795BF7B27832', 'ORG_ADMIN', 'Owns an organization: its stores, members and billing.', 'ORGANIZATION', true),
       ('58C35650-746C-48F8-84E7-78E588045194', 'STORE_ADMIN', 'Runs one store: catalog, orders, content and staff.', 'ORGANIZATION', true),
       ('23BAB562-5FF0-4690-A0C2-89E2CEA6FCE8', 'STORE_MODERATOR', 'Reads a store and edits its content; no orders, no staff.', 'ORGANIZATION', true),
       ('7A1D2C3B-4E5F-4A6B-8C7D-9E0F1A2B3C4D', 'STORE_RETAIL', 'Point-of-sale staff for one store.', 'ORGANIZATION', true),
       ('9E2B7C10-5A4D-4F3E-8B6C-1D2E3F4A5B6C', 'SUPPORT', 'Platform support: finds a merchant and acts as them, read-only.', 'REALM', true)
on conflict (id) do nothing;

-- SUPER_ADMIN holds every permission in the catalogue (the enum in store-commons:commons is the source of truth).
insert into uaa.role_permissions (role_id, permission)
select 'c1e3a8e6-3a3e-4b1a-8e3e-3a3e4b1a8e3e', p
from unnest(array['users:read', 'users:write', 'users:invite', 'users:sessions', 'users:unlock',
                  'roles:read', 'roles:write', 'clients:read', 'clients:write', 'clients:secrets',
                  'idps:read', 'idps:write', 'settings:read', 'settings:write', 'audit:read',
                  'keys:read', 'keys:rotate', 'dashboard:read', 'users:impersonate']) as p
on conflict (role_id, permission) do nothing;

-- Organization and store roles: what the console lets them do, expressed as permissions the token will carry.
insert into uaa.role_permissions (role_id, permission)
values ('4CA169A8-E8AC-4874-ACAE-795BF7B27832', 'users:read'),
       ('4CA169A8-E8AC-4874-ACAE-795BF7B27832', 'users:write'),
       ('4CA169A8-E8AC-4874-ACAE-795BF7B27832', 'users:invite'),
       ('58C35650-746C-48F8-84E7-78E588045194', 'users:read'),
       ('58C35650-746C-48F8-84E7-78E588045194', 'users:write'),
       ('23BAB562-5FF0-4690-A0C2-89E2CEA6FCE8', 'users:read')
on conflict (role_id, permission) do nothing;

-- SUPPORT: enough to find a merchant account and act as it. Deliberately none of the write permissions, and none of
-- the roles a merchant screen authorises on — the way in is the impersonation grant, read-only.
insert into uaa.role_permissions (role_id, permission)
values ('9E2B7C10-5A4D-4F3E-8B6C-1D2E3F4A5B6C', 'users:read'),
       ('9E2B7C10-5A4D-4F3E-8B6C-1D2E3F4A5B6C', 'users:impersonate'),
       ('9E2B7C10-5A4D-4F3E-8B6C-1D2E3F4A5B6C', 'audit:read')
on conflict (role_id, permission) do nothing;

insert into uaa.users (id, username, email, first_name, last_name, password_hash, enabled, metadata)
values ('65D8419C-8765-4B8B-A15F-910DCE959931', 'super-admin', 'super-admin@mail.com', 'Super', 'Admin',
        '{bcrypt}$2a$10$pse9zsAXkH/zOjZpfiP7X.weD6CNtVY/NR5A4mYUwbYqcYThHchRa', TRUE, '{}')
on conflict (id) do nothing;

insert into uaa.user_roles (user_id, role_id)
values ('65D8419C-8765-4B8B-A15F-910DCE959931', 'c1e3a8e6-3a3e-4b1a-8e3e-3a3e4b1a8e3e')
on conflict (user_id, role_id) do nothing;

-- Seed OAuth clients. Token lifetimes: access 15 min, refresh 12 h, authorization code 5 min — a code is a
-- one-shot secret in a browser redirect and a day-long one defeats the point of the grant. web-app requires PKCE.
-- admin-sdk: client_credentials with scope super_admin
-- client_secret: secret
insert into uaa.oauth2_registered_client (id, client_id, client_id_issued_at, client_secret, client_name,
                                          client_authentication_methods, authorization_grant_types, redirect_uris,
                                          post_logout_redirect_uris, scopes, client_settings, token_settings)
values ('a5c7e2c0-7e7e-8f5f-c2d2-7e7e8f5fc2d2',
        'admin-sdk', now(), '{bcrypt}$2a$10$KEyYNPGHPotegD5Ui8/yX.WzIv75INVsEzgjkD2GqQdmcBG0qke8i', 'Admin SDK',
        'client_secret_basic', 'client_credentials',
        NULL, NULL,
        'super_admin',
        '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-proof-key":false,"settings.client.require-authorization-consent":false}',
        '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":false,"settings.token.x509-certificate-bound-access-tokens":false,"settings.token.id-token-signature-algorithm":["org.springframework.security.oauth2.jose.jws.SignatureAlgorithm","RS256"],"settings.token.access-token-time-to-live":["java.time.Duration",900.000000000],"settings.token.access-token-format":{"@class":"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat","value":"self-contained"},"settings.token.refresh-token-time-to-live":["java.time.Duration",43200.000000000],"settings.token.authorization-code-time-to-live":["java.time.Duration",300.000000000],"settings.token.device-code-time-to-live":["java.time.Duration",300.000000000]}')
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
        '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":false,"settings.token.x509-certificate-bound-access-tokens":false,"settings.token.id-token-signature-algorithm":["org.springframework.security.oauth2.jose.jws.SignatureAlgorithm","RS256"],"settings.token.access-token-time-to-live":["java.time.Duration",900.000000000],"settings.token.access-token-format":{"@class":"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat","value":"self-contained"},"settings.token.refresh-token-time-to-live":["java.time.Duration",43200.000000000],"settings.token.authorization-code-time-to-live":["java.time.Duration",300.000000000],"settings.token.device-code-time-to-live":["java.time.Duration",300.000000000]}')
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
        '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":false,"settings.token.x509-certificate-bound-access-tokens":false,"settings.token.id-token-signature-algorithm":["org.springframework.security.oauth2.jose.jws.SignatureAlgorithm","RS256"],"settings.token.access-token-time-to-live":["java.time.Duration",900.000000000],"settings.token.access-token-format":{"@class":"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat","value":"self-contained"},"settings.token.refresh-token-time-to-live":["java.time.Duration",43200.000000000],"settings.token.authorization-code-time-to-live":["java.time.Duration",300.000000000],"settings.token.device-code-time-to-live":["java.time.Duration",300.000000000]}')
on conflict (id) do nothing;

-- webapp: authorization_code + refresh_token with PKCE and consent
-- client_secret: secret
insert into uaa.oauth2_registered_client (id, client_id, client_id_issued_at, client_secret, client_name,
                                          client_authentication_methods, authorization_grant_types, redirect_uris,
                                          post_logout_redirect_uris, scopes, client_settings, token_settings)
values ('b6d8f3d1-8f8f-9060-d3e3-8f8f9060d3e3',
        'web-app', now(), '{bcrypt}$2a$10$KEyYNPGHPotegD5Ui8/yX.WzIv75INVsEzgjkD2GqQdmcBG0qke8i', 'Web App',
        'client_secret_basic', 'authorization_code,refresh_token',
        'http://localhost:8000/login/oauth2/code/uaa,http://gateway.com:8000/login/oauth2/code/uaa,http://console-ui.gateway.com:8000/login/oauth2/code/uaa',
        'http://localhost:8000,http://gateway.com:8000,http://console-ui.gateway.com:8000',
        'openid',
        '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-proof-key":true,"settings.client.require-authorization-consent":true}',
        '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":false,"settings.token.x509-certificate-bound-access-tokens":false,"settings.token.id-token-signature-algorithm":["org.springframework.security.oauth2.jose.jws.SignatureAlgorithm","RS256"],"settings.token.access-token-time-to-live":["java.time.Duration",900.000000000],"settings.token.access-token-format":{"@class":"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat","value":"self-contained"},"settings.token.refresh-token-time-to-live":["java.time.Duration",43200.000000000],"settings.token.authorization-code-time-to-live":["java.time.Duration",300.000000000],"settings.token.device-code-time-to-live":["java.time.Duration",300.000000000]}')
on conflict (id) do nothing;

-- console-impersonation: the gateway's impersonation client — token exchange only, no refresh_token grant, so an
-- exchanged token cannot renew itself. Its secret is the gateway's alone; the grant refuses every other client.
-- client_secret: secret
insert into uaa.oauth2_registered_client (id, client_id, client_id_issued_at, client_secret, client_name,
                                          client_authentication_methods, authorization_grant_types, redirect_uris,
                                          post_logout_redirect_uris, scopes, client_settings, token_settings)
values ('C0A5E3D7-2B1F-4E8A-9C6D-3F2E1D0C9B8A',
        'console-impersonation', now(), '{bcrypt}$2a$10$KEyYNPGHPotegD5Ui8/yX.WzIv75INVsEzgjkD2GqQdmcBG0qke8i',
        'Console Impersonation',
        'client_secret_basic', 'urn:ietf:params:oauth:grant-type:token-exchange',
        NULL, NULL,
        'openid',
        '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-proof-key":false,"settings.client.require-authorization-consent":false}',
        '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":false,"settings.token.x509-certificate-bound-access-tokens":false,"settings.token.id-token-signature-algorithm":["org.springframework.security.oauth2.jose.jws.SignatureAlgorithm","RS256"],"settings.token.access-token-time-to-live":["java.time.Duration",900.000000000],"settings.token.access-token-format":{"@class":"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat","value":"self-contained"},"settings.token.refresh-token-time-to-live":["java.time.Duration",900.000000000],"settings.token.authorization-code-time-to-live":["java.time.Duration",300.000000000],"settings.token.device-code-time-to-live":["java.time.Duration",300.000000000]}')
on conflict (id) do nothing;

-- Every seeded client is enabled; the row is what enable/disable and last-token tracking write to.
insert into uaa.client_extension (registered_client_id, enabled)
values ('a5c7e2c0-7e7e-8f5f-c2d2-7e7e8f5fc2d2', true),
       ('BECF0252-14DD-437A-85B8-0C8EEF1BD03F', true),
       ('608A79F2-CB4D-42CA-8BA9-2571DE69BDE8', true),
       ('b6d8f3d1-8f8f-9060-d3e3-8f8f9060d3e3', true),
       ('C0A5E3D7-2B1F-4E8A-9C6D-3F2E1D0C9B8A', true)
on conflict (registered_client_id) do nothing;
