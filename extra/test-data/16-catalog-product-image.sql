/*
generate more product_image relation based on product file  and add 5 relevant images for every product  start product_image_id=451
Image filenames are generated based on product names/SEF URLs.
*/
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (1, true, false, 0, $generator.product_product_image_name(), 0, 46)
on conflict do nothing;
