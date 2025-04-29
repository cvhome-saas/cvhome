/*
generate more product_category relation based on product from 91 to 135 and category file 25 to 36
*/
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (1, 1)
on conflict do nothing;