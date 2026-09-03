-- Demo store 1: the realm, its policy, one shopper and the providers it brokers.
--
-- The realm row is what makes this a store cua serves: without it there is no OAuth2 client and the
-- authorize endpoint answers "no such client", which is how an unknown store is refused now.
insert into cua.realms (id, display_name) values ('65f020632bc46470c104b76f', 'Demo store 1')
on conflict (id) do nothing;

-- Shoppers sign themselves up, so unlike the platform realm this one has self-registration on.
insert into cua.settings (realm_id, display_name, self_registration_enabled)
values ('65f020632bc46470c104b76f', 'Demo store 1', true)
on conflict (realm_id) do nothing;

-- password: revo
insert into cua.users (id, realm_id, username, email, first_name, last_name, password_hash, enabled)
values ('318F2FD5-E235-4C2E-AB7E-6C949BA4CDD1', '65f020632bc46470c104b76f', 'user', 'user@mail.com', 'user', 'user',
        '{bcrypt}$2a$10$pse9zsAXkH/zOjZpfiP7X.weD6CNtVY/NR5A4mYUwbYqcYThHchRa', true)
on conflict (id) do nothing;

-- Brokered providers. The alias is the Spring registrationId and is unique per realm, so every store may
-- have its own 'google'. Client ids and secrets are secret-crypto envelopes.
--
-- The endpoint columns are spelled out because a stored provider is self-contained: IdentityProviderMapper
-- resolves a preset's defaults into the row when one is created through the API, and nothing fills them in on
-- read. A row seeded without them is a provider that cannot be built — "give an issuer to discover from, or the
-- authorization and token endpoints by hand". These mirror IdpPreset.GOOGLE and IdpPreset.GITHUB.
insert into cua.identity_providers (id, realm_id, alias, display_name, type, preset, enabled,
                                    hide_on_login, sort_order, client_id_enc, client_secret_enc,
                                    issuer_uri, authorization_uri, token_uri, user_info_uri, jwk_set_uri,
                                    scopes, user_name_attribute, client_auth_method, attribute_mapping,
                                    account_linking, jit_provisioning, trust_email_verified,
                                    created_at, updated_at)
values
    (gen_random_uuid(), '65f020632bc46470c104b76f', 'google', 'Google', 'OIDC', 'GOOGLE', true, false, 0,
     'ENC:1:default-key:AES-256-GCM:wK1wBF3994Pl18DU:O+3nIg14UfunYfgwO2AChB/CjmvIWc1ZuIDsLMiJgN4K7E4LpBqTmj73sVW/WI8w+Z6KiRKz7aqFzwPIsyEuYypYRqGRY28GMmYYM9/AycTx8oTFimOwMA==',
     'ENC:1:default-key:AES-256-GCM:L4DLeZZK1FrVt2HT:shNmEE6dRRnvp2QAJOn+Dj1glyNmw1m0OXMrDngOCPSi01Y3hS2G1L9IsJ16SEGOWuV9',
     'https://accounts.google.com', 'https://accounts.google.com/o/oauth2/v2/auth',
     'https://oauth2.googleapis.com/token', 'https://openidconnect.googleapis.com/v1/userinfo',
     'https://www.googleapis.com/oauth2/v3/certs',
     'openid profile email', 'sub', 'client_secret_basic',
     '{"email": "email", "given_name": "firstName", "family_name": "lastName"}'::jsonb,
     'CONFIRM', true, true, now(), now()),
    (gen_random_uuid(), '65f020632bc46470c104b76f', 'github', 'GitHub', 'OAUTH2', 'GITHUB', true, false, 1,
     'ENC:1:default-key:AES-256-GCM:2ky6qTJ7Q526PUMh:Gtagmo7cH99rrfzHjhZwRvCamcbwU8Q8/gPghV3dUMbdMk8u',
     'ENC:1:default-key:AES-256-GCM:gWao1lpRxoqZqqry:/qV5PXK+AM9ETM13Qs/SFhAbgxzcGH/qftlcAPT2GGeVmcXgHWEExTv/DN6mwMNPZm8u0uhq4qA=',
     null, 'https://github.com/login/oauth/authorize',
     'https://github.com/login/oauth/access_token', 'https://api.github.com/user',
     null,
     'read:user user:email', 'id', 'client_secret_basic',
     '{"email": "email", "name": "firstName"}'::jsonb,
     'CONFIRM', true, true, now(), now())
on conflict do nothing;

-- The storefront's OAuth2 client. A public client with PKCE — the storefront is a browser app and holds no secret.
--
-- redirect_uris is a placeholder: the real one is derived per request from the host the shopper is on, because a
-- store is reached on its subdomain and on any custom domain the merchant points at it, in any of its languages.
-- The row exists because an issued authorization carries a foreign key to the client.
--
-- The authorization code lives five minutes, not the day it used to: a code that leaks through a Referer header,
-- browser history or a log is a credential for as long as it lives.
insert into cua.oauth2_registered_client (id, client_id, client_id_issued_at, client_secret, client_name,
                                          client_authentication_methods, authorization_grant_types, redirect_uris,
                                          post_logout_redirect_uris, scopes, client_settings, token_settings)
values ('65f020632bc46470c104b76f', '65f020632bc46470c104b76f', now(), null, 'Storefront',
        'none', 'authorization_code,refresh_token',
        'https://storefront.invalid/callback', 'https://storefront.invalid',
        'openid',
        '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-proof-key":true,"settings.client.require-authorization-consent":false}',
        '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":false,"settings.token.x509-certificate-bound-access-tokens":false,"settings.token.id-token-signature-algorithm":["org.springframework.security.oauth2.jose.jws.SignatureAlgorithm","RS256"],"settings.token.access-token-time-to-live":["java.time.Duration",900.000000000],"settings.token.access-token-format":{"@class":"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat","value":"self-contained"},"settings.token.refresh-token-time-to-live":["java.time.Duration",2592000.000000000],"settings.token.authorization-code-time-to-live":["java.time.Duration",300.000000000],"settings.token.device-code-time-to-live":["java.time.Duration",300.000000000]}')
on conflict (id) do nothing;
