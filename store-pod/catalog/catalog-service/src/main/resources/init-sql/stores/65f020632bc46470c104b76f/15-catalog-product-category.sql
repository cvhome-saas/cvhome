/*
Generated SQL inserts for product_category relation based on product and category files.
Links products 46-90 to relevant categories 13-24.
*/

-- Product 46: YSL Touche Éclat Illuminating Pen -> Makeup (14), Face Makeup (19)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (46, 14)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (46, 19)
on conflict do nothing;

-- Product 47: Guerlain Abeille Royale Advanced Youth Watery Oil -> Skincare (13)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (47, 13)
on conflict do nothing;

-- Product 48: Shiseido Ultimune Power Infusing Concentrate -> Skincare (13)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (48, 13)
on conflict do nothing;

-- Product 49: NARS Radiant Creamy Concealer -> Makeup (14), Face Makeup (19)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (49, 14)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (49, 19)
on conflict do nothing;

-- Product 50: La Roche-Posay Anthelios UVMUNE 400 Fluid SPF50+ -> Skincare (13)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (50, 13)
on conflict do nothing;

-- Product 51: Kérastase Genesis Bain Hydra-Fortifiant Shampoo -> Haircare (16), Shampoo (23)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (51, 16)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (51, 23)
on conflict do nothing;

-- Product 52: YSL Black Opium Eau de Parfum -> Fragrance (15), Women's Fragrance (21)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (52, 15)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (52, 21)
on conflict do nothing;

-- Product 53: Guerlain Terracotta Bronzing Powder -> Makeup (14), Face Makeup (19)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (53, 14)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (53, 19)
on conflict do nothing;

-- Product 54: Shiseido Benefiance Wrinkle Smoothing Cream -> Skincare (13), Moisturizers (18)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (54, 13)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (54, 18)
on conflict do nothing;

-- Product 55: NARS Orgasm Blush -> Makeup (14), Face Makeup (19)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (55, 14)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (55, 19)
on conflict do nothing;

-- Product 56: La Roche-Posay Cicaplast Baume B5 -> Skincare (13), Moisturizers (18)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (56, 13)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (56, 18)
on conflict do nothing;

-- Product 57: Kérastase Elixir Ultime Original Hair Oil -> Haircare (16)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (57, 16)
on conflict do nothing;

-- Product 58: YSL Libre Eau de Parfum -> Fragrance (15), Women's Fragrance (21)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (58, 15)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (58, 21)
on conflict do nothing;

-- Product 59: Guerlain Météorites Pearls -> Makeup (14), Face Makeup (19)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (59, 14)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (59, 19)
on conflict do nothing;

-- Product 60: Shiseido Synchro Skin Self-Refreshing Foundation -> Makeup (14), Face Makeup (19)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (60, 14)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (60, 19)
on conflict do nothing;

-- Product 61: NARS Laguna Bronzing Powder -> Makeup (14), Face Makeup (19)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (61, 14)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (61, 19)
on conflict do nothing;

-- Product 62: La Roche-Posay Hyalu B5 Serum -> Skincare (13)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (62, 13)
on conflict do nothing;

-- Product 63: Kérastase Blond Absolu Cicaflash Conditioner -> Haircare (16), Conditioner (24)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (63, 16)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (63, 24)
on conflict do nothing;

-- Product 64: YSL Rouge Pur Couture Lipstick -> Makeup (14)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (64, 14)
on conflict do nothing;

-- Product 65: Guerlain Mon Guerlain Eau de Parfum -> Fragrance (15), Women's Fragrance (21)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (65, 15)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (65, 21)
on conflict do nothing;

-- Product 66: Shiseido Vital Perfection Uplifting and Firming Cream -> Skincare (13), Moisturizers (18)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (66, 13)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (66, 18)
on conflict do nothing;

-- Product 67: NARS Sheer Glow Foundation -> Makeup (14), Face Makeup (19)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (67, 14)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (67, 19)
on conflict do nothing;

-- Product 68: La Roche-Posay Effaclar Duo (+) -> Skincare (13), Moisturizers (18) -- Also a treatment, but fits moisturizer category
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (68, 13)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (68, 18)
on conflict do nothing;

-- Product 69: Kérastase Chronologiste Huile de Parfum -> Haircare (16)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (69, 16)
on conflict do nothing;

-- Product 70: YSL Mon Paris Eau de Parfum -> Fragrance (15), Women's Fragrance (21)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (70, 15)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (70, 21)
on conflict do nothing;

-- Product 71: Guerlain Orchidée Impériale The Cream -> Skincare (13), Moisturizers (18)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (71, 13)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (71, 18)
on conflict do nothing;

-- Product 72: Shiseido Minimalist WhippedPowder Blush -> Makeup (14), Face Makeup (19)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (72, 14)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (72, 19)
on conflict do nothing;

-- Product 73: NARS Climax Mascara -> Makeup (14), Eye Makeup (20)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (73, 14)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (73, 20)
on conflict do nothing;

-- Product 74: La Roche-Posay Toleriane Sensitive Crème -> Skincare (13), Moisturizers (18)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (74, 13)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (74, 18)
on conflict do nothing;

-- Product 75: Kérastase Resistance Ciment Thermique -> Haircare (16)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (75, 16)
on conflict do nothing;

-- Product 76: YSL Pure Shots Night Reboot Serum -> Skincare (13)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (76, 13)
on conflict do nothing;

-- Product 77: Guerlain Aqua Allegoria Mandarine Basilic EDT -> Fragrance (15)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (77, 15)
on conflict do nothing;

-- Product 78: Shiseido Tsubaki Premium Repair Mask -> Haircare (16)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (78, 16)
on conflict do nothing;

-- Product 79: NARS Afterglow Lip Balm -> Makeup (14)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (79, 14)
on conflict do nothing;

-- Product 80: La Roche-Posay Lipikar Baume AP+M -> Skincare (13), Moisturizers (18)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (80, 13)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (80, 18)
on conflict do nothing;

-- Product 81: Kérastase Nutritive 8H Magic Night Serum -> Haircare (16)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (81, 16)
on conflict do nothing;

-- Product 82: YSL All Hours Foundation -> Makeup (14), Face Makeup (19)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (82, 14)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (82, 19)
on conflict do nothing;

-- Product 83: Guerlain Super Aqua-Serum -> Skincare (13)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (83, 13)
on conflict do nothing;

-- Product 84: Shiseido Ginza Eau de Parfum -> Fragrance (15), Women's Fragrance (21)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (84, 15)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (84, 21)
on conflict do nothing;

-- Product 85: NARS Light Reflecting Setting Powder -> Makeup (14), Face Makeup (19)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (85, 14)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (85, 19)
on conflict do nothing;

-- Product 86: La Roche-Posay Pure Vitamin C10 Serum -> Skincare (13)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (86, 13)
on conflict do nothing;

-- Product 87: Kérastase Discipline Fluidissime Spray -> Haircare (16)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (87, 16)
on conflict do nothing;

-- Product 88: YSL Y Eau de Parfum -> Fragrance (15), Men's Fragrance (22)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (88, 15)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (88, 22)
on conflict do nothing;

-- Product 89: Guerlain L'Essentiel Natural Glow Foundation -> Makeup (14), Face Makeup (19)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (89, 14)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (89, 19)
on conflict do nothing;

-- Product 90: Shiseido Waso Shikulime Gel-to-Oil Cleanser -> Skincare (13), Cleansers (17)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (90, 13)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (90, 17)
on conflict do nothing;