/*
can you generate another sql inserts similar to this
where languages=['en','fr'] and store_id='65f023632bc46470c104b75f'
start manufacturer id from 19
start manufacturer_description from 37
generate 6 manufacturer
domain for this store is electronics
*/
INSERT INTO catalog.manufacturer (manufacturer_id, date_created, date_modified, code, manufacturer_image, sort_order,
                                  store_merchant_id)
VALUES (1, '2024-03-31 08:45:39.000000', '2024-03-31 08:45:39.000000', $generator.manufacturer_code(), null, 0, $paramter.store_id)
on conflict do nothing;


-- <<Begin Loop on $parameter.languages l
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (1, '2024-03-31 08:45:39.000000', '2024-03-31 08:45:39.000000', $generator.manufacturer_description(l), $generator.manufacturer_code(l), null, $each.l, 1)
on conflict do nothing;
-- End Loop>>
