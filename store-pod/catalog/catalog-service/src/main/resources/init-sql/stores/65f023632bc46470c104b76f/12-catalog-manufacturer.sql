/*
can you generate another sql inserts similar to this
where languages=['ar','en'] and store_id='65f023632bc46470c104b76f'
start manufacturer id from 1
start manufacturer_description from 1
generate 6 manufacturer
domain for this store is fashion
*/

-- Manufacturer 1: Nike
INSERT INTO catalog.manufacturer (manufacturer_id, date_created, date_modified, code, manufacturer_image, sort_order,
                                  store_merchant_id)
VALUES (1, NOW(), NOW(), 'NIKE', null, 0, '65f023632bc46470c104b76f')
on conflict do nothing;

-- <<Begin Loop on $parameter.languages l
-- English Description (ID: 1)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (1, NOW(), NOW(), 'Nike sportswear and apparel description in English.', 'Nike', null, 'en', 1)
on conflict do nothing;
-- Arabic Description (ID: 2)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (2, NOW(), NOW(), 'وصف ملابس نايكي الرياضية باللغة العربية.', 'نايكي', null, 'ar', 1) -- Using Arabic name
on conflict do nothing;
-- End Loop>>

-- Manufacturer 2: Zara
INSERT INTO catalog.manufacturer (manufacturer_id, date_created, date_modified, code, manufacturer_image, sort_order,
                                  store_merchant_id)
VALUES (2, NOW(), NOW(), 'ZARA', null, 1, '65f023632bc46470c104b76f')
on conflict do nothing;

-- <<Begin Loop on $parameter.languages l
-- English Description (ID: 3)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (3, NOW(), NOW(), 'Zara fast fashion clothing description in English.', 'Zara', null, 'en', 2)
on conflict do nothing;
-- Arabic Description (ID: 4)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (4, NOW(), NOW(), 'وصف ملابس زارا للأزياء السريعة باللغة العربية.', 'زارا', null, 'ar', 2) -- Using Arabic name
on conflict do nothing;
-- End Loop>>

-- Manufacturer 3: Adidas
INSERT INTO catalog.manufacturer (manufacturer_id, date_created, date_modified, code, manufacturer_image, sort_order,
                                  store_merchant_id)
VALUES (3, NOW(), NOW(), 'ADIDAS', null, 2, '65f023632bc46470c104b76f')
on conflict do nothing;

-- <<Begin Loop on $parameter.languages l
-- English Description (ID: 5)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (5, NOW(), NOW(), 'Adidas sportswear and shoes description in English.', 'Adidas', null, 'en', 3)
on conflict do nothing;
-- Arabic Description (ID: 6)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (6, NOW(), NOW(), 'وصف ملابس وأحذية أديداس الرياضية باللغة العربية.', 'أديداس', null, 'ar',
        3) -- Using Arabic name
on conflict do nothing;
-- End Loop>>

-- Manufacturer 4: H&M
INSERT INTO catalog.manufacturer (manufacturer_id, date_created, date_modified, code, manufacturer_image, sort_order,
                                  store_merchant_id)
VALUES (4, NOW(), NOW(), 'H&M', null, 3, '65f023632bc46470c104b76f')
on conflict do nothing;

-- <<Begin Loop on $parameter.languages l
-- English Description (ID: 7)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (7, NOW(), NOW(), 'H&M affordable fashion description in English.', 'H&M', null, 'en', 4)
on conflict do nothing;
-- Arabic Description (ID: 8)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (8, NOW(), NOW(), 'وصف أزياء إتش آند إم بأسعار معقولة باللغة العربية.', 'إتش آند إم', null, 'ar',
        4) -- Using Arabic name
on conflict do nothing;
-- End Loop>>

-- Manufacturer 5: Gucci
INSERT INTO catalog.manufacturer (manufacturer_id, date_created, date_modified, code, manufacturer_image, sort_order,
                                  store_merchant_id)
VALUES (5, NOW(), NOW(), 'GUCCI', null, 4, '65f023632bc46470c104b76f')
on conflict do nothing;

-- <<Begin Loop on $parameter.languages l
-- English Description (ID: 9)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (9, NOW(), NOW(), 'Gucci luxury fashion house description in English.', 'Gucci', null, 'en', 5)
on conflict do nothing;
-- Arabic Description (ID: 10)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (10, NOW(), NOW(), 'وصف دار أزياء غوتشي الفاخرة باللغة العربية.', 'غوتشي', null, 'ar', 5) -- Using Arabic name
on conflict do nothing;
-- End Loop>>

-- Manufacturer 6: Chanel
INSERT INTO catalog.manufacturer (manufacturer_id, date_created, date_modified, code, manufacturer_image, sort_order,
                                  store_merchant_id)
VALUES (6, NOW(), NOW(), 'CHANEL', null, 5, '65f023632bc46470c104b76f')
on conflict do nothing;

-- <<Begin Loop on $parameter.languages l
-- English Description (ID: 11)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (11, NOW(), NOW(), 'Chanel high fashion brand description in English.', 'Chanel', null, 'en', 6)
on conflict do nothing;
-- Arabic Description (ID: 12)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (12, NOW(), NOW(), 'وصف علامة شانيل للأزياء الراقية باللغة العربية.', 'شانيل', null, 'ar',
        6) -- Using Arabic name
on conflict do nothing;
-- End Loop>>