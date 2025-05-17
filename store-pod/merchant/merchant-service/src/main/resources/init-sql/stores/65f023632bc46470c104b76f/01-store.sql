/*
can you generate another sql inserts similar to this
where languages=['ar','en'] and store_id='65f023632bc46470c104b76f' and country_id='SA'
domain for this store is fashion
*/
INSERT INTO merchant.merchant_store (store_merchant_id, currency_format_national, in_business_since, org, theme,
                                     color_theme, seizeunitcode, store_email, store_logo, store_banner, store_address,
                                     store_city, store_name, store_phone, store_postal_code, store_state_prov,
                                     use_cache, weightunitcode, country_id, currency_id, language_code)
VALUES ('65f023632bc46470c104b76f', false, '2024-03-31', '21f023932bc66470c104b76f', 'DEFAULT', 'LIGHT',
        'CM', -- seizeunitcode for fashion
        'info@riyadhfashion.sa', 'logo.jpeg', 'banner.jpeg', '123 Olaya Street', 'Riyadh', 'Riyadh-Fashion-Hub',
        '+966 50 123 4567',
        '11564', 'Riyadh Province', false, 'KG', 'SA', 'SAR', 'ar')
on conflict (store_merchant_id) do nothing;


-- <<Begin Loop on $parameter.languages l
INSERT INTO merchant.merchant_language (store_merchant_id, language_code)
VALUES ('65f023632bc46470c104b76f', 'ar')
on conflict (store_merchant_id, language_code) do nothing;
INSERT INTO merchant.merchant_language (store_merchant_id, language_code)
VALUES ('65f023632bc46470c104b76f', 'en')
on conflict (store_merchant_id, language_code) do nothing;
-- End Loop>>

-- <<Begin Loop on l in ('0','1','2','3','4')
INSERT INTO merchant.merchant_slider_images (store_merchant_id, priority, name)
VALUES ('65f023632bc46470c104b76f', 0, 'slide-1.jpeg');
INSERT INTO merchant.merchant_slider_images (store_merchant_id, priority, name)
VALUES ('65f023632bc46470c104b76f', 1, 'slide-2.jpeg');
INSERT INTO merchant.merchant_slider_images (store_merchant_id, priority, name)
VALUES ('65f023632bc46470c104b76f', 2, 'slide-3.jpeg');
INSERT INTO merchant.merchant_slider_images (store_merchant_id, priority, name)
VALUES ('65f023632bc46470c104b76f', 3, 'slide-4.jpeg');
INSERT INTO merchant.merchant_slider_images (store_merchant_id, priority, name)
VALUES ('65f023632bc46470c104b76f', 4, 'slide-5.jpeg');
-- End Loop>>


-- <<Begin Loop on l in ('FACEBOOK','X','INSTAGRAM','TIKTOK')
INSERT INTO merchant.social_links (store_merchant_id, provider, url)
VALUES ('65f023632bc46470c104b76f', 'FACEBOOK', 'https://facebook.com/riyadhfashionhub');
INSERT INTO merchant.social_links (store_merchant_id, provider, url)
VALUES ('65f023632bc46470c104b76f', 'X', 'https://x.com/riyadhfashionhub');
INSERT INTO merchant.social_links (store_merchant_id, provider, url)
VALUES ('65f023632bc46470c104b76f', 'INSTAGRAM', 'https://instagram.com/riyadhfashionhub');
INSERT INTO merchant.social_links (store_merchant_id, provider, url)
VALUES ('65f023632bc46470c104b76f', 'TIKTOK', 'https://tiktok.com/@riyadhfashionhub');
-- End Loop>>