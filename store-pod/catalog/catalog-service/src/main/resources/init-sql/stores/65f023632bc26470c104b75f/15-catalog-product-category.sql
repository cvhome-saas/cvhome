/*
Generated product_category relations based on products 91-135 and categories 25-36
Store ID: '65f023632bc26470c104b75f'
Domain: Cars
*/

-- Product 91 (Toyota Camry) -> Category 25 (SEDANS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (91, 25)
on conflict do nothing;

-- Product 92 (BMW X5) -> Category 26 (SUVS), 36 (LUXURY_CARS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (92, 26)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (92, 36)
on conflict do nothing;

-- Product 93 (Mercedes EQS) -> Category 27 (ELECTRIC_CARS), 25 (SEDANS), 36 (LUXURY_CARS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (93, 27)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (93, 25)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (93, 36)
on conflict do nothing;

-- Product 94 (Hyundai Tucson - Used) -> Category 28 (USED_CARS), 26 (SUVS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (94, 28)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (94, 26)
on conflict do nothing;

-- Product 95 (Kia Sportage) -> Category 26 (SUVS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (95, 26)
on conflict do nothing;

-- Product 96 (Ford Mustang) -> Category 36 (LUXURY_CARS) -- Assuming Luxury/Sports
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (96, 36)
on conflict do nothing;

-- Product 97 (Toyota RAV4) -> Category 26 (SUVS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (97, 26)
on conflict do nothing;

-- Product 98 (BMW 3 Series - Used) -> Category 28 (USED_CARS), 25 (SEDANS), 36 (LUXURY_CARS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (98, 28)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (98, 25)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (98, 36)
on conflict do nothing;

-- Product 99 (Mercedes C-Class) -> Category 25 (SEDANS), 36 (LUXURY_CARS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (99, 25)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (99, 36)
on conflict do nothing;

-- Product 100 (Hyundai Elantra) -> Category 25 (SEDANS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (100, 25)
on conflict do nothing;

-- Product 101 (Kia Seltos) -> Category 26 (SUVS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (101, 26)
on conflict do nothing;

-- Product 102 (Ford F-150 - Used) -> Category 28 (USED_CARS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (102, 28)
on conflict do nothing;

-- Product 103 (Toyota Corolla) -> Category 25 (SEDANS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (103, 25)
on conflict do nothing;

-- Product 104 (BMW i4) -> Category 27 (ELECTRIC_CARS), 25 (SEDANS), 36 (LUXURY_CARS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (104, 27)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (104, 25)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (104, 36)
on conflict do nothing;

-- Product 105 (Mercedes E-Class) -> Category 25 (SEDANS), 36 (LUXURY_CARS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (105, 25)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (105, 36)
on conflict do nothing;

-- Product 106 (Hyundai Sonata - Used) -> Category 28 (USED_CARS), 25 (SEDANS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (106, 28)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (106, 25)
on conflict do nothing;

-- Product 107 (Kia EV6) -> Category 27 (ELECTRIC_CARS), 26 (SUVS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (107, 27)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (107, 26)
on conflict do nothing;

-- Product 108 (Ford Explorer) -> Category 26 (SUVS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (108, 26)
on conflict do nothing;

-- Product 109 (Toyota Highlander) -> Category 26 (SUVS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (109, 26)
on conflict do nothing;

-- Product 110 (BMW X3 - Used) -> Category 28 (USED_CARS), 26 (SUVS), 36 (LUXURY_CARS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (110, 28)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (110, 26)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (110, 36)
on conflict do nothing;

-- Product 111 (Mercedes GLC) -> Category 26 (SUVS), 36 (LUXURY_CARS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (111, 26)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (111, 36)
on conflict do nothing;

-- Product 112 (Hyundai Santa Fe) -> Category 26 (SUVS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (112, 26)
on conflict do nothing;

-- Product 113 (Kia Telluride) -> Category 26 (SUVS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (113, 26)
on conflict do nothing;

-- Product 114 (Ford Bronco - Used) -> Category 28 (USED_CARS), 26 (SUVS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (114, 28)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (114, 26)
on conflict do nothing;

-- Product 115 (Toyota Sienna) -> Category 26 (SUVS) -- No MINIVAN category, using SUV as placeholder
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (115, 26)
on conflict do nothing;

-- Product 116 (BMW 5 Series) -> Category 25 (SEDANS), 36 (LUXURY_CARS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (116, 25)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (116, 36)
on conflict do nothing;

-- Product 117 (Mercedes S-Class) -> Category 25 (SEDANS), 36 (LUXURY_CARS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (117, 25)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (117, 36)
on conflict do nothing;

-- Product 118 (Hyundai Kona Electric - Used) -> Category 28 (USED_CARS), 27 (ELECTRIC_CARS), 26 (SUVS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (118, 28)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (118, 27)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (118, 26)
on conflict do nothing;

-- Product 119 (Kia Niro EV) -> Category 27 (ELECTRIC_CARS), 26 (SUVS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (119, 27)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (119, 26)
on conflict do nothing;

-- Product 120 (Ford Mustang Mach-E) -> Category 27 (ELECTRIC_CARS), 26 (SUVS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (120, 27)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (120, 26)
on conflict do nothing;

-- Product 121 (Toyota Avalon) -> Category 25 (SEDANS), 36 (LUXURY_CARS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (121, 25)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (121, 36)
on conflict do nothing;

-- Product 122 (BMW X1) -> Category 26 (SUVS), 36 (LUXURY_CARS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (122, 26)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (122, 36)
on conflict do nothing;

-- Product 123 (Mercedes EQB) -> Category 27 (ELECTRIC_CARS), 26 (SUVS), 36 (LUXURY_CARS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (123, 27)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (123, 26)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (123, 36)
on conflict do nothing;

-- Product 124 (Hyundai Accent - Used) -> Category 28 (USED_CARS), 25 (SEDANS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (124, 28)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (124, 25)
on conflict do nothing;

-- Product 125 (Kia K5) -> Category 25 (SEDANS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (125, 25)
on conflict do nothing;

-- Product 126 (Ford Escape) -> Category 26 (SUVS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (126, 26)
on conflict do nothing;

-- Product 127 (Toyota bZ4X) -> Category 27 (ELECTRIC_CARS), 26 (SUVS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (127, 27)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (127, 26)
on conflict do nothing;

-- Product 128 (BMW X7 - Used) -> Category 28 (USED_CARS), 26 (SUVS), 36 (LUXURY_CARS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (128, 28)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (128, 26)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (128, 36)
on conflict do nothing;

-- Product 129 (Mercedes A-Class Sedan) -> Category 25 (SEDANS), 36 (LUXURY_CARS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (129, 25)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (129, 36)
on conflict do nothing;

-- Product 130 (Hyundai Palisade) -> Category 26 (SUVS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (130, 26)
on conflict do nothing;

-- Product 131 (Kia Soul EV) -> Category 27 (ELECTRIC_CARS), 26 (SUVS) -- Treat as subcompact SUV
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (131, 27)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (131, 26)
on conflict do nothing;

-- Product 132 (Ford Focus - Used) -> Category 28 (USED_CARS), 25 (SEDANS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (132, 28)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (132, 25)
on conflict do nothing;

-- Product 133 (Toyota Crown) -> Category 25 (SEDANS), 36 (LUXURY_CARS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (133, 25)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (133, 36)
on conflict do nothing;

-- Product 134 (BMW X6) -> Category 26 (SUVS), 36 (LUXURY_CARS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (134, 26)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (134, 36)
on conflict do nothing;

-- Product 135 (Mercedes EQC) -> Category 27 (ELECTRIC_CARS), 26 (SUVS), 36 (LUXURY_CARS)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (135, 27)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (135, 26)
on conflict do nothing;