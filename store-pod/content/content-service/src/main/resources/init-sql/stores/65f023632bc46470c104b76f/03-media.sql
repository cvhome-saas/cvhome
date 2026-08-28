-- No urls here: storage_key is the object's key in the bucket, and the url a browser fetches is composed from
-- it and the configured CDN base when the asset is read. The seed used to carry a second, absolute copy, which
-- meant every environment came up serving the demo library from one developer's MinIO.

-- Demo product photos, registered as media assets.
--
-- The objects already exist in MinIO under the key layout catalog used when it owned files; the seed
-- registers them rather than moving bytes. Ids are negative on purpose: media asset ids come from a
-- sequence that only grows upward, so seed-only rows below zero can never collide with uploads.
-- catalog.product_image references these ids — see 16-catalog-product-image.sql in the catalog service.
-- bytes is nominal: the objects are placeholders a tester generates, so no real size exists to record,
-- and 0 made the storage figure read "no storage used" beside a library listing hundreds of files.

INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400001, '65f023632bc46470c104b76f', 'nike-zoomx-invincible-run-3-1.jpg', 'nike-zoomx-invincible-run-3-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400001', 'products/65f023632bc46470c104b76f/SKU-NK-RUN-001/SMALL/nike-zoomx-invincible-run-3-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400002, '65f023632bc46470c104b76f', 'nike-zoomx-invincible-run-3-2.jpg', 'nike-zoomx-invincible-run-3-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400002', 'products/65f023632bc46470c104b76f/SKU-NK-RUN-001/SMALL/nike-zoomx-invincible-run-3-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400003, '65f023632bc46470c104b76f', 'nike-zoomx-invincible-run-3-3.jpg', 'nike-zoomx-invincible-run-3-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400003', 'products/65f023632bc46470c104b76f/SKU-NK-RUN-001/SMALL/nike-zoomx-invincible-run-3-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400004, '65f023632bc46470c104b76f', 'nike-zoomx-invincible-run-3-4.jpg', 'nike-zoomx-invincible-run-3-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400004', 'products/65f023632bc46470c104b76f/SKU-NK-RUN-001/SMALL/nike-zoomx-invincible-run-3-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400005, '65f023632bc46470c104b76f', 'nike-zoomx-invincible-run-3-5.jpg', 'nike-zoomx-invincible-run-3-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400005', 'products/65f023632bc46470c104b76f/SKU-NK-RUN-001/SMALL/nike-zoomx-invincible-run-3-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400006, '65f023632bc46470c104b76f', 'zara-satin-effect-midi-dress-2-1.jpg', 'zara-satin-effect-midi-dress-2-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400006', 'products/65f023632bc46470c104b76f/SKU-ZR-CL-DRS02/SMALL/zara-satin-effect-midi-dress-2-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400007, '65f023632bc46470c104b76f', 'zara-satin-effect-midi-dress-2-2.jpg', 'zara-satin-effect-midi-dress-2-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400007', 'products/65f023632bc46470c104b76f/SKU-ZR-CL-DRS02/SMALL/zara-satin-effect-midi-dress-2-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400008, '65f023632bc46470c104b76f', 'zara-satin-effect-midi-dress-2-3.jpg', 'zara-satin-effect-midi-dress-2-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400008', 'products/65f023632bc46470c104b76f/SKU-ZR-CL-DRS02/SMALL/zara-satin-effect-midi-dress-2-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400009, '65f023632bc46470c104b76f', 'zara-satin-effect-midi-dress-2-4.jpg', 'zara-satin-effect-midi-dress-2-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400009', 'products/65f023632bc46470c104b76f/SKU-ZR-CL-DRS02/SMALL/zara-satin-effect-midi-dress-2-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400010, '65f023632bc46470c104b76f', 'zara-satin-effect-midi-dress-2-5.jpg', 'zara-satin-effect-midi-dress-2-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400010', 'products/65f023632bc46470c104b76f/SKU-ZR-CL-DRS02/SMALL/zara-satin-effect-midi-dress-2-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400011, '65f023632bc46470c104b76f', 'adidas-tiro-23-training-pants-3-1.jpg', 'adidas-tiro-23-training-pants-3-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400011', 'products/65f023632bc46470c104b76f/SKU-AD-CL-TPT03/SMALL/adidas-tiro-23-training-pants-3-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400012, '65f023632bc46470c104b76f', 'adidas-tiro-23-training-pants-3-2.jpg', 'adidas-tiro-23-training-pants-3-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400012', 'products/65f023632bc46470c104b76f/SKU-AD-CL-TPT03/SMALL/adidas-tiro-23-training-pants-3-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400013, '65f023632bc46470c104b76f', 'adidas-tiro-23-training-pants-3-3.jpg', 'adidas-tiro-23-training-pants-3-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400013', 'products/65f023632bc46470c104b76f/SKU-AD-CL-TPT03/SMALL/adidas-tiro-23-training-pants-3-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400014, '65f023632bc46470c104b76f', 'adidas-tiro-23-training-pants-3-4.jpg', 'adidas-tiro-23-training-pants-3-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400014', 'products/65f023632bc46470c104b76f/SKU-AD-CL-TPT03/SMALL/adidas-tiro-23-training-pants-3-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400015, '65f023632bc46470c104b76f', 'adidas-tiro-23-training-pants-3-5.jpg', 'adidas-tiro-23-training-pants-3-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400015', 'products/65f023632bc46470c104b76f/SKU-AD-CL-TPT03/SMALL/adidas-tiro-23-training-pants-3-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400016, '65f023632bc46470c104b76f', 'hm-rib-knit-sweater-women-4-1.jpg', 'hm-rib-knit-sweater-women-4-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400016', 'products/65f023632bc46470c104b76f/SKU-HM-CL-SWT04/SMALL/hm-rib-knit-sweater-women-4-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400017, '65f023632bc46470c104b76f', 'hm-rib-knit-sweater-women-4-2.jpg', 'hm-rib-knit-sweater-women-4-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400017', 'products/65f023632bc46470c104b76f/SKU-HM-CL-SWT04/SMALL/hm-rib-knit-sweater-women-4-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400018, '65f023632bc46470c104b76f', 'hm-rib-knit-sweater-women-4-3.jpg', 'hm-rib-knit-sweater-women-4-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400018', 'products/65f023632bc46470c104b76f/SKU-HM-CL-SWT04/SMALL/hm-rib-knit-sweater-women-4-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400019, '65f023632bc46470c104b76f', 'hm-rib-knit-sweater-women-4-4.jpg', 'hm-rib-knit-sweater-women-4-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400019', 'products/65f023632bc46470c104b76f/SKU-HM-CL-SWT04/SMALL/hm-rib-knit-sweater-women-4-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400020, '65f023632bc46470c104b76f', 'hm-rib-knit-sweater-women-4-5.jpg', 'hm-rib-knit-sweater-women-4-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400020', 'products/65f023632bc46470c104b76f/SKU-HM-CL-SWT04/SMALL/hm-rib-knit-sweater-women-4-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400021, '65f023632bc46470c104b76f', 'gucci-gg-marmont-shoulder-bag-5-1.jpg', 'gucci-gg-marmont-shoulder-bag-5-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400021', 'products/65f023632bc46470c104b76f/SKU-GU-BG-MAR05/SMALL/gucci-gg-marmont-shoulder-bag-5-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400022, '65f023632bc46470c104b76f', 'gucci-gg-marmont-shoulder-bag-5-2.jpg', 'gucci-gg-marmont-shoulder-bag-5-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400022', 'products/65f023632bc46470c104b76f/SKU-GU-BG-MAR05/SMALL/gucci-gg-marmont-shoulder-bag-5-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400023, '65f023632bc46470c104b76f', 'gucci-gg-marmont-shoulder-bag-5-3.jpg', 'gucci-gg-marmont-shoulder-bag-5-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400023', 'products/65f023632bc46470c104b76f/SKU-GU-BG-MAR05/SMALL/gucci-gg-marmont-shoulder-bag-5-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400024, '65f023632bc46470c104b76f', 'gucci-gg-marmont-shoulder-bag-5-4.jpg', 'gucci-gg-marmont-shoulder-bag-5-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400024', 'products/65f023632bc46470c104b76f/SKU-GU-BG-MAR05/SMALL/gucci-gg-marmont-shoulder-bag-5-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400025, '65f023632bc46470c104b76f', 'gucci-gg-marmont-shoulder-bag-5-5.jpg', 'gucci-gg-marmont-shoulder-bag-5-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400025', 'products/65f023632bc46470c104b76f/SKU-GU-BG-MAR05/SMALL/gucci-gg-marmont-shoulder-bag-5-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400026, '65f023632bc46470c104b76f', 'chanel-butterfly-sunglasses-6-1.jpg', 'chanel-butterfly-sunglasses-6-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400026', 'products/65f023632bc46470c104b76f/SKU-CH-AC-SUN06/SMALL/chanel-butterfly-sunglasses-6-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400027, '65f023632bc46470c104b76f', 'chanel-butterfly-sunglasses-6-2.jpg', 'chanel-butterfly-sunglasses-6-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400027', 'products/65f023632bc46470c104b76f/SKU-CH-AC-SUN06/SMALL/chanel-butterfly-sunglasses-6-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400028, '65f023632bc46470c104b76f', 'chanel-butterfly-sunglasses-6-3.jpg', 'chanel-butterfly-sunglasses-6-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400028', 'products/65f023632bc46470c104b76f/SKU-CH-AC-SUN06/SMALL/chanel-butterfly-sunglasses-6-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400029, '65f023632bc46470c104b76f', 'chanel-butterfly-sunglasses-6-4.jpg', 'chanel-butterfly-sunglasses-6-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400029', 'products/65f023632bc46470c104b76f/SKU-CH-AC-SUN06/SMALL/chanel-butterfly-sunglasses-6-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400030, '65f023632bc46470c104b76f', 'chanel-butterfly-sunglasses-6-5.jpg', 'chanel-butterfly-sunglasses-6-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400030', 'products/65f023632bc46470c104b76f/SKU-CH-AC-SUN06/SMALL/chanel-butterfly-sunglasses-6-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400031, '65f023632bc46470c104b76f', 'nike-club-fleece-hoodie-kids-7-1.jpg', 'nike-club-fleece-hoodie-kids-7-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400031', 'products/65f023632bc46470c104b76f/SKU-NK-CL-KHD07/SMALL/nike-club-fleece-hoodie-kids-7-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400032, '65f023632bc46470c104b76f', 'nike-club-fleece-hoodie-kids-7-2.jpg', 'nike-club-fleece-hoodie-kids-7-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400032', 'products/65f023632bc46470c104b76f/SKU-NK-CL-KHD07/SMALL/nike-club-fleece-hoodie-kids-7-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400033, '65f023632bc46470c104b76f', 'nike-club-fleece-hoodie-kids-7-3.jpg', 'nike-club-fleece-hoodie-kids-7-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400033', 'products/65f023632bc46470c104b76f/SKU-NK-CL-KHD07/SMALL/nike-club-fleece-hoodie-kids-7-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400034, '65f023632bc46470c104b76f', 'nike-club-fleece-hoodie-kids-7-4.jpg', 'nike-club-fleece-hoodie-kids-7-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400034', 'products/65f023632bc46470c104b76f/SKU-NK-CL-KHD07/SMALL/nike-club-fleece-hoodie-kids-7-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400035, '65f023632bc46470c104b76f', 'nike-club-fleece-hoodie-kids-7-5.jpg', 'nike-club-fleece-hoodie-kids-7-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400035', 'products/65f023632bc46470c104b76f/SKU-NK-CL-KHD07/SMALL/nike-club-fleece-hoodie-kids-7-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400036, '65f023632bc46470c104b76f', 'zara-contrast-sole-sneakers-men-8-1.jpg', 'zara-contrast-sole-sneakers-men-8-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400036', 'products/65f023632bc46470c104b76f/SKU-ZR-SH-SNK08/SMALL/zara-contrast-sole-sneakers-men-8-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400037, '65f023632bc46470c104b76f', 'zara-contrast-sole-sneakers-men-8-2.jpg', 'zara-contrast-sole-sneakers-men-8-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400037', 'products/65f023632bc46470c104b76f/SKU-ZR-SH-SNK08/SMALL/zara-contrast-sole-sneakers-men-8-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400038, '65f023632bc46470c104b76f', 'zara-contrast-sole-sneakers-men-8-3.jpg', 'zara-contrast-sole-sneakers-men-8-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400038', 'products/65f023632bc46470c104b76f/SKU-ZR-SH-SNK08/SMALL/zara-contrast-sole-sneakers-men-8-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400039, '65f023632bc46470c104b76f', 'zara-contrast-sole-sneakers-men-8-4.jpg', 'zara-contrast-sole-sneakers-men-8-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400039', 'products/65f023632bc46470c104b76f/SKU-ZR-SH-SNK08/SMALL/zara-contrast-sole-sneakers-men-8-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400040, '65f023632bc46470c104b76f', 'zara-contrast-sole-sneakers-men-8-5.jpg', 'zara-contrast-sole-sneakers-men-8-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400040', 'products/65f023632bc46470c104b76f/SKU-ZR-SH-SNK08/SMALL/zara-contrast-sole-sneakers-men-8-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400041, '65f023632bc46470c104b76f', 'adidas-classic-backpack-9-1.jpg', 'adidas-classic-backpack-9-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400041', 'products/65f023632bc46470c104b76f/SKU-AD-BG-BPK09/SMALL/adidas-classic-backpack-9-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400042, '65f023632bc46470c104b76f', 'adidas-classic-backpack-9-2.jpg', 'adidas-classic-backpack-9-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400042', 'products/65f023632bc46470c104b76f/SKU-AD-BG-BPK09/SMALL/adidas-classic-backpack-9-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400043, '65f023632bc46470c104b76f', 'adidas-classic-backpack-9-3.jpg', 'adidas-classic-backpack-9-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400043', 'products/65f023632bc46470c104b76f/SKU-AD-BG-BPK09/SMALL/adidas-classic-backpack-9-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400044, '65f023632bc46470c104b76f', 'adidas-classic-backpack-9-4.jpg', 'adidas-classic-backpack-9-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400044', 'products/65f023632bc46470c104b76f/SKU-AD-BG-BPK09/SMALL/adidas-classic-backpack-9-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400045, '65f023632bc46470c104b76f', 'adidas-classic-backpack-9-5.jpg', 'adidas-classic-backpack-9-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400045', 'products/65f023632bc46470c104b76f/SKU-AD-BG-BPK09/SMALL/adidas-classic-backpack-9-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400046, '65f023632bc46470c104b76f', 'hm-leather-belt-men-10-1.jpg', 'hm-leather-belt-men-10-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400046', 'products/65f023632bc46470c104b76f/SKU-HM-AC-BLT10/SMALL/hm-leather-belt-men-10-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400047, '65f023632bc46470c104b76f', 'hm-leather-belt-men-10-2.jpg', 'hm-leather-belt-men-10-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400047', 'products/65f023632bc46470c104b76f/SKU-HM-AC-BLT10/SMALL/hm-leather-belt-men-10-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400048, '65f023632bc46470c104b76f', 'hm-leather-belt-men-10-3.jpg', 'hm-leather-belt-men-10-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400048', 'products/65f023632bc46470c104b76f/SKU-HM-AC-BLT10/SMALL/hm-leather-belt-men-10-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400049, '65f023632bc46470c104b76f', 'hm-leather-belt-men-10-4.jpg', 'hm-leather-belt-men-10-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400049', 'products/65f023632bc46470c104b76f/SKU-HM-AC-BLT10/SMALL/hm-leather-belt-men-10-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400050, '65f023632bc46470c104b76f', 'hm-leather-belt-men-10-5.jpg', 'hm-leather-belt-men-10-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400050', 'products/65f023632bc46470c104b76f/SKU-HM-AC-BLT10/SMALL/hm-leather-belt-men-10-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400051, '65f023632bc46470c104b76f', 'gucci-jordaan-loafer-women-11-1.jpg', 'gucci-jordaan-loafer-women-11-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400051', 'products/65f023632bc46470c104b76f/SKU-GU-SH-LOF11/SMALL/gucci-jordaan-loafer-women-11-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400052, '65f023632bc46470c104b76f', 'gucci-jordaan-loafer-women-11-2.jpg', 'gucci-jordaan-loafer-women-11-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400052', 'products/65f023632bc46470c104b76f/SKU-GU-SH-LOF11/SMALL/gucci-jordaan-loafer-women-11-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400053, '65f023632bc46470c104b76f', 'gucci-jordaan-loafer-women-11-3.jpg', 'gucci-jordaan-loafer-women-11-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400053', 'products/65f023632bc46470c104b76f/SKU-GU-SH-LOF11/SMALL/gucci-jordaan-loafer-women-11-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400054, '65f023632bc46470c104b76f', 'gucci-jordaan-loafer-women-11-4.jpg', 'gucci-jordaan-loafer-women-11-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400054', 'products/65f023632bc46470c104b76f/SKU-GU-SH-LOF11/SMALL/gucci-jordaan-loafer-women-11-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400055, '65f023632bc46470c104b76f', 'gucci-jordaan-loafer-women-11-5.jpg', 'gucci-jordaan-loafer-women-11-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400055', 'products/65f023632bc46470c104b76f/SKU-GU-SH-LOF11/SMALL/gucci-jordaan-loafer-women-11-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400056, '65f023632bc46470c104b76f', 'chanel-classic-card-holder-12-1.jpg', 'chanel-classic-card-holder-12-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400056', 'products/65f023632bc46470c104b76f/SKU-CH-AC-CRD12/SMALL/chanel-classic-card-holder-12-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400057, '65f023632bc46470c104b76f', 'chanel-classic-card-holder-12-2.jpg', 'chanel-classic-card-holder-12-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400057', 'products/65f023632bc46470c104b76f/SKU-CH-AC-CRD12/SMALL/chanel-classic-card-holder-12-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400058, '65f023632bc46470c104b76f', 'chanel-classic-card-holder-12-3.jpg', 'chanel-classic-card-holder-12-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400058', 'products/65f023632bc46470c104b76f/SKU-CH-AC-CRD12/SMALL/chanel-classic-card-holder-12-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400059, '65f023632bc46470c104b76f', 'chanel-classic-card-holder-12-4.jpg', 'chanel-classic-card-holder-12-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400059', 'products/65f023632bc46470c104b76f/SKU-CH-AC-CRD12/SMALL/chanel-classic-card-holder-12-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400060, '65f023632bc46470c104b76f', 'chanel-classic-card-holder-12-5.jpg', 'chanel-classic-card-holder-12-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400060', 'products/65f023632bc46470c104b76f/SKU-CH-AC-CRD12/SMALL/chanel-classic-card-holder-12-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400061, '65f023632bc46470c104b76f', 'nike-one-leggings-women-13-1.jpg', 'nike-one-leggings-women-13-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400061', 'products/65f023632bc46470c104b76f/SKU-NK-CL-LEG13/SMALL/nike-one-leggings-women-13-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400062, '65f023632bc46470c104b76f', 'nike-one-leggings-women-13-2.jpg', 'nike-one-leggings-women-13-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400062', 'products/65f023632bc46470c104b76f/SKU-NK-CL-LEG13/SMALL/nike-one-leggings-women-13-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400063, '65f023632bc46470c104b76f', 'nike-one-leggings-women-13-3.jpg', 'nike-one-leggings-women-13-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400063', 'products/65f023632bc46470c104b76f/SKU-NK-CL-LEG13/SMALL/nike-one-leggings-women-13-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400064, '65f023632bc46470c104b76f', 'nike-one-leggings-women-13-4.jpg', 'nike-one-leggings-women-13-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400064', 'products/65f023632bc46470c104b76f/SKU-NK-CL-LEG13/SMALL/nike-one-leggings-women-13-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400065, '65f023632bc46470c104b76f', 'nike-one-leggings-women-13-5.jpg', 'nike-one-leggings-women-13-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400065', 'products/65f023632bc46470c104b76f/SKU-NK-CL-LEG13/SMALL/nike-one-leggings-women-13-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400066, '65f023632bc46470c104b76f', 'zara-basic-polo-shirt-men-14-1.jpg', 'zara-basic-polo-shirt-men-14-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400066', 'products/65f023632bc46470c104b76f/SKU-ZR-CL-POL14/SMALL/zara-basic-polo-shirt-men-14-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400067, '65f023632bc46470c104b76f', 'zara-basic-polo-shirt-men-14-2.jpg', 'zara-basic-polo-shirt-men-14-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400067', 'products/65f023632bc46470c104b76f/SKU-ZR-CL-POL14/SMALL/zara-basic-polo-shirt-men-14-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400068, '65f023632bc46470c104b76f', 'zara-basic-polo-shirt-men-14-3.jpg', 'zara-basic-polo-shirt-men-14-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400068', 'products/65f023632bc46470c104b76f/SKU-ZR-CL-POL14/SMALL/zara-basic-polo-shirt-men-14-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400069, '65f023632bc46470c104b76f', 'zara-basic-polo-shirt-men-14-4.jpg', 'zara-basic-polo-shirt-men-14-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400069', 'products/65f023632bc46470c104b76f/SKU-ZR-CL-POL14/SMALL/zara-basic-polo-shirt-men-14-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400070, '65f023632bc46470c104b76f', 'zara-basic-polo-shirt-men-14-5.jpg', 'zara-basic-polo-shirt-men-14-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400070', 'products/65f023632bc46470c104b76f/SKU-ZR-CL-POL14/SMALL/zara-basic-polo-shirt-men-14-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400071, '65f023632bc46470c104b76f', 'adidas-adilette-aqua-slides-kids-15-1.jpg', 'adidas-adilette-aqua-slides-kids-15-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400071', 'products/65f023632bc46470c104b76f/SKU-AD-SH-SND15/SMALL/adidas-adilette-aqua-slides-kids-15-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400072, '65f023632bc46470c104b76f', 'adidas-adilette-aqua-slides-kids-15-2.jpg', 'adidas-adilette-aqua-slides-kids-15-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400072', 'products/65f023632bc46470c104b76f/SKU-AD-SH-SND15/SMALL/adidas-adilette-aqua-slides-kids-15-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400073, '65f023632bc46470c104b76f', 'adidas-adilette-aqua-slides-kids-15-3.jpg', 'adidas-adilette-aqua-slides-kids-15-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400073', 'products/65f023632bc46470c104b76f/SKU-AD-SH-SND15/SMALL/adidas-adilette-aqua-slides-kids-15-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400074, '65f023632bc46470c104b76f', 'adidas-adilette-aqua-slides-kids-15-4.jpg', 'adidas-adilette-aqua-slides-kids-15-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400074', 'products/65f023632bc46470c104b76f/SKU-AD-SH-SND15/SMALL/adidas-adilette-aqua-slides-kids-15-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400075, '65f023632bc46470c104b76f', 'adidas-adilette-aqua-slides-kids-15-5.jpg', 'adidas-adilette-aqua-slides-kids-15-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400075', 'products/65f023632bc46470c104b76f/SKU-AD-SH-SND15/SMALL/adidas-adilette-aqua-slides-kids-15-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400076, '65f023632bc46470c104b76f', 'hm-patterned-wrap-dress-women-16-1.jpg', 'hm-patterned-wrap-dress-women-16-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400076', 'products/65f023632bc46470c104b76f/SKU-HM-CL-DRS16/SMALL/hm-patterned-wrap-dress-women-16-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400077, '65f023632bc46470c104b76f', 'hm-patterned-wrap-dress-women-16-2.jpg', 'hm-patterned-wrap-dress-women-16-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400077', 'products/65f023632bc46470c104b76f/SKU-HM-CL-DRS16/SMALL/hm-patterned-wrap-dress-women-16-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400078, '65f023632bc46470c104b76f', 'hm-patterned-wrap-dress-women-16-3.jpg', 'hm-patterned-wrap-dress-women-16-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400078', 'products/65f023632bc46470c104b76f/SKU-HM-CL-DRS16/SMALL/hm-patterned-wrap-dress-women-16-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400079, '65f023632bc46470c104b76f', 'hm-patterned-wrap-dress-women-16-4.jpg', 'hm-patterned-wrap-dress-women-16-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400079', 'products/65f023632bc46470c104b76f/SKU-HM-CL-DRS16/SMALL/hm-patterned-wrap-dress-women-16-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400080, '65f023632bc46470c104b76f', 'hm-patterned-wrap-dress-women-16-5.jpg', 'hm-patterned-wrap-dress-women-16-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400080', 'products/65f023632bc46470c104b76f/SKU-HM-CL-DRS16/SMALL/hm-patterned-wrap-dress-women-16-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400081, '65f023632bc46470c104b76f', 'gucci-gg-supreme-wallet-men-17-1.jpg', 'gucci-gg-supreme-wallet-men-17-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400081', 'products/65f023632bc46470c104b76f/SKU-GU-AC-WAL17/SMALL/gucci-gg-supreme-wallet-men-17-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400082, '65f023632bc46470c104b76f', 'gucci-gg-supreme-wallet-men-17-2.jpg', 'gucci-gg-supreme-wallet-men-17-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400082', 'products/65f023632bc46470c104b76f/SKU-GU-AC-WAL17/SMALL/gucci-gg-supreme-wallet-men-17-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400083, '65f023632bc46470c104b76f', 'gucci-gg-supreme-wallet-men-17-3.jpg', 'gucci-gg-supreme-wallet-men-17-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400083', 'products/65f023632bc46470c104b76f/SKU-GU-AC-WAL17/SMALL/gucci-gg-supreme-wallet-men-17-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400084, '65f023632bc46470c104b76f', 'gucci-gg-supreme-wallet-men-17-4.jpg', 'gucci-gg-supreme-wallet-men-17-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400084', 'products/65f023632bc46470c104b76f/SKU-GU-AC-WAL17/SMALL/gucci-gg-supreme-wallet-men-17-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400085, '65f023632bc46470c104b76f', 'gucci-gg-supreme-wallet-men-17-5.jpg', 'gucci-gg-supreme-wallet-men-17-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400085', 'products/65f023632bc46470c104b76f/SKU-GU-AC-WAL17/SMALL/gucci-gg-supreme-wallet-men-17-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400086, '65f023632bc46470c104b76f', 'chanel-cc-logo-brooch-18-1.jpg', 'chanel-cc-logo-brooch-18-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400086', 'products/65f023632bc46470c104b76f/SKU-CH-AC-BRH18/SMALL/chanel-cc-logo-brooch-18-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400087, '65f023632bc46470c104b76f', 'chanel-cc-logo-brooch-18-2.jpg', 'chanel-cc-logo-brooch-18-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400087', 'products/65f023632bc46470c104b76f/SKU-CH-AC-BRH18/SMALL/chanel-cc-logo-brooch-18-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400088, '65f023632bc46470c104b76f', 'chanel-cc-logo-brooch-18-3.jpg', 'chanel-cc-logo-brooch-18-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400088', 'products/65f023632bc46470c104b76f/SKU-CH-AC-BRH18/SMALL/chanel-cc-logo-brooch-18-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400089, '65f023632bc46470c104b76f', 'chanel-cc-logo-brooch-18-4.jpg', 'chanel-cc-logo-brooch-18-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400089', 'products/65f023632bc46470c104b76f/SKU-CH-AC-BRH18/SMALL/chanel-cc-logo-brooch-18-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400090, '65f023632bc46470c104b76f', 'chanel-cc-logo-brooch-18-5.jpg', 'chanel-cc-logo-brooch-18-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400090', 'products/65f023632bc46470c104b76f/SKU-CH-AC-BRH18/SMALL/chanel-cc-logo-brooch-18-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400091, '65f023632bc46470c104b76f', 'nike-dna-basketball-shorts-men-19-1.jpg', 'nike-dna-basketball-shorts-men-19-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400091', 'products/65f023632bc46470c104b76f/SKU-NK-CL-BBS19/SMALL/nike-dna-basketball-shorts-men-19-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400092, '65f023632bc46470c104b76f', 'nike-dna-basketball-shorts-men-19-2.jpg', 'nike-dna-basketball-shorts-men-19-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400092', 'products/65f023632bc46470c104b76f/SKU-NK-CL-BBS19/SMALL/nike-dna-basketball-shorts-men-19-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400093, '65f023632bc46470c104b76f', 'nike-dna-basketball-shorts-men-19-3.jpg', 'nike-dna-basketball-shorts-men-19-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400093', 'products/65f023632bc46470c104b76f/SKU-NK-CL-BBS19/SMALL/nike-dna-basketball-shorts-men-19-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400094, '65f023632bc46470c104b76f', 'nike-dna-basketball-shorts-men-19-4.jpg', 'nike-dna-basketball-shorts-men-19-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400094', 'products/65f023632bc46470c104b76f/SKU-NK-CL-BBS19/SMALL/nike-dna-basketball-shorts-men-19-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400095, '65f023632bc46470c104b76f', 'nike-dna-basketball-shorts-men-19-5.jpg', 'nike-dna-basketball-shorts-men-19-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400095', 'products/65f023632bc46470c104b76f/SKU-NK-CL-BBS19/SMALL/nike-dna-basketball-shorts-men-19-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400096, '65f023632bc46470c104b76f', 'zara-flat-leather-sandals-women-20-1.jpg', 'zara-flat-leather-sandals-women-20-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400096', 'products/65f023632bc46470c104b76f/SKU-ZR-SH-SND20/SMALL/zara-flat-leather-sandals-women-20-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400097, '65f023632bc46470c104b76f', 'zara-flat-leather-sandals-women-20-2.jpg', 'zara-flat-leather-sandals-women-20-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400097', 'products/65f023632bc46470c104b76f/SKU-ZR-SH-SND20/SMALL/zara-flat-leather-sandals-women-20-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400098, '65f023632bc46470c104b76f', 'zara-flat-leather-sandals-women-20-3.jpg', 'zara-flat-leather-sandals-women-20-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400098', 'products/65f023632bc46470c104b76f/SKU-ZR-SH-SND20/SMALL/zara-flat-leather-sandals-women-20-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400099, '65f023632bc46470c104b76f', 'zara-flat-leather-sandals-women-20-4.jpg', 'zara-flat-leather-sandals-women-20-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400099', 'products/65f023632bc46470c104b76f/SKU-ZR-SH-SND20/SMALL/zara-flat-leather-sandals-women-20-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400100, '65f023632bc46470c104b76f', 'zara-flat-leather-sandals-women-20-5.jpg', 'zara-flat-leather-sandals-women-20-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400100', 'products/65f023632bc46470c104b76f/SKU-ZR-SH-SND20/SMALL/zara-flat-leather-sandals-women-20-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400101, '65f023632bc46470c104b76f', 'adidas-essentials-fleece-hoodie-men-21-1.jpg', 'adidas-essentials-fleece-hoodie-men-21-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400101', 'products/65f023632bc46470c104b76f/SKU-AD-CL-HOD21/SMALL/adidas-essentials-fleece-hoodie-men-21-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400102, '65f023632bc46470c104b76f', 'adidas-essentials-fleece-hoodie-men-21-2.jpg', 'adidas-essentials-fleece-hoodie-men-21-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400102', 'products/65f023632bc46470c104b76f/SKU-AD-CL-HOD21/SMALL/adidas-essentials-fleece-hoodie-men-21-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400103, '65f023632bc46470c104b76f', 'adidas-essentials-fleece-hoodie-men-21-3.jpg', 'adidas-essentials-fleece-hoodie-men-21-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400103', 'products/65f023632bc46470c104b76f/SKU-AD-CL-HOD21/SMALL/adidas-essentials-fleece-hoodie-men-21-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400104, '65f023632bc46470c104b76f', 'adidas-essentials-fleece-hoodie-men-21-4.jpg', 'adidas-essentials-fleece-hoodie-men-21-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400104', 'products/65f023632bc46470c104b76f/SKU-AD-CL-HOD21/SMALL/adidas-essentials-fleece-hoodie-men-21-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400105, '65f023632bc46470c104b76f', 'adidas-essentials-fleece-hoodie-men-21-5.jpg', 'adidas-essentials-fleece-hoodie-men-21-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400105', 'products/65f023632bc46470c104b76f/SKU-AD-CL-HOD21/SMALL/adidas-essentials-fleece-hoodie-men-21-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400106, '65f023632bc46470c104b76f', 'hm-5-pack-tshirts-kids-22-1.jpg', 'hm-5-pack-tshirts-kids-22-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400106', 'products/65f023632bc46470c104b76f/SKU-HM-CL-KTP22/SMALL/hm-5-pack-tshirts-kids-22-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400107, '65f023632bc46470c104b76f', 'hm-5-pack-tshirts-kids-22-2.jpg', 'hm-5-pack-tshirts-kids-22-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400107', 'products/65f023632bc46470c104b76f/SKU-HM-CL-KTP22/SMALL/hm-5-pack-tshirts-kids-22-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400108, '65f023632bc46470c104b76f', 'hm-5-pack-tshirts-kids-22-3.jpg', 'hm-5-pack-tshirts-kids-22-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400108', 'products/65f023632bc46470c104b76f/SKU-HM-CL-KTP22/SMALL/hm-5-pack-tshirts-kids-22-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400109, '65f023632bc46470c104b76f', 'hm-5-pack-tshirts-kids-22-4.jpg', 'hm-5-pack-tshirts-kids-22-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400109', 'products/65f023632bc46470c104b76f/SKU-HM-CL-KTP22/SMALL/hm-5-pack-tshirts-kids-22-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400110, '65f023632bc46470c104b76f', 'hm-5-pack-tshirts-kids-22-5.jpg', 'hm-5-pack-tshirts-kids-22-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400110', 'products/65f023632bc46470c104b76f/SKU-HM-CL-KTP22/SMALL/hm-5-pack-tshirts-kids-22-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400111, '65f023632bc46470c104b76f', 'gucci-ace-leather-sneaker-men-23-1.jpg', 'gucci-ace-leather-sneaker-men-23-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400111', 'products/65f023632bc46470c104b76f/SKU-GU-SH-SNK23/SMALL/gucci-ace-leather-sneaker-men-23-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400112, '65f023632bc46470c104b76f', 'gucci-ace-leather-sneaker-men-23-2.jpg', 'gucci-ace-leather-sneaker-men-23-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400112', 'products/65f023632bc46470c104b76f/SKU-GU-SH-SNK23/SMALL/gucci-ace-leather-sneaker-men-23-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400113, '65f023632bc46470c104b76f', 'gucci-ace-leather-sneaker-men-23-3.jpg', 'gucci-ace-leather-sneaker-men-23-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400113', 'products/65f023632bc46470c104b76f/SKU-GU-SH-SNK23/SMALL/gucci-ace-leather-sneaker-men-23-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400114, '65f023632bc46470c104b76f', 'gucci-ace-leather-sneaker-men-23-4.jpg', 'gucci-ace-leather-sneaker-men-23-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400114', 'products/65f023632bc46470c104b76f/SKU-GU-SH-SNK23/SMALL/gucci-ace-leather-sneaker-men-23-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400115, '65f023632bc46470c104b76f', 'gucci-ace-leather-sneaker-men-23-5.jpg', 'gucci-ace-leather-sneaker-men-23-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400115', 'products/65f023632bc46470c104b76f/SKU-GU-SH-SNK23/SMALL/gucci-ace-leather-sneaker-men-23-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400116, '65f023632bc46470c104b76f', 'chanel-ballerinas-24-1.jpg', 'chanel-ballerinas-24-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400116', 'products/65f023632bc46470c104b76f/SKU-CH-SH-BAL24/SMALL/chanel-ballerinas-24-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400117, '65f023632bc46470c104b76f', 'chanel-ballerinas-24-2.jpg', 'chanel-ballerinas-24-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400117', 'products/65f023632bc46470c104b76f/SKU-CH-SH-BAL24/SMALL/chanel-ballerinas-24-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400118, '65f023632bc46470c104b76f', 'chanel-ballerinas-24-3.jpg', 'chanel-ballerinas-24-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400118', 'products/65f023632bc46470c104b76f/SKU-CH-SH-BAL24/SMALL/chanel-ballerinas-24-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400119, '65f023632bc46470c104b76f', 'chanel-ballerinas-24-4.jpg', 'chanel-ballerinas-24-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400119', 'products/65f023632bc46470c104b76f/SKU-CH-SH-BAL24/SMALL/chanel-ballerinas-24-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400120, '65f023632bc46470c104b76f', 'chanel-ballerinas-24-5.jpg', 'chanel-ballerinas-24-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400120', 'products/65f023632bc46470c104b76f/SKU-CH-SH-BAL24/SMALL/chanel-ballerinas-24-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400121, '65f023632bc46470c104b76f', 'nike-dri-fit-one-luxe-tank-women-25-1.jpg', 'nike-dri-fit-one-luxe-tank-women-25-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400121', 'products/65f023632bc46470c104b76f/SKU-NK-CL-TNK25/SMALL/nike-dri-fit-one-luxe-tank-women-25-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400122, '65f023632bc46470c104b76f', 'nike-dri-fit-one-luxe-tank-women-25-2.jpg', 'nike-dri-fit-one-luxe-tank-women-25-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400122', 'products/65f023632bc46470c104b76f/SKU-NK-CL-TNK25/SMALL/nike-dri-fit-one-luxe-tank-women-25-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400123, '65f023632bc46470c104b76f', 'nike-dri-fit-one-luxe-tank-women-25-3.jpg', 'nike-dri-fit-one-luxe-tank-women-25-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400123', 'products/65f023632bc46470c104b76f/SKU-NK-CL-TNK25/SMALL/nike-dri-fit-one-luxe-tank-women-25-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400124, '65f023632bc46470c104b76f', 'nike-dri-fit-one-luxe-tank-women-25-4.jpg', 'nike-dri-fit-one-luxe-tank-women-25-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400124', 'products/65f023632bc46470c104b76f/SKU-NK-CL-TNK25/SMALL/nike-dri-fit-one-luxe-tank-women-25-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400125, '65f023632bc46470c104b76f', 'nike-dri-fit-one-luxe-tank-women-25-5.jpg', 'nike-dri-fit-one-luxe-tank-women-25-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400125', 'products/65f023632bc46470c104b76f/SKU-NK-CL-TNK25/SMALL/nike-dri-fit-one-luxe-tank-women-25-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400126, '65f023632bc46470c104b76f', 'zara-slim-fit-chinos-men-26-1.jpg', 'zara-slim-fit-chinos-men-26-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400126', 'products/65f023632bc46470c104b76f/SKU-ZR-CL-TRS26/SMALL/zara-slim-fit-chinos-men-26-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400127, '65f023632bc46470c104b76f', 'zara-slim-fit-chinos-men-26-2.jpg', 'zara-slim-fit-chinos-men-26-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400127', 'products/65f023632bc46470c104b76f/SKU-ZR-CL-TRS26/SMALL/zara-slim-fit-chinos-men-26-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400128, '65f023632bc46470c104b76f', 'zara-slim-fit-chinos-men-26-3.jpg', 'zara-slim-fit-chinos-men-26-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400128', 'products/65f023632bc46470c104b76f/SKU-ZR-CL-TRS26/SMALL/zara-slim-fit-chinos-men-26-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400129, '65f023632bc46470c104b76f', 'zara-slim-fit-chinos-men-26-4.jpg', 'zara-slim-fit-chinos-men-26-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400129', 'products/65f023632bc46470c104b76f/SKU-ZR-CL-TRS26/SMALL/zara-slim-fit-chinos-men-26-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400130, '65f023632bc46470c104b76f', 'zara-slim-fit-chinos-men-26-5.jpg', 'zara-slim-fit-chinos-men-26-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400130', 'products/65f023632bc46470c104b76f/SKU-ZR-CL-TRS26/SMALL/zara-slim-fit-chinos-men-26-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400131, '65f023632bc46470c104b76f', 'adidas-essentials-tracksuit-kids-27-1.jpg', 'adidas-essentials-tracksuit-kids-27-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400131', 'products/65f023632bc46470c104b76f/SKU-AD-CL-KTS27/SMALL/adidas-essentials-tracksuit-kids-27-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400132, '65f023632bc46470c104b76f', 'adidas-essentials-tracksuit-kids-27-2.jpg', 'adidas-essentials-tracksuit-kids-27-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400132', 'products/65f023632bc46470c104b76f/SKU-AD-CL-KTS27/SMALL/adidas-essentials-tracksuit-kids-27-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400133, '65f023632bc46470c104b76f', 'adidas-essentials-tracksuit-kids-27-3.jpg', 'adidas-essentials-tracksuit-kids-27-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400133', 'products/65f023632bc46470c104b76f/SKU-AD-CL-KTS27/SMALL/adidas-essentials-tracksuit-kids-27-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400134, '65f023632bc46470c104b76f', 'adidas-essentials-tracksuit-kids-27-4.jpg', 'adidas-essentials-tracksuit-kids-27-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400134', 'products/65f023632bc46470c104b76f/SKU-AD-CL-KTS27/SMALL/adidas-essentials-tracksuit-kids-27-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400135, '65f023632bc46470c104b76f', 'adidas-essentials-tracksuit-kids-27-5.jpg', 'adidas-essentials-tracksuit-kids-27-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400135', 'products/65f023632bc46470c104b76f/SKU-AD-CL-KTS27/SMALL/adidas-essentials-tracksuit-kids-27-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400136, '65f023632bc46470c104b76f', 'hm-large-scarf-28-1.jpg', 'hm-large-scarf-28-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400136', 'products/65f023632bc46470c104b76f/SKU-HM-AC-SCF28/SMALL/hm-large-scarf-28-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400137, '65f023632bc46470c104b76f', 'hm-large-scarf-28-2.jpg', 'hm-large-scarf-28-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400137', 'products/65f023632bc46470c104b76f/SKU-HM-AC-SCF28/SMALL/hm-large-scarf-28-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400138, '65f023632bc46470c104b76f', 'hm-large-scarf-28-3.jpg', 'hm-large-scarf-28-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400138', 'products/65f023632bc46470c104b76f/SKU-HM-AC-SCF28/SMALL/hm-large-scarf-28-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400139, '65f023632bc46470c104b76f', 'hm-large-scarf-28-4.jpg', 'hm-large-scarf-28-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400139', 'products/65f023632bc46470c104b76f/SKU-HM-AC-SCF28/SMALL/hm-large-scarf-28-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400140, '65f023632bc46470c104b76f', 'hm-large-scarf-28-5.jpg', 'hm-large-scarf-28-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400140', 'products/65f023632bc46470c104b76f/SKU-HM-AC-SCF28/SMALL/hm-large-scarf-28-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400141, '65f023632bc46470c104b76f', 'gucci-gg-marmont-belt-bag-29-1.jpg', 'gucci-gg-marmont-belt-bag-29-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400141', 'products/65f023632bc46470c104b76f/SKU-GU-BG-BBG29/SMALL/gucci-gg-marmont-belt-bag-29-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400142, '65f023632bc46470c104b76f', 'gucci-gg-marmont-belt-bag-29-2.jpg', 'gucci-gg-marmont-belt-bag-29-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400142', 'products/65f023632bc46470c104b76f/SKU-GU-BG-BBG29/SMALL/gucci-gg-marmont-belt-bag-29-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400143, '65f023632bc46470c104b76f', 'gucci-gg-marmont-belt-bag-29-3.jpg', 'gucci-gg-marmont-belt-bag-29-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400143', 'products/65f023632bc46470c104b76f/SKU-GU-BG-BBG29/SMALL/gucci-gg-marmont-belt-bag-29-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400144, '65f023632bc46470c104b76f', 'gucci-gg-marmont-belt-bag-29-4.jpg', 'gucci-gg-marmont-belt-bag-29-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400144', 'products/65f023632bc46470c104b76f/SKU-GU-BG-BBG29/SMALL/gucci-gg-marmont-belt-bag-29-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400145, '65f023632bc46470c104b76f', 'gucci-gg-marmont-belt-bag-29-5.jpg', 'gucci-gg-marmont-belt-bag-29-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400145', 'products/65f023632bc46470c104b76f/SKU-GU-BG-BBG29/SMALL/gucci-gg-marmont-belt-bag-29-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400146, '65f023632bc46470c104b76f', 'chanel-trainers-30-1.jpg', 'chanel-trainers-30-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400146', 'products/65f023632bc46470c104b76f/SKU-CH-SH-SNK30/SMALL/chanel-trainers-30-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400147, '65f023632bc46470c104b76f', 'chanel-trainers-30-2.jpg', 'chanel-trainers-30-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400147', 'products/65f023632bc46470c104b76f/SKU-CH-SH-SNK30/SMALL/chanel-trainers-30-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400148, '65f023632bc46470c104b76f', 'chanel-trainers-30-3.jpg', 'chanel-trainers-30-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400148', 'products/65f023632bc46470c104b76f/SKU-CH-SH-SNK30/SMALL/chanel-trainers-30-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400149, '65f023632bc46470c104b76f', 'chanel-trainers-30-4.jpg', 'chanel-trainers-30-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400149', 'products/65f023632bc46470c104b76f/SKU-CH-SH-SNK30/SMALL/chanel-trainers-30-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400150, '65f023632bc46470c104b76f', 'chanel-trainers-30-5.jpg', 'chanel-trainers-30-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400150', 'products/65f023632bc46470c104b76f/SKU-CH-SH-SNK30/SMALL/chanel-trainers-30-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400151, '65f023632bc46470c104b76f', 'nike-windrunner-jacket-men-31-1.jpg', 'nike-windrunner-jacket-men-31-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400151', 'products/65f023632bc46470c104b76f/SKU-NK-CL-JKT31/SMALL/nike-windrunner-jacket-men-31-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400152, '65f023632bc46470c104b76f', 'nike-windrunner-jacket-men-31-2.jpg', 'nike-windrunner-jacket-men-31-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400152', 'products/65f023632bc46470c104b76f/SKU-NK-CL-JKT31/SMALL/nike-windrunner-jacket-men-31-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400153, '65f023632bc46470c104b76f', 'nike-windrunner-jacket-men-31-3.jpg', 'nike-windrunner-jacket-men-31-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400153', 'products/65f023632bc46470c104b76f/SKU-NK-CL-JKT31/SMALL/nike-windrunner-jacket-men-31-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400154, '65f023632bc46470c104b76f', 'nike-windrunner-jacket-men-31-4.jpg', 'nike-windrunner-jacket-men-31-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400154', 'products/65f023632bc46470c104b76f/SKU-NK-CL-JKT31/SMALL/nike-windrunner-jacket-men-31-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400155, '65f023632bc46470c104b76f', 'nike-windrunner-jacket-men-31-5.jpg', 'nike-windrunner-jacket-men-31-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400155', 'products/65f023632bc46470c104b76f/SKU-NK-CL-JKT31/SMALL/nike-windrunner-jacket-men-31-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400156, '65f023632bc46470c104b76f', 'zara-high-waisted-skinny-jeans-women-32-1.jpg', 'zara-high-waisted-skinny-jeans-women-32-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400156', 'products/65f023632bc46470c104b76f/SKU-ZR-CL-JNS32/SMALL/zara-high-waisted-skinny-jeans-women-32-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400157, '65f023632bc46470c104b76f', 'zara-high-waisted-skinny-jeans-women-32-2.jpg', 'zara-high-waisted-skinny-jeans-women-32-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400157', 'products/65f023632bc46470c104b76f/SKU-ZR-CL-JNS32/SMALL/zara-high-waisted-skinny-jeans-women-32-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400158, '65f023632bc46470c104b76f', 'zara-high-waisted-skinny-jeans-women-32-3.jpg', 'zara-high-waisted-skinny-jeans-women-32-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400158', 'products/65f023632bc46470c104b76f/SKU-ZR-CL-JNS32/SMALL/zara-high-waisted-skinny-jeans-women-32-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400159, '65f023632bc46470c104b76f', 'zara-high-waisted-skinny-jeans-women-32-4.jpg', 'zara-high-waisted-skinny-jeans-women-32-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400159', 'products/65f023632bc46470c104b76f/SKU-ZR-CL-JNS32/SMALL/zara-high-waisted-skinny-jeans-women-32-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400160, '65f023632bc46470c104b76f', 'zara-high-waisted-skinny-jeans-women-32-5.jpg', 'zara-high-waisted-skinny-jeans-women-32-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400160', 'products/65f023632bc46470c104b76f/SKU-ZR-CL-JNS32/SMALL/zara-high-waisted-skinny-jeans-women-32-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400161, '65f023632bc46470c104b76f', 'adidas-trefoil-baseball-cap-33-1.jpg', 'adidas-trefoil-baseball-cap-33-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400161', 'products/65f023632bc46470c104b76f/SKU-AD-AC-CAP33/SMALL/adidas-trefoil-baseball-cap-33-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400162, '65f023632bc46470c104b76f', 'adidas-trefoil-baseball-cap-33-2.jpg', 'adidas-trefoil-baseball-cap-33-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400162', 'products/65f023632bc46470c104b76f/SKU-AD-AC-CAP33/SMALL/adidas-trefoil-baseball-cap-33-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400163, '65f023632bc46470c104b76f', 'adidas-trefoil-baseball-cap-33-3.jpg', 'adidas-trefoil-baseball-cap-33-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400163', 'products/65f023632bc46470c104b76f/SKU-AD-AC-CAP33/SMALL/adidas-trefoil-baseball-cap-33-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400164, '65f023632bc46470c104b76f', 'adidas-trefoil-baseball-cap-33-4.jpg', 'adidas-trefoil-baseball-cap-33-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400164', 'products/65f023632bc46470c104b76f/SKU-AD-AC-CAP33/SMALL/adidas-trefoil-baseball-cap-33-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400165, '65f023632bc46470c104b76f', 'adidas-trefoil-baseball-cap-33-5.jpg', 'adidas-trefoil-baseball-cap-33-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400165', 'products/65f023632bc46470c104b76f/SKU-AD-AC-CAP33/SMALL/adidas-trefoil-baseball-cap-33-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400166, '65f023632bc46470c104b76f', 'hm-waterproof-rain-jacket-kids-34-1.jpg', 'hm-waterproof-rain-jacket-kids-34-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400166', 'products/65f023632bc46470c104b76f/SKU-HM-CL-KRJ34/SMALL/hm-waterproof-rain-jacket-kids-34-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400167, '65f023632bc46470c104b76f', 'hm-waterproof-rain-jacket-kids-34-2.jpg', 'hm-waterproof-rain-jacket-kids-34-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400167', 'products/65f023632bc46470c104b76f/SKU-HM-CL-KRJ34/SMALL/hm-waterproof-rain-jacket-kids-34-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400168, '65f023632bc46470c104b76f', 'hm-waterproof-rain-jacket-kids-34-3.jpg', 'hm-waterproof-rain-jacket-kids-34-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400168', 'products/65f023632bc46470c104b76f/SKU-HM-CL-KRJ34/SMALL/hm-waterproof-rain-jacket-kids-34-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400169, '65f023632bc46470c104b76f', 'hm-waterproof-rain-jacket-kids-34-4.jpg', 'hm-waterproof-rain-jacket-kids-34-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400169', 'products/65f023632bc46470c104b76f/SKU-HM-CL-KRJ34/SMALL/hm-waterproof-rain-jacket-kids-34-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400170, '65f023632bc46470c104b76f', 'hm-waterproof-rain-jacket-kids-34-5.jpg', 'hm-waterproof-rain-jacket-kids-34-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400170', 'products/65f023632bc46470c104b76f/SKU-HM-CL-KRJ34/SMALL/hm-waterproof-rain-jacket-kids-34-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400171, '65f023632bc46470c104b76f', 'gucci-gg-wool-silk-scarf-35-1.jpg', 'gucci-gg-wool-silk-scarf-35-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400171', 'products/65f023632bc46470c104b76f/SKU-GU-AC-SCF35/SMALL/gucci-gg-wool-silk-scarf-35-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400172, '65f023632bc46470c104b76f', 'gucci-gg-wool-silk-scarf-35-2.jpg', 'gucci-gg-wool-silk-scarf-35-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400172', 'products/65f023632bc46470c104b76f/SKU-GU-AC-SCF35/SMALL/gucci-gg-wool-silk-scarf-35-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400173, '65f023632bc46470c104b76f', 'gucci-gg-wool-silk-scarf-35-3.jpg', 'gucci-gg-wool-silk-scarf-35-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400173', 'products/65f023632bc46470c104b76f/SKU-GU-AC-SCF35/SMALL/gucci-gg-wool-silk-scarf-35-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400174, '65f023632bc46470c104b76f', 'gucci-gg-wool-silk-scarf-35-4.jpg', 'gucci-gg-wool-silk-scarf-35-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400174', 'products/65f023632bc46470c104b76f/SKU-GU-AC-SCF35/SMALL/gucci-gg-wool-silk-scarf-35-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400175, '65f023632bc46470c104b76f', 'gucci-gg-wool-silk-scarf-35-5.jpg', 'gucci-gg-wool-silk-scarf-35-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400175', 'products/65f023632bc46470c104b76f/SKU-GU-AC-SCF35/SMALL/gucci-gg-wool-silk-scarf-35-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400176, '65f023632bc46470c104b76f', 'chanel-wallet-on-chain-woc-36-1.jpg', 'chanel-wallet-on-chain-woc-36-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400176', 'products/65f023632bc46470c104b76f/SKU-CH-BG-WOC36/SMALL/chanel-wallet-on-chain-woc-36-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400177, '65f023632bc46470c104b76f', 'chanel-wallet-on-chain-woc-36-2.jpg', 'chanel-wallet-on-chain-woc-36-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400177', 'products/65f023632bc46470c104b76f/SKU-CH-BG-WOC36/SMALL/chanel-wallet-on-chain-woc-36-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400178, '65f023632bc46470c104b76f', 'chanel-wallet-on-chain-woc-36-3.jpg', 'chanel-wallet-on-chain-woc-36-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400178', 'products/65f023632bc46470c104b76f/SKU-CH-BG-WOC36/SMALL/chanel-wallet-on-chain-woc-36-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400179, '65f023632bc46470c104b76f', 'chanel-wallet-on-chain-woc-36-4.jpg', 'chanel-wallet-on-chain-woc-36-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400179', 'products/65f023632bc46470c104b76f/SKU-CH-BG-WOC36/SMALL/chanel-wallet-on-chain-woc-36-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400180, '65f023632bc46470c104b76f', 'chanel-wallet-on-chain-woc-36-5.jpg', 'chanel-wallet-on-chain-woc-36-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400180', 'products/65f023632bc46470c104b76f/SKU-CH-BG-WOC36/SMALL/chanel-wallet-on-chain-woc-36-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400181, '65f023632bc46470c104b76f', 'nike-tempo-running-shorts-women-37-1.jpg', 'nike-tempo-running-shorts-women-37-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400181', 'products/65f023632bc46470c104b76f/SKU-NK-CL-WRS37/SMALL/nike-tempo-running-shorts-women-37-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400182, '65f023632bc46470c104b76f', 'nike-tempo-running-shorts-women-37-2.jpg', 'nike-tempo-running-shorts-women-37-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400182', 'products/65f023632bc46470c104b76f/SKU-NK-CL-WRS37/SMALL/nike-tempo-running-shorts-women-37-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400183, '65f023632bc46470c104b76f', 'nike-tempo-running-shorts-women-37-3.jpg', 'nike-tempo-running-shorts-women-37-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400183', 'products/65f023632bc46470c104b76f/SKU-NK-CL-WRS37/SMALL/nike-tempo-running-shorts-women-37-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400184, '65f023632bc46470c104b76f', 'nike-tempo-running-shorts-women-37-4.jpg', 'nike-tempo-running-shorts-women-37-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400184', 'products/65f023632bc46470c104b76f/SKU-NK-CL-WRS37/SMALL/nike-tempo-running-shorts-women-37-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400185, '65f023632bc46470c104b76f', 'nike-tempo-running-shorts-women-37-5.jpg', 'nike-tempo-running-shorts-women-37-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400185', 'products/65f023632bc46470c104b76f/SKU-NK-CL-WRS37/SMALL/nike-tempo-running-shorts-women-37-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400186, '65f023632bc46470c104b76f', 'zara-basic-knit-sweater-kids-38-1.jpg', 'zara-basic-knit-sweater-kids-38-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400186', 'products/65f023632bc46470c104b76f/SKU-ZR-CL-KSW38/SMALL/zara-basic-knit-sweater-kids-38-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400187, '65f023632bc46470c104b76f', 'zara-basic-knit-sweater-kids-38-2.jpg', 'zara-basic-knit-sweater-kids-38-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400187', 'products/65f023632bc46470c104b76f/SKU-ZR-CL-KSW38/SMALL/zara-basic-knit-sweater-kids-38-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400188, '65f023632bc46470c104b76f', 'zara-basic-knit-sweater-kids-38-3.jpg', 'zara-basic-knit-sweater-kids-38-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400188', 'products/65f023632bc46470c104b76f/SKU-ZR-CL-KSW38/SMALL/zara-basic-knit-sweater-kids-38-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400189, '65f023632bc46470c104b76f', 'zara-basic-knit-sweater-kids-38-4.jpg', 'zara-basic-knit-sweater-kids-38-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400189', 'products/65f023632bc46470c104b76f/SKU-ZR-CL-KSW38/SMALL/zara-basic-knit-sweater-kids-38-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400190, '65f023632bc46470c104b76f', 'zara-basic-knit-sweater-kids-38-5.jpg', 'zara-basic-knit-sweater-kids-38-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400190', 'products/65f023632bc46470c104b76f/SKU-ZR-CL-KSW38/SMALL/zara-basic-knit-sweater-kids-38-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400191, '65f023632bc46470c104b76f', 'adidas-cushioned-crew-socks-3pack-39-1.jpg', 'adidas-cushioned-crew-socks-3pack-39-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400191', 'products/65f023632bc46470c104b76f/SKU-AD-AC-SCK39/SMALL/adidas-cushioned-crew-socks-3pack-39-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400192, '65f023632bc46470c104b76f', 'adidas-cushioned-crew-socks-3pack-39-2.jpg', 'adidas-cushioned-crew-socks-3pack-39-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400192', 'products/65f023632bc46470c104b76f/SKU-AD-AC-SCK39/SMALL/adidas-cushioned-crew-socks-3pack-39-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400193, '65f023632bc46470c104b76f', 'adidas-cushioned-crew-socks-3pack-39-3.jpg', 'adidas-cushioned-crew-socks-3pack-39-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400193', 'products/65f023632bc46470c104b76f/SKU-AD-AC-SCK39/SMALL/adidas-cushioned-crew-socks-3pack-39-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400194, '65f023632bc46470c104b76f', 'adidas-cushioned-crew-socks-3pack-39-4.jpg', 'adidas-cushioned-crew-socks-3pack-39-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400194', 'products/65f023632bc46470c104b76f/SKU-AD-AC-SCK39/SMALL/adidas-cushioned-crew-socks-3pack-39-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400195, '65f023632bc46470c104b76f', 'adidas-cushioned-crew-socks-3pack-39-5.jpg', 'adidas-cushioned-crew-socks-3pack-39-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400195', 'products/65f023632bc46470c104b76f/SKU-AD-AC-SCK39/SMALL/adidas-cushioned-crew-socks-3pack-39-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400196, '65f023632bc46470c104b76f', 'hm-swim-shorts-men-40-1.jpg', 'hm-swim-shorts-men-40-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400196', 'products/65f023632bc46470c104b76f/SKU-HM-CL-SWM40/SMALL/hm-swim-shorts-men-40-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400197, '65f023632bc46470c104b76f', 'hm-swim-shorts-men-40-2.jpg', 'hm-swim-shorts-men-40-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400197', 'products/65f023632bc46470c104b76f/SKU-HM-CL-SWM40/SMALL/hm-swim-shorts-men-40-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400198, '65f023632bc46470c104b76f', 'hm-swim-shorts-men-40-3.jpg', 'hm-swim-shorts-men-40-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400198', 'products/65f023632bc46470c104b76f/SKU-HM-CL-SWM40/SMALL/hm-swim-shorts-men-40-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400199, '65f023632bc46470c104b76f', 'hm-swim-shorts-men-40-4.jpg', 'hm-swim-shorts-men-40-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400199', 'products/65f023632bc46470c104b76f/SKU-HM-CL-SWM40/SMALL/hm-swim-shorts-men-40-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400200, '65f023632bc46470c104b76f', 'hm-swim-shorts-men-40-5.jpg', 'hm-swim-shorts-men-40-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400200', 'products/65f023632bc46470c104b76f/SKU-HM-CL-SWM40/SMALL/hm-swim-shorts-men-40-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400201, '65f023632bc46470c104b76f', 'gucci-1953-horsebit-loafer-men-41-1.jpg', 'gucci-1953-horsebit-loafer-men-41-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400201', 'products/65f023632bc46470c104b76f/SKU-GU-SH-HBL41/SMALL/gucci-1953-horsebit-loafer-men-41-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400202, '65f023632bc46470c104b76f', 'gucci-1953-horsebit-loafer-men-41-2.jpg', 'gucci-1953-horsebit-loafer-men-41-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400202', 'products/65f023632bc46470c104b76f/SKU-GU-SH-HBL41/SMALL/gucci-1953-horsebit-loafer-men-41-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400203, '65f023632bc46470c104b76f', 'gucci-1953-horsebit-loafer-men-41-3.jpg', 'gucci-1953-horsebit-loafer-men-41-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400203', 'products/65f023632bc46470c104b76f/SKU-GU-SH-HBL41/SMALL/gucci-1953-horsebit-loafer-men-41-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400204, '65f023632bc46470c104b76f', 'gucci-1953-horsebit-loafer-men-41-4.jpg', 'gucci-1953-horsebit-loafer-men-41-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400204', 'products/65f023632bc46470c104b76f/SKU-GU-SH-HBL41/SMALL/gucci-1953-horsebit-loafer-men-41-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400205, '65f023632bc46470c104b76f', 'gucci-1953-horsebit-loafer-men-41-5.jpg', 'gucci-1953-horsebit-loafer-men-41-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400205', 'products/65f023632bc46470c104b76f/SKU-GU-SH-HBL41/SMALL/gucci-1953-horsebit-loafer-men-41-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400206, '65f023632bc46470c104b76f', 'chanel-cc-stud-earrings-42-1.jpg', 'chanel-cc-stud-earrings-42-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400206', 'products/65f023632bc46470c104b76f/SKU-CH-AC-EAR42/SMALL/chanel-cc-stud-earrings-42-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400207, '65f023632bc46470c104b76f', 'chanel-cc-stud-earrings-42-2.jpg', 'chanel-cc-stud-earrings-42-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400207', 'products/65f023632bc46470c104b76f/SKU-CH-AC-EAR42/SMALL/chanel-cc-stud-earrings-42-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400208, '65f023632bc46470c104b76f', 'chanel-cc-stud-earrings-42-3.jpg', 'chanel-cc-stud-earrings-42-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400208', 'products/65f023632bc46470c104b76f/SKU-CH-AC-EAR42/SMALL/chanel-cc-stud-earrings-42-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400209, '65f023632bc46470c104b76f', 'chanel-cc-stud-earrings-42-4.jpg', 'chanel-cc-stud-earrings-42-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400209', 'products/65f023632bc46470c104b76f/SKU-CH-AC-EAR42/SMALL/chanel-cc-stud-earrings-42-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400210, '65f023632bc46470c104b76f', 'chanel-cc-stud-earrings-42-5.jpg', 'chanel-cc-stud-earrings-42-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400210', 'products/65f023632bc46470c104b76f/SKU-CH-AC-EAR42/SMALL/chanel-cc-stud-earrings-42-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400211, '65f023632bc46470c104b76f', 'nike-brasilia-duffel-bag-medium-43-1.jpg', 'nike-brasilia-duffel-bag-medium-43-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400211', 'products/65f023632bc46470c104b76f/SKU-NK-BG-DUF43/SMALL/nike-brasilia-duffel-bag-medium-43-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400212, '65f023632bc46470c104b76f', 'nike-brasilia-duffel-bag-medium-43-2.jpg', 'nike-brasilia-duffel-bag-medium-43-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400212', 'products/65f023632bc46470c104b76f/SKU-NK-BG-DUF43/SMALL/nike-brasilia-duffel-bag-medium-43-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400213, '65f023632bc46470c104b76f', 'nike-brasilia-duffel-bag-medium-43-3.jpg', 'nike-brasilia-duffel-bag-medium-43-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400213', 'products/65f023632bc46470c104b76f/SKU-NK-BG-DUF43/SMALL/nike-brasilia-duffel-bag-medium-43-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400214, '65f023632bc46470c104b76f', 'nike-brasilia-duffel-bag-medium-43-4.jpg', 'nike-brasilia-duffel-bag-medium-43-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400214', 'products/65f023632bc46470c104b76f/SKU-NK-BG-DUF43/SMALL/nike-brasilia-duffel-bag-medium-43-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400215, '65f023632bc46470c104b76f', 'nike-brasilia-duffel-bag-medium-43-5.jpg', 'nike-brasilia-duffel-bag-medium-43-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400215', 'products/65f023632bc46470c104b76f/SKU-NK-BG-DUF43/SMALL/nike-brasilia-duffel-bag-medium-43-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400216, '65f023632bc46470c104b76f', 'zara-poplin-shirt-women-44-1.jpg', 'zara-poplin-shirt-women-44-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400216', 'products/65f023632bc46470c104b76f/SKU-ZR-CL-BLS44/SMALL/zara-poplin-shirt-women-44-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400217, '65f023632bc46470c104b76f', 'zara-poplin-shirt-women-44-2.jpg', 'zara-poplin-shirt-women-44-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400217', 'products/65f023632bc46470c104b76f/SKU-ZR-CL-BLS44/SMALL/zara-poplin-shirt-women-44-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400218, '65f023632bc46470c104b76f', 'zara-poplin-shirt-women-44-3.jpg', 'zara-poplin-shirt-women-44-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400218', 'products/65f023632bc46470c104b76f/SKU-ZR-CL-BLS44/SMALL/zara-poplin-shirt-women-44-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400219, '65f023632bc46470c104b76f', 'zara-poplin-shirt-women-44-4.jpg', 'zara-poplin-shirt-women-44-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400219', 'products/65f023632bc46470c104b76f/SKU-ZR-CL-BLS44/SMALL/zara-poplin-shirt-women-44-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400220, '65f023632bc46470c104b76f', 'zara-poplin-shirt-women-44-5.jpg', 'zara-poplin-shirt-women-44-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400220', 'products/65f023632bc46470c104b76f/SKU-ZR-CL-BLS44/SMALL/zara-poplin-shirt-women-44-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400221, '65f023632bc46470c104b76f', 'adidas-adilette-comfort-slides-45-1.jpg', 'adidas-adilette-comfort-slides-45-1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400221', 'products/65f023632bc46470c104b76f/SKU-AD-SH-SLD45/SMALL/adidas-adilette-comfort-slides-45-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400222, '65f023632bc46470c104b76f', 'adidas-adilette-comfort-slides-45-2.jpg', 'adidas-adilette-comfort-slides-45-2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400222', 'products/65f023632bc46470c104b76f/SKU-AD-SH-SLD45/SMALL/adidas-adilette-comfort-slides-45-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400223, '65f023632bc46470c104b76f', 'adidas-adilette-comfort-slides-45-3.jpg', 'adidas-adilette-comfort-slides-45-3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400223', 'products/65f023632bc46470c104b76f/SKU-AD-SH-SLD45/SMALL/adidas-adilette-comfort-slides-45-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400224, '65f023632bc46470c104b76f', 'adidas-adilette-comfort-slides-45-4.jpg', 'adidas-adilette-comfort-slides-45-4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400224', 'products/65f023632bc46470c104b76f/SKU-AD-SH-SLD45/SMALL/adidas-adilette-comfort-slides-45-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-400225, '65f023632bc46470c104b76f', 'adidas-adilette-comfort-slides-45-5.jpg', 'adidas-adilette-comfort-slides-45-5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-400225', 'products/65f023632bc46470c104b76f/SKU-AD-SH-SLD45/SMALL/adidas-adilette-comfort-slides-45-5.jpg', now())
on conflict (id) do nothing;
