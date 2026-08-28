-- image_url holds the asset's path in the bucket, matching content.media_asset.storage_key. The url a browser
-- fetches is that path under the configured CDN base, composed when a product is read. It used to be a whole
-- url, which meant every environment came up serving the demo catalogue from one developer's MinIO.

/*
generate more product_image relation based on product file  and add 5 relevant images for every product  start product_image_id=451
Image filenames are generated based on product names/SEF URLs.
*/

-- Images for Product 91 (Toyota Camry 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (451, true, 0, -200001, 'products/65f023632bc26470c104b75f/CAR-SKU-91/SMALL/toyota-camry-2024-1.jpg', 0, 91)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (452, false, 0, -200002, 'products/65f023632bc26470c104b75f/CAR-SKU-91/SMALL/toyota-camry-2024-2.jpg', 1, 91)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (453, false, 0, -200003, 'products/65f023632bc26470c104b75f/CAR-SKU-91/SMALL/toyota-camry-2024-3.jpg', 2, 91)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (454, false, 0, -200004, 'products/65f023632bc26470c104b75f/CAR-SKU-91/SMALL/toyota-camry-2024-4.jpg', 3, 91)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (455, false, 0, -200005, 'products/65f023632bc26470c104b75f/CAR-SKU-91/SMALL/toyota-camry-2024-5.jpg', 4, 91)
on conflict do nothing;

-- Images for Product 92 (BMW X5 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (456, true, 0, -200006, 'products/65f023632bc26470c104b75f/CAR-SKU-92/SMALL/bmw-x5-2024-1.jpg', 0, 92)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (457, false, 0, -200007, 'products/65f023632bc26470c104b75f/CAR-SKU-92/SMALL/bmw-x5-2024-2.jpg', 1, 92)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (458, false, 0, -200008, 'products/65f023632bc26470c104b75f/CAR-SKU-92/SMALL/bmw-x5-2024-3.jpg', 2, 92)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (459, false, 0, -200009, 'products/65f023632bc26470c104b75f/CAR-SKU-92/SMALL/bmw-x5-2024-4.jpg', 3, 92)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (460, false, 0, -200010, 'products/65f023632bc26470c104b75f/CAR-SKU-92/SMALL/bmw-x5-2024-5.jpg', 4, 92)
on conflict do nothing;

-- Images for Product 93 (Mercedes EQS 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (461, true, 0, -200011, 'products/65f023632bc26470c104b75f/CAR-SKU-93/SMALL/mercedes-eqs-2024-1.jpg', 0, 93)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (462, false, 0, -200012, 'products/65f023632bc26470c104b75f/CAR-SKU-93/SMALL/mercedes-eqs-2024-2.jpg', 1, 93)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (463, false, 0, -200013, 'products/65f023632bc26470c104b75f/CAR-SKU-93/SMALL/mercedes-eqs-2024-3.jpg', 2, 93)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (464, false, 0, -200014, 'products/65f023632bc26470c104b75f/CAR-SKU-93/SMALL/mercedes-eqs-2024-4.jpg', 3, 93)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (465, false, 0, -200015, 'products/65f023632bc26470c104b75f/CAR-SKU-93/SMALL/mercedes-eqs-2024-5.jpg', 4, 93)
on conflict do nothing;

-- Images for Product 94 (Hyundai Tucson 2021 Used)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (466, true, 0, -200016, 'products/65f023632bc26470c104b75f/CAR-SKU-94/SMALL/hyundai-tucson-2021-used-1.jpg', 0, 94)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (467, false, 0, -200017, 'products/65f023632bc26470c104b75f/CAR-SKU-94/SMALL/hyundai-tucson-2021-used-2.jpg', 1, 94)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (468, false, 0, -200018, 'products/65f023632bc26470c104b75f/CAR-SKU-94/SMALL/hyundai-tucson-2021-used-3.jpg', 2, 94)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (469, false, 0, -200019, 'products/65f023632bc26470c104b75f/CAR-SKU-94/SMALL/hyundai-tucson-2021-used-4.jpg', 3, 94)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (470, false, 0, -200020, 'products/65f023632bc26470c104b75f/CAR-SKU-94/SMALL/hyundai-tucson-2021-used-5.jpg', 4, 94)
on conflict do nothing;

-- Images for Product 95 (Kia Sportage 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (471, true, 0, -200021, 'products/65f023632bc26470c104b75f/CAR-SKU-95/SMALL/kia-sportage-2024-1.jpg', 0, 95)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (472, false, 0, -200022, 'products/65f023632bc26470c104b75f/CAR-SKU-95/SMALL/kia-sportage-2024-2.jpg', 1, 95)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (473, false, 0, -200023, 'products/65f023632bc26470c104b75f/CAR-SKU-95/SMALL/kia-sportage-2024-3.jpg', 2, 95)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (474, false, 0, -200024, 'products/65f023632bc26470c104b75f/CAR-SKU-95/SMALL/kia-sportage-2024-4.jpg', 3, 95)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (475, false, 0, -200025, 'products/65f023632bc26470c104b75f/CAR-SKU-95/SMALL/kia-sportage-2024-5.jpg', 4, 95)
on conflict do nothing;

-- Images for Product 96 (Ford Mustang 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (476, true, 0, -200026, 'products/65f023632bc26470c104b75f/CAR-SKU-96/SMALL/ford-mustang-2024-1.jpg', 0, 96)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (477, false, 0, -200027, 'products/65f023632bc26470c104b75f/CAR-SKU-96/SMALL/ford-mustang-2024-2.jpg', 1, 96)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (478, false, 0, -200028, 'products/65f023632bc26470c104b75f/CAR-SKU-96/SMALL/ford-mustang-2024-3.jpg', 2, 96)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (479, false, 0, -200029, 'products/65f023632bc26470c104b75f/CAR-SKU-96/SMALL/ford-mustang-2024-4.jpg', 3, 96)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (480, false, 0, -200030, 'products/65f023632bc26470c104b75f/CAR-SKU-96/SMALL/ford-mustang-2024-5.jpg', 4, 96)
on conflict do nothing;

-- Images for Product 97 (Toyota RAV4 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (481, true, 0, -200031, 'products/65f023632bc26470c104b75f/CAR-SKU-97/SMALL/toyota-rav4-2024-1.jpg', 0, 97)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (482, false, 0, -200032, 'products/65f023632bc26470c104b75f/CAR-SKU-97/SMALL/toyota-rav4-2024-2.jpg', 1, 97)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (483, false, 0, -200033, 'products/65f023632bc26470c104b75f/CAR-SKU-97/SMALL/toyota-rav4-2024-3.jpg', 2, 97)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (484, false, 0, -200034, 'products/65f023632bc26470c104b75f/CAR-SKU-97/SMALL/toyota-rav4-2024-4.jpg', 3, 97)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (485, false, 0, -200035, 'products/65f023632bc26470c104b75f/CAR-SKU-97/SMALL/toyota-rav4-2024-5.jpg', 4, 97)
on conflict do nothing;

-- Images for Product 98 (BMW 3 Series 2020 Used)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (486, true, 0, -200036, 'products/65f023632bc26470c104b75f/CAR-SKU-98/SMALL/bmw-3-series-2020-used-1.jpg', 0, 98)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (487, false, 0, -200037, 'products/65f023632bc26470c104b75f/CAR-SKU-98/SMALL/bmw-3-series-2020-used-2.jpg', 1, 98)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (488, false, 0, -200038, 'products/65f023632bc26470c104b75f/CAR-SKU-98/SMALL/bmw-3-series-2020-used-3.jpg', 2, 98)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (489, false, 0, -200039, 'products/65f023632bc26470c104b75f/CAR-SKU-98/SMALL/bmw-3-series-2020-used-4.jpg', 3, 98)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (490, false, 0, -200040, 'products/65f023632bc26470c104b75f/CAR-SKU-98/SMALL/bmw-3-series-2020-used-5.jpg', 4, 98)
on conflict do nothing;

-- Images for Product 99 (Mercedes C-Class 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (491, true, 0, -200041, 'products/65f023632bc26470c104b75f/CAR-SKU-99/SMALL/mercedes-c-class-2024-1.jpg', 0, 99)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (492, false, 0, -200042, 'products/65f023632bc26470c104b75f/CAR-SKU-99/SMALL/mercedes-c-class-2024-2.jpg', 1, 99)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (493, false, 0, -200043, 'products/65f023632bc26470c104b75f/CAR-SKU-99/SMALL/mercedes-c-class-2024-3.jpg', 2, 99)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (494, false, 0, -200044, 'products/65f023632bc26470c104b75f/CAR-SKU-99/SMALL/mercedes-c-class-2024-4.jpg', 3, 99)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (495, false, 0, -200045, 'products/65f023632bc26470c104b75f/CAR-SKU-99/SMALL/mercedes-c-class-2024-5.jpg', 4, 99)
on conflict do nothing;

-- Images for Product 100 (Hyundai Elantra 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (496, true, 0, -200046, 'products/65f023632bc26470c104b75f/CAR-SKU-100/SMALL/hyundai-elantra-2024-1.jpg', 0, 100)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (497, false, 0, -200047, 'products/65f023632bc26470c104b75f/CAR-SKU-100/SMALL/hyundai-elantra-2024-2.jpg', 1, 100)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (498, false, 0, -200048, 'products/65f023632bc26470c104b75f/CAR-SKU-100/SMALL/hyundai-elantra-2024-3.jpg', 2, 100)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (499, false, 0, -200049, 'products/65f023632bc26470c104b75f/CAR-SKU-100/SMALL/hyundai-elantra-2024-4.jpg', 3, 100)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (500, false, 0, -200050, 'products/65f023632bc26470c104b75f/CAR-SKU-100/SMALL/hyundai-elantra-2024-5.jpg', 4, 100)
on conflict do nothing;

-- Images for Product 101 (Kia Seltos 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (501, true, 0, -200051, 'products/65f023632bc26470c104b75f/CAR-SKU-101/SMALL/kia-seltos-2024-1.jpg', 0, 101)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (502, false, 0, -200052, 'products/65f023632bc26470c104b75f/CAR-SKU-101/SMALL/kia-seltos-2024-2.jpg', 1, 101)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (503, false, 0, -200053, 'products/65f023632bc26470c104b75f/CAR-SKU-101/SMALL/kia-seltos-2024-3.jpg', 2, 101)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (504, false, 0, -200054, 'products/65f023632bc26470c104b75f/CAR-SKU-101/SMALL/kia-seltos-2024-4.jpg', 3, 101)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (505, false, 0, -200055, 'products/65f023632bc26470c104b75f/CAR-SKU-101/SMALL/kia-seltos-2024-5.jpg', 4, 101)
on conflict do nothing;

-- Images for Product 102 (Ford F-150 2019 Used)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (506, true, 0, -200056, 'products/65f023632bc26470c104b75f/CAR-SKU-102/SMALL/ford-f150-2019-used-1.jpg', 0, 102)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (507, false, 0, -200057, 'products/65f023632bc26470c104b75f/CAR-SKU-102/SMALL/ford-f150-2019-used-2.jpg', 1, 102)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (508, false, 0, -200058, 'products/65f023632bc26470c104b75f/CAR-SKU-102/SMALL/ford-f150-2019-used-3.jpg', 2, 102)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (509, false, 0, -200059, 'products/65f023632bc26470c104b75f/CAR-SKU-102/SMALL/ford-f150-2019-used-4.jpg', 3, 102)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (510, false, 0, -200060, 'products/65f023632bc26470c104b75f/CAR-SKU-102/SMALL/ford-f150-2019-used-5.jpg', 4, 102)
on conflict do nothing;

-- Images for Product 103 (Toyota Corolla 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (511, true, 0, -200061, 'products/65f023632bc26470c104b75f/CAR-SKU-103/SMALL/toyota-corolla-2024-1.jpg', 0, 103)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (512, false, 0, -200062, 'products/65f023632bc26470c104b75f/CAR-SKU-103/SMALL/toyota-corolla-2024-2.jpg', 1, 103)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (513, false, 0, -200063, 'products/65f023632bc26470c104b75f/CAR-SKU-103/SMALL/toyota-corolla-2024-3.jpg', 2, 103)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (514, false, 0, -200064, 'products/65f023632bc26470c104b75f/CAR-SKU-103/SMALL/toyota-corolla-2024-4.jpg', 3, 103)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (515, false, 0, -200065, 'products/65f023632bc26470c104b75f/CAR-SKU-103/SMALL/toyota-corolla-2024-5.jpg', 4, 103)
on conflict do nothing;

-- Images for Product 104 (BMW i4 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (516, true, 0, -200066, 'products/65f023632bc26470c104b75f/CAR-SKU-104/SMALL/bmw-i4-2024-1.jpg', 0, 104)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (517, false, 0, -200067, 'products/65f023632bc26470c104b75f/CAR-SKU-104/SMALL/bmw-i4-2024-2.jpg', 1, 104)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (518, false, 0, -200068, 'products/65f023632bc26470c104b75f/CAR-SKU-104/SMALL/bmw-i4-2024-3.jpg', 2, 104)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (519, false, 0, -200069, 'products/65f023632bc26470c104b75f/CAR-SKU-104/SMALL/bmw-i4-2024-4.jpg', 3, 104)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (520, false, 0, -200070, 'products/65f023632bc26470c104b75f/CAR-SKU-104/SMALL/bmw-i4-2024-5.jpg', 4, 104)
on conflict do nothing;

-- Images for Product 105 (Mercedes E-Class 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (521, true, 0, -200071, 'products/65f023632bc26470c104b75f/CAR-SKU-105/SMALL/mercedes-e-class-2024-1.jpg', 0, 105)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (522, false, 0, -200072, 'products/65f023632bc26470c104b75f/CAR-SKU-105/SMALL/mercedes-e-class-2024-2.jpg', 1, 105)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (523, false, 0, -200073, 'products/65f023632bc26470c104b75f/CAR-SKU-105/SMALL/mercedes-e-class-2024-3.jpg', 2, 105)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (524, false, 0, -200074, 'products/65f023632bc26470c104b75f/CAR-SKU-105/SMALL/mercedes-e-class-2024-4.jpg', 3, 105)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (525, false, 0, -200075, 'products/65f023632bc26470c104b75f/CAR-SKU-105/SMALL/mercedes-e-class-2024-5.jpg', 4, 105)
on conflict do nothing;

-- Images for Product 106 (Hyundai Sonata 2022 Used)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (526, true, 0, -200076, 'products/65f023632bc26470c104b75f/CAR-SKU-106/SMALL/hyundai-sonata-2022-used-1.jpg', 0, 106)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (527, false, 0, -200077, 'products/65f023632bc26470c104b75f/CAR-SKU-106/SMALL/hyundai-sonata-2022-used-2.jpg', 1, 106)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (528, false, 0, -200078, 'products/65f023632bc26470c104b75f/CAR-SKU-106/SMALL/hyundai-sonata-2022-used-3.jpg', 2, 106)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (529, false, 0, -200079, 'products/65f023632bc26470c104b75f/CAR-SKU-106/SMALL/hyundai-sonata-2022-used-4.jpg', 3, 106)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (530, false, 0, -200080, 'products/65f023632bc26470c104b75f/CAR-SKU-106/SMALL/hyundai-sonata-2022-used-5.jpg', 4, 106)
on conflict do nothing;

-- Images for Product 107 (Kia EV6 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (531, true, 0, -200081, 'products/65f023632bc26470c104b75f/CAR-SKU-107/SMALL/kia-ev6-2024-1.jpg', 0, 107)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (532, false, 0, -200082, 'products/65f023632bc26470c104b75f/CAR-SKU-107/SMALL/kia-ev6-2024-2.jpg', 1, 107)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (533, false, 0, -200083, 'products/65f023632bc26470c104b75f/CAR-SKU-107/SMALL/kia-ev6-2024-3.jpg', 2, 107)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (534, false, 0, -200084, 'products/65f023632bc26470c104b75f/CAR-SKU-107/SMALL/kia-ev6-2024-4.jpg', 3, 107)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (535, false, 0, -200085, 'products/65f023632bc26470c104b75f/CAR-SKU-107/SMALL/kia-ev6-2024-5.jpg', 4, 107)
on conflict do nothing;

-- Images for Product 108 (Ford Explorer 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (536, true, 0, -200086, 'products/65f023632bc26470c104b75f/CAR-SKU-108/SMALL/ford-explorer-2024-1.jpg', 0, 108)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (537, false, 0, -200087, 'products/65f023632bc26470c104b75f/CAR-SKU-108/SMALL/ford-explorer-2024-2.jpg', 1, 108)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (538, false, 0, -200088, 'products/65f023632bc26470c104b75f/CAR-SKU-108/SMALL/ford-explorer-2024-3.jpg', 2, 108)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (539, false, 0, -200089, 'products/65f023632bc26470c104b75f/CAR-SKU-108/SMALL/ford-explorer-2024-4.jpg', 3, 108)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (540, false, 0, -200090, 'products/65f023632bc26470c104b75f/CAR-SKU-108/SMALL/ford-explorer-2024-5.jpg', 4, 108)
on conflict do nothing;

-- Images for Product 109 (Toyota Highlander 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (541, true, 0, -200091, 'products/65f023632bc26470c104b75f/CAR-SKU-109/SMALL/toyota-highlander-2024-1.jpg', 0, 109)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (542, false, 0, -200092, 'products/65f023632bc26470c104b75f/CAR-SKU-109/SMALL/toyota-highlander-2024-2.jpg', 1, 109)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (543, false, 0, -200093, 'products/65f023632bc26470c104b75f/CAR-SKU-109/SMALL/toyota-highlander-2024-3.jpg', 2, 109)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (544, false, 0, -200094, 'products/65f023632bc26470c104b75f/CAR-SKU-109/SMALL/toyota-highlander-2024-4.jpg', 3, 109)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (545, false, 0, -200095, 'products/65f023632bc26470c104b75f/CAR-SKU-109/SMALL/toyota-highlander-2024-5.jpg', 4, 109)
on conflict do nothing;

-- Images for Product 110 (BMW X3 2021 Used)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (546, true, 0, -200096, 'products/65f023632bc26470c104b75f/CAR-SKU-110/SMALL/bmw-x3-2021-used-1.jpg', 0, 110)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (547, false, 0, -200097, 'products/65f023632bc26470c104b75f/CAR-SKU-110/SMALL/bmw-x3-2021-used-2.jpg', 1, 110)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (548, false, 0, -200098, 'products/65f023632bc26470c104b75f/CAR-SKU-110/SMALL/bmw-x3-2021-used-3.jpg', 2, 110)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (549, false, 0, -200099, 'products/65f023632bc26470c104b75f/CAR-SKU-110/SMALL/bmw-x3-2021-used-4.jpg', 3, 110)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (550, false, 0, -200100, 'products/65f023632bc26470c104b75f/CAR-SKU-110/SMALL/bmw-x3-2021-used-5.jpg', 4, 110)
on conflict do nothing;

-- Images for Product 111 (Mercedes GLC 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (551, true, 0, -200101, 'products/65f023632bc26470c104b75f/CAR-SKU-111/SMALL/mercedes-glc-2024-1.jpg', 0, 111)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (552, false, 0, -200102, 'products/65f023632bc26470c104b75f/CAR-SKU-111/SMALL/mercedes-glc-2024-2.jpg', 1, 111)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (553, false, 0, -200103, 'products/65f023632bc26470c104b75f/CAR-SKU-111/SMALL/mercedes-glc-2024-3.jpg', 2, 111)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (554, false, 0, -200104, 'products/65f023632bc26470c104b75f/CAR-SKU-111/SMALL/mercedes-glc-2024-4.jpg', 3, 111)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (555, false, 0, -200105, 'products/65f023632bc26470c104b75f/CAR-SKU-111/SMALL/mercedes-glc-2024-5.jpg', 4, 111)
on conflict do nothing;

-- Images for Product 112 (Hyundai Santa Fe 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (556, true, 0, -200106, 'products/65f023632bc26470c104b75f/CAR-SKU-112/SMALL/hyundai-santa-fe-2024-1.jpg', 0, 112)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (557, false, 0, -200107, 'products/65f023632bc26470c104b75f/CAR-SKU-112/SMALL/hyundai-santa-fe-2024-2.jpg', 1, 112)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (558, false, 0, -200108, 'products/65f023632bc26470c104b75f/CAR-SKU-112/SMALL/hyundai-santa-fe-2024-3.jpg', 2, 112)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (559, false, 0, -200109, 'products/65f023632bc26470c104b75f/CAR-SKU-112/SMALL/hyundai-santa-fe-2024-4.jpg', 3, 112)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (560, false, 0, -200110, 'products/65f023632bc26470c104b75f/CAR-SKU-112/SMALL/hyundai-santa-fe-2024-5.jpg', 4, 112)
on conflict do nothing;

-- Images for Product 113 (Kia Telluride 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (561, true, 0, -200111, 'products/65f023632bc26470c104b75f/CAR-SKU-113/SMALL/kia-telluride-2024-1.jpg', 0, 113)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (562, false, 0, -200112, 'products/65f023632bc26470c104b75f/CAR-SKU-113/SMALL/kia-telluride-2024-2.jpg', 1, 113)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (563, false, 0, -200113, 'products/65f023632bc26470c104b75f/CAR-SKU-113/SMALL/kia-telluride-2024-3.jpg', 2, 113)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (564, false, 0, -200114, 'products/65f023632bc26470c104b75f/CAR-SKU-113/SMALL/kia-telluride-2024-4.jpg', 3, 113)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (565, false, 0, -200115, 'products/65f023632bc26470c104b75f/CAR-SKU-113/SMALL/kia-telluride-2024-5.jpg', 4, 113)
on conflict do nothing;

-- Images for Product 114 (Ford Bronco 2022 Used)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (566, true, 0, -200116, 'products/65f023632bc26470c104b75f/CAR-SKU-114/SMALL/ford-bronco-2022-used-1.jpg', 0, 114)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (567, false, 0, -200117, 'products/65f023632bc26470c104b75f/CAR-SKU-114/SMALL/ford-bronco-2022-used-2.jpg', 1, 114)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (568, false, 0, -200118, 'products/65f023632bc26470c104b75f/CAR-SKU-114/SMALL/ford-bronco-2022-used-3.jpg', 2, 114)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (569, false, 0, -200119, 'products/65f023632bc26470c104b75f/CAR-SKU-114/SMALL/ford-bronco-2022-used-4.jpg', 3, 114)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (570, false, 0, -200120, 'products/65f023632bc26470c104b75f/CAR-SKU-114/SMALL/ford-bronco-2022-used-5.jpg', 4, 114)
on conflict do nothing;

-- Images for Product 115 (Toyota Sienna 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (571, true, 0, -200121, 'products/65f023632bc26470c104b75f/CAR-SKU-115/SMALL/toyota-sienna-2024-1.jpg', 0, 115)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (572, false, 0, -200122, 'products/65f023632bc26470c104b75f/CAR-SKU-115/SMALL/toyota-sienna-2024-2.jpg', 1, 115)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (573, false, 0, -200123, 'products/65f023632bc26470c104b75f/CAR-SKU-115/SMALL/toyota-sienna-2024-3.jpg', 2, 115)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (574, false, 0, -200124, 'products/65f023632bc26470c104b75f/CAR-SKU-115/SMALL/toyota-sienna-2024-4.jpg', 3, 115)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (575, false, 0, -200125, 'products/65f023632bc26470c104b75f/CAR-SKU-115/SMALL/toyota-sienna-2024-5.jpg', 4, 115)
on conflict do nothing;

-- Images for Product 116 (BMW 5 Series 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (576, true, 0, -200126, 'products/65f023632bc26470c104b75f/CAR-SKU-116/SMALL/bmw-5-series-2024-1.jpg', 0, 116)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (577, false, 0, -200127, 'products/65f023632bc26470c104b75f/CAR-SKU-116/SMALL/bmw-5-series-2024-2.jpg', 1, 116)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (578, false, 0, -200128, 'products/65f023632bc26470c104b75f/CAR-SKU-116/SMALL/bmw-5-series-2024-3.jpg', 2, 116)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (579, false, 0, -200129, 'products/65f023632bc26470c104b75f/CAR-SKU-116/SMALL/bmw-5-series-2024-4.jpg', 3, 116)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (580, false, 0, -200130, 'products/65f023632bc26470c104b75f/CAR-SKU-116/SMALL/bmw-5-series-2024-5.jpg', 4, 116)
on conflict do nothing;

-- Images for Product 117 (Mercedes S-Class 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (581, true, 0, -200131, 'products/65f023632bc26470c104b75f/CAR-SKU-117/SMALL/mercedes-s-class-2024-1.jpg', 0, 117)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (582, false, 0, -200132, 'products/65f023632bc26470c104b75f/CAR-SKU-117/SMALL/mercedes-s-class-2024-2.jpg', 1, 117)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (583, false, 0, -200133, 'products/65f023632bc26470c104b75f/CAR-SKU-117/SMALL/mercedes-s-class-2024-3.jpg', 2, 117)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (584, false, 0, -200134, 'products/65f023632bc26470c104b75f/CAR-SKU-117/SMALL/mercedes-s-class-2024-4.jpg', 3, 117)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (585, false, 0, -200135, 'products/65f023632bc26470c104b75f/CAR-SKU-117/SMALL/mercedes-s-class-2024-5.jpg', 4, 117)
on conflict do nothing;

-- Images for Product 118 (Hyundai Kona Electric 2022 Used)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (586, true, 0, -200136, 'products/65f023632bc26470c104b75f/CAR-SKU-118/SMALL/hyundai-kona-electric-2022-used-1.jpg', 0, 118)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (587, false, 0, -200137, 'products/65f023632bc26470c104b75f/CAR-SKU-118/SMALL/hyundai-kona-electric-2022-used-2.jpg', 1, 118)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (588, false, 0, -200138, 'products/65f023632bc26470c104b75f/CAR-SKU-118/SMALL/hyundai-kona-electric-2022-used-3.jpg', 2, 118)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (589, false, 0, -200139, 'products/65f023632bc26470c104b75f/CAR-SKU-118/SMALL/hyundai-kona-electric-2022-used-4.jpg', 3, 118)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (590, false, 0, -200140, 'products/65f023632bc26470c104b75f/CAR-SKU-118/SMALL/hyundai-kona-electric-2022-used-5.jpg', 4, 118)
on conflict do nothing;

-- Images for Product 119 (Kia Niro EV 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (591, true, 0, -200141, 'products/65f023632bc26470c104b75f/CAR-SKU-119/SMALL/kia-niro-ev-2024-1.jpg', 0, 119)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (592, false, 0, -200142, 'products/65f023632bc26470c104b75f/CAR-SKU-119/SMALL/kia-niro-ev-2024-2.jpg', 1, 119)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (593, false, 0, -200143, 'products/65f023632bc26470c104b75f/CAR-SKU-119/SMALL/kia-niro-ev-2024-3.jpg', 2, 119)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (594, false, 0, -200144, 'products/65f023632bc26470c104b75f/CAR-SKU-119/SMALL/kia-niro-ev-2024-4.jpg', 3, 119)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (595, false, 0, -200145, 'products/65f023632bc26470c104b75f/CAR-SKU-119/SMALL/kia-niro-ev-2024-5.jpg', 4, 119)
on conflict do nothing;

-- Images for Product 120 (Ford Mustang Mach-E 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (596, true, 0, -200146, 'products/65f023632bc26470c104b75f/CAR-SKU-120/SMALL/ford-mustang-mach-e-2024-1.jpg', 0, 120)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (597, false, 0, -200147, 'products/65f023632bc26470c104b75f/CAR-SKU-120/SMALL/ford-mustang-mach-e-2024-2.jpg', 1, 120)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (598, false, 0, -200148, 'products/65f023632bc26470c104b75f/CAR-SKU-120/SMALL/ford-mustang-mach-e-2024-3.jpg', 2, 120)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (599, false, 0, -200149, 'products/65f023632bc26470c104b75f/CAR-SKU-120/SMALL/ford-mustang-mach-e-2024-4.jpg', 3, 120)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (600, false, 0, -200150, 'products/65f023632bc26470c104b75f/CAR-SKU-120/SMALL/ford-mustang-mach-e-2024-5.jpg', 4, 120)
on conflict do nothing;

-- Images for Product 121 (Toyota Avalon 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (601, true, 0, -200151, 'products/65f023632bc26470c104b75f/CAR-SKU-121/SMALL/toyota-avalon-2024-1.jpg', 0, 121)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (602, false, 0, -200152, 'products/65f023632bc26470c104b75f/CAR-SKU-121/SMALL/toyota-avalon-2024-2.jpg', 1, 121)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (603, false, 0, -200153, 'products/65f023632bc26470c104b75f/CAR-SKU-121/SMALL/toyota-avalon-2024-3.jpg', 2, 121)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (604, false, 0, -200154, 'products/65f023632bc26470c104b75f/CAR-SKU-121/SMALL/toyota-avalon-2024-4.jpg', 3, 121)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (605, false, 0, -200155, 'products/65f023632bc26470c104b75f/CAR-SKU-121/SMALL/toyota-avalon-2024-5.jpg', 4, 121)
on conflict do nothing;

-- Images for Product 122 (BMW X1 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (606, true, 0, -200156, 'products/65f023632bc26470c104b75f/CAR-SKU-122/SMALL/bmw-x1-2024-1.jpg', 0, 122)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (607, false, 0, -200157, 'products/65f023632bc26470c104b75f/CAR-SKU-122/SMALL/bmw-x1-2024-2.jpg', 1, 122)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (608, false, 0, -200158, 'products/65f023632bc26470c104b75f/CAR-SKU-122/SMALL/bmw-x1-2024-3.jpg', 2, 122)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (609, false, 0, -200159, 'products/65f023632bc26470c104b75f/CAR-SKU-122/SMALL/bmw-x1-2024-4.jpg', 3, 122)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (610, false, 0, -200160, 'products/65f023632bc26470c104b75f/CAR-SKU-122/SMALL/bmw-x1-2024-5.jpg', 4, 122)
on conflict do nothing;

-- Images for Product 123 (Mercedes EQB 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (611, true, 0, -200161, 'products/65f023632bc26470c104b75f/CAR-SKU-123/SMALL/mercedes-eqb-2024-1.jpg', 0, 123)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (612, false, 0, -200162, 'products/65f023632bc26470c104b75f/CAR-SKU-123/SMALL/mercedes-eqb-2024-2.jpg', 1, 123)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (613, false, 0, -200163, 'products/65f023632bc26470c104b75f/CAR-SKU-123/SMALL/mercedes-eqb-2024-3.jpg', 2, 123)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (614, false, 0, -200164, 'products/65f023632bc26470c104b75f/CAR-SKU-123/SMALL/mercedes-eqb-2024-4.jpg', 3, 123)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (615, false, 0, -200165, 'products/65f023632bc26470c104b75f/CAR-SKU-123/SMALL/mercedes-eqb-2024-5.jpg', 4, 123)
on conflict do nothing;

-- Images for Product 124 (Hyundai Accent 2020 Used)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (616, true, 0, -200166, 'products/65f023632bc26470c104b75f/CAR-SKU-124/SMALL/hyundai-accent-2020-used-1.jpg', 0, 124)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (617, false, 0, -200167, 'products/65f023632bc26470c104b75f/CAR-SKU-124/SMALL/hyundai-accent-2020-used-2.jpg', 1, 124)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (618, false, 0, -200168, 'products/65f023632bc26470c104b75f/CAR-SKU-124/SMALL/hyundai-accent-2020-used-3.jpg', 2, 124)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (619, false, 0, -200169, 'products/65f023632bc26470c104b75f/CAR-SKU-124/SMALL/hyundai-accent-2020-used-4.jpg', 3, 124)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (620, false, 0, -200170, 'products/65f023632bc26470c104b75f/CAR-SKU-124/SMALL/hyundai-accent-2020-used-5.jpg', 4, 124)
on conflict do nothing;

-- Images for Product 125 (Kia K5 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (621, true, 0, -200171, 'products/65f023632bc26470c104b75f/CAR-SKU-125/SMALL/kia-k5-2024-1.jpg', 0, 125)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (622, false, 0, -200172, 'products/65f023632bc26470c104b75f/CAR-SKU-125/SMALL/kia-k5-2024-2.jpg', 1, 125)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (623, false, 0, -200173, 'products/65f023632bc26470c104b75f/CAR-SKU-125/SMALL/kia-k5-2024-3.jpg', 2, 125)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (624, false, 0, -200174, 'products/65f023632bc26470c104b75f/CAR-SKU-125/SMALL/kia-k5-2024-4.jpg', 3, 125)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (625, false, 0, -200175, 'products/65f023632bc26470c104b75f/CAR-SKU-125/SMALL/kia-k5-2024-5.jpg', 4, 125)
on conflict do nothing;

-- Images for Product 126 (Ford Escape 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (626, true, 0, -200176, 'products/65f023632bc26470c104b75f/CAR-SKU-126/SMALL/ford-escape-2024-1.jpg', 0, 126)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (627, false, 0, -200177, 'products/65f023632bc26470c104b75f/CAR-SKU-126/SMALL/ford-escape-2024-2.jpg', 1, 126)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (628, false, 0, -200178, 'products/65f023632bc26470c104b75f/CAR-SKU-126/SMALL/ford-escape-2024-3.jpg', 2, 126)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (629, false, 0, -200179, 'products/65f023632bc26470c104b75f/CAR-SKU-126/SMALL/ford-escape-2024-4.jpg', 3, 126)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (630, false, 0, -200180, 'products/65f023632bc26470c104b75f/CAR-SKU-126/SMALL/ford-escape-2024-5.jpg', 4, 126)
on conflict do nothing;

-- Images for Product 127 (Toyota bZ4X 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (631, true, 0, -200181, 'products/65f023632bc26470c104b75f/CAR-SKU-127/SMALL/toyota-bz4x-2024-1.jpg', 0, 127)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (632, false, 0, -200182, 'products/65f023632bc26470c104b75f/CAR-SKU-127/SMALL/toyota-bz4x-2024-2.jpg', 1, 127)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (633, false, 0, -200183, 'products/65f023632bc26470c104b75f/CAR-SKU-127/SMALL/toyota-bz4x-2024-3.jpg', 2, 127)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (634, false, 0, -200184, 'products/65f023632bc26470c104b75f/CAR-SKU-127/SMALL/toyota-bz4x-2024-4.jpg', 3, 127)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (635, false, 0, -200185, 'products/65f023632bc26470c104b75f/CAR-SKU-127/SMALL/toyota-bz4x-2024-5.jpg', 4, 127)
on conflict do nothing;

-- Images for Product 128 (BMW X7 2021 Used)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (636, true, 0, -200186, 'products/65f023632bc26470c104b75f/CAR-SKU-128/SMALL/bmw-x7-2021-used-1.jpg', 0, 128)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (637, false, 0, -200187, 'products/65f023632bc26470c104b75f/CAR-SKU-128/SMALL/bmw-x7-2021-used-2.jpg', 1, 128)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (638, false, 0, -200188, 'products/65f023632bc26470c104b75f/CAR-SKU-128/SMALL/bmw-x7-2021-used-3.jpg', 2, 128)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (639, false, 0, -200189, 'products/65f023632bc26470c104b75f/CAR-SKU-128/SMALL/bmw-x7-2021-used-4.jpg', 3, 128)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (640, false, 0, -200190, 'products/65f023632bc26470c104b75f/CAR-SKU-128/SMALL/bmw-x7-2021-used-5.jpg', 4, 128)
on conflict do nothing;

-- Images for Product 129 (Mercedes A-Class Sedan 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (641, true, 0, -200191, 'products/65f023632bc26470c104b75f/CAR-SKU-129/SMALL/mercedes-a-class-sedan-2024-1.jpg', 0, 129)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (642, false, 0, -200192, 'products/65f023632bc26470c104b75f/CAR-SKU-129/SMALL/mercedes-a-class-sedan-2024-2.jpg', 1, 129)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (643, false, 0, -200193, 'products/65f023632bc26470c104b75f/CAR-SKU-129/SMALL/mercedes-a-class-sedan-2024-3.jpg', 2, 129)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (644, false, 0, -200194, 'products/65f023632bc26470c104b75f/CAR-SKU-129/SMALL/mercedes-a-class-sedan-2024-4.jpg', 3, 129)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (645, false, 0, -200195, 'products/65f023632bc26470c104b75f/CAR-SKU-129/SMALL/mercedes-a-class-sedan-2024-5.jpg', 4, 129)
on conflict do nothing;

-- Images for Product 130 (Hyundai Palisade 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (646, true, 0, -200196, 'products/65f023632bc26470c104b75f/CAR-SKU-130/SMALL/hyundai-palisade-2024-1.jpg', 0, 130)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (647, false, 0, -200197, 'products/65f023632bc26470c104b75f/CAR-SKU-130/SMALL/hyundai-palisade-2024-2.jpg', 1, 130)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (648, false, 0, -200198, 'products/65f023632bc26470c104b75f/CAR-SKU-130/SMALL/hyundai-palisade-2024-3.jpg', 2, 130)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (649, false, 0, -200199, 'products/65f023632bc26470c104b75f/CAR-SKU-130/SMALL/hyundai-palisade-2024-4.jpg', 3, 130)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (650, false, 0, -200200, 'products/65f023632bc26470c104b75f/CAR-SKU-130/SMALL/hyundai-palisade-2024-5.jpg', 4, 130)
on conflict do nothing;

-- Images for Product 131 (Kia Soul EV 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (651, true, 0, -200201, 'products/65f023632bc26470c104b75f/CAR-SKU-131/SMALL/kia-soul-ev-2024-1.jpg', 0, 131)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (652, false, 0, -200202, 'products/65f023632bc26470c104b75f/CAR-SKU-131/SMALL/kia-soul-ev-2024-2.jpg', 1, 131)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (653, false, 0, -200203, 'products/65f023632bc26470c104b75f/CAR-SKU-131/SMALL/kia-soul-ev-2024-3.jpg', 2, 131)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (654, false, 0, -200204, 'products/65f023632bc26470c104b75f/CAR-SKU-131/SMALL/kia-soul-ev-2024-4.jpg', 3, 131)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (655, false, 0, -200205, 'products/65f023632bc26470c104b75f/CAR-SKU-131/SMALL/kia-soul-ev-2024-5.jpg', 4, 131)
on conflict do nothing;

-- Images for Product 132 (Ford Focus 2019 Used)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (656, true, 0, -200206, 'products/65f023632bc26470c104b75f/CAR-SKU-132/SMALL/ford-focus-2019-used-1.jpg', 0, 132)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (657, false, 0, -200207, 'products/65f023632bc26470c104b75f/CAR-SKU-132/SMALL/ford-focus-2019-used-2.jpg', 1, 132)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (658, false, 0, -200208, 'products/65f023632bc26470c104b75f/CAR-SKU-132/SMALL/ford-focus-2019-used-3.jpg', 2, 132)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (659, false, 0, -200209, 'products/65f023632bc26470c104b75f/CAR-SKU-132/SMALL/ford-focus-2019-used-4.jpg', 3, 132)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (660, false, 0, -200210, 'products/65f023632bc26470c104b75f/CAR-SKU-132/SMALL/ford-focus-2019-used-5.jpg', 4, 132)
on conflict do nothing;

-- Images for Product 133 (Toyota Crown 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (661, true, 0, -200211, 'products/65f023632bc26470c104b75f/CAR-SKU-133/SMALL/toyota-crown-2024-1.jpg', 0, 133)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (662, false, 0, -200212, 'products/65f023632bc26470c104b75f/CAR-SKU-133/SMALL/toyota-crown-2024-2.jpg', 1, 133)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (663, false, 0, -200213, 'products/65f023632bc26470c104b75f/CAR-SKU-133/SMALL/toyota-crown-2024-3.jpg', 2, 133)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (664, false, 0, -200214, 'products/65f023632bc26470c104b75f/CAR-SKU-133/SMALL/toyota-crown-2024-4.jpg', 3, 133)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (665, false, 0, -200215, 'products/65f023632bc26470c104b75f/CAR-SKU-133/SMALL/toyota-crown-2024-5.jpg', 4, 133)
on conflict do nothing;

-- Images for Product 134 (BMW X6 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (666, true, 0, -200216, 'products/65f023632bc26470c104b75f/CAR-SKU-134/SMALL/bmw-x6-2024-1.jpg', 0, 134)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (667, false, 0, -200217, 'products/65f023632bc26470c104b75f/CAR-SKU-134/SMALL/bmw-x6-2024-2.jpg', 1, 134)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (668, false, 0, -200218, 'products/65f023632bc26470c104b75f/CAR-SKU-134/SMALL/bmw-x6-2024-3.jpg', 2, 134)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (669, false, 0, -200219, 'products/65f023632bc26470c104b75f/CAR-SKU-134/SMALL/bmw-x6-2024-4.jpg', 3, 134)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (670, false, 0, -200220, 'products/65f023632bc26470c104b75f/CAR-SKU-134/SMALL/bmw-x6-2024-5.jpg', 4, 134)
on conflict do nothing;

-- Images for Product 135 (Mercedes EQC 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (671, true, 0, -200221, 'products/65f023632bc26470c104b75f/CAR-SKU-135/SMALL/mercedes-eqc-2024-1.jpg', 0, 135)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (672, false, 0, -200222, 'products/65f023632bc26470c104b75f/CAR-SKU-135/SMALL/mercedes-eqc-2024-2.jpg', 1, 135)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (673, false, 0, -200223, 'products/65f023632bc26470c104b75f/CAR-SKU-135/SMALL/mercedes-eqc-2024-3.jpg', 2, 135)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (674, false, 0, -200224, 'products/65f023632bc26470c104b75f/CAR-SKU-135/SMALL/mercedes-eqc-2024-4.jpg', 3, 135)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (675, false, 0, -200225, 'products/65f023632bc26470c104b75f/CAR-SKU-135/SMALL/mercedes-eqc-2024-5.jpg', 4, 135)
on conflict do nothing;