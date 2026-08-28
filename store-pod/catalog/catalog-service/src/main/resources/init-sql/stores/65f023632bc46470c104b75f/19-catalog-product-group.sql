/*
Generated SQL inserts for product groups based on product relationship file.
- Store ID: '65f023632bc46470c104b75f'
- Group Codes: 'HOME_PAGE', 'RECOMMENDED', 'NEWLY_ADDED', 'FEATURED_ITEMS'
*/

-- Product Groups
INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created,
                                   date_modified)
VALUES (5, true, 'HOME_PAGE', '65f023632bc46470c104b75f', null, NOW(), NOW())
on conflict do nothing;
INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created,
                                   date_modified)
VALUES (6, true, 'RECOMMENDED', '65f023632bc46470c104b75f', null, NOW(), NOW())
on conflict do nothing;
INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created,
                                   date_modified)
VALUES (7, true, 'NEWLY_ADDED', '65f023632bc46470c104b75f', null, NOW(), NOW())
on conflict do nothing;
INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created,
                                   date_modified)
VALUES (8, true, 'FEATURED_ITEMS', '65f023632bc46470c104b75f', null, NOW(), NOW())
on conflict do nothing;

-- Group Descriptions (English & Arabic)
-- HOME_PAGE
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created,
                                               date_modified)
VALUES (9, 'Home Page', 'en', 5, NOW(), NOW())
on conflict do nothing;
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created,
                                               date_modified)
VALUES (10, 'الصفحة الرئيسية', 'ar', 5, NOW(), NOW())
on conflict do nothing;

-- RECOMMENDED
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created,
                                               date_modified)
VALUES (11, 'Recommended', 'en', 6, NOW(), NOW())
on conflict do nothing;
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created,
                                               date_modified)
VALUES (12, 'مقترح لك', 'ar', 6, NOW(), NOW())
on conflict do nothing;

-- NEWLY_ADDED
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created,
                                               date_modified)
VALUES (13, 'Newly Added', 'en', 7, NOW(), NOW())
on conflict do nothing;
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created,
                                               date_modified)
VALUES (14, 'أضيف حديثا', 'ar', 7, NOW(), NOW())
on conflict do nothing;

-- FEATURED_ITEMS
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created,
                                               date_modified)
VALUES (15, 'Featured Items', 'en', 8, NOW(), NOW())
on conflict do nothing;
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created,
                                               date_modified)
VALUES (16, 'منتجات مميزة', 'ar', 8, NOW(), NOW())
on conflict do nothing;

-- Product Group Memberships (Assuming Product IDs match relationship file)
-- IDs based on relationship file range (simplified to match IDs 1-45 logic)

INSERT INTO catalog.product_group_product (product_group_id, product_id)
VALUES (5, 136),
       (5, 137),
       (5, 138),
       (5, 139),
       (5, 140),
       (5, 141),
       (5, 142),
       (5, 143),
       (5, 144),
       (5, 145),
       (5, 146),
       (5, 147),
       (5, 148),
       (5, 149),
       (5, 150),
       (5, 151),
       (5, 152),
       (5, 153),
       (5, 154),
       (5, 155),
       (5, 156),
       (5, 157),
       (5, 158),
       (5, 159)
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_group_product (product_group_id, product_id)
VALUES (6, 160),
       (6, 161),
       (6, 162),
       (6, 163),
       (6, 164),
       (6, 165),
       (6, 166),
       (6, 167),
       (6, 168),
       (6, 169),
       (6, 170),
       (6, 171)
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_group_product (product_group_id, product_id)
VALUES (7, 171),
       (7, 172),
       (7, 173),
       (7, 174),
       (7, 175),
       (7, 176),
       (7, 177),
       (7, 178),
       (7, 179),
       (7, 180)
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_group_product (product_group_id, product_id)
VALUES (8, 136),
       (8, 137),
       (8, 138),
       (8, 140),
       (8, 141),
       (8, 144),
       (8, 145),
       (8, 150),
       (8, 155),
       (8, 156),
       (8, 158),
       (8, 168),
       (8, 169),
       (8, 172)
ON CONFLICT DO NOTHING;
-- French group names. The seed above carries English and Arabic only, so on the three storefronts that sell in
-- French every product rail dropped out of the home page: the storefront skips a group whose description does
-- not resolve in the requested locale. Ids are negative — description ids come from a sequence that only grows.
-- HOME_PAGE
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id,
                                               date_created, date_modified)
VALUES (-5, 'Page d''accueil', 'fr', 5, NOW(), NOW())
on conflict do nothing;
-- RECOMMENDED
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id,
                                               date_created, date_modified)
VALUES (-6, 'Recommandé pour vous', 'fr', 6, NOW(), NOW())
on conflict do nothing;
-- NEWLY_ADDED
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id,
                                               date_created, date_modified)
VALUES (-7, 'Nouveautés', 'fr', 7, NOW(), NOW())
on conflict do nothing;
-- FEATURED_ITEMS
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id,
                                               date_created, date_modified)
VALUES (-8, 'Notre sélection', 'fr', 8, NOW(), NOW())
on conflict do nothing;
