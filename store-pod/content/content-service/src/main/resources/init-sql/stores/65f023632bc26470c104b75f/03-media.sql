-- Demo product photos, registered as media assets.
--
-- The objects already exist in MinIO under the key layout catalog used when it owned files; the seed
-- registers them rather than moving bytes. Ids are negative on purpose: media asset ids come from a
-- sequence that only grows upward, so seed-only rows below zero can never collide with uploads.
-- catalog.product_image references these ids — see 16-catalog-product-image.sql in the catalog service.
-- bytes is 0 because the seed never weighed the objects; the quota bar therefore ignores demo photos.

INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200001, '65f023632bc26470c104b75f', 'toyota-camry-2024-1.jpg', 'toyota-camry-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200001', 'products/65f023632bc26470c104b75f/CAR-SKU-91/SMALL/toyota-camry-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-91/SMALL/toyota-camry-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200002, '65f023632bc26470c104b75f', 'toyota-camry-2024-2.jpg', 'toyota-camry-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200002', 'products/65f023632bc26470c104b75f/CAR-SKU-91/SMALL/toyota-camry-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-91/SMALL/toyota-camry-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200003, '65f023632bc26470c104b75f', 'toyota-camry-2024-3.jpg', 'toyota-camry-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200003', 'products/65f023632bc26470c104b75f/CAR-SKU-91/SMALL/toyota-camry-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-91/SMALL/toyota-camry-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200004, '65f023632bc26470c104b75f', 'toyota-camry-2024-4.jpg', 'toyota-camry-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200004', 'products/65f023632bc26470c104b75f/CAR-SKU-91/SMALL/toyota-camry-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-91/SMALL/toyota-camry-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200005, '65f023632bc26470c104b75f', 'toyota-camry-2024-5.jpg', 'toyota-camry-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200005', 'products/65f023632bc26470c104b75f/CAR-SKU-91/SMALL/toyota-camry-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-91/SMALL/toyota-camry-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200006, '65f023632bc26470c104b75f', 'bmw-x5-2024-1.jpg', 'bmw-x5-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200006', 'products/65f023632bc26470c104b75f/CAR-SKU-92/SMALL/bmw-x5-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-92/SMALL/bmw-x5-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200007, '65f023632bc26470c104b75f', 'bmw-x5-2024-2.jpg', 'bmw-x5-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200007', 'products/65f023632bc26470c104b75f/CAR-SKU-92/SMALL/bmw-x5-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-92/SMALL/bmw-x5-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200008, '65f023632bc26470c104b75f', 'bmw-x5-2024-3.jpg', 'bmw-x5-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200008', 'products/65f023632bc26470c104b75f/CAR-SKU-92/SMALL/bmw-x5-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-92/SMALL/bmw-x5-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200009, '65f023632bc26470c104b75f', 'bmw-x5-2024-4.jpg', 'bmw-x5-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200009', 'products/65f023632bc26470c104b75f/CAR-SKU-92/SMALL/bmw-x5-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-92/SMALL/bmw-x5-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200010, '65f023632bc26470c104b75f', 'bmw-x5-2024-5.jpg', 'bmw-x5-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200010', 'products/65f023632bc26470c104b75f/CAR-SKU-92/SMALL/bmw-x5-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-92/SMALL/bmw-x5-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200011, '65f023632bc26470c104b75f', 'mercedes-eqs-2024-1.jpg', 'mercedes-eqs-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200011', 'products/65f023632bc26470c104b75f/CAR-SKU-93/SMALL/mercedes-eqs-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-93/SMALL/mercedes-eqs-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200012, '65f023632bc26470c104b75f', 'mercedes-eqs-2024-2.jpg', 'mercedes-eqs-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200012', 'products/65f023632bc26470c104b75f/CAR-SKU-93/SMALL/mercedes-eqs-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-93/SMALL/mercedes-eqs-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200013, '65f023632bc26470c104b75f', 'mercedes-eqs-2024-3.jpg', 'mercedes-eqs-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200013', 'products/65f023632bc26470c104b75f/CAR-SKU-93/SMALL/mercedes-eqs-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-93/SMALL/mercedes-eqs-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200014, '65f023632bc26470c104b75f', 'mercedes-eqs-2024-4.jpg', 'mercedes-eqs-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200014', 'products/65f023632bc26470c104b75f/CAR-SKU-93/SMALL/mercedes-eqs-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-93/SMALL/mercedes-eqs-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200015, '65f023632bc26470c104b75f', 'mercedes-eqs-2024-5.jpg', 'mercedes-eqs-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200015', 'products/65f023632bc26470c104b75f/CAR-SKU-93/SMALL/mercedes-eqs-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-93/SMALL/mercedes-eqs-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200016, '65f023632bc26470c104b75f', 'hyundai-tucson-2021-used-1.jpg', 'hyundai-tucson-2021-used-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200016', 'products/65f023632bc26470c104b75f/CAR-SKU-94/SMALL/hyundai-tucson-2021-used-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-94/SMALL/hyundai-tucson-2021-used-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200017, '65f023632bc26470c104b75f', 'hyundai-tucson-2021-used-2.jpg', 'hyundai-tucson-2021-used-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200017', 'products/65f023632bc26470c104b75f/CAR-SKU-94/SMALL/hyundai-tucson-2021-used-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-94/SMALL/hyundai-tucson-2021-used-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200018, '65f023632bc26470c104b75f', 'hyundai-tucson-2021-used-3.jpg', 'hyundai-tucson-2021-used-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200018', 'products/65f023632bc26470c104b75f/CAR-SKU-94/SMALL/hyundai-tucson-2021-used-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-94/SMALL/hyundai-tucson-2021-used-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200019, '65f023632bc26470c104b75f', 'hyundai-tucson-2021-used-4.jpg', 'hyundai-tucson-2021-used-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200019', 'products/65f023632bc26470c104b75f/CAR-SKU-94/SMALL/hyundai-tucson-2021-used-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-94/SMALL/hyundai-tucson-2021-used-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200020, '65f023632bc26470c104b75f', 'hyundai-tucson-2021-used-5.jpg', 'hyundai-tucson-2021-used-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200020', 'products/65f023632bc26470c104b75f/CAR-SKU-94/SMALL/hyundai-tucson-2021-used-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-94/SMALL/hyundai-tucson-2021-used-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200021, '65f023632bc26470c104b75f', 'kia-sportage-2024-1.jpg', 'kia-sportage-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200021', 'products/65f023632bc26470c104b75f/CAR-SKU-95/SMALL/kia-sportage-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-95/SMALL/kia-sportage-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200022, '65f023632bc26470c104b75f', 'kia-sportage-2024-2.jpg', 'kia-sportage-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200022', 'products/65f023632bc26470c104b75f/CAR-SKU-95/SMALL/kia-sportage-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-95/SMALL/kia-sportage-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200023, '65f023632bc26470c104b75f', 'kia-sportage-2024-3.jpg', 'kia-sportage-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200023', 'products/65f023632bc26470c104b75f/CAR-SKU-95/SMALL/kia-sportage-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-95/SMALL/kia-sportage-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200024, '65f023632bc26470c104b75f', 'kia-sportage-2024-4.jpg', 'kia-sportage-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200024', 'products/65f023632bc26470c104b75f/CAR-SKU-95/SMALL/kia-sportage-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-95/SMALL/kia-sportage-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200025, '65f023632bc26470c104b75f', 'kia-sportage-2024-5.jpg', 'kia-sportage-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200025', 'products/65f023632bc26470c104b75f/CAR-SKU-95/SMALL/kia-sportage-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-95/SMALL/kia-sportage-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200026, '65f023632bc26470c104b75f', 'ford-mustang-2024-1.jpg', 'ford-mustang-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200026', 'products/65f023632bc26470c104b75f/CAR-SKU-96/SMALL/ford-mustang-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-96/SMALL/ford-mustang-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200027, '65f023632bc26470c104b75f', 'ford-mustang-2024-2.jpg', 'ford-mustang-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200027', 'products/65f023632bc26470c104b75f/CAR-SKU-96/SMALL/ford-mustang-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-96/SMALL/ford-mustang-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200028, '65f023632bc26470c104b75f', 'ford-mustang-2024-3.jpg', 'ford-mustang-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200028', 'products/65f023632bc26470c104b75f/CAR-SKU-96/SMALL/ford-mustang-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-96/SMALL/ford-mustang-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200029, '65f023632bc26470c104b75f', 'ford-mustang-2024-4.jpg', 'ford-mustang-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200029', 'products/65f023632bc26470c104b75f/CAR-SKU-96/SMALL/ford-mustang-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-96/SMALL/ford-mustang-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200030, '65f023632bc26470c104b75f', 'ford-mustang-2024-5.jpg', 'ford-mustang-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200030', 'products/65f023632bc26470c104b75f/CAR-SKU-96/SMALL/ford-mustang-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-96/SMALL/ford-mustang-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200031, '65f023632bc26470c104b75f', 'toyota-rav4-2024-1.jpg', 'toyota-rav4-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200031', 'products/65f023632bc26470c104b75f/CAR-SKU-97/SMALL/toyota-rav4-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-97/SMALL/toyota-rav4-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200032, '65f023632bc26470c104b75f', 'toyota-rav4-2024-2.jpg', 'toyota-rav4-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200032', 'products/65f023632bc26470c104b75f/CAR-SKU-97/SMALL/toyota-rav4-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-97/SMALL/toyota-rav4-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200033, '65f023632bc26470c104b75f', 'toyota-rav4-2024-3.jpg', 'toyota-rav4-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200033', 'products/65f023632bc26470c104b75f/CAR-SKU-97/SMALL/toyota-rav4-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-97/SMALL/toyota-rav4-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200034, '65f023632bc26470c104b75f', 'toyota-rav4-2024-4.jpg', 'toyota-rav4-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200034', 'products/65f023632bc26470c104b75f/CAR-SKU-97/SMALL/toyota-rav4-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-97/SMALL/toyota-rav4-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200035, '65f023632bc26470c104b75f', 'toyota-rav4-2024-5.jpg', 'toyota-rav4-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200035', 'products/65f023632bc26470c104b75f/CAR-SKU-97/SMALL/toyota-rav4-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-97/SMALL/toyota-rav4-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200036, '65f023632bc26470c104b75f', 'bmw-3-series-2020-used-1.jpg', 'bmw-3-series-2020-used-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200036', 'products/65f023632bc26470c104b75f/CAR-SKU-98/SMALL/bmw-3-series-2020-used-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-98/SMALL/bmw-3-series-2020-used-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200037, '65f023632bc26470c104b75f', 'bmw-3-series-2020-used-2.jpg', 'bmw-3-series-2020-used-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200037', 'products/65f023632bc26470c104b75f/CAR-SKU-98/SMALL/bmw-3-series-2020-used-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-98/SMALL/bmw-3-series-2020-used-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200038, '65f023632bc26470c104b75f', 'bmw-3-series-2020-used-3.jpg', 'bmw-3-series-2020-used-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200038', 'products/65f023632bc26470c104b75f/CAR-SKU-98/SMALL/bmw-3-series-2020-used-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-98/SMALL/bmw-3-series-2020-used-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200039, '65f023632bc26470c104b75f', 'bmw-3-series-2020-used-4.jpg', 'bmw-3-series-2020-used-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200039', 'products/65f023632bc26470c104b75f/CAR-SKU-98/SMALL/bmw-3-series-2020-used-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-98/SMALL/bmw-3-series-2020-used-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200040, '65f023632bc26470c104b75f', 'bmw-3-series-2020-used-5.jpg', 'bmw-3-series-2020-used-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200040', 'products/65f023632bc26470c104b75f/CAR-SKU-98/SMALL/bmw-3-series-2020-used-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-98/SMALL/bmw-3-series-2020-used-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200041, '65f023632bc26470c104b75f', 'mercedes-c-class-2024-1.jpg', 'mercedes-c-class-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200041', 'products/65f023632bc26470c104b75f/CAR-SKU-99/SMALL/mercedes-c-class-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-99/SMALL/mercedes-c-class-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200042, '65f023632bc26470c104b75f', 'mercedes-c-class-2024-2.jpg', 'mercedes-c-class-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200042', 'products/65f023632bc26470c104b75f/CAR-SKU-99/SMALL/mercedes-c-class-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-99/SMALL/mercedes-c-class-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200043, '65f023632bc26470c104b75f', 'mercedes-c-class-2024-3.jpg', 'mercedes-c-class-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200043', 'products/65f023632bc26470c104b75f/CAR-SKU-99/SMALL/mercedes-c-class-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-99/SMALL/mercedes-c-class-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200044, '65f023632bc26470c104b75f', 'mercedes-c-class-2024-4.jpg', 'mercedes-c-class-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200044', 'products/65f023632bc26470c104b75f/CAR-SKU-99/SMALL/mercedes-c-class-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-99/SMALL/mercedes-c-class-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200045, '65f023632bc26470c104b75f', 'mercedes-c-class-2024-5.jpg', 'mercedes-c-class-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200045', 'products/65f023632bc26470c104b75f/CAR-SKU-99/SMALL/mercedes-c-class-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-99/SMALL/mercedes-c-class-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200046, '65f023632bc26470c104b75f', 'hyundai-elantra-2024-1.jpg', 'hyundai-elantra-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200046', 'products/65f023632bc26470c104b75f/CAR-SKU-100/SMALL/hyundai-elantra-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-100/SMALL/hyundai-elantra-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200047, '65f023632bc26470c104b75f', 'hyundai-elantra-2024-2.jpg', 'hyundai-elantra-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200047', 'products/65f023632bc26470c104b75f/CAR-SKU-100/SMALL/hyundai-elantra-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-100/SMALL/hyundai-elantra-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200048, '65f023632bc26470c104b75f', 'hyundai-elantra-2024-3.jpg', 'hyundai-elantra-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200048', 'products/65f023632bc26470c104b75f/CAR-SKU-100/SMALL/hyundai-elantra-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-100/SMALL/hyundai-elantra-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200049, '65f023632bc26470c104b75f', 'hyundai-elantra-2024-4.jpg', 'hyundai-elantra-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200049', 'products/65f023632bc26470c104b75f/CAR-SKU-100/SMALL/hyundai-elantra-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-100/SMALL/hyundai-elantra-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200050, '65f023632bc26470c104b75f', 'hyundai-elantra-2024-5.jpg', 'hyundai-elantra-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200050', 'products/65f023632bc26470c104b75f/CAR-SKU-100/SMALL/hyundai-elantra-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-100/SMALL/hyundai-elantra-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200051, '65f023632bc26470c104b75f', 'kia-seltos-2024-1.jpg', 'kia-seltos-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200051', 'products/65f023632bc26470c104b75f/CAR-SKU-101/SMALL/kia-seltos-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-101/SMALL/kia-seltos-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200052, '65f023632bc26470c104b75f', 'kia-seltos-2024-2.jpg', 'kia-seltos-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200052', 'products/65f023632bc26470c104b75f/CAR-SKU-101/SMALL/kia-seltos-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-101/SMALL/kia-seltos-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200053, '65f023632bc26470c104b75f', 'kia-seltos-2024-3.jpg', 'kia-seltos-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200053', 'products/65f023632bc26470c104b75f/CAR-SKU-101/SMALL/kia-seltos-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-101/SMALL/kia-seltos-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200054, '65f023632bc26470c104b75f', 'kia-seltos-2024-4.jpg', 'kia-seltos-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200054', 'products/65f023632bc26470c104b75f/CAR-SKU-101/SMALL/kia-seltos-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-101/SMALL/kia-seltos-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200055, '65f023632bc26470c104b75f', 'kia-seltos-2024-5.jpg', 'kia-seltos-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200055', 'products/65f023632bc26470c104b75f/CAR-SKU-101/SMALL/kia-seltos-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-101/SMALL/kia-seltos-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200056, '65f023632bc26470c104b75f', 'ford-f150-2019-used-1.jpg', 'ford-f150-2019-used-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200056', 'products/65f023632bc26470c104b75f/CAR-SKU-102/SMALL/ford-f150-2019-used-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-102/SMALL/ford-f150-2019-used-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200057, '65f023632bc26470c104b75f', 'ford-f150-2019-used-2.jpg', 'ford-f150-2019-used-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200057', 'products/65f023632bc26470c104b75f/CAR-SKU-102/SMALL/ford-f150-2019-used-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-102/SMALL/ford-f150-2019-used-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200058, '65f023632bc26470c104b75f', 'ford-f150-2019-used-3.jpg', 'ford-f150-2019-used-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200058', 'products/65f023632bc26470c104b75f/CAR-SKU-102/SMALL/ford-f150-2019-used-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-102/SMALL/ford-f150-2019-used-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200059, '65f023632bc26470c104b75f', 'ford-f150-2019-used-4.jpg', 'ford-f150-2019-used-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200059', 'products/65f023632bc26470c104b75f/CAR-SKU-102/SMALL/ford-f150-2019-used-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-102/SMALL/ford-f150-2019-used-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200060, '65f023632bc26470c104b75f', 'ford-f150-2019-used-5.jpg', 'ford-f150-2019-used-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200060', 'products/65f023632bc26470c104b75f/CAR-SKU-102/SMALL/ford-f150-2019-used-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-102/SMALL/ford-f150-2019-used-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200061, '65f023632bc26470c104b75f', 'toyota-corolla-2024-1.jpg', 'toyota-corolla-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200061', 'products/65f023632bc26470c104b75f/CAR-SKU-103/SMALL/toyota-corolla-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-103/SMALL/toyota-corolla-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200062, '65f023632bc26470c104b75f', 'toyota-corolla-2024-2.jpg', 'toyota-corolla-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200062', 'products/65f023632bc26470c104b75f/CAR-SKU-103/SMALL/toyota-corolla-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-103/SMALL/toyota-corolla-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200063, '65f023632bc26470c104b75f', 'toyota-corolla-2024-3.jpg', 'toyota-corolla-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200063', 'products/65f023632bc26470c104b75f/CAR-SKU-103/SMALL/toyota-corolla-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-103/SMALL/toyota-corolla-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200064, '65f023632bc26470c104b75f', 'toyota-corolla-2024-4.jpg', 'toyota-corolla-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200064', 'products/65f023632bc26470c104b75f/CAR-SKU-103/SMALL/toyota-corolla-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-103/SMALL/toyota-corolla-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200065, '65f023632bc26470c104b75f', 'toyota-corolla-2024-5.jpg', 'toyota-corolla-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200065', 'products/65f023632bc26470c104b75f/CAR-SKU-103/SMALL/toyota-corolla-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-103/SMALL/toyota-corolla-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200066, '65f023632bc26470c104b75f', 'bmw-i4-2024-1.jpg', 'bmw-i4-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200066', 'products/65f023632bc26470c104b75f/CAR-SKU-104/SMALL/bmw-i4-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-104/SMALL/bmw-i4-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200067, '65f023632bc26470c104b75f', 'bmw-i4-2024-2.jpg', 'bmw-i4-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200067', 'products/65f023632bc26470c104b75f/CAR-SKU-104/SMALL/bmw-i4-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-104/SMALL/bmw-i4-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200068, '65f023632bc26470c104b75f', 'bmw-i4-2024-3.jpg', 'bmw-i4-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200068', 'products/65f023632bc26470c104b75f/CAR-SKU-104/SMALL/bmw-i4-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-104/SMALL/bmw-i4-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200069, '65f023632bc26470c104b75f', 'bmw-i4-2024-4.jpg', 'bmw-i4-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200069', 'products/65f023632bc26470c104b75f/CAR-SKU-104/SMALL/bmw-i4-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-104/SMALL/bmw-i4-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200070, '65f023632bc26470c104b75f', 'bmw-i4-2024-5.jpg', 'bmw-i4-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200070', 'products/65f023632bc26470c104b75f/CAR-SKU-104/SMALL/bmw-i4-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-104/SMALL/bmw-i4-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200071, '65f023632bc26470c104b75f', 'mercedes-e-class-2024-1.jpg', 'mercedes-e-class-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200071', 'products/65f023632bc26470c104b75f/CAR-SKU-105/SMALL/mercedes-e-class-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-105/SMALL/mercedes-e-class-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200072, '65f023632bc26470c104b75f', 'mercedes-e-class-2024-2.jpg', 'mercedes-e-class-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200072', 'products/65f023632bc26470c104b75f/CAR-SKU-105/SMALL/mercedes-e-class-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-105/SMALL/mercedes-e-class-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200073, '65f023632bc26470c104b75f', 'mercedes-e-class-2024-3.jpg', 'mercedes-e-class-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200073', 'products/65f023632bc26470c104b75f/CAR-SKU-105/SMALL/mercedes-e-class-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-105/SMALL/mercedes-e-class-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200074, '65f023632bc26470c104b75f', 'mercedes-e-class-2024-4.jpg', 'mercedes-e-class-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200074', 'products/65f023632bc26470c104b75f/CAR-SKU-105/SMALL/mercedes-e-class-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-105/SMALL/mercedes-e-class-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200075, '65f023632bc26470c104b75f', 'mercedes-e-class-2024-5.jpg', 'mercedes-e-class-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200075', 'products/65f023632bc26470c104b75f/CAR-SKU-105/SMALL/mercedes-e-class-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-105/SMALL/mercedes-e-class-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200076, '65f023632bc26470c104b75f', 'hyundai-sonata-2022-used-1.jpg', 'hyundai-sonata-2022-used-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200076', 'products/65f023632bc26470c104b75f/CAR-SKU-106/SMALL/hyundai-sonata-2022-used-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-106/SMALL/hyundai-sonata-2022-used-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200077, '65f023632bc26470c104b75f', 'hyundai-sonata-2022-used-2.jpg', 'hyundai-sonata-2022-used-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200077', 'products/65f023632bc26470c104b75f/CAR-SKU-106/SMALL/hyundai-sonata-2022-used-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-106/SMALL/hyundai-sonata-2022-used-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200078, '65f023632bc26470c104b75f', 'hyundai-sonata-2022-used-3.jpg', 'hyundai-sonata-2022-used-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200078', 'products/65f023632bc26470c104b75f/CAR-SKU-106/SMALL/hyundai-sonata-2022-used-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-106/SMALL/hyundai-sonata-2022-used-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200079, '65f023632bc26470c104b75f', 'hyundai-sonata-2022-used-4.jpg', 'hyundai-sonata-2022-used-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200079', 'products/65f023632bc26470c104b75f/CAR-SKU-106/SMALL/hyundai-sonata-2022-used-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-106/SMALL/hyundai-sonata-2022-used-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200080, '65f023632bc26470c104b75f', 'hyundai-sonata-2022-used-5.jpg', 'hyundai-sonata-2022-used-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200080', 'products/65f023632bc26470c104b75f/CAR-SKU-106/SMALL/hyundai-sonata-2022-used-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-106/SMALL/hyundai-sonata-2022-used-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200081, '65f023632bc26470c104b75f', 'kia-ev6-2024-1.jpg', 'kia-ev6-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200081', 'products/65f023632bc26470c104b75f/CAR-SKU-107/SMALL/kia-ev6-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-107/SMALL/kia-ev6-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200082, '65f023632bc26470c104b75f', 'kia-ev6-2024-2.jpg', 'kia-ev6-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200082', 'products/65f023632bc26470c104b75f/CAR-SKU-107/SMALL/kia-ev6-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-107/SMALL/kia-ev6-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200083, '65f023632bc26470c104b75f', 'kia-ev6-2024-3.jpg', 'kia-ev6-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200083', 'products/65f023632bc26470c104b75f/CAR-SKU-107/SMALL/kia-ev6-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-107/SMALL/kia-ev6-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200084, '65f023632bc26470c104b75f', 'kia-ev6-2024-4.jpg', 'kia-ev6-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200084', 'products/65f023632bc26470c104b75f/CAR-SKU-107/SMALL/kia-ev6-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-107/SMALL/kia-ev6-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200085, '65f023632bc26470c104b75f', 'kia-ev6-2024-5.jpg', 'kia-ev6-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200085', 'products/65f023632bc26470c104b75f/CAR-SKU-107/SMALL/kia-ev6-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-107/SMALL/kia-ev6-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200086, '65f023632bc26470c104b75f', 'ford-explorer-2024-1.jpg', 'ford-explorer-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200086', 'products/65f023632bc26470c104b75f/CAR-SKU-108/SMALL/ford-explorer-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-108/SMALL/ford-explorer-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200087, '65f023632bc26470c104b75f', 'ford-explorer-2024-2.jpg', 'ford-explorer-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200087', 'products/65f023632bc26470c104b75f/CAR-SKU-108/SMALL/ford-explorer-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-108/SMALL/ford-explorer-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200088, '65f023632bc26470c104b75f', 'ford-explorer-2024-3.jpg', 'ford-explorer-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200088', 'products/65f023632bc26470c104b75f/CAR-SKU-108/SMALL/ford-explorer-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-108/SMALL/ford-explorer-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200089, '65f023632bc26470c104b75f', 'ford-explorer-2024-4.jpg', 'ford-explorer-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200089', 'products/65f023632bc26470c104b75f/CAR-SKU-108/SMALL/ford-explorer-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-108/SMALL/ford-explorer-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200090, '65f023632bc26470c104b75f', 'ford-explorer-2024-5.jpg', 'ford-explorer-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200090', 'products/65f023632bc26470c104b75f/CAR-SKU-108/SMALL/ford-explorer-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-108/SMALL/ford-explorer-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200091, '65f023632bc26470c104b75f', 'toyota-highlander-2024-1.jpg', 'toyota-highlander-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200091', 'products/65f023632bc26470c104b75f/CAR-SKU-109/SMALL/toyota-highlander-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-109/SMALL/toyota-highlander-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200092, '65f023632bc26470c104b75f', 'toyota-highlander-2024-2.jpg', 'toyota-highlander-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200092', 'products/65f023632bc26470c104b75f/CAR-SKU-109/SMALL/toyota-highlander-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-109/SMALL/toyota-highlander-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200093, '65f023632bc26470c104b75f', 'toyota-highlander-2024-3.jpg', 'toyota-highlander-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200093', 'products/65f023632bc26470c104b75f/CAR-SKU-109/SMALL/toyota-highlander-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-109/SMALL/toyota-highlander-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200094, '65f023632bc26470c104b75f', 'toyota-highlander-2024-4.jpg', 'toyota-highlander-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200094', 'products/65f023632bc26470c104b75f/CAR-SKU-109/SMALL/toyota-highlander-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-109/SMALL/toyota-highlander-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200095, '65f023632bc26470c104b75f', 'toyota-highlander-2024-5.jpg', 'toyota-highlander-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200095', 'products/65f023632bc26470c104b75f/CAR-SKU-109/SMALL/toyota-highlander-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-109/SMALL/toyota-highlander-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200096, '65f023632bc26470c104b75f', 'bmw-x3-2021-used-1.jpg', 'bmw-x3-2021-used-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200096', 'products/65f023632bc26470c104b75f/CAR-SKU-110/SMALL/bmw-x3-2021-used-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-110/SMALL/bmw-x3-2021-used-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200097, '65f023632bc26470c104b75f', 'bmw-x3-2021-used-2.jpg', 'bmw-x3-2021-used-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200097', 'products/65f023632bc26470c104b75f/CAR-SKU-110/SMALL/bmw-x3-2021-used-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-110/SMALL/bmw-x3-2021-used-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200098, '65f023632bc26470c104b75f', 'bmw-x3-2021-used-3.jpg', 'bmw-x3-2021-used-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200098', 'products/65f023632bc26470c104b75f/CAR-SKU-110/SMALL/bmw-x3-2021-used-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-110/SMALL/bmw-x3-2021-used-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200099, '65f023632bc26470c104b75f', 'bmw-x3-2021-used-4.jpg', 'bmw-x3-2021-used-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200099', 'products/65f023632bc26470c104b75f/CAR-SKU-110/SMALL/bmw-x3-2021-used-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-110/SMALL/bmw-x3-2021-used-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200100, '65f023632bc26470c104b75f', 'bmw-x3-2021-used-5.jpg', 'bmw-x3-2021-used-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200100', 'products/65f023632bc26470c104b75f/CAR-SKU-110/SMALL/bmw-x3-2021-used-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-110/SMALL/bmw-x3-2021-used-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200101, '65f023632bc26470c104b75f', 'mercedes-glc-2024-1.jpg', 'mercedes-glc-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200101', 'products/65f023632bc26470c104b75f/CAR-SKU-111/SMALL/mercedes-glc-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-111/SMALL/mercedes-glc-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200102, '65f023632bc26470c104b75f', 'mercedes-glc-2024-2.jpg', 'mercedes-glc-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200102', 'products/65f023632bc26470c104b75f/CAR-SKU-111/SMALL/mercedes-glc-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-111/SMALL/mercedes-glc-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200103, '65f023632bc26470c104b75f', 'mercedes-glc-2024-3.jpg', 'mercedes-glc-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200103', 'products/65f023632bc26470c104b75f/CAR-SKU-111/SMALL/mercedes-glc-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-111/SMALL/mercedes-glc-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200104, '65f023632bc26470c104b75f', 'mercedes-glc-2024-4.jpg', 'mercedes-glc-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200104', 'products/65f023632bc26470c104b75f/CAR-SKU-111/SMALL/mercedes-glc-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-111/SMALL/mercedes-glc-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200105, '65f023632bc26470c104b75f', 'mercedes-glc-2024-5.jpg', 'mercedes-glc-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200105', 'products/65f023632bc26470c104b75f/CAR-SKU-111/SMALL/mercedes-glc-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-111/SMALL/mercedes-glc-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200106, '65f023632bc26470c104b75f', 'hyundai-santa-fe-2024-1.jpg', 'hyundai-santa-fe-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200106', 'products/65f023632bc26470c104b75f/CAR-SKU-112/SMALL/hyundai-santa-fe-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-112/SMALL/hyundai-santa-fe-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200107, '65f023632bc26470c104b75f', 'hyundai-santa-fe-2024-2.jpg', 'hyundai-santa-fe-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200107', 'products/65f023632bc26470c104b75f/CAR-SKU-112/SMALL/hyundai-santa-fe-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-112/SMALL/hyundai-santa-fe-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200108, '65f023632bc26470c104b75f', 'hyundai-santa-fe-2024-3.jpg', 'hyundai-santa-fe-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200108', 'products/65f023632bc26470c104b75f/CAR-SKU-112/SMALL/hyundai-santa-fe-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-112/SMALL/hyundai-santa-fe-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200109, '65f023632bc26470c104b75f', 'hyundai-santa-fe-2024-4.jpg', 'hyundai-santa-fe-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200109', 'products/65f023632bc26470c104b75f/CAR-SKU-112/SMALL/hyundai-santa-fe-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-112/SMALL/hyundai-santa-fe-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200110, '65f023632bc26470c104b75f', 'hyundai-santa-fe-2024-5.jpg', 'hyundai-santa-fe-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200110', 'products/65f023632bc26470c104b75f/CAR-SKU-112/SMALL/hyundai-santa-fe-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-112/SMALL/hyundai-santa-fe-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200111, '65f023632bc26470c104b75f', 'kia-telluride-2024-1.jpg', 'kia-telluride-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200111', 'products/65f023632bc26470c104b75f/CAR-SKU-113/SMALL/kia-telluride-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-113/SMALL/kia-telluride-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200112, '65f023632bc26470c104b75f', 'kia-telluride-2024-2.jpg', 'kia-telluride-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200112', 'products/65f023632bc26470c104b75f/CAR-SKU-113/SMALL/kia-telluride-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-113/SMALL/kia-telluride-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200113, '65f023632bc26470c104b75f', 'kia-telluride-2024-3.jpg', 'kia-telluride-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200113', 'products/65f023632bc26470c104b75f/CAR-SKU-113/SMALL/kia-telluride-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-113/SMALL/kia-telluride-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200114, '65f023632bc26470c104b75f', 'kia-telluride-2024-4.jpg', 'kia-telluride-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200114', 'products/65f023632bc26470c104b75f/CAR-SKU-113/SMALL/kia-telluride-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-113/SMALL/kia-telluride-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200115, '65f023632bc26470c104b75f', 'kia-telluride-2024-5.jpg', 'kia-telluride-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200115', 'products/65f023632bc26470c104b75f/CAR-SKU-113/SMALL/kia-telluride-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-113/SMALL/kia-telluride-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200116, '65f023632bc26470c104b75f', 'ford-bronco-2022-used-1.jpg', 'ford-bronco-2022-used-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200116', 'products/65f023632bc26470c104b75f/CAR-SKU-114/SMALL/ford-bronco-2022-used-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-114/SMALL/ford-bronco-2022-used-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200117, '65f023632bc26470c104b75f', 'ford-bronco-2022-used-2.jpg', 'ford-bronco-2022-used-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200117', 'products/65f023632bc26470c104b75f/CAR-SKU-114/SMALL/ford-bronco-2022-used-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-114/SMALL/ford-bronco-2022-used-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200118, '65f023632bc26470c104b75f', 'ford-bronco-2022-used-3.jpg', 'ford-bronco-2022-used-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200118', 'products/65f023632bc26470c104b75f/CAR-SKU-114/SMALL/ford-bronco-2022-used-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-114/SMALL/ford-bronco-2022-used-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200119, '65f023632bc26470c104b75f', 'ford-bronco-2022-used-4.jpg', 'ford-bronco-2022-used-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200119', 'products/65f023632bc26470c104b75f/CAR-SKU-114/SMALL/ford-bronco-2022-used-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-114/SMALL/ford-bronco-2022-used-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200120, '65f023632bc26470c104b75f', 'ford-bronco-2022-used-5.jpg', 'ford-bronco-2022-used-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200120', 'products/65f023632bc26470c104b75f/CAR-SKU-114/SMALL/ford-bronco-2022-used-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-114/SMALL/ford-bronco-2022-used-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200121, '65f023632bc26470c104b75f', 'toyota-sienna-2024-1.jpg', 'toyota-sienna-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200121', 'products/65f023632bc26470c104b75f/CAR-SKU-115/SMALL/toyota-sienna-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-115/SMALL/toyota-sienna-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200122, '65f023632bc26470c104b75f', 'toyota-sienna-2024-2.jpg', 'toyota-sienna-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200122', 'products/65f023632bc26470c104b75f/CAR-SKU-115/SMALL/toyota-sienna-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-115/SMALL/toyota-sienna-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200123, '65f023632bc26470c104b75f', 'toyota-sienna-2024-3.jpg', 'toyota-sienna-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200123', 'products/65f023632bc26470c104b75f/CAR-SKU-115/SMALL/toyota-sienna-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-115/SMALL/toyota-sienna-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200124, '65f023632bc26470c104b75f', 'toyota-sienna-2024-4.jpg', 'toyota-sienna-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200124', 'products/65f023632bc26470c104b75f/CAR-SKU-115/SMALL/toyota-sienna-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-115/SMALL/toyota-sienna-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200125, '65f023632bc26470c104b75f', 'toyota-sienna-2024-5.jpg', 'toyota-sienna-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200125', 'products/65f023632bc26470c104b75f/CAR-SKU-115/SMALL/toyota-sienna-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-115/SMALL/toyota-sienna-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200126, '65f023632bc26470c104b75f', 'bmw-5-series-2024-1.jpg', 'bmw-5-series-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200126', 'products/65f023632bc26470c104b75f/CAR-SKU-116/SMALL/bmw-5-series-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-116/SMALL/bmw-5-series-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200127, '65f023632bc26470c104b75f', 'bmw-5-series-2024-2.jpg', 'bmw-5-series-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200127', 'products/65f023632bc26470c104b75f/CAR-SKU-116/SMALL/bmw-5-series-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-116/SMALL/bmw-5-series-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200128, '65f023632bc26470c104b75f', 'bmw-5-series-2024-3.jpg', 'bmw-5-series-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200128', 'products/65f023632bc26470c104b75f/CAR-SKU-116/SMALL/bmw-5-series-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-116/SMALL/bmw-5-series-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200129, '65f023632bc26470c104b75f', 'bmw-5-series-2024-4.jpg', 'bmw-5-series-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200129', 'products/65f023632bc26470c104b75f/CAR-SKU-116/SMALL/bmw-5-series-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-116/SMALL/bmw-5-series-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200130, '65f023632bc26470c104b75f', 'bmw-5-series-2024-5.jpg', 'bmw-5-series-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200130', 'products/65f023632bc26470c104b75f/CAR-SKU-116/SMALL/bmw-5-series-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-116/SMALL/bmw-5-series-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200131, '65f023632bc26470c104b75f', 'mercedes-s-class-2024-1.jpg', 'mercedes-s-class-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200131', 'products/65f023632bc26470c104b75f/CAR-SKU-117/SMALL/mercedes-s-class-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-117/SMALL/mercedes-s-class-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200132, '65f023632bc26470c104b75f', 'mercedes-s-class-2024-2.jpg', 'mercedes-s-class-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200132', 'products/65f023632bc26470c104b75f/CAR-SKU-117/SMALL/mercedes-s-class-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-117/SMALL/mercedes-s-class-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200133, '65f023632bc26470c104b75f', 'mercedes-s-class-2024-3.jpg', 'mercedes-s-class-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200133', 'products/65f023632bc26470c104b75f/CAR-SKU-117/SMALL/mercedes-s-class-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-117/SMALL/mercedes-s-class-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200134, '65f023632bc26470c104b75f', 'mercedes-s-class-2024-4.jpg', 'mercedes-s-class-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200134', 'products/65f023632bc26470c104b75f/CAR-SKU-117/SMALL/mercedes-s-class-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-117/SMALL/mercedes-s-class-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200135, '65f023632bc26470c104b75f', 'mercedes-s-class-2024-5.jpg', 'mercedes-s-class-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200135', 'products/65f023632bc26470c104b75f/CAR-SKU-117/SMALL/mercedes-s-class-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-117/SMALL/mercedes-s-class-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200136, '65f023632bc26470c104b75f', 'hyundai-kona-electric-2022-used-1.jpg', 'hyundai-kona-electric-2022-used-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200136', 'products/65f023632bc26470c104b75f/CAR-SKU-118/SMALL/hyundai-kona-electric-2022-used-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-118/SMALL/hyundai-kona-electric-2022-used-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200137, '65f023632bc26470c104b75f', 'hyundai-kona-electric-2022-used-2.jpg', 'hyundai-kona-electric-2022-used-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200137', 'products/65f023632bc26470c104b75f/CAR-SKU-118/SMALL/hyundai-kona-electric-2022-used-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-118/SMALL/hyundai-kona-electric-2022-used-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200138, '65f023632bc26470c104b75f', 'hyundai-kona-electric-2022-used-3.jpg', 'hyundai-kona-electric-2022-used-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200138', 'products/65f023632bc26470c104b75f/CAR-SKU-118/SMALL/hyundai-kona-electric-2022-used-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-118/SMALL/hyundai-kona-electric-2022-used-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200139, '65f023632bc26470c104b75f', 'hyundai-kona-electric-2022-used-4.jpg', 'hyundai-kona-electric-2022-used-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200139', 'products/65f023632bc26470c104b75f/CAR-SKU-118/SMALL/hyundai-kona-electric-2022-used-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-118/SMALL/hyundai-kona-electric-2022-used-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200140, '65f023632bc26470c104b75f', 'hyundai-kona-electric-2022-used-5.jpg', 'hyundai-kona-electric-2022-used-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200140', 'products/65f023632bc26470c104b75f/CAR-SKU-118/SMALL/hyundai-kona-electric-2022-used-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-118/SMALL/hyundai-kona-electric-2022-used-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200141, '65f023632bc26470c104b75f', 'kia-niro-ev-2024-1.jpg', 'kia-niro-ev-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200141', 'products/65f023632bc26470c104b75f/CAR-SKU-119/SMALL/kia-niro-ev-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-119/SMALL/kia-niro-ev-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200142, '65f023632bc26470c104b75f', 'kia-niro-ev-2024-2.jpg', 'kia-niro-ev-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200142', 'products/65f023632bc26470c104b75f/CAR-SKU-119/SMALL/kia-niro-ev-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-119/SMALL/kia-niro-ev-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200143, '65f023632bc26470c104b75f', 'kia-niro-ev-2024-3.jpg', 'kia-niro-ev-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200143', 'products/65f023632bc26470c104b75f/CAR-SKU-119/SMALL/kia-niro-ev-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-119/SMALL/kia-niro-ev-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200144, '65f023632bc26470c104b75f', 'kia-niro-ev-2024-4.jpg', 'kia-niro-ev-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200144', 'products/65f023632bc26470c104b75f/CAR-SKU-119/SMALL/kia-niro-ev-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-119/SMALL/kia-niro-ev-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200145, '65f023632bc26470c104b75f', 'kia-niro-ev-2024-5.jpg', 'kia-niro-ev-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200145', 'products/65f023632bc26470c104b75f/CAR-SKU-119/SMALL/kia-niro-ev-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-119/SMALL/kia-niro-ev-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200146, '65f023632bc26470c104b75f', 'ford-mustang-mach-e-2024-1.jpg', 'ford-mustang-mach-e-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200146', 'products/65f023632bc26470c104b75f/CAR-SKU-120/SMALL/ford-mustang-mach-e-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-120/SMALL/ford-mustang-mach-e-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200147, '65f023632bc26470c104b75f', 'ford-mustang-mach-e-2024-2.jpg', 'ford-mustang-mach-e-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200147', 'products/65f023632bc26470c104b75f/CAR-SKU-120/SMALL/ford-mustang-mach-e-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-120/SMALL/ford-mustang-mach-e-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200148, '65f023632bc26470c104b75f', 'ford-mustang-mach-e-2024-3.jpg', 'ford-mustang-mach-e-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200148', 'products/65f023632bc26470c104b75f/CAR-SKU-120/SMALL/ford-mustang-mach-e-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-120/SMALL/ford-mustang-mach-e-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200149, '65f023632bc26470c104b75f', 'ford-mustang-mach-e-2024-4.jpg', 'ford-mustang-mach-e-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200149', 'products/65f023632bc26470c104b75f/CAR-SKU-120/SMALL/ford-mustang-mach-e-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-120/SMALL/ford-mustang-mach-e-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200150, '65f023632bc26470c104b75f', 'ford-mustang-mach-e-2024-5.jpg', 'ford-mustang-mach-e-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200150', 'products/65f023632bc26470c104b75f/CAR-SKU-120/SMALL/ford-mustang-mach-e-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-120/SMALL/ford-mustang-mach-e-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200151, '65f023632bc26470c104b75f', 'toyota-avalon-2024-1.jpg', 'toyota-avalon-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200151', 'products/65f023632bc26470c104b75f/CAR-SKU-121/SMALL/toyota-avalon-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-121/SMALL/toyota-avalon-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200152, '65f023632bc26470c104b75f', 'toyota-avalon-2024-2.jpg', 'toyota-avalon-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200152', 'products/65f023632bc26470c104b75f/CAR-SKU-121/SMALL/toyota-avalon-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-121/SMALL/toyota-avalon-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200153, '65f023632bc26470c104b75f', 'toyota-avalon-2024-3.jpg', 'toyota-avalon-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200153', 'products/65f023632bc26470c104b75f/CAR-SKU-121/SMALL/toyota-avalon-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-121/SMALL/toyota-avalon-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200154, '65f023632bc26470c104b75f', 'toyota-avalon-2024-4.jpg', 'toyota-avalon-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200154', 'products/65f023632bc26470c104b75f/CAR-SKU-121/SMALL/toyota-avalon-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-121/SMALL/toyota-avalon-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200155, '65f023632bc26470c104b75f', 'toyota-avalon-2024-5.jpg', 'toyota-avalon-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200155', 'products/65f023632bc26470c104b75f/CAR-SKU-121/SMALL/toyota-avalon-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-121/SMALL/toyota-avalon-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200156, '65f023632bc26470c104b75f', 'bmw-x1-2024-1.jpg', 'bmw-x1-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200156', 'products/65f023632bc26470c104b75f/CAR-SKU-122/SMALL/bmw-x1-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-122/SMALL/bmw-x1-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200157, '65f023632bc26470c104b75f', 'bmw-x1-2024-2.jpg', 'bmw-x1-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200157', 'products/65f023632bc26470c104b75f/CAR-SKU-122/SMALL/bmw-x1-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-122/SMALL/bmw-x1-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200158, '65f023632bc26470c104b75f', 'bmw-x1-2024-3.jpg', 'bmw-x1-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200158', 'products/65f023632bc26470c104b75f/CAR-SKU-122/SMALL/bmw-x1-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-122/SMALL/bmw-x1-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200159, '65f023632bc26470c104b75f', 'bmw-x1-2024-4.jpg', 'bmw-x1-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200159', 'products/65f023632bc26470c104b75f/CAR-SKU-122/SMALL/bmw-x1-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-122/SMALL/bmw-x1-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200160, '65f023632bc26470c104b75f', 'bmw-x1-2024-5.jpg', 'bmw-x1-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200160', 'products/65f023632bc26470c104b75f/CAR-SKU-122/SMALL/bmw-x1-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-122/SMALL/bmw-x1-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200161, '65f023632bc26470c104b75f', 'mercedes-eqb-2024-1.jpg', 'mercedes-eqb-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200161', 'products/65f023632bc26470c104b75f/CAR-SKU-123/SMALL/mercedes-eqb-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-123/SMALL/mercedes-eqb-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200162, '65f023632bc26470c104b75f', 'mercedes-eqb-2024-2.jpg', 'mercedes-eqb-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200162', 'products/65f023632bc26470c104b75f/CAR-SKU-123/SMALL/mercedes-eqb-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-123/SMALL/mercedes-eqb-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200163, '65f023632bc26470c104b75f', 'mercedes-eqb-2024-3.jpg', 'mercedes-eqb-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200163', 'products/65f023632bc26470c104b75f/CAR-SKU-123/SMALL/mercedes-eqb-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-123/SMALL/mercedes-eqb-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200164, '65f023632bc26470c104b75f', 'mercedes-eqb-2024-4.jpg', 'mercedes-eqb-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200164', 'products/65f023632bc26470c104b75f/CAR-SKU-123/SMALL/mercedes-eqb-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-123/SMALL/mercedes-eqb-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200165, '65f023632bc26470c104b75f', 'mercedes-eqb-2024-5.jpg', 'mercedes-eqb-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200165', 'products/65f023632bc26470c104b75f/CAR-SKU-123/SMALL/mercedes-eqb-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-123/SMALL/mercedes-eqb-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200166, '65f023632bc26470c104b75f', 'hyundai-accent-2020-used-1.jpg', 'hyundai-accent-2020-used-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200166', 'products/65f023632bc26470c104b75f/CAR-SKU-124/SMALL/hyundai-accent-2020-used-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-124/SMALL/hyundai-accent-2020-used-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200167, '65f023632bc26470c104b75f', 'hyundai-accent-2020-used-2.jpg', 'hyundai-accent-2020-used-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200167', 'products/65f023632bc26470c104b75f/CAR-SKU-124/SMALL/hyundai-accent-2020-used-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-124/SMALL/hyundai-accent-2020-used-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200168, '65f023632bc26470c104b75f', 'hyundai-accent-2020-used-3.jpg', 'hyundai-accent-2020-used-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200168', 'products/65f023632bc26470c104b75f/CAR-SKU-124/SMALL/hyundai-accent-2020-used-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-124/SMALL/hyundai-accent-2020-used-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200169, '65f023632bc26470c104b75f', 'hyundai-accent-2020-used-4.jpg', 'hyundai-accent-2020-used-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200169', 'products/65f023632bc26470c104b75f/CAR-SKU-124/SMALL/hyundai-accent-2020-used-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-124/SMALL/hyundai-accent-2020-used-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200170, '65f023632bc26470c104b75f', 'hyundai-accent-2020-used-5.jpg', 'hyundai-accent-2020-used-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200170', 'products/65f023632bc26470c104b75f/CAR-SKU-124/SMALL/hyundai-accent-2020-used-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-124/SMALL/hyundai-accent-2020-used-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200171, '65f023632bc26470c104b75f', 'kia-k5-2024-1.jpg', 'kia-k5-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200171', 'products/65f023632bc26470c104b75f/CAR-SKU-125/SMALL/kia-k5-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-125/SMALL/kia-k5-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200172, '65f023632bc26470c104b75f', 'kia-k5-2024-2.jpg', 'kia-k5-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200172', 'products/65f023632bc26470c104b75f/CAR-SKU-125/SMALL/kia-k5-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-125/SMALL/kia-k5-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200173, '65f023632bc26470c104b75f', 'kia-k5-2024-3.jpg', 'kia-k5-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200173', 'products/65f023632bc26470c104b75f/CAR-SKU-125/SMALL/kia-k5-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-125/SMALL/kia-k5-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200174, '65f023632bc26470c104b75f', 'kia-k5-2024-4.jpg', 'kia-k5-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200174', 'products/65f023632bc26470c104b75f/CAR-SKU-125/SMALL/kia-k5-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-125/SMALL/kia-k5-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200175, '65f023632bc26470c104b75f', 'kia-k5-2024-5.jpg', 'kia-k5-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200175', 'products/65f023632bc26470c104b75f/CAR-SKU-125/SMALL/kia-k5-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-125/SMALL/kia-k5-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200176, '65f023632bc26470c104b75f', 'ford-escape-2024-1.jpg', 'ford-escape-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200176', 'products/65f023632bc26470c104b75f/CAR-SKU-126/SMALL/ford-escape-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-126/SMALL/ford-escape-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200177, '65f023632bc26470c104b75f', 'ford-escape-2024-2.jpg', 'ford-escape-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200177', 'products/65f023632bc26470c104b75f/CAR-SKU-126/SMALL/ford-escape-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-126/SMALL/ford-escape-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200178, '65f023632bc26470c104b75f', 'ford-escape-2024-3.jpg', 'ford-escape-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200178', 'products/65f023632bc26470c104b75f/CAR-SKU-126/SMALL/ford-escape-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-126/SMALL/ford-escape-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200179, '65f023632bc26470c104b75f', 'ford-escape-2024-4.jpg', 'ford-escape-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200179', 'products/65f023632bc26470c104b75f/CAR-SKU-126/SMALL/ford-escape-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-126/SMALL/ford-escape-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200180, '65f023632bc26470c104b75f', 'ford-escape-2024-5.jpg', 'ford-escape-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200180', 'products/65f023632bc26470c104b75f/CAR-SKU-126/SMALL/ford-escape-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-126/SMALL/ford-escape-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200181, '65f023632bc26470c104b75f', 'toyota-bz4x-2024-1.jpg', 'toyota-bz4x-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200181', 'products/65f023632bc26470c104b75f/CAR-SKU-127/SMALL/toyota-bz4x-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-127/SMALL/toyota-bz4x-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200182, '65f023632bc26470c104b75f', 'toyota-bz4x-2024-2.jpg', 'toyota-bz4x-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200182', 'products/65f023632bc26470c104b75f/CAR-SKU-127/SMALL/toyota-bz4x-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-127/SMALL/toyota-bz4x-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200183, '65f023632bc26470c104b75f', 'toyota-bz4x-2024-3.jpg', 'toyota-bz4x-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200183', 'products/65f023632bc26470c104b75f/CAR-SKU-127/SMALL/toyota-bz4x-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-127/SMALL/toyota-bz4x-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200184, '65f023632bc26470c104b75f', 'toyota-bz4x-2024-4.jpg', 'toyota-bz4x-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200184', 'products/65f023632bc26470c104b75f/CAR-SKU-127/SMALL/toyota-bz4x-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-127/SMALL/toyota-bz4x-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200185, '65f023632bc26470c104b75f', 'toyota-bz4x-2024-5.jpg', 'toyota-bz4x-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200185', 'products/65f023632bc26470c104b75f/CAR-SKU-127/SMALL/toyota-bz4x-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-127/SMALL/toyota-bz4x-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200186, '65f023632bc26470c104b75f', 'bmw-x7-2021-used-1.jpg', 'bmw-x7-2021-used-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200186', 'products/65f023632bc26470c104b75f/CAR-SKU-128/SMALL/bmw-x7-2021-used-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-128/SMALL/bmw-x7-2021-used-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200187, '65f023632bc26470c104b75f', 'bmw-x7-2021-used-2.jpg', 'bmw-x7-2021-used-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200187', 'products/65f023632bc26470c104b75f/CAR-SKU-128/SMALL/bmw-x7-2021-used-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-128/SMALL/bmw-x7-2021-used-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200188, '65f023632bc26470c104b75f', 'bmw-x7-2021-used-3.jpg', 'bmw-x7-2021-used-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200188', 'products/65f023632bc26470c104b75f/CAR-SKU-128/SMALL/bmw-x7-2021-used-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-128/SMALL/bmw-x7-2021-used-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200189, '65f023632bc26470c104b75f', 'bmw-x7-2021-used-4.jpg', 'bmw-x7-2021-used-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200189', 'products/65f023632bc26470c104b75f/CAR-SKU-128/SMALL/bmw-x7-2021-used-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-128/SMALL/bmw-x7-2021-used-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200190, '65f023632bc26470c104b75f', 'bmw-x7-2021-used-5.jpg', 'bmw-x7-2021-used-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200190', 'products/65f023632bc26470c104b75f/CAR-SKU-128/SMALL/bmw-x7-2021-used-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-128/SMALL/bmw-x7-2021-used-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200191, '65f023632bc26470c104b75f', 'mercedes-a-class-sedan-2024-1.jpg', 'mercedes-a-class-sedan-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200191', 'products/65f023632bc26470c104b75f/CAR-SKU-129/SMALL/mercedes-a-class-sedan-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-129/SMALL/mercedes-a-class-sedan-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200192, '65f023632bc26470c104b75f', 'mercedes-a-class-sedan-2024-2.jpg', 'mercedes-a-class-sedan-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200192', 'products/65f023632bc26470c104b75f/CAR-SKU-129/SMALL/mercedes-a-class-sedan-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-129/SMALL/mercedes-a-class-sedan-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200193, '65f023632bc26470c104b75f', 'mercedes-a-class-sedan-2024-3.jpg', 'mercedes-a-class-sedan-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200193', 'products/65f023632bc26470c104b75f/CAR-SKU-129/SMALL/mercedes-a-class-sedan-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-129/SMALL/mercedes-a-class-sedan-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200194, '65f023632bc26470c104b75f', 'mercedes-a-class-sedan-2024-4.jpg', 'mercedes-a-class-sedan-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200194', 'products/65f023632bc26470c104b75f/CAR-SKU-129/SMALL/mercedes-a-class-sedan-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-129/SMALL/mercedes-a-class-sedan-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200195, '65f023632bc26470c104b75f', 'mercedes-a-class-sedan-2024-5.jpg', 'mercedes-a-class-sedan-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200195', 'products/65f023632bc26470c104b75f/CAR-SKU-129/SMALL/mercedes-a-class-sedan-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-129/SMALL/mercedes-a-class-sedan-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200196, '65f023632bc26470c104b75f', 'hyundai-palisade-2024-1.jpg', 'hyundai-palisade-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200196', 'products/65f023632bc26470c104b75f/CAR-SKU-130/SMALL/hyundai-palisade-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-130/SMALL/hyundai-palisade-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200197, '65f023632bc26470c104b75f', 'hyundai-palisade-2024-2.jpg', 'hyundai-palisade-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200197', 'products/65f023632bc26470c104b75f/CAR-SKU-130/SMALL/hyundai-palisade-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-130/SMALL/hyundai-palisade-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200198, '65f023632bc26470c104b75f', 'hyundai-palisade-2024-3.jpg', 'hyundai-palisade-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200198', 'products/65f023632bc26470c104b75f/CAR-SKU-130/SMALL/hyundai-palisade-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-130/SMALL/hyundai-palisade-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200199, '65f023632bc26470c104b75f', 'hyundai-palisade-2024-4.jpg', 'hyundai-palisade-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200199', 'products/65f023632bc26470c104b75f/CAR-SKU-130/SMALL/hyundai-palisade-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-130/SMALL/hyundai-palisade-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200200, '65f023632bc26470c104b75f', 'hyundai-palisade-2024-5.jpg', 'hyundai-palisade-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200200', 'products/65f023632bc26470c104b75f/CAR-SKU-130/SMALL/hyundai-palisade-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-130/SMALL/hyundai-palisade-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200201, '65f023632bc26470c104b75f', 'kia-soul-ev-2024-1.jpg', 'kia-soul-ev-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200201', 'products/65f023632bc26470c104b75f/CAR-SKU-131/SMALL/kia-soul-ev-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-131/SMALL/kia-soul-ev-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200202, '65f023632bc26470c104b75f', 'kia-soul-ev-2024-2.jpg', 'kia-soul-ev-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200202', 'products/65f023632bc26470c104b75f/CAR-SKU-131/SMALL/kia-soul-ev-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-131/SMALL/kia-soul-ev-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200203, '65f023632bc26470c104b75f', 'kia-soul-ev-2024-3.jpg', 'kia-soul-ev-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200203', 'products/65f023632bc26470c104b75f/CAR-SKU-131/SMALL/kia-soul-ev-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-131/SMALL/kia-soul-ev-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200204, '65f023632bc26470c104b75f', 'kia-soul-ev-2024-4.jpg', 'kia-soul-ev-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200204', 'products/65f023632bc26470c104b75f/CAR-SKU-131/SMALL/kia-soul-ev-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-131/SMALL/kia-soul-ev-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200205, '65f023632bc26470c104b75f', 'kia-soul-ev-2024-5.jpg', 'kia-soul-ev-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200205', 'products/65f023632bc26470c104b75f/CAR-SKU-131/SMALL/kia-soul-ev-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-131/SMALL/kia-soul-ev-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200206, '65f023632bc26470c104b75f', 'ford-focus-2019-used-1.jpg', 'ford-focus-2019-used-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200206', 'products/65f023632bc26470c104b75f/CAR-SKU-132/SMALL/ford-focus-2019-used-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-132/SMALL/ford-focus-2019-used-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200207, '65f023632bc26470c104b75f', 'ford-focus-2019-used-2.jpg', 'ford-focus-2019-used-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200207', 'products/65f023632bc26470c104b75f/CAR-SKU-132/SMALL/ford-focus-2019-used-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-132/SMALL/ford-focus-2019-used-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200208, '65f023632bc26470c104b75f', 'ford-focus-2019-used-3.jpg', 'ford-focus-2019-used-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200208', 'products/65f023632bc26470c104b75f/CAR-SKU-132/SMALL/ford-focus-2019-used-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-132/SMALL/ford-focus-2019-used-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200209, '65f023632bc26470c104b75f', 'ford-focus-2019-used-4.jpg', 'ford-focus-2019-used-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200209', 'products/65f023632bc26470c104b75f/CAR-SKU-132/SMALL/ford-focus-2019-used-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-132/SMALL/ford-focus-2019-used-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200210, '65f023632bc26470c104b75f', 'ford-focus-2019-used-5.jpg', 'ford-focus-2019-used-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200210', 'products/65f023632bc26470c104b75f/CAR-SKU-132/SMALL/ford-focus-2019-used-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-132/SMALL/ford-focus-2019-used-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200211, '65f023632bc26470c104b75f', 'toyota-crown-2024-1.jpg', 'toyota-crown-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200211', 'products/65f023632bc26470c104b75f/CAR-SKU-133/SMALL/toyota-crown-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-133/SMALL/toyota-crown-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200212, '65f023632bc26470c104b75f', 'toyota-crown-2024-2.jpg', 'toyota-crown-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200212', 'products/65f023632bc26470c104b75f/CAR-SKU-133/SMALL/toyota-crown-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-133/SMALL/toyota-crown-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200213, '65f023632bc26470c104b75f', 'toyota-crown-2024-3.jpg', 'toyota-crown-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200213', 'products/65f023632bc26470c104b75f/CAR-SKU-133/SMALL/toyota-crown-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-133/SMALL/toyota-crown-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200214, '65f023632bc26470c104b75f', 'toyota-crown-2024-4.jpg', 'toyota-crown-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200214', 'products/65f023632bc26470c104b75f/CAR-SKU-133/SMALL/toyota-crown-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-133/SMALL/toyota-crown-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200215, '65f023632bc26470c104b75f', 'toyota-crown-2024-5.jpg', 'toyota-crown-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200215', 'products/65f023632bc26470c104b75f/CAR-SKU-133/SMALL/toyota-crown-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-133/SMALL/toyota-crown-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200216, '65f023632bc26470c104b75f', 'bmw-x6-2024-1.jpg', 'bmw-x6-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200216', 'products/65f023632bc26470c104b75f/CAR-SKU-134/SMALL/bmw-x6-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-134/SMALL/bmw-x6-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200217, '65f023632bc26470c104b75f', 'bmw-x6-2024-2.jpg', 'bmw-x6-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200217', 'products/65f023632bc26470c104b75f/CAR-SKU-134/SMALL/bmw-x6-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-134/SMALL/bmw-x6-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200218, '65f023632bc26470c104b75f', 'bmw-x6-2024-3.jpg', 'bmw-x6-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200218', 'products/65f023632bc26470c104b75f/CAR-SKU-134/SMALL/bmw-x6-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-134/SMALL/bmw-x6-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200219, '65f023632bc26470c104b75f', 'bmw-x6-2024-4.jpg', 'bmw-x6-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200219', 'products/65f023632bc26470c104b75f/CAR-SKU-134/SMALL/bmw-x6-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-134/SMALL/bmw-x6-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200220, '65f023632bc26470c104b75f', 'bmw-x6-2024-5.jpg', 'bmw-x6-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200220', 'products/65f023632bc26470c104b75f/CAR-SKU-134/SMALL/bmw-x6-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-134/SMALL/bmw-x6-2024-5.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200221, '65f023632bc26470c104b75f', 'mercedes-eqc-2024-1.jpg', 'mercedes-eqc-2024-1.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200221', 'products/65f023632bc26470c104b75f/CAR-SKU-135/SMALL/mercedes-eqc-2024-1.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-135/SMALL/mercedes-eqc-2024-1.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200222, '65f023632bc26470c104b75f', 'mercedes-eqc-2024-2.jpg', 'mercedes-eqc-2024-2.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200222', 'products/65f023632bc26470c104b75f/CAR-SKU-135/SMALL/mercedes-eqc-2024-2.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-135/SMALL/mercedes-eqc-2024-2.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200223, '65f023632bc26470c104b75f', 'mercedes-eqc-2024-3.jpg', 'mercedes-eqc-2024-3.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200223', 'products/65f023632bc26470c104b75f/CAR-SKU-135/SMALL/mercedes-eqc-2024-3.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-135/SMALL/mercedes-eqc-2024-3.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200224, '65f023632bc26470c104b75f', 'mercedes-eqc-2024-4.jpg', 'mercedes-eqc-2024-4.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200224', 'products/65f023632bc26470c104b75f/CAR-SKU-135/SMALL/mercedes-eqc-2024-4.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-135/SMALL/mercedes-eqc-2024-4.jpg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-200225, '65f023632bc26470c104b75f', 'mercedes-eqc-2024-5.jpg', 'mercedes-eqc-2024-5.jpg', 'image/jpeg', 'IMAGE',
        0, 'seed-200225', 'products/65f023632bc26470c104b75f/CAR-SKU-135/SMALL/mercedes-eqc-2024-5.jpg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/products/65f023632bc26470c104b75f/CAR-SKU-135/SMALL/mercedes-eqc-2024-5.jpg', now())
on conflict (id) do nothing;
