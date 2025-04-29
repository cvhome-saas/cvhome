/*
Generated SQL inserts for product relationships based on product file.
- Starts product_relationship_id from 5.
- Uses store_id = '65f023632bc46470c104b76f'.
- Uses codes: 'HOME_PAGE', 'RECOMMENDED', 'NEWLY_ADDED', 'FEATURED_ITEMS'.
- 'HOME_PAGE' contains 24 products.
- Other relationships contain between 8 and 16 products.
- Uses related_product_id as the joining column to link products to the relationship type.
- Assumes products with IDs 1 through 45 exist.
*/

-- Base Relationship Type Definitions (IDs 1-4)
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (1, true, 'HOME_PAGE', null, null, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (2, true, 'RECOMMENDED', null, null, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (3, true, 'NEWLY_ADDED', null, null, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (4, true, 'FEATURED_ITEMS', null, null, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;


-- Assigning Products to 'HOME_PAGE' (24 products, IDs 5-28)
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (5, true, 'HOME_PAGE', null, 1, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (6, true, 'HOME_PAGE', null, 2, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (7, true, 'HOME_PAGE', null, 3, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (8, true, 'HOME_PAGE', null, 4, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (9, true, 'HOME_PAGE', null, 5, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (10, true, 'HOME_PAGE', null, 6, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (11, true, 'HOME_PAGE', null, 7, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (12, true, 'HOME_PAGE', null, 8, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (13, true, 'HOME_PAGE', null, 9, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (14, true, 'HOME_PAGE', null, 10, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (15, true, 'HOME_PAGE', null, 11, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (16, true, 'HOME_PAGE', null, 12, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (17, true, 'HOME_PAGE', null, 13, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (18, true, 'HOME_PAGE', null, 14, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (19, true, 'HOME_PAGE', null, 15, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (20, true, 'HOME_PAGE', null, 16, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (21, true, 'HOME_PAGE', null, 17, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (22, true, 'HOME_PAGE', null, 18, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (23, true, 'HOME_PAGE', null, 19, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (24, true, 'HOME_PAGE', null, 20, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (25, true, 'HOME_PAGE', null, 21, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (26, true, 'HOME_PAGE', null, 22, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (27, true, 'HOME_PAGE', null, 23, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (28, true, 'HOME_PAGE', null, 24, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;

-- Assigning Products to 'RECOMMENDED' (12 products, IDs 29-40)
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (29, true, 'RECOMMENDED', null, 16, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (30, true, 'RECOMMENDED', null, 17, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (31, true, 'RECOMMENDED', null, 18, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (32, true, 'RECOMMENDED', null, 19, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (33, true, 'RECOMMENDED', null, 20, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (34, true, 'RECOMMENDED', null, 21, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (35, true, 'RECOMMENDED', null, 22, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (36, true, 'RECOMMENDED', null, 23, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (37, true, 'RECOMMENDED', null, 24, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (38, true, 'RECOMMENDED', null, 25, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (39, true, 'RECOMMENDED', null, 26, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (40, true, 'RECOMMENDED', null, 27, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;

-- Assigning Products to 'NEWLY_ADDED' (15 products, IDs 41-55)
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (41, true, 'NEWLY_ADDED', null, 31, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (42, true, 'NEWLY_ADDED', null, 32, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (43, true, 'NEWLY_ADDED', null, 33, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (44, true, 'NEWLY_ADDED', null, 34, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (45, true, 'NEWLY_ADDED', null, 35, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (46, true, 'NEWLY_ADDED', null, 36, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (47, true, 'NEWLY_ADDED', null, 37, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (48, true, 'NEWLY_ADDED', null, 38, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (49, true, 'NEWLY_ADDED', null, 39, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (50, true, 'NEWLY_ADDED', null, 40, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (51, true, 'NEWLY_ADDED', null, 41, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (52, true, 'NEWLY_ADDED', null, 42, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (53, true, 'NEWLY_ADDED', null, 43, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (54, true, 'NEWLY_ADDED', null, 44, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (55, true, 'NEWLY_ADDED', null, 45, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;

-- Assigning Products to 'FEATURED_ITEMS' (10 products, IDs 56-65) - Focusing on luxury items
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (56, true, 'FEATURED_ITEMS', null, 5, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing; -- Gucci Bag
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (57, true, 'FEATURED_ITEMS', null, 6, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing; -- Chanel Sunglasses
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (58, true, 'FEATURED_ITEMS', null, 11, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing; -- Gucci Loafers
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (59, true, 'FEATURED_ITEMS', null, 12, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing; -- Chanel Card Holder
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (60, true, 'FEATURED_ITEMS', null, 17, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing; -- Gucci Wallet
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (61, true, 'FEATURED_ITEMS', null, 18, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing; -- Chanel Brooch
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (62, true, 'FEATURED_ITEMS', null, 23, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing; -- Gucci Sneakers
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (63, true, 'FEATURED_ITEMS', null, 24, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing; -- Chanel Flats
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (64, true, 'FEATURED_ITEMS', null, 29, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing; -- Gucci Belt Bag
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (65, true, 'FEATURED_ITEMS', null, 30, '65f023632bc46470c104b76f')
on conflict (product_relationship_id) do nothing; -- Chanel Sneakers