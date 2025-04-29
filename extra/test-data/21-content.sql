/*
Generated content for store_id='65f023632bc46470c104b75f' (electronics Domain) store name USA Electronics Hub
Pages: ['about-us', 'contact-us', 'terms', 'privacy', 'location', 'faq']
Boxes: ['header-message', 'agreement']
Languages: ['en', 'fr']
Starting content_id: 25
Starting description_id: 49
Starting sort_order: 1
*/

-- <<Begin Loop on $parameter.pages p
INSERT INTO content.content (content_id, code, content_type,
                             link_to_menu, sort_order, visible, store_merchant_id)
VALUES (1, $ each.p, 'PAGE', true, $sequnece.next(), true, $parameter.store_id)
on conflict do nothing;


-- <<Begin Loop on $parameter.languages l
INSERT INTO content.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (9, '2024-03-31 15:28:01.000000', '2024-03-31 15:28:01.000000', $generator.html_description(), $generator.name(),
           $generator.title(), $generator.meta_description(), $generator.meta_keywords(), $generator.meta_title(),
        $generator.sef_url(), 1, $each.l)
on conflict do nothing;
-- End Loop>>
-- End Loop>>

-- <<Begin Loop on $parameter.boxes b
INSERT INTO content.content (content_id, code, content_type,
                             link_to_menu, sort_order, visible, store_merchant_id)
VALUES (1, $ each.b, 'BOX', false, $sequnece.next(), true, $parameter.store_id)
on conflict do nothing;


-- <<Begin Loop on $parameter.languages l
INSERT INTO content.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (9, '2024-03-31 15:28:01.000000', '2024-03-31 15:28:01.000000', $generator.html_description(), $generator.name(),
           $generator.title(), $generator.meta_description(), $generator.meta_keywords(), $generator.meta_title(),
        $generator.sef_url(), 1, $each.l)
on conflict do nothing;
-- End Loop>>
-- End Loop>>
