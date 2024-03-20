INSERT INTO store.category (id, store_id, name, image_link, parent_id, sequence, level)
VALUES ('65f5b471c919a450ab3f8649', '507f1f77bcf86cd799439011', 'string', 'https://google.com', null, 0, 0) on conflict do nothing;

INSERT INTO store.category (id, store_id, name, image_link, parent_id, sequence, level)
VALUES ('65f5b471c919a450ab3f8641', '507f1f77bcf86cd799439011', 'string', 'https://google.com', null, 0, 0) on conflict do nothing;

INSERT INTO store.category (id, store_id, name, image_link, parent_id, sequence, level)
VALUES ('65f5b471c919a450ab3f8642', '507f1f77bcf86cd799439011', 'string', 'https://google.com',
        '65f5b471c919a450ab3f8649', 0, 0) on conflict do nothing;

INSERT INTO store.category (id, store_id, name, image_link, parent_id, sequence, level)
VALUES ('65f5b471c919a450ab3f8643', '507f1f77bcf86cd799439011', 'string', 'https://google.com',
        '65f5b471c919a450ab3f8649', 0, 0) on conflict do nothing;

INSERT INTO store.category (id, store_id, name, image_link, parent_id, sequence, level)
VALUES ('65f5b471c919a450ab3f8644', '507f1f77bcf86cd799439011', 'string', 'https://google.com',
        '65f5b471c919a450ab3f8641', 0, 0) on conflict do nothing;
INSERT INTO store.category (id, store_id, name, image_link, parent_id, sequence, level)
VALUES ('65f5b471c919a450ab3f8645', '507f1f77bcf86cd799439011', 'string', 'https://google.com',
        '65f5b471c919a450ab3f8641', 0, 0) on conflict do nothing;

INSERT INTO store.category (id, store_id, name, image_link, parent_id, sequence, level)
VALUES ('65f5b471c919a450ab3f8646', '507f1f77bcf86cd799439011', 'string', 'https://google.com',
        '65f5b471c919a450ab3f8642', 0, 0) on conflict do nothing;

INSERT INTO store.category (id, store_id, name, image_link, parent_id, sequence, level)
VALUES ('65f5b471c919a450ab3f8647', '507f1f77bcf86cd799439011', 'string', 'https://google.com',
        '65f5b471c919a450ab3f8642', 0, 0) on conflict do nothing;


INSERT INTO store.product (id, store_id, category_id, name, description, price, currency, published, deleted,
                           image_link, amount, product_type, sub_products, image_links)
VALUES ('65f5b484c919a450ab3f864a', '507f1f77bcf86cd799439011', '65f5b471c919a450ab3f8649', 'string', 'string', 20,
        'USD', false, false, 'https://google.com', 15, 'SINGLE',
        '{"productIds":{"java.util.ImmutableCollections$ListN":[]}}',
        '{"imagesLinks":{"java.util.ArrayList":[{"imageLink":"https://google.com"}]}}')  on conflict do nothing;

INSERT INTO store.product (id, store_id, category_id, name, description, price, currency, published, deleted,
                           image_link, amount, product_type, sub_products, image_links)
VALUES ('65f5b494c919a450ab3f864b', '507f1f77bcf86cd799439011', '65f5b471c919a450ab3f8649', 'string', 'string', 20,
        'USD', false, false, 'https://google.com', 15, 'SINGLE',
        '{"productIds":{"java.util.ImmutableCollections$ListN":[]}}',
        '{"imagesLinks":{"java.util.ArrayList":[{"imageLink":"https://google.com"}]}}') on conflict do nothing;
INSERT INTO store.product (id, store_id, category_id, name, description, price, currency, published, deleted,
                           image_link, amount, product_type, sub_products, image_links)
VALUES ('65f5b4c9c919a450ab3f864c', '507f1f77bcf86cd799439011', '65f5b471c919a450ab3f8649', 'string', 'string', 20,
        'USD', false, false, 'https://google.com', 15, 'GROUP',
        '{"productIds":{"java.util.ArrayList":[{"id":"65f5b494c919a450ab3f864b"},{"id":"65f5b484c919a450ab3f864a"}]}}',
        '{"imagesLinks":{"java.util.ArrayList":[{"imageLink":"https://google.com"}]}}') on conflict do nothing;

INSERT INTO store.product_details (id, product_id, store_id, product_details)
VALUES ('65f5b4c9c919a450ab3f864d', '65f5b4c9c919a450ab3f864c', '507f1f77bcf86cd799439011',
        '{"detail":{"name":"any","shortDescription":"sd","descriptions":{"java.util.ArrayList":["d1","d2"]},"spec":{"java.util.LinkedHashMap":{"key":"value"}},"ltr":false},"extraImages":{"imagesLinks":{"java.util.ArrayList":[{"imageLink":"https://google.com"}]}}}') on conflict do nothing;
