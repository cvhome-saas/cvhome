/*
can you generate another sql inserts similar to this
where languages=['ar','en'] and store_id='65f023632bc46470c104b76f'
start category id from 1
start category_description from 1
generate 12 categories
domain for this store is fashion
*/

-- Category 1: Men (Top Level)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (1, NOW(), NOW(), 'men-category.jpg', true, 'MEN', 0,
        true, '/1/', 0, true, '65f023632bc46470c104b76f', null)
on conflict do nothing;

-- <<Begin Loop on $parameter.languages l
-- English Description (ID: 1)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (1, NOW(), NOW(), 'Shop the latest fashion trends for men.', 'Men', 'Men''s Fashion',
        'New Arrivals for Men', 'Discover stylish clothing, shoes, and accessories for men.',
        'men fashion, menswear, clothing', 'Men''s Fashion | Shop Online', 'men', 'en', 1)
on conflict do nothing;
-- Arabic Description (ID: 2)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (2, NOW(), NOW(), 'تسوق أحدث صيحات الموضة للرجال.', 'رجال', 'أزياء رجالية',
        'وصل حديثاً للرجال', 'اكتشف ملابس وأحذية وإكسسوارات أنيقة للرجال.', 'أزياء رجالية, ملابس رجالية',
        'أزياء رجالية | تسوق أونلاين', 'men-ar', 'ar', 1)
on conflict do nothing;
-- End Loop>>

-- Category 2: Women (Top Level)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (2, NOW(), NOW(), 'women-category.jpg', true, 'WOMEN', 0,
        true, '/2/', 1, true, '65f023632bc46470c104b76f', null)
on conflict do nothing;

-- <<Begin Loop on $parameter.languages l
-- English Description (ID: 3)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (3, NOW(), NOW(), 'Explore the newest collection for women.', 'Women', 'Women''s Fashion',
        'Latest Styles for Women', 'Find chic dresses, tops, shoes, and accessories for women.',
        'women fashion, womenswear, clothing', 'Women''s Fashion | Shop Online', 'women', 'en', 2)
on conflict do nothing;
-- Arabic Description (ID: 4)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (4, NOW(), NOW(), 'اكتشفي أحدث مجموعة للنساء.', 'نساء', 'أزياء نسائية',
        'أحدث الموديلات للنساء', 'اعثري على فساتين وبلوزات وأحذية وإكسسوارات أنيقة للنساء.',
        'أزياء نسائية, ملابس نسائية', 'أزياء نسائية | تسوقي أونلاين', 'women-ar', 'ar', 2)
on conflict do nothing;
-- End Loop>>

-- Category 3: Kids (Top Level)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (3, NOW(), NOW(), 'kids-category.jpg', true, 'KIDS', 0,
        false, '/3/', 2, true, '65f023632bc46470c104b76f', null)
on conflict do nothing;

-- <<Begin Loop on $parameter.languages l
-- English Description (ID: 5)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (5, NOW(), NOW(), 'Fun and stylish clothing for kids.', 'Kids', 'Kids'' Fashion',
        'Shop for Boys & Girls', 'Browse comfortable and trendy outfits for children.',
        'kids fashion, childrenswear, clothing', 'Kids'' Fashion | Shop Online', 'kids', 'en', 3)
on conflict do nothing;
-- Arabic Description (ID: 6)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (6, NOW(), NOW(), 'ملابس ممتعة وأنيقة للأطفال.', 'أطفال', 'أزياء أطفال',
        'تسوق للأولاد والبنات', 'تصفح ملابس مريحة وعصرية للأطفال.', 'أزياء أطفال, ملابس أطفال',
        'أزياء أطفال | تسوق أونلاين', 'kids-ar', 'ar', 3)
on conflict do nothing;
-- End Loop>>

-- Category 4: Accessories (Top Level)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (4, NOW(), NOW(), 'accessories-category.jpg', true, 'ACCESSORIES', 0,
        false, '/4/', 3, true, '65f023632bc46470c104b76f', null)
on conflict do nothing;

-- <<Begin Loop on $parameter.languages l
-- English Description (ID: 7)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (7, NOW(), NOW(), 'Complete your look with our range of accessories.', 'Accessories', 'Fashion Accessories',
        'Bags, Scarves, Jewelry & More', 'Find the perfect accessories including bags, scarves, belts, and jewelry.',
        'accessories, fashion accessories, bags, jewelry', 'Fashion Accessories | Shop Online', 'accessories', 'en', 4)
on conflict do nothing;
-- Arabic Description (ID: 8)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (8, NOW(), NOW(), 'أكمل إطلالتك مع مجموعتنا من الإكسسوارات.', 'إكسسوارات', 'إكسسوارات الموضة',
        'حقائب، أوشحة، مجوهرات والمزيد',
        'اعثر على الإكسسوارات المثالية بما في ذلك الحقائب والأوشحة والأحزمة والمجوهرات.',
        'إكسسوارات, إكسسوارات الموضة, حقائب, مجوهرات', 'إكسسوارات الموضة | تسوق أونلاين', 'accessories-ar', 'ar', 4)
on conflict do nothing;
-- End Loop>>

-- Category 5: Men's Tops (Child of Men)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (5, NOW(), NOW(), 'men-tops.jpg', true, 'MEN_TOPS', 1,
        false, '/1/5/', 0, true, '65f023632bc46470c104b76f', 1)
on conflict do nothing;

-- <<Begin Loop on $parameter.languages l
-- English Description (ID: 9)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (9, NOW(), NOW(), 'T-shirts, shirts, polos, and more for men.', 'Tops', 'Men''s Tops',
        'Casual & Formal Tops', 'Shop a variety of men''s tops for every occasion.',
        'men tops, t-shirts, shirts, polos', 'Men''s Tops | Shop Online', 'men-tops', 'en', 5) -- Changed
on conflict do nothing;
-- Arabic Description (ID: 10)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (10, NOW(), NOW(), 'تي شيرتات، قمصان، بولو، والمزيد للرجال.', 'بلوزات', 'بلوزات رجالية',
        'بلوزات كاجوال ورسمية', 'تسوق مجموعة متنوعة من البلوزات الرجالية لكل مناسبة.',
        'بلوزات رجالية, تي شيرتات, قمصان, بولو', 'بلوزات رجالية | تسوق أونلاين', 'men-tops-ar', 'ar', 5) -- Changed
on conflict do nothing;
-- End Loop>>

-- Category 6: Men's Bottoms (Child of Men)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (6, NOW(), NOW(), 'men-bottoms.jpg', true, 'MEN_BOTTOMS', 1,
        false, '/1/6/', 1, true, '65f023632bc46470c104b76f', 1)
on conflict do nothing;

-- <<Begin Loop on $parameter.languages l
-- English Description (ID: 11)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (11, NOW(), NOW(), 'Jeans, trousers, shorts for men.', 'Bottoms', 'Men''s Bottoms',
        'Trousers, Jeans & Shorts', 'Explore our collection of men''s bottoms.', 'men bottoms, jeans, trousers, shorts',
        'Men''s Bottoms | Shop Online', 'men-bottoms', 'en', 6) -- Changed
on conflict do nothing;
-- Arabic Description (ID: 12)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (12, NOW(), NOW(), 'جينز، بناطيل، شورتات للرجال.', 'بناطيل', 'بناطيل رجالية',
        'بناطيل، جينز وشورتات', 'اكتشف مجموعتنا من البناطيل الرجالية.', 'بناطيل رجالية, جينز, بناطيل قماش, شورتات',
        'بناطيل رجالية | تسوق أونلاين', 'men-bottoms-ar', 'ar', 6) -- Changed
on conflict do nothing;
-- End Loop>>

-- Category 7: Men's Shoes (Child of Men)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (7, NOW(), NOW(), 'men-shoes.jpg', true, 'MEN_SHOES', 1,
        false, '/1/7/', 2, true, '65f023632bc46470c104b76f', 1)
on conflict do nothing;

-- <<Begin Loop on $parameter.languages l
-- English Description (ID: 13)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (13, NOW(), NOW(), 'Sneakers, boots, formal shoes for men.', 'Shoes', 'Men''s Shoes',
        'Footwear for Every Style', 'Find the perfect pair of men''s shoes.',
        'men shoes, sneakers, boots, formal shoes', 'Men''s Shoes | Shop Online', 'men-shoes', 'en', 7) -- Changed
on conflict do nothing;
-- Arabic Description (ID: 14)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (14, NOW(), NOW(), 'أحذية رياضية، بوتات، أحذية رسمية للرجال.', 'أحذية', 'أحذية رجالية',
        'أحذية لكل ستايل', 'اعثر على زوج الأحذية الرجالية المثالي.', 'أحذية رجالية, سنيكرز, بوتات, أحذية رسمية',
        'أحذية رجالية | تسوق أونلاين', 'men-shoes-ar', 'ar', 7) -- Changed
on conflict do nothing;
-- End Loop>>

-- Category 8: Women's Dresses (Child of Women)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (8, NOW(), NOW(), 'women-dresses.jpg', true, 'WOMEN_DRESSES', 1,
        true, '/2/8/', 0, true, '65f023632bc46470c104b76f', 2)
on conflict do nothing;

-- <<Begin Loop on $parameter.languages l
-- English Description (ID: 15)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (15, NOW(), NOW(), 'Casual, evening, and party dresses for women.', 'Dresses', 'Women''s Dresses',
        'Dresses for All Occasions', 'Discover a wide range of stylish women''s dresses.',
        'women dresses, casual dresses, evening dresses', 'Women''s Dresses | Shop Online', 'women-dresses', 'en',
        8) -- Changed
on conflict do nothing;
-- Arabic Description (ID: 16)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (16, NOW(), NOW(), 'فساتين كاجوال، سهرة، وحفلات للنساء.', 'فساتين', 'فساتين نسائية',
        'فساتين لجميع المناسبات', 'اكتشفي مجموعة واسعة من الفساتين النسائية الأنيقة.',
        'فساتين نسائية, فساتين كاجوال, فساتين سهرة', 'فساتين نسائية | تسوقي أونلاين', 'women-dresses-ar', 'ar',
        8) -- Changed
on conflict do nothing;
-- End Loop>>

-- Category 9: Women's Tops (Child of Women)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (9, NOW(), NOW(), 'women-tops.jpg', true, 'WOMEN_TOPS', 1,
        false, '/2/9/', 1, true, '65f023632bc46470c104b76f', 2)
on conflict do nothing;

-- <<Begin Loop on $parameter.languages l
-- English Description (ID: 17)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (17, NOW(), NOW(), 'Blouses, t-shirts, sweaters for women.', 'Tops', 'Women''s Tops',
        'Stylish Tops & Blouses', 'Shop trendy tops for women.', 'women tops, blouses, t-shirts, sweaters',
        'Women''s Tops | Shop Online', 'women-tops', 'en', 9) -- Changed
on conflict do nothing;
-- Arabic Description (ID: 18)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (18, NOW(), NOW(), 'بلوزات، تي شيرتات، سترات للنساء.', 'بلوزات', 'بلوزات نسائية',
        'بلوزات وقمصان أنيقة', 'تسوقي بلوزات عصرية للنساء.', 'بلوزات نسائية, قمصان, تي شيرتات, سترات',
        'بلوزات نسائية | تسوقي أونلاين', 'women-tops-ar', 'ar', 9) -- Changed
on conflict do nothing;
-- End Loop>>

-- Category 10: Women's Shoes (Child of Women)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (10, NOW(), NOW(), 'women-shoes.jpg', true, 'WOMEN_SHOES', 1,
        false, '/2/10/', 2, true, '65f023632bc46470c104b76f', 2)
on conflict do nothing;

-- <<Begin Loop on $parameter.languages l
-- English Description (ID: 19)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (19, NOW(), NOW(), 'Heels, flats, sandals, boots for women.', 'Shoes', 'Women''s Shoes',
        'Elegant & Casual Footwear', 'Find your perfect pair of women''s shoes.',
        'women shoes, heels, flats, sandals, boots', 'Women''s Shoes | Shop Online', 'women-shoes', 'en', 10) -- Changed
on conflict do nothing;
-- Arabic Description (ID: 20)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (20, NOW(), NOW(), 'كعوب، فلات، صنادل، بوتات للنساء.', 'أحذية', 'أحذية نسائية',
        'أحذية أنيقة وكاجوال', 'اعثري على زوج الأحذية النسائية المثالي.', 'أحذية نسائية, كعب عالي, فلات, صنادل, بوتات',
        'أحذية نسائية | تسوقي أونلاين', 'women-shoes-ar', 'ar', 10) -- Changed
on conflict do nothing;
-- End Loop>>

-- Category 11: Boys (Child of Kids)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (11, NOW(), NOW(), 'boys-clothing.jpg', true, 'KIDS_BOYS', 1,
        false, '/3/11/', 0, true, '65f023632bc46470c104b76f', 3)
on conflict do nothing;

-- <<Begin Loop on $parameter.languages l
-- English Description (ID: 21)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (21, NOW(), NOW(), 'Clothing and shoes for boys.', 'Boys', 'Boys'' Fashion',
        'Cool Outfits for Boys', 'Shop the latest trends for boys.', 'boys clothing, boys fashion, kids boys',
        'Boys'' Fashion | Shop Online', 'kids-boys', 'en', 11) -- Changed
on conflict do nothing;
-- Arabic Description (ID: 22)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (22, NOW(), NOW(), 'ملابس وأحذية للأولاد.', 'أولاد', 'أزياء أولاد',
        'ملابس رائعة للأولاد', 'تسوق أحدث الصيحات للأولاد.', 'ملابس أولاد, أزياء أولاد, أطفال أولاد',
        'أزياء أولاد | تسوق أونلاين', 'kids-boys-ar', 'ar', 11) -- Changed
on conflict do nothing;
-- End Loop>>

-- Category 12: Girls (Child of Kids)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (12, NOW(), NOW(), 'girls-clothing.jpg', true, 'KIDS_GIRLS', 1,
        false, '/3/12/', 1, true, '65f023632bc46470c104b76f', 3)
on conflict do nothing;

-- <<Begin Loop on $parameter.languages l
-- English Description (ID: 23)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (23, NOW(), NOW(), 'Dresses, tops, and accessories for girls.', 'Girls', 'Girls'' Fashion',
        'Cute Styles for Girls', 'Explore adorable outfits for girls.', 'girls clothing, girls fashion, kids girls',
        'Girls'' Fashion | Shop Online', 'kids-girls', 'en', 12) -- Changed
on conflict do nothing;
-- Arabic Description (ID: 24)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (24, NOW(), NOW(), 'فساتين، بلوزات، وإكسسوارات للبنات.', 'بنات', 'أزياء بنات',
        'موديلات جميلة للبنات', 'اكتشفي ملابس رائعة للبنات.', 'ملابس بنات, أزياء بنات, أطفال بنات',
        'أزياء بنات | تسوقي أونلاين', 'kids-girls-ar', 'ar', 12) -- Changed
on conflict do nothing;
-- End Loop>>