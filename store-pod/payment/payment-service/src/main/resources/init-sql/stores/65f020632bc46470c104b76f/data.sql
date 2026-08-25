INSERT INTO payment.payment_configuration (store_merchant_id, payment_type, api_key, secret_key, webhook_secret,
                                           enabled)
VALUES ('65f020632bc46470c104b76f', 'STRIPE', null, 'ENC:1:default-key:AES-256-GCM:Su2GFSFPt+ml3yyd:/tLGn7hG4FCbdL1WgC988FvD7/Xvcfz1eKtAsvDoY7yQCrHrH5nQHh2I6z2caYti5Fw6Av2MvnIVhZyUfb307y5rju0U0hyW0YlD+9nuvzNGWG1mMjPd3yH5HiKyKWNuPeavceDMFglNX3BaxcE8Dr1UHolG95eG5XRA', 'ENC:1:default-key:AES-256-GCM:JeTf8ypalDZc3sAY:mzt4Ixl3SmNISAFx2VwUxZTFn0xMc5u4Nfz00/LhGeUX7fTTeIEbQeoYWw==', true),
       ('65f020632bc46470c104b76f', 'PAYPAL', 'paypal-app-id', 'paypal-app-secret', 'paypal-webook-secret', true),
       ('65f020632bc46470c104b76f', 'COD', NULL, NULL, NULL, true),
       ('65f020632bc46470c104b76f', 'MANUAL_TRANSFER', NULL, NULL, NULL, true)
ON CONFLICT DO NOTHING;
