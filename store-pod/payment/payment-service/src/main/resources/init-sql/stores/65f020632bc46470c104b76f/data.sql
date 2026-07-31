INSERT INTO payment.payment_configuration (store_merchant_id, payment_type, api_key, secret_key, webhook_secret,
                                           enabled)
VALUES ('65f020632bc46470c104b76f', 'STRIPE', null, 'ENC:1:default-key:AES-256-GCM:y0tyiM+WCflwmsfN:SVvSZ4Jxbo52x2FDvvpUKThcY78qc8Ar6WyiWIz0tPgEpbbpgd1LDQyO33lZUdRb1kwuxDlXTKQl0+lLZsjBf8AcbGj4mDqRj6MDKXKCoFTJlNBt6IegzImhaoKCHoeBpsWbeciIcQ6MDl0mF93yQf8wCp5IbrCuu9JMyqdZ8Q==', 'ENC:1:default-key:AES-256-GCM:wedZfkeJH8ccRPcF:/Dn4KeBUrfQv/ZFSaVO5hHE6nmRfIPy+r+F9hi2nmAeWVSxXmpEbI247aapP7WbzHHLUjn+4UM5wI62pFuO1/vNuG3ryXvVOTsrZA05l4F1QZ8ap1Rc=', true),
       ('65f020632bc46470c104b76f', 'PAYPAL', 'paypal-app-id', 'paypal-app-secret', 'paypal-webook-secret', true),
       ('65f020632bc46470c104b76f', 'COD', NULL, NULL, NULL, true),
       ('65f020632bc46470c104b76f', 'MANUAL_TRANSFER', NULL, NULL, NULL, true);
