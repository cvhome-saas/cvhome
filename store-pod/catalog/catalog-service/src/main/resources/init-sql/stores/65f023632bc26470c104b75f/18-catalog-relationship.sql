/*
generate more product_relationship  based on product file
start product_relationship_id=120  store_id=65f023632bc26470c104b75f
where languages=['ar','fr']
code in ('HOME_PAGE', 'RECOMMENDED', 'NEWLY_ADDED', 'FEATURED_ITEMS')
every relationship should contain 8 product to 16 products at least make sure HOME_PAGE relationship contain 24
the joning column is related_product_id
the product range from 91 to 135
the product_id column always null don't fill it
*/
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (124, true, 'HOME_PAGE', null, null, '65f023632bc26470c104b75f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (125, true, 'RECOMMENDED', null, null, '65f023632bc26470c104b75f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (126, true, 'NEWLY_ADDED', null, null, '65f023632bc26470c104b75f')
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (127, true, 'FEATURED_ITEMS', null, null, '65f023632bc26470c104b75f')
on conflict (product_relationship_id) do nothing;

-- HOME_PAGE Relationship (24 Products: 91-114)
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (128, true, 'HOME_PAGE', null, 91, '65f023632bc26470c104b75f') -- Start ID updated to 128
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (129, true, 'HOME_PAGE', null, 92, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (130, true, 'HOME_PAGE', null, 93, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (131, true, 'HOME_PAGE', null, 94, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (132, true, 'HOME_PAGE', null, 95, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (133, true, 'HOME_PAGE', null, 96, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (134, true, 'HOME_PAGE', null, 97, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (135, true, 'HOME_PAGE', null, 98, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (136, true, 'HOME_PAGE', null, 99, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (137, true, 'HOME_PAGE', null, 100, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (138, true, 'HOME_PAGE', null, 101, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (139, true, 'HOME_PAGE', null, 102, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (140, true, 'HOME_PAGE', null, 103, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (141, true, 'HOME_PAGE', null, 104, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (142, true, 'HOME_PAGE', null, 105, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (143, true, 'HOME_PAGE', null, 106, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (144, true, 'HOME_PAGE', null, 107, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (145, true, 'HOME_PAGE', null, 108, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (146, true, 'HOME_PAGE', null, 109, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (147, true, 'HOME_PAGE', null, 110, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (148, true, 'HOME_PAGE', null, 111, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (149, true, 'HOME_PAGE', null, 112, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (150, true, 'HOME_PAGE', null, 113, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (151, true, 'HOME_PAGE', null, 114, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;

-- RECOMMENDED Relationship (10 Products: 115, 117, 119, 121, 123, 125, 127, 129, 131, 133)
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (152, true, 'RECOMMENDED', null, 115, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (153, true, 'RECOMMENDED', null, 117, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (154, true, 'RECOMMENDED', null, 119, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (155, true, 'RECOMMENDED', null, 121, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (156, true, 'RECOMMENDED', null, 123, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (157, true, 'RECOMMENDED', null, 125, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (158, true, 'RECOMMENDED', null, 127, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (159, true, 'RECOMMENDED', null, 129, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (160, true, 'RECOMMENDED', null, 131, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (161, true, 'RECOMMENDED', null, 133, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;

-- NEWLY_ADDED Relationship (12 Products: 135, 134, 132, 130, 128, 126, 124, 122, 120, 118, 116, 114)
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (162, true, 'NEWLY_ADDED', null, 135, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (163, true, 'NEWLY_ADDED', null, 134, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (164, true, 'NEWLY_ADDED', null, 132, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (165, true, 'NEWLY_ADDED', null, 130, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (166, true, 'NEWLY_ADDED', null, 128, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (167, true, 'NEWLY_ADDED', null, 126, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (168, true, 'NEWLY_ADDED', null, 124, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (169, true, 'NEWLY_ADDED', null, 122, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (170, true, 'NEWLY_ADDED', null, 120, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (171, true, 'NEWLY_ADDED', null, 118, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (172, true, 'NEWLY_ADDED', null, 116, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (173, true, 'NEWLY_ADDED', null, 114, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;

-- FEATURED_ITEMS Relationship (8 Products: 91, 95, 99, 103, 107, 111, 115, 119)
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (174, true, 'FEATURED_ITEMS', null, 91, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (175, true, 'FEATURED_ITEMS', null, 95, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (176, true, 'FEATURED_ITEMS', null, 99, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (177, true, 'FEATURED_ITEMS', null, 103, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (178, true, 'FEATURED_ITEMS', null, 107, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (179, true, 'FEATURED_ITEMS', null, 111, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (180, true, 'FEATURED_ITEMS', null, 115, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (181, true, 'FEATURED_ITEMS', null, 119, '65f023632bc26470c104b75f') -- Incremented
on conflict (product_relationship_id) do nothing;