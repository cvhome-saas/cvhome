/*
Generated SQL inserts for catalog.product_relationship based on product data.
Store ID: '65f023632bc46470c104b75f'
Product range: 136 to 180
Relationship codes: 'HOME_PAGE', 'RECOMMENDED', 'NEWLY_ADDED', 'FEATURED_ITEMS'
Starting product_relationship_id: 182 -- Updated Start ID
Requirements:
- HOME_PAGE: 24 products
- RECOMMENDED: 8-16 products (Target: 12)
- NEWLY_ADDED: 8-16 products (Target: 10)
- FEATURED_ITEMS: 8-16 products (Target: 14)
- product_id is always null.
- related_product_id links to a product in the range.
*/

-- HOME_PAGE Relationship (24 products: 136-159)
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (182, true, 'HOME_PAGE', null, 136, '65f023632bc46470c104b75f') -- Start ID updated
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (183, true, 'HOME_PAGE', null, 137, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (184, true, 'HOME_PAGE', null, 138, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (185, true, 'HOME_PAGE', null, 139, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (186, true, 'HOME_PAGE', null, 140, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (187, true, 'HOME_PAGE', null, 141, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (188, true, 'HOME_PAGE', null, 142, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (189, true, 'HOME_PAGE', null, 143, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (190, true, 'HOME_PAGE', null, 144, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (191, true, 'HOME_PAGE', null, 145, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (192, true, 'HOME_PAGE', null, 146, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (193, true, 'HOME_PAGE', null, 147, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (194, true, 'HOME_PAGE', null, 148, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (195, true, 'HOME_PAGE', null, 149, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (196, true, 'HOME_PAGE', null, 150, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (197, true, 'HOME_PAGE', null, 151, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (198, true, 'HOME_PAGE', null, 152, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (199, true, 'HOME_PAGE', null, 153, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (200, true, 'HOME_PAGE', null, 154, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (201, true, 'HOME_PAGE', null, 155, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (202, true, 'HOME_PAGE', null, 156, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (203, true, 'HOME_PAGE', null, 157, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (204, true, 'HOME_PAGE', null, 158, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (205, true, 'HOME_PAGE', null, 159, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;

-- RECOMMENDED Relationship (12 products: 160-171)
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (206, true, 'RECOMMENDED', null, 160, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (207, true, 'RECOMMENDED', null, 161, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (208, true, 'RECOMMENDED', null, 162, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (209, true, 'RECOMMENDED', null, 163, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (210, true, 'RECOMMENDED', null, 164, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (211, true, 'RECOMMENDED', null, 165, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (212, true, 'RECOMMENDED', null, 166, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (213, true, 'RECOMMENDED', null, 167, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (214, true, 'RECOMMENDED', null, 168, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (215, true, 'RECOMMENDED', null, 169, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (216, true, 'RECOMMENDED', null, 170, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (217, true, 'RECOMMENDED', null, 171, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;

-- NEWLY_ADDED Relationship (10 products: 171-180)
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (218, true, 'NEWLY_ADDED', null, 171, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (219, true, 'NEWLY_ADDED', null, 172, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (220, true, 'NEWLY_ADDED', null, 173, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (221, true, 'NEWLY_ADDED', null, 174, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (222, true, 'NEWLY_ADDED', null, 175, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (223, true, 'NEWLY_ADDED', null, 176, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (224, true, 'NEWLY_ADDED', null, 177, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (225, true, 'NEWLY_ADDED', null, 178, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (226, true, 'NEWLY_ADDED', null, 179, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (227, true, 'NEWLY_ADDED', null, 180, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;

-- FEATURED_ITEMS Relationship (14 products: Mix)
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (228, true, 'FEATURED_ITEMS', null, 136, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (229, true, 'FEATURED_ITEMS', null, 137, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (230, true, 'FEATURED_ITEMS', null, 138, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (231, true, 'FEATURED_ITEMS', null, 140, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (232, true, 'FEATURED_ITEMS', null, 141, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (233, true, 'FEATURED_ITEMS', null, 144, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (234, true, 'FEATURED_ITEMS', null, 145, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (235, true, 'FEATURED_ITEMS', null, 150, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (236, true, 'FEATURED_ITEMS', null, 155, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (237, true, 'FEATURED_ITEMS', null, 156, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (238, true, 'FEATURED_ITEMS', null, 158, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (239, true, 'FEATURED_ITEMS', null, 168, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (240, true, 'FEATURED_ITEMS', null, 169, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (241, true, 'FEATURED_ITEMS', null, 172, '65f023632bc46470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;

-- Note: The original file had placeholder INSERT statements (IDs 13-16) and a template loop.
-- These have been replaced with specific inserts according to the requirements.
-- Total inserts generated: 24 (HOME_PAGE) + 12 (RECOMMENDED) + 10 (NEWLY_ADDED) + 14 (FEATURED_ITEMS) = 60 rows.
-- product_relationship_id range used: 182 to 241. -- Updated comment