INSERT INTO store.category (id, store_id, name, image_link, parent_id, sequence, level)
VALUES ('65f5b471c919a450ab3f8649', '65f023632bc46470c104b76f', 'string', 'https://google.com', null, 0,
        0)
on conflict do nothing;

INSERT INTO store.category (id, store_id, name, image_link, parent_id, sequence, level)
VALUES ('65f5b471c919a450ab3f8641', '65f023632bc46470c104b76f', 'string', 'https://google.com', null, 0,
        0)
on conflict do nothing;

INSERT INTO store.category (id, store_id, name, image_link, parent_id, sequence, level)
VALUES ('65f5b471c919a450ab3f8642', '65f023632bc46470c104b76f', 'string', 'https://google.com',
        '65f5b471c919a450ab3f8649', 0, 0)
on conflict do nothing;

INSERT INTO store.category (id, store_id, name, image_link, parent_id, sequence, level)
VALUES ('65f5b471c919a450ab3f8643', '65f023632bc46470c104b76f', 'string', 'https://google.com',
        '65f5b471c919a450ab3f8649', 0, 0)
on conflict do nothing;

INSERT INTO store.category (id, store_id, name, image_link, parent_id, sequence, level)
VALUES ('65f5b471c919a450ab3f8644', '65f023632bc46470c104b76f', 'string', 'https://google.com',
        '65f5b471c919a450ab3f8641', 0, 0)
on conflict do nothing;
INSERT INTO store.category (id, store_id, name, image_link, parent_id, sequence, level)
VALUES ('65f5b471c919a450ab3f8645', '65f023632bc46470c104b76f', 'string', 'https://google.com',
        '65f5b471c919a450ab3f8641', 0, 0)
on conflict do nothing;

INSERT INTO store.category (id, store_id, name, image_link, parent_id, sequence, level)
VALUES ('65f5b471c919a450ab3f8646', '65f023632bc46470c104b76f', 'string', 'https://google.com',
        '65f5b471c919a450ab3f8642', 0, 0)
on conflict do nothing;

INSERT INTO store.category (id, store_id, name, image_link, parent_id, sequence, level)
VALUES ('65f5b471c919a450ab3f8647', '65f023632bc46470c104b76f', 'string', 'https://google.com',
        '65f5b471c919a450ab3f8642', 0, 0)
on conflict do nothing;


INSERT INTO store.product (id, store_id, category_id, name, description, price, currency, published, deleted,
                           image_link, amount, product_type, sub_products, image_links)
VALUES ('65f5b484c919a450ab3f864a', '65f023632bc46470c104b76f', '65f5b471c919a450ab3f8649', 'string', 'string', 20,
        'USD', true, false, 'https://google.com', 15, 'SINGLE',
        '{"productIds":{"java.util.ImmutableCollections$ListN":[]}}',
        '{"imagesLinks":{"java.util.ArrayList":[{"imageLink":"https://google.com"}]}}')
on conflict do nothing;

INSERT INTO store.product (id, store_id, category_id, name, description, price, currency, published, deleted,
                           image_link, amount, product_type, sub_products, image_links)
VALUES ('65f5b494c919a450ab3f864b', '65f023632bc46470c104b76f', '65f5b471c919a450ab3f8649', 'string', 'string', 20,
        'USD', true, false, 'https://google.com', 15, 'SINGLE',
        '{"productIds":{"java.util.ImmutableCollections$ListN":[]}}',
        '{"imagesLinks":{"java.util.ArrayList":[{"imageLink":"https://google.com"}]}}')
on conflict do nothing;
INSERT INTO store.product (id, store_id, category_id, name, description, price, currency, published, deleted,
                           image_link, amount, product_type, sub_products, image_links)
VALUES ('65f5b4c9c919a450ab3f864c', '65f023632bc46470c104b76f', '65f5b471c919a450ab3f8649', 'string', 'string', 20,
        'USD', true, false, 'https://google.com', 15, 'GROUP',
        '{"productIds":{"java.util.ArrayList":[{"id":"65f5b494c919a450ab3f864b"},{"id":"65f5b484c919a450ab3f864a"}]}}',
        '{"imagesLinks":{"java.util.ArrayList":[{"imageLink":"https://google.com"}]}}')
on conflict do nothing;

INSERT INTO store.product_details (id, product_id, store_id, product_details)
VALUES ('65f5b4c9c919a450ab3f864d', '65f5b4c9c919a450ab3f864c', '65f023632bc46470c104b76f',
        '{"detail":{"name":"any","shortDescription":"sd","descriptions":{"java.util.ArrayList":["d1","d2"]},"spec":{"java.util.LinkedHashMap":{"key":"value"}},"ltr":true},"extraImages":{"imagesLinks":{"java.util.ArrayList":[{"imageLink":"https://google.com"}]}}}')
on conflict do nothing;
