-- Demo product photos, registered as media assets.
--
-- The objects already exist in MinIO under the key layout catalog used when it owned files; the seed
-- registers them rather than moving bytes. Ids are negative on purpose: media asset ids come from a
-- sequence that only grows upward, so seed-only rows below zero can never collide with uploads.
-- catalog.product_image references these ids — see 16-catalog-product-image.sql in the catalog service.
-- bytes is nominal: the objects are placeholders a tester generates, so no real size exists to record,
-- and 0 made the storage figure read "no storage used" beside a library listing hundreds of files.

INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100001, '65f020632bc46470c104b76f', 'ysl-touche-eclat-illuminating-pen_1.jpg', 'ysl-touche-eclat-illuminating-pen_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100001', 'products/65f020632bc46470c104b76f/width in cm
        ''REF-YSL-MAKE-TEIPEN46''/SMALL/ysl-touche-eclat-illuminating-pen_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/width in cm
        ''REF-YSL-MAKE-TEIPEN46''/SMALL/ysl-touche-eclat-illuminating-pen_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100002, '65f020632bc46470c104b76f', 'ysl-touche-eclat-illuminating-pen_2.jpg', 'ysl-touche-eclat-illuminating-pen_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100002', 'products/65f020632bc46470c104b76f/width in cm
        ''REF-YSL-MAKE-TEIPEN46''/SMALL/ysl-touche-eclat-illuminating-pen_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/width in cm
        ''REF-YSL-MAKE-TEIPEN46''/SMALL/ysl-touche-eclat-illuminating-pen_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100003, '65f020632bc46470c104b76f', 'ysl-touche-eclat-illuminating-pen_3.jpg', 'ysl-touche-eclat-illuminating-pen_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100003', 'products/65f020632bc46470c104b76f/width in cm
        ''REF-YSL-MAKE-TEIPEN46''/SMALL/ysl-touche-eclat-illuminating-pen_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/width in cm
        ''REF-YSL-MAKE-TEIPEN46''/SMALL/ysl-touche-eclat-illuminating-pen_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100004, '65f020632bc46470c104b76f', 'ysl-touche-eclat-illuminating-pen_4.jpg', 'ysl-touche-eclat-illuminating-pen_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100004', 'products/65f020632bc46470c104b76f/width in cm
        ''REF-YSL-MAKE-TEIPEN46''/SMALL/ysl-touche-eclat-illuminating-pen_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/width in cm
        ''REF-YSL-MAKE-TEIPEN46''/SMALL/ysl-touche-eclat-illuminating-pen_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100005, '65f020632bc46470c104b76f', 'ysl-touche-eclat-illuminating-pen_5.jpg', 'ysl-touche-eclat-illuminating-pen_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100005', 'products/65f020632bc46470c104b76f/width in cm
        ''REF-YSL-MAKE-TEIPEN46''/SMALL/ysl-touche-eclat-illuminating-pen_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/width in cm
        ''REF-YSL-MAKE-TEIPEN46''/SMALL/ysl-touche-eclat-illuminating-pen_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100006, '65f020632bc46470c104b76f', 'guerlain-abeille-royale-advanced-youth-watery-oil_1.jpg', 'guerlain-abeille-royale-advanced-youth-watery-oil_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100006', 'products/65f020632bc46470c104b76f/SKU-GUER-SKIN-ARWOIL47/SMALL/guerlain-abeille-royale-advanced-youth-watery-oil_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-SKIN-ARWOIL47/SMALL/guerlain-abeille-royale-advanced-youth-watery-oil_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100007, '65f020632bc46470c104b76f', 'guerlain-abeille-royale-advanced-youth-watery-oil_2.jpg', 'guerlain-abeille-royale-advanced-youth-watery-oil_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100007', 'products/65f020632bc46470c104b76f/SKU-GUER-SKIN-ARWOIL47/SMALL/guerlain-abeille-royale-advanced-youth-watery-oil_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-SKIN-ARWOIL47/SMALL/guerlain-abeille-royale-advanced-youth-watery-oil_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100008, '65f020632bc46470c104b76f', 'guerlain-abeille-royale-advanced-youth-watery-oil_3.jpg', 'guerlain-abeille-royale-advanced-youth-watery-oil_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100008', 'products/65f020632bc46470c104b76f/SKU-GUER-SKIN-ARWOIL47/SMALL/guerlain-abeille-royale-advanced-youth-watery-oil_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-SKIN-ARWOIL47/SMALL/guerlain-abeille-royale-advanced-youth-watery-oil_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100009, '65f020632bc46470c104b76f', 'guerlain-abeille-royale-advanced-youth-watery-oil_4.jpg', 'guerlain-abeille-royale-advanced-youth-watery-oil_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100009', 'products/65f020632bc46470c104b76f/SKU-GUER-SKIN-ARWOIL47/SMALL/guerlain-abeille-royale-advanced-youth-watery-oil_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-SKIN-ARWOIL47/SMALL/guerlain-abeille-royale-advanced-youth-watery-oil_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100010, '65f020632bc46470c104b76f', 'guerlain-abeille-royale-advanced-youth-watery-oil_5.jpg', 'guerlain-abeille-royale-advanced-youth-watery-oil_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100010', 'products/65f020632bc46470c104b76f/SKU-GUER-SKIN-ARWOIL47/SMALL/guerlain-abeille-royale-advanced-youth-watery-oil_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-SKIN-ARWOIL47/SMALL/guerlain-abeille-royale-advanced-youth-watery-oil_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100011, '65f020632bc46470c104b76f', 'shiseido-ultimune-power-infusing-concentrate_1.jpg', 'shiseido-ultimune-power-infusing-concentrate_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100011', 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-ULTIMUNE48/SMALL/shiseido-ultimune-power-infusing-concentrate_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-ULTIMUNE48/SMALL/shiseido-ultimune-power-infusing-concentrate_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100012, '65f020632bc46470c104b76f', 'shiseido-ultimune-power-infusing-concentrate_2.jpg', 'shiseido-ultimune-power-infusing-concentrate_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100012', 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-ULTIMUNE48/SMALL/shiseido-ultimune-power-infusing-concentrate_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-ULTIMUNE48/SMALL/shiseido-ultimune-power-infusing-concentrate_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100013, '65f020632bc46470c104b76f', 'shiseido-ultimune-power-infusing-concentrate_3.jpg', 'shiseido-ultimune-power-infusing-concentrate_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100013', 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-ULTIMUNE48/SMALL/shiseido-ultimune-power-infusing-concentrate_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-ULTIMUNE48/SMALL/shiseido-ultimune-power-infusing-concentrate_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100014, '65f020632bc46470c104b76f', 'shiseido-ultimune-power-infusing-concentrate_4.jpg', 'shiseido-ultimune-power-infusing-concentrate_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100014', 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-ULTIMUNE48/SMALL/shiseido-ultimune-power-infusing-concentrate_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-ULTIMUNE48/SMALL/shiseido-ultimune-power-infusing-concentrate_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100015, '65f020632bc46470c104b76f', 'shiseido-ultimune-power-infusing-concentrate_5.jpg', 'shiseido-ultimune-power-infusing-concentrate_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100015', 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-ULTIMUNE48/SMALL/shiseido-ultimune-power-infusing-concentrate_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-ULTIMUNE48/SMALL/shiseido-ultimune-power-infusing-concentrate_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100016, '65f020632bc46470c104b76f', 'nars-radiant-creamy-concealer_1.jpg', 'nars-radiant-creamy-concealer_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100016', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-RCCONC49/SMALL/nars-radiant-creamy-concealer_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-RCCONC49/SMALL/nars-radiant-creamy-concealer_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100017, '65f020632bc46470c104b76f', 'nars-radiant-creamy-concealer_2.jpg', 'nars-radiant-creamy-concealer_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100017', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-RCCONC49/SMALL/nars-radiant-creamy-concealer_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-RCCONC49/SMALL/nars-radiant-creamy-concealer_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100018, '65f020632bc46470c104b76f', 'nars-radiant-creamy-concealer_3.jpg', 'nars-radiant-creamy-concealer_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100018', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-RCCONC49/SMALL/nars-radiant-creamy-concealer_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-RCCONC49/SMALL/nars-radiant-creamy-concealer_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100019, '65f020632bc46470c104b76f', 'nars-radiant-creamy-concealer_4.jpg', 'nars-radiant-creamy-concealer_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100019', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-RCCONC49/SMALL/nars-radiant-creamy-concealer_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-RCCONC49/SMALL/nars-radiant-creamy-concealer_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100020, '65f020632bc46470c104b76f', 'nars-radiant-creamy-concealer_5.jpg', 'nars-radiant-creamy-concealer_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100020', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-RCCONC49/SMALL/nars-radiant-creamy-concealer_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-RCCONC49/SMALL/nars-radiant-creamy-concealer_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100021, '65f020632bc46470c104b76f', 'la-roche-posay-anthelios-uvmune-400-spf50_1.jpg', 'la-roche-posay-anthelios-uvmune-400-spf50_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100021', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-ANTHUV50/SMALL/la-roche-posay-anthelios-uvmune-400-spf50_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-ANTHUV50/SMALL/la-roche-posay-anthelios-uvmune-400-spf50_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100022, '65f020632bc46470c104b76f', 'la-roche-posay-anthelios-uvmune-400-spf50_2.jpg', 'la-roche-posay-anthelios-uvmune-400-spf50_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100022', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-ANTHUV50/SMALL/la-roche-posay-anthelios-uvmune-400-spf50_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-ANTHUV50/SMALL/la-roche-posay-anthelios-uvmune-400-spf50_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100023, '65f020632bc46470c104b76f', 'la-roche-posay-anthelios-uvmune-400-spf50_3.jpg', 'la-roche-posay-anthelios-uvmune-400-spf50_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100023', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-ANTHUV50/SMALL/la-roche-posay-anthelios-uvmune-400-spf50_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-ANTHUV50/SMALL/la-roche-posay-anthelios-uvmune-400-spf50_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100024, '65f020632bc46470c104b76f', 'la-roche-posay-anthelios-uvmune-400-spf50_4.jpg', 'la-roche-posay-anthelios-uvmune-400-spf50_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100024', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-ANTHUV50/SMALL/la-roche-posay-anthelios-uvmune-400-spf50_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-ANTHUV50/SMALL/la-roche-posay-anthelios-uvmune-400-spf50_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100025, '65f020632bc46470c104b76f', 'la-roche-posay-anthelios-uvmune-400-spf50_5.jpg', 'la-roche-posay-anthelios-uvmune-400-spf50_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100025', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-ANTHUV50/SMALL/la-roche-posay-anthelios-uvmune-400-spf50_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-ANTHUV50/SMALL/la-roche-posay-anthelios-uvmune-400-spf50_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100026, '65f020632bc46470c104b76f', 'kerastase-genesis-bain-hydra-fortifiant-shampoo_1.jpg', 'kerastase-genesis-bain-hydra-fortifiant-shampoo_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100026', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-GENSHMP51/SMALL/kerastase-genesis-bain-hydra-fortifiant-shampoo_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-GENSHMP51/SMALL/kerastase-genesis-bain-hydra-fortifiant-shampoo_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100027, '65f020632bc46470c104b76f', 'kerastase-genesis-bain-hydra-fortifiant-shampoo_2.jpg', 'kerastase-genesis-bain-hydra-fortifiant-shampoo_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100027', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-GENSHMP51/SMALL/kerastase-genesis-bain-hydra-fortifiant-shampoo_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-GENSHMP51/SMALL/kerastase-genesis-bain-hydra-fortifiant-shampoo_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100028, '65f020632bc46470c104b76f', 'kerastase-genesis-bain-hydra-fortifiant-shampoo_3.jpg', 'kerastase-genesis-bain-hydra-fortifiant-shampoo_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100028', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-GENSHMP51/SMALL/kerastase-genesis-bain-hydra-fortifiant-shampoo_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-GENSHMP51/SMALL/kerastase-genesis-bain-hydra-fortifiant-shampoo_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100029, '65f020632bc46470c104b76f', 'kerastase-genesis-bain-hydra-fortifiant-shampoo_4.jpg', 'kerastase-genesis-bain-hydra-fortifiant-shampoo_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100029', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-GENSHMP51/SMALL/kerastase-genesis-bain-hydra-fortifiant-shampoo_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-GENSHMP51/SMALL/kerastase-genesis-bain-hydra-fortifiant-shampoo_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100030, '65f020632bc46470c104b76f', 'kerastase-genesis-bain-hydra-fortifiant-shampoo_5.jpg', 'kerastase-genesis-bain-hydra-fortifiant-shampoo_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100030', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-GENSHMP51/SMALL/kerastase-genesis-bain-hydra-fortifiant-shampoo_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-GENSHMP51/SMALL/kerastase-genesis-bain-hydra-fortifiant-shampoo_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100031, '65f020632bc46470c104b76f', 'ysl-black-opium-eau-de-parfum_1.jpg', 'ysl-black-opium-eau-de-parfum_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100031', 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-BLACKOPIUM52/SMALL/ysl-black-opium-eau-de-parfum_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-FRAG-BLACKOPIUM52/SMALL/ysl-black-opium-eau-de-parfum_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100032, '65f020632bc46470c104b76f', 'ysl-black-opium-eau-de-parfum_2.jpg', 'ysl-black-opium-eau-de-parfum_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100032', 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-BLACKOPIUM52/SMALL/ysl-black-opium-eau-de-parfum_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-FRAG-BLACKOPIUM52/SMALL/ysl-black-opium-eau-de-parfum_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100033, '65f020632bc46470c104b76f', 'ysl-black-opium-eau-de-parfum_3.jpg', 'ysl-black-opium-eau-de-parfum_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100033', 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-BLACKOPIUM52/SMALL/ysl-black-opium-eau-de-parfum_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-FRAG-BLACKOPIUM52/SMALL/ysl-black-opium-eau-de-parfum_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100034, '65f020632bc46470c104b76f', 'ysl-black-opium-eau-de-parfum_4.jpg', 'ysl-black-opium-eau-de-parfum_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100034', 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-BLACKOPIUM52/SMALL/ysl-black-opium-eau-de-parfum_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-FRAG-BLACKOPIUM52/SMALL/ysl-black-opium-eau-de-parfum_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100035, '65f020632bc46470c104b76f', 'ysl-black-opium-eau-de-parfum_5.jpg', 'ysl-black-opium-eau-de-parfum_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100035', 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-BLACKOPIUM52/SMALL/ysl-black-opium-eau-de-parfum_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-FRAG-BLACKOPIUM52/SMALL/ysl-black-opium-eau-de-parfum_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100036, '65f020632bc46470c104b76f', 'guerlain-terracotta-bronzing-powder_1.jpg', 'guerlain-terracotta-bronzing-powder_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100036', 'products/65f020632bc46470c104b76f/SKU-GUER-MAKE-TERRACOTTA53/SMALL/guerlain-terracotta-bronzing-powder_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-MAKE-TERRACOTTA53/SMALL/guerlain-terracotta-bronzing-powder_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100037, '65f020632bc46470c104b76f', 'guerlain-terracotta-bronzing-powder_2.jpg', 'guerlain-terracotta-bronzing-powder_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100037', 'products/65f020632bc46470c104b76f/SKU-GUER-MAKE-TERRACOTTA53/SMALL/guerlain-terracotta-bronzing-powder_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-MAKE-TERRACOTTA53/SMALL/guerlain-terracotta-bronzing-powder_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100038, '65f020632bc46470c104b76f', 'guerlain-terracotta-bronzing-powder_3.jpg', 'guerlain-terracotta-bronzing-powder_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100038', 'products/65f020632bc46470c104b76f/SKU-GUER-MAKE-TERRACOTTA53/SMALL/guerlain-terracotta-bronzing-powder_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-MAKE-TERRACOTTA53/SMALL/guerlain-terracotta-bronzing-powder_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100039, '65f020632bc46470c104b76f', 'guerlain-terracotta-bronzing-powder_4.jpg', 'guerlain-terracotta-bronzing-powder_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100039', 'products/65f020632bc46470c104b76f/SKU-GUER-MAKE-TERRACOTTA53/SMALL/guerlain-terracotta-bronzing-powder_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-MAKE-TERRACOTTA53/SMALL/guerlain-terracotta-bronzing-powder_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100040, '65f020632bc46470c104b76f', 'guerlain-terracotta-bronzing-powder_5.jpg', 'guerlain-terracotta-bronzing-powder_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100040', 'products/65f020632bc46470c104b76f/SKU-GUER-MAKE-TERRACOTTA53/SMALL/guerlain-terracotta-bronzing-powder_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-MAKE-TERRACOTTA53/SMALL/guerlain-terracotta-bronzing-powder_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100041, '65f020632bc46470c104b76f', 'shiseido-benefiance-wrinkle-smoothing-cream_1.jpg', 'shiseido-benefiance-wrinkle-smoothing-cream_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100041', 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-BENEFCR54/SMALL/shiseido-benefiance-wrinkle-smoothing-cream_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-BENEFCR54/SMALL/shiseido-benefiance-wrinkle-smoothing-cream_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100042, '65f020632bc46470c104b76f', 'shiseido-benefiance-wrinkle-smoothing-cream_2.jpg', 'shiseido-benefiance-wrinkle-smoothing-cream_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100042', 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-BENEFCR54/SMALL/shiseido-benefiance-wrinkle-smoothing-cream_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-BENEFCR54/SMALL/shiseido-benefiance-wrinkle-smoothing-cream_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100043, '65f020632bc46470c104b76f', 'shiseido-benefiance-wrinkle-smoothing-cream_3.jpg', 'shiseido-benefiance-wrinkle-smoothing-cream_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100043', 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-BENEFCR54/SMALL/shiseido-benefiance-wrinkle-smoothing-cream_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-BENEFCR54/SMALL/shiseido-benefiance-wrinkle-smoothing-cream_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100044, '65f020632bc46470c104b76f', 'shiseido-benefiance-wrinkle-smoothing-cream_4.jpg', 'shiseido-benefiance-wrinkle-smoothing-cream_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100044', 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-BENEFCR54/SMALL/shiseido-benefiance-wrinkle-smoothing-cream_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-BENEFCR54/SMALL/shiseido-benefiance-wrinkle-smoothing-cream_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100045, '65f020632bc46470c104b76f', 'shiseido-benefiance-wrinkle-smoothing-cream_5.jpg', 'shiseido-benefiance-wrinkle-smoothing-cream_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100045', 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-BENEFCR54/SMALL/shiseido-benefiance-wrinkle-smoothing-cream_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-BENEFCR54/SMALL/shiseido-benefiance-wrinkle-smoothing-cream_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100046, '65f020632bc46470c104b76f', 'nars-orgasm-blush_1.jpg', 'nars-orgasm-blush_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100046', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-ORGBLUSH55/SMALL/nars-orgasm-blush_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-ORGBLUSH55/SMALL/nars-orgasm-blush_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100047, '65f020632bc46470c104b76f', 'nars-orgasm-blush_2.jpg', 'nars-orgasm-blush_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100047', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-ORGBLUSH55/SMALL/nars-orgasm-blush_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-ORGBLUSH55/SMALL/nars-orgasm-blush_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100048, '65f020632bc46470c104b76f', 'nars-orgasm-blush_3.jpg', 'nars-orgasm-blush_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100048', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-ORGBLUSH55/SMALL/nars-orgasm-blush_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-ORGBLUSH55/SMALL/nars-orgasm-blush_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100049, '65f020632bc46470c104b76f', 'nars-orgasm-blush_4.jpg', 'nars-orgasm-blush_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100049', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-ORGBLUSH55/SMALL/nars-orgasm-blush_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-ORGBLUSH55/SMALL/nars-orgasm-blush_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100050, '65f020632bc46470c104b76f', 'nars-orgasm-blush_5.jpg', 'nars-orgasm-blush_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100050', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-ORGBLUSH55/SMALL/nars-orgasm-blush_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-ORGBLUSH55/SMALL/nars-orgasm-blush_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100051, '65f020632bc46470c104b76f', 'la-roche-posay-cicaplast-baume-b5_1.jpg', 'la-roche-posay-cicaplast-baume-b5_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100051', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-CICAB5-56/SMALL/la-roche-posay-cicaplast-baume-b5_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-CICAB5-56/SMALL/la-roche-posay-cicaplast-baume-b5_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100052, '65f020632bc46470c104b76f', 'la-roche-posay-cicaplast-baume-b5_2.jpg', 'la-roche-posay-cicaplast-baume-b5_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100052', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-CICAB5-56/SMALL/la-roche-posay-cicaplast-baume-b5_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-CICAB5-56/SMALL/la-roche-posay-cicaplast-baume-b5_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100053, '65f020632bc46470c104b76f', 'la-roche-posay-cicaplast-baume-b5_3.jpg', 'la-roche-posay-cicaplast-baume-b5_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100053', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-CICAB5-56/SMALL/la-roche-posay-cicaplast-baume-b5_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-CICAB5-56/SMALL/la-roche-posay-cicaplast-baume-b5_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100054, '65f020632bc46470c104b76f', 'la-roche-posay-cicaplast-baume-b5_4.jpg', 'la-roche-posay-cicaplast-baume-b5_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100054', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-CICAB5-56/SMALL/la-roche-posay-cicaplast-baume-b5_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-CICAB5-56/SMALL/la-roche-posay-cicaplast-baume-b5_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100055, '65f020632bc46470c104b76f', 'la-roche-posay-cicaplast-baume-b5_5.jpg', 'la-roche-posay-cicaplast-baume-b5_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100055', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-CICAB5-56/SMALL/la-roche-posay-cicaplast-baume-b5_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-CICAB5-56/SMALL/la-roche-posay-cicaplast-baume-b5_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100056, '65f020632bc46470c104b76f', 'kerastase-elixir-ultime-original-hair-oil_1.jpg', 'kerastase-elixir-ultime-original-hair-oil_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100056', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-ELIXIR57/SMALL/kerastase-elixir-ultime-original-hair-oil_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-ELIXIR57/SMALL/kerastase-elixir-ultime-original-hair-oil_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100057, '65f020632bc46470c104b76f', 'kerastase-elixir-ultime-original-hair-oil_2.jpg', 'kerastase-elixir-ultime-original-hair-oil_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100057', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-ELIXIR57/SMALL/kerastase-elixir-ultime-original-hair-oil_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-ELIXIR57/SMALL/kerastase-elixir-ultime-original-hair-oil_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100058, '65f020632bc46470c104b76f', 'kerastase-elixir-ultime-original-hair-oil_3.jpg', 'kerastase-elixir-ultime-original-hair-oil_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100058', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-ELIXIR57/SMALL/kerastase-elixir-ultime-original-hair-oil_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-ELIXIR57/SMALL/kerastase-elixir-ultime-original-hair-oil_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100059, '65f020632bc46470c104b76f', 'kerastase-elixir-ultime-original-hair-oil_4.jpg', 'kerastase-elixir-ultime-original-hair-oil_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100059', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-ELIXIR57/SMALL/kerastase-elixir-ultime-original-hair-oil_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-ELIXIR57/SMALL/kerastase-elixir-ultime-original-hair-oil_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100060, '65f020632bc46470c104b76f', 'kerastase-elixir-ultime-original-hair-oil_5.jpg', 'kerastase-elixir-ultime-original-hair-oil_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100060', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-ELIXIR57/SMALL/kerastase-elixir-ultime-original-hair-oil_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-ELIXIR57/SMALL/kerastase-elixir-ultime-original-hair-oil_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100061, '65f020632bc46470c104b76f', 'ysl-libre-eau-de-parfum_1.jpg', 'ysl-libre-eau-de-parfum_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100061', 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-LIBREEDP58/SMALL/ysl-libre-eau-de-parfum_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-FRAG-LIBREEDP58/SMALL/ysl-libre-eau-de-parfum_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100062, '65f020632bc46470c104b76f', 'ysl-libre-eau-de-parfum_2.jpg', 'ysl-libre-eau-de-parfum_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100062', 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-LIBREEDP58/SMALL/ysl-libre-eau-de-parfum_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-FRAG-LIBREEDP58/SMALL/ysl-libre-eau-de-parfum_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100063, '65f020632bc46470c104b76f', 'ysl-libre-eau-de-parfum_3.jpg', 'ysl-libre-eau-de-parfum_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100063', 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-LIBREEDP58/SMALL/ysl-libre-eau-de-parfum_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-FRAG-LIBREEDP58/SMALL/ysl-libre-eau-de-parfum_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100064, '65f020632bc46470c104b76f', 'ysl-libre-eau-de-parfum_4.jpg', 'ysl-libre-eau-de-parfum_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100064', 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-LIBREEDP58/SMALL/ysl-libre-eau-de-parfum_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-FRAG-LIBREEDP58/SMALL/ysl-libre-eau-de-parfum_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100065, '65f020632bc46470c104b76f', 'ysl-libre-eau-de-parfum_5.jpg', 'ysl-libre-eau-de-parfum_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100065', 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-LIBREEDP58/SMALL/ysl-libre-eau-de-parfum_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-FRAG-LIBREEDP58/SMALL/ysl-libre-eau-de-parfum_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100066, '65f020632bc46470c104b76f', 'guerlain-meteorites-pearls_1.jpg', 'guerlain-meteorites-pearls_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100066', 'products/65f020632bc46470c104b76f/SKU-GUER-MAKE-METEORITES59/SMALL/guerlain-meteorites-pearls_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-MAKE-METEORITES59/SMALL/guerlain-meteorites-pearls_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100067, '65f020632bc46470c104b76f', 'guerlain-meteorites-pearls_2.jpg', 'guerlain-meteorites-pearls_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100067', 'products/65f020632bc46470c104b76f/SKU-GUER-MAKE-METEORITES59/SMALL/guerlain-meteorites-pearls_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-MAKE-METEORITES59/SMALL/guerlain-meteorites-pearls_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100068, '65f020632bc46470c104b76f', 'guerlain-meteorites-pearls_3.jpg', 'guerlain-meteorites-pearls_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100068', 'products/65f020632bc46470c104b76f/SKU-GUER-MAKE-METEORITES59/SMALL/guerlain-meteorites-pearls_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-MAKE-METEORITES59/SMALL/guerlain-meteorites-pearls_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100069, '65f020632bc46470c104b76f', 'guerlain-meteorites-pearls_4.jpg', 'guerlain-meteorites-pearls_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100069', 'products/65f020632bc46470c104b76f/SKU-GUER-MAKE-METEORITES59/SMALL/guerlain-meteorites-pearls_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-MAKE-METEORITES59/SMALL/guerlain-meteorites-pearls_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100070, '65f020632bc46470c104b76f', 'guerlain-meteorites-pearls_5.jpg', 'guerlain-meteorites-pearls_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100070', 'products/65f020632bc46470c104b76f/SKU-GUER-MAKE-METEORITES59/SMALL/guerlain-meteorites-pearls_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-MAKE-METEORITES59/SMALL/guerlain-meteorites-pearls_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100071, '65f020632bc46470c104b76f', 'shiseido-synchro-skin-self-refreshing-foundation_1.jpg', 'shiseido-synchro-skin-self-refreshing-foundation_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100071', 'products/65f020632bc46470c104b76f/SKU-SHIS-MAKE-SYNCSKIN60/SMALL/shiseido-synchro-skin-self-refreshing-foundation_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-MAKE-SYNCSKIN60/SMALL/shiseido-synchro-skin-self-refreshing-foundation_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100072, '65f020632bc46470c104b76f', 'shiseido-synchro-skin-self-refreshing-foundation_2.jpg', 'shiseido-synchro-skin-self-refreshing-foundation_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100072', 'products/65f020632bc46470c104b76f/SKU-SHIS-MAKE-SYNCSKIN60/SMALL/shiseido-synchro-skin-self-refreshing-foundation_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-MAKE-SYNCSKIN60/SMALL/shiseido-synchro-skin-self-refreshing-foundation_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100073, '65f020632bc46470c104b76f', 'shiseido-synchro-skin-self-refreshing-foundation_3.jpg', 'shiseido-synchro-skin-self-refreshing-foundation_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100073', 'products/65f020632bc46470c104b76f/SKU-SHIS-MAKE-SYNCSKIN60/SMALL/shiseido-synchro-skin-self-refreshing-foundation_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-MAKE-SYNCSKIN60/SMALL/shiseido-synchro-skin-self-refreshing-foundation_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100074, '65f020632bc46470c104b76f', 'shiseido-synchro-skin-self-refreshing-foundation_4.jpg', 'shiseido-synchro-skin-self-refreshing-foundation_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100074', 'products/65f020632bc46470c104b76f/SKU-SHIS-MAKE-SYNCSKIN60/SMALL/shiseido-synchro-skin-self-refreshing-foundation_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-MAKE-SYNCSKIN60/SMALL/shiseido-synchro-skin-self-refreshing-foundation_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100075, '65f020632bc46470c104b76f', 'shiseido-synchro-skin-self-refreshing-foundation_5.jpg', 'shiseido-synchro-skin-self-refreshing-foundation_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100075', 'products/65f020632bc46470c104b76f/SKU-SHIS-MAKE-SYNCSKIN60/SMALL/shiseido-synchro-skin-self-refreshing-foundation_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-MAKE-SYNCSKIN60/SMALL/shiseido-synchro-skin-self-refreshing-foundation_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100076, '65f020632bc46470c104b76f', 'nars-laguna-bronzing-powder_1.jpg', 'nars-laguna-bronzing-powder_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100076', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-LAGUNABRZ61/SMALL/nars-laguna-bronzing-powder_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-LAGUNABRZ61/SMALL/nars-laguna-bronzing-powder_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100077, '65f020632bc46470c104b76f', 'nars-laguna-bronzing-powder_2.jpg', 'nars-laguna-bronzing-powder_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100077', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-LAGUNABRZ61/SMALL/nars-laguna-bronzing-powder_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-LAGUNABRZ61/SMALL/nars-laguna-bronzing-powder_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100078, '65f020632bc46470c104b76f', 'nars-laguna-bronzing-powder_3.jpg', 'nars-laguna-bronzing-powder_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100078', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-LAGUNABRZ61/SMALL/nars-laguna-bronzing-powder_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-LAGUNABRZ61/SMALL/nars-laguna-bronzing-powder_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100079, '65f020632bc46470c104b76f', 'nars-laguna-bronzing-powder_4.jpg', 'nars-laguna-bronzing-powder_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100079', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-LAGUNABRZ61/SMALL/nars-laguna-bronzing-powder_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-LAGUNABRZ61/SMALL/nars-laguna-bronzing-powder_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100080, '65f020632bc46470c104b76f', 'nars-laguna-bronzing-powder_5.jpg', 'nars-laguna-bronzing-powder_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100080', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-LAGUNABRZ61/SMALL/nars-laguna-bronzing-powder_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-LAGUNABRZ61/SMALL/nars-laguna-bronzing-powder_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100081, '65f020632bc46470c104b76f', 'la-roche-posay-hyalu-b5-serum_1.jpg', 'la-roche-posay-hyalu-b5-serum_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100081', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-HYALUB5-62/SMALL/la-roche-posay-hyalu-b5-serum_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-HYALUB5-62/SMALL/la-roche-posay-hyalu-b5-serum_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100082, '65f020632bc46470c104b76f', 'la-roche-posay-hyalu-b5-serum_2.jpg', 'la-roche-posay-hyalu-b5-serum_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100082', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-HYALUB5-62/SMALL/la-roche-posay-hyalu-b5-serum_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-HYALUB5-62/SMALL/la-roche-posay-hyalu-b5-serum_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100083, '65f020632bc46470c104b76f', 'la-roche-posay-hyalu-b5-serum_3.jpg', 'la-roche-posay-hyalu-b5-serum_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100083', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-HYALUB5-62/SMALL/la-roche-posay-hyalu-b5-serum_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-HYALUB5-62/SMALL/la-roche-posay-hyalu-b5-serum_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100084, '65f020632bc46470c104b76f', 'la-roche-posay-hyalu-b5-serum_4.jpg', 'la-roche-posay-hyalu-b5-serum_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100084', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-HYALUB5-62/SMALL/la-roche-posay-hyalu-b5-serum_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-HYALUB5-62/SMALL/la-roche-posay-hyalu-b5-serum_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100085, '65f020632bc46470c104b76f', 'la-roche-posay-hyalu-b5-serum_5.jpg', 'la-roche-posay-hyalu-b5-serum_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100085', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-HYALUB5-62/SMALL/la-roche-posay-hyalu-b5-serum_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-HYALUB5-62/SMALL/la-roche-posay-hyalu-b5-serum_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100086, '65f020632bc46470c104b76f', 'kerastase-blond-absolu-cicaflash-conditioner_1.jpg', 'kerastase-blond-absolu-cicaflash-conditioner_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100086', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-BACICAFLASH63/SMALL/kerastase-blond-absolu-cicaflash-conditioner_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-BACICAFLASH63/SMALL/kerastase-blond-absolu-cicaflash-conditioner_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100087, '65f020632bc46470c104b76f', 'kerastase-blond-absolu-cicaflash-conditioner_2.jpg', 'kerastase-blond-absolu-cicaflash-conditioner_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100087', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-BACICAFLASH63/SMALL/kerastase-blond-absolu-cicaflash-conditioner_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-BACICAFLASH63/SMALL/kerastase-blond-absolu-cicaflash-conditioner_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100088, '65f020632bc46470c104b76f', 'kerastase-blond-absolu-cicaflash-conditioner_3.jpg', 'kerastase-blond-absolu-cicaflash-conditioner_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100088', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-BACICAFLASH63/SMALL/kerastase-blond-absolu-cicaflash-conditioner_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-BACICAFLASH63/SMALL/kerastase-blond-absolu-cicaflash-conditioner_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100089, '65f020632bc46470c104b76f', 'kerastase-blond-absolu-cicaflash-conditioner_4.jpg', 'kerastase-blond-absolu-cicaflash-conditioner_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100089', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-BACICAFLASH63/SMALL/kerastase-blond-absolu-cicaflash-conditioner_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-BACICAFLASH63/SMALL/kerastase-blond-absolu-cicaflash-conditioner_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100090, '65f020632bc46470c104b76f', 'kerastase-blond-absolu-cicaflash-conditioner_5.jpg', 'kerastase-blond-absolu-cicaflash-conditioner_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100090', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-BACICAFLASH63/SMALL/kerastase-blond-absolu-cicaflash-conditioner_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-BACICAFLASH63/SMALL/kerastase-blond-absolu-cicaflash-conditioner_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100091, '65f020632bc46470c104b76f', 'ysl-rouge-pur-couture-lipstick_1.jpg', 'ysl-rouge-pur-couture-lipstick_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100091', 'products/65f020632bc46470c104b76f/SKU-YSL-MAKE-RPCLIP64/SMALL/ysl-rouge-pur-couture-lipstick_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-MAKE-RPCLIP64/SMALL/ysl-rouge-pur-couture-lipstick_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100092, '65f020632bc46470c104b76f', 'ysl-rouge-pur-couture-lipstick_2.jpg', 'ysl-rouge-pur-couture-lipstick_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100092', 'products/65f020632bc46470c104b76f/SKU-YSL-MAKE-RPCLIP64/SMALL/ysl-rouge-pur-couture-lipstick_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-MAKE-RPCLIP64/SMALL/ysl-rouge-pur-couture-lipstick_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100093, '65f020632bc46470c104b76f', 'ysl-rouge-pur-couture-lipstick_3.jpg', 'ysl-rouge-pur-couture-lipstick_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100093', 'products/65f020632bc46470c104b76f/SKU-YSL-MAKE-RPCLIP64/SMALL/ysl-rouge-pur-couture-lipstick_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-MAKE-RPCLIP64/SMALL/ysl-rouge-pur-couture-lipstick_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100094, '65f020632bc46470c104b76f', 'ysl-rouge-pur-couture-lipstick_4.jpg', 'ysl-rouge-pur-couture-lipstick_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100094', 'products/65f020632bc46470c104b76f/SKU-YSL-MAKE-RPCLIP64/SMALL/ysl-rouge-pur-couture-lipstick_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-MAKE-RPCLIP64/SMALL/ysl-rouge-pur-couture-lipstick_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100095, '65f020632bc46470c104b76f', 'ysl-rouge-pur-couture-lipstick_5.jpg', 'ysl-rouge-pur-couture-lipstick_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100095', 'products/65f020632bc46470c104b76f/SKU-YSL-MAKE-RPCLIP64/SMALL/ysl-rouge-pur-couture-lipstick_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-MAKE-RPCLIP64/SMALL/ysl-rouge-pur-couture-lipstick_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100096, '65f020632bc46470c104b76f', 'guerlain-mon-guerlain-eau-de-parfum_1.jpg', 'guerlain-mon-guerlain-eau-de-parfum_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100096', 'products/65f020632bc46470c104b76f/SKU-GUER-FRAG-MONGUERLAIN65/SMALL/guerlain-mon-guerlain-eau-de-parfum_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-FRAG-MONGUERLAIN65/SMALL/guerlain-mon-guerlain-eau-de-parfum_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100097, '65f020632bc46470c104b76f', 'guerlain-mon-guerlain-eau-de-parfum_2.jpg', 'guerlain-mon-guerlain-eau-de-parfum_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100097', 'products/65f020632bc46470c104b76f/SKU-GUER-FRAG-MONGUERLAIN65/SMALL/guerlain-mon-guerlain-eau-de-parfum_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-FRAG-MONGUERLAIN65/SMALL/guerlain-mon-guerlain-eau-de-parfum_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100098, '65f020632bc46470c104b76f', 'guerlain-mon-guerlain-eau-de-parfum_3.jpg', 'guerlain-mon-guerlain-eau-de-parfum_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100098', 'products/65f020632bc46470c104b76f/SKU-GUER-FRAG-MONGUERLAIN65/SMALL/guerlain-mon-guerlain-eau-de-parfum_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-FRAG-MONGUERLAIN65/SMALL/guerlain-mon-guerlain-eau-de-parfum_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100099, '65f020632bc46470c104b76f', 'guerlain-mon-guerlain-eau-de-parfum_4.jpg', 'guerlain-mon-guerlain-eau-de-parfum_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100099', 'products/65f020632bc46470c104b76f/SKU-GUER-FRAG-MONGUERLAIN65/SMALL/guerlain-mon-guerlain-eau-de-parfum_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-FRAG-MONGUERLAIN65/SMALL/guerlain-mon-guerlain-eau-de-parfum_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100100, '65f020632bc46470c104b76f', 'guerlain-mon-guerlain-eau-de-parfum_5.jpg', 'guerlain-mon-guerlain-eau-de-parfum_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100100', 'products/65f020632bc46470c104b76f/SKU-GUER-FRAG-MONGUERLAIN65/SMALL/guerlain-mon-guerlain-eau-de-parfum_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-FRAG-MONGUERLAIN65/SMALL/guerlain-mon-guerlain-eau-de-parfum_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100101, '65f020632bc46470c104b76f', 'shiseido-vital-perfection-uplifting-firming-cream_1.jpg', 'shiseido-vital-perfection-uplifting-firming-cream_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100101', 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-VPUFCR66/SMALL/shiseido-vital-perfection-uplifting-firming-cream_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-VPUFCR66/SMALL/shiseido-vital-perfection-uplifting-firming-cream_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100102, '65f020632bc46470c104b76f', 'shiseido-vital-perfection-uplifting-firming-cream_2.jpg', 'shiseido-vital-perfection-uplifting-firming-cream_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100102', 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-VPUFCR66/SMALL/shiseido-vital-perfection-uplifting-firming-cream_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-VPUFCR66/SMALL/shiseido-vital-perfection-uplifting-firming-cream_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100103, '65f020632bc46470c104b76f', 'shiseido-vital-perfection-uplifting-firming-cream_3.jpg', 'shiseido-vital-perfection-uplifting-firming-cream_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100103', 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-VPUFCR66/SMALL/shiseido-vital-perfection-uplifting-firming-cream_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-VPUFCR66/SMALL/shiseido-vital-perfection-uplifting-firming-cream_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100104, '65f020632bc46470c104b76f', 'shiseido-vital-perfection-uplifting-firming-cream_4.jpg', 'shiseido-vital-perfection-uplifting-firming-cream_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100104', 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-VPUFCR66/SMALL/shiseido-vital-perfection-uplifting-firming-cream_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-VPUFCR66/SMALL/shiseido-vital-perfection-uplifting-firming-cream_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100105, '65f020632bc46470c104b76f', 'shiseido-vital-perfection-uplifting-firming-cream_5.jpg', 'shiseido-vital-perfection-uplifting-firming-cream_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100105', 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-VPUFCR66/SMALL/shiseido-vital-perfection-uplifting-firming-cream_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-VPUFCR66/SMALL/shiseido-vital-perfection-uplifting-firming-cream_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100106, '65f020632bc46470c104b76f', 'nars-sheer-glow-foundation_1.jpg', 'nars-sheer-glow-foundation_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100106', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-SHEERGLOW67/SMALL/nars-sheer-glow-foundation_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-SHEERGLOW67/SMALL/nars-sheer-glow-foundation_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100107, '65f020632bc46470c104b76f', 'nars-sheer-glow-foundation_2.jpg', 'nars-sheer-glow-foundation_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100107', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-SHEERGLOW67/SMALL/nars-sheer-glow-foundation_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-SHEERGLOW67/SMALL/nars-sheer-glow-foundation_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100108, '65f020632bc46470c104b76f', 'nars-sheer-glow-foundation_3.jpg', 'nars-sheer-glow-foundation_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100108', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-SHEERGLOW67/SMALL/nars-sheer-glow-foundation_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-SHEERGLOW67/SMALL/nars-sheer-glow-foundation_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100109, '65f020632bc46470c104b76f', 'nars-sheer-glow-foundation_4.jpg', 'nars-sheer-glow-foundation_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100109', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-SHEERGLOW67/SMALL/nars-sheer-glow-foundation_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-SHEERGLOW67/SMALL/nars-sheer-glow-foundation_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100110, '65f020632bc46470c104b76f', 'nars-sheer-glow-foundation_5.jpg', 'nars-sheer-glow-foundation_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100110', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-SHEERGLOW67/SMALL/nars-sheer-glow-foundation_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-SHEERGLOW67/SMALL/nars-sheer-glow-foundation_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100111, '65f020632bc46470c104b76f', 'la-roche-posay-effaclar-duo-plus_1.jpg', 'la-roche-posay-effaclar-duo-plus_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100111', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-EFFADUO68/SMALL/la-roche-posay-effaclar-duo-plus_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-EFFADUO68/SMALL/la-roche-posay-effaclar-duo-plus_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100112, '65f020632bc46470c104b76f', 'la-roche-posay-effaclar-duo-plus_2.jpg', 'la-roche-posay-effaclar-duo-plus_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100112', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-EFFADUO68/SMALL/la-roche-posay-effaclar-duo-plus_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-EFFADUO68/SMALL/la-roche-posay-effaclar-duo-plus_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100113, '65f020632bc46470c104b76f', 'la-roche-posay-effaclar-duo-plus_3.jpg', 'la-roche-posay-effaclar-duo-plus_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100113', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-EFFADUO68/SMALL/la-roche-posay-effaclar-duo-plus_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-EFFADUO68/SMALL/la-roche-posay-effaclar-duo-plus_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100114, '65f020632bc46470c104b76f', 'la-roche-posay-effaclar-duo-plus_4.jpg', 'la-roche-posay-effaclar-duo-plus_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100114', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-EFFADUO68/SMALL/la-roche-posay-effaclar-duo-plus_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-EFFADUO68/SMALL/la-roche-posay-effaclar-duo-plus_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100115, '65f020632bc46470c104b76f', 'la-roche-posay-effaclar-duo-plus_5.jpg', 'la-roche-posay-effaclar-duo-plus_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100115', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-EFFADUO68/SMALL/la-roche-posay-effaclar-duo-plus_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-EFFADUO68/SMALL/la-roche-posay-effaclar-duo-plus_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100116, '65f020632bc46470c104b76f', 'kerastase-chronologiste-huile-de-parfum_1.jpg', 'kerastase-chronologiste-huile-de-parfum_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100116', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-CHRONOHUILE69/SMALL/kerastase-chronologiste-huile-de-parfum_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-CHRONOHUILE69/SMALL/kerastase-chronologiste-huile-de-parfum_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100117, '65f020632bc46470c104b76f', 'kerastase-chronologiste-huile-de-parfum_2.jpg', 'kerastase-chronologiste-huile-de-parfum_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100117', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-CHRONOHUILE69/SMALL/kerastase-chronologiste-huile-de-parfum_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-CHRONOHUILE69/SMALL/kerastase-chronologiste-huile-de-parfum_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100118, '65f020632bc46470c104b76f', 'kerastase-chronologiste-huile-de-parfum_3.jpg', 'kerastase-chronologiste-huile-de-parfum_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100118', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-CHRONOHUILE69/SMALL/kerastase-chronologiste-huile-de-parfum_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-CHRONOHUILE69/SMALL/kerastase-chronologiste-huile-de-parfum_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100119, '65f020632bc46470c104b76f', 'kerastase-chronologiste-huile-de-parfum_4.jpg', 'kerastase-chronologiste-huile-de-parfum_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100119', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-CHRONOHUILE69/SMALL/kerastase-chronologiste-huile-de-parfum_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-CHRONOHUILE69/SMALL/kerastase-chronologiste-huile-de-parfum_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100120, '65f020632bc46470c104b76f', 'kerastase-chronologiste-huile-de-parfum_5.jpg', 'kerastase-chronologiste-huile-de-parfum_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100120', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-CHRONOHUILE69/SMALL/kerastase-chronologiste-huile-de-parfum_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-CHRONOHUILE69/SMALL/kerastase-chronologiste-huile-de-parfum_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100121, '65f020632bc46470c104b76f', 'ysl-mon-paris-eau-de-parfum_1.jpg', 'ysl-mon-paris-eau-de-parfum_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100121', 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-MONPARIS70/SMALL/ysl-mon-paris-eau-de-parfum_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-FRAG-MONPARIS70/SMALL/ysl-mon-paris-eau-de-parfum_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100122, '65f020632bc46470c104b76f', 'ysl-mon-paris-eau-de-parfum_2.jpg', 'ysl-mon-paris-eau-de-parfum_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100122', 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-MONPARIS70/SMALL/ysl-mon-paris-eau-de-parfum_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-FRAG-MONPARIS70/SMALL/ysl-mon-paris-eau-de-parfum_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100123, '65f020632bc46470c104b76f', 'ysl-mon-paris-eau-de-parfum_3.jpg', 'ysl-mon-paris-eau-de-parfum_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100123', 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-MONPARIS70/SMALL/ysl-mon-paris-eau-de-parfum_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-FRAG-MONPARIS70/SMALL/ysl-mon-paris-eau-de-parfum_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100124, '65f020632bc46470c104b76f', 'ysl-mon-paris-eau-de-parfum_4.jpg', 'ysl-mon-paris-eau-de-parfum_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100124', 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-MONPARIS70/SMALL/ysl-mon-paris-eau-de-parfum_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-FRAG-MONPARIS70/SMALL/ysl-mon-paris-eau-de-parfum_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100125, '65f020632bc46470c104b76f', 'ysl-mon-paris-eau-de-parfum_5.jpg', 'ysl-mon-paris-eau-de-parfum_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100125', 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-MONPARIS70/SMALL/ysl-mon-paris-eau-de-parfum_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-FRAG-MONPARIS70/SMALL/ysl-mon-paris-eau-de-parfum_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100126, '65f020632bc46470c104b76f', 'guerlain-orchidee-imperiale-the-cream_1.jpg', 'guerlain-orchidee-imperiale-the-cream_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100126', 'products/65f020632bc46470c104b76f/heavier glass
        ''REF-GUER-SKIN-OICREAM71''/SMALL/guerlain-orchidee-imperiale-the-cream_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/heavier glass
        ''REF-GUER-SKIN-OICREAM71''/SMALL/guerlain-orchidee-imperiale-the-cream_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100127, '65f020632bc46470c104b76f', 'guerlain-orchidee-imperiale-the-cream_2.jpg', 'guerlain-orchidee-imperiale-the-cream_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100127', 'products/65f020632bc46470c104b76f/heavier glass
        ''REF-GUER-SKIN-OICREAM71''/SMALL/guerlain-orchidee-imperiale-the-cream_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/heavier glass
        ''REF-GUER-SKIN-OICREAM71''/SMALL/guerlain-orchidee-imperiale-the-cream_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100128, '65f020632bc46470c104b76f', 'guerlain-orchidee-imperiale-the-cream_3.jpg', 'guerlain-orchidee-imperiale-the-cream_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100128', 'products/65f020632bc46470c104b76f/heavier glass
        ''REF-GUER-SKIN-OICREAM71''/SMALL/guerlain-orchidee-imperiale-the-cream_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/heavier glass
        ''REF-GUER-SKIN-OICREAM71''/SMALL/guerlain-orchidee-imperiale-the-cream_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100129, '65f020632bc46470c104b76f', 'guerlain-orchidee-imperiale-the-cream_4.jpg', 'guerlain-orchidee-imperiale-the-cream_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100129', 'products/65f020632bc46470c104b76f/heavier glass
        ''REF-GUER-SKIN-OICREAM71''/SMALL/guerlain-orchidee-imperiale-the-cream_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/heavier glass
        ''REF-GUER-SKIN-OICREAM71''/SMALL/guerlain-orchidee-imperiale-the-cream_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100130, '65f020632bc46470c104b76f', 'guerlain-orchidee-imperiale-the-cream_5.jpg', 'guerlain-orchidee-imperiale-the-cream_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100130', 'products/65f020632bc46470c104b76f/heavier glass
        ''REF-GUER-SKIN-OICREAM71''/SMALL/guerlain-orchidee-imperiale-the-cream_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/heavier glass
        ''REF-GUER-SKIN-OICREAM71''/SMALL/guerlain-orchidee-imperiale-the-cream_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100131, '65f020632bc46470c104b76f', 'shiseido-minimalist-whippedpowder-blush_1.jpg', 'shiseido-minimalist-whippedpowder-blush_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100131', 'products/65f020632bc46470c104b76f/SKU-SHIS-MAKE-MINBLUSH72/SMALL/shiseido-minimalist-whippedpowder-blush_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-MAKE-MINBLUSH72/SMALL/shiseido-minimalist-whippedpowder-blush_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100132, '65f020632bc46470c104b76f', 'shiseido-minimalist-whippedpowder-blush_2.jpg', 'shiseido-minimalist-whippedpowder-blush_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100132', 'products/65f020632bc46470c104b76f/SKU-SHIS-MAKE-MINBLUSH72/SMALL/shiseido-minimalist-whippedpowder-blush_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-MAKE-MINBLUSH72/SMALL/shiseido-minimalist-whippedpowder-blush_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100133, '65f020632bc46470c104b76f', 'shiseido-minimalist-whippedpowder-blush_3.jpg', 'shiseido-minimalist-whippedpowder-blush_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100133', 'products/65f020632bc46470c104b76f/SKU-SHIS-MAKE-MINBLUSH72/SMALL/shiseido-minimalist-whippedpowder-blush_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-MAKE-MINBLUSH72/SMALL/shiseido-minimalist-whippedpowder-blush_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100134, '65f020632bc46470c104b76f', 'shiseido-minimalist-whippedpowder-blush_4.jpg', 'shiseido-minimalist-whippedpowder-blush_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100134', 'products/65f020632bc46470c104b76f/SKU-SHIS-MAKE-MINBLUSH72/SMALL/shiseido-minimalist-whippedpowder-blush_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-MAKE-MINBLUSH72/SMALL/shiseido-minimalist-whippedpowder-blush_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100135, '65f020632bc46470c104b76f', 'shiseido-minimalist-whippedpowder-blush_5.jpg', 'shiseido-minimalist-whippedpowder-blush_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100135', 'products/65f020632bc46470c104b76f/SKU-SHIS-MAKE-MINBLUSH72/SMALL/shiseido-minimalist-whippedpowder-blush_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-MAKE-MINBLUSH72/SMALL/shiseido-minimalist-whippedpowder-blush_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100136, '65f020632bc46470c104b76f', 'nars-climax-mascara_1.jpg', 'nars-climax-mascara_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100136', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-CLIMAXMASC73/SMALL/nars-climax-mascara_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-CLIMAXMASC73/SMALL/nars-climax-mascara_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100137, '65f020632bc46470c104b76f', 'nars-climax-mascara_2.jpg', 'nars-climax-mascara_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100137', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-CLIMAXMASC73/SMALL/nars-climax-mascara_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-CLIMAXMASC73/SMALL/nars-climax-mascara_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100138, '65f020632bc46470c104b76f', 'nars-climax-mascara_3.jpg', 'nars-climax-mascara_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100138', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-CLIMAXMASC73/SMALL/nars-climax-mascara_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-CLIMAXMASC73/SMALL/nars-climax-mascara_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100139, '65f020632bc46470c104b76f', 'nars-climax-mascara_4.jpg', 'nars-climax-mascara_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100139', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-CLIMAXMASC73/SMALL/nars-climax-mascara_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-CLIMAXMASC73/SMALL/nars-climax-mascara_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100140, '65f020632bc46470c104b76f', 'nars-climax-mascara_5.jpg', 'nars-climax-mascara_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100140', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-CLIMAXMASC73/SMALL/nars-climax-mascara_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-CLIMAXMASC73/SMALL/nars-climax-mascara_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100141, '65f020632bc46470c104b76f', 'la-roche-posay-toleriane-sensitive-creme_1.jpg', 'la-roche-posay-toleriane-sensitive-creme_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100141', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-TOLSENSCR74/SMALL/la-roche-posay-toleriane-sensitive-creme_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-TOLSENSCR74/SMALL/la-roche-posay-toleriane-sensitive-creme_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100142, '65f020632bc46470c104b76f', 'la-roche-posay-toleriane-sensitive-creme_2.jpg', 'la-roche-posay-toleriane-sensitive-creme_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100142', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-TOLSENSCR74/SMALL/la-roche-posay-toleriane-sensitive-creme_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-TOLSENSCR74/SMALL/la-roche-posay-toleriane-sensitive-creme_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100143, '65f020632bc46470c104b76f', 'la-roche-posay-toleriane-sensitive-creme_3.jpg', 'la-roche-posay-toleriane-sensitive-creme_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100143', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-TOLSENSCR74/SMALL/la-roche-posay-toleriane-sensitive-creme_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-TOLSENSCR74/SMALL/la-roche-posay-toleriane-sensitive-creme_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100144, '65f020632bc46470c104b76f', 'la-roche-posay-toleriane-sensitive-creme_4.jpg', 'la-roche-posay-toleriane-sensitive-creme_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100144', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-TOLSENSCR74/SMALL/la-roche-posay-toleriane-sensitive-creme_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-TOLSENSCR74/SMALL/la-roche-posay-toleriane-sensitive-creme_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100145, '65f020632bc46470c104b76f', 'la-roche-posay-toleriane-sensitive-creme_5.jpg', 'la-roche-posay-toleriane-sensitive-creme_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100145', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-TOLSENSCR74/SMALL/la-roche-posay-toleriane-sensitive-creme_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-TOLSENSCR74/SMALL/la-roche-posay-toleriane-sensitive-creme_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100146, '65f020632bc46470c104b76f', 'kerastase-resistance-ciment-thermique_1.jpg', 'kerastase-resistance-ciment-thermique_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100146', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-CIMENTHERM75/SMALL/kerastase-resistance-ciment-thermique_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-CIMENTHERM75/SMALL/kerastase-resistance-ciment-thermique_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100147, '65f020632bc46470c104b76f', 'kerastase-resistance-ciment-thermique_2.jpg', 'kerastase-resistance-ciment-thermique_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100147', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-CIMENTHERM75/SMALL/kerastase-resistance-ciment-thermique_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-CIMENTHERM75/SMALL/kerastase-resistance-ciment-thermique_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100148, '65f020632bc46470c104b76f', 'kerastase-resistance-ciment-thermique_3.jpg', 'kerastase-resistance-ciment-thermique_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100148', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-CIMENTHERM75/SMALL/kerastase-resistance-ciment-thermique_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-CIMENTHERM75/SMALL/kerastase-resistance-ciment-thermique_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100149, '65f020632bc46470c104b76f', 'kerastase-resistance-ciment-thermique_4.jpg', 'kerastase-resistance-ciment-thermique_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100149', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-CIMENTHERM75/SMALL/kerastase-resistance-ciment-thermique_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-CIMENTHERM75/SMALL/kerastase-resistance-ciment-thermique_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100150, '65f020632bc46470c104b76f', 'kerastase-resistance-ciment-thermique_5.jpg', 'kerastase-resistance-ciment-thermique_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100150', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-CIMENTHERM75/SMALL/kerastase-resistance-ciment-thermique_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-CIMENTHERM75/SMALL/kerastase-resistance-ciment-thermique_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100151, '65f020632bc46470c104b76f', 'ysl-pure-shots-night-reboot-serum_1.jpg', 'ysl-pure-shots-night-reboot-serum_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100151', 'products/65f020632bc46470c104b76f/SKU-YSL-SKIN-PSNIGHT76/SMALL/ysl-pure-shots-night-reboot-serum_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-SKIN-PSNIGHT76/SMALL/ysl-pure-shots-night-reboot-serum_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100152, '65f020632bc46470c104b76f', 'ysl-pure-shots-night-reboot-serum_2.jpg', 'ysl-pure-shots-night-reboot-serum_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100152', 'products/65f020632bc46470c104b76f/SKU-YSL-SKIN-PSNIGHT76/SMALL/ysl-pure-shots-night-reboot-serum_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-SKIN-PSNIGHT76/SMALL/ysl-pure-shots-night-reboot-serum_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100153, '65f020632bc46470c104b76f', 'ysl-pure-shots-night-reboot-serum_3.jpg', 'ysl-pure-shots-night-reboot-serum_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100153', 'products/65f020632bc46470c104b76f/SKU-YSL-SKIN-PSNIGHT76/SMALL/ysl-pure-shots-night-reboot-serum_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-SKIN-PSNIGHT76/SMALL/ysl-pure-shots-night-reboot-serum_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100154, '65f020632bc46470c104b76f', 'ysl-pure-shots-night-reboot-serum_4.jpg', 'ysl-pure-shots-night-reboot-serum_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100154', 'products/65f020632bc46470c104b76f/SKU-YSL-SKIN-PSNIGHT76/SMALL/ysl-pure-shots-night-reboot-serum_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-SKIN-PSNIGHT76/SMALL/ysl-pure-shots-night-reboot-serum_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100155, '65f020632bc46470c104b76f', 'ysl-pure-shots-night-reboot-serum_5.jpg', 'ysl-pure-shots-night-reboot-serum_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100155', 'products/65f020632bc46470c104b76f/SKU-YSL-SKIN-PSNIGHT76/SMALL/ysl-pure-shots-night-reboot-serum_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-SKIN-PSNIGHT76/SMALL/ysl-pure-shots-night-reboot-serum_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100156, '65f020632bc46470c104b76f', 'guerlain-aqua-allegoria-mandarine-basilic-edt_1.jpg', 'guerlain-aqua-allegoria-mandarine-basilic-edt_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100156', 'products/65f020632bc46470c104b76f/SKU-GUER-FRAG-AAMANDBAS77/SMALL/guerlain-aqua-allegoria-mandarine-basilic-edt_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-FRAG-AAMANDBAS77/SMALL/guerlain-aqua-allegoria-mandarine-basilic-edt_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100157, '65f020632bc46470c104b76f', 'guerlain-aqua-allegoria-mandarine-basilic-edt_2.jpg', 'guerlain-aqua-allegoria-mandarine-basilic-edt_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100157', 'products/65f020632bc46470c104b76f/SKU-GUER-FRAG-AAMANDBAS77/SMALL/guerlain-aqua-allegoria-mandarine-basilic-edt_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-FRAG-AAMANDBAS77/SMALL/guerlain-aqua-allegoria-mandarine-basilic-edt_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100158, '65f020632bc46470c104b76f', 'guerlain-aqua-allegoria-mandarine-basilic-edt_3.jpg', 'guerlain-aqua-allegoria-mandarine-basilic-edt_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100158', 'products/65f020632bc46470c104b76f/SKU-GUER-FRAG-AAMANDBAS77/SMALL/guerlain-aqua-allegoria-mandarine-basilic-edt_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-FRAG-AAMANDBAS77/SMALL/guerlain-aqua-allegoria-mandarine-basilic-edt_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100159, '65f020632bc46470c104b76f', 'guerlain-aqua-allegoria-mandarine-basilic-edt_4.jpg', 'guerlain-aqua-allegoria-mandarine-basilic-edt_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100159', 'products/65f020632bc46470c104b76f/SKU-GUER-FRAG-AAMANDBAS77/SMALL/guerlain-aqua-allegoria-mandarine-basilic-edt_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-FRAG-AAMANDBAS77/SMALL/guerlain-aqua-allegoria-mandarine-basilic-edt_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100160, '65f020632bc46470c104b76f', 'guerlain-aqua-allegoria-mandarine-basilic-edt_5.jpg', 'guerlain-aqua-allegoria-mandarine-basilic-edt_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100160', 'products/65f020632bc46470c104b76f/SKU-GUER-FRAG-AAMANDBAS77/SMALL/guerlain-aqua-allegoria-mandarine-basilic-edt_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-FRAG-AAMANDBAS77/SMALL/guerlain-aqua-allegoria-mandarine-basilic-edt_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100161, '65f020632bc46470c104b76f', 'shiseido-tsubaki-premium-repair-mask_1.jpg', 'shiseido-tsubaki-premium-repair-mask_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100161', 'products/65f020632bc46470c104b76f/SKU-SHIS-HAIR-TSUBAKIMASK78/SMALL/shiseido-tsubaki-premium-repair-mask_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-HAIR-TSUBAKIMASK78/SMALL/shiseido-tsubaki-premium-repair-mask_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100162, '65f020632bc46470c104b76f', 'shiseido-tsubaki-premium-repair-mask_2.jpg', 'shiseido-tsubaki-premium-repair-mask_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100162', 'products/65f020632bc46470c104b76f/SKU-SHIS-HAIR-TSUBAKIMASK78/SMALL/shiseido-tsubaki-premium-repair-mask_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-HAIR-TSUBAKIMASK78/SMALL/shiseido-tsubaki-premium-repair-mask_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100163, '65f020632bc46470c104b76f', 'shiseido-tsubaki-premium-repair-mask_3.jpg', 'shiseido-tsubaki-premium-repair-mask_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100163', 'products/65f020632bc46470c104b76f/SKU-SHIS-HAIR-TSUBAKIMASK78/SMALL/shiseido-tsubaki-premium-repair-mask_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-HAIR-TSUBAKIMASK78/SMALL/shiseido-tsubaki-premium-repair-mask_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100164, '65f020632bc46470c104b76f', 'shiseido-tsubaki-premium-repair-mask_4.jpg', 'shiseido-tsubaki-premium-repair-mask_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100164', 'products/65f020632bc46470c104b76f/SKU-SHIS-HAIR-TSUBAKIMASK78/SMALL/shiseido-tsubaki-premium-repair-mask_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-HAIR-TSUBAKIMASK78/SMALL/shiseido-tsubaki-premium-repair-mask_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100165, '65f020632bc46470c104b76f', 'shiseido-tsubaki-premium-repair-mask_5.jpg', 'shiseido-tsubaki-premium-repair-mask_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100165', 'products/65f020632bc46470c104b76f/SKU-SHIS-HAIR-TSUBAKIMASK78/SMALL/shiseido-tsubaki-premium-repair-mask_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-HAIR-TSUBAKIMASK78/SMALL/shiseido-tsubaki-premium-repair-mask_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100166, '65f020632bc46470c104b76f', 'nars-afterglow-lip-balm_1.jpg', 'nars-afterglow-lip-balm_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100166', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-AFTERGLOWLB79/SMALL/nars-afterglow-lip-balm_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-AFTERGLOWLB79/SMALL/nars-afterglow-lip-balm_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100167, '65f020632bc46470c104b76f', 'nars-afterglow-lip-balm_2.jpg', 'nars-afterglow-lip-balm_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100167', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-AFTERGLOWLB79/SMALL/nars-afterglow-lip-balm_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-AFTERGLOWLB79/SMALL/nars-afterglow-lip-balm_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100168, '65f020632bc46470c104b76f', 'nars-afterglow-lip-balm_3.jpg', 'nars-afterglow-lip-balm_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100168', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-AFTERGLOWLB79/SMALL/nars-afterglow-lip-balm_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-AFTERGLOWLB79/SMALL/nars-afterglow-lip-balm_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100169, '65f020632bc46470c104b76f', 'nars-afterglow-lip-balm_4.jpg', 'nars-afterglow-lip-balm_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100169', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-AFTERGLOWLB79/SMALL/nars-afterglow-lip-balm_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-AFTERGLOWLB79/SMALL/nars-afterglow-lip-balm_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100170, '65f020632bc46470c104b76f', 'nars-afterglow-lip-balm_5.jpg', 'nars-afterglow-lip-balm_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100170', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-AFTERGLOWLB79/SMALL/nars-afterglow-lip-balm_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-AFTERGLOWLB79/SMALL/nars-afterglow-lip-balm_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100171, '65f020632bc46470c104b76f', 'la-roche-posay-lipikar-baume-ap-m_1.jpg', 'la-roche-posay-lipikar-baume-ap-m_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100171', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-LIPIAPM80/SMALL/la-roche-posay-lipikar-baume-ap-m_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-LIPIAPM80/SMALL/la-roche-posay-lipikar-baume-ap-m_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100172, '65f020632bc46470c104b76f', 'la-roche-posay-lipikar-baume-ap-m_2.jpg', 'la-roche-posay-lipikar-baume-ap-m_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100172', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-LIPIAPM80/SMALL/la-roche-posay-lipikar-baume-ap-m_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-LIPIAPM80/SMALL/la-roche-posay-lipikar-baume-ap-m_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100173, '65f020632bc46470c104b76f', 'la-roche-posay-lipikar-baume-ap-m_3.jpg', 'la-roche-posay-lipikar-baume-ap-m_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100173', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-LIPIAPM80/SMALL/la-roche-posay-lipikar-baume-ap-m_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-LIPIAPM80/SMALL/la-roche-posay-lipikar-baume-ap-m_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100174, '65f020632bc46470c104b76f', 'la-roche-posay-lipikar-baume-ap-m_4.jpg', 'la-roche-posay-lipikar-baume-ap-m_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100174', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-LIPIAPM80/SMALL/la-roche-posay-lipikar-baume-ap-m_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-LIPIAPM80/SMALL/la-roche-posay-lipikar-baume-ap-m_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100175, '65f020632bc46470c104b76f', 'la-roche-posay-lipikar-baume-ap-m_5.jpg', 'la-roche-posay-lipikar-baume-ap-m_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100175', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-LIPIAPM80/SMALL/la-roche-posay-lipikar-baume-ap-m_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-LIPIAPM80/SMALL/la-roche-posay-lipikar-baume-ap-m_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100176, '65f020632bc46470c104b76f', 'kerastase-nutritive-8h-magic-night-serum_1.jpg', 'kerastase-nutritive-8h-magic-night-serum_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100176', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-NUTRI8HSERUM81/SMALL/kerastase-nutritive-8h-magic-night-serum_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-NUTRI8HSERUM81/SMALL/kerastase-nutritive-8h-magic-night-serum_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100177, '65f020632bc46470c104b76f', 'kerastase-nutritive-8h-magic-night-serum_2.jpg', 'kerastase-nutritive-8h-magic-night-serum_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100177', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-NUTRI8HSERUM81/SMALL/kerastase-nutritive-8h-magic-night-serum_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-NUTRI8HSERUM81/SMALL/kerastase-nutritive-8h-magic-night-serum_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100178, '65f020632bc46470c104b76f', 'kerastase-nutritive-8h-magic-night-serum_3.jpg', 'kerastase-nutritive-8h-magic-night-serum_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100178', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-NUTRI8HSERUM81/SMALL/kerastase-nutritive-8h-magic-night-serum_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-NUTRI8HSERUM81/SMALL/kerastase-nutritive-8h-magic-night-serum_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100179, '65f020632bc46470c104b76f', 'kerastase-nutritive-8h-magic-night-serum_4.jpg', 'kerastase-nutritive-8h-magic-night-serum_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100179', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-NUTRI8HSERUM81/SMALL/kerastase-nutritive-8h-magic-night-serum_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-NUTRI8HSERUM81/SMALL/kerastase-nutritive-8h-magic-night-serum_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100180, '65f020632bc46470c104b76f', 'kerastase-nutritive-8h-magic-night-serum_5.jpg', 'kerastase-nutritive-8h-magic-night-serum_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100180', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-NUTRI8HSERUM81/SMALL/kerastase-nutritive-8h-magic-night-serum_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-NUTRI8HSERUM81/SMALL/kerastase-nutritive-8h-magic-night-serum_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100181, '65f020632bc46470c104b76f', 'ysl-all-hours-foundation_1.jpg', 'ysl-all-hours-foundation_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100181', 'products/65f020632bc46470c104b76f/SKU-YSL-MAKE-ALLHOURSFND82/SMALL/ysl-all-hours-foundation_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-MAKE-ALLHOURSFND82/SMALL/ysl-all-hours-foundation_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100182, '65f020632bc46470c104b76f', 'ysl-all-hours-foundation_2.jpg', 'ysl-all-hours-foundation_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100182', 'products/65f020632bc46470c104b76f/SKU-YSL-MAKE-ALLHOURSFND82/SMALL/ysl-all-hours-foundation_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-MAKE-ALLHOURSFND82/SMALL/ysl-all-hours-foundation_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100183, '65f020632bc46470c104b76f', 'ysl-all-hours-foundation_3.jpg', 'ysl-all-hours-foundation_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100183', 'products/65f020632bc46470c104b76f/SKU-YSL-MAKE-ALLHOURSFND82/SMALL/ysl-all-hours-foundation_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-MAKE-ALLHOURSFND82/SMALL/ysl-all-hours-foundation_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100184, '65f020632bc46470c104b76f', 'ysl-all-hours-foundation_4.jpg', 'ysl-all-hours-foundation_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100184', 'products/65f020632bc46470c104b76f/SKU-YSL-MAKE-ALLHOURSFND82/SMALL/ysl-all-hours-foundation_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-MAKE-ALLHOURSFND82/SMALL/ysl-all-hours-foundation_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100185, '65f020632bc46470c104b76f', 'ysl-all-hours-foundation_5.jpg', 'ysl-all-hours-foundation_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100185', 'products/65f020632bc46470c104b76f/SKU-YSL-MAKE-ALLHOURSFND82/SMALL/ysl-all-hours-foundation_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-MAKE-ALLHOURSFND82/SMALL/ysl-all-hours-foundation_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100186, '65f020632bc46470c104b76f', 'guerlain-super-aqua-serum_1.jpg', 'guerlain-super-aqua-serum_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100186', 'products/65f020632bc46470c104b76f/SKU-GUER-SKIN-SUPERAQUA83/SMALL/guerlain-super-aqua-serum_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-SKIN-SUPERAQUA83/SMALL/guerlain-super-aqua-serum_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100187, '65f020632bc46470c104b76f', 'guerlain-super-aqua-serum_2.jpg', 'guerlain-super-aqua-serum_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100187', 'products/65f020632bc46470c104b76f/SKU-GUER-SKIN-SUPERAQUA83/SMALL/guerlain-super-aqua-serum_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-SKIN-SUPERAQUA83/SMALL/guerlain-super-aqua-serum_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100188, '65f020632bc46470c104b76f', 'guerlain-super-aqua-serum_3.jpg', 'guerlain-super-aqua-serum_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100188', 'products/65f020632bc46470c104b76f/SKU-GUER-SKIN-SUPERAQUA83/SMALL/guerlain-super-aqua-serum_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-SKIN-SUPERAQUA83/SMALL/guerlain-super-aqua-serum_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100189, '65f020632bc46470c104b76f', 'guerlain-super-aqua-serum_4.jpg', 'guerlain-super-aqua-serum_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100189', 'products/65f020632bc46470c104b76f/SKU-GUER-SKIN-SUPERAQUA83/SMALL/guerlain-super-aqua-serum_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-SKIN-SUPERAQUA83/SMALL/guerlain-super-aqua-serum_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100190, '65f020632bc46470c104b76f', 'guerlain-super-aqua-serum_5.jpg', 'guerlain-super-aqua-serum_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100190', 'products/65f020632bc46470c104b76f/SKU-GUER-SKIN-SUPERAQUA83/SMALL/guerlain-super-aqua-serum_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-SKIN-SUPERAQUA83/SMALL/guerlain-super-aqua-serum_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100191, '65f020632bc46470c104b76f', 'shiseido-ginza-eau-de-parfum_1.jpg', 'shiseido-ginza-eau-de-parfum_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100191', 'products/65f020632bc46470c104b76f/SKU-SHIS-FRAG-GINZAEDP84/SMALL/shiseido-ginza-eau-de-parfum_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-FRAG-GINZAEDP84/SMALL/shiseido-ginza-eau-de-parfum_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100192, '65f020632bc46470c104b76f', 'shiseido-ginza-eau-de-parfum_2.jpg', 'shiseido-ginza-eau-de-parfum_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100192', 'products/65f020632bc46470c104b76f/SKU-SHIS-FRAG-GINZAEDP84/SMALL/shiseido-ginza-eau-de-parfum_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-FRAG-GINZAEDP84/SMALL/shiseido-ginza-eau-de-parfum_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100193, '65f020632bc46470c104b76f', 'shiseido-ginza-eau-de-parfum_3.jpg', 'shiseido-ginza-eau-de-parfum_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100193', 'products/65f020632bc46470c104b76f/SKU-SHIS-FRAG-GINZAEDP84/SMALL/shiseido-ginza-eau-de-parfum_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-FRAG-GINZAEDP84/SMALL/shiseido-ginza-eau-de-parfum_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100194, '65f020632bc46470c104b76f', 'shiseido-ginza-eau-de-parfum_4.jpg', 'shiseido-ginza-eau-de-parfum_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100194', 'products/65f020632bc46470c104b76f/SKU-SHIS-FRAG-GINZAEDP84/SMALL/shiseido-ginza-eau-de-parfum_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-FRAG-GINZAEDP84/SMALL/shiseido-ginza-eau-de-parfum_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100195, '65f020632bc46470c104b76f', 'shiseido-ginza-eau-de-parfum_5.jpg', 'shiseido-ginza-eau-de-parfum_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100195', 'products/65f020632bc46470c104b76f/SKU-SHIS-FRAG-GINZAEDP84/SMALL/shiseido-ginza-eau-de-parfum_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-FRAG-GINZAEDP84/SMALL/shiseido-ginza-eau-de-parfum_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100196, '65f020632bc46470c104b76f', 'nars-light-reflecting-setting-powder_1.jpg', 'nars-light-reflecting-setting-powder_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100196', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-LRSPWD85/SMALL/nars-light-reflecting-setting-powder_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-LRSPWD85/SMALL/nars-light-reflecting-setting-powder_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100197, '65f020632bc46470c104b76f', 'nars-light-reflecting-setting-powder_2.jpg', 'nars-light-reflecting-setting-powder_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100197', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-LRSPWD85/SMALL/nars-light-reflecting-setting-powder_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-LRSPWD85/SMALL/nars-light-reflecting-setting-powder_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100198, '65f020632bc46470c104b76f', 'nars-light-reflecting-setting-powder_3.jpg', 'nars-light-reflecting-setting-powder_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100198', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-LRSPWD85/SMALL/nars-light-reflecting-setting-powder_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-LRSPWD85/SMALL/nars-light-reflecting-setting-powder_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100199, '65f020632bc46470c104b76f', 'nars-light-reflecting-setting-powder_4.jpg', 'nars-light-reflecting-setting-powder_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100199', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-LRSPWD85/SMALL/nars-light-reflecting-setting-powder_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-LRSPWD85/SMALL/nars-light-reflecting-setting-powder_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100200, '65f020632bc46470c104b76f', 'nars-light-reflecting-setting-powder_5.jpg', 'nars-light-reflecting-setting-powder_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100200', 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-LRSPWD85/SMALL/nars-light-reflecting-setting-powder_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-NARS-MAKE-LRSPWD85/SMALL/nars-light-reflecting-setting-powder_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100201, '65f020632bc46470c104b76f', 'la-roche-posay-pure-vitamin-c10-serum_1.jpg', 'la-roche-posay-pure-vitamin-c10-serum_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100201', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-VITC10-86/SMALL/la-roche-posay-pure-vitamin-c10-serum_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-VITC10-86/SMALL/la-roche-posay-pure-vitamin-c10-serum_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100202, '65f020632bc46470c104b76f', 'la-roche-posay-pure-vitamin-c10-serum_2.jpg', 'la-roche-posay-pure-vitamin-c10-serum_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100202', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-VITC10-86/SMALL/la-roche-posay-pure-vitamin-c10-serum_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-VITC10-86/SMALL/la-roche-posay-pure-vitamin-c10-serum_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100203, '65f020632bc46470c104b76f', 'la-roche-posay-pure-vitamin-c10-serum_3.jpg', 'la-roche-posay-pure-vitamin-c10-serum_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100203', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-VITC10-86/SMALL/la-roche-posay-pure-vitamin-c10-serum_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-VITC10-86/SMALL/la-roche-posay-pure-vitamin-c10-serum_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100204, '65f020632bc46470c104b76f', 'la-roche-posay-pure-vitamin-c10-serum_4.jpg', 'la-roche-posay-pure-vitamin-c10-serum_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100204', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-VITC10-86/SMALL/la-roche-posay-pure-vitamin-c10-serum_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-VITC10-86/SMALL/la-roche-posay-pure-vitamin-c10-serum_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100205, '65f020632bc46470c104b76f', 'la-roche-posay-pure-vitamin-c10-serum_5.jpg', 'la-roche-posay-pure-vitamin-c10-serum_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100205', 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-VITC10-86/SMALL/la-roche-posay-pure-vitamin-c10-serum_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-LRP-SKIN-VITC10-86/SMALL/la-roche-posay-pure-vitamin-c10-serum_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100206, '65f020632bc46470c104b76f', 'kerastase-discipline-fluidissime-spray_1.jpg', 'kerastase-discipline-fluidissime-spray_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100206', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-DISCFLUID87/SMALL/kerastase-discipline-fluidissime-spray_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-DISCFLUID87/SMALL/kerastase-discipline-fluidissime-spray_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100207, '65f020632bc46470c104b76f', 'kerastase-discipline-fluidissime-spray_2.jpg', 'kerastase-discipline-fluidissime-spray_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100207', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-DISCFLUID87/SMALL/kerastase-discipline-fluidissime-spray_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-DISCFLUID87/SMALL/kerastase-discipline-fluidissime-spray_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100208, '65f020632bc46470c104b76f', 'kerastase-discipline-fluidissime-spray_3.jpg', 'kerastase-discipline-fluidissime-spray_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100208', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-DISCFLUID87/SMALL/kerastase-discipline-fluidissime-spray_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-DISCFLUID87/SMALL/kerastase-discipline-fluidissime-spray_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100209, '65f020632bc46470c104b76f', 'kerastase-discipline-fluidissime-spray_4.jpg', 'kerastase-discipline-fluidissime-spray_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100209', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-DISCFLUID87/SMALL/kerastase-discipline-fluidissime-spray_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-DISCFLUID87/SMALL/kerastase-discipline-fluidissime-spray_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100210, '65f020632bc46470c104b76f', 'kerastase-discipline-fluidissime-spray_5.jpg', 'kerastase-discipline-fluidissime-spray_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100210', 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-DISCFLUID87/SMALL/kerastase-discipline-fluidissime-spray_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-KERA-HAIR-DISCFLUID87/SMALL/kerastase-discipline-fluidissime-spray_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100211, '65f020632bc46470c104b76f', 'ysl-y-eau-de-parfum_1.jpg', 'ysl-y-eau-de-parfum_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100211', 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-YEDP88/SMALL/ysl-y-eau-de-parfum_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-FRAG-YEDP88/SMALL/ysl-y-eau-de-parfum_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100212, '65f020632bc46470c104b76f', 'ysl-y-eau-de-parfum_2.jpg', 'ysl-y-eau-de-parfum_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100212', 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-YEDP88/SMALL/ysl-y-eau-de-parfum_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-FRAG-YEDP88/SMALL/ysl-y-eau-de-parfum_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100213, '65f020632bc46470c104b76f', 'ysl-y-eau-de-parfum_3.jpg', 'ysl-y-eau-de-parfum_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100213', 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-YEDP88/SMALL/ysl-y-eau-de-parfum_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-FRAG-YEDP88/SMALL/ysl-y-eau-de-parfum_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100214, '65f020632bc46470c104b76f', 'ysl-y-eau-de-parfum_4.jpg', 'ysl-y-eau-de-parfum_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100214', 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-YEDP88/SMALL/ysl-y-eau-de-parfum_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-FRAG-YEDP88/SMALL/ysl-y-eau-de-parfum_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100215, '65f020632bc46470c104b76f', 'ysl-y-eau-de-parfum_5.jpg', 'ysl-y-eau-de-parfum_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100215', 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-YEDP88/SMALL/ysl-y-eau-de-parfum_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-YSL-FRAG-YEDP88/SMALL/ysl-y-eau-de-parfum_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100216, '65f020632bc46470c104b76f', 'guerlain-lessentiel-natural-glow-foundation_1.jpg', 'guerlain-lessentiel-natural-glow-foundation_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100216', 'products/65f020632bc46470c104b76f/SKU-GUER-MAKE-LESSENTIEL89/SMALL/guerlain-lessentiel-natural-glow-foundation_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-MAKE-LESSENTIEL89/SMALL/guerlain-lessentiel-natural-glow-foundation_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100217, '65f020632bc46470c104b76f', 'guerlain-lessentiel-natural-glow-foundation_2.jpg', 'guerlain-lessentiel-natural-glow-foundation_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100217', 'products/65f020632bc46470c104b76f/SKU-GUER-MAKE-LESSENTIEL89/SMALL/guerlain-lessentiel-natural-glow-foundation_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-MAKE-LESSENTIEL89/SMALL/guerlain-lessentiel-natural-glow-foundation_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100218, '65f020632bc46470c104b76f', 'guerlain-lessentiel-natural-glow-foundation_3.jpg', 'guerlain-lessentiel-natural-glow-foundation_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100218', 'products/65f020632bc46470c104b76f/SKU-GUER-MAKE-LESSENTIEL89/SMALL/guerlain-lessentiel-natural-glow-foundation_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-MAKE-LESSENTIEL89/SMALL/guerlain-lessentiel-natural-glow-foundation_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100219, '65f020632bc46470c104b76f', 'guerlain-lessentiel-natural-glow-foundation_4.jpg', 'guerlain-lessentiel-natural-glow-foundation_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100219', 'products/65f020632bc46470c104b76f/SKU-GUER-MAKE-LESSENTIEL89/SMALL/guerlain-lessentiel-natural-glow-foundation_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-MAKE-LESSENTIEL89/SMALL/guerlain-lessentiel-natural-glow-foundation_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100220, '65f020632bc46470c104b76f', 'guerlain-lessentiel-natural-glow-foundation_5.jpg', 'guerlain-lessentiel-natural-glow-foundation_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100220', 'products/65f020632bc46470c104b76f/SKU-GUER-MAKE-LESSENTIEL89/SMALL/guerlain-lessentiel-natural-glow-foundation_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-GUER-MAKE-LESSENTIEL89/SMALL/guerlain-lessentiel-natural-glow-foundation_5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100221, '65f020632bc46470c104b76f', 'shiseido-waso-shikulime-gel-to-oil-cleanser_1.jpg', 'shiseido-waso-shikulime-gel-to-oil-cleanser_1.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100221', 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-WASOCLEAN90/SMALL/shiseido-waso-shikulime-gel-to-oil-cleanser_1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-WASOCLEAN90/SMALL/shiseido-waso-shikulime-gel-to-oil-cleanser_1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100222, '65f020632bc46470c104b76f', 'shiseido-waso-shikulime-gel-to-oil-cleanser_2.jpg', 'shiseido-waso-shikulime-gel-to-oil-cleanser_2.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100222', 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-WASOCLEAN90/SMALL/shiseido-waso-shikulime-gel-to-oil-cleanser_2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-WASOCLEAN90/SMALL/shiseido-waso-shikulime-gel-to-oil-cleanser_2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100223, '65f020632bc46470c104b76f', 'shiseido-waso-shikulime-gel-to-oil-cleanser_3.jpg', 'shiseido-waso-shikulime-gel-to-oil-cleanser_3.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100223', 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-WASOCLEAN90/SMALL/shiseido-waso-shikulime-gel-to-oil-cleanser_3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-WASOCLEAN90/SMALL/shiseido-waso-shikulime-gel-to-oil-cleanser_3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100224, '65f020632bc46470c104b76f', 'shiseido-waso-shikulime-gel-to-oil-cleanser_4.jpg', 'shiseido-waso-shikulime-gel-to-oil-cleanser_4.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100224', 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-WASOCLEAN90/SMALL/shiseido-waso-shikulime-gel-to-oil-cleanser_4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-WASOCLEAN90/SMALL/shiseido-waso-shikulime-gel-to-oil-cleanser_4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-100225, '65f020632bc46470c104b76f', 'shiseido-waso-shikulime-gel-to-oil-cleanser_5.jpg', 'shiseido-waso-shikulime-gel-to-oil-cleanser_5.jpg', 'image/jpeg', 'IMAGE',
        145000, 'seed-100225', 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-WASOCLEAN90/SMALL/shiseido-waso-shikulime-gel-to-oil-cleanser_5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-WASOCLEAN90/SMALL/shiseido-waso-shikulime-gel-to-oil-cleanser_5.jpg', now())
on conflict (id) do nothing;
