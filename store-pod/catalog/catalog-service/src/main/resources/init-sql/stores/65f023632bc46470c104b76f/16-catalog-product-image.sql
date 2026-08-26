/*
Generated SQL inserts for catalog.product_image based on products 1-15.
Adds 5 relevant images for every product.
product_image_id starts from 1 and increments sequentially.
Image filenames are generated based on product names/SEF URLs.
*/

-- Product 1: Nike ZoomX Invincible Run 3 (IDs: 1-5)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (1, true, 0, -400001, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-RUN-001/SMALL/nike-zoomx-invincible-run-3-1.jpg', 0, 1)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (2, false, 0, -400002, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-RUN-001/SMALL/nike-zoomx-invincible-run-3-2.jpg', 1, 1)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (3, false, 0, -400003, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-RUN-001/SMALL/nike-zoomx-invincible-run-3-3.jpg', 2, 1)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (4, false, 0, -400004, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-RUN-001/SMALL/nike-zoomx-invincible-run-3-4.jpg', 3, 1)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (5, false, 0, -400005, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-RUN-001/SMALL/nike-zoomx-invincible-run-3-5.jpg', 4, 1)
on conflict do nothing;

-- Product 2: Zara Satin Effect Midi Dress (IDs: 6-10)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (6, true, 0, -400006, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-CL-DRS02/SMALL/zara-satin-effect-midi-dress-2-1.jpg', 0, 2)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (7, false, 0, -400007, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-CL-DRS02/SMALL/zara-satin-effect-midi-dress-2-2.jpg', 1, 2)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (8, false, 0, -400008, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-CL-DRS02/SMALL/zara-satin-effect-midi-dress-2-3.jpg', 2, 2)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (9, false, 0, -400009, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-CL-DRS02/SMALL/zara-satin-effect-midi-dress-2-4.jpg', 3, 2)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (10, false, 0, -400010, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-CL-DRS02/SMALL/zara-satin-effect-midi-dress-2-5.jpg', 4, 2)
on conflict do nothing;

-- Product 3: Adidas Tiro 23 Training Pants (IDs: 11-15)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (11, true, 0, -400011, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-CL-TPT03/SMALL/adidas-tiro-23-training-pants-3-1.jpg', 0, 3)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (12, false, 0, -400012, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-CL-TPT03/SMALL/adidas-tiro-23-training-pants-3-2.jpg', 1, 3)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (13, false, 0, -400013, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-CL-TPT03/SMALL/adidas-tiro-23-training-pants-3-3.jpg', 2, 3)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (14, false, 0, -400014, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-CL-TPT03/SMALL/adidas-tiro-23-training-pants-3-4.jpg', 3, 3)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (15, false, 0, -400015, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-CL-TPT03/SMALL/adidas-tiro-23-training-pants-3-5.jpg', 4, 3)
on conflict do nothing;

-- Product 4: H&M Rib-knit Sweater (IDs: 16-20)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (16, true, 0, -400016, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-CL-SWT04/SMALL/hm-rib-knit-sweater-women-4-1.jpg', 0, 4)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (17, false, 0, -400017, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-CL-SWT04/SMALL/hm-rib-knit-sweater-women-4-2.jpg', 1, 4)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (18, false, 0, -400018, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-CL-SWT04/SMALL/hm-rib-knit-sweater-women-4-3.jpg', 2, 4)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (19, false, 0, -400019, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-CL-SWT04/SMALL/hm-rib-knit-sweater-women-4-4.jpg', 3, 4)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (20, false, 0, -400020, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-CL-SWT04/SMALL/hm-rib-knit-sweater-women-4-5.jpg', 4, 4)
on conflict do nothing;

-- Product 5: Gucci GG Marmont Shoulder Bag (IDs: 21-25)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (21, true, 0, -400021, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-BG-MAR05/SMALL/gucci-gg-marmont-shoulder-bag-5-1.jpg', 0, 5)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (22, false, 0, -400022, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-BG-MAR05/SMALL/gucci-gg-marmont-shoulder-bag-5-2.jpg', 1, 5)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (23, false, 0, -400023, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-BG-MAR05/SMALL/gucci-gg-marmont-shoulder-bag-5-3.jpg', 2, 5)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (24, false, 0, -400024, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-BG-MAR05/SMALL/gucci-gg-marmont-shoulder-bag-5-4.jpg', 3, 5)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (25, false, 0, -400025, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-BG-MAR05/SMALL/gucci-gg-marmont-shoulder-bag-5-5.jpg', 4, 5)
on conflict do nothing;

-- Product 6: Chanel Butterfly Sunglasses (IDs: 26-30)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (26, true, 0, -400026, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-AC-SUN06/SMALL/chanel-butterfly-sunglasses-6-1.jpg', 0, 6)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (27, false, 0, -400027, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-AC-SUN06/SMALL/chanel-butterfly-sunglasses-6-2.jpg', 1, 6)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (28, false, 0, -400028, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-AC-SUN06/SMALL/chanel-butterfly-sunglasses-6-3.jpg', 2, 6)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (29, false, 0, -400029, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-AC-SUN06/SMALL/chanel-butterfly-sunglasses-6-4.jpg', 3, 6)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (30, false, 0, -400030, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-AC-SUN06/SMALL/chanel-butterfly-sunglasses-6-5.jpg', 4, 6)
on conflict do nothing;

-- Product 7: Nike Club Fleece Hoodie (Kids) (IDs: 31-35)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (31, true, 0, -400031, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-CL-KHD07/SMALL/nike-club-fleece-hoodie-kids-7-1.jpg', 0, 7)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (32, false, 0, -400032, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-CL-KHD07/SMALL/nike-club-fleece-hoodie-kids-7-2.jpg', 1, 7)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (33, false, 0, -400033, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-CL-KHD07/SMALL/nike-club-fleece-hoodie-kids-7-3.jpg', 2, 7)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (34, false, 0, -400034, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-CL-KHD07/SMALL/nike-club-fleece-hoodie-kids-7-4.jpg', 3, 7)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (35, false, 0, -400035, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-CL-KHD07/SMALL/nike-club-fleece-hoodie-kids-7-5.jpg', 4, 7)
on conflict do nothing;

-- Product 8: Zara Contrast Sole Sneakers (IDs: 36-40)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (36, true, 0, -400036, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-SH-SNK08/SMALL/zara-contrast-sole-sneakers-men-8-1.jpg', 0, 8)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (37, false, 0, -400037, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-SH-SNK08/SMALL/zara-contrast-sole-sneakers-men-8-2.jpg', 1, 8)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (38, false, 0, -400038, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-SH-SNK08/SMALL/zara-contrast-sole-sneakers-men-8-3.jpg', 2, 8)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (39, false, 0, -400039, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-SH-SNK08/SMALL/zara-contrast-sole-sneakers-men-8-4.jpg', 3, 8)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (40, false, 0, -400040, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-SH-SNK08/SMALL/zara-contrast-sole-sneakers-men-8-5.jpg', 4, 8)
on conflict do nothing;

-- Product 9: Adidas Classic Backpack (IDs: 41-45)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (41, true, 0, -400041, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-BG-BPK09/SMALL/adidas-classic-backpack-9-1.jpg', 0, 9)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (42, false, 0, -400042, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-BG-BPK09/SMALL/adidas-classic-backpack-9-2.jpg', 1, 9)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (43, false, 0, -400043, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-BG-BPK09/SMALL/adidas-classic-backpack-9-3.jpg', 2, 9)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (44, false, 0, -400044, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-BG-BPK09/SMALL/adidas-classic-backpack-9-4.jpg', 3, 9)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (45, false, 0, -400045, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-BG-BPK09/SMALL/adidas-classic-backpack-9-5.jpg', 4, 9)
on conflict do nothing;

-- Product 10: H&M Leather Belt (IDs: 46-50)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (46, true, 0, -400046, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-AC-BLT10/SMALL/hm-leather-belt-men-10-1.jpg', 0, 10)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (47, false, 0, -400047, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-AC-BLT10/SMALL/hm-leather-belt-men-10-2.jpg', 1, 10)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (48, false, 0, -400048, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-AC-BLT10/SMALL/hm-leather-belt-men-10-3.jpg', 2, 10)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (49, false, 0, -400049, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-AC-BLT10/SMALL/hm-leather-belt-men-10-4.jpg', 3, 10)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (50, false, 0, -400050, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-AC-BLT10/SMALL/hm-leather-belt-men-10-5.jpg', 4, 10)
on conflict do nothing;

-- Product 11: Gucci Jordaan Loafer (Women) (IDs: 51-55)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (51, true, 0, -400051, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-SH-LOF11/SMALL/gucci-jordaan-loafer-women-11-1.jpg', 0, 11)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (52, false, 0, -400052, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-SH-LOF11/SMALL/gucci-jordaan-loafer-women-11-2.jpg', 1, 11)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (53, false, 0, -400053, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-SH-LOF11/SMALL/gucci-jordaan-loafer-women-11-3.jpg', 2, 11)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (54, false, 0, -400054, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-SH-LOF11/SMALL/gucci-jordaan-loafer-women-11-4.jpg', 3, 11)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (55, false, 0, -400055, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-SH-LOF11/SMALL/gucci-jordaan-loafer-women-11-5.jpg', 4, 11)
on conflict do nothing;

-- Product 12: Chanel Classic Card Holder (IDs: 56-60)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (56, true, 0, -400056, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-AC-CRD12/SMALL/chanel-classic-card-holder-12-1.jpg', 0, 12)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (57, false, 0, -400057, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-AC-CRD12/SMALL/chanel-classic-card-holder-12-2.jpg', 1, 12)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (58, false, 0, -400058, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-AC-CRD12/SMALL/chanel-classic-card-holder-12-3.jpg', 2, 12)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (59, false, 0, -400059, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-AC-CRD12/SMALL/chanel-classic-card-holder-12-4.jpg', 3, 12)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (60, false, 0, -400060, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-AC-CRD12/SMALL/chanel-classic-card-holder-12-5.jpg', 4, 12)
on conflict do nothing;

-- Product 13: Nike One Leggings (Women) (IDs: 61-65)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (61, true, 0, -400061, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-CL-LEG13/SMALL/nike-one-leggings-women-13-1.jpg', 0, 13)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (62, false, 0, -400062, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-CL-LEG13/SMALL/nike-one-leggings-women-13-2.jpg', 1, 13)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (63, false, 0, -400063, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-CL-LEG13/SMALL/nike-one-leggings-women-13-3.jpg', 2, 13)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (64, false, 0, -400064, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-CL-LEG13/SMALL/nike-one-leggings-women-13-4.jpg', 3, 13)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (65, false, 0, -400065, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-CL-LEG13/SMALL/nike-one-leggings-women-13-5.jpg', 4, 13)
on conflict do nothing;

-- Product 14: Zara Basic Polo Shirt (IDs: 66-70)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (66, true, 0, -400066, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-CL-POL14/SMALL/zara-basic-polo-shirt-men-14-1.jpg', 0, 14)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (67, false, 0, -400067, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-CL-POL14/SMALL/zara-basic-polo-shirt-men-14-2.jpg', 1, 14)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (68, false, 0, -400068, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-CL-POL14/SMALL/zara-basic-polo-shirt-men-14-3.jpg', 2, 14)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (69, false, 0, -400069, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-CL-POL14/SMALL/zara-basic-polo-shirt-men-14-4.jpg', 3, 14)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (70, false, 0, -400070, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-CL-POL14/SMALL/zara-basic-polo-shirt-men-14-5.jpg', 4, 14)
on conflict do nothing;

-- Product 15: Adidas Adilette Aqua Slides (Kids) (IDs: 71-75)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (71, true, 0, -400071, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-SH-SND15/SMALL/adidas-adilette-aqua-slides-kids-15-1.jpg', 0, 15)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (72, false, 0, -400072, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-SH-SND15/SMALL/adidas-adilette-aqua-slides-kids-15-2.jpg', 1, 15)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (73, false, 0, -400073, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-SH-SND15/SMALL/adidas-adilette-aqua-slides-kids-15-3.jpg', 2, 15)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (74, false, 0, -400074, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-SH-SND15/SMALL/adidas-adilette-aqua-slides-kids-15-4.jpg', 3, 15)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (75, false, 0, -400075, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-SH-SND15/SMALL/adidas-adilette-aqua-slides-kids-15-5.jpg', 4, 15)
on conflict do nothing;

-- Product 16: H&M Patterned Wrap Dress (IDs: 76-80)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (76, true, 0, -400076, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-CL-DRS16/SMALL/hm-patterned-wrap-dress-women-16-1.jpg', 0, 16)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (77, false, 0, -400077, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-CL-DRS16/SMALL/hm-patterned-wrap-dress-women-16-2.jpg', 1, 16)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (78, false, 0, -400078, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-CL-DRS16/SMALL/hm-patterned-wrap-dress-women-16-3.jpg', 2, 16)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (79, false, 0, -400079, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-CL-DRS16/SMALL/hm-patterned-wrap-dress-women-16-4.jpg', 3, 16)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (80, false, 0, -400080, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-CL-DRS16/SMALL/hm-patterned-wrap-dress-women-16-5.jpg', 4, 16)
on conflict do nothing;

-- Product 17: Gucci GG Supreme Wallet (IDs: 81-85)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (81, true, 0, -400081, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-AC-WAL17/SMALL/gucci-gg-supreme-wallet-men-17-1.jpg', 0, 17)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (82, false, 0, -400082, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-AC-WAL17/SMALL/gucci-gg-supreme-wallet-men-17-2.jpg', 1, 17)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (83, false, 0, -400083, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-AC-WAL17/SMALL/gucci-gg-supreme-wallet-men-17-3.jpg', 2, 17)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (84, false, 0, -400084, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-AC-WAL17/SMALL/gucci-gg-supreme-wallet-men-17-4.jpg', 3, 17)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (85, false, 0, -400085, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-AC-WAL17/SMALL/gucci-gg-supreme-wallet-men-17-5.jpg', 4, 17)
on conflict do nothing;

-- Product 18: Chanel CC Logo Brooch (IDs: 86-90)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (86, true, 0, -400086, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-AC-BRH18/SMALL/chanel-cc-logo-brooch-18-1.jpg', 0, 18)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (87, false, 0, -400087, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-AC-BRH18/SMALL/chanel-cc-logo-brooch-18-2.jpg', 1, 18)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (88, false, 0, -400088, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-AC-BRH18/SMALL/chanel-cc-logo-brooch-18-3.jpg', 2, 18)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (89, false, 0, -400089, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-AC-BRH18/SMALL/chanel-cc-logo-brooch-18-4.jpg', 3, 18)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (90, false, 0, -400090, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-AC-BRH18/SMALL/chanel-cc-logo-brooch-18-5.jpg', 4, 18)
on conflict do nothing;

-- Product 19: Nike DNA Basketball Shorts (IDs: 91-95)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (91, true, 0, -400091, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-CL-BBS19/SMALL/nike-dna-basketball-shorts-men-19-1.jpg', 0, 19)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (92, false, 0, -400092, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-CL-BBS19/SMALL/nike-dna-basketball-shorts-men-19-2.jpg', 1, 19)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (93, false, 0, -400093, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-CL-BBS19/SMALL/nike-dna-basketball-shorts-men-19-3.jpg', 2, 19)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (94, false, 0, -400094, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-CL-BBS19/SMALL/nike-dna-basketball-shorts-men-19-4.jpg', 3, 19)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (95, false, 0, -400095, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-CL-BBS19/SMALL/nike-dna-basketball-shorts-men-19-5.jpg', 4, 19)
on conflict do nothing;

-- Product 20: Zara Flat Leather Sandals (IDs: 96-100)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (96, true, 0, -400096, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-SH-SND20/SMALL/zara-flat-leather-sandals-women-20-1.jpg', 0, 20)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (97, false, 0, -400097, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-SH-SND20/SMALL/zara-flat-leather-sandals-women-20-2.jpg', 1, 20)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (98, false, 0, -400098, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-SH-SND20/SMALL/zara-flat-leather-sandals-women-20-3.jpg', 2, 20)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (99, false, 0, -400099, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-SH-SND20/SMALL/zara-flat-leather-sandals-women-20-4.jpg', 3, 20)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (100, false, 0, -400100, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-SH-SND20/SMALL/zara-flat-leather-sandals-women-20-5.jpg', 4, 20)
on conflict do nothing;

-- Product 21: Adidas Essentials Fleece Hoodie (IDs: 101-105)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (101, true, 0, -400101, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-CL-HOD21/SMALL/adidas-essentials-fleece-hoodie-men-21-1.jpg', 0, 21)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (102, false, 0, -400102, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-CL-HOD21/SMALL/adidas-essentials-fleece-hoodie-men-21-2.jpg', 1, 21)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (103, false, 0, -400103, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-CL-HOD21/SMALL/adidas-essentials-fleece-hoodie-men-21-3.jpg', 2, 21)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (104, false, 0, -400104, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-CL-HOD21/SMALL/adidas-essentials-fleece-hoodie-men-21-4.jpg', 3, 21)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (105, false, 0, -400105, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-CL-HOD21/SMALL/adidas-essentials-fleece-hoodie-men-21-5.jpg', 4, 21)
on conflict do nothing;

-- Product 22: H&M 5-Pack T-Shirts (Kids) (IDs: 106-110)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (106, true, 0, -400106, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-CL-KTP22/SMALL/hm-5-pack-tshirts-kids-22-1.jpg', 0, 22)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (107, false, 0, -400107, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-CL-KTP22/SMALL/hm-5-pack-tshirts-kids-22-2.jpg', 1, 22)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (108, false, 0, -400108, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-CL-KTP22/SMALL/hm-5-pack-tshirts-kids-22-3.jpg', 2, 22)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (109, false, 0, -400109, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-CL-KTP22/SMALL/hm-5-pack-tshirts-kids-22-4.jpg', 3, 22)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (110, false, 0, -400110, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-CL-KTP22/SMALL/hm-5-pack-tshirts-kids-22-5.jpg', 4, 22)
on conflict do nothing;

-- Product 23: Gucci Ace Leather Sneaker (IDs: 111-115)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (111, true, 0, -400111, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-SH-SNK23/SMALL/gucci-ace-leather-sneaker-men-23-1.jpg', 0, 23)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (112, false, 0, -400112, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-SH-SNK23/SMALL/gucci-ace-leather-sneaker-men-23-2.jpg', 1, 23)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (113, false, 0, -400113, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-SH-SNK23/SMALL/gucci-ace-leather-sneaker-men-23-3.jpg', 2, 23)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (114, false, 0, -400114, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-SH-SNK23/SMALL/gucci-ace-leather-sneaker-men-23-4.jpg', 3, 23)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (115, false, 0, -400115, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-SH-SNK23/SMALL/gucci-ace-leather-sneaker-men-23-5.jpg', 4, 23)
on conflict do nothing;

-- Product 24: Chanel Ballerinas (IDs: 116-120)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (116, true, 0, -400116, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-SH-BAL24/SMALL/chanel-ballerinas-24-1.jpg', 0, 24)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (117, false, 0, -400117, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-SH-BAL24/SMALL/chanel-ballerinas-24-2.jpg', 1, 24)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (118, false, 0, -400118, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-SH-BAL24/SMALL/chanel-ballerinas-24-3.jpg', 2, 24)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (119, false, 0, -400119, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-SH-BAL24/SMALL/chanel-ballerinas-24-4.jpg', 3, 24)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (120, false, 0, -400120, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-SH-BAL24/SMALL/chanel-ballerinas-24-5.jpg', 4, 24)
on conflict do nothing;

-- Product 25: Nike Dri-FIT One Luxe Tank (IDs: 121-125)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (121, true, 0, -400121, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-CL-TNK25/SMALL/nike-dri-fit-one-luxe-tank-women-25-1.jpg', 0, 25)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (122, false, 0, -400122, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-CL-TNK25/SMALL/nike-dri-fit-one-luxe-tank-women-25-2.jpg', 1, 25)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (123, false, 0, -400123, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-CL-TNK25/SMALL/nike-dri-fit-one-luxe-tank-women-25-3.jpg', 2, 25)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (124, false, 0, -400124, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-CL-TNK25/SMALL/nike-dri-fit-one-luxe-tank-women-25-4.jpg', 3, 25)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (125, false, 0, -400125, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-CL-TNK25/SMALL/nike-dri-fit-one-luxe-tank-women-25-5.jpg', 4, 25)
on conflict do nothing;

-- Product 26: Zara Slim Fit Chinos (IDs: 126-130)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (126, true, 0, -400126, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-CL-TRS26/SMALL/zara-slim-fit-chinos-men-26-1.jpg', 0, 26)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (127, false, 0, -400127, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-CL-TRS26/SMALL/zara-slim-fit-chinos-men-26-2.jpg', 1, 26)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (128, false, 0, -400128, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-CL-TRS26/SMALL/zara-slim-fit-chinos-men-26-3.jpg', 2, 26)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (129, false, 0, -400129, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-CL-TRS26/SMALL/zara-slim-fit-chinos-men-26-4.jpg', 3, 26)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (130, false, 0, -400130, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-CL-TRS26/SMALL/zara-slim-fit-chinos-men-26-5.jpg', 4, 26)
on conflict do nothing;

-- Product 27: Adidas Essentials Tracksuit (Kids) (IDs: 131-135)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (131, true, 0, -400131, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-CL-KTS27/SMALL/adidas-essentials-tracksuit-kids-27-1.jpg', 0, 27)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (132, false, 0, -400132, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-CL-KTS27/SMALL/adidas-essentials-tracksuit-kids-27-2.jpg', 1, 27)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (133, false, 0, -400133, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-CL-KTS27/SMALL/adidas-essentials-tracksuit-kids-27-3.jpg', 2, 27)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (134, false, 0, -400134, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-CL-KTS27/SMALL/adidas-essentials-tracksuit-kids-27-4.jpg', 3, 27)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (135, false, 0, -400135, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-CL-KTS27/SMALL/adidas-essentials-tracksuit-kids-27-5.jpg', 4, 27)
on conflict do nothing;

-- Product 28: H&M Large Scarf (IDs: 136-140)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (136, true, 0, -400136, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-AC-SCF28/SMALL/hm-large-scarf-28-1.jpg', 0, 28)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (137, false, 0, -400137, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-AC-SCF28/SMALL/hm-large-scarf-28-2.jpg', 1, 28)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (138, false, 0, -400138, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-AC-SCF28/SMALL/hm-large-scarf-28-3.jpg', 2, 28)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (139, false, 0, -400139, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-AC-SCF28/SMALL/hm-large-scarf-28-4.jpg', 3, 28)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (140, false, 0, -400140, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-AC-SCF28/SMALL/hm-large-scarf-28-5.jpg', 4, 28)
on conflict do nothing;

-- Product 29: Gucci GG Marmont Belt Bag (IDs: 141-145)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (141, true, 0, -400141, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-BG-BBG29/SMALL/gucci-gg-marmont-belt-bag-29-1.jpg', 0, 29)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (142, false, 0, -400142, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-BG-BBG29/SMALL/gucci-gg-marmont-belt-bag-29-2.jpg', 1, 29)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (143, false, 0, -400143, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-BG-BBG29/SMALL/gucci-gg-marmont-belt-bag-29-3.jpg', 2, 29)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (144, false, 0, -400144, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-BG-BBG29/SMALL/gucci-gg-marmont-belt-bag-29-4.jpg', 3, 29)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (145, false, 0, -400145, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-BG-BBG29/SMALL/gucci-gg-marmont-belt-bag-29-5.jpg', 4, 29)
on conflict do nothing;

-- Product 30: Chanel Trainers (IDs: 146-150)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (146, true, 0, -400146, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-SH-SNK30/SMALL/chanel-trainers-30-1.jpg', 0, 30)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (147, false, 0, -400147, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-SH-SNK30/SMALL/chanel-trainers-30-2.jpg', 1, 30)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (148, false, 0, -400148, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-SH-SNK30/SMALL/chanel-trainers-30-3.jpg', 2, 30)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (149, false, 0, -400149, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-SH-SNK30/SMALL/chanel-trainers-30-4.jpg', 3, 30)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (150, false, 0, -400150, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-SH-SNK30/SMALL/chanel-trainers-30-5.jpg', 4, 30)
on conflict do nothing;

-- Product 31: Nike Windrunner Jacket (IDs: 151-155)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (151, true, 0, -400151, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-CL-JKT31/SMALL/nike-windrunner-jacket-men-31-1.jpg', 0, 31)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (152, false, 0, -400152, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-CL-JKT31/SMALL/nike-windrunner-jacket-men-31-2.jpg', 1, 31)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (153, false, 0, -400153, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-CL-JKT31/SMALL/nike-windrunner-jacket-men-31-3.jpg', 2, 31)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (154, false, 0, -400154, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-CL-JKT31/SMALL/nike-windrunner-jacket-men-31-4.jpg', 3, 31)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (155, false, 0, -400155, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-CL-JKT31/SMALL/nike-windrunner-jacket-men-31-5.jpg', 4, 31)
on conflict do nothing;

-- Product 32: Zara High-Waisted Skinny Jeans (IDs: 156-160)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (156, true, 0, -400156, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-CL-JNS32/SMALL/zara-high-waisted-skinny-jeans-women-32-1.jpg', 0, 32)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (157, false, 0, -400157, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-CL-JNS32/SMALL/zara-high-waisted-skinny-jeans-women-32-2.jpg', 1, 32)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (158, false, 0, -400158, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-CL-JNS32/SMALL/zara-high-waisted-skinny-jeans-women-32-3.jpg', 2, 32)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (159, false, 0, -400159, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-CL-JNS32/SMALL/zara-high-waisted-skinny-jeans-women-32-4.jpg', 3, 32)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (160, false, 0, -400160, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-CL-JNS32/SMALL/zara-high-waisted-skinny-jeans-women-32-5.jpg', 4, 32)
on conflict do nothing;

-- Product 33: Adidas Baseball Cap (IDs: 161-165)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (161, true, 0, -400161, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-AC-CAP33/SMALL/adidas-trefoil-baseball-cap-33-1.jpg', 0, 33)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (162, false, 0, -400162, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-AC-CAP33/SMALL/adidas-trefoil-baseball-cap-33-2.jpg', 1, 33)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (163, false, 0, -400163, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-AC-CAP33/SMALL/adidas-trefoil-baseball-cap-33-3.jpg', 2, 33)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (164, false, 0, -400164, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-AC-CAP33/SMALL/adidas-trefoil-baseball-cap-33-4.jpg', 3, 33)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (165, false, 0, -400165, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-AC-CAP33/SMALL/adidas-trefoil-baseball-cap-33-5.jpg', 4, 33)
on conflict do nothing;

-- Product 34: H&M Waterproof Rain Jacket (Kids) (IDs: 166-170)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (166, true, 0, -400166, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-CL-KRJ34/SMALL/hm-waterproof-rain-jacket-kids-34-1.jpg', 0, 34)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (167, false, 0, -400167, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-CL-KRJ34/SMALL/hm-waterproof-rain-jacket-kids-34-2.jpg', 1, 34)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (168, false, 0, -400168, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-CL-KRJ34/SMALL/hm-waterproof-rain-jacket-kids-34-3.jpg', 2, 34)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (169, false, 0, -400169, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-CL-KRJ34/SMALL/hm-waterproof-rain-jacket-kids-34-4.jpg', 3, 34)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (170, false, 0, -400170, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-CL-KRJ34/SMALL/hm-waterproof-rain-jacket-kids-34-5.jpg', 4, 34)
on conflict do nothing;

-- Product 35: Gucci GG Wool Silk Scarf (IDs: 171-175)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (171, true, 0, -400171, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-AC-SCF35/SMALL/gucci-gg-wool-silk-scarf-35-1.jpg', 0, 35)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (172, false, 0, -400172, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-AC-SCF35/SMALL/gucci-gg-wool-silk-scarf-35-2.jpg', 1, 35)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (173, false, 0, -400173, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-AC-SCF35/SMALL/gucci-gg-wool-silk-scarf-35-3.jpg', 2, 35)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (174, false, 0, -400174, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-AC-SCF35/SMALL/gucci-gg-wool-silk-scarf-35-4.jpg', 3, 35)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (175, false, 0, -400175, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-AC-SCF35/SMALL/gucci-gg-wool-silk-scarf-35-5.jpg', 4, 35)
on conflict do nothing;

-- Product 36: Chanel Wallet on Chain (WOC) (IDs: 176-180)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (176, true, 0, -400176, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-BG-WOC36/SMALL/chanel-wallet-on-chain-woc-36-1.jpg', 0, 36)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (177, false, 0, -400177, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-BG-WOC36/SMALL/chanel-wallet-on-chain-woc-36-2.jpg', 1, 36)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (178, false, 0, -400178, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-BG-WOC36/SMALL/chanel-wallet-on-chain-woc-36-3.jpg', 2, 36)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (179, false, 0, -400179, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-BG-WOC36/SMALL/chanel-wallet-on-chain-woc-36-4.jpg', 3, 36)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (180, false, 0, -400180, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-BG-WOC36/SMALL/chanel-wallet-on-chain-woc-36-5.jpg', 4, 36)
on conflict do nothing;

-- Product 37: Nike Tempo Running Shorts (Women) (IDs: 181-185)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (181, true, 0, -400181, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-CL-WRS37/SMALL/nike-tempo-running-shorts-women-37-1.jpg', 0, 37)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (182, false, 0, -400182, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-CL-WRS37/SMALL/nike-tempo-running-shorts-women-37-2.jpg', 1, 37)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (183, false, 0, -400183, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-CL-WRS37/SMALL/nike-tempo-running-shorts-women-37-3.jpg', 2, 37)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (184, false, 0, -400184, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-CL-WRS37/SMALL/nike-tempo-running-shorts-women-37-4.jpg', 3, 37)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (185, false, 0, -400185, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-CL-WRS37/SMALL/nike-tempo-running-shorts-women-37-5.jpg', 4, 37)
on conflict do nothing;

-- Product 38: Zara Basic Knit Sweater (Kids) (IDs: 186-190)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (186, true, 0, -400186, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-CL-KSW38/SMALL/zara-basic-knit-sweater-kids-38-1.jpg', 0, 38)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (187, false, 0, -400187, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-CL-KSW38/SMALL/zara-basic-knit-sweater-kids-38-2.jpg', 1, 38)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (188, false, 0, -400188, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-CL-KSW38/SMALL/zara-basic-knit-sweater-kids-38-3.jpg', 2, 38)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (189, false, 0, -400189, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-CL-KSW38/SMALL/zara-basic-knit-sweater-kids-38-4.jpg', 3, 38)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (190, false, 0, -400190, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-CL-KSW38/SMALL/zara-basic-knit-sweater-kids-38-5.jpg', 4, 38)
on conflict do nothing;

-- Product 39: Adidas Cushioned Crew Socks (3-Pack) (IDs: 191-195)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (191, true, 0, -400191, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-AC-SCK39/SMALL/adidas-cushioned-crew-socks-3pack-39-1.jpg', 0, 39)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (192, false, 0, -400192, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-AC-SCK39/SMALL/adidas-cushioned-crew-socks-3pack-39-2.jpg', 1, 39)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (193, false, 0, -400193, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-AC-SCK39/SMALL/adidas-cushioned-crew-socks-3pack-39-3.jpg', 2, 39)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (194, false, 0, -400194, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-AC-SCK39/SMALL/adidas-cushioned-crew-socks-3pack-39-4.jpg', 3, 39)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (195, false, 0, -400195, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-AC-SCK39/SMALL/adidas-cushioned-crew-socks-3pack-39-5.jpg', 4, 39)
on conflict do nothing;

-- Product 40: H&M Swim Shorts (IDs: 196-200)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (196, true, 0, -400196, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-CL-SWM40/SMALL/hm-swim-shorts-men-40-1.jpg', 0, 40)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (197, false, 0, -400197, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-CL-SWM40/SMALL/hm-swim-shorts-men-40-2.jpg', 1, 40)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (198, false, 0, -400198, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-CL-SWM40/SMALL/hm-swim-shorts-men-40-3.jpg', 2, 40)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (199, false, 0, -400199, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-CL-SWM40/SMALL/hm-swim-shorts-men-40-4.jpg', 3, 40)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (200, false, 0, -400200, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-HM-CL-SWM40/SMALL/hm-swim-shorts-men-40-5.jpg', 4, 40)
on conflict do nothing;

-- Product 41: Gucci 1953 Horsebit Loafer (IDs: 201-205)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (201, true, 0, -400201, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-SH-HBL41/SMALL/gucci-1953-horsebit-loafer-men-41-1.jpg', 0, 41)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (202, false, 0, -400202, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-SH-HBL41/SMALL/gucci-1953-horsebit-loafer-men-41-2.jpg', 1, 41)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (203, false, 0, -400203, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-SH-HBL41/SMALL/gucci-1953-horsebit-loafer-men-41-3.jpg', 2, 41)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (204, false, 0, -400204, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-SH-HBL41/SMALL/gucci-1953-horsebit-loafer-men-41-4.jpg', 3, 41)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (205, false, 0, -400205, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-GU-SH-HBL41/SMALL/gucci-1953-horsebit-loafer-men-41-5.jpg', 4, 41)
on conflict do nothing;

-- Product 42: Chanel CC Stud Earrings (IDs: 206-210)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (206, true, 0, -400206, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-AC-EAR42/SMALL/chanel-cc-stud-earrings-42-1.jpg', 0, 42)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (207, false, 0, -400207, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-AC-EAR42/SMALL/chanel-cc-stud-earrings-42-2.jpg', 1, 42)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (208, false, 0, -400208, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-AC-EAR42/SMALL/chanel-cc-stud-earrings-42-3.jpg', 2, 42)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (209, false, 0, -400209, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-AC-EAR42/SMALL/chanel-cc-stud-earrings-42-4.jpg', 3, 42)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (210, false, 0, -400210, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-CH-AC-EAR42/SMALL/chanel-cc-stud-earrings-42-5.jpg', 4, 42)
on conflict do nothing;

-- Product 43: Nike Brasilia Duffel Bag (Medium) (IDs: 211-215)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (211, true, 0, -400211, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-BG-DUF43/SMALL/nike-brasilia-duffel-bag-medium-43-1.jpg', 0, 43)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (212, false, 0, -400212, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-BG-DUF43/SMALL/nike-brasilia-duffel-bag-medium-43-2.jpg', 1, 43)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (213, false, 0, -400213, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-BG-DUF43/SMALL/nike-brasilia-duffel-bag-medium-43-3.jpg', 2, 43)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (214, false, 0, -400214, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-BG-DUF43/SMALL/nike-brasilia-duffel-bag-medium-43-4.jpg', 3, 43)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (215, false, 0, -400215, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-NK-BG-DUF43/SMALL/nike-brasilia-duffel-bag-medium-43-5.jpg', 4, 43)
on conflict do nothing;

-- Product 44: Zara Poplin Shirt (IDs: 216-220)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (216, true, 0, -400216, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-CL-BLS44/SMALL/zara-poplin-shirt-women-44-1.jpg', 0, 44)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (217, false, 0, -400217, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-CL-BLS44/SMALL/zara-poplin-shirt-women-44-2.jpg', 1, 44)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (218, false, 0, -400218, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-CL-BLS44/SMALL/zara-poplin-shirt-women-44-3.jpg', 2, 44)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (219, false, 0, -400219, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-CL-BLS44/SMALL/zara-poplin-shirt-women-44-4.jpg', 3, 44)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (220, false, 0, -400220, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-ZR-CL-BLS44/SMALL/zara-poplin-shirt-women-44-5.jpg', 4, 44)
on conflict do nothing;

-- Product 45: Adidas Adilette Comfort Slides (IDs: 221-225)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (221, true, 0, -400221, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-SH-SLD45/SMALL/adidas-adilette-comfort-slides-45-1.jpg', 0, 45)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (222, false, 0, -400222, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-SH-SLD45/SMALL/adidas-adilette-comfort-slides-45-2.jpg', 1, 45)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (223, false, 0, -400223, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-SH-SLD45/SMALL/adidas-adilette-comfort-slides-45-3.jpg', 2, 45)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (224, false, 0, -400224, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-SH-SLD45/SMALL/adidas-adilette-comfort-slides-45-4.jpg', 3, 45)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (225, false, 0, -400225, 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b76f/SKU-AD-SH-SLD45/SMALL/adidas-adilette-comfort-slides-45-5.jpg', 4, 45)
on conflict do nothing;