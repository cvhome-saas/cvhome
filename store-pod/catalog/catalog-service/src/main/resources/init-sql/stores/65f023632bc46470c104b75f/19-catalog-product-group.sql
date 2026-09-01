INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created,
                                   date_modified)
VALUES (6, true, 'RECOMMENDED', '65f023632bc46470c104b75f', null, NOW(), NOW())
on conflict do nothing;INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created,
                                   date_modified)
VALUES (7, true, 'NEWLY_ADDED', '65f023632bc46470c104b75f', null, NOW(), NOW())
on conflict do nothing;INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created,
                                   date_modified)
VALUES (8, true, 'FEATURED_ITEMS', '65f023632bc46470c104b75f', null, NOW(), NOW())
on conflict do nothing;
-- RECOMMENDED
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created,
                                               date_modified)
VALUES (11, 'Recommended', 'en', 6, NOW(), NOW())
on conflict do nothing;INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created,
                                               date_modified)
VALUES (12, 'مقترح لك', 'ar', 6, NOW(), NOW())
on conflict do nothing;
-- NEWLY_ADDED
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created,
                                               date_modified)
VALUES (13, 'Newly Added', 'en', 7, NOW(), NOW())
on conflict do nothing;INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created,
                                               date_modified)
VALUES (14, 'أضيف حديثا', 'ar', 7, NOW(), NOW())
on conflict do nothing;
-- FEATURED_ITEMS
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created,
                                               date_modified)
VALUES (15, 'Featured Items', 'en', 8, NOW(), NOW())
on conflict do nothing;INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created,
                                               date_modified)
VALUES (16, 'منتجات مميزة', 'ar', 8, NOW(), NOW())
on conflict do nothing;INSERT INTO catalog.product_group_product (product_group_id, product_id)
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
ON CONFLICT DO NOTHING;INSERT INTO catalog.product_group_product (product_group_id, product_id)
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
ON CONFLICT DO NOTHING;INSERT INTO catalog.product_group_product (product_group_id, product_id)
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