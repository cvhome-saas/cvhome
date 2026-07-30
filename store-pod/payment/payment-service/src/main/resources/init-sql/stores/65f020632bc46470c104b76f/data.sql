INSERT INTO payment.payment_configuration (store_merchant_id, payment_type, api_key, secret_key, webhook_secret,
                                           enabled)
VALUES ('65f020632bc46470c104b76f', 'STRIPE', 'stripe-app-id', 'stripe-app-secret', 'stripe-webook-secret', true),
       ('65f020632bc46470c104b76f', 'PAYPAL', 'paypal-app-id', 'paypal-app-secret', 'paypal-webook-secret', true),
       ('65f020632bc46470c104b76f', 'COD', NULL, NULL, NULL, true),
       ('65f020632bc46470c104b76f', 'MANUAL_TRANSFER', NULL, NULL, NULL, true);
