INSERT INTO payment.payment_configuration (store_merchant_id, payment_type, api_key, secret_key, webhook_secret,
                                           enabled)
VALUES ('65f023632bc46470c104b75f', 'STRIPE', 'stripe-app-id', 'stripe-app-secret', 'stripe-webook-secret', true),
       ('65f023632bc46470c104b75f', 'PAYPAL', 'paypal-app-id', 'paypal-app-secret', 'paypal-webook-secret', true),
       ('65f023632bc46470c104b75f', 'COD', NULL, NULL, NULL, true),
       ('65f023632bc46470c104b75f', 'MANUAL_TRANSFER', NULL, NULL, NULL, true);
