-- image_url holds the asset's path in the bucket, matching content.media_asset.storage_key. The url a browser
-- fetches is that path under the configured CDN base, composed when a product is read. It used to be a whole
-- url, which meant every environment came up serving the demo catalogue from one developer's MinIO.

-- Generated SQL inserts for product_image relation based on products 46 to 90.
-- Adds 5 relevant images for every product.
-- Starts product_image_id from 226.
-- Image filenames generated based on product SEF URLs (e.g., {sef_url}_1.jpg).
-- Using SEF URLs from the provided context file: /home/ashraf-revo/IdeaProjects/cvhome/extra/test-data/generated/65f020632bc46470c104b76f/14-catalog-product.sql

-- Product 46: YSL Touche Éclat Illuminating Pen (sef_url: ysl-touche-eclat-illuminating-pen)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (226, true, 0, -100001, 'products/65f020632bc46470c104b76f/width in cm
        ''REF-YSL-MAKE-TEIPEN46''/SMALL/ysl-touche-eclat-illuminating-pen_1.jpg', 0, 46)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (227, false, 0, -100002, 'products/65f020632bc46470c104b76f/width in cm
        ''REF-YSL-MAKE-TEIPEN46''/SMALL/ysl-touche-eclat-illuminating-pen_2.jpg', 1, 46)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (228, false, 0, -100003, 'products/65f020632bc46470c104b76f/width in cm
        ''REF-YSL-MAKE-TEIPEN46''/SMALL/ysl-touche-eclat-illuminating-pen_3.jpg', 2, 46)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (229, false, 0, -100004, 'products/65f020632bc46470c104b76f/width in cm
        ''REF-YSL-MAKE-TEIPEN46''/SMALL/ysl-touche-eclat-illuminating-pen_4.jpg', 3, 46)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (230, false, 0, -100005, 'products/65f020632bc46470c104b76f/width in cm
        ''REF-YSL-MAKE-TEIPEN46''/SMALL/ysl-touche-eclat-illuminating-pen_5.jpg', 4, 46)
on conflict (product_image_id) do nothing;

-- Product 47: Guerlain Abeille Royale Advanced Youth Watery Oil (sef_url: guerlain-abeille-royale-advanced-youth-watery-oil)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (231, true, 0, -100006, 'products/65f020632bc46470c104b76f/SKU-GUER-SKIN-ARWOIL47/SMALL/guerlain-abeille-royale-advanced-youth-watery-oil_1.jpg', 0, 47)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (232, false, 0, -100007, 'products/65f020632bc46470c104b76f/SKU-GUER-SKIN-ARWOIL47/SMALL/guerlain-abeille-royale-advanced-youth-watery-oil_2.jpg', 1, 47)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (233, false, 0, -100008, 'products/65f020632bc46470c104b76f/SKU-GUER-SKIN-ARWOIL47/SMALL/guerlain-abeille-royale-advanced-youth-watery-oil_3.jpg', 2, 47)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (234, false, 0, -100009, 'products/65f020632bc46470c104b76f/SKU-GUER-SKIN-ARWOIL47/SMALL/guerlain-abeille-royale-advanced-youth-watery-oil_4.jpg', 3, 47)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (235, false, 0, -100010, 'products/65f020632bc46470c104b76f/SKU-GUER-SKIN-ARWOIL47/SMALL/guerlain-abeille-royale-advanced-youth-watery-oil_5.jpg', 4, 47)
on conflict (product_image_id) do nothing;

-- Product 48: Shiseido Ultimune Power Infusing Concentrate (sef_url: shiseido-ultimune-power-infusing-concentrate)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (236, true, 0, -100011, 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-ULTIMUNE48/SMALL/shiseido-ultimune-power-infusing-concentrate_1.jpg', 0, 48)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (237, false, 0, -100012, 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-ULTIMUNE48/SMALL/shiseido-ultimune-power-infusing-concentrate_2.jpg', 1, 48)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (238, false, 0, -100013, 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-ULTIMUNE48/SMALL/shiseido-ultimune-power-infusing-concentrate_3.jpg', 2, 48)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (239, false, 0, -100014, 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-ULTIMUNE48/SMALL/shiseido-ultimune-power-infusing-concentrate_4.jpg', 3, 48)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (240, false, 0, -100015, 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-ULTIMUNE48/SMALL/shiseido-ultimune-power-infusing-concentrate_5.jpg', 4, 48)
on conflict (product_image_id) do nothing;

-- Product 49: NARS Radiant Creamy Concealer (sef_url: nars-radiant-creamy-concealer)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (241, true, 0, -100016, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-RCCONC49/SMALL/nars-radiant-creamy-concealer_1.jpg', 0, 49)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (242, false, 0, -100017, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-RCCONC49/SMALL/nars-radiant-creamy-concealer_2.jpg', 1, 49)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (243, false, 0, -100018, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-RCCONC49/SMALL/nars-radiant-creamy-concealer_3.jpg', 2, 49)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (244, false, 0, -100019, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-RCCONC49/SMALL/nars-radiant-creamy-concealer_4.jpg', 3, 49)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (245, false, 0, -100020, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-RCCONC49/SMALL/nars-radiant-creamy-concealer_5.jpg', 4, 49)
on conflict (product_image_id) do nothing;

-- Product 50: La Roche-Posay Anthelios UVMUNE 400 Fluid SPF50+ (sef_url: la-roche-posay-anthelios-uvmune-400-spf50)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (246, true, 0, -100021, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-ANTHUV50/SMALL/la-roche-posay-anthelios-uvmune-400-spf50_1.jpg', 0, 50)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (247, false, 0, -100022, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-ANTHUV50/SMALL/la-roche-posay-anthelios-uvmune-400-spf50_2.jpg', 1, 50)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (248, false, 0, -100023, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-ANTHUV50/SMALL/la-roche-posay-anthelios-uvmune-400-spf50_3.jpg', 2, 50)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (249, false, 0, -100024, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-ANTHUV50/SMALL/la-roche-posay-anthelios-uvmune-400-spf50_4.jpg', 3, 50)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (250, false, 0, -100025, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-ANTHUV50/SMALL/la-roche-posay-anthelios-uvmune-400-spf50_5.jpg', 4, 50)
on conflict (product_image_id) do nothing;

-- Product 51: Kérastase Genesis Bain Hydra-Fortifiant Shampoo (sef_url: kerastase-genesis-bain-hydra-fortifiant-shampoo)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (251, true, 0, -100026, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-GENSHMP51/SMALL/kerastase-genesis-bain-hydra-fortifiant-shampoo_1.jpg', 0, 51)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (252, false, 0, -100027, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-GENSHMP51/SMALL/kerastase-genesis-bain-hydra-fortifiant-shampoo_2.jpg', 1, 51)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (253, false, 0, -100028, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-GENSHMP51/SMALL/kerastase-genesis-bain-hydra-fortifiant-shampoo_3.jpg', 2, 51)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (254, false, 0, -100029, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-GENSHMP51/SMALL/kerastase-genesis-bain-hydra-fortifiant-shampoo_4.jpg', 3, 51)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (255, false, 0, -100030, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-GENSHMP51/SMALL/kerastase-genesis-bain-hydra-fortifiant-shampoo_5.jpg', 4, 51)
on conflict (product_image_id) do nothing;

-- Product 52: YSL Black Opium Eau de Parfum (sef_url: ysl-black-opium-eau-de-parfum)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (256, true, 0, -100031, 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-BLACKOPIUM52/SMALL/ysl-black-opium-eau-de-parfum_1.jpg', 0, 52)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (257, false, 0, -100032, 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-BLACKOPIUM52/SMALL/ysl-black-opium-eau-de-parfum_2.jpg', 1, 52)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (258, false, 0, -100033, 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-BLACKOPIUM52/SMALL/ysl-black-opium-eau-de-parfum_3.jpg', 2, 52)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (259, false, 0, -100034, 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-BLACKOPIUM52/SMALL/ysl-black-opium-eau-de-parfum_4.jpg', 3, 52)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (260, false, 0, -100035, 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-BLACKOPIUM52/SMALL/ysl-black-opium-eau-de-parfum_5.jpg', 4, 52)
on conflict (product_image_id) do nothing;

-- Product 53: Guerlain Terracotta Bronzing Powder (sef_url: guerlain-terracotta-bronzing-powder)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (261, true, 0, -100036, 'products/65f020632bc46470c104b76f/SKU-GUER-MAKE-TERRACOTTA53/SMALL/guerlain-terracotta-bronzing-powder_1.jpg', 0, 53)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (262, false, 0, -100037, 'products/65f020632bc46470c104b76f/SKU-GUER-MAKE-TERRACOTTA53/SMALL/guerlain-terracotta-bronzing-powder_2.jpg', 1, 53)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (263, false, 0, -100038, 'products/65f020632bc46470c104b76f/SKU-GUER-MAKE-TERRACOTTA53/SMALL/guerlain-terracotta-bronzing-powder_3.jpg', 2, 53)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (264, false, 0, -100039, 'products/65f020632bc46470c104b76f/SKU-GUER-MAKE-TERRACOTTA53/SMALL/guerlain-terracotta-bronzing-powder_4.jpg', 3, 53)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (265, false, 0, -100040, 'products/65f020632bc46470c104b76f/SKU-GUER-MAKE-TERRACOTTA53/SMALL/guerlain-terracotta-bronzing-powder_5.jpg', 4, 53)
on conflict (product_image_id) do nothing;

-- Product 54: Shiseido Benefiance Wrinkle Smoothing Cream (sef_url: shiseido-benefiance-wrinkle-smoothing-cream)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (266, true, 0, -100041, 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-BENEFCR54/SMALL/shiseido-benefiance-wrinkle-smoothing-cream_1.jpg', 0, 54)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (267, false, 0, -100042, 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-BENEFCR54/SMALL/shiseido-benefiance-wrinkle-smoothing-cream_2.jpg', 1, 54)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (268, false, 0, -100043, 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-BENEFCR54/SMALL/shiseido-benefiance-wrinkle-smoothing-cream_3.jpg', 2, 54)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (269, false, 0, -100044, 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-BENEFCR54/SMALL/shiseido-benefiance-wrinkle-smoothing-cream_4.jpg', 3, 54)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (270, false, 0, -100045, 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-BENEFCR54/SMALL/shiseido-benefiance-wrinkle-smoothing-cream_5.jpg', 4, 54)
on conflict (product_image_id) do nothing;

-- Product 55: NARS Orgasm Blush (sef_url: nars-orgasm-blush)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (271, true, 0, -100046, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-ORGBLUSH55/SMALL/nars-orgasm-blush_1.jpg', 0, 55)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (272, false, 0, -100047, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-ORGBLUSH55/SMALL/nars-orgasm-blush_2.jpg', 1, 55)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (273, false, 0, -100048, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-ORGBLUSH55/SMALL/nars-orgasm-blush_3.jpg', 2, 55)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (274, false, 0, -100049, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-ORGBLUSH55/SMALL/nars-orgasm-blush_4.jpg', 3, 55)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (275, false, 0, -100050, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-ORGBLUSH55/SMALL/nars-orgasm-blush_5.jpg', 4, 55)
on conflict (product_image_id) do nothing;

-- Product 56: La Roche-Posay Cicaplast Baume B5 (sef_url: la-roche-posay-cicaplast-baume-b5)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (276, true, 0, -100051, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-CICAB5-56/SMALL/la-roche-posay-cicaplast-baume-b5_1.jpg', 0, 56)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (277, false, 0, -100052, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-CICAB5-56/SMALL/la-roche-posay-cicaplast-baume-b5_2.jpg', 1, 56)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (278, false, 0, -100053, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-CICAB5-56/SMALL/la-roche-posay-cicaplast-baume-b5_3.jpg', 2, 56)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (279, false, 0, -100054, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-CICAB5-56/SMALL/la-roche-posay-cicaplast-baume-b5_4.jpg', 3, 56)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (280, false, 0, -100055, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-CICAB5-56/SMALL/la-roche-posay-cicaplast-baume-b5_5.jpg', 4, 56)
on conflict (product_image_id) do nothing;

-- Product 57: Kérastase Elixir Ultime Original Hair Oil (sef_url: kerastase-elixir-ultime-original-hair-oil)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (281, true, 0, -100056, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-ELIXIR57/SMALL/kerastase-elixir-ultime-original-hair-oil_1.jpg', 0, 57)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (282, false, 0, -100057, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-ELIXIR57/SMALL/kerastase-elixir-ultime-original-hair-oil_2.jpg', 1, 57)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (283, false, 0, -100058, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-ELIXIR57/SMALL/kerastase-elixir-ultime-original-hair-oil_3.jpg', 2, 57)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (284, false, 0, -100059, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-ELIXIR57/SMALL/kerastase-elixir-ultime-original-hair-oil_4.jpg', 3, 57)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (285, false, 0, -100060, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-ELIXIR57/SMALL/kerastase-elixir-ultime-original-hair-oil_5.jpg', 4, 57)
on conflict (product_image_id) do nothing;

-- Product 58: YSL Libre Eau de Parfum (sef_url: ysl-libre-eau-de-parfum)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (286, true, 0, -100061, 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-LIBREEDP58/SMALL/ysl-libre-eau-de-parfum_1.jpg', 0, 58)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (287, false, 0, -100062, 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-LIBREEDP58/SMALL/ysl-libre-eau-de-parfum_2.jpg', 1, 58)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (288, false, 0, -100063, 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-LIBREEDP58/SMALL/ysl-libre-eau-de-parfum_3.jpg', 2, 58)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (289, false, 0, -100064, 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-LIBREEDP58/SMALL/ysl-libre-eau-de-parfum_4.jpg', 3, 58)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (290, false, 0, -100065, 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-LIBREEDP58/SMALL/ysl-libre-eau-de-parfum_5.jpg', 4, 58)
on conflict (product_image_id) do nothing;

-- Product 59: Guerlain Météorites Pearls (sef_url: guerlain-meteorites-pearls)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (291, true, 0, -100066, 'products/65f020632bc46470c104b76f/SKU-GUER-MAKE-METEORITES59/SMALL/guerlain-meteorites-pearls_1.jpg', 0, 59)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (292, false, 0, -100067, 'products/65f020632bc46470c104b76f/SKU-GUER-MAKE-METEORITES59/SMALL/guerlain-meteorites-pearls_2.jpg', 1, 59)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (293, false, 0, -100068, 'products/65f020632bc46470c104b76f/SKU-GUER-MAKE-METEORITES59/SMALL/guerlain-meteorites-pearls_3.jpg', 2, 59)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (294, false, 0, -100069, 'products/65f020632bc46470c104b76f/SKU-GUER-MAKE-METEORITES59/SMALL/guerlain-meteorites-pearls_4.jpg', 3, 59)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (295, false, 0, -100070, 'products/65f020632bc46470c104b76f/SKU-GUER-MAKE-METEORITES59/SMALL/guerlain-meteorites-pearls_5.jpg', 4, 59)
on conflict (product_image_id) do nothing;

-- Product 60: Shiseido Synchro Skin Self-Refreshing Foundation (sef_url: shiseido-synchro-skin-self-refreshing-foundation)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (296, true, 0, -100071, 'products/65f020632bc46470c104b76f/SKU-SHIS-MAKE-SYNCSKIN60/SMALL/shiseido-synchro-skin-self-refreshing-foundation_1.jpg', 0, 60)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (297, false, 0, -100072, 'products/65f020632bc46470c104b76f/SKU-SHIS-MAKE-SYNCSKIN60/SMALL/shiseido-synchro-skin-self-refreshing-foundation_2.jpg', 1, 60)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (298, false, 0, -100073, 'products/65f020632bc46470c104b76f/SKU-SHIS-MAKE-SYNCSKIN60/SMALL/shiseido-synchro-skin-self-refreshing-foundation_3.jpg', 2, 60)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (299, false, 0, -100074, 'products/65f020632bc46470c104b76f/SKU-SHIS-MAKE-SYNCSKIN60/SMALL/shiseido-synchro-skin-self-refreshing-foundation_4.jpg', 3, 60)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (300, false, 0, -100075, 'products/65f020632bc46470c104b76f/SKU-SHIS-MAKE-SYNCSKIN60/SMALL/shiseido-synchro-skin-self-refreshing-foundation_5.jpg', 4, 60)
on conflict (product_image_id) do nothing;

-- Product 61: NARS Laguna Bronzing Powder (sef_url: nars-laguna-bronzing-powder)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (301, true, 0, -100076, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-LAGUNABRZ61/SMALL/nars-laguna-bronzing-powder_1.jpg', 0, 61)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (302, false, 0, -100077, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-LAGUNABRZ61/SMALL/nars-laguna-bronzing-powder_2.jpg', 1, 61)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (303, false, 0, -100078, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-LAGUNABRZ61/SMALL/nars-laguna-bronzing-powder_3.jpg', 2, 61)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (304, false, 0, -100079, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-LAGUNABRZ61/SMALL/nars-laguna-bronzing-powder_4.jpg', 3, 61)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (305, false, 0, -100080, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-LAGUNABRZ61/SMALL/nars-laguna-bronzing-powder_5.jpg', 4, 61)
on conflict (product_image_id) do nothing;

-- Product 62: La Roche-Posay Hyalu B5 Serum (sef_url: la-roche-posay-hyalu-b5-serum)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (306, true, 0, -100081, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-HYALUB5-62/SMALL/la-roche-posay-hyalu-b5-serum_1.jpg', 0, 62)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (307, false, 0, -100082, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-HYALUB5-62/SMALL/la-roche-posay-hyalu-b5-serum_2.jpg', 1, 62)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (308, false, 0, -100083, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-HYALUB5-62/SMALL/la-roche-posay-hyalu-b5-serum_3.jpg', 2, 62)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (309, false, 0, -100084, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-HYALUB5-62/SMALL/la-roche-posay-hyalu-b5-serum_4.jpg', 3, 62)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (310, false, 0, -100085, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-HYALUB5-62/SMALL/la-roche-posay-hyalu-b5-serum_5.jpg', 4, 62)
on conflict (product_image_id) do nothing;

-- Product 63: Kérastase Blond Absolu Cicaflash Conditioner (sef_url: kerastase-blond-absolu-cicaflash-conditioner)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (311, true, 0, -100086, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-BACICAFLASH63/SMALL/kerastase-blond-absolu-cicaflash-conditioner_1.jpg', 0, 63)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (312, false, 0, -100087, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-BACICAFLASH63/SMALL/kerastase-blond-absolu-cicaflash-conditioner_2.jpg', 1, 63)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (313, false, 0, -100088, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-BACICAFLASH63/SMALL/kerastase-blond-absolu-cicaflash-conditioner_3.jpg', 2, 63)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (314, false, 0, -100089, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-BACICAFLASH63/SMALL/kerastase-blond-absolu-cicaflash-conditioner_4.jpg', 3, 63)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (315, false, 0, -100090, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-BACICAFLASH63/SMALL/kerastase-blond-absolu-cicaflash-conditioner_5.jpg', 4, 63)
on conflict (product_image_id) do nothing;

-- Product 64: YSL Rouge Pur Couture Lipstick (sef_url: ysl-rouge-pur-couture-lipstick)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (316, true, 0, -100091, 'products/65f020632bc46470c104b76f/SKU-YSL-MAKE-RPCLIP64/SMALL/ysl-rouge-pur-couture-lipstick_1.jpg', 0, 64)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (317, false, 0, -100092, 'products/65f020632bc46470c104b76f/SKU-YSL-MAKE-RPCLIP64/SMALL/ysl-rouge-pur-couture-lipstick_2.jpg', 1, 64)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (318, false, 0, -100093, 'products/65f020632bc46470c104b76f/SKU-YSL-MAKE-RPCLIP64/SMALL/ysl-rouge-pur-couture-lipstick_3.jpg', 2, 64)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (319, false, 0, -100094, 'products/65f020632bc46470c104b76f/SKU-YSL-MAKE-RPCLIP64/SMALL/ysl-rouge-pur-couture-lipstick_4.jpg', 3, 64)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (320, false, 0, -100095, 'products/65f020632bc46470c104b76f/SKU-YSL-MAKE-RPCLIP64/SMALL/ysl-rouge-pur-couture-lipstick_5.jpg', 4, 64)
on conflict (product_image_id) do nothing;

-- Product 65: Guerlain Mon Guerlain Eau de Parfum (sef_url: guerlain-mon-guerlain-eau-de-parfum)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (321, true, 0, -100096, 'products/65f020632bc46470c104b76f/SKU-GUER-FRAG-MONGUERLAIN65/SMALL/guerlain-mon-guerlain-eau-de-parfum_1.jpg', 0, 65)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (322, false, 0, -100097, 'products/65f020632bc46470c104b76f/SKU-GUER-FRAG-MONGUERLAIN65/SMALL/guerlain-mon-guerlain-eau-de-parfum_2.jpg', 1, 65)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (323, false, 0, -100098, 'products/65f020632bc46470c104b76f/SKU-GUER-FRAG-MONGUERLAIN65/SMALL/guerlain-mon-guerlain-eau-de-parfum_3.jpg', 2, 65)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (324, false, 0, -100099, 'products/65f020632bc46470c104b76f/SKU-GUER-FRAG-MONGUERLAIN65/SMALL/guerlain-mon-guerlain-eau-de-parfum_4.jpg', 3, 65)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (325, false, 0, -100100, 'products/65f020632bc46470c104b76f/SKU-GUER-FRAG-MONGUERLAIN65/SMALL/guerlain-mon-guerlain-eau-de-parfum_5.jpg', 4, 65)
on conflict (product_image_id) do nothing;

-- Product 66: Shiseido Vital Perfection Uplifting and Firming Cream (sef_url: shiseido-vital-perfection-uplifting-firming-cream)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (326, true, 0, -100101, 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-VPUFCR66/SMALL/shiseido-vital-perfection-uplifting-firming-cream_1.jpg', 0, 66)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (327, false, 0, -100102, 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-VPUFCR66/SMALL/shiseido-vital-perfection-uplifting-firming-cream_2.jpg', 1, 66)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (328, false, 0, -100103, 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-VPUFCR66/SMALL/shiseido-vital-perfection-uplifting-firming-cream_3.jpg', 2, 66)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (329, false, 0, -100104, 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-VPUFCR66/SMALL/shiseido-vital-perfection-uplifting-firming-cream_4.jpg', 3, 66)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (330, false, 0, -100105, 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-VPUFCR66/SMALL/shiseido-vital-perfection-uplifting-firming-cream_5.jpg', 4, 66)
on conflict (product_image_id) do nothing;

-- Product 67: NARS Sheer Glow Foundation (sef_url: nars-sheer-glow-foundation)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (331, true, 0, -100106, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-SHEERGLOW67/SMALL/nars-sheer-glow-foundation_1.jpg', 0, 67)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (332, false, 0, -100107, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-SHEERGLOW67/SMALL/nars-sheer-glow-foundation_2.jpg', 1, 67)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (333, false, 0, -100108, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-SHEERGLOW67/SMALL/nars-sheer-glow-foundation_3.jpg', 2, 67)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (334, false, 0, -100109, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-SHEERGLOW67/SMALL/nars-sheer-glow-foundation_4.jpg', 3, 67)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (335, false, 0, -100110, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-SHEERGLOW67/SMALL/nars-sheer-glow-foundation_5.jpg', 4, 67)
on conflict (product_image_id) do nothing;

-- Product 68: La Roche-Posay Effaclar Duo (+) (sef_url: la-roche-posay-effaclar-duo-plus)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (336, true, 0, -100111, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-EFFADUO68/SMALL/la-roche-posay-effaclar-duo-plus_1.jpg', 0, 68)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (337, false, 0, -100112, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-EFFADUO68/SMALL/la-roche-posay-effaclar-duo-plus_2.jpg', 1, 68)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (338, false, 0, -100113, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-EFFADUO68/SMALL/la-roche-posay-effaclar-duo-plus_3.jpg', 2, 68)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (339, false, 0, -100114, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-EFFADUO68/SMALL/la-roche-posay-effaclar-duo-plus_4.jpg', 3, 68)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (340, false, 0, -100115, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-EFFADUO68/SMALL/la-roche-posay-effaclar-duo-plus_5.jpg', 4, 68)
on conflict (product_image_id) do nothing;

-- Product 69: Kérastase Chronologiste Huile de Parfum (sef_url: kerastase-chronologiste-huile-de-parfum)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (341, true, 0, -100116, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-CHRONOHUILE69/SMALL/kerastase-chronologiste-huile-de-parfum_1.jpg', 0, 69)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (342, false, 0, -100117, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-CHRONOHUILE69/SMALL/kerastase-chronologiste-huile-de-parfum_2.jpg', 1, 69)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (343, false, 0, -100118, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-CHRONOHUILE69/SMALL/kerastase-chronologiste-huile-de-parfum_3.jpg', 2, 69)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (344, false, 0, -100119, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-CHRONOHUILE69/SMALL/kerastase-chronologiste-huile-de-parfum_4.jpg', 3, 69)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (345, false, 0, -100120, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-CHRONOHUILE69/SMALL/kerastase-chronologiste-huile-de-parfum_5.jpg', 4, 69)
on conflict (product_image_id) do nothing;

-- Product 70: YSL Mon Paris Eau de Parfum (sef_url: ysl-mon-paris-eau-de-parfum)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (346, true, 0, -100121, 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-MONPARIS70/SMALL/ysl-mon-paris-eau-de-parfum_1.jpg', 0, 70)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (347, false, 0, -100122, 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-MONPARIS70/SMALL/ysl-mon-paris-eau-de-parfum_2.jpg', 1, 70)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (348, false, 0, -100123, 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-MONPARIS70/SMALL/ysl-mon-paris-eau-de-parfum_3.jpg', 2, 70)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (349, false, 0, -100124, 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-MONPARIS70/SMALL/ysl-mon-paris-eau-de-parfum_4.jpg', 3, 70)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (350, false, 0, -100125, 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-MONPARIS70/SMALL/ysl-mon-paris-eau-de-parfum_5.jpg', 4, 70)
on conflict (product_image_id) do nothing;

-- Product 71: Guerlain Orchidée Impériale The Cream (sef_url: guerlain-orchidee-imperiale-the-cream)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (351, true, 0, -100126, 'products/65f020632bc46470c104b76f/heavier glass
        ''REF-GUER-SKIN-OICREAM71''/SMALL/guerlain-orchidee-imperiale-the-cream_1.jpg', 0, 71)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (352, false, 0, -100127, 'products/65f020632bc46470c104b76f/heavier glass
        ''REF-GUER-SKIN-OICREAM71''/SMALL/guerlain-orchidee-imperiale-the-cream_2.jpg', 1, 71)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (353, false, 0, -100128, 'products/65f020632bc46470c104b76f/heavier glass
        ''REF-GUER-SKIN-OICREAM71''/SMALL/guerlain-orchidee-imperiale-the-cream_3.jpg', 2, 71)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (354, false, 0, -100129, 'products/65f020632bc46470c104b76f/heavier glass
        ''REF-GUER-SKIN-OICREAM71''/SMALL/guerlain-orchidee-imperiale-the-cream_4.jpg', 3, 71)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (355, false, 0, -100130, 'products/65f020632bc46470c104b76f/heavier glass
        ''REF-GUER-SKIN-OICREAM71''/SMALL/guerlain-orchidee-imperiale-the-cream_5.jpg', 4, 71)
on conflict (product_image_id) do nothing;

-- Product 72: Shiseido Minimalist WhippedPowder Blush (sef_url: shiseido-minimalist-whippedpowder-blush)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (356, true, 0, -100131, 'products/65f020632bc46470c104b76f/SKU-SHIS-MAKE-MINBLUSH72/SMALL/shiseido-minimalist-whippedpowder-blush_1.jpg', 0, 72)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (357, false, 0, -100132, 'products/65f020632bc46470c104b76f/SKU-SHIS-MAKE-MINBLUSH72/SMALL/shiseido-minimalist-whippedpowder-blush_2.jpg', 1, 72)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (358, false, 0, -100133, 'products/65f020632bc46470c104b76f/SKU-SHIS-MAKE-MINBLUSH72/SMALL/shiseido-minimalist-whippedpowder-blush_3.jpg', 2, 72)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (359, false, 0, -100134, 'products/65f020632bc46470c104b76f/SKU-SHIS-MAKE-MINBLUSH72/SMALL/shiseido-minimalist-whippedpowder-blush_4.jpg', 3, 72)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (360, false, 0, -100135, 'products/65f020632bc46470c104b76f/SKU-SHIS-MAKE-MINBLUSH72/SMALL/shiseido-minimalist-whippedpowder-blush_5.jpg', 4, 72)
on conflict (product_image_id) do nothing;

-- Product 73: NARS Climax Mascara (sef_url: nars-climax-mascara)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (361, true, 0, -100136, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-CLIMAXMASC73/SMALL/nars-climax-mascara_1.jpg', 0, 73)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (362, false, 0, -100137, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-CLIMAXMASC73/SMALL/nars-climax-mascara_2.jpg', 1, 73)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (363, false, 0, -100138, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-CLIMAXMASC73/SMALL/nars-climax-mascara_3.jpg', 2, 73)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (364, false, 0, -100139, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-CLIMAXMASC73/SMALL/nars-climax-mascara_4.jpg', 3, 73)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (365, false, 0, -100140, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-CLIMAXMASC73/SMALL/nars-climax-mascara_5.jpg', 4, 73)
on conflict (product_image_id) do nothing;

-- Product 74: La Roche-Posay Toleriane Sensitive Crème (sef_url: la-roche-posay-toleriane-sensitive-creme)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (366, true, 0, -100141, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-TOLSENSCR74/SMALL/la-roche-posay-toleriane-sensitive-creme_1.jpg', 0, 74)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (367, false, 0, -100142, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-TOLSENSCR74/SMALL/la-roche-posay-toleriane-sensitive-creme_2.jpg', 1, 74)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (368, false, 0, -100143, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-TOLSENSCR74/SMALL/la-roche-posay-toleriane-sensitive-creme_3.jpg', 2, 74)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (369, false, 0, -100144, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-TOLSENSCR74/SMALL/la-roche-posay-toleriane-sensitive-creme_4.jpg', 3, 74)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (370, false, 0, -100145, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-TOLSENSCR74/SMALL/la-roche-posay-toleriane-sensitive-creme_5.jpg', 4, 74)
on conflict (product_image_id) do nothing;

-- Product 75: Kérastase Resistance Ciment Thermique (sef_url: kerastase-resistance-ciment-thermique)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (371, true, 0, -100146, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-CIMENTHERM75/SMALL/kerastase-resistance-ciment-thermique_1.jpg', 0, 75)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (372, false, 0, -100147, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-CIMENTHERM75/SMALL/kerastase-resistance-ciment-thermique_2.jpg', 1, 75)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (373, false, 0, -100148, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-CIMENTHERM75/SMALL/kerastase-resistance-ciment-thermique_3.jpg', 2, 75)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (374, false, 0, -100149, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-CIMENTHERM75/SMALL/kerastase-resistance-ciment-thermique_4.jpg', 3, 75)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (375, false, 0, -100150, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-CIMENTHERM75/SMALL/kerastase-resistance-ciment-thermique_5.jpg', 4, 75)
on conflict (product_image_id) do nothing;

-- Product 76: YSL Pure Shots Night Reboot Serum (sef_url: ysl-pure-shots-night-reboot-serum)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (376, true, 0, -100151, 'products/65f020632bc46470c104b76f/SKU-YSL-SKIN-PSNIGHT76/SMALL/ysl-pure-shots-night-reboot-serum_1.jpg', 0, 76)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (377, false, 0, -100152, 'products/65f020632bc46470c104b76f/SKU-YSL-SKIN-PSNIGHT76/SMALL/ysl-pure-shots-night-reboot-serum_2.jpg', 1, 76)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (378, false, 0, -100153, 'products/65f020632bc46470c104b76f/SKU-YSL-SKIN-PSNIGHT76/SMALL/ysl-pure-shots-night-reboot-serum_3.jpg', 2, 76)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (379, false, 0, -100154, 'products/65f020632bc46470c104b76f/SKU-YSL-SKIN-PSNIGHT76/SMALL/ysl-pure-shots-night-reboot-serum_4.jpg', 3, 76)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (380, false, 0, -100155, 'products/65f020632bc46470c104b76f/SKU-YSL-SKIN-PSNIGHT76/SMALL/ysl-pure-shots-night-reboot-serum_5.jpg', 4, 76)
on conflict (product_image_id) do nothing;

-- Product 77: Guerlain Aqua Allegoria Mandarine Basilic EDT (sef_url: guerlain-aqua-allegoria-mandarine-basilic-edt)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (381, true, 0, -100156, 'products/65f020632bc46470c104b76f/SKU-GUER-FRAG-AAMANDBAS77/SMALL/guerlain-aqua-allegoria-mandarine-basilic-edt_1.jpg', 0, 77)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (382, false, 0, -100157, 'products/65f020632bc46470c104b76f/SKU-GUER-FRAG-AAMANDBAS77/SMALL/guerlain-aqua-allegoria-mandarine-basilic-edt_2.jpg', 1, 77)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (383, false, 0, -100158, 'products/65f020632bc46470c104b76f/SKU-GUER-FRAG-AAMANDBAS77/SMALL/guerlain-aqua-allegoria-mandarine-basilic-edt_3.jpg', 2, 77)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (384, false, 0, -100159, 'products/65f020632bc46470c104b76f/SKU-GUER-FRAG-AAMANDBAS77/SMALL/guerlain-aqua-allegoria-mandarine-basilic-edt_4.jpg', 3, 77)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (385, false, 0, -100160, 'products/65f020632bc46470c104b76f/SKU-GUER-FRAG-AAMANDBAS77/SMALL/guerlain-aqua-allegoria-mandarine-basilic-edt_5.jpg', 4, 77)
on conflict (product_image_id) do nothing;

-- Product 78: Shiseido Tsubaki Premium Repair Mask (sef_url: shiseido-tsubaki-premium-repair-mask)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (386, true, 0, -100161, 'products/65f020632bc46470c104b76f/SKU-SHIS-HAIR-TSUBAKIMASK78/SMALL/shiseido-tsubaki-premium-repair-mask_1.jpg', 0, 78)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (387, false, 0, -100162, 'products/65f020632bc46470c104b76f/SKU-SHIS-HAIR-TSUBAKIMASK78/SMALL/shiseido-tsubaki-premium-repair-mask_2.jpg', 1, 78)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (388, false, 0, -100163, 'products/65f020632bc46470c104b76f/SKU-SHIS-HAIR-TSUBAKIMASK78/SMALL/shiseido-tsubaki-premium-repair-mask_3.jpg', 2, 78)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (389, false, 0, -100164, 'products/65f020632bc46470c104b76f/SKU-SHIS-HAIR-TSUBAKIMASK78/SMALL/shiseido-tsubaki-premium-repair-mask_4.jpg', 3, 78)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (390, false, 0, -100165, 'products/65f020632bc46470c104b76f/SKU-SHIS-HAIR-TSUBAKIMASK78/SMALL/shiseido-tsubaki-premium-repair-mask_5.jpg', 4, 78)
on conflict (product_image_id) do nothing;

-- Product 79: NARS Afterglow Lip Balm (sef_url: nars-afterglow-lip-balm)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (391, true, 0, -100166, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-AFTERGLOWLB79/SMALL/nars-afterglow-lip-balm_1.jpg', 0, 79)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (392, false, 0, -100167, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-AFTERGLOWLB79/SMALL/nars-afterglow-lip-balm_2.jpg', 1, 79)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (393, false, 0, -100168, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-AFTERGLOWLB79/SMALL/nars-afterglow-lip-balm_3.jpg', 2, 79)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (394, false, 0, -100169, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-AFTERGLOWLB79/SMALL/nars-afterglow-lip-balm_4.jpg', 3, 79)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (395, false, 0, -100170, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-AFTERGLOWLB79/SMALL/nars-afterglow-lip-balm_5.jpg', 4, 79)
on conflict (product_image_id) do nothing;

-- Product 80: La Roche-Posay Lipikar Baume AP+M (sef_url: la-roche-posay-lipikar-baume-ap-m)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (396, true, 0, -100171, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-LIPIAPM80/SMALL/la-roche-posay-lipikar-baume-ap-m_1.jpg', 0, 80)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (397, false, 0, -100172, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-LIPIAPM80/SMALL/la-roche-posay-lipikar-baume-ap-m_2.jpg', 1, 80)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (398, false, 0, -100173, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-LIPIAPM80/SMALL/la-roche-posay-lipikar-baume-ap-m_3.jpg', 2, 80)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (399, false, 0, -100174, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-LIPIAPM80/SMALL/la-roche-posay-lipikar-baume-ap-m_4.jpg', 3, 80)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (400, false, 0, -100175, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-LIPIAPM80/SMALL/la-roche-posay-lipikar-baume-ap-m_5.jpg', 4, 80)
on conflict (product_image_id) do nothing;

-- Product 81: Kérastase Nutritive 8H Magic Night Serum (sef_url: kerastase-nutritive-8h-magic-night-serum)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (401, true, 0, -100176, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-NUTRI8HSERUM81/SMALL/kerastase-nutritive-8h-magic-night-serum_1.jpg', 0, 81)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (402, false, 0, -100177, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-NUTRI8HSERUM81/SMALL/kerastase-nutritive-8h-magic-night-serum_2.jpg', 1, 81)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (403, false, 0, -100178, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-NUTRI8HSERUM81/SMALL/kerastase-nutritive-8h-magic-night-serum_3.jpg', 2, 81)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (404, false, 0, -100179, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-NUTRI8HSERUM81/SMALL/kerastase-nutritive-8h-magic-night-serum_4.jpg', 3, 81)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (405, false, 0, -100180, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-NUTRI8HSERUM81/SMALL/kerastase-nutritive-8h-magic-night-serum_5.jpg', 4, 81)
on conflict (product_image_id) do nothing;

-- Product 82: YSL All Hours Foundation (sef_url: ysl-all-hours-foundation)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (406, true, 0, -100181, 'products/65f020632bc46470c104b76f/SKU-YSL-MAKE-ALLHOURSFND82/SMALL/ysl-all-hours-foundation_1.jpg', 0, 82)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (407, false, 0, -100182, 'products/65f020632bc46470c104b76f/SKU-YSL-MAKE-ALLHOURSFND82/SMALL/ysl-all-hours-foundation_2.jpg', 1, 82)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (408, false, 0, -100183, 'products/65f020632bc46470c104b76f/SKU-YSL-MAKE-ALLHOURSFND82/SMALL/ysl-all-hours-foundation_3.jpg', 2, 82)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (409, false, 0, -100184, 'products/65f020632bc46470c104b76f/SKU-YSL-MAKE-ALLHOURSFND82/SMALL/ysl-all-hours-foundation_4.jpg', 3, 82)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (410, false, 0, -100185, 'products/65f020632bc46470c104b76f/SKU-YSL-MAKE-ALLHOURSFND82/SMALL/ysl-all-hours-foundation_5.jpg', 4, 82)
on conflict (product_image_id) do nothing;

-- Product 83: Guerlain Super Aqua-Serum (sef_url: guerlain-super-aqua-serum)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (411, true, 0, -100186, 'products/65f020632bc46470c104b76f/SKU-GUER-SKIN-SUPERAQUA83/SMALL/guerlain-super-aqua-serum_1.jpg', 0, 83)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (412, false, 0, -100187, 'products/65f020632bc46470c104b76f/SKU-GUER-SKIN-SUPERAQUA83/SMALL/guerlain-super-aqua-serum_2.jpg', 1, 83)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (413, false, 0, -100188, 'products/65f020632bc46470c104b76f/SKU-GUER-SKIN-SUPERAQUA83/SMALL/guerlain-super-aqua-serum_3.jpg', 2, 83)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (414, false, 0, -100189, 'products/65f020632bc46470c104b76f/SKU-GUER-SKIN-SUPERAQUA83/SMALL/guerlain-super-aqua-serum_4.jpg', 3, 83)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (415, false, 0, -100190, 'products/65f020632bc46470c104b76f/SKU-GUER-SKIN-SUPERAQUA83/SMALL/guerlain-super-aqua-serum_5.jpg', 4, 83)
on conflict (product_image_id) do nothing;

-- Product 84: Shiseido Ginza Eau de Parfum (sef_url: shiseido-ginza-eau-de-parfum)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (416, true, 0, -100191, 'products/65f020632bc46470c104b76f/SKU-SHIS-FRAG-GINZAEDP84/SMALL/shiseido-ginza-eau-de-parfum_1.jpg', 0, 84)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (417, false, 0, -100192, 'products/65f020632bc46470c104b76f/SKU-SHIS-FRAG-GINZAEDP84/SMALL/shiseido-ginza-eau-de-parfum_2.jpg', 1, 84)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (418, false, 0, -100193, 'products/65f020632bc46470c104b76f/SKU-SHIS-FRAG-GINZAEDP84/SMALL/shiseido-ginza-eau-de-parfum_3.jpg', 2, 84)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (419, false, 0, -100194, 'products/65f020632bc46470c104b76f/SKU-SHIS-FRAG-GINZAEDP84/SMALL/shiseido-ginza-eau-de-parfum_4.jpg', 3, 84)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (420, false, 0, -100195, 'products/65f020632bc46470c104b76f/SKU-SHIS-FRAG-GINZAEDP84/SMALL/shiseido-ginza-eau-de-parfum_5.jpg', 4, 84)
on conflict (product_image_id) do nothing;

-- Product 85: NARS Light Reflecting Setting Powder (sef_url: nars-light-reflecting-setting-powder)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (421, true, 0, -100196, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-LRSPWD85/SMALL/nars-light-reflecting-setting-powder_1.jpg', 0, 85)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (422, false, 0, -100197, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-LRSPWD85/SMALL/nars-light-reflecting-setting-powder_2.jpg', 1, 85)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (423, false, 0, -100198, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-LRSPWD85/SMALL/nars-light-reflecting-setting-powder_3.jpg', 2, 85)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (424, false, 0, -100199, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-LRSPWD85/SMALL/nars-light-reflecting-setting-powder_4.jpg', 3, 85)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (425, false, 0, -100200, 'products/65f020632bc46470c104b76f/SKU-NARS-MAKE-LRSPWD85/SMALL/nars-light-reflecting-setting-powder_5.jpg', 4, 85)
on conflict (product_image_id) do nothing;

-- Product 86: La Roche-Posay Pure Vitamin C10 Serum (sef_url: la-roche-posay-pure-vitamin-c10-serum)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (426, true, 0, -100201, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-VITC10-86/SMALL/la-roche-posay-pure-vitamin-c10-serum_1.jpg', 0, 86)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (427, false, 0, -100202, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-VITC10-86/SMALL/la-roche-posay-pure-vitamin-c10-serum_2.jpg', 1, 86)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (428, false, 0, -100203, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-VITC10-86/SMALL/la-roche-posay-pure-vitamin-c10-serum_3.jpg', 2, 86)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (429, false, 0, -100204, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-VITC10-86/SMALL/la-roche-posay-pure-vitamin-c10-serum_4.jpg', 3, 86)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (430, false, 0, -100205, 'products/65f020632bc46470c104b76f/SKU-LRP-SKIN-VITC10-86/SMALL/la-roche-posay-pure-vitamin-c10-serum_5.jpg', 4, 86)
on conflict (product_image_id) do nothing;

-- Product 87: Kérastase Discipline Fluidissime Spray (sef_url: kerastase-discipline-fluidissime-spray)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (431, true, 0, -100206, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-DISCFLUID87/SMALL/kerastase-discipline-fluidissime-spray_1.jpg', 0, 87)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (432, false, 0, -100207, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-DISCFLUID87/SMALL/kerastase-discipline-fluidissime-spray_2.jpg', 1, 87)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (433, false, 0, -100208, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-DISCFLUID87/SMALL/kerastase-discipline-fluidissime-spray_3.jpg', 2, 87)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (434, false, 0, -100209, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-DISCFLUID87/SMALL/kerastase-discipline-fluidissime-spray_4.jpg', 3, 87)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (435, false, 0, -100210, 'products/65f020632bc46470c104b76f/SKU-KERA-HAIR-DISCFLUID87/SMALL/kerastase-discipline-fluidissime-spray_5.jpg', 4, 87)
on conflict (product_image_id) do nothing;

-- Product 88: YSL Y Eau de Parfum (sef_url: ysl-y-eau-de-parfum)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (436, true, 0, -100211, 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-YEDP88/SMALL/ysl-y-eau-de-parfum_1.jpg', 0, 88)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (437, false, 0, -100212, 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-YEDP88/SMALL/ysl-y-eau-de-parfum_2.jpg', 1, 88)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (438, false, 0, -100213, 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-YEDP88/SMALL/ysl-y-eau-de-parfum_3.jpg', 2, 88)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (439, false, 0, -100214, 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-YEDP88/SMALL/ysl-y-eau-de-parfum_4.jpg', 3, 88)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (440, false, 0, -100215, 'products/65f020632bc46470c104b76f/SKU-YSL-FRAG-YEDP88/SMALL/ysl-y-eau-de-parfum_5.jpg', 4, 88)
on conflict (product_image_id) do nothing;

-- Product 89: Guerlain L'Essentiel Natural Glow Foundation (sef_url: guerlain-lessentiel-natural-glow-foundation)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (441, true, 0, -100216, 'products/65f020632bc46470c104b76f/SKU-GUER-MAKE-LESSENTIEL89/SMALL/guerlain-lessentiel-natural-glow-foundation_1.jpg', 0, 89)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (442, false, 0, -100217, 'products/65f020632bc46470c104b76f/SKU-GUER-MAKE-LESSENTIEL89/SMALL/guerlain-lessentiel-natural-glow-foundation_2.jpg', 1, 89)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (443, false, 0, -100218, 'products/65f020632bc46470c104b76f/SKU-GUER-MAKE-LESSENTIEL89/SMALL/guerlain-lessentiel-natural-glow-foundation_3.jpg', 2, 89)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (444, false, 0, -100219, 'products/65f020632bc46470c104b76f/SKU-GUER-MAKE-LESSENTIEL89/SMALL/guerlain-lessentiel-natural-glow-foundation_4.jpg', 3, 89)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (445, false, 0, -100220, 'products/65f020632bc46470c104b76f/SKU-GUER-MAKE-LESSENTIEL89/SMALL/guerlain-lessentiel-natural-glow-foundation_5.jpg', 4, 89)
on conflict (product_image_id) do nothing;

-- Product 90: Shiseido Waso Shikulime Gel-to-Oil Cleanser (sef_url: shiseido-waso-shikulime-gel-to-oil-cleanser)
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (446, true, 0, -100221, 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-WASOCLEAN90/SMALL/shiseido-waso-shikulime-gel-to-oil-cleanser_1.jpg', 0, 90)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (447, false, 0, -100222, 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-WASOCLEAN90/SMALL/shiseido-waso-shikulime-gel-to-oil-cleanser_2.jpg', 1, 90)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (448, false, 0, -100223, 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-WASOCLEAN90/SMALL/shiseido-waso-shikulime-gel-to-oil-cleanser_3.jpg', 2, 90)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (449, false, 0, -100224, 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-WASOCLEAN90/SMALL/shiseido-waso-shikulime-gel-to-oil-cleanser_4.jpg', 3, 90)
on conflict (product_image_id) do nothing;
INSERT INTO catalog.product_image (product_image_id, default_image, image_type, media_asset_id, image_url, sort_order, product_id)
VALUES (450, false, 0, -100225, 'products/65f020632bc46470c104b76f/SKU-SHIS-SKIN-WASOCLEAN90/SMALL/shiseido-waso-shikulime-gel-to-oil-cleanser_5.jpg', 4, 90)
on conflict (product_image_id) do nothing;