/*
can you generate another sql inserts similar to this
where languages=['en','fr'] and store_id='65f023632bc46470c104b75f' and manufacturer_id in (19,20,21,22,23,24) and product_type_id in (13,14,15,16)
start product id from 136
start product_description from 271
generate 15 products
make sure to replace all ids with real number in sequence
use html format for html_description generator so it contain valid html tags describe the product and its features and specification and more
domain for this store is electronics
*/
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (1, '2024-03-31 15:26:49.000000', '2024-03-31 15:26:49.000000', true, '2024-03-31 00:00:00.000000', false, null,
        false, null, true, false, null, null, $generator.ref_sku(), $generator.sku(), 1, $parameter.manufacturer_id, $parameter.store_id, $parameter.product_type_id)
on conflict do nothing;



-- <<Begin Loop on $parameter.languages l
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (1, '2024-03-31 15:26:49.000000', '2024-03-31 15:26:49.000000', $generator.html_description(),
           $generator.name(),$generator.title(), $generator.meta_description(), $generator.meta_keywords(), $generator.meta_title(), $generator.product_highlight(), $generator.sef_url(), $each.l, 1)
on conflict do nothing;
-- End Loop>>
