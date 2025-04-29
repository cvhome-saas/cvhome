/*
generate more product_relationship  based on product file
start product_relationship_id=66  store_id=65f020632bc46470c104b76f
where languages=['fr','en']
code in ('HOME_PAGE', 'RECOMMENDED', 'NEWLY_ADDED', 'FEATURED_ITEMS')
every relationship should contain 8 product to 16 products at least make sure HOME_PAGE relationship contain 24
the joning column is related_product_id
the product range from 46 to 90
the product_id column always null don't fill it
*/

-- Define Relationship Groups (Using IDs 5, 6, 7, 8 as placeholders/types)
-- product_id and related_product_id are null for these definition rows.
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (66, true, 'HOME_PAGE', null, null, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (67, true, 'RECOMMENDED', null, null, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (68, true, 'NEWLY_ADDED', null, null, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (69, true, 'FEATURED_ITEMS', null, null, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;


-- product_id is NULL as requested
-- Renumbered product_relationship_id starting from 70

-- Link products to HOME_PAGE (24 Products: 46-69) - Start ID 70
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (70, true, 'HOME_PAGE', null, 46, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (71, true, 'HOME_PAGE', null, 47, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (72, true, 'HOME_PAGE', null, 48, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (73, true, 'HOME_PAGE', null, 49, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (74, true, 'HOME_PAGE', null, 50, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (75, true, 'HOME_PAGE', null, 51, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (76, true, 'HOME_PAGE', null, 52, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (77, true, 'HOME_PAGE', null, 53, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (78, true, 'HOME_PAGE', null, 54, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (79, true, 'HOME_PAGE', null, 55, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (80, true, 'HOME_PAGE', null, 56, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (81, true, 'HOME_PAGE', null, 57, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (82, true, 'HOME_PAGE', null, 58, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (83, true, 'HOME_PAGE', null, 59, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (84, true, 'HOME_PAGE', null, 60, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (85, true, 'HOME_PAGE', null, 61, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (86, true, 'HOME_PAGE', null, 62, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (87, true, 'HOME_PAGE', null, 63, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (88, true, 'HOME_PAGE', null, 64, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (89, true, 'HOME_PAGE', null, 65, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (90, true, 'HOME_PAGE', null, 66, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (91, true, 'HOME_PAGE', null, 67, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (92, true, 'HOME_PAGE', null, 68, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (93, true, 'HOME_PAGE', null, 69, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;

-- Link products to RECOMMENDED (12 Products: 70-81) - Start ID 94
-- product_id is NULL as requested
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (94, true, 'RECOMMENDED', null, 70, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (95, true, 'RECOMMENDED', null, 71, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (96, true, 'RECOMMENDED', null, 72, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (97, true, 'RECOMMENDED', null, 73, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (98, true, 'RECOMMENDED', null, 74, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (99, true, 'RECOMMENDED', null, 75, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (100, true, 'RECOMMENDED', null, 76, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (101, true, 'RECOMMENDED', null, 77, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (102, true, 'RECOMMENDED', null, 78, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (103, true, 'RECOMMENDED', null, 79, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (104, true, 'RECOMMENDED', null, 80, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (105, true, 'RECOMMENDED', null, 81, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;

-- Link products to NEWLY_ADDED (10 Products: 82-90, 46) - Start ID 106
-- product_id is NULL as requested
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (106, true, 'NEWLY_ADDED', null, 82, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (107, true, 'NEWLY_ADDED', null, 83, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (108, true, 'NEWLY_ADDED', null, 84, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (109, true, 'NEWLY_ADDED', null, 85, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (110, true, 'NEWLY_ADDED', null, 86, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (111, true, 'NEWLY_ADDED', null, 87, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (112, true, 'NEWLY_ADDED', null, 88, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (113, true, 'NEWLY_ADDED', null, 89, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (114, true, 'NEWLY_ADDED', null, 90, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (115, true, 'NEWLY_ADDED', null, 46, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
-- Wrap around/reuse

-- Link products to FEATURED_ITEMS (8 Products: 47-54) - Start ID 116
-- product_id is NULL as requested
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (116, true, 'FEATURED_ITEMS', null, 47, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (117, true, 'FEATURED_ITEMS', null, 48, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (118, true, 'FEATURED_ITEMS', null, 49, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (119, true, 'FEATURED_ITEMS', null, 50, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (120, true, 'FEATURED_ITEMS', null, 51, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (121, true, 'FEATURED_ITEMS', null, 52, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (122, true, 'FEATURED_ITEMS', null, 53, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (123, true, 'FEATURED_ITEMS', null, 54, '65f020632bc46470c104b76f')
on conflict (product_relationship_id) do nothing;

-- Removed the incorrect template line from the original file:
-- INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
--                                           store_merchant_id)
-- VALUES ($sequnce.nextval, true, $random.code, null, null, $random.product, $parameter.store_id)
-- on conflict do nothing;