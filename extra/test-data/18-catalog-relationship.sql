/*
generate more product_relationship  based on product file
start product_relationship_id=174  store_id=65f023632bc46470c104b75f
where languages=['en','fr']
code in ('HOME_PAGE', 'RECOMMENDED', 'NEWLY_ADDED', 'FEATURED_ITEMS')
every relationship should contain 8 product to 16 products at least make sure HOME_PAGE relationship contain 24
the joning column is related_product_id
the product range from 136 to 180
the product_id column always null don't fill it
*/

INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (13, true, 'HOME_PAGE', null, null, $parameter.store_id)
on conflict do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (14, true, 'RECOMMENDED', null, null, $parameter.store_id)
on conflict do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (15, true, 'NEWLY_ADDED', null, null, $parameter.store_id)
on conflict do nothing;
INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES (16, true, 'FEATURED_ITEMS', null, null, $parameter.store_id)
on conflict do nothing;


INSERT INTO catalog.product_relationship (product_relationship_id, active, code, product_id, related_product_id,
                                          store_merchant_id)
VALUES ($sequnce.nextval, true, $random.code, null, null, $random.product, $parameter.store_id)
on conflict do nothing;
