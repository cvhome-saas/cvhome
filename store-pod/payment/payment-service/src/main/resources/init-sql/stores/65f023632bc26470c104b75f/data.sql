SELECT 1;
INSERT INTO payment.payment_configuration (store_merchant_id, payment_type, api_key, secret_key, webhook_secret,
                                           enabled)
VALUES ('65f023632bc26470c104b75f', 'STRIPE', null, 'ENC:1:default-key:AES-256-GCM:Su2GFSFPt+ml3yyd:/tLGn7hG4FCbdL1WgC988FvD7/Xvcfz1eKtAsvDoY7yQCrHrH5nQHh2I6z2caYti5Fw6Av2MvnIVhZyUfb307y5rju0U0hyW0YlD+9nuvzNGWG1mMjPd3yH5HiKyKWNuPeavceDMFglNX3BaxcE8Dr1UHolG95eG5XRA', 'ENC:1:default-key:AES-256-GCM:wedZfkeJH8ccRPcF:/Dn4KeBUrfQv/ZFSaVO5hHE6nmRfIPy+r+F9hi2nmAeWVSxXmpEbI247aapP7WbzHHLUjn+4UM5wI62pFuO1/vNuG3ryXvVOTsrZA05l4F1QZ8ap1Rc=', true),
       ('65f023632bc26470c104b75f', 'PAYPAL', 'paypal-app-id', 'paypal-app-secret', 'paypal-webook-secret', true),
       ('65f023632bc26470c104b75f', 'COD', NULL, NULL, NULL, true),
       ('65f023632bc26470c104b75f', 'MANUAL_TRANSFER', NULL, NULL, NULL, true)
ON CONFLICT DO NOTHING;
