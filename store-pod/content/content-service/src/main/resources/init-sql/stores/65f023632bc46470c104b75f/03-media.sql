-- Demo product photos, registered as media assets.
--
-- The objects already exist in MinIO under the key layout catalog used when it owned files; the seed
-- registers them rather than moving bytes. Ids are negative on purpose: media asset ids come from a
-- sequence that only grows upward, so seed-only rows below zero can never collide with uploads.
-- catalog.product_image references these ids — see 16-catalog-product-image.sql in the catalog service.
-- bytes is 0 because the seed never weighed the objects; the quota bar therefore ignores demo photos.

INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300001, '65f023632bc46470c104b75f', 'apple-iphone-15-pro-1.jpg', 'apple-iphone-15-pro-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300001', 'products/65f023632bc46470c104b75f/ELEC-SKU-136/SMALL/apple-iphone-15-pro-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-136/SMALL/apple-iphone-15-pro-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300002, '65f023632bc46470c104b75f', 'apple-iphone-15-pro-2.jpg', 'apple-iphone-15-pro-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300002', 'products/65f023632bc46470c104b75f/ELEC-SKU-136/SMALL/apple-iphone-15-pro-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-136/SMALL/apple-iphone-15-pro-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300003, '65f023632bc46470c104b75f', 'apple-iphone-15-pro-3.jpg', 'apple-iphone-15-pro-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300003', 'products/65f023632bc46470c104b75f/ELEC-SKU-136/SMALL/apple-iphone-15-pro-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-136/SMALL/apple-iphone-15-pro-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300004, '65f023632bc46470c104b75f', 'apple-iphone-15-pro-4.jpg', 'apple-iphone-15-pro-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300004', 'products/65f023632bc46470c104b75f/ELEC-SKU-136/SMALL/apple-iphone-15-pro-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-136/SMALL/apple-iphone-15-pro-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300005, '65f023632bc46470c104b75f', 'apple-iphone-15-pro-5.jpg', 'apple-iphone-15-pro-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300005', 'products/65f023632bc46470c104b75f/ELEC-SKU-136/SMALL/apple-iphone-15-pro-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-136/SMALL/apple-iphone-15-pro-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300006, '65f023632bc46470c104b75f', 'samsung-galaxy-s24-ultra-1.jpg', 'samsung-galaxy-s24-ultra-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300006', 'products/65f023632bc46470c104b75f/ELEC-SKU-137/SMALL/samsung-galaxy-s24-ultra-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-137/SMALL/samsung-galaxy-s24-ultra-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300007, '65f023632bc46470c104b75f', 'samsung-galaxy-s24-ultra-2.jpg', 'samsung-galaxy-s24-ultra-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300007', 'products/65f023632bc46470c104b75f/ELEC-SKU-137/SMALL/samsung-galaxy-s24-ultra-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-137/SMALL/samsung-galaxy-s24-ultra-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300008, '65f023632bc46470c104b75f', 'samsung-galaxy-s24-ultra-3.jpg', 'samsung-galaxy-s24-ultra-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300008', 'products/65f023632bc46470c104b75f/ELEC-SKU-137/SMALL/samsung-galaxy-s24-ultra-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-137/SMALL/samsung-galaxy-s24-ultra-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300009, '65f023632bc46470c104b75f', 'samsung-galaxy-s24-ultra-4.jpg', 'samsung-galaxy-s24-ultra-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300009', 'products/65f023632bc46470c104b75f/ELEC-SKU-137/SMALL/samsung-galaxy-s24-ultra-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-137/SMALL/samsung-galaxy-s24-ultra-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300010, '65f023632bc46470c104b75f', 'samsung-galaxy-s24-ultra-5.jpg', 'samsung-galaxy-s24-ultra-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300010', 'products/65f023632bc46470c104b75f/ELEC-SKU-137/SMALL/samsung-galaxy-s24-ultra-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-137/SMALL/samsung-galaxy-s24-ultra-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300011, '65f023632bc46470c104b75f', 'dell-xps-15-laptop-1.jpg', 'dell-xps-15-laptop-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300011', 'products/65f023632bc46470c104b75f/ELEC-SKU-138/SMALL/dell-xps-15-laptop-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-138/SMALL/dell-xps-15-laptop-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300012, '65f023632bc46470c104b75f', 'dell-xps-15-laptop-2.jpg', 'dell-xps-15-laptop-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300012', 'products/65f023632bc46470c104b75f/ELEC-SKU-138/SMALL/dell-xps-15-laptop-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-138/SMALL/dell-xps-15-laptop-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300013, '65f023632bc46470c104b75f', 'dell-xps-15-laptop-3.jpg', 'dell-xps-15-laptop-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300013', 'products/65f023632bc46470c104b75f/ELEC-SKU-138/SMALL/dell-xps-15-laptop-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-138/SMALL/dell-xps-15-laptop-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300014, '65f023632bc46470c104b75f', 'dell-xps-15-laptop-4.jpg', 'dell-xps-15-laptop-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300014', 'products/65f023632bc46470c104b75f/ELEC-SKU-138/SMALL/dell-xps-15-laptop-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-138/SMALL/dell-xps-15-laptop-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300015, '65f023632bc46470c104b75f', 'dell-xps-15-laptop-5.jpg', 'dell-xps-15-laptop-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300015', 'products/65f023632bc46470c104b75f/ELEC-SKU-138/SMALL/dell-xps-15-laptop-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-138/SMALL/dell-xps-15-laptop-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300016, '65f023632bc46470c104b75f', 'sony-wh-1000xm5-headphones-1.jpg', 'sony-wh-1000xm5-headphones-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300016', 'products/65f023632bc46470c104b75f/ELEC-SKU-139/SMALL/sony-wh-1000xm5-headphones-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-139/SMALL/sony-wh-1000xm5-headphones-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300017, '65f023632bc46470c104b75f', 'sony-wh-1000xm5-headphones-2.jpg', 'sony-wh-1000xm5-headphones-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300017', 'products/65f023632bc46470c104b75f/ELEC-SKU-139/SMALL/sony-wh-1000xm5-headphones-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-139/SMALL/sony-wh-1000xm5-headphones-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300018, '65f023632bc46470c104b75f', 'sony-wh-1000xm5-headphones-3.jpg', 'sony-wh-1000xm5-headphones-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300018', 'products/65f023632bc46470c104b75f/ELEC-SKU-139/SMALL/sony-wh-1000xm5-headphones-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-139/SMALL/sony-wh-1000xm5-headphones-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300019, '65f023632bc46470c104b75f', 'sony-wh-1000xm5-headphones-4.jpg', 'sony-wh-1000xm5-headphones-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300019', 'products/65f023632bc46470c104b75f/ELEC-SKU-139/SMALL/sony-wh-1000xm5-headphones-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-139/SMALL/sony-wh-1000xm5-headphones-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300020, '65f023632bc46470c104b75f', 'sony-wh-1000xm5-headphones-5.jpg', 'sony-wh-1000xm5-headphones-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300020', 'products/65f023632bc46470c104b75f/ELEC-SKU-139/SMALL/sony-wh-1000xm5-headphones-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-139/SMALL/sony-wh-1000xm5-headphones-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300021, '65f023632bc46470c104b75f', 'lg-c3-65-inch-oled-tv-1.jpg', 'lg-c3-65-inch-oled-tv-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300021', 'products/65f023632bc46470c104b75f/ELEC-SKU-140/SMALL/lg-c3-65-inch-oled-tv-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-140/SMALL/lg-c3-65-inch-oled-tv-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300022, '65f023632bc46470c104b75f', 'lg-c3-65-inch-oled-tv-2.jpg', 'lg-c3-65-inch-oled-tv-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300022', 'products/65f023632bc46470c104b75f/ELEC-SKU-140/SMALL/lg-c3-65-inch-oled-tv-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-140/SMALL/lg-c3-65-inch-oled-tv-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300023, '65f023632bc46470c104b75f', 'lg-c3-65-inch-oled-tv-3.jpg', 'lg-c3-65-inch-oled-tv-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300023', 'products/65f023632bc46470c104b75f/ELEC-SKU-140/SMALL/lg-c3-65-inch-oled-tv-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-140/SMALL/lg-c3-65-inch-oled-tv-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300024, '65f023632bc46470c104b75f', 'lg-c3-65-inch-oled-tv-4.jpg', 'lg-c3-65-inch-oled-tv-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300024', 'products/65f023632bc46470c104b75f/ELEC-SKU-140/SMALL/lg-c3-65-inch-oled-tv-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-140/SMALL/lg-c3-65-inch-oled-tv-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300025, '65f023632bc46470c104b75f', 'lg-c3-65-inch-oled-tv-5.jpg', 'lg-c3-65-inch-oled-tv-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300025', 'products/65f023632bc46470c104b75f/ELEC-SKU-140/SMALL/lg-c3-65-inch-oled-tv-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-140/SMALL/lg-c3-65-inch-oled-tv-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300026, '65f023632bc46470c104b75f', 'apple-macbook-air-13-m3-1.jpg', 'apple-macbook-air-13-m3-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300026', 'products/65f023632bc46470c104b75f/ELEC-SKU-141/SMALL/apple-macbook-air-13-m3-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-141/SMALL/apple-macbook-air-13-m3-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300027, '65f023632bc46470c104b75f', 'apple-macbook-air-13-m3-2.jpg', 'apple-macbook-air-13-m3-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300027', 'products/65f023632bc46470c104b75f/ELEC-SKU-141/SMALL/apple-macbook-air-13-m3-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-141/SMALL/apple-macbook-air-13-m3-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300028, '65f023632bc46470c104b75f', 'apple-macbook-air-13-m3-3.jpg', 'apple-macbook-air-13-m3-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300028', 'products/65f023632bc46470c104b75f/ELEC-SKU-141/SMALL/apple-macbook-air-13-m3-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-141/SMALL/apple-macbook-air-13-m3-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300029, '65f023632bc46470c104b75f', 'apple-macbook-air-13-m3-4.jpg', 'apple-macbook-air-13-m3-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300029', 'products/65f023632bc46470c104b75f/ELEC-SKU-141/SMALL/apple-macbook-air-13-m3-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-141/SMALL/apple-macbook-air-13-m3-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300030, '65f023632bc46470c104b75f', 'apple-macbook-air-13-m3-5.jpg', 'apple-macbook-air-13-m3-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300030', 'products/65f023632bc46470c104b75f/ELEC-SKU-141/SMALL/apple-macbook-air-13-m3-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-141/SMALL/apple-macbook-air-13-m3-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300031, '65f023632bc46470c104b75f', 'samsung-qn90c-55-inch-neo-qled-tv-1.jpg', 'samsung-qn90c-55-inch-neo-qled-tv-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300031', 'products/65f023632bc46470c104b75f/ELEC-SKU-142/SMALL/samsung-qn90c-55-inch-neo-qled-tv-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-142/SMALL/samsung-qn90c-55-inch-neo-qled-tv-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300032, '65f023632bc46470c104b75f', 'samsung-qn90c-55-inch-neo-qled-tv-2.jpg', 'samsung-qn90c-55-inch-neo-qled-tv-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300032', 'products/65f023632bc46470c104b75f/ELEC-SKU-142/SMALL/samsung-qn90c-55-inch-neo-qled-tv-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-142/SMALL/samsung-qn90c-55-inch-neo-qled-tv-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300033, '65f023632bc46470c104b75f', 'samsung-qn90c-55-inch-neo-qled-tv-3.jpg', 'samsung-qn90c-55-inch-neo-qled-tv-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300033', 'products/65f023632bc46470c104b75f/ELEC-SKU-142/SMALL/samsung-qn90c-55-inch-neo-qled-tv-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-142/SMALL/samsung-qn90c-55-inch-neo-qled-tv-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300034, '65f023632bc46470c104b75f', 'samsung-qn90c-55-inch-neo-qled-tv-4.jpg', 'samsung-qn90c-55-inch-neo-qled-tv-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300034', 'products/65f023632bc46470c104b75f/ELEC-SKU-142/SMALL/samsung-qn90c-55-inch-neo-qled-tv-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-142/SMALL/samsung-qn90c-55-inch-neo-qled-tv-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300035, '65f023632bc46470c104b75f', 'samsung-qn90c-55-inch-neo-qled-tv-5.jpg', 'samsung-qn90c-55-inch-neo-qled-tv-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300035', 'products/65f023632bc46470c104b75f/ELEC-SKU-142/SMALL/samsung-qn90c-55-inch-neo-qled-tv-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-142/SMALL/samsung-qn90c-55-inch-neo-qled-tv-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300036, '65f023632bc46470c104b75f', 'hp-spectre-x360-14-laptop-1.jpg', 'hp-spectre-x360-14-laptop-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300036', 'products/65f023632bc46470c104b75f/ELEC-SKU-143/SMALL/hp-spectre-x360-14-laptop-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-143/SMALL/hp-spectre-x360-14-laptop-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300037, '65f023632bc46470c104b75f', 'hp-spectre-x360-14-laptop-2.jpg', 'hp-spectre-x360-14-laptop-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300037', 'products/65f023632bc46470c104b75f/ELEC-SKU-143/SMALL/hp-spectre-x360-14-laptop-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-143/SMALL/hp-spectre-x360-14-laptop-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300038, '65f023632bc46470c104b75f', 'hp-spectre-x360-14-laptop-3.jpg', 'hp-spectre-x360-14-laptop-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300038', 'products/65f023632bc46470c104b75f/ELEC-SKU-143/SMALL/hp-spectre-x360-14-laptop-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-143/SMALL/hp-spectre-x360-14-laptop-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300039, '65f023632bc46470c104b75f', 'hp-spectre-x360-14-laptop-4.jpg', 'hp-spectre-x360-14-laptop-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300039', 'products/65f023632bc46470c104b75f/ELEC-SKU-143/SMALL/hp-spectre-x360-14-laptop-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-143/SMALL/hp-spectre-x360-14-laptop-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300040, '65f023632bc46470c104b75f', 'hp-spectre-x360-14-laptop-5.jpg', 'hp-spectre-x360-14-laptop-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300040', 'products/65f023632bc46470c104b75f/ELEC-SKU-143/SMALL/hp-spectre-x360-14-laptop-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-143/SMALL/hp-spectre-x360-14-laptop-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300041, '65f023632bc46470c104b75f', 'apple-airpods-pro-2nd-gen-1.jpg', 'apple-airpods-pro-2nd-gen-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300041', 'products/65f023632bc46470c104b75f/ELEC-SKU-144/SMALL/apple-airpods-pro-2nd-gen-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-144/SMALL/apple-airpods-pro-2nd-gen-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300042, '65f023632bc46470c104b75f', 'apple-airpods-pro-2nd-gen-2.jpg', 'apple-airpods-pro-2nd-gen-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300042', 'products/65f023632bc46470c104b75f/ELEC-SKU-144/SMALL/apple-airpods-pro-2nd-gen-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-144/SMALL/apple-airpods-pro-2nd-gen-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300043, '65f023632bc46470c104b75f', 'apple-airpods-pro-2nd-gen-3.jpg', 'apple-airpods-pro-2nd-gen-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300043', 'products/65f023632bc46470c104b75f/ELEC-SKU-144/SMALL/apple-airpods-pro-2nd-gen-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-144/SMALL/apple-airpods-pro-2nd-gen-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300044, '65f023632bc46470c104b75f', 'apple-airpods-pro-2nd-gen-4.jpg', 'apple-airpods-pro-2nd-gen-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300044', 'products/65f023632bc46470c104b75f/ELEC-SKU-144/SMALL/apple-airpods-pro-2nd-gen-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-144/SMALL/apple-airpods-pro-2nd-gen-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300045, '65f023632bc46470c104b75f', 'apple-airpods-pro-2nd-gen-5.jpg', 'apple-airpods-pro-2nd-gen-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300045', 'products/65f023632bc46470c104b75f/ELEC-SKU-144/SMALL/apple-airpods-pro-2nd-gen-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-144/SMALL/apple-airpods-pro-2nd-gen-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300046, '65f023632bc46470c104b75f', 'samsung-galaxy-buds2-pro-1.jpg', 'samsung-galaxy-buds2-pro-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300046', 'products/65f023632bc46470c104b75f/ELEC-SKU-145/SMALL/samsung-galaxy-buds2-pro-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-145/SMALL/samsung-galaxy-buds2-pro-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300047, '65f023632bc46470c104b75f', 'samsung-galaxy-buds2-pro-2.jpg', 'samsung-galaxy-buds2-pro-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300047', 'products/65f023632bc46470c104b75f/ELEC-SKU-145/SMALL/samsung-galaxy-buds2-pro-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-145/SMALL/samsung-galaxy-buds2-pro-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300048, '65f023632bc46470c104b75f', 'samsung-galaxy-buds2-pro-3.jpg', 'samsung-galaxy-buds2-pro-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300048', 'products/65f023632bc46470c104b75f/ELEC-SKU-145/SMALL/samsung-galaxy-buds2-pro-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-145/SMALL/samsung-galaxy-buds2-pro-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300049, '65f023632bc46470c104b75f', 'samsung-galaxy-buds2-pro-4.jpg', 'samsung-galaxy-buds2-pro-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300049', 'products/65f023632bc46470c104b75f/ELEC-SKU-145/SMALL/samsung-galaxy-buds2-pro-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-145/SMALL/samsung-galaxy-buds2-pro-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300050, '65f023632bc46470c104b75f', 'samsung-galaxy-buds2-pro-5.jpg', 'samsung-galaxy-buds2-pro-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300050', 'products/65f023632bc46470c104b75f/ELEC-SKU-145/SMALL/samsung-galaxy-buds2-pro-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-145/SMALL/samsung-galaxy-buds2-pro-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300051, '65f023632bc46470c104b75f', 'sony-bravia-x90l-65-inch-tv-1.jpg', 'sony-bravia-x90l-65-inch-tv-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300051', 'products/65f023632bc46470c104b75f/ELEC-SKU-146/SMALL/sony-bravia-x90l-65-inch-tv-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-146/SMALL/sony-bravia-x90l-65-inch-tv-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300052, '65f023632bc46470c104b75f', 'sony-bravia-x90l-65-inch-tv-2.jpg', 'sony-bravia-x90l-65-inch-tv-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300052', 'products/65f023632bc46470c104b75f/ELEC-SKU-146/SMALL/sony-bravia-x90l-65-inch-tv-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-146/SMALL/sony-bravia-x90l-65-inch-tv-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300053, '65f023632bc46470c104b75f', 'sony-bravia-x90l-65-inch-tv-3.jpg', 'sony-bravia-x90l-65-inch-tv-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300053', 'products/65f023632bc46470c104b75f/ELEC-SKU-146/SMALL/sony-bravia-x90l-65-inch-tv-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-146/SMALL/sony-bravia-x90l-65-inch-tv-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300054, '65f023632bc46470c104b75f', 'sony-bravia-x90l-65-inch-tv-4.jpg', 'sony-bravia-x90l-65-inch-tv-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300054', 'products/65f023632bc46470c104b75f/ELEC-SKU-146/SMALL/sony-bravia-x90l-65-inch-tv-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-146/SMALL/sony-bravia-x90l-65-inch-tv-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300055, '65f023632bc46470c104b75f', 'sony-bravia-x90l-65-inch-tv-5.jpg', 'sony-bravia-x90l-65-inch-tv-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300055', 'products/65f023632bc46470c104b75f/ELEC-SKU-146/SMALL/sony-bravia-x90l-65-inch-tv-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-146/SMALL/sony-bravia-x90l-65-inch-tv-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300056, '65f023632bc46470c104b75f', 'samsung-galaxy-a54-5g-1.jpg', 'samsung-galaxy-a54-5g-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300056', 'products/65f023632bc46470c104b75f/ELEC-SKU-147/SMALL/samsung-galaxy-a54-5g-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-147/SMALL/samsung-galaxy-a54-5g-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300057, '65f023632bc46470c104b75f', 'samsung-galaxy-a54-5g-2.jpg', 'samsung-galaxy-a54-5g-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300057', 'products/65f023632bc46470c104b75f/ELEC-SKU-147/SMALL/samsung-galaxy-a54-5g-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-147/SMALL/samsung-galaxy-a54-5g-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300058, '65f023632bc46470c104b75f', 'samsung-galaxy-a54-5g-3.jpg', 'samsung-galaxy-a54-5g-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300058', 'products/65f023632bc46470c104b75f/ELEC-SKU-147/SMALL/samsung-galaxy-a54-5g-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-147/SMALL/samsung-galaxy-a54-5g-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300059, '65f023632bc46470c104b75f', 'samsung-galaxy-a54-5g-4.jpg', 'samsung-galaxy-a54-5g-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300059', 'products/65f023632bc46470c104b75f/ELEC-SKU-147/SMALL/samsung-galaxy-a54-5g-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-147/SMALL/samsung-galaxy-a54-5g-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300060, '65f023632bc46470c104b75f', 'samsung-galaxy-a54-5g-5.jpg', 'samsung-galaxy-a54-5g-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300060', 'products/65f023632bc46470c104b75f/ELEC-SKU-147/SMALL/samsung-galaxy-a54-5g-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-147/SMALL/samsung-galaxy-a54-5g-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300061, '65f023632bc46470c104b75f', 'sony-linkbuds-s-earbuds-1.jpg', 'sony-linkbuds-s-earbuds-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300061', 'products/65f023632bc46470c104b75f/ELEC-SKU-148/SMALL/sony-linkbuds-s-earbuds-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-148/SMALL/sony-linkbuds-s-earbuds-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300062, '65f023632bc46470c104b75f', 'sony-linkbuds-s-earbuds-2.jpg', 'sony-linkbuds-s-earbuds-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300062', 'products/65f023632bc46470c104b75f/ELEC-SKU-148/SMALL/sony-linkbuds-s-earbuds-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-148/SMALL/sony-linkbuds-s-earbuds-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300063, '65f023632bc46470c104b75f', 'sony-linkbuds-s-earbuds-3.jpg', 'sony-linkbuds-s-earbuds-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300063', 'products/65f023632bc46470c104b75f/ELEC-SKU-148/SMALL/sony-linkbuds-s-earbuds-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-148/SMALL/sony-linkbuds-s-earbuds-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300064, '65f023632bc46470c104b75f', 'sony-linkbuds-s-earbuds-4.jpg', 'sony-linkbuds-s-earbuds-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300064', 'products/65f023632bc46470c104b75f/ELEC-SKU-148/SMALL/sony-linkbuds-s-earbuds-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-148/SMALL/sony-linkbuds-s-earbuds-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300065, '65f023632bc46470c104b75f', 'sony-linkbuds-s-earbuds-5.jpg', 'sony-linkbuds-s-earbuds-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300065', 'products/65f023632bc46470c104b75f/ELEC-SKU-148/SMALL/sony-linkbuds-s-earbuds-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-148/SMALL/sony-linkbuds-s-earbuds-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300066, '65f023632bc46470c104b75f', 'dell-inspiron-16-laptop-1.jpg', 'dell-inspiron-16-laptop-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300066', 'products/65f023632bc46470c104b75f/ELEC-SKU-149/SMALL/dell-inspiron-16-laptop-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-149/SMALL/dell-inspiron-16-laptop-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300067, '65f023632bc46470c104b75f', 'dell-inspiron-16-laptop-2.jpg', 'dell-inspiron-16-laptop-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300067', 'products/65f023632bc46470c104b75f/ELEC-SKU-149/SMALL/dell-inspiron-16-laptop-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-149/SMALL/dell-inspiron-16-laptop-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300068, '65f023632bc46470c104b75f', 'dell-inspiron-16-laptop-3.jpg', 'dell-inspiron-16-laptop-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300068', 'products/65f023632bc46470c104b75f/ELEC-SKU-149/SMALL/dell-inspiron-16-laptop-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-149/SMALL/dell-inspiron-16-laptop-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300069, '65f023632bc46470c104b75f', 'dell-inspiron-16-laptop-4.jpg', 'dell-inspiron-16-laptop-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300069', 'products/65f023632bc46470c104b75f/ELEC-SKU-149/SMALL/dell-inspiron-16-laptop-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-149/SMALL/dell-inspiron-16-laptop-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300070, '65f023632bc46470c104b75f', 'dell-inspiron-16-laptop-5.jpg', 'dell-inspiron-16-laptop-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300070', 'products/65f023632bc46470c104b75f/ELEC-SKU-149/SMALL/dell-inspiron-16-laptop-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-149/SMALL/dell-inspiron-16-laptop-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300071, '65f023632bc46470c104b75f', 'lg-gram-17-laptop-1.jpg', 'lg-gram-17-laptop-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300071', 'products/65f023632bc46470c104b75f/ELEC-SKU-150/SMALL/lg-gram-17-laptop-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-150/SMALL/lg-gram-17-laptop-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300072, '65f023632bc46470c104b75f', 'lg-gram-17-laptop-2.jpg', 'lg-gram-17-laptop-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300072', 'products/65f023632bc46470c104b75f/ELEC-SKU-150/SMALL/lg-gram-17-laptop-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-150/SMALL/lg-gram-17-laptop-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300073, '65f023632bc46470c104b75f', 'lg-gram-17-laptop-3.jpg', 'lg-gram-17-laptop-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300073', 'products/65f023632bc46470c104b75f/ELEC-SKU-150/SMALL/lg-gram-17-laptop-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-150/SMALL/lg-gram-17-laptop-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300074, '65f023632bc46470c104b75f', 'lg-gram-17-laptop-4.jpg', 'lg-gram-17-laptop-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300074', 'products/65f023632bc46470c104b75f/ELEC-SKU-150/SMALL/lg-gram-17-laptop-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-150/SMALL/lg-gram-17-laptop-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300075, '65f023632bc46470c104b75f', 'lg-gram-17-laptop-5.jpg', 'lg-gram-17-laptop-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300075', 'products/65f023632bc46470c104b75f/ELEC-SKU-150/SMALL/lg-gram-17-laptop-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-150/SMALL/lg-gram-17-laptop-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300076, '65f023632bc46470c104b75f', 'hp-envy-x360-16-laptop-1.jpg', 'hp-envy-x360-16-laptop-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300076', 'products/65f023632bc46470c104b75f/ELEC-SKU-151/SMALL/hp-envy-x360-16-laptop-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-151/SMALL/hp-envy-x360-16-laptop-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300077, '65f023632bc46470c104b75f', 'hp-envy-x360-16-laptop-2.jpg', 'hp-envy-x360-16-laptop-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300077', 'products/65f023632bc46470c104b75f/ELEC-SKU-151/SMALL/hp-envy-x360-16-laptop-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-151/SMALL/hp-envy-x360-16-laptop-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300078, '65f023632bc46470c104b75f', 'hp-envy-x360-16-laptop-3.jpg', 'hp-envy-x360-16-laptop-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300078', 'products/65f023632bc46470c104b75f/ELEC-SKU-151/SMALL/hp-envy-x360-16-laptop-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-151/SMALL/hp-envy-x360-16-laptop-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300079, '65f023632bc46470c104b75f', 'hp-envy-x360-16-laptop-4.jpg', 'hp-envy-x360-16-laptop-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300079', 'products/65f023632bc46470c104b75f/ELEC-SKU-151/SMALL/hp-envy-x360-16-laptop-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-151/SMALL/hp-envy-x360-16-laptop-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300080, '65f023632bc46470c104b75f', 'hp-envy-x360-16-laptop-5.jpg', 'hp-envy-x360-16-laptop-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300080', 'products/65f023632bc46470c104b75f/ELEC-SKU-151/SMALL/hp-envy-x360-16-laptop-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-151/SMALL/hp-envy-x360-16-laptop-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300081, '65f023632bc46470c104b75f', 'apple-tv-4k-2022-1.jpg', 'apple-tv-4k-2022-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300081', 'products/65f023632bc46470c104b75f/ELEC-SKU-152/SMALL/apple-tv-4k-2022-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-152/SMALL/apple-tv-4k-2022-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300082, '65f023632bc46470c104b75f', 'apple-tv-4k-2022-2.jpg', 'apple-tv-4k-2022-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300082', 'products/65f023632bc46470c104b75f/ELEC-SKU-152/SMALL/apple-tv-4k-2022-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-152/SMALL/apple-tv-4k-2022-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300083, '65f023632bc46470c104b75f', 'apple-tv-4k-2022-3.jpg', 'apple-tv-4k-2022-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300083', 'products/65f023632bc46470c104b75f/ELEC-SKU-152/SMALL/apple-tv-4k-2022-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-152/SMALL/apple-tv-4k-2022-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300084, '65f023632bc46470c104b75f', 'apple-tv-4k-2022-4.jpg', 'apple-tv-4k-2022-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300084', 'products/65f023632bc46470c104b75f/ELEC-SKU-152/SMALL/apple-tv-4k-2022-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-152/SMALL/apple-tv-4k-2022-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300085, '65f023632bc46470c104b75f', 'apple-tv-4k-2022-5.jpg', 'apple-tv-4k-2022-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300085', 'products/65f023632bc46470c104b75f/ELEC-SKU-152/SMALL/apple-tv-4k-2022-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-152/SMALL/apple-tv-4k-2022-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300086, '65f023632bc46470c104b75f', 'samsung-the-frame-55-inch-tv-1.jpg', 'samsung-the-frame-55-inch-tv-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300086', 'products/65f023632bc46470c104b75f/ELEC-SKU-153/SMALL/samsung-the-frame-55-inch-tv-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-153/SMALL/samsung-the-frame-55-inch-tv-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300087, '65f023632bc46470c104b75f', 'samsung-the-frame-55-inch-tv-2.jpg', 'samsung-the-frame-55-inch-tv-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300087', 'products/65f023632bc46470c104b75f/ELEC-SKU-153/SMALL/samsung-the-frame-55-inch-tv-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-153/SMALL/samsung-the-frame-55-inch-tv-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300088, '65f023632bc46470c104b75f', 'samsung-the-frame-55-inch-tv-3.jpg', 'samsung-the-frame-55-inch-tv-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300088', 'products/65f023632bc46470c104b75f/ELEC-SKU-153/SMALL/samsung-the-frame-55-inch-tv-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-153/SMALL/samsung-the-frame-55-inch-tv-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300089, '65f023632bc46470c104b75f', 'samsung-the-frame-55-inch-tv-4.jpg', 'samsung-the-frame-55-inch-tv-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300089', 'products/65f023632bc46470c104b75f/ELEC-SKU-153/SMALL/samsung-the-frame-55-inch-tv-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-153/SMALL/samsung-the-frame-55-inch-tv-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300090, '65f023632bc46470c104b75f', 'samsung-the-frame-55-inch-tv-5.jpg', 'samsung-the-frame-55-inch-tv-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300090', 'products/65f023632bc46470c104b75f/ELEC-SKU-153/SMALL/samsung-the-frame-55-inch-tv-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-153/SMALL/samsung-the-frame-55-inch-tv-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300091, '65f023632bc46470c104b75f', 'sony-inzone-h9-gaming-headset-1.jpg', 'sony-inzone-h9-gaming-headset-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300091', 'products/65f023632bc46470c104b75f/ELEC-SKU-154/SMALL/sony-inzone-h9-gaming-headset-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-154/SMALL/sony-inzone-h9-gaming-headset-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300092, '65f023632bc46470c104b75f', 'sony-inzone-h9-gaming-headset-2.jpg', 'sony-inzone-h9-gaming-headset-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300092', 'products/65f023632bc46470c104b75f/ELEC-SKU-154/SMALL/sony-inzone-h9-gaming-headset-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-154/SMALL/sony-inzone-h9-gaming-headset-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300093, '65f023632bc46470c104b75f', 'sony-inzone-h9-gaming-headset-3.jpg', 'sony-inzone-h9-gaming-headset-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300093', 'products/65f023632bc46470c104b75f/ELEC-SKU-154/SMALL/sony-inzone-h9-gaming-headset-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-154/SMALL/sony-inzone-h9-gaming-headset-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300094, '65f023632bc46470c104b75f', 'sony-inzone-h9-gaming-headset-4.jpg', 'sony-inzone-h9-gaming-headset-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300094', 'products/65f023632bc46470c104b75f/ELEC-SKU-154/SMALL/sony-inzone-h9-gaming-headset-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-154/SMALL/sony-inzone-h9-gaming-headset-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300095, '65f023632bc46470c104b75f', 'sony-inzone-h9-gaming-headset-5.jpg', 'sony-inzone-h9-gaming-headset-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300095', 'products/65f023632bc46470c104b75f/ELEC-SKU-154/SMALL/sony-inzone-h9-gaming-headset-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-154/SMALL/sony-inzone-h9-gaming-headset-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300096, '65f023632bc46470c104b75f', 'lg-ultragear-27-oled-monitor-1.jpg', 'lg-ultragear-27-oled-monitor-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300096', 'products/65f023632bc46470c104b75f/ELEC-SKU-155/SMALL/lg-ultragear-27-oled-monitor-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-155/SMALL/lg-ultragear-27-oled-monitor-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300097, '65f023632bc46470c104b75f', 'lg-ultragear-27-oled-monitor-2.jpg', 'lg-ultragear-27-oled-monitor-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300097', 'products/65f023632bc46470c104b75f/ELEC-SKU-155/SMALL/lg-ultragear-27-oled-monitor-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-155/SMALL/lg-ultragear-27-oled-monitor-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300098, '65f023632bc46470c104b75f', 'lg-ultragear-27-oled-monitor-3.jpg', 'lg-ultragear-27-oled-monitor-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300098', 'products/65f023632bc46470c104b75f/ELEC-SKU-155/SMALL/lg-ultragear-27-oled-monitor-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-155/SMALL/lg-ultragear-27-oled-monitor-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300099, '65f023632bc46470c104b75f', 'lg-ultragear-27-oled-monitor-4.jpg', 'lg-ultragear-27-oled-monitor-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300099', 'products/65f023632bc46470c104b75f/ELEC-SKU-155/SMALL/lg-ultragear-27-oled-monitor-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-155/SMALL/lg-ultragear-27-oled-monitor-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300100, '65f023632bc46470c104b75f', 'lg-ultragear-27-oled-monitor-5.jpg', 'lg-ultragear-27-oled-monitor-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300100', 'products/65f023632bc46470c104b75f/ELEC-SKU-155/SMALL/lg-ultragear-27-oled-monitor-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-155/SMALL/lg-ultragear-27-oled-monitor-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300101, '65f023632bc46470c104b75f', 'alienware-m16-gaming-laptop-1.jpg', 'alienware-m16-gaming-laptop-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300101', 'products/65f023632bc46470c104b75f/ELEC-SKU-156/SMALL/alienware-m16-gaming-laptop-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-156/SMALL/alienware-m16-gaming-laptop-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300102, '65f023632bc46470c104b75f', 'alienware-m16-gaming-laptop-2.jpg', 'alienware-m16-gaming-laptop-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300102', 'products/65f023632bc46470c104b75f/ELEC-SKU-156/SMALL/alienware-m16-gaming-laptop-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-156/SMALL/alienware-m16-gaming-laptop-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300103, '65f023632bc46470c104b75f', 'alienware-m16-gaming-laptop-3.jpg', 'alienware-m16-gaming-laptop-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300103', 'products/65f023632bc46470c104b75f/ELEC-SKU-156/SMALL/alienware-m16-gaming-laptop-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-156/SMALL/alienware-m16-gaming-laptop-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300104, '65f023632bc46470c104b75f', 'alienware-m16-gaming-laptop-4.jpg', 'alienware-m16-gaming-laptop-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300104', 'products/65f023632bc46470c104b75f/ELEC-SKU-156/SMALL/alienware-m16-gaming-laptop-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-156/SMALL/alienware-m16-gaming-laptop-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300105, '65f023632bc46470c104b75f', 'alienware-m16-gaming-laptop-5.jpg', 'alienware-m16-gaming-laptop-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300105', 'products/65f023632bc46470c104b75f/ELEC-SKU-156/SMALL/alienware-m16-gaming-laptop-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-156/SMALL/alienware-m16-gaming-laptop-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300106, '65f023632bc46470c104b75f', 'omen-gaming-laptop-16-1.jpg', 'omen-gaming-laptop-16-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300106', 'products/65f023632bc46470c104b75f/ELEC-SKU-157/SMALL/omen-gaming-laptop-16-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-157/SMALL/omen-gaming-laptop-16-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300107, '65f023632bc46470c104b75f', 'omen-gaming-laptop-16-2.jpg', 'omen-gaming-laptop-16-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300107', 'products/65f023632bc46470c104b75f/ELEC-SKU-157/SMALL/omen-gaming-laptop-16-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-157/SMALL/omen-gaming-laptop-16-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300108, '65f023632bc46470c104b75f', 'omen-gaming-laptop-16-3.jpg', 'omen-gaming-laptop-16-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300108', 'products/65f023632bc46470c104b75f/ELEC-SKU-157/SMALL/omen-gaming-laptop-16-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-157/SMALL/omen-gaming-laptop-16-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300109, '65f023632bc46470c104b75f', 'omen-gaming-laptop-16-4.jpg', 'omen-gaming-laptop-16-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300109', 'products/65f023632bc46470c104b75f/ELEC-SKU-157/SMALL/omen-gaming-laptop-16-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-157/SMALL/omen-gaming-laptop-16-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300110, '65f023632bc46470c104b75f', 'omen-gaming-laptop-16-5.jpg', 'omen-gaming-laptop-16-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300110', 'products/65f023632bc46470c104b75f/ELEC-SKU-157/SMALL/omen-gaming-laptop-16-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-157/SMALL/omen-gaming-laptop-16-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300111, '65f023632bc46470c104b75f', 'apple-studio-display-1.jpg', 'apple-studio-display-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300111', 'products/65f023632bc46470c104b75f/ELEC-SKU-158/SMALL/apple-studio-display-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-158/SMALL/apple-studio-display-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300112, '65f023632bc46470c104b75f', 'apple-studio-display-2.jpg', 'apple-studio-display-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300112', 'products/65f023632bc46470c104b75f/ELEC-SKU-158/SMALL/apple-studio-display-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-158/SMALL/apple-studio-display-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300113, '65f023632bc46470c104b75f', 'apple-studio-display-3.jpg', 'apple-studio-display-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300113', 'products/65f023632bc46470c104b75f/ELEC-SKU-158/SMALL/apple-studio-display-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-158/SMALL/apple-studio-display-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300114, '65f023632bc46470c104b75f', 'apple-studio-display-4.jpg', 'apple-studio-display-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300114', 'products/65f023632bc46470c104b75f/ELEC-SKU-158/SMALL/apple-studio-display-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-158/SMALL/apple-studio-display-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300115, '65f023632bc46470c104b75f', 'apple-studio-display-5.jpg', 'apple-studio-display-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300115', 'products/65f023632bc46470c104b75f/ELEC-SKU-158/SMALL/apple-studio-display-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-158/SMALL/apple-studio-display-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300116, '65f023632bc46470c104b75f', 'samsung-galaxy-book4-pro-1.jpg', 'samsung-galaxy-book4-pro-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300116', 'products/65f023632bc46470c104b75f/ELEC-SKU-159/SMALL/samsung-galaxy-book4-pro-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-159/SMALL/samsung-galaxy-book4-pro-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300117, '65f023632bc46470c104b75f', 'samsung-galaxy-book4-pro-2.jpg', 'samsung-galaxy-book4-pro-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300117', 'products/65f023632bc46470c104b75f/ELEC-SKU-159/SMALL/samsung-galaxy-book4-pro-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-159/SMALL/samsung-galaxy-book4-pro-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300118, '65f023632bc46470c104b75f', 'samsung-galaxy-book4-pro-3.jpg', 'samsung-galaxy-book4-pro-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300118', 'products/65f023632bc46470c104b75f/ELEC-SKU-159/SMALL/samsung-galaxy-book4-pro-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-159/SMALL/samsung-galaxy-book4-pro-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300119, '65f023632bc46470c104b75f', 'samsung-galaxy-book4-pro-4.jpg', 'samsung-galaxy-book4-pro-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300119', 'products/65f023632bc46470c104b75f/ELEC-SKU-159/SMALL/samsung-galaxy-book4-pro-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-159/SMALL/samsung-galaxy-book4-pro-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300120, '65f023632bc46470c104b75f', 'samsung-galaxy-book4-pro-5.jpg', 'samsung-galaxy-book4-pro-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300120', 'products/65f023632bc46470c104b75f/ELEC-SKU-159/SMALL/samsung-galaxy-book4-pro-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-159/SMALL/samsung-galaxy-book4-pro-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300121, '65f023632bc46470c104b75f', 'sony-ht-a5000-soundbar-1.jpg', 'sony-ht-a5000-soundbar-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300121', 'products/65f023632bc46470c104b75f/ELEC-SKU-160/SMALL/sony-ht-a5000-soundbar-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-160/SMALL/sony-ht-a5000-soundbar-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300122, '65f023632bc46470c104b75f', 'sony-ht-a5000-soundbar-2.jpg', 'sony-ht-a5000-soundbar-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300122', 'products/65f023632bc46470c104b75f/ELEC-SKU-160/SMALL/sony-ht-a5000-soundbar-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-160/SMALL/sony-ht-a5000-soundbar-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300123, '65f023632bc46470c104b75f', 'sony-ht-a5000-soundbar-3.jpg', 'sony-ht-a5000-soundbar-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300123', 'products/65f023632bc46470c104b75f/ELEC-SKU-160/SMALL/sony-ht-a5000-soundbar-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-160/SMALL/sony-ht-a5000-soundbar-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300124, '65f023632bc46470c104b75f', 'sony-ht-a5000-soundbar-4.jpg', 'sony-ht-a5000-soundbar-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300124', 'products/65f023632bc46470c104b75f/ELEC-SKU-160/SMALL/sony-ht-a5000-soundbar-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-160/SMALL/sony-ht-a5000-soundbar-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300125, '65f023632bc46470c104b75f', 'sony-ht-a5000-soundbar-5.jpg', 'sony-ht-a5000-soundbar-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300125', 'products/65f023632bc46470c104b75f/ELEC-SKU-160/SMALL/sony-ht-a5000-soundbar-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-160/SMALL/sony-ht-a5000-soundbar-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300126, '65f023632bc46470c104b75f', 'lg-tone-free-fp9-earbuds-1.jpg', 'lg-tone-free-fp9-earbuds-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300126', 'products/65f023632bc46470c104b75f/ELEC-SKU-161/SMALL/lg-tone-free-fp9-earbuds-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-161/SMALL/lg-tone-free-fp9-earbuds-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300127, '65f023632bc46470c104b75f', 'lg-tone-free-fp9-earbuds-2.jpg', 'lg-tone-free-fp9-earbuds-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300127', 'products/65f023632bc46470c104b75f/ELEC-SKU-161/SMALL/lg-tone-free-fp9-earbuds-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-161/SMALL/lg-tone-free-fp9-earbuds-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300128, '65f023632bc46470c104b75f', 'lg-tone-free-fp9-earbuds-3.jpg', 'lg-tone-free-fp9-earbuds-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300128', 'products/65f023632bc46470c104b75f/ELEC-SKU-161/SMALL/lg-tone-free-fp9-earbuds-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-161/SMALL/lg-tone-free-fp9-earbuds-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300129, '65f023632bc46470c104b75f', 'lg-tone-free-fp9-earbuds-4.jpg', 'lg-tone-free-fp9-earbuds-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300129', 'products/65f023632bc46470c104b75f/ELEC-SKU-161/SMALL/lg-tone-free-fp9-earbuds-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-161/SMALL/lg-tone-free-fp9-earbuds-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300130, '65f023632bc46470c104b75f', 'lg-tone-free-fp9-earbuds-5.jpg', 'lg-tone-free-fp9-earbuds-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300130', 'products/65f023632bc46470c104b75f/ELEC-SKU-161/SMALL/lg-tone-free-fp9-earbuds-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-161/SMALL/lg-tone-free-fp9-earbuds-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300131, '65f023632bc46470c104b75f', 'dell-g15-gaming-laptop-1.jpg', 'dell-g15-gaming-laptop-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300131', 'products/65f023632bc46470c104b75f/ELEC-SKU-162/SMALL/dell-g15-gaming-laptop-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-162/SMALL/dell-g15-gaming-laptop-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300132, '65f023632bc46470c104b75f', 'dell-g15-gaming-laptop-2.jpg', 'dell-g15-gaming-laptop-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300132', 'products/65f023632bc46470c104b75f/ELEC-SKU-162/SMALL/dell-g15-gaming-laptop-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-162/SMALL/dell-g15-gaming-laptop-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300133, '65f023632bc46470c104b75f', 'dell-g15-gaming-laptop-3.jpg', 'dell-g15-gaming-laptop-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300133', 'products/65f023632bc46470c104b75f/ELEC-SKU-162/SMALL/dell-g15-gaming-laptop-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-162/SMALL/dell-g15-gaming-laptop-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300134, '65f023632bc46470c104b75f', 'dell-g15-gaming-laptop-4.jpg', 'dell-g15-gaming-laptop-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300134', 'products/65f023632bc46470c104b75f/ELEC-SKU-162/SMALL/dell-g15-gaming-laptop-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-162/SMALL/dell-g15-gaming-laptop-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300135, '65f023632bc46470c104b75f', 'dell-g15-gaming-laptop-5.jpg', 'dell-g15-gaming-laptop-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300135', 'products/65f023632bc46470c104b75f/ELEC-SKU-162/SMALL/dell-g15-gaming-laptop-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-162/SMALL/dell-g15-gaming-laptop-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300136, '65f023632bc46470c104b75f', 'hp-pavilion-plus-14-laptop-1.jpg', 'hp-pavilion-plus-14-laptop-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300136', 'products/65f023632bc46470c104b75f/ELEC-SKU-163/SMALL/hp-pavilion-plus-14-laptop-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-163/SMALL/hp-pavilion-plus-14-laptop-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300137, '65f023632bc46470c104b75f', 'hp-pavilion-plus-14-laptop-2.jpg', 'hp-pavilion-plus-14-laptop-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300137', 'products/65f023632bc46470c104b75f/ELEC-SKU-163/SMALL/hp-pavilion-plus-14-laptop-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-163/SMALL/hp-pavilion-plus-14-laptop-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300138, '65f023632bc46470c104b75f', 'hp-pavilion-plus-14-laptop-3.jpg', 'hp-pavilion-plus-14-laptop-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300138', 'products/65f023632bc46470c104b75f/ELEC-SKU-163/SMALL/hp-pavilion-plus-14-laptop-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-163/SMALL/hp-pavilion-plus-14-laptop-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300139, '65f023632bc46470c104b75f', 'hp-pavilion-plus-14-laptop-4.jpg', 'hp-pavilion-plus-14-laptop-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300139', 'products/65f023632bc46470c104b75f/ELEC-SKU-163/SMALL/hp-pavilion-plus-14-laptop-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-163/SMALL/hp-pavilion-plus-14-laptop-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300140, '65f023632bc46470c104b75f', 'hp-pavilion-plus-14-laptop-5.jpg', 'hp-pavilion-plus-14-laptop-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300140', 'products/65f023632bc46470c104b75f/ELEC-SKU-163/SMALL/hp-pavilion-plus-14-laptop-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-163/SMALL/hp-pavilion-plus-14-laptop-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300141, '65f023632bc46470c104b75f', 'beats-studio-pro-headphones-1.jpg', 'beats-studio-pro-headphones-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300141', 'products/65f023632bc46470c104b75f/ELEC-SKU-164/SMALL/beats-studio-pro-headphones-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-164/SMALL/beats-studio-pro-headphones-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300142, '65f023632bc46470c104b75f', 'beats-studio-pro-headphones-2.jpg', 'beats-studio-pro-headphones-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300142', 'products/65f023632bc46470c104b75f/ELEC-SKU-164/SMALL/beats-studio-pro-headphones-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-164/SMALL/beats-studio-pro-headphones-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300143, '65f023632bc46470c104b75f', 'beats-studio-pro-headphones-3.jpg', 'beats-studio-pro-headphones-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300143', 'products/65f023632bc46470c104b75f/ELEC-SKU-164/SMALL/beats-studio-pro-headphones-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-164/SMALL/beats-studio-pro-headphones-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300144, '65f023632bc46470c104b75f', 'beats-studio-pro-headphones-4.jpg', 'beats-studio-pro-headphones-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300144', 'products/65f023632bc46470c104b75f/ELEC-SKU-164/SMALL/beats-studio-pro-headphones-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-164/SMALL/beats-studio-pro-headphones-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300145, '65f023632bc46470c104b75f', 'beats-studio-pro-headphones-5.jpg', 'beats-studio-pro-headphones-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300145', 'products/65f023632bc46470c104b75f/ELEC-SKU-164/SMALL/beats-studio-pro-headphones-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-164/SMALL/beats-studio-pro-headphones-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300146, '65f023632bc46470c104b75f', 'samsung-galaxy-s23-fe-1.jpg', 'samsung-galaxy-s23-fe-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300146', 'products/65f023632bc46470c104b75f/ELEC-SKU-165/SMALL/samsung-galaxy-s23-fe-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-165/SMALL/samsung-galaxy-s23-fe-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300147, '65f023632bc46470c104b75f', 'samsung-galaxy-s23-fe-2.jpg', 'samsung-galaxy-s23-fe-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300147', 'products/65f023632bc46470c104b75f/ELEC-SKU-165/SMALL/samsung-galaxy-s23-fe-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-165/SMALL/samsung-galaxy-s23-fe-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300148, '65f023632bc46470c104b75f', 'samsung-galaxy-s23-fe-3.jpg', 'samsung-galaxy-s23-fe-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300148', 'products/65f023632bc46470c104b75f/ELEC-SKU-165/SMALL/samsung-galaxy-s23-fe-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-165/SMALL/samsung-galaxy-s23-fe-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300149, '65f023632bc46470c104b75f', 'samsung-galaxy-s23-fe-4.jpg', 'samsung-galaxy-s23-fe-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300149', 'products/65f023632bc46470c104b75f/ELEC-SKU-165/SMALL/samsung-galaxy-s23-fe-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-165/SMALL/samsung-galaxy-s23-fe-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300150, '65f023632bc46470c104b75f', 'samsung-galaxy-s23-fe-5.jpg', 'samsung-galaxy-s23-fe-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300150', 'products/65f023632bc46470c104b75f/ELEC-SKU-165/SMALL/samsung-galaxy-s23-fe-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-165/SMALL/samsung-galaxy-s23-fe-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300151, '65f023632bc46470c104b75f', 'lg-oled-evo-c3-55-inch-tv-1.jpg', 'lg-oled-evo-c3-55-inch-tv-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300151', 'products/65f023632bc46470c104b75f/ELEC-SKU-166/SMALL/lg-oled-evo-c3-55-inch-tv-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-166/SMALL/lg-oled-evo-c3-55-inch-tv-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300152, '65f023632bc46470c104b75f', 'lg-oled-evo-c3-55-inch-tv-2.jpg', 'lg-oled-evo-c3-55-inch-tv-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300152', 'products/65f023632bc46470c104b75f/ELEC-SKU-166/SMALL/lg-oled-evo-c3-55-inch-tv-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-166/SMALL/lg-oled-evo-c3-55-inch-tv-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300153, '65f023632bc46470c104b75f', 'lg-oled-evo-c3-55-inch-tv-3.jpg', 'lg-oled-evo-c3-55-inch-tv-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300153', 'products/65f023632bc46470c104b75f/ELEC-SKU-166/SMALL/lg-oled-evo-c3-55-inch-tv-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-166/SMALL/lg-oled-evo-c3-55-inch-tv-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300154, '65f023632bc46470c104b75f', 'lg-oled-evo-c3-55-inch-tv-4.jpg', 'lg-oled-evo-c3-55-inch-tv-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300154', 'products/65f023632bc46470c104b75f/ELEC-SKU-166/SMALL/lg-oled-evo-c3-55-inch-tv-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-166/SMALL/lg-oled-evo-c3-55-inch-tv-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300155, '65f023632bc46470c104b75f', 'lg-oled-evo-c3-55-inch-tv-5.jpg', 'lg-oled-evo-c3-55-inch-tv-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300155', 'products/65f023632bc46470c104b75f/ELEC-SKU-166/SMALL/lg-oled-evo-c3-55-inch-tv-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-166/SMALL/lg-oled-evo-c3-55-inch-tv-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300156, '65f023632bc46470c104b75f', 'dell-ultrasharp-27-4k-monitor-u2723qe-1.jpg', 'dell-ultrasharp-27-4k-monitor-u2723qe-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300156', 'products/65f023632bc46470c104b75f/ELEC-SKU-167/SMALL/dell-ultrasharp-27-4k-monitor-u2723qe-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-167/SMALL/dell-ultrasharp-27-4k-monitor-u2723qe-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300157, '65f023632bc46470c104b75f', 'dell-ultrasharp-27-4k-monitor-u2723qe-2.jpg', 'dell-ultrasharp-27-4k-monitor-u2723qe-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300157', 'products/65f023632bc46470c104b75f/ELEC-SKU-167/SMALL/dell-ultrasharp-27-4k-monitor-u2723qe-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-167/SMALL/dell-ultrasharp-27-4k-monitor-u2723qe-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300158, '65f023632bc46470c104b75f', 'dell-ultrasharp-27-4k-monitor-u2723qe-3.jpg', 'dell-ultrasharp-27-4k-monitor-u2723qe-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300158', 'products/65f023632bc46470c104b75f/ELEC-SKU-167/SMALL/dell-ultrasharp-27-4k-monitor-u2723qe-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-167/SMALL/dell-ultrasharp-27-4k-monitor-u2723qe-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300159, '65f023632bc46470c104b75f', 'dell-ultrasharp-27-4k-monitor-u2723qe-4.jpg', 'dell-ultrasharp-27-4k-monitor-u2723qe-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300159', 'products/65f023632bc46470c104b75f/ELEC-SKU-167/SMALL/dell-ultrasharp-27-4k-monitor-u2723qe-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-167/SMALL/dell-ultrasharp-27-4k-monitor-u2723qe-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300160, '65f023632bc46470c104b75f', 'dell-ultrasharp-27-4k-monitor-u2723qe-5.jpg', 'dell-ultrasharp-27-4k-monitor-u2723qe-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300160', 'products/65f023632bc46470c104b75f/ELEC-SKU-167/SMALL/dell-ultrasharp-27-4k-monitor-u2723qe-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-167/SMALL/dell-ultrasharp-27-4k-monitor-u2723qe-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300161, '65f023632bc46470c104b75f', 'sony-wf-1000xm5-earbuds-1.jpg', 'sony-wf-1000xm5-earbuds-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300161', 'products/65f023632bc46470c104b75f/ELEC-SKU-168/SMALL/sony-wf-1000xm5-earbuds-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-168/SMALL/sony-wf-1000xm5-earbuds-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300162, '65f023632bc46470c104b75f', 'sony-wf-1000xm5-earbuds-2.jpg', 'sony-wf-1000xm5-earbuds-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300162', 'products/65f023632bc46470c104b75f/ELEC-SKU-168/SMALL/sony-wf-1000xm5-earbuds-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-168/SMALL/sony-wf-1000xm5-earbuds-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300163, '65f023632bc46470c104b75f', 'sony-wf-1000xm5-earbuds-3.jpg', 'sony-wf-1000xm5-earbuds-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300163', 'products/65f023632bc46470c104b75f/ELEC-SKU-168/SMALL/sony-wf-1000xm5-earbuds-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-168/SMALL/sony-wf-1000xm5-earbuds-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300164, '65f023632bc46470c104b75f', 'sony-wf-1000xm5-earbuds-4.jpg', 'sony-wf-1000xm5-earbuds-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300164', 'products/65f023632bc46470c104b75f/ELEC-SKU-168/SMALL/sony-wf-1000xm5-earbuds-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-168/SMALL/sony-wf-1000xm5-earbuds-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300165, '65f023632bc46470c104b75f', 'sony-wf-1000xm5-earbuds-5.jpg', 'sony-wf-1000xm5-earbuds-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300165', 'products/65f023632bc46470c104b75f/ELEC-SKU-168/SMALL/sony-wf-1000xm5-earbuds-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-168/SMALL/sony-wf-1000xm5-earbuds-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300166, '65f023632bc46470c104b75f', 'apple-watch-series-9-1.jpg', 'apple-watch-series-9-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300166', 'products/65f023632bc46470c104b75f/ELEC-SKU-169/SMALL/apple-watch-series-9-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-169/SMALL/apple-watch-series-9-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300167, '65f023632bc46470c104b75f', 'apple-watch-series-9-2.jpg', 'apple-watch-series-9-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300167', 'products/65f023632bc46470c104b75f/ELEC-SKU-169/SMALL/apple-watch-series-9-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-169/SMALL/apple-watch-series-9-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300168, '65f023632bc46470c104b75f', 'apple-watch-series-9-3.jpg', 'apple-watch-series-9-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300168', 'products/65f023632bc46470c104b75f/ELEC-SKU-169/SMALL/apple-watch-series-9-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-169/SMALL/apple-watch-series-9-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300169, '65f023632bc46470c104b75f', 'apple-watch-series-9-4.jpg', 'apple-watch-series-9-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300169', 'products/65f023632bc46470c104b75f/ELEC-SKU-169/SMALL/apple-watch-series-9-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-169/SMALL/apple-watch-series-9-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300170, '65f023632bc46470c104b75f', 'apple-watch-series-9-5.jpg', 'apple-watch-series-9-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300170', 'products/65f023632bc46470c104b75f/ELEC-SKU-169/SMALL/apple-watch-series-9-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-169/SMALL/apple-watch-series-9-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300171, '65f023632bc46470c104b75f', 'samsung-galaxy-tab-s9-1.jpg', 'samsung-galaxy-tab-s9-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300171', 'products/65f023632bc46470c104b75f/ELEC-SKU-170/SMALL/samsung-galaxy-tab-s9-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-170/SMALL/samsung-galaxy-tab-s9-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300172, '65f023632bc46470c104b75f', 'samsung-galaxy-tab-s9-2.jpg', 'samsung-galaxy-tab-s9-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300172', 'products/65f023632bc46470c104b75f/ELEC-SKU-170/SMALL/samsung-galaxy-tab-s9-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-170/SMALL/samsung-galaxy-tab-s9-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300173, '65f023632bc46470c104b75f', 'samsung-galaxy-tab-s9-3.jpg', 'samsung-galaxy-tab-s9-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300173', 'products/65f023632bc46470c104b75f/ELEC-SKU-170/SMALL/samsung-galaxy-tab-s9-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-170/SMALL/samsung-galaxy-tab-s9-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300174, '65f023632bc46470c104b75f', 'samsung-galaxy-tab-s9-4.jpg', 'samsung-galaxy-tab-s9-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300174', 'products/65f023632bc46470c104b75f/ELEC-SKU-170/SMALL/samsung-galaxy-tab-s9-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-170/SMALL/samsung-galaxy-tab-s9-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300175, '65f023632bc46470c104b75f', 'samsung-galaxy-tab-s9-5.jpg', 'samsung-galaxy-tab-s9-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300175', 'products/65f023632bc46470c104b75f/ELEC-SKU-170/SMALL/samsung-galaxy-tab-s9-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-170/SMALL/samsung-galaxy-tab-s9-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300176, '65f023632bc46470c104b75f', 'hp-chromebook-x360-14c-1.jpg', 'hp-chromebook-x360-14c-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300176', 'products/65f023632bc46470c104b75f/ELEC-SKU-171/SMALL/hp-chromebook-x360-14c-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-171/SMALL/hp-chromebook-x360-14c-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300177, '65f023632bc46470c104b75f', 'hp-chromebook-x360-14c-2.jpg', 'hp-chromebook-x360-14c-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300177', 'products/65f023632bc46470c104b75f/ELEC-SKU-171/SMALL/hp-chromebook-x360-14c-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-171/SMALL/hp-chromebook-x360-14c-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300178, '65f023632bc46470c104b75f', 'hp-chromebook-x360-14c-3.jpg', 'hp-chromebook-x360-14c-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300178', 'products/65f023632bc46470c104b75f/ELEC-SKU-171/SMALL/hp-chromebook-x360-14c-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-171/SMALL/hp-chromebook-x360-14c-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300179, '65f023632bc46470c104b75f', 'hp-chromebook-x360-14c-4.jpg', 'hp-chromebook-x360-14c-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300179', 'products/65f023632bc46470c104b75f/ELEC-SKU-171/SMALL/hp-chromebook-x360-14c-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-171/SMALL/hp-chromebook-x360-14c-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300180, '65f023632bc46470c104b75f', 'hp-chromebook-x360-14c-5.jpg', 'hp-chromebook-x360-14c-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300180', 'products/65f023632bc46470c104b75f/ELEC-SKU-171/SMALL/hp-chromebook-x360-14c-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-171/SMALL/hp-chromebook-x360-14c-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300181, '65f023632bc46470c104b75f', 'samsung-galaxy-z-fold5-1.jpg', 'samsung-galaxy-z-fold5-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300181', 'products/65f023632bc46470c104b75f/ELEC-SKU-172/SMALL/samsung-galaxy-z-fold5-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-172/SMALL/samsung-galaxy-z-fold5-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300182, '65f023632bc46470c104b75f', 'samsung-galaxy-z-fold5-2.jpg', 'samsung-galaxy-z-fold5-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300182', 'products/65f023632bc46470c104b75f/ELEC-SKU-172/SMALL/samsung-galaxy-z-fold5-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-172/SMALL/samsung-galaxy-z-fold5-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300183, '65f023632bc46470c104b75f', 'samsung-galaxy-z-fold5-3.jpg', 'samsung-galaxy-z-fold5-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300183', 'products/65f023632bc46470c104b75f/ELEC-SKU-172/SMALL/samsung-galaxy-z-fold5-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-172/SMALL/samsung-galaxy-z-fold5-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300184, '65f023632bc46470c104b75f', 'samsung-galaxy-z-fold5-4.jpg', 'samsung-galaxy-z-fold5-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300184', 'products/65f023632bc46470c104b75f/ELEC-SKU-172/SMALL/samsung-galaxy-z-fold5-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-172/SMALL/samsung-galaxy-z-fold5-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300185, '65f023632bc46470c104b75f', 'samsung-galaxy-z-fold5-5.jpg', 'samsung-galaxy-z-fold5-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300185', 'products/65f023632bc46470c104b75f/ELEC-SKU-172/SMALL/samsung-galaxy-z-fold5-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-172/SMALL/samsung-galaxy-z-fold5-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300186, '65f023632bc46470c104b75f', 'lg-ultrafine-32-4k-monitor-32uq85r-w-1.jpg', 'lg-ultrafine-32-4k-monitor-32uq85r-w-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300186', 'products/65f023632bc46470c104b75f/ELEC-SKU-173/SMALL/lg-ultrafine-32-4k-monitor-32uq85r-w-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-173/SMALL/lg-ultrafine-32-4k-monitor-32uq85r-w-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300187, '65f023632bc46470c104b75f', 'lg-ultrafine-32-4k-monitor-32uq85r-w-2.jpg', 'lg-ultrafine-32-4k-monitor-32uq85r-w-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300187', 'products/65f023632bc46470c104b75f/ELEC-SKU-173/SMALL/lg-ultrafine-32-4k-monitor-32uq85r-w-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-173/SMALL/lg-ultrafine-32-4k-monitor-32uq85r-w-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300188, '65f023632bc46470c104b75f', 'lg-ultrafine-32-4k-monitor-32uq85r-w-3.jpg', 'lg-ultrafine-32-4k-monitor-32uq85r-w-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300188', 'products/65f023632bc46470c104b75f/ELEC-SKU-173/SMALL/lg-ultrafine-32-4k-monitor-32uq85r-w-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-173/SMALL/lg-ultrafine-32-4k-monitor-32uq85r-w-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300189, '65f023632bc46470c104b75f', 'lg-ultrafine-32-4k-monitor-32uq85r-w-4.jpg', 'lg-ultrafine-32-4k-monitor-32uq85r-w-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300189', 'products/65f023632bc46470c104b75f/ELEC-SKU-173/SMALL/lg-ultrafine-32-4k-monitor-32uq85r-w-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-173/SMALL/lg-ultrafine-32-4k-monitor-32uq85r-w-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300190, '65f023632bc46470c104b75f', 'lg-ultrafine-32-4k-monitor-32uq85r-w-5.jpg', 'lg-ultrafine-32-4k-monitor-32uq85r-w-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300190', 'products/65f023632bc46470c104b75f/ELEC-SKU-173/SMALL/lg-ultrafine-32-4k-monitor-32uq85r-w-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-173/SMALL/lg-ultrafine-32-4k-monitor-32uq85r-w-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300191, '65f023632bc46470c104b75f', 'apple-ipad-air-11-m2-1.jpg', 'apple-ipad-air-11-m2-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300191', 'products/65f023632bc46470c104b75f/ELEC-SKU-174/SMALL/apple-ipad-air-11-m2-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-174/SMALL/apple-ipad-air-11-m2-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300192, '65f023632bc46470c104b75f', 'apple-ipad-air-11-m2-2.jpg', 'apple-ipad-air-11-m2-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300192', 'products/65f023632bc46470c104b75f/ELEC-SKU-174/SMALL/apple-ipad-air-11-m2-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-174/SMALL/apple-ipad-air-11-m2-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300193, '65f023632bc46470c104b75f', 'apple-ipad-air-11-m2-3.jpg', 'apple-ipad-air-11-m2-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300193', 'products/65f023632bc46470c104b75f/ELEC-SKU-174/SMALL/apple-ipad-air-11-m2-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-174/SMALL/apple-ipad-air-11-m2-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300194, '65f023632bc46470c104b75f', 'apple-ipad-air-11-m2-4.jpg', 'apple-ipad-air-11-m2-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300194', 'products/65f023632bc46470c104b75f/ELEC-SKU-174/SMALL/apple-ipad-air-11-m2-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-174/SMALL/apple-ipad-air-11-m2-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300195, '65f023632bc46470c104b75f', 'apple-ipad-air-11-m2-5.jpg', 'apple-ipad-air-11-m2-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300195', 'products/65f023632bc46470c104b75f/ELEC-SKU-174/SMALL/apple-ipad-air-11-m2-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-174/SMALL/apple-ipad-air-11-m2-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300196, '65f023632bc46470c104b75f', 'sony-bravia-7-65-inch-qled-tv-1.jpg', 'sony-bravia-7-65-inch-qled-tv-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300196', 'products/65f023632bc46470c104b75f/ELEC-SKU-175/SMALL/sony-bravia-7-65-inch-qled-tv-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-175/SMALL/sony-bravia-7-65-inch-qled-tv-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300197, '65f023632bc46470c104b75f', 'sony-bravia-7-65-inch-qled-tv-2.jpg', 'sony-bravia-7-65-inch-qled-tv-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300197', 'products/65f023632bc46470c104b75f/ELEC-SKU-175/SMALL/sony-bravia-7-65-inch-qled-tv-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-175/SMALL/sony-bravia-7-65-inch-qled-tv-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300198, '65f023632bc46470c104b75f', 'sony-bravia-7-65-inch-qled-tv-3.jpg', 'sony-bravia-7-65-inch-qled-tv-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300198', 'products/65f023632bc46470c104b75f/ELEC-SKU-175/SMALL/sony-bravia-7-65-inch-qled-tv-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-175/SMALL/sony-bravia-7-65-inch-qled-tv-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300199, '65f023632bc46470c104b75f', 'sony-bravia-7-65-inch-qled-tv-4.jpg', 'sony-bravia-7-65-inch-qled-tv-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300199', 'products/65f023632bc46470c104b75f/ELEC-SKU-175/SMALL/sony-bravia-7-65-inch-qled-tv-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-175/SMALL/sony-bravia-7-65-inch-qled-tv-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300200, '65f023632bc46470c104b75f', 'sony-bravia-7-65-inch-qled-tv-5.jpg', 'sony-bravia-7-65-inch-qled-tv-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300200', 'products/65f023632bc46470c104b75f/ELEC-SKU-175/SMALL/sony-bravia-7-65-inch-qled-tv-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-175/SMALL/sony-bravia-7-65-inch-qled-tv-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300201, '65f023632bc46470c104b75f', 'dell-latitude-7440-laptop-1.jpg', 'dell-latitude-7440-laptop-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300201', 'products/65f023632bc46470c104b75f/ELEC-SKU-176/SMALL/dell-latitude-7440-laptop-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-176/SMALL/dell-latitude-7440-laptop-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300202, '65f023632bc46470c104b75f', 'dell-latitude-7440-laptop-2.jpg', 'dell-latitude-7440-laptop-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300202', 'products/65f023632bc46470c104b75f/ELEC-SKU-176/SMALL/dell-latitude-7440-laptop-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-176/SMALL/dell-latitude-7440-laptop-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300203, '65f023632bc46470c104b75f', 'dell-latitude-7440-laptop-3.jpg', 'dell-latitude-7440-laptop-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300203', 'products/65f023632bc46470c104b75f/ELEC-SKU-176/SMALL/dell-latitude-7440-laptop-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-176/SMALL/dell-latitude-7440-laptop-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300204, '65f023632bc46470c104b75f', 'dell-latitude-7440-laptop-4.jpg', 'dell-latitude-7440-laptop-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300204', 'products/65f023632bc46470c104b75f/ELEC-SKU-176/SMALL/dell-latitude-7440-laptop-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-176/SMALL/dell-latitude-7440-laptop-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300205, '65f023632bc46470c104b75f', 'dell-latitude-7440-laptop-5.jpg', 'dell-latitude-7440-laptop-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300205', 'products/65f023632bc46470c104b75f/ELEC-SKU-176/SMALL/dell-latitude-7440-laptop-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-176/SMALL/dell-latitude-7440-laptop-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300206, '65f023632bc46470c104b75f', 'samsung-galaxy-watch6-1.jpg', 'samsung-galaxy-watch6-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300206', 'products/65f023632bc46470c104b75f/ELEC-SKU-177/SMALL/samsung-galaxy-watch6-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-177/SMALL/samsung-galaxy-watch6-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300207, '65f023632bc46470c104b75f', 'samsung-galaxy-watch6-2.jpg', 'samsung-galaxy-watch6-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300207', 'products/65f023632bc46470c104b75f/ELEC-SKU-177/SMALL/samsung-galaxy-watch6-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-177/SMALL/samsung-galaxy-watch6-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300208, '65f023632bc46470c104b75f', 'samsung-galaxy-watch6-3.jpg', 'samsung-galaxy-watch6-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300208', 'products/65f023632bc46470c104b75f/ELEC-SKU-177/SMALL/samsung-galaxy-watch6-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-177/SMALL/samsung-galaxy-watch6-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300209, '65f023632bc46470c104b75f', 'samsung-galaxy-watch6-4.jpg', 'samsung-galaxy-watch6-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300209', 'products/65f023632bc46470c104b75f/ELEC-SKU-177/SMALL/samsung-galaxy-watch6-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-177/SMALL/samsung-galaxy-watch6-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300210, '65f023632bc46470c104b75f', 'samsung-galaxy-watch6-5.jpg', 'samsung-galaxy-watch6-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300210', 'products/65f023632bc46470c104b75f/ELEC-SKU-177/SMALL/samsung-galaxy-watch6-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-177/SMALL/samsung-galaxy-watch6-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300211, '65f023632bc46470c104b75f', 'beats-fit-pro-earbuds-1.jpg', 'beats-fit-pro-earbuds-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300211', 'products/65f023632bc46470c104b75f/ELEC-SKU-178/SMALL/beats-fit-pro-earbuds-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-178/SMALL/beats-fit-pro-earbuds-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300212, '65f023632bc46470c104b75f', 'beats-fit-pro-earbuds-2.jpg', 'beats-fit-pro-earbuds-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300212', 'products/65f023632bc46470c104b75f/ELEC-SKU-178/SMALL/beats-fit-pro-earbuds-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-178/SMALL/beats-fit-pro-earbuds-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300213, '65f023632bc46470c104b75f', 'beats-fit-pro-earbuds-3.jpg', 'beats-fit-pro-earbuds-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300213', 'products/65f023632bc46470c104b75f/ELEC-SKU-178/SMALL/beats-fit-pro-earbuds-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-178/SMALL/beats-fit-pro-earbuds-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300214, '65f023632bc46470c104b75f', 'beats-fit-pro-earbuds-4.jpg', 'beats-fit-pro-earbuds-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300214', 'products/65f023632bc46470c104b75f/ELEC-SKU-178/SMALL/beats-fit-pro-earbuds-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-178/SMALL/beats-fit-pro-earbuds-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300215, '65f023632bc46470c104b75f', 'beats-fit-pro-earbuds-5.jpg', 'beats-fit-pro-earbuds-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300215', 'products/65f023632bc46470c104b75f/ELEC-SKU-178/SMALL/beats-fit-pro-earbuds-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-178/SMALL/beats-fit-pro-earbuds-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300216, '65f023632bc46470c104b75f', 'hp-elitebook-840-g10-laptop-1.jpg', 'hp-elitebook-840-g10-laptop-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300216', 'products/65f023632bc46470c104b75f/ELEC-SKU-179/SMALL/hp-elitebook-840-g10-laptop-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-179/SMALL/hp-elitebook-840-g10-laptop-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300217, '65f023632bc46470c104b75f', 'hp-elitebook-840-g10-laptop-2.jpg', 'hp-elitebook-840-g10-laptop-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300217', 'products/65f023632bc46470c104b75f/ELEC-SKU-179/SMALL/hp-elitebook-840-g10-laptop-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-179/SMALL/hp-elitebook-840-g10-laptop-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300218, '65f023632bc46470c104b75f', 'hp-elitebook-840-g10-laptop-3.jpg', 'hp-elitebook-840-g10-laptop-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300218', 'products/65f023632bc46470c104b75f/ELEC-SKU-179/SMALL/hp-elitebook-840-g10-laptop-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-179/SMALL/hp-elitebook-840-g10-laptop-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300219, '65f023632bc46470c104b75f', 'hp-elitebook-840-g10-laptop-4.jpg', 'hp-elitebook-840-g10-laptop-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300219', 'products/65f023632bc46470c104b75f/ELEC-SKU-179/SMALL/hp-elitebook-840-g10-laptop-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-179/SMALL/hp-elitebook-840-g10-laptop-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300220, '65f023632bc46470c104b75f', 'hp-elitebook-840-g10-laptop-5.jpg', 'hp-elitebook-840-g10-laptop-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300220', 'products/65f023632bc46470c104b75f/ELEC-SKU-179/SMALL/hp-elitebook-840-g10-laptop-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-179/SMALL/hp-elitebook-840-g10-laptop-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300221, '65f023632bc46470c104b75f', 'lg-s90qy-soundbar-1.jpg', 'lg-s90qy-soundbar-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300221', 'products/65f023632bc46470c104b75f/ELEC-SKU-180/SMALL/lg-s90qy-soundbar-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-180/SMALL/lg-s90qy-soundbar-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300222, '65f023632bc46470c104b75f', 'lg-s90qy-soundbar-2.jpg', 'lg-s90qy-soundbar-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300222', 'products/65f023632bc46470c104b75f/ELEC-SKU-180/SMALL/lg-s90qy-soundbar-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-180/SMALL/lg-s90qy-soundbar-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300223, '65f023632bc46470c104b75f', 'lg-s90qy-soundbar-3.jpg', 'lg-s90qy-soundbar-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300223', 'products/65f023632bc46470c104b75f/ELEC-SKU-180/SMALL/lg-s90qy-soundbar-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-180/SMALL/lg-s90qy-soundbar-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300224, '65f023632bc46470c104b75f', 'lg-s90qy-soundbar-4.jpg', 'lg-s90qy-soundbar-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300224', 'products/65f023632bc46470c104b75f/ELEC-SKU-180/SMALL/lg-s90qy-soundbar-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-180/SMALL/lg-s90qy-soundbar-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-300225, '65f023632bc46470c104b75f', 'lg-s90qy-soundbar-5.jpg', 'lg-s90qy-soundbar-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-300225', 'products/65f023632bc46470c104b75f/ELEC-SKU-180/SMALL/lg-s90qy-soundbar-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc46470c104b75f/ELEC-SKU-180/SMALL/lg-s90qy-soundbar-5.jpg', now())
on conflict (id) do nothing;
