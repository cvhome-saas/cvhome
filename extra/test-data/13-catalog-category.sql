/*
can you generate another sql inserts similar to this
where languages=['en','fr'] and store_id='65f023632bc46470c104b75f'
start category id from 37
start category_description from 73
generate 12 categories
domain for this store is electronics
*/
INSERT INTO catalog.category (category_id, date_created, date_modified,  category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (1, '2024-03-31 15:24:30.000000', '2024-03-31 15:24:31.000000',  null, false, $generator.category_code(), 0,
        false, '/1/', 0, true, $parameter.store_id, null)
on conflict do nothing;

-- <<Begin Loop on $parameter.languages l
INSERT INTO catalog.category_description (description_id, date_created, date_modified,  description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (1, '2024-03-31 15:24:30.000000', '2024-03-31 15:24:30.000000',  $generator.description(), $generator.name(), $generator.title(),$generator.category_highlight(), $generator.meta_description(), $generator.meta_keywords(),$generator.meta_title(),
           $generator.meta_title(), $generator.sef_url(), $each.l, 1)
on conflict do nothing;
-- End Loop>>