/*
Generated SQL inserts similar to the provided template
where languages=['fr','en'] and store_id='65f020632bc46470c104b76f' and country_id='FR'
domain for this store is beauté
*/

-- Insert into merchant.merchant_store`
INSERT INTO merchant.merchant_store (store_merchant_id, currency_format_national, in_business_since, org, theme,
                                     color_theme, seizeunitcode, store_email, store_logo, store_banner, store_address,
                                     store_city, store_name, store_phone, store_postal_code, store_state_prov,
                                     use_cache, weightunitcode, country_id, currency_id, language_code)
VALUES ('65f020632bc46470c104b76f', false, '2024-03-31', '352023632b046970c104b76f', 'DEFAULT', 'LIGHT',
        'CM', -- Changed seizeunitcode to CM
        'contact@beaute-elegante.fr', 'logo.jpeg', 'banner.jpeg', '15 Rue de la Paix', 'Paris', 'Beauté Élégante Paris',
        '+33 1 23 45 67 89',
        '75002', 'Île-de-France', false, 'KG', 'FR', 'EUR',
        'fr') -- Changed weightunitcode to KG, set currency to EUR, first language to fr
on conflict (store_merchant_id) do nothing;

-- Insert into merchant.merchant_language for 'fr' and 'en'
INSERT INTO merchant.merchant_language (store_merchant_id, language_code)
VALUES ('65f020632bc46470c104b76f', 'fr')
on conflict (store_merchant_id, language_code) do nothing;

INSERT INTO merchant.merchant_language (store_merchant_id, language_code)
VALUES ('65f020632bc46470c104b76f', 'en')
on conflict (store_merchant_id, language_code) do nothing;

INSERT INTO merchant.merchant_slider_images (store_merchant_id, priority, url)
VALUES ('65f020632bc46470c104b76f', 0, 'slide-1.jpeg');
INSERT INTO merchant.merchant_slider_images (store_merchant_id, priority, url)
VALUES ('65f020632bc46470c104b76f', 1, 'slide-2.jpeg');
INSERT INTO merchant.merchant_slider_images (store_merchant_id, priority, url)
VALUES ('65f020632bc46470c104b76f', 2, 'slide-3.jpeg');
INSERT INTO merchant.merchant_slider_images (store_merchant_id, priority, url)
VALUES ('65f020632bc46470c104b76f', 3, 'slide-4.jpeg');
INSERT INTO merchant.merchant_slider_images (store_merchant_id, priority, url)
VALUES ('65f020632bc46470c104b76f', 4, 'slide-5.jpeg');


-- Insert into merchant.social_links for 'FACEBOOK', 'X', 'INSTAGRAM', 'TIKTOK'
INSERT INTO merchant.social_links (store_merchant_id, provider, url)
VALUES ('65f020632bc46470c104b76f', 'FACEBOOK', 'https://www.facebook.com/beauteeleganteparis');

INSERT INTO merchant.social_links (store_merchant_id, provider, url)
VALUES ('65f020632bc46470c104b76f', 'X', 'https://www.twitter.com/beauteelegante');

INSERT INTO merchant.social_links (store_merchant_id, provider, url)
VALUES ('65f020632bc46470c104b76f', 'INSTAGRAM', 'https://www.instagram.com/beaute.elegante.paris');

INSERT INTO merchant.social_links (store_merchant_id, provider, url)
VALUES ('65f020632bc46470c104b76f', 'TIKTOK', 'https://www.tiktok.com/@beauteelegante');