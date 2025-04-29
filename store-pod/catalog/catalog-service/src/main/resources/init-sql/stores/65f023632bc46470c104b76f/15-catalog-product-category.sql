/*
Generated SQL inserts for catalog.product_category based on products 1-45 and categories 1-12.
Assigns products to their most specific category and relevant parent categories.
*/

-- Product 1: Nike Men's Running Shoes -> Men (1), Men's Shoes (7)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (1, 1)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (1, 7)
on conflict do nothing;

-- Product 2: Zara Women's Midi Dress -> Women (2), Women's Dresses (8)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (2, 2)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (2, 8)
on conflict do nothing;

-- Product 3: Adidas Men's Track Pants -> Men (1), Men's Bottoms (6)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (3, 1)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (3, 6)
on conflict do nothing;

-- Product 4: H&M Women's Knit Sweater -> Women (2), Women's Tops (9)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (4, 2)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (4, 9)
on conflict do nothing;

-- Product 5: Gucci Marmont Shoulder Bag -> Women (2), Accessories (4)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (5, 2)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (5, 4)
on conflict do nothing;

-- Product 6: Chanel Sunglasses -> Women (2), Accessories (4)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (6, 2)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (6, 4)
on conflict do nothing;

-- Product 7: Nike Kids' Hoodie -> Kids (3), Boys (11), Girls (12)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (7, 3)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (7, 11)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (7, 12)
on conflict do nothing;

-- Product 8: Zara Men's Sneakers -> Men (1), Men's Shoes (7)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (8, 1)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (8, 7)
on conflict do nothing;

-- Product 9: Adidas Women's Backpack -> Women (2), Accessories (4)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (9, 2)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (9, 4)
on conflict do nothing;

-- Product 10: H&M Men's Belt -> Men (1), Accessories (4)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (10, 1)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (10, 4)
on conflict do nothing;

-- Product 11: Gucci Women's Loafers -> Women (2), Women's Shoes (10)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (11, 2)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (11, 10)
on conflict do nothing;

-- Product 12: Chanel Card Holder -> Women (2), Accessories (4)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (12, 2)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (12, 4)
on conflict do nothing;

-- Product 13: Nike Women's Leggings -> Women (2)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (13, 2)
on conflict do nothing;
-- Assigning also to Women's Tops (9) as a general clothing category if no specific bottoms exist
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (13, 9)
on conflict do nothing;


-- Product 14: Zara Men's Polo Shirt -> Men (1), Men's Tops (5)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (14, 1)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (14, 5)
on conflict do nothing;

-- Product 15: Adidas Kids' Sandals -> Kids (3), Boys (11), Girls (12)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (15, 3)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (15, 11)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (15, 12)
on conflict do nothing;

-- Product 16: H&M Women's Wrap Dress -> Women (2), Women's Dresses (8)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (16, 2)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (16, 8)
on conflict do nothing;

-- Product 17: Gucci Men's Wallet -> Men (1), Accessories (4)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (17, 1)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (17, 4)
on conflict do nothing;

-- Product 18: Chanel Brooch -> Women (2), Accessories (4)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (18, 2)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (18, 4)
on conflict do nothing;

-- Product 19: Nike Men's Basketball Shorts -> Men (1), Men's Bottoms (6)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (19, 1)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (19, 6)
on conflict do nothing;

-- Product 20: Zara Women's Sandals -> Women (2), Women's Shoes (10)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (20, 2)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (20, 10)
on conflict do nothing;

-- Product 21: Adidas Men's Hoodie -> Men (1), Men's Tops (5)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (21, 1)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (21, 5)
on conflict do nothing;

-- Product 22: H&M Kids' T-Shirt Pack -> Kids (3), Boys (11), Girls (12)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (22, 3)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (22, 11)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (22, 12)
on conflict do nothing;

-- Product 23: Gucci Men's Sneakers -> Men (1), Men's Shoes (7)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (23, 1)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (23, 7)
on conflict do nothing;

-- Product 24: Chanel Ballet Flats -> Women (2), Women's Shoes (10)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (24, 2)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (24, 10)
on conflict do nothing;

-- Product 25: Nike Women's Tank Top -> Women (2), Women's Tops (9)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (25, 2)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (25, 9)
on conflict do nothing;

-- Product 26: Zara Men's Chino Trousers -> Men (1), Men's Bottoms (6)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (26, 1)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (26, 6)
on conflict do nothing;

-- Product 27: Adidas Kids' Tracksuit -> Kids (3), Boys (11), Girls (12)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (27, 3)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (27, 11)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (27, 12)
on conflict do nothing;

-- Product 28: H&M Scarf -> Accessories (4), Men (1), Women (2)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (28, 4)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (28, 1)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (28, 2)
on conflict do nothing;

-- Product 29: Gucci Belt Bag -> Accessories (4), Men (1), Women (2)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (29, 4)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (29, 1)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (29, 2)
on conflict do nothing;

-- Product 30: Chanel Sneakers -> Women (2), Women's Shoes (10)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (30, 2)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (30, 10)
on conflict do nothing;

-- Product 31: Nike Men's Windrunner Jacket -> Men (1), Men's Tops (5)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (31, 1)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (31, 5)
on conflict do nothing;

-- Product 32: Zara Women's Skinny Jeans -> Women (2)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (32, 2)
on conflict do nothing;
-- Assigning also to Women's Tops (9) as a general clothing category if no specific bottoms exist
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (32, 9)
on conflict do nothing;

-- Product 33: Adidas Cap -> Accessories (4), Men (1), Women (2)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (33, 4)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (33, 1)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (33, 2)
on conflict do nothing;

-- Product 34: H&M Kids' Rain Jacket -> Kids (3), Boys (11), Girls (12)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (34, 3)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (34, 11)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (34, 12)
on conflict do nothing;

-- Product 35: Gucci Scarf -> Accessories (4), Men (1), Women (2)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (35, 4)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (35, 1)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (35, 2)
on conflict do nothing;

-- Product 36: Chanel Wallet on Chain (WOC) -> Women (2), Accessories (4)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (36, 2)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (36, 4)
on conflict do nothing;

-- Product 37: Nike Women's Running Shorts -> Women (2)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (37, 2)
on conflict do nothing;
-- Assigning also to Women's Tops (9) as a general clothing category if no specific bottoms exist
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (37, 9)
on conflict do nothing;

-- Product 38: Zara Kids' Sweater -> Kids (3), Boys (11), Girls (12)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (38, 3)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (38, 11)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (38, 12)
on conflict do nothing;

-- Product 39: Adidas Socks (3-Pack) -> Accessories (4), Men (1), Women (2), Kids (3)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (39, 4)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (39, 1)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (39, 2)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (39, 3)
on conflict do nothing;

-- Product 40: H&M Men's Swim Shorts -> Men (1), Men's Bottoms (6)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (40, 1)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (40, 6)
on conflict do nothing;

-- Product 41: Gucci Horsebit Loafers -> Men (1), Men's Shoes (7)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (41, 1)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (41, 7)
on conflict do nothing;

-- Product 42: Chanel Earrings -> Women (2), Accessories (4)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (42, 2)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (42, 4)
on conflict do nothing;

-- Product 43: Nike Duffel Bag -> Accessories (4), Men (1), Women (2)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (43, 4)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (43, 1)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (43, 2)
on conflict do nothing;

-- Product 44: Zara Women's Poplin Shirt -> Women (2), Women's Tops (9)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (44, 2)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (44, 9)
on conflict do nothing;

-- Product 45: Adidas Slides -> Men (1), Men's Shoes (7), Women (2), Women's Shoes (10)
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (45, 1)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (45, 7)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (45, 2)
on conflict do nothing;
INSERT INTO catalog.product_category (product_id, category_id)
VALUES (45, 10)
on conflict do nothing;