-- Seed rows for the store the payment integration tests read.
--
-- The two STRIPE secrets are ENC:1:… envelopes encrypted with the key pinned in
-- `application-test-stores.yml` (com.asrevo.cvhome.crypto.local.key). They decrypt to
-- `sk_test_seeded_store_one` and `whsec_test_seeded_store_one`.
--
-- They were previously encrypted under whichever key the author's machine held in
-- ~/.cvhome/secret-crypto/keys, so they decrypted there and nowhere else: CI resolves no ENV or FILE key, falls
-- back to a RANDOM one, and PaymentConfigurationMapper turns the resulting failure into a null. Re-encrypting them
-- under a key that lives in the repository is what makes the fixture mean the same thing everywhere.
INSERT INTO payment.payment_configuration (store_merchant_id, payment_type, api_key, secret_key, webhook_secret,
                                           enabled)
VALUES ('65f023632bc46470c104b76f', 'STRIPE', null, 'ENC:1:default-key:AES-256-GCM:pGimC4MS14Qk7R0u:Wr5Xe/yfELe/FDbGd0Mgxb4j/j3DYaM3/rrHarrGZPj5y1PPsk8J7A==', 'ENC:1:default-key:AES-256-GCM:JeTf8ypalDZc3sAY:mzt4Ixl3SmNISAFx2VwUxZTFn0xMc5u4Nfz00/LhGeUX7fTTeIEbQeoYWw==', true),
       ('65f023632bc46470c104b76f', 'PAYPAL', 'paypal-app-id', 'paypal-app-secret', 'paypal-webook-secret', true),
       ('65f023632bc46470c104b76f', 'COD', NULL, NULL, NULL, true),
       ('65f023632bc46470c104b76f', 'MANUAL_TRANSFER', NULL, NULL, NULL, true)
ON CONFLICT DO NOTHING;
