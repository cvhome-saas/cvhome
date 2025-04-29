/*
generate more product_image relation based on product file  and add 5 relevant images for every product  start product_image_id=451
Image filenames are generated based on product names/SEF URLs.
*/

-- Images for Product 91 (Toyota Camry 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (451, true, false, 0, 'toyota-camry-2024-1.jpg', 0, 91)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (452, false, false, 0, 'toyota-camry-2024-2.jpg', 1, 91)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (453, false, false, 0, 'toyota-camry-2024-3.jpg', 2, 91)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (454, false, false, 0, 'toyota-camry-2024-4.jpg', 3, 91)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (455, false, false, 0, 'toyota-camry-2024-5.jpg', 4, 91)
on conflict do nothing;

-- Images for Product 92 (BMW X5 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (456, true, false, 0, 'bmw-x5-2024-1.jpg', 0, 92)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (457, false, false, 0, 'bmw-x5-2024-2.jpg', 1, 92)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (458, false, false, 0, 'bmw-x5-2024-3.jpg', 2, 92)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (459, false, false, 0, 'bmw-x5-2024-4.jpg', 3, 92)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (460, false, false, 0, 'bmw-x5-2024-5.jpg', 4, 92)
on conflict do nothing;

-- Images for Product 93 (Mercedes EQS 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (461, true, false, 0, 'mercedes-eqs-2024-1.jpg', 0, 93)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (462, false, false, 0, 'mercedes-eqs-2024-2.jpg', 1, 93)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (463, false, false, 0, 'mercedes-eqs-2024-3.jpg', 2, 93)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (464, false, false, 0, 'mercedes-eqs-2024-4.jpg', 3, 93)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (465, false, false, 0, 'mercedes-eqs-2024-5.jpg', 4, 93)
on conflict do nothing;

-- Images for Product 94 (Hyundai Tucson 2021 Used)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (466, true, false, 0, 'hyundai-tucson-2021-used-1.jpg', 0, 94)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (467, false, false, 0, 'hyundai-tucson-2021-used-2.jpg', 1, 94)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (468, false, false, 0, 'hyundai-tucson-2021-used-3.jpg', 2, 94)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (469, false, false, 0, 'hyundai-tucson-2021-used-4.jpg', 3, 94)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (470, false, false, 0, 'hyundai-tucson-2021-used-5.jpg', 4, 94)
on conflict do nothing;

-- Images for Product 95 (Kia Sportage 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (471, true, false, 0, 'kia-sportage-2024-1.jpg', 0, 95)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (472, false, false, 0, 'kia-sportage-2024-2.jpg', 1, 95)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (473, false, false, 0, 'kia-sportage-2024-3.jpg', 2, 95)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (474, false, false, 0, 'kia-sportage-2024-4.jpg', 3, 95)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (475, false, false, 0, 'kia-sportage-2024-5.jpg', 4, 95)
on conflict do nothing;

-- Images for Product 96 (Ford Mustang 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (476, true, false, 0, 'ford-mustang-2024-1.jpg', 0, 96)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (477, false, false, 0, 'ford-mustang-2024-2.jpg', 1, 96)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (478, false, false, 0, 'ford-mustang-2024-3.jpg', 2, 96)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (479, false, false, 0, 'ford-mustang-2024-4.jpg', 3, 96)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (480, false, false, 0, 'ford-mustang-2024-5.jpg', 4, 96)
on conflict do nothing;

-- Images for Product 97 (Toyota RAV4 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (481, true, false, 0, 'toyota-rav4-2024-1.jpg', 0, 97)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (482, false, false, 0, 'toyota-rav4-2024-2.jpg', 1, 97)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (483, false, false, 0, 'toyota-rav4-2024-3.jpg', 2, 97)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (484, false, false, 0, 'toyota-rav4-2024-4.jpg', 3, 97)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (485, false, false, 0, 'toyota-rav4-2024-5.jpg', 4, 97)
on conflict do nothing;

-- Images for Product 98 (BMW 3 Series 2020 Used)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (486, true, false, 0, 'bmw-3-series-2020-used-1.jpg', 0, 98)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (487, false, false, 0, 'bmw-3-series-2020-used-2.jpg', 1, 98)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (488, false, false, 0, 'bmw-3-series-2020-used-3.jpg', 2, 98)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (489, false, false, 0, 'bmw-3-series-2020-used-4.jpg', 3, 98)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (490, false, false, 0, 'bmw-3-series-2020-used-5.jpg', 4, 98)
on conflict do nothing;

-- Images for Product 99 (Mercedes C-Class 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (491, true, false, 0, 'mercedes-c-class-2024-1.jpg', 0, 99)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (492, false, false, 0, 'mercedes-c-class-2024-2.jpg', 1, 99)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (493, false, false, 0, 'mercedes-c-class-2024-3.jpg', 2, 99)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (494, false, false, 0, 'mercedes-c-class-2024-4.jpg', 3, 99)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (495, false, false, 0, 'mercedes-c-class-2024-5.jpg', 4, 99)
on conflict do nothing;

-- Images for Product 100 (Hyundai Elantra 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (496, true, false, 0, 'hyundai-elantra-2024-1.jpg', 0, 100)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (497, false, false, 0, 'hyundai-elantra-2024-2.jpg', 1, 100)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (498, false, false, 0, 'hyundai-elantra-2024-3.jpg', 2, 100)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (499, false, false, 0, 'hyundai-elantra-2024-4.jpg', 3, 100)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (500, false, false, 0, 'hyundai-elantra-2024-5.jpg', 4, 100)
on conflict do nothing;

-- Images for Product 101 (Kia Seltos 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (501, true, false, 0, 'kia-seltos-2024-1.jpg', 0, 101)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (502, false, false, 0, 'kia-seltos-2024-2.jpg', 1, 101)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (503, false, false, 0, 'kia-seltos-2024-3.jpg', 2, 101)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (504, false, false, 0, 'kia-seltos-2024-4.jpg', 3, 101)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (505, false, false, 0, 'kia-seltos-2024-5.jpg', 4, 101)
on conflict do nothing;

-- Images for Product 102 (Ford F-150 2019 Used)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (506, true, false, 0, 'ford-f150-2019-used-1.jpg', 0, 102)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (507, false, false, 0, 'ford-f150-2019-used-2.jpg', 1, 102)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (508, false, false, 0, 'ford-f150-2019-used-3.jpg', 2, 102)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (509, false, false, 0, 'ford-f150-2019-used-4.jpg', 3, 102)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (510, false, false, 0, 'ford-f150-2019-used-5.jpg', 4, 102)
on conflict do nothing;

-- Images for Product 103 (Toyota Corolla 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (511, true, false, 0, 'toyota-corolla-2024-1.jpg', 0, 103)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (512, false, false, 0, 'toyota-corolla-2024-2.jpg', 1, 103)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (513, false, false, 0, 'toyota-corolla-2024-3.jpg', 2, 103)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (514, false, false, 0, 'toyota-corolla-2024-4.jpg', 3, 103)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (515, false, false, 0, 'toyota-corolla-2024-5.jpg', 4, 103)
on conflict do nothing;

-- Images for Product 104 (BMW i4 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (516, true, false, 0, 'bmw-i4-2024-1.jpg', 0, 104)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (517, false, false, 0, 'bmw-i4-2024-2.jpg', 1, 104)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (518, false, false, 0, 'bmw-i4-2024-3.jpg', 2, 104)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (519, false, false, 0, 'bmw-i4-2024-4.jpg', 3, 104)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (520, false, false, 0, 'bmw-i4-2024-5.jpg', 4, 104)
on conflict do nothing;

-- Images for Product 105 (Mercedes E-Class 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (521, true, false, 0, 'mercedes-e-class-2024-1.jpg', 0, 105)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (522, false, false, 0, 'mercedes-e-class-2024-2.jpg', 1, 105)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (523, false, false, 0, 'mercedes-e-class-2024-3.jpg', 2, 105)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (524, false, false, 0, 'mercedes-e-class-2024-4.jpg', 3, 105)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (525, false, false, 0, 'mercedes-e-class-2024-5.jpg', 4, 105)
on conflict do nothing;

-- Images for Product 106 (Hyundai Sonata 2022 Used)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (526, true, false, 0, 'hyundai-sonata-2022-used-1.jpg', 0, 106)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (527, false, false, 0, 'hyundai-sonata-2022-used-2.jpg', 1, 106)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (528, false, false, 0, 'hyundai-sonata-2022-used-3.jpg', 2, 106)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (529, false, false, 0, 'hyundai-sonata-2022-used-4.jpg', 3, 106)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (530, false, false, 0, 'hyundai-sonata-2022-used-5.jpg', 4, 106)
on conflict do nothing;

-- Images for Product 107 (Kia EV6 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (531, true, false, 0, 'kia-ev6-2024-1.jpg', 0, 107)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (532, false, false, 0, 'kia-ev6-2024-2.jpg', 1, 107)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (533, false, false, 0, 'kia-ev6-2024-3.jpg', 2, 107)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (534, false, false, 0, 'kia-ev6-2024-4.jpg', 3, 107)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (535, false, false, 0, 'kia-ev6-2024-5.jpg', 4, 107)
on conflict do nothing;

-- Images for Product 108 (Ford Explorer 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (536, true, false, 0, 'ford-explorer-2024-1.jpg', 0, 108)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (537, false, false, 0, 'ford-explorer-2024-2.jpg', 1, 108)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (538, false, false, 0, 'ford-explorer-2024-3.jpg', 2, 108)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (539, false, false, 0, 'ford-explorer-2024-4.jpg', 3, 108)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (540, false, false, 0, 'ford-explorer-2024-5.jpg', 4, 108)
on conflict do nothing;

-- Images for Product 109 (Toyota Highlander 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (541, true, false, 0, 'toyota-highlander-2024-1.jpg', 0, 109)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (542, false, false, 0, 'toyota-highlander-2024-2.jpg', 1, 109)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (543, false, false, 0, 'toyota-highlander-2024-3.jpg', 2, 109)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (544, false, false, 0, 'toyota-highlander-2024-4.jpg', 3, 109)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (545, false, false, 0, 'toyota-highlander-2024-5.jpg', 4, 109)
on conflict do nothing;

-- Images for Product 110 (BMW X3 2021 Used)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (546, true, false, 0, 'bmw-x3-2021-used-1.jpg', 0, 110)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (547, false, false, 0, 'bmw-x3-2021-used-2.jpg', 1, 110)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (548, false, false, 0, 'bmw-x3-2021-used-3.jpg', 2, 110)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (549, false, false, 0, 'bmw-x3-2021-used-4.jpg', 3, 110)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (550, false, false, 0, 'bmw-x3-2021-used-5.jpg', 4, 110)
on conflict do nothing;

-- Images for Product 111 (Mercedes GLC 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (551, true, false, 0, 'mercedes-glc-2024-1.jpg', 0, 111)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (552, false, false, 0, 'mercedes-glc-2024-2.jpg', 1, 111)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (553, false, false, 0, 'mercedes-glc-2024-3.jpg', 2, 111)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (554, false, false, 0, 'mercedes-glc-2024-4.jpg', 3, 111)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (555, false, false, 0, 'mercedes-glc-2024-5.jpg', 4, 111)
on conflict do nothing;

-- Images for Product 112 (Hyundai Santa Fe 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (556, true, false, 0, 'hyundai-santa-fe-2024-1.jpg', 0, 112)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (557, false, false, 0, 'hyundai-santa-fe-2024-2.jpg', 1, 112)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (558, false, false, 0, 'hyundai-santa-fe-2024-3.jpg', 2, 112)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (559, false, false, 0, 'hyundai-santa-fe-2024-4.jpg', 3, 112)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (560, false, false, 0, 'hyundai-santa-fe-2024-5.jpg', 4, 112)
on conflict do nothing;

-- Images for Product 113 (Kia Telluride 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (561, true, false, 0, 'kia-telluride-2024-1.jpg', 0, 113)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (562, false, false, 0, 'kia-telluride-2024-2.jpg', 1, 113)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (563, false, false, 0, 'kia-telluride-2024-3.jpg', 2, 113)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (564, false, false, 0, 'kia-telluride-2024-4.jpg', 3, 113)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (565, false, false, 0, 'kia-telluride-2024-5.jpg', 4, 113)
on conflict do nothing;

-- Images for Product 114 (Ford Bronco 2022 Used)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (566, true, false, 0, 'ford-bronco-2022-used-1.jpg', 0, 114)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (567, false, false, 0, 'ford-bronco-2022-used-2.jpg', 1, 114)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (568, false, false, 0, 'ford-bronco-2022-used-3.jpg', 2, 114)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (569, false, false, 0, 'ford-bronco-2022-used-4.jpg', 3, 114)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (570, false, false, 0, 'ford-bronco-2022-used-5.jpg', 4, 114)
on conflict do nothing;

-- Images for Product 115 (Toyota Sienna 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (571, true, false, 0, 'toyota-sienna-2024-1.jpg', 0, 115)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (572, false, false, 0, 'toyota-sienna-2024-2.jpg', 1, 115)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (573, false, false, 0, 'toyota-sienna-2024-3.jpg', 2, 115)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (574, false, false, 0, 'toyota-sienna-2024-4.jpg', 3, 115)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (575, false, false, 0, 'toyota-sienna-2024-5.jpg', 4, 115)
on conflict do nothing;

-- Images for Product 116 (BMW 5 Series 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (576, true, false, 0, 'bmw-5-series-2024-1.jpg', 0, 116)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (577, false, false, 0, 'bmw-5-series-2024-2.jpg', 1, 116)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (578, false, false, 0, 'bmw-5-series-2024-3.jpg', 2, 116)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (579, false, false, 0, 'bmw-5-series-2024-4.jpg', 3, 116)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (580, false, false, 0, 'bmw-5-series-2024-5.jpg', 4, 116)
on conflict do nothing;

-- Images for Product 117 (Mercedes S-Class 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (581, true, false, 0, 'mercedes-s-class-2024-1.jpg', 0, 117)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (582, false, false, 0, 'mercedes-s-class-2024-2.jpg', 1, 117)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (583, false, false, 0, 'mercedes-s-class-2024-3.jpg', 2, 117)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (584, false, false, 0, 'mercedes-s-class-2024-4.jpg', 3, 117)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (585, false, false, 0, 'mercedes-s-class-2024-5.jpg', 4, 117)
on conflict do nothing;

-- Images for Product 118 (Hyundai Kona Electric 2022 Used)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (586, true, false, 0, 'hyundai-kona-electric-2022-used-1.jpg', 0, 118)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (587, false, false, 0, 'hyundai-kona-electric-2022-used-2.jpg', 1, 118)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (588, false, false, 0, 'hyundai-kona-electric-2022-used-3.jpg', 2, 118)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (589, false, false, 0, 'hyundai-kona-electric-2022-used-4.jpg', 3, 118)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (590, false, false, 0, 'hyundai-kona-electric-2022-used-5.jpg', 4, 118)
on conflict do nothing;

-- Images for Product 119 (Kia Niro EV 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (591, true, false, 0, 'kia-niro-ev-2024-1.jpg', 0, 119)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (592, false, false, 0, 'kia-niro-ev-2024-2.jpg', 1, 119)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (593, false, false, 0, 'kia-niro-ev-2024-3.jpg', 2, 119)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (594, false, false, 0, 'kia-niro-ev-2024-4.jpg', 3, 119)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (595, false, false, 0, 'kia-niro-ev-2024-5.jpg', 4, 119)
on conflict do nothing;

-- Images for Product 120 (Ford Mustang Mach-E 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (596, true, false, 0, 'ford-mustang-mach-e-2024-1.jpg', 0, 120)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (597, false, false, 0, 'ford-mustang-mach-e-2024-2.jpg', 1, 120)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (598, false, false, 0, 'ford-mustang-mach-e-2024-3.jpg', 2, 120)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (599, false, false, 0, 'ford-mustang-mach-e-2024-4.jpg', 3, 120)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (600, false, false, 0, 'ford-mustang-mach-e-2024-5.jpg', 4, 120)
on conflict do nothing;

-- Images for Product 121 (Toyota Avalon 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (601, true, false, 0, 'toyota-avalon-2024-1.jpg', 0, 121)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (602, false, false, 0, 'toyota-avalon-2024-2.jpg', 1, 121)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (603, false, false, 0, 'toyota-avalon-2024-3.jpg', 2, 121)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (604, false, false, 0, 'toyota-avalon-2024-4.jpg', 3, 121)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (605, false, false, 0, 'toyota-avalon-2024-5.jpg', 4, 121)
on conflict do nothing;

-- Images for Product 122 (BMW X1 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (606, true, false, 0, 'bmw-x1-2024-1.jpg', 0, 122)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (607, false, false, 0, 'bmw-x1-2024-2.jpg', 1, 122)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (608, false, false, 0, 'bmw-x1-2024-3.jpg', 2, 122)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (609, false, false, 0, 'bmw-x1-2024-4.jpg', 3, 122)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (610, false, false, 0, 'bmw-x1-2024-5.jpg', 4, 122)
on conflict do nothing;

-- Images for Product 123 (Mercedes EQB 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (611, true, false, 0, 'mercedes-eqb-2024-1.jpg', 0, 123)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (612, false, false, 0, 'mercedes-eqb-2024-2.jpg', 1, 123)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (613, false, false, 0, 'mercedes-eqb-2024-3.jpg', 2, 123)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (614, false, false, 0, 'mercedes-eqb-2024-4.jpg', 3, 123)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (615, false, false, 0, 'mercedes-eqb-2024-5.jpg', 4, 123)
on conflict do nothing;

-- Images for Product 124 (Hyundai Accent 2020 Used)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (616, true, false, 0, 'hyundai-accent-2020-used-1.jpg', 0, 124)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (617, false, false, 0, 'hyundai-accent-2020-used-2.jpg', 1, 124)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (618, false, false, 0, 'hyundai-accent-2020-used-3.jpg', 2, 124)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (619, false, false, 0, 'hyundai-accent-2020-used-4.jpg', 3, 124)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (620, false, false, 0, 'hyundai-accent-2020-used-5.jpg', 4, 124)
on conflict do nothing;

-- Images for Product 125 (Kia K5 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (621, true, false, 0, 'kia-k5-2024-1.jpg', 0, 125)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (622, false, false, 0, 'kia-k5-2024-2.jpg', 1, 125)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (623, false, false, 0, 'kia-k5-2024-3.jpg', 2, 125)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (624, false, false, 0, 'kia-k5-2024-4.jpg', 3, 125)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (625, false, false, 0, 'kia-k5-2024-5.jpg', 4, 125)
on conflict do nothing;

-- Images for Product 126 (Ford Escape 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (626, true, false, 0, 'ford-escape-2024-1.jpg', 0, 126)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (627, false, false, 0, 'ford-escape-2024-2.jpg', 1, 126)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (628, false, false, 0, 'ford-escape-2024-3.jpg', 2, 126)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (629, false, false, 0, 'ford-escape-2024-4.jpg', 3, 126)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (630, false, false, 0, 'ford-escape-2024-5.jpg', 4, 126)
on conflict do nothing;

-- Images for Product 127 (Toyota bZ4X 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (631, true, false, 0, 'toyota-bz4x-2024-1.jpg', 0, 127)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (632, false, false, 0, 'toyota-bz4x-2024-2.jpg', 1, 127)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (633, false, false, 0, 'toyota-bz4x-2024-3.jpg', 2, 127)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (634, false, false, 0, 'toyota-bz4x-2024-4.jpg', 3, 127)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (635, false, false, 0, 'toyota-bz4x-2024-5.jpg', 4, 127)
on conflict do nothing;

-- Images for Product 128 (BMW X7 2021 Used)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (636, true, false, 0, 'bmw-x7-2021-used-1.jpg', 0, 128)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (637, false, false, 0, 'bmw-x7-2021-used-2.jpg', 1, 128)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (638, false, false, 0, 'bmw-x7-2021-used-3.jpg', 2, 128)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (639, false, false, 0, 'bmw-x7-2021-used-4.jpg', 3, 128)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (640, false, false, 0, 'bmw-x7-2021-used-5.jpg', 4, 128)
on conflict do nothing;

-- Images for Product 129 (Mercedes A-Class Sedan 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (641, true, false, 0, 'mercedes-a-class-sedan-2024-1.jpg', 0, 129)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (642, false, false, 0, 'mercedes-a-class-sedan-2024-2.jpg', 1, 129)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (643, false, false, 0, 'mercedes-a-class-sedan-2024-3.jpg', 2, 129)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (644, false, false, 0, 'mercedes-a-class-sedan-2024-4.jpg', 3, 129)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (645, false, false, 0, 'mercedes-a-class-sedan-2024-5.jpg', 4, 129)
on conflict do nothing;

-- Images for Product 130 (Hyundai Palisade 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (646, true, false, 0, 'hyundai-palisade-2024-1.jpg', 0, 130)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (647, false, false, 0, 'hyundai-palisade-2024-2.jpg', 1, 130)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (648, false, false, 0, 'hyundai-palisade-2024-3.jpg', 2, 130)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (649, false, false, 0, 'hyundai-palisade-2024-4.jpg', 3, 130)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (650, false, false, 0, 'hyundai-palisade-2024-5.jpg', 4, 130)
on conflict do nothing;

-- Images for Product 131 (Kia Soul EV 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (651, true, false, 0, 'kia-soul-ev-2024-1.jpg', 0, 131)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (652, false, false, 0, 'kia-soul-ev-2024-2.jpg', 1, 131)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (653, false, false, 0, 'kia-soul-ev-2024-3.jpg', 2, 131)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (654, false, false, 0, 'kia-soul-ev-2024-4.jpg', 3, 131)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (655, false, false, 0, 'kia-soul-ev-2024-5.jpg', 4, 131)
on conflict do nothing;

-- Images for Product 132 (Ford Focus 2019 Used)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (656, true, false, 0, 'ford-focus-2019-used-1.jpg', 0, 132)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (657, false, false, 0, 'ford-focus-2019-used-2.jpg', 1, 132)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (658, false, false, 0, 'ford-focus-2019-used-3.jpg', 2, 132)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (659, false, false, 0, 'ford-focus-2019-used-4.jpg', 3, 132)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (660, false, false, 0, 'ford-focus-2019-used-5.jpg', 4, 132)
on conflict do nothing;

-- Images for Product 133 (Toyota Crown 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (661, true, false, 0, 'toyota-crown-2024-1.jpg', 0, 133)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (662, false, false, 0, 'toyota-crown-2024-2.jpg', 1, 133)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (663, false, false, 0, 'toyota-crown-2024-3.jpg', 2, 133)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (664, false, false, 0, 'toyota-crown-2024-4.jpg', 3, 133)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (665, false, false, 0, 'toyota-crown-2024-5.jpg', 4, 133)
on conflict do nothing;

-- Images for Product 134 (BMW X6 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (666, true, false, 0, 'bmw-x6-2024-1.jpg', 0, 134)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (667, false, false, 0, 'bmw-x6-2024-2.jpg', 1, 134)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (668, false, false, 0, 'bmw-x6-2024-3.jpg', 2, 134)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (669, false, false, 0, 'bmw-x6-2024-4.jpg', 3, 134)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (670, false, false, 0, 'bmw-x6-2024-5.jpg', 4, 134)
on conflict do nothing;

-- Images for Product 135 (Mercedes EQC 2024)
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (671, true, false, 0, 'mercedes-eqc-2024-1.jpg', 0, 135)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (672, false, false, 0, 'mercedes-eqc-2024-2.jpg', 1, 135)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (673, false, false, 0, 'mercedes-eqc-2024-3.jpg', 2, 135)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (674, false, false, 0, 'mercedes-eqc-2024-4.jpg', 3, 135)
on conflict do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_crop, image_type, product_image, sort_order,
                                   product_id)
VALUES (675, false, false, 0, 'mercedes-eqc-2024-5.jpg', 4, 135)
on conflict do nothing;