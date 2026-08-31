/*
Generated SQL inserts based on the provided template and instructions:
- 15 products for catalog.product.
- 2 descriptions (en, ar) for each product in catalog.product_description.
- store_id = '65f023632bc46470c104b76f'.
- manufacturer_id from (1, 2, 3, 4, 5, 6).
- product_type_id from (1, 2, 3, 4).
- product_id sequential from 1 to 15.
- description_id sequential from 1 to 30.
- Realistic SKUs, Ref SKUs generated.
- Realistic fashion-related names, titles, meta descriptions, etc. generated.
- Valid HTML generated for the description field.
- Timestamps replaced with NOW().
- Domain is fashion.
*/

-- Product 1: Nike Running Shoes (Manuf: 1, Type: 2)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (1, NOW(), NOW(), true, NOW(), false, 12.0,
        false, 30.0, true, false, 0.8, 10.0, 'REF-NK-RUN-001', 1, 1,
        '65f023632bc46470c104b76f', 2)
on conflict (product_id) do nothing;

-- English Description (ID: 1)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (1, NOW(), NOW(),
        '<div>
           <h2>Nike ZoomX Invincible Run Flyknit 3</h2>
           <p>Push your limits with maximum cushioning. The Nike ZoomX Invincible Run Flyknit 3 offers our highest level of comfort underfoot to help you stay on your feet today, tomorrow and beyond.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>ZoomX Foam:</strong> Provides incredible responsiveness and energy return.</li>
             <li><strong>Flyknit Upper:</strong> Offers breathability and a secure, sock-like fit.</li>
             <li><strong>Wider Forefoot:</strong> Enhances stability.</li>
             <li><strong>Durable Outsole:</strong> Designed for long-lasting wear.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Use:</strong> Road Running</li>
             <li><strong>Cushioning:</strong> Maximum</li>
             <li><strong>Color:</strong> White/Black/Volt</li>
           </ul>
         </div>',
        'Nike ZoomX Invincible Run 3', 'Nike ZoomX Invincible Run Flyknit 3 Men''s Shoes',
        'Experience maximum cushioning with the Nike ZoomX Invincible Run Flyknit 3 running shoes.',
        'Nike, ZoomX, Invincible Run, Flyknit, running shoes, men shoes, cushioning',
        'Nike ZoomX Invincible Run Flyknit 3 | Max Cushioning', 'Highest level of ZoomX cushioning',
        'nike-zoomx-invincible-run-3', 'en', 1)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 2)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (2, NOW(), NOW(),
        '<div dir="rtl">
           <h2>نايكي زوم اكس انفيسيبل رن فلاي نت 3</h2>
           <p>تجاوز حدودك مع أقصى درجات التوسيد. يوفر حذاء نايكي زوم اكس انفيسيبل رن فلاي نت 3 أعلى مستوى من الراحة تحت القدم لمساعدتك على البقاء على قدميك اليوم وغدًا وما بعده.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>فوم ZoomX:</strong> يوفر استجابة وعودة طاقة لا تصدق.</li>
             <li><strong>جزء علوي Flyknit:</strong> يوفر التهوية ومقاسًا آمنًا يشبه الجورب.</li>
             <li><strong>مقدمة قدم أوسع:</strong> تعزز الاستقرار.</li>
             <li><strong>نعل سفلي متين:</strong> مصمم لارتداء طويل الأمد.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الاستخدام:</strong> الجري على الطرق</li>
             <li><strong>التوسيد:</strong> أقصى درجة</li>
             <li><strong>اللون:</strong> أبيض/أسود/فولت</li>
           </ul>
         </div>',
        'نايكي زوم اكس انفيسيبل رن 3', 'حذاء الجري نايكي زوم اكس انفيسيبل رن فلاي نت 3 للرجال',
        'جرب أقصى درجات التوسيد مع حذاء الجري نايكي زوم اكس انفيسيبل رن فلاي نت 3.',
        'نايكي, زوم اكس, انفيسيبل رن, فلاي نت, حذاء جري, أحذية رجالية, توسيد',
        'نايكي زوم اكس انفيسيبل رن فلاي نت 3 | أقصى توسيد', 'أعلى مستوى من توسيد ZoomX',
        'nike-zoomx-invincible-run-3-ar', 'ar', 1)
on conflict (description_id) do nothing;

-- Product 2: Zara Women's Midi Dress (Manuf: 2, Type: 1)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (2, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.5, null, 'REF-ZR-CL-DRS02', 2, 2,
        '65f023632bc46470c104b76f', 1)
on conflict (product_id) do nothing;

-- English Description (ID: 3)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (3, NOW(), NOW(),
        '<div>
           <h2>Zara Satin Effect Midi Dress</h2>
           <p>Elegant midi dress from Zara with a beautiful satin effect finish. Features a flattering V-neckline and adjustable thin straps.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Satin Effect Fabric:</strong> Luxurious sheen and smooth feel.</li>
             <li><strong>Midi Length:</strong> Versatile and chic.</li>
             <li><strong>V-Neckline:</strong> Enhances the décolletage.</li>
             <li><strong>Adjustable Straps:</strong> For a perfect fit.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> 100% Polyester</li>
             <li><strong>Color:</strong> Bottle Green</li>
             <li><strong>Fit:</strong> Flowy</li>
             <li><strong>Care:</strong> Machine wash cold</li>
           </ul>
         </div>',
        'Zara Satin Effect Midi Dress', 'Zara Women''s Satin Effect Midi Dress',
        'Shop the elegant Zara satin effect midi dress. Perfect for evenings and special occasions.',
        'Zara, dress, midi dress, satin dress, women clothing, evening wear',
        'Zara Women''s Satin Effect Midi Dress | Elegant Style', 'Luxurious satin effect fabric',
        'zara-satin-effect-midi-dress', 'en', 2)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 4)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (4, NOW(), NOW(),
        '<div dir="rtl">
           <h2>فستان زارا ميدي بتأثير الساتان</h2>
           <p>فستان ميدي أنيق من زارا بلمسة نهائية جميلة بتأثير الساتان. يتميز بفتحة رقبة على شكل V جذابة وأحزمة رفيعة قابلة للتعديل.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>نسيج بتأثير الساتان:</strong> لمعان فاخر وملمس ناعم.</li>
             <li><strong>طول ميدي:</strong> متعدد الاستخدامات وأنيق.</li>
             <li><strong>فتحة رقبة على شكل V:</strong> تبرز منطقة الصدر.</li>
             <li><strong>أحزمة قابلة للتعديل:</strong> لمقاس مثالي.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> 100% بوليستر</li>
             <li><strong>اللون:</strong> أخضر زجاجي</li>
             <li><strong>المقاس:</strong> انسيابي</li>
             <li><strong>العناية:</strong> غسيل في الغسالة بماء بارد</li>
           </ul>
         </div>',
        'فستان زارا ميدي بتأثير الساتان', 'فستان نسائي ميدي بتأثير الساتان من زارا',
        'تسوقي فستان زارا الأنيق الميدي بتأثير الساتان. مثالي للسهرات والمناسبات الخاصة.',
        'زارا, فستان, فستان ميدي, فستان ساتان, ملابس نسائية, ملابس سهرة',
        'فستان نسائي ميدي بتأثير الساتان من زارا | أناقة راقية', 'نسيج فاخر بتأثير الساتان',
        'zara-satin-effect-midi-dress-ar', 'ar', 2)
on conflict (description_id) do nothing;

-- Product 3: Adidas Men's Track Pants (Manuf: 3, Type: 1)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (3, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.5, null, 'REF-AD-CL-TPT03', 3, 3,
        '65f023632bc46470c104b76f', 1)
on conflict (product_id) do nothing;

-- English Description (ID: 5)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (5, NOW(), NOW(),
        '<div>
           <h2>Adidas Tiro 23 League Training Pants</h2>
           <p>Stay comfortable on the training pitch or the sidelines. These Adidas Tiro 23 League pants use AEROREADY to keep you dry and feature ankle zips for easy on and off over boots.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>AEROREADY Technology:</strong> Absorbs moisture to keep you dry.</li>
             <li><strong>Slim Fit:</strong> Streamlined feel.</li>
             <li><strong>Ankle Zips:</strong> Convenient for changing.</li>
             <li><strong>Zip Pockets:</strong> Secure storage for essentials.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> 100% Recycled Polyester Doubleknit</li>
             <li><strong>Color:</strong> Black/White</li>
             <li><strong>Fit:</strong> Slim</li>
           </ul>
         </div>',
        'Adidas Tiro 23 Training Pants', 'Adidas Men''s Tiro 23 League Training Pants',
        'Shop the Adidas Tiro 23 training pants for men. Features AEROREADY technology and ankle zips.',
        'Adidas, Tiro 23, training pants, track pants, men clothing, sportswear, AEROREADY',
        'Adidas Men''s Tiro 23 Training Pants | Sportswear', 'AEROREADY moisture-absorbing',
        'adidas-tiro-23-training-pants', 'en', 3)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 6)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (6, NOW(), NOW(),
        '<div dir="rtl">
           <h2>بنطال تدريب أديداس تيرو 23 ليج</h2>
           <p>حافظ على راحتك في ملعب التدريب أو على الخطوط الجانبية. يستخدم بنطال أديداس تيرو 23 ليج تقنية AEROREADY للحفاظ على جفافك ويتميز بسحابات الكاحل لسهولة الارتداء والخلع فوق الأحذية.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>تقنية AEROREADY:</strong> تمتص الرطوبة للحفاظ على جفافك.</li>
             <li><strong>قصة ضيقة:</strong> شعور انسيابي.</li>
             <li><strong>سحابات الكاحل:</strong> مريحة للتغيير.</li>
             <li><strong>جيوب بسحاب:</strong> تخزين آمن للضروريات.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> 100% بوليستر معاد تدويره مزدوج الحياكة</li>
             <li><strong>اللون:</strong> أسود/أبيض</li>
             <li><strong>المقاس:</strong> ضيق</li>
           </ul>
         </div>',
        'بنطال تدريب أديداس تيرو 23', 'بنطال تدريب رجالي أديداس تيرو 23 ليج',
        'تسوق بنطال تدريب أديداس تيرو 23 للرجال. يتميز بتقنية AEROREADY وسحابات الكاحل.',
        'أديداس, تيرو 23, بنطال تدريب, بنطال رياضي, ملابس رجالية, ملابس رياضية, AEROREADY',
        'بنطال تدريب رجالي أديداس تيرو 23 | ملابس رياضية', 'AEROREADY ممتص للرطوبة', 'adidas-tiro-23-training-pants-ar',
        'ar', 3)
on conflict (description_id) do nothing;

-- Product 4: H&M Women's Knit Sweater (Manuf: 4, Type: 1)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (4, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.6, null, 'REF-HM-CL-SWT04', 4, 4,
        '65f023632bc46470c104b76f', 1)
on conflict (product_id) do nothing;

-- English Description (ID: 7)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (7, NOW(), NOW(),
        '<div>
           <h2>H&M Rib-knit Sweater</h2>
           <p>Cozy up in this soft, rib-knit sweater from H&M. Features a relaxed fit, dropped shoulders, and wide sleeves for ultimate comfort.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Soft Rib-knit Fabric:</strong> Comfortable and warm.</li>
             <li><strong>Relaxed Fit:</strong> Easy and comfortable to wear.</li>
             <li><strong>Dropped Shoulders:</strong> Creates a casual silhouette.</li>
             <li><strong>Wide Sleeves:</strong> Adds a modern touch.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> Polyester/Acrylic Blend</li>
             <li><strong>Color:</strong> Cream</li>
             <li><strong>Neckline:</strong> Round Neck</li>
           </ul>
         </div>',
        'H&M Rib-knit Sweater', 'H&M Women''s Relaxed Fit Rib-knit Sweater',
        'Shop the cozy H&M rib-knit sweater for women. Relaxed fit for ultimate comfort.',
        'H&M, sweater, knitwear, women clothing, relaxed fit, rib-knit',
        'H&M Women''s Rib-knit Sweater | Cozy Knitwear', 'Soft rib-knit fabric', 'hm-rib-knit-sweater-women', 'en', 4)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 8)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (8, NOW(), NOW(),
        '<div dir="rtl">
           <h2>سترة إتش آند إم محبوكة مضلعة</h2>
           <p>استمتعي بالدفء مع هذه السترة الناعمة المحبوكة المضلعة من إتش آند إم. تتميز بقصة مريحة وأكتاف منسدلة وأكمام واسعة لراحة قصوى.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>نسيج محبوك مضلع ناعم:</strong> مريح ودافئ.</li>
             <li><strong>قصة مريحة:</strong> سهلة ومريحة للارتداء.</li>
             <li><strong>أكتاف منسدلة:</strong> تخلق صورة ظلية كاجوال.</li>
             <li><strong>أكمام واسعة:</strong> تضيف لمسة عصرية.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> مزيج بوليستر/أكريليك</li>
             <li><strong>اللون:</strong> كريمي</li>
             <li><strong>خط العنق:</strong> رقبة دائرية</li>
           </ul>
         </div>',
        'سترة إتش آند إم محبوكة مضلعة', 'سترة نسائية بقصة مريحة محبوكة مضلعة من إتش آند إم',
        'تسوقي سترة إتش آند إم المريحة المحبوكة المضلعة للنساء. قصة مريحة لراحة قصوى.',
        'إتش آند إم, سترة, ملابس محبوكة, ملابس نسائية, قصة مريحة, محبوك مضلع',
        'سترة نسائية محبوكة مضلعة من إتش آند إم | ملابس محبوكة مريحة', 'نسيج محبوك مضلع ناعم',
        'hm-rib-knit-sweater-women-ar', 'ar', 4)
on conflict (description_id) do nothing;

-- Product 5: Gucci Marmont Shoulder Bag (Manuf: 5, Type: 4)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (5, NOW(), NOW(), true, NOW(), false, 15.0,
        false, 26.0, true, false, 0.7, 7.0, 'REF-GU-BG-MAR05', 5, 5,
        '65f023632bc46470c104b76f', 4)
on conflict (product_id) do nothing;

-- English Description (ID: 9)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (9, NOW(), NOW(),
        '<div>
           <h2>Gucci GG Marmont Small Matelassé Shoulder Bag</h2>
           <p>The iconic GG Marmont shoulder bag by Gucci, featuring matelassé chevron leather with a heart detail on the back and the signature Double G hardware.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Matelassé Chevron Leather:</strong> Softly structured shape with iconic quilting.</li>
             <li><strong>Double G Hardware:</strong> Antique gold-toned hardware.</li>
             <li><strong>Sliding Chain Strap:</strong> Can be worn as a shoulder bag or top handle bag.</li>
             <li><strong>Interior Zipper Pocket:</strong> Secure storage.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> Leather</li>
             <li><strong>Color:</strong> Black</li>
             <li><strong>Lining:</strong> Microfiber lining with a suede-like finish</li>
             <li><strong>Made in Italy</strong></li>
           </ul>
         </div>',
        'Gucci GG Marmont Shoulder Bag', 'Gucci GG Marmont Small Matelassé Shoulder Bag',
        'Shop the iconic Gucci GG Marmont shoulder bag in black matelassé leather.',
        'Gucci, GG Marmont, shoulder bag, handbag, women bag, leather, luxury, matelasse',
        'Gucci GG Marmont Shoulder Bag | Luxury Handbags', 'Matelassé chevron leather', 'gucci-gg-marmont-shoulder-bag',
        'en', 5)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 10)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (10, NOW(), NOW(),
        '<div dir="rtl">
           <h2>حقيبة كتف غوتشي جي جي مارمونت صغيرة مبطنة ماتيلاسيه</h2>
           <p>حقيبة كتف جي جي مارمونت الأيقونية من غوتشي، تتميز بجلد شيفرون مبطن ماتيلاسيه مع تفاصيل قلب في الخلف وقطع معدنية Double G المميزة.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>جلد شيفرون مبطن ماتيلاسيه:</strong> شكل منظم بنعومة مع تبطين أيقوني.</li>
             <li><strong>قطع معدنية Double G:</strong> قطع معدنية بلون ذهبي عتيق.</li>
             <li><strong>حزام سلسلة منزلق:</strong> يمكن ارتداؤه كحقيبة كتف أو حقيبة بمقبض علوي.</li>
             <li><strong>جيب داخلي بسحاب:</strong> تخزين آمن.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> جلد</li>
             <li><strong>اللون:</strong> أسود</li>
             <li><strong>البطانة:</strong> بطانة من الألياف الدقيقة بلمسة نهائية تشبه الجلد السويدي</li>
             <li><strong>صنع في إيطاليا</strong></li>
           </ul>
         </div>',
        'حقيبة كتف غوتشي جي جي مارمونت', 'حقيبة كتف غوتشي جي جي مارمونت صغيرة مبطنة ماتيلاسيه',
        'تسوقي حقيبة كتف غوتشي جي جي مارمونت الأيقونية من الجلد المبطن الأسود.',
        'غوتشي, جي جي مارمونت, حقيبة كتف, حقيبة يد, حقيبة نسائية, جلد, فخامة, ماتيلاسيه',
        'حقيبة كتف غوتشي جي جي مارمونت | حقائب يد فاخرة', 'جلد شيفرون مبطن ماتيلاسيه',
        'gucci-gg-marmont-shoulder-bag-ar', 'ar', 5)
on conflict (description_id) do nothing;

-- Product 6: Chanel Sunglasses (Manuf: 6, Type: 3)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (6, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.1, null, 'REF-CH-AC-SUN06', 6, 6,
        '65f023632bc46470c104b76f', 3)
on conflict (product_id) do nothing;

-- English Description (ID: 11)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (11, NOW(), NOW(),
        '<div>
           <h2>Chanel Butterfly Sunglasses</h2>
           <p>Elegant butterfly-shaped sunglasses from Chanel. Featuring acetate frames and gradient lenses offering 100% UVA and UVB protection.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Butterfly Shape:</strong> Flattering and feminine frame design.</li>
             <li><strong>Acetate Frame:</strong> Lightweight and durable material.</li>
             <li><strong>Gradient Lenses:</strong> Stylish look with UV protection.</li>
             <li><strong>Chanel Logo Detail:</strong> Signature branding on the temples.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Frame Material:</strong> Acetate</li>
             <li><strong>Lens Color:</strong> Brown Gradient</li>
             <li><strong>Frame Color:</strong> Tortoise</li>
             <li><strong>Protection:</strong> 100% UVA/UVB</li>
           </ul>
         </div>',
        'Chanel Butterfly Sunglasses', 'Chanel Women''s Butterfly Acetate Sunglasses',
        'Shop elegant Chanel butterfly sunglasses with gradient lenses and UV protection.',
        'Chanel, sunglasses, butterfly sunglasses, women accessories, acetate, luxury',
        'Chanel Butterfly Sunglasses | Luxury Eyewear', 'Elegant butterfly shape', 'chanel-butterfly-sunglasses', 'en',
        6)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 12)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (12, NOW(), NOW(),
        '<div dir="rtl">
           <h2>نظارات شمسية شانيل شكل فراشة</h2>
           <p>نظارات شمسية أنيقة على شكل فراشة من شانيل. تتميز بإطارات أسيتات وعدسات متدرجة توفر حماية 100% من الأشعة فوق البنفسجية UVA و UVB.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>شكل فراشة:</strong> تصميم إطار جذاب وأنثوي.</li>
             <li><strong>إطار أسيتات:</strong> مادة خفيفة الوزن ومتينة.</li>
             <li><strong>عدسات متدرجة:</strong> مظهر أنيق مع حماية من الأشعة فوق البنفسجية.</li>
             <li><strong>تفاصيل شعار شانيل:</strong> علامة تجارية مميزة على الذراعين.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>مادة الإطار:</strong> أسيتات</li>
             <li><strong>لون العدسة:</strong> بني متدرج</li>
             <li><strong>لون الإطار:</strong> تورتواز (صدفة السلحفاة)</li>
             <li><strong>الحماية:</strong> 100% UVA/UVB</li>
           </ul>
         </div>',
        'نظارات شمسية شانيل فراشة', 'نظارات شمسية نسائية أسيتات شكل فراشة من شانيل',
        'تسوقي نظارات شانيل الشمسية الأنيقة شكل فراشة بعدسات متدرجة وحماية من الأشعة فوق البنفسجية.',
        'شانيل, نظارات شمسية, نظارات فراشة, إكسسوارات نسائية, أسيتات, فخامة', 'نظارات شمسية شانيل فراشة | نظارات فاخرة',
        'شكل فراشة أنيق', 'chanel-butterfly-sunglasses-ar', 'ar', 6)
on conflict (description_id) do nothing;

-- Product 7: Nike Kids' Hoodie (Manuf: 1, Type: 1)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (7, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.4, null, 'REF-NK-CL-KHD07', 7, 1,
        '65f023632bc46470c104b76f', 1)
on conflict (product_id) do nothing;

-- English Description (ID: 13)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (13, NOW(), NOW(),
        '<div>
           <h2>Nike Sportswear Club Fleece Hoodie (Kids)</h2>
           <p>Keep your little one cozy in the Nike Sportswear Club Fleece Hoodie. Made with soft fleece fabric for all-day comfort and warmth.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Club Fleece Fabric:</strong> Soft, comfortable, and warm.</li>
             <li><strong>Standard Fit:</strong> Relaxed and easy feel.</li>
             <li><strong>Kangaroo Pocket:</strong> Convenient storage and hand warmth.</li>
             <li><strong>Ribbed Cuffs and Hem:</strong> Help keep the hoodie in place.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> Cotton/Polyester Blend</li>
             <li><strong>Color:</strong> Grey Heather/White</li>
             <li><strong>Age Group:</strong> Kids (Boys/Girls)</li>
           </ul>
         </div>',
        'Nike Club Fleece Hoodie (Kids)', 'Nike Kids'' Sportswear Club Fleece Pullover Hoodie',
        'Shop the comfortable Nike Club Fleece hoodie for kids. Soft fabric for everyday warmth.',
        'Nike, hoodie, kids clothing, sportswear, Club Fleece, pullover', 'Nike Kids'' Club Fleece Hoodie | Sportswear',
        'Soft Club Fleece fabric', 'nike-club-fleece-hoodie-kids', 'en', 7)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 14)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (14, NOW(), NOW(),
        '<div dir="rtl">
           <h2>هودي نايكي سبورتسوير كلوب فليس (للأطفال)</h2>
           <p>حافظ على دفء طفلك الصغير مع هودي نايكي سبورتسوير كلوب فليس. مصنوع من نسيج فليس ناعم لراحة ودفء طوال اليوم.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>نسيج كلوب فليس:</strong> ناعم ومريح ودافئ.</li>
             <li><strong>قصة قياسية:</strong> شعور مريح وسهل.</li>
             <li><strong>جيب كنغر:</strong> تخزين مريح وتدفئة لليدين.</li>
             <li><strong>أساور وحافة مضلعة:</strong> تساعد في الحفاظ على الهودي في مكانه.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> مزيج قطن/بوليستر</li>
             <li><strong>اللون:</strong> رمادي هيذر/أبيض</li>
             <li><strong>الفئة العمرية:</strong> أطفال (أولاد/بنات)</li>
           </ul>
         </div>',
        'هودي نايكي كلوب فليس (أطفال)', 'هودي بلوفر نايكي سبورتسوير كلوب فليس للأطفال',
        'تسوق هودي نايكي كلوب فليس المريح للأطفال. نسيج ناعم للدفء اليومي.',
        'نايكي, هودي, ملابس أطفال, ملابس رياضية, كلوب فليس, بلوفر', 'هودي نايكي كلوب فليس للأطفال | ملابس رياضية',
        'نسيج كلوب فليس ناعم', 'nike-club-fleece-hoodie-kids-ar', 'ar', 7)
on conflict (description_id) do nothing;

-- Product 8: Zara Men's Sneakers (Manuf: 2, Type: 2)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (8, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.9, null, 'REF-ZR-SH-SNK08', 8, 2,
        '65f023632bc46470c104b76f', 2)
on conflict (product_id) do nothing;

-- English Description (ID: 15)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (15, NOW(), NOW(),
        '<div>
           <h2>Zara Contrast Sole Sneakers</h2>
           <p>Casual sneakers from Zara featuring a combination of materials on the upper and a chunky contrast sole. Lace-up fastening for a secure fit.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Combination Upper:</strong> Mix of textures and materials.</li>
             <li><strong>Chunky Contrast Sole:</strong> Modern and trendy look.</li>
             <li><strong>Lace-up Fastening:</strong> Secure and adjustable fit.</li>
             <li><strong>Padded Collar:</strong> Added comfort around the ankle.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Upper Material:</strong> Polyurethane/Polyester Blend</li>
             <li><strong>Sole Material:</strong> Rubber</li>
             <li><strong>Color:</strong> White/Grey/Blue</li>
           </ul>
         </div>',
        'Zara Contrast Sole Sneakers', 'Zara Men''s Contrast Sole Sneakers',
        'Shop casual men''s sneakers from Zara with a trendy contrast sole design.',
        'Zara, sneakers, men shoes, casual shoes, contrast sole',
        'Zara Men''s Contrast Sole Sneakers | Casual Footwear', 'Chunky contrast sole',
        'zara-contrast-sole-sneakers-men', 'en', 8)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 16)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (16, NOW(), NOW(),
        '<div dir="rtl">
           <h2>سنيكرز زارا بنعل متباين</h2>
           <p>سنيكرز كاجوال من زارا يتميز بمزيج من المواد في الجزء العلوي ونعل سميك متباين. إغلاق برباط لمقاس آمن.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>جزء علوي مختلط:</strong> مزيج من الأنسجة والمواد.</li>
             <li><strong>نعل سميك متباين:</strong> مظهر عصري ورائج.</li>
             <li><strong>إغلاق برباط:</strong> مقاس آمن وقابل للتعديل.</li>
             <li><strong>ياقة مبطنة:</strong> راحة إضافية حول الكاحل.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>مادة الجزء العلوي:</strong> مزيج بولي يوريثان/بوليستر</li>
             <li><strong>مادة النعل:</strong> مطاط</li>
             <li><strong>اللون:</strong> أبيض/رمادي/أزرق</li>
           </ul>
         </div>',
        'سنيكرز زارا بنعل متباين', 'سنيكرز رجالي بنعل متباين من زارا',
        'تسوق سنيكرز رجالي كاجوال من زارا بتصميم نعل متباين عصري.',
        'زارا, سنيكرز, أحذية رجالية, حذاء كاجوال, نعل متباين', 'سنيكرز رجالي بنعل متباين من زارا | أحذية كاجوال',
        'نعل سميك متباين', 'zara-contrast-sole-sneakers-men-ar', 'ar', 8)
on conflict (description_id) do nothing;

-- Product 9: Adidas Women's Backpack (Manuf: 3, Type: 4)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (9, NOW(), NOW(), true, NOW(), false, 45.0,
        false, 30.0, true, false, 0.5, 15.0, 'REF-AD-BG-BPK09', 9, 3,
        '65f023632bc46470c104b76f', 4)
on conflict (product_id) do nothing;

-- English Description (ID: 17)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (17, NOW(), NOW(),
        '<div>
           <h2>Adidas Classic Badge of Sport Backpack</h2>
           <p>Carry your essentials in style with this classic Adidas backpack. Features a spacious main compartment, front zip pocket, and padded shoulder straps for comfort.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Spacious Main Compartment:</strong> Fits books, laptop, and more.</li>
             <li><strong>Front Zip Pocket:</strong> Quick access to smaller items.</li>
             <li><strong>Padded Shoulder Straps:</strong> Comfortable carrying.</li>
             <li><strong>Durable Base:</strong> Coated for extra protection.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> 100% Recycled Polyester Plain Weave</li>
             <li><strong>Color:</strong> Black/White</li>
             <li><strong>Volume:</strong> Approx. 27.5 L</li>
           </ul>
         </div>',
        'Adidas Classic Backpack', 'Adidas Classic Badge of Sport Backpack',
        'Shop the durable Adidas Classic backpack. Perfect for school, work, or gym.',
        'Adidas, backpack, bag, classic backpack, Badge of Sport', 'Adidas Classic Badge of Sport Backpack',
        'Padded shoulder straps', 'adidas-classic-backpack', 'en', 9)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 18)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (18, NOW(), NOW(),
        '<div dir="rtl">
           <h2>حقيبة ظهر أديداس كلاسيك بشعار Badge of Sport</h2>
           <p>احمل ضرورياتك بأناقة مع حقيبة ظهر أديداس الكلاسيكية هذه. تتميز بمقصورة رئيسية واسعة وجيب أمامي بسحاب وأحزمة كتف مبطنة للراحة.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>مقصورة رئيسية واسعة:</strong> تتسع للكتب والكمبيوتر المحمول والمزيد.</li>
             <li><strong>جيب أمامي بسحاب:</strong> وصول سريع للأشياء الصغيرة.</li>
             <li><strong>أحزمة كتف مبطنة:</strong> حمل مريح.</li>
             <li><strong>قاعدة متينة:</strong> مطلية لحماية إضافية.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> 100% بوليستر معاد تدويره منسوج</li>
             <li><strong>اللون:</strong> أسود/أبيض</li>
             <li><strong>الحجم:</strong> حوالي 27.5 لتر</li>
           </ul>
         </div>',
        'حقيبة ظهر أديداس كلاسيك', 'حقيبة ظهر أديداس كلاسيك بشعار Badge of Sport',
        'تسوق حقيبة ظهر أديداس كلاسيك المتينة. مثالية للمدرسة أو العمل أو الصالة الرياضية.',
        'أديداس, حقيبة ظهر, حقيبة, حقيبة كلاسيك, Badge of Sport', 'حقيبة ظهر أديداس كلاسيك بشعار Badge of Sport',
        'أحزمة كتف مبطنة', 'adidas-classic-backpack-ar', 'ar', 9)
on conflict (description_id) do nothing;

-- Product 10: H&M Men's Belt (Manuf: 4, Type: 3)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (10, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.2, 3.5, 'REF-HM-AC-BLT10', 10, 4,
        '65f023632bc46470c104b76f', 3)
on conflict (product_id) do nothing;

-- English Description (ID: 19)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (19, NOW(), NOW(),
        '<div>
           <h2>H&M Leather Belt</h2>
           <p>A classic leather belt from H&M with a metal buckle. A versatile accessory for jeans or trousers.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Genuine Leather:</strong> Durable and develops character over time.</li>
             <li><strong>Metal Buckle:</strong> Simple and classic design.</li>
             <li><strong>Adjustable Fit:</strong> Multiple holes for sizing.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> 100% Leather</li>
             <li><strong>Color:</strong> Brown</li>
             <li><strong>Width:</strong> Approx. 3.5cm</li>
           </ul>
         </div>',
        'H&M Leather Belt', 'H&M Men''s Leather Belt',
        'Shop the classic H&M leather belt for men. A versatile wardrobe essential.',
        'H&M, belt, leather belt, men accessories', 'H&M Men''s Leather Belt', 'Genuine leather', 'hm-leather-belt-men',
        'en', 10)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 20)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (20, NOW(), NOW(),
        '<div dir="rtl">
           <h2>حزام جلد إتش آند إم</h2>
           <p>حزام جلد كلاسيكي من إتش آند إم بإبزيم معدني. إكسسوار متعدد الاستخدامات للجينز أو البناطيل.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>جلد طبيعي:</strong> متين ويكتسب طابعًا بمرور الوقت.</li>
             <li><strong>إبزيم معدني:</strong> تصميم بسيط وكلاسيكي.</li>
             <li><strong>مقاس قابل للتعديل:</strong> ثقوب متعددة لتحديد المقاس.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> 100% جلد</li>
             <li><strong>اللون:</strong> بني</li>
             <li><strong>العرض:</strong> حوالي 3.5 سم</li>
           </ul>
         </div>',
        'حزام جلد إتش آند إم', 'حزام جلد رجالي من إتش آند إم',
        'تسوق حزام الجلد الكلاسيكي من إتش آند إم للرجال. قطعة أساسية متعددة الاستخدامات في خزانة الملابس.',
        'إتش آند إم, حزام, حزام جلد, إكسسوارات رجالية', 'حزام جلد رجالي إتش آند إم', 'جلد طبيعي',
        'hm-leather-belt-men-ar', 'ar', 10)
on conflict (description_id) do nothing;

-- Product 11: Gucci Women's Loafers (Manuf: 5, Type: 2)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (11, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.7, null, 'REF-GU-SH-LOF11', 11, 5,
        '65f023632bc46470c104b76f', 2)
on conflict (product_id) do nothing;

-- English Description (ID: 21)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (21, NOW(), NOW(),
        '<div>
           <h2>Gucci Jordaan Leather Loafer (Women)</h2>
           <p>The Gucci Jordaan loafer is a key silhouette, updated in smooth leather with the signature Horsebit detail. Timeless style and Italian craftsmanship.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Horsebit Detail:</strong> Iconic Gucci hardware.</li>
             <li><strong>Smooth Leather:</strong> High-quality and elegant.</li>
             <li><strong>Slim Shape:</strong> Refined silhouette.</li>
             <li><strong>Leather Sole:</strong> Durable and classic.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> Leather</li>
             <li><strong>Color:</strong> Black</li>
             <li><strong>Heel Height:</strong> 10mm</li>
             <li><strong>Made in Italy</strong></li>
           </ul>
         </div>',
        'Gucci Jordaan Loafer (Women)', 'Gucci Women''s Jordaan Leather Loafer',
        'Shop the timeless Gucci Jordaan leather loafers for women, featuring the iconic Horsebit detail.',
        'Gucci, loafers, Jordaan, women shoes, leather shoes, Horsebit, luxury',
        'Gucci Women''s Jordaan Loafer | Luxury Footwear', 'Signature Horsebit detail', 'gucci-jordaan-loafer-women',
        'en', 11)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 22)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (22, NOW(), NOW(),
        '<div dir="rtl">
           <h2>حذاء لوفر غوتشي جوردان الجلدي (للنساء)</h2>
           <p>يعد حذاء لوفر غوتشي جوردان صورة ظلية رئيسية، تم تحديثه بجلد ناعم مع تفاصيل Horsebit المميزة. أناقة خالدة وحرفية إيطالية.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>تفاصيل Horsebit:</strong> قطع معدنية أيقونية من غوتشي.</li>
             <li><strong>جلد ناعم:</strong> عالي الجودة وأنيق.</li>
             <li><strong>شكل نحيف:</strong> صورة ظلية مصقولة.</li>
             <li><strong>نعل جلدي:</strong> متين وكلاسيكي.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> جلد</li>
             <li><strong>اللون:</strong> أسود</li>
             <li><strong>ارتفاع الكعب:</strong> 10 مم</li>
             <li><strong>صنع في إيطاليا</strong></li>
           </ul>
         </div>',
        'لوفر غوتشي جوردان (نساء)', 'حذاء لوفر نسائي غوتشي جوردان من الجلد',
        'تسوقي حذاء لوفر غوتشي جوردان الجلدي الخالد للنساء، الذي يتميز بتفاصيل Horsebit الأيقونية.',
        'غوتشي, لوفر, جوردان, أحذية نسائية, حذاء جلد, Horsebit, فخامة', 'حذاء لوفر نسائي غوتشي جوردان | أحذية فاخرة',
        'تفاصيل Horsebit المميزة', 'gucci-jordaan-loafer-women-ar', 'ar', 11)
on conflict (description_id) do nothing;

-- Product 12: Chanel Card Holder (Manuf: 6, Type: 3)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (12, NOW(), NOW(), true, NOW(), false, 7.5,
        false, 11.2, true, false, 0.1, 0.5, 'REF-CH-AC-CRD12', 12, 6,
        '65f023632bc46470c104b76f', 3)
on conflict (product_id) do nothing;

-- English Description (ID: 23)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (23, NOW(), NOW(),
        '<div>
           <h2>Chanel Classic Card Holder</h2>
           <p>An elegant and practical accessory, the Chanel Classic Card Holder in quilted caviar leather. Features the iconic CC logo and multiple card slots.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Quilted Caviar Leather:</strong> Durable and textured finish.</li>
             <li><strong>CC Logo:</strong> Signature metal hardware.</li>
             <li><strong>Multiple Card Slots:</strong> Organizes your essential cards.</li>
             <li><strong>Compact Design:</strong> Fits easily into small bags or pockets.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> Caviar Leather</li>
             <li><strong>Color:</strong> Black with Gold-Tone Metal</li>
             <li><strong>Dimensions:</strong> Approx. 11.2cm x 7.5cm x 0.5cm</li>
           </ul>
         </div>',
        'Chanel Classic Card Holder', 'Chanel Classic Quilted Card Holder Caviar Leather',
        'Shop the elegant Chanel Classic Card Holder in durable caviar leather.',
        'Chanel, card holder, wallet, caviar leather, quilted, CC logo, accessories, luxury',
        'Chanel Classic Card Holder | Luxury Small Leather Goods', 'Quilted caviar leather',
        'chanel-classic-card-holder', 'en', 12)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 24)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (24, NOW(), NOW(),
        '<div dir="rtl">
           <h2>حامل بطاقات شانيل كلاسيك</h2>
           <p>إكسسوار أنيق وعملي، حامل بطاقات شانيل كلاسيك من جلد الكافيار المبطن. يتميز بشعار CC الأيقوني وفتحات بطاقات متعددة.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>جلد كافيار مبطن:</strong> لمسة نهائية متينة ومحببة.</li>
             <li><strong>شعار CC:</strong> قطع معدنية مميزة.</li>
             <li><strong>فتحات بطاقات متعددة:</strong> تنظم بطاقاتك الأساسية.</li>
             <li><strong>تصميم مدمج:</strong> يناسب بسهولة الحقائب الصغيرة أو الجيوب.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> جلد كافيار</li>
             <li><strong>اللون:</strong> أسود مع معدن ذهبي اللون</li>
             <li><strong>الأبعاد:</strong> حوالي 11.2 سم × 7.5 سم × 0.5 سم</li>
           </ul>
         </div>',
        'حامل بطاقات شانيل كلاسيك', 'حامل بطاقات شانيل كلاسيك مبطن جلد كافيار',
        'تسوقي حامل بطاقات شانيل كلاسيك الأنيق من جلد الكافيار المتين.',
        'شانيل, حامل بطاقات, محفظة, جلد كافيار, مبطن, شعار CC, إكسسوارات, فخامة',
        'حامل بطاقات شانيل كلاسيك | منتجات جلدية صغيرة فاخرة', 'جلد كافيار مبطن', 'chanel-classic-card-holder-ar', 'ar',
        12)
on conflict (description_id) do nothing;

-- Product 13: Nike Women's Leggings (Manuf: 1, Type: 1)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (13, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.3, null, 'REF-NK-CL-LEG13', 13, 1,
        '65f023632bc46470c104b76f', 1)
on conflict (product_id) do nothing;

-- English Description (ID: 25)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (25, NOW(), NOW(),
        '<div>
           <h2>Nike One Women''s Mid-Rise Leggings</h2>
           <p>Designed for all-day wear, not just for training. The Nike One Leggings are made with soft, Dri-FIT fabric that moves sweat from your skin for quicker evaporation to help you stay dry and comfortable.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Dri-FIT Technology:</strong> Keeps you dry and comfortable.</li>
             <li><strong>Non-Sheer Fabric:</strong> Passes the squat test for confident coverage.</li>
             <li><strong>Mid-Rise Waistband:</strong> Contoured design with a V shape on the back.</li>
             <li><strong>Hidden Pockets:</strong> Waistband includes 2 hidden pockets for essentials.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> Polyester/Spandex Blend</li>
             <li><strong>Color:</strong> Black/White</li>
             <li><strong>Fit:</strong> Tight Fit</li>
           </ul>
         </div>',
        'Nike One Leggings (Women)', 'Nike One Women''s Mid-Rise Dri-FIT Leggings',
        'Shop the versatile Nike One leggings for women. Dri-FIT technology for all-day comfort.',
        'Nike, leggings, Dri-FIT, women clothing, sportswear, training tights',
        'Nike One Women''s Mid-Rise Leggings | All-Day Comfort', 'Dri-FIT sweat-wicking fabric',
        'nike-one-leggings-women', 'en', 13)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 26)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (26, NOW(), NOW(),
        '<div dir="rtl">
           <h2>ليقنز نايكي ون للنساء بخصر متوسط</h2>
           <p>مصمم للارتداء طوال اليوم، وليس فقط للتدريب. ليقنز نايكي ون مصنوع من نسيج Dri-FIT ناعم يمتص العرق من بشرتك لتبخر أسرع لمساعدتك على البقاء جافة ومرتاحة.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>تقنية Dri-FIT:</strong> تبقيك جافة ومرتاحة.</li>
             <li><strong>نسيج غير شفاف:</strong> يجتاز اختبار القرفصاء لتغطية واثقة.</li>
             <li><strong>حزام خصر متوسط الارتفاع:</strong> تصميم محدد بشكل V في الخلف.</li>
             <li><strong>جيوب مخفية:</strong> يتضمن حزام الخصر جيبين مخفيين للضروريات.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> مزيج بوليستر/سباندكس</li>
             <li><strong>اللون:</strong> أسود/أبيض</li>
             <li><strong>المقاس:</strong> ضيق</li>
           </ul>
         </div>',
        'ليقنز نايكي ون (نساء)', 'ليقنز نسائي نايكي ون بخصر متوسط وتقنية Dri-FIT',
        'تسوقي ليقنز نايكي ون متعدد الاستخدامات للنساء. تقنية Dri-FIT لراحة طوال اليوم.',
        'نايكي, ليقنز, Dri-FIT, ملابس نسائية, ملابس رياضية, بنطال تدريب',
        'ليقنز نسائي نايكي ون بخصر متوسط | راحة طوال اليوم', 'نسيج Dri-FIT طارد للعرق', 'nike-one-leggings-women-ar',
        'ar', 13)
on conflict (description_id) do nothing;

-- Product 14: Zara Men's Polo Shirt (Manuf: 2, Type: 1)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (14, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.3, null, 'REF-ZR-CL-POL14', 14, 2,
        '65f023632bc46470c104b76f', 1)
on conflict (product_id) do nothing;

-- English Description (ID: 27)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (27, NOW(), NOW(),
        '<div>
           <h2>Zara Basic Piqué Polo Shirt</h2>
           <p>A classic wardrobe staple, this polo shirt from Zara is made from comfortable cotton piqué fabric. Features a traditional collar and button placket.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Cotton Piqué Fabric:</strong> Textured and breathable.</li>
             <li><strong>Classic Polo Collar:</strong> With button placket.</li>
             <li><strong>Short Sleeves:</strong> With ribbed cuffs.</li>
             <li><strong>Regular Fit:</strong> Comfortable for everyday wear.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> 100% Cotton</li>
             <li><strong>Color:</strong> White</li>
             <li><strong>Care:</strong> Machine wash</li>
           </ul>
         </div>',
        'Zara Basic Polo Shirt', 'Zara Men''s Basic Piqué Polo Shirt',
        'Shop the classic Zara basic piqué polo shirt for men. A comfortable cotton essential.',
        'Zara, polo shirt, piqué, men clothing, basic, cotton', 'Zara Men''s Basic Piqué Polo Shirt | Wardrobe Staple',
        'Comfortable cotton piqué', 'zara-basic-polo-shirt-men', 'en', 14)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 28)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (28, NOW(), NOW(),
        '<div dir="rtl">
           <h2>قميص بولو أساسي بيكيه من زارا</h2>
           <p>قطعة أساسية كلاسيكية في خزانة الملابس، قميص البولو هذا من زارا مصنوع من نسيج قطن بيكيه مريح. يتميز بياقة تقليدية وفتحة أزرار.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>نسيج قطن بيكيه:</strong> محكم ويسمح بالتهوية.</li>
             <li><strong>ياقة بولو كلاسيكية:</strong> مع فتحة أزرار.</li>
             <li><strong>أكمام قصيرة:</strong> بأساور مضلعة.</li>
             <li><strong>قصة عادية:</strong> مريحة للارتداء اليومي.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> 100% قطن</li>
             <li><strong>اللون:</strong> أبيض</li>
             <li><strong>العناية:</strong> غسيل في الغسالة</li>
           </ul>
         </div>',
        'قميص بولو أساسي زارا', 'قميص بولو رجالي أساسي بيكيه من زارا',
        'تسوق قميص بولو زارا الأساسي البيكيه الكلاسيكي للرجال. قطعة قطنية أساسية مريحة.',
        'زارا, قميص بولو, بيكيه, ملابس رجالية, أساسي, قطن', 'قميص بولو رجالي أساسي بيكيه من زارا | قطعة أساسية',
        'قطن بيكيه مريح', 'zara-basic-polo-shirt-men-ar', 'ar', 14)
on conflict (description_id) do nothing;

-- Product 15: Adidas Kids' Sandals (Manuf: 3, Type: 2)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (15, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.3, null, 'REF-AD-SH-SND15', 15, 3,
        '65f023632bc46470c104b76f', 2)
on conflict (product_id) do nothing;

-- English Description (ID: 29)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (29, NOW(), NOW(),
        '<div>
           <h2>Adidas Adilette Aqua Slides (Kids)</h2>
           <p>Simple and comfortable slides for kids. The Adidas Adilette Aqua slides are perfect for the pool or shower, featuring a quick-drying design.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Quick-Drying:</strong> Ideal for wear around water.</li>
             <li><strong>Single-Bandage Upper:</strong> Classic Adilette style.</li>
             <li><strong>Soft Cloudfoam Footbed:</strong> Provides lightweight cushioning.</li>
             <li><strong>EVA Outsole:</strong> Lightweight and durable.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> Synthetic Upper / EVA Outsole</li>
             <li><strong>Color:</strong> Core Black / Cloud White</li>
             <li><strong>Use:</strong> Pool, Shower, Casual</li>
           </ul>
         </div>',
        'Adidas Adilette Aqua Slides (Kids)', 'Adidas Kids'' Adilette Aqua Slides',
        'Shop the comfortable and quick-drying Adidas Adilette Aqua slides for kids.',
        'Adidas, slides, sandals, kids shoes, Adilette, Cloudfoam, pool shoes',
        'Adidas Kids'' Adilette Aqua Slides | Pool & Casual', 'Quick-drying design', 'adidas-adilette-aqua-slides-kids',
        'en', 15)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 30)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (30, NOW(), NOW(),
        '<div dir="rtl">
           <h2>شبشب أديداس أديليت أكوا (للأطفال)</h2>
           <p>شبشب بسيط ومريح للأطفال. شبشب أديداس أديليت أكوا مثالي للمسبح أو الاستحمام، ويتميز بتصميم سريع الجفاف.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>سريع الجفاف:</strong> مثالي للارتداء حول الماء.</li>
             <li><strong>جزء علوي بشريط واحد:</strong> ستايل أديليت كلاسيكي.</li>
             <li><strong>وسادة قدم Cloudfoam ناعمة:</strong> توفر توسيدًا خفيف الوزن.</li>
             <li><strong>نعل سفلي EVA:</strong> خفيف الوزن ومتين.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> جزء علوي صناعي / نعل سفلي EVA</li>
             <li><strong>اللون:</strong> أسود أساسي / أبيض سحابي</li>
             <li><strong>الاستخدام:</strong> مسبح، استحمام، كاجوال</li>
           </ul>
         </div>',
        'شبشب أديداس أديليت أكوا (أطفال)', 'شبشب أديداس أديليت أكوا للأطفال',
        'تسوق شبشب أديداس أديليت أكوا المريح وسريع الجفاف للأطفال.',
        'أديداس, شبشب, صندل, أحذية أطفال, أديليت, كلاود فوم, حذاء مسبح',
        'شبشب أديداس أديليت أكوا للأطفال | مسبح وكاجوال', 'تصميم سريع الجفاف', 'adidas-adilette-aqua-slides-kids-ar',
        'ar', 15)
on conflict (description_id) do nothing;
/*
Generated SQL inserts for products 16-30 based on the provided template and instructions:
- 15 products for catalog.product.
- 2 descriptions (en, ar) for each product in catalog.product_description.
- store_id = '65f023632bc46470c104b76f'.
- manufacturer_id from (1, 2, 3, 4, 5, 6).
- product_type_id from (1, 2, 3, 4).
- product_id sequential from 16 to 30.
- description_id sequential from 31 to 60.
- Realistic SKUs, Ref SKUs generated.
- Realistic fashion-related names, titles, meta descriptions, etc. generated.
- Valid HTML generated for the description field.
- Timestamps replaced with NOW().
- Domain is fashion.
*/

-- Product 16: H&M Women's Dress (Manuf: 4, Type: 1)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (16, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.4, null, 'REF-HM-CL-DRS16', 16, 4,
        '65f023632bc46470c104b76f', 1)
on conflict (product_id) do nothing;

-- English Description (ID: 31)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (31, NOW(), NOW(),
        '<div>
           <h2>H&M Patterned Wrap Dress</h2>
           <p>A flattering wrap dress from H&M in a soft, woven fabric with an all-over pattern. Features a V-neckline and tie belt at the waist.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Wrap Design:</strong> Creates a customizable and flattering fit.</li>
             <li><strong>Woven Fabric:</strong> Lightweight and comfortable.</li>
             <li><strong>V-Neckline:</strong> Elegant and feminine.</li>
             <li><strong>Tie Belt:</strong> Cinches the waist for definition.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> Viscose Blend</li>
             <li><strong>Color:</strong> Black/Floral Print</li>
             <li><strong>Length:</strong> Knee-length (approx.)</li>
             <li><strong>Sleeves:</strong> Short Sleeves</li>
           </ul>
         </div>',
        'H&M Patterned Wrap Dress', 'H&M Women''s Patterned Wrap Dress',
        'Shop the stylish H&M patterned wrap dress. Flattering design, perfect for casual or semi-formal occasions.',
        'H&M, dress, wrap dress, patterned dress, women clothing, floral print',
        'H&M Women''s Patterned Wrap Dress | Versatile Style', 'Flattering wrap design',
        'hm-patterned-wrap-dress-women', 'en', 16)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 32)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (32, NOW(), NOW(),
        '<div dir="rtl">
           <h2>فستان لف بنقوش من إتش آند إم</h2>
           <p>فستان لف جذاب من إتش آند إم من نسيج منسوج ناعم بنقوش شاملة. يتميز بفتحة رقبة على شكل V وحزام ربط عند الخصر.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>تصميم لف:</strong> يخلق مقاسًا قابلاً للتخصيص وجذابًا.</li>
             <li><strong>نسيج منسوج:</strong> خفيف الوزن ومريح.</li>
             <li><strong>فتحة رقبة على شكل V:</strong> أنيقة وأنثوية.</li>
             <li><strong>حزام ربط:</strong> يحدد الخصر.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> مزيج فيسكوز</li>
             <li><strong>اللون:</strong> أسود/طبعة زهور</li>
             <li><strong>الطول:</strong> طول الركبة (تقريبًا)</li>
             <li><strong>الأكمام:</strong> أكمام قصيرة</li>
           </ul>
         </div>',
        'فستان لف بنقوش إتش آند إم', 'فستان نسائي لف بنقوش من إتش آند إم',
        'تسوقي فستان اللف الأنيق بنقوش من إتش آند إم. تصميم جذاب، مثالي للمناسبات الكاجوال أو شبه الرسمية.',
        'إتش آند إم, فستان, فستان لف, فستان منقوش, ملابس نسائية, طبعة زهور',
        'فستان نسائي لف بنقوش من إتش آند إم | أناقة متعددة الاستخدامات', 'تصميم لف جذاب',
        'hm-patterned-wrap-dress-women-ar', 'ar', 16)
on conflict (description_id) do nothing;

-- Product 17: Gucci Men's Wallet (Manuf: 5, Type: 3)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (17, NOW(), NOW(), true, NOW(), false, 9.0,
        false, 11.0, true, false, 0.1, 1.5, 'REF-GU-AC-WAL17', 17, 5,
        '65f023632bc46470c104b76f', 3)
on conflict (product_id) do nothing;

-- English Description (ID: 33)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (33, NOW(), NOW(),
        '<div>
           <h2>Gucci GG Supreme Wallet</h2>
           <p>A classic bi-fold wallet from Gucci crafted in the iconic GG Supreme canvas with leather trim. Features multiple card slots and bill compartments.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>GG Supreme Canvas:</strong> Durable coated canvas with low environmental impact.</li>
             <li><strong>Leather Trim:</strong> Adds a touch of luxury and durability.</li>
             <li><strong>Multiple Card Slots:</strong> Eight card slots for organization.</li>
             <li><strong>Bill Compartments:</strong> Two compartments for notes.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> GG Supreme Canvas / Leather</li>
             <li><strong>Color:</strong> Beige/Ebony with Brown Leather Trim</li>
             <li><strong>Dimensions:</strong> Approx. 11cm (W) x 9cm (H)</li>
             <li><strong>Made in Italy</strong></li>
           </ul>
         </div>',
        'Gucci GG Supreme Wallet', 'Gucci Men''s GG Supreme Bi-fold Wallet',
        'Shop the classic Gucci GG Supreme bi-fold wallet for men. Durable canvas and leather construction.',
        'Gucci, wallet, bi-fold wallet, GG Supreme, canvas, men accessories, luxury',
        'Gucci Men''s GG Supreme Wallet | Luxury Accessories', 'Iconic GG Supreme canvas',
        'gucci-gg-supreme-wallet-men', 'en', 17)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 34)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (34, NOW(), NOW(),
        '<div dir="rtl">
           <h2>محفظة غوتشي جي جي سوبريم</h2>
           <p>محفظة كلاسيكية ثنائية الطي من غوتشي مصنوعة من قماش جي جي سوبريم الأيقوني مع حواف جلدية. تتميز بفتحات بطاقات متعددة ومقصورات للأوراق النقدية.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>قماش جي جي سوبريم:</strong> قماش متين مطلي بتأثير بيئي منخفض.</li>
             <li><strong>حواف جلدية:</strong> تضيف لمسة من الفخامة والمتانة.</li>
             <li><strong>فتحات بطاقات متعددة:</strong> ثماني فتحات بطاقات للتنظيم.</li>
             <li><strong>مقصورات للأوراق النقدية:</strong> مقصورتان للأوراق.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> قماش جي جي سوبريم / جلد</li>
             <li><strong>اللون:</strong> بيج/أبنوسي مع حواف جلدية بنية</li>
             <li><strong>الأبعاد:</strong> حوالي 11 سم (عرض) × 9 سم (ارتفاع)</li>
             <li><strong>صنع في إيطاليا</strong></li>
           </ul>
         </div>',
        'محفظة غوتشي جي جي سوبريم', 'محفظة رجالية ثنائية الطي غوتشي جي جي سوبريم',
        'تسوق محفظة غوتشي جي جي سوبريم الكلاسيكية ثنائية الطي للرجال. تصميم متين من القماش والجلد.',
        'غوتشي, محفظة, محفظة ثنائية الطي, جي جي سوبريم, قماش, إكسسوارات رجالية, فخامة',
        'محفظة رجالية غوتشي جي جي سوبريم | إكسسوارات فاخرة', 'قماش جي جي سوبريم الأيقوني',
        'gucci-gg-supreme-wallet-men-ar', 'ar', 17)
on conflict (description_id) do nothing;

-- Product 18: Chanel Brooch (Manuf: 6, Type: 3)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (18, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.05, null, 'REF-CH-AC-BRH18', 18, 6,
        '65f023632bc46470c104b76f', 3)
on conflict (product_id) do nothing;

-- English Description (ID: 35)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (35, NOW(), NOW(),
        '<div>
           <h2>Chanel CC Logo Brooch</h2>
           <p>An iconic Chanel brooch featuring the signature interlocking CC logo, often embellished with crystals, pearls, or crafted from metal.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Interlocking CC Logo:</strong> Instantly recognizable Chanel signature.</li>
             <li><strong>Premium Materials:</strong> Often uses metal, crystals, pearls, or resin.</li>
             <li><strong>Pin Fastening:</strong> Securely attaches to clothing or accessories.</li>
             <li><strong>Versatile Accessory:</strong> Adds a touch of luxury to any outfit.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> Metal & Strass (Example)</li>
             <li><strong>Color:</strong> Gold-Tone & Crystal (Example)</li>
             <li><strong>Dimensions:</strong> Varies by specific design</li>
           </ul>
         </div>',
        'Chanel CC Logo Brooch', 'Chanel Metal & Strass CC Logo Brooch',
        'Shop the iconic Chanel CC logo brooch. A timeless luxury accessory to elevate any look.',
        'Chanel, brooch, CC logo, accessories, luxury, jewelry', 'Chanel CC Logo Brooch | Luxury Accessories',
        'Signature CC logo design', 'chanel-cc-logo-brooch', 'en', 18)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 36)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (36, NOW(), NOW(),
        '<div dir="rtl">
           <h2>بروش شانيل بشعار CC</h2>
           <p>بروش شانيل أيقوني يتميز بشعار CC المتشابك المميز، غالبًا ما يكون مزينًا بالكريستال أو اللؤلؤ أو مصنوعًا من المعدن.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>شعار CC المتشابك:</strong> توقيع شانيل يمكن التعرف عليه على الفور.</li>
             <li><strong>مواد فاخرة:</strong> غالبًا ما تستخدم المعدن أو الكريستال أو اللؤلؤ أو الراتنج.</li>
             <li><strong>إغلاق بدبوس:</strong> يثبت بأمان على الملابس أو الإكسسوارات.</li>
             <li><strong>إكسسوار متعدد الاستخدامات:</strong> يضيف لمسة من الفخامة إلى أي زي.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> معدن وستراس (مثال)</li>
             <li><strong>اللون:</strong> لون ذهبي وكريستال (مثال)</li>
             <li><strong>الأبعاد:</strong> تختلف حسب التصميم المحدد</li>
           </ul>
         </div>',
        'بروش شانيل بشعار CC', 'بروش شانيل معدن وستراس بشعار CC',
        'تسوقي بروش شانيل الأيقوني بشعار CC. إكسسوار فاخر خالد للارتقاء بأي إطلالة.',
        'شانيل, بروش, شعار CC, إكسسوارات, فخامة, مجوهرات', 'بروش شانيل بشعار CC | إكسسوارات فاخرة',
        'تصميم شعار CC المميز', 'chanel-cc-logo-brooch-ar', 'ar', 18)
on conflict (description_id) do nothing;

-- Product 19: Nike Men's Basketball Shorts (Manuf: 1, Type: 1)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (19, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.3, null, 'REF-NK-CL-BBS19', 19, 1,
        '65f023632bc46470c104b76f', 1)
on conflict (product_id) do nothing;

-- English Description (ID: 37)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (37, NOW(), NOW(),
        '<div>
           <h2>Nike Dri-FIT DNA Basketball Shorts</h2>
           <p>Dominate the court in the Nike Dri-FIT DNA Shorts. Made with lightweight, breathable fabric featuring sweat-wicking technology to keep you comfortable.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Dri-FIT Technology:</strong> Helps you stay dry and comfortable.</li>
             <li><strong>Lightweight Knit Fabric:</strong> Soft feel with breathability.</li>
             <li><strong>Elastic Waistband with Drawcord:</strong> Secure, personalized fit.</li>
             <li><strong>Zippered Utility Pocket:</strong> Securely stores small items.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> 100% Polyester</li>
             <li><strong>Color:</strong> University Red/Black</li>
             <li><strong>Fit:</strong> Standard Fit</li>
           </ul>
         </div>',
        'Nike DNA Basketball Shorts', 'Nike Men''s Dri-FIT DNA Basketball Shorts',
        'Shop Nike Dri-FIT DNA basketball shorts for men. Lightweight, breathable, and comfortable on the court.',
        'Nike, basketball shorts, Dri-FIT, men clothing, sportswear, DNA shorts',
        'Nike Men''s DNA Basketball Shorts | Sportswear', 'Lightweight Dri-FIT fabric',
        'nike-dna-basketball-shorts-men', 'en', 19)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 38)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (38, NOW(), NOW(),
        '<div dir="rtl">
           <h2>شورت كرة السلة نايكي دراي-فت DNA</h2>
           <p>سيطر على الملعب مع شورت نايكي دراي-فت DNA. مصنوع من نسيج خفيف الوزن يسمح بالتهوية ويتميز بتقنية طاردة للعرق للحفاظ على راحتك.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>تقنية Dri-FIT:</strong> تساعدك على البقاء جافًا ومرتاحًا.</li>
             <li><strong>نسيج محبوك خفيف الوزن:</strong> ملمس ناعم مع تهوية.</li>
             <li><strong>حزام خصر مطاطي برباط:</strong> مقاس آمن وشخصي.</li>
             <li><strong>جيب عملي بسحاب:</strong> يخزن الأشياء الصغيرة بأمان.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> 100% بوليستر</li>
             <li><strong>اللون:</strong> أحمر جامعي/أسود</li>
             <li><strong>المقاس:</strong> قصة قياسية</li>
           </ul>
         </div>',
        'شورت كرة السلة نايكي DNA', 'شورت كرة سلة رجالي نايكي دراي-فت DNA',
        'تسوق شورت كرة السلة نايكي دراي-فت DNA للرجال. خفيف الوزن، يسمح بالتهوية، ومريح في الملعب.',
        'نايكي, شورت كرة سلة, دراي-فت, ملابس رجالية, ملابس رياضية, شورت DNA',
        'شورت كرة سلة رجالي نايكي DNA | ملابس رياضية', 'نسيج Dri-FIT خفيف الوزن', 'nike-dna-basketball-shorts-men-ar',
        'ar', 19)
on conflict (description_id) do nothing;

-- Product 20: Zara Women's Sandals (Manuf: 2, Type: 2)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (20, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.5, null, 'REF-ZR-SH-SND20', 20, 2,
        '65f023632bc46470c104b76f', 2)
on conflict (product_id) do nothing;

-- English Description (ID: 39)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (39, NOW(), NOW(),
        '<div>
           <h2>Zara Flat Leather Sandals</h2>
           <p>Simple and chic flat sandals from Zara crafted from leather. Feature thin straps and an ankle buckle closure for a secure fit.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Leather Upper:</strong> Quality material for comfort and style.</li>
             <li><strong>Flat Sole:</strong> Comfortable for all-day wear.</li>
             <li><strong>Thin Straps:</strong> Minimalist and elegant design.</li>
             <li><strong>Ankle Buckle Closure:</strong> Ensures a secure fit.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> 100% Leather Upper</li>
             <li><strong>Sole:</strong> Rubber/Synthetic</li>
             <li><strong>Color:</strong> Tan</li>
           </ul>
         </div>',
        'Zara Flat Leather Sandals', 'Zara Women''s Flat Leather Sandals',
        'Shop simple and chic flat leather sandals from Zara. Perfect for summer days.',
        'Zara, sandals, flat sandals, leather sandals, women shoes, summer shoes',
        'Zara Women''s Flat Leather Sandals | Summer Footwear', 'Genuine leather upper',
        'zara-flat-leather-sandals-women', 'en', 20)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 40)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (40, NOW(), NOW(),
        '<div dir="rtl">
           <h2>صندل زارا مسطح من الجلد</h2>
           <p>صندل مسطح بسيط وأنيق من زارا مصنوع من الجلد. يتميز بأحزمة رفيعة وإغلاق بإبزيم الكاحل لمقاس آمن.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>جزء علوي من الجلد:</strong> خامة عالية الجودة للراحة والأناقة.</li>
             <li><strong>نعل مسطح:</strong> مريح للارتداء طوال اليوم.</li>
             <li><strong>أحزمة رفيعة:</strong> تصميم بسيط وأنيق.</li>
             <li><strong>إغلاق بإبزيم الكاحل:</strong> يضمن مقاسًا آمنًا.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> جزء علوي جلد 100%</li>
             <li><strong>النعل:</strong> مطاط/صناعي</li>
             <li><strong>اللون:</strong> تان (بني فاتح)</li>
           </ul>
         </div>',
        'صندل زارا مسطح جلد', 'صندل نسائي مسطح من الجلد من زارا',
        'تسوقي صندل زارا المسطح البسيط والأنيق من الجلد. مثالي لأيام الصيف.',
        'زارا, صندل, صندل مسطح, صندل جلد, أحذية نسائية, حذاء صيفي', 'صندل نسائي مسطح من الجلد من زارا | أحذية صيفية',
        'جزء علوي من الجلد الطبيعي', 'zara-flat-leather-sandals-women-ar', 'ar', 20)
on conflict (description_id) do nothing;

-- Product 21: Adidas Men's Hoodie (Manuf: 3, Type: 1)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (21, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.7, null, 'REF-AD-CL-HOD21', 21, 3,
        '65f023632bc46470c104b76f', 1)
on conflict (product_id) do nothing;

-- English Description (ID: 41)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (41, NOW(), NOW(),
        '<div>
           <h2>Adidas Essentials Fleece 3-Stripes Hoodie</h2>
           <p>A comfortable classic. This Adidas hoodie is made of soft fleece for a cozy feel. Features the iconic 3-Stripes down the sleeves and a kangaroo pocket.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Soft Fleece Fabric:</strong> Provides warmth and comfort.</li>
             <li><strong>Regular Fit:</strong> Comfortable balance between loose and snug.</li>
             <li><strong>Drawcord-Adjustable Hood:</strong> Offers personalized coverage.</li>
             <li><strong>Kangaroo Pocket:</strong> For storage or hand warmth.</li>
             <li><strong>Ribbed Cuffs and Hem:</strong> Ensure a snug fit.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> Cotton/Recycled Polyester Fleece</li>
             <li><strong>Color:</strong> Medium Grey Heather/Black</li>
           </ul>
         </div>',
        'Adidas Essentials Fleece Hoodie', 'Adidas Men''s Essentials Fleece 3-Stripes Hoodie',
        'Shop the comfortable Adidas Essentials fleece hoodie for men. Classic style with 3-Stripes detail.',
        'Adidas, hoodie, fleece hoodie, men clothing, sportswear, Essentials, 3-Stripes',
        'Adidas Men''s Essentials Fleece Hoodie | Sportswear', 'Soft fleece fabric',
        'adidas-essentials-fleece-hoodie-men', 'en', 21)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 42)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (42, NOW(), NOW(),
        '<div dir="rtl">
           <h2>هودي أديداس إسنشالز فليس 3 خطوط</h2>
           <p>قطعة كلاسيكية مريحة. هذا الهودي من أديداس مصنوع من فليس ناعم لشعور دافئ. يتميز بخطوط 3 الأيقونية على الأكمام وجيب كنغر.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>نسيج فليس ناعم:</strong> يوفر الدفء والراحة.</li>
             <li><strong>قصة عادية:</strong> توازن مريح بين الفضفاض والضيق.</li>
             <li><strong>غطاء رأس قابل للتعديل برباط:</strong> يوفر تغطية شخصية.</li>
             <li><strong>جيب كنغر:</strong> للتخزين أو تدفئة اليدين.</li>
             <li><strong>أساور وحافة مضلعة:</strong> تضمن مقاسًا محكمًا.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> فليس قطن/بوليستر معاد تدويره</li>
             <li><strong>اللون:</strong> رمادي هيذر متوسط/أسود</li>
           </ul>
         </div>',
        'هودي أديداس إسنشالز فليس', 'هودي رجالي أديداس إسنشالز فليس 3 خطوط',
        'تسوق هودي أديداس إسنشالز فليس المريح للرجال. ستايل كلاسيكي بتفاصيل 3 خطوط.',
        'أديداس, هودي, هودي فليس, ملابس رجالية, ملابس رياضية, إسنشالز, 3 خطوط',
        'هودي رجالي أديداس إسنشالز فليس | ملابس رياضية', 'نسيج فليس ناعم', 'adidas-essentials-fleece-hoodie-men-ar',
        'ar', 21)
on conflict (description_id) do nothing;

-- Product 22: H&M Kids' T-Shirt Pack (Manuf: 4, Type: 1)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (22, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.4, null, 'REF-HM-CL-KTP22', 22, 4,
        '65f023632bc46470c104b76f', 1)
on conflict (product_id) do nothing;

-- English Description (ID: 43)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (43, NOW(), NOW(),
        '<div>
           <h2>H&M 5-Pack T-Shirts (Kids)</h2>
           <p>Stock up on essentials with this pack of five soft cotton jersey T-shirts from H&M for kids. Features classic crew necks and short sleeves.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>5-Pack Value:</strong> Great value for everyday wear.</li>
             <li><strong>Soft Cotton Jersey:</strong> Comfortable and breathable.</li>
             <li><strong>Crew Neck:</strong> Classic T-shirt design.</li>
             <li><strong>Assorted Colors:</strong> Provides variety (colors may vary).</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> 100% Cotton (may vary slightly by color)</li>
             <li><strong>Fit:</strong> Regular Fit</li>
             <li><strong>Age Group:</strong> Kids</li>
           </ul>
         </div>',
        'H&M 5-Pack T-Shirts (Kids)', 'H&M Kids'' 5-Pack Cotton T-Shirts',
        'Stock up on kids'' essentials with this 5-pack of cotton T-shirts from H&M.',
        'H&M, t-shirt pack, kids clothing, cotton t-shirt, basics, multi-pack',
        'H&M Kids'' 5-Pack T-Shirts | Essentials', 'Soft cotton jersey 5-pack', 'hm-5-pack-tshirts-kids', 'en', 22)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 44)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (44, NOW(), NOW(),
        '<div dir="rtl">
           <h2>عبوة 5 تي شيرتات إتش آند إم (للأطفال)</h2>
           <p>احصل على الأساسيات مع هذه العبوة المكونة من خمسة تي شيرتات من قطن الجيرسي الناعم من إتش آند إم للأطفال. تتميز برقبة دائرية كلاسيكية وأكمام قصيرة.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>قيمة عبوة 5 قطع:</strong> قيمة رائعة للارتداء اليومي.</li>
             <li><strong>جيرسي قطني ناعم:</strong> مريح ويسمح بالتهوية.</li>
             <li><strong>رقبة دائرية:</strong> تصميم تي شيرت كلاسيكي.</li>
             <li><strong>ألوان متنوعة:</strong> توفر التنوع (قد تختلف الألوان).</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> 100% قطن (قد يختلف قليلاً حسب اللون)</li>
             <li><strong>المقاس:</strong> قصة عادية</li>
             <li><strong>الفئة العمرية:</strong> أطفال</li>
           </ul>
         </div>',
        'عبوة 5 تي شيرتات إتش آند إم (أطفال)', 'عبوة 5 تي شيرتات قطنية للأطفال من إتش آند إم',
        'احصل على أساسيات الأطفال مع هذه العبوة المكونة من 5 تي شيرتات قطنية من إتش آند إم.',
        'إتش آند إم, عبوة تي شيرتات, ملابس أطفال, تي شيرت قطن, أساسيات, عبوة متعددة',
        'عبوة 5 تي شيرتات للأطفال إتش آند إم | أساسيات', 'عبوة 5 قطع جيرسي قطني ناعم', 'hm-5-pack-tshirts-kids-ar',
        'ar', 22)
on conflict (description_id) do nothing;

-- Product 23: Gucci Men's Sneakers (Manuf: 5, Type: 2)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (23, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 1.0, null, 'REF-GU-SH-SNK23', 23, 5,
        '65f023632bc46470c104b76f', 2)
on conflict (product_id) do nothing;

-- English Description (ID: 45)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (45, NOW(), NOW(),
        '<div>
           <h2>Gucci Ace Leather Sneaker</h2>
           <p>The classic Gucci Ace sneaker, crafted from smooth leather and featuring the iconic Web stripe detail on the sides. A versatile luxury sneaker.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Smooth Leather Upper:</strong> Premium quality and look.</li>
             <li><strong>Web Stripe Detail:</strong> Signature green and red (or blue and red) stripe.</li>
             <li><strong>Embroidered Detail (Optional):</strong> Some versions feature bees, stars, etc.</li>
             <li><strong>Rubber Sole:</strong> Durable and comfortable.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> Leather</li>
             <li><strong>Color:</strong> White with Green/Red Web</li>
             <li><strong>Made in Italy</strong></li>
           </ul>
         </div>',
        'Gucci Ace Leather Sneaker', 'Gucci Men''s Ace Leather Sneaker with Web',
        'Shop the iconic Gucci Ace leather sneakers for men, featuring the signature Web stripe.',
        'Gucci, Ace sneaker, sneakers, men shoes, leather sneakers, Web stripe, luxury',
        'Gucci Men''s Ace Leather Sneaker | Luxury Footwear', 'Signature Web stripe detail',
        'gucci-ace-leather-sneaker-men', 'en', 23)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 46)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (46, NOW(), NOW(),
        '<div dir="rtl">
           <h2>سنيكرز غوتشي Ace الجلدي</h2>
           <p>سنيكرز غوتشي Ace الكلاسيكي، مصنوع من جلد ناعم ويتميز بتفاصيل شريط Web الأيقوني على الجانبين. سنيكرز فاخر متعدد الاستخدامات.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>جزء علوي من الجلد الناعم:</strong> جودة ومظهر فاخر.</li>
             <li><strong>تفاصيل شريط Web:</strong> شريط أخضر وأحمر (أو أزرق وأحمر) مميز.</li>
             <li><strong>تفاصيل مطرزة (اختياري):</strong> بعض الإصدارات تتميز بنحل، نجوم، إلخ.</li>
             <li><strong>نعل مطاطي:</strong> متين ومريح.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> جلد</li>
             <li><strong>اللون:</strong> أبيض مع شريط Web أخضر/أحمر</li>
             <li><strong>صنع في إيطاليا</strong></li>
           </ul>
         </div>',
        'سنيكرز غوتشي Ace جلد', 'سنيكرز رجالي غوتشي Ace جلدي مع شريط Web',
        'تسوق سنيكرز غوتشي Ace الجلدي الأيقوني للرجال، الذي يتميز بشريط Web المميز.',
        'غوتشي, سنيكرز Ace, سنيكرز, أحذية رجالية, سنيكرز جلد, شريط Web, فخامة',
        'سنيكرز رجالي غوتشي Ace جلدي | أحذية فاخرة', 'تفاصيل شريط Web المميز', 'gucci-ace-leather-sneaker-men-ar', 'ar',
        23)
on conflict (description_id) do nothing;

-- Product 24: Chanel Ballet Flats (Manuf: 6, Type: 2)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (24, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.5, null, 'REF-CH-SH-BAL24', 24, 6,
        '65f023632bc46470c104b76f', 2)
on conflict (product_id) do nothing;

-- English Description (ID: 47)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (47, NOW(), NOW(),
        '<div>
           <h2>Chanel Ballerinas</h2>
           <p>The iconic Chanel two-tone ballerina flats. Crafted from supple leather, often featuring a contrasting cap toe and a delicate bow detail.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Two-Tone Design:</strong> Classic and instantly recognizable Chanel style.</li>
             <li><strong>Supple Leather:</strong> Provides comfort and molds to the foot.</li>
             <li><strong>Cap Toe Detail:</strong> Often in a contrasting color or material.</li>
             <li><strong>Bow Accent:</strong> Delicate and feminine touch.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> Lambskin/Goatskin/Patent Leather (Varies by model)</li>
             <li><strong>Color:</strong> Beige/Black (Classic combination)</li>
             <li><strong>Heel Height:</strong> Flat</li>
           </ul>
         </div>',
        'Chanel Ballerinas', 'Chanel Two-Tone Leather Ballerinas',
        'Shop the iconic Chanel two-tone ballerina flats. Timeless elegance and comfort.',
        'Chanel, ballerinas, ballet flats, flat shoes, women shoes, two-tone, luxury',
        'Chanel Ballerinas | Luxury Footwear', 'Iconic two-tone design', 'chanel-ballerinas', 'en', 24)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 48)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (48, NOW(), NOW(),
        '<div dir="rtl">
           <h2>حذاء باليرينا شانيل</h2>
           <p>حذاء باليرينا شانيل الأيقوني بلونين. مصنوع من جلد مرن، وغالبًا ما يتميز بمقدمة بلون مغاير وتفاصيل فيونكة رقيقة.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>تصميم بلونين:</strong> ستايل شانيل كلاسيكي يمكن التعرف عليه على الفور.</li>
             <li><strong>جلد مرن:</strong> يوفر الراحة ويتشكل حسب القدم.</li>
             <li><strong>تفاصيل مقدمة الحذاء:</strong> غالبًا بلون أو خامة مغايرة.</li>
             <li><strong>لمسة فيونكة:</strong> لمسة رقيقة وأنثوية.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> جلد حمل/جلد ماعز/جلد لامع (يختلف حسب الموديل)</li>
             <li><strong>اللون:</strong> بيج/أسود (المزيج الكلاسيكي)</li>
             <li><strong>ارتفاع الكعب:</strong> مسطح</li>
           </ul>
         </div>',
        'باليرينا شانيل', 'حذاء باليرينا شانيل جلدي بلونين',
        'تسوقي حذاء باليرينا شانيل الأيقوني بلونين. أناقة وراحة خالدة.',
        'شانيل, باليرينا, حذاء فلات, حذاء مسطح, أحذية نسائية, لونين, فخامة', 'حذاء باليرينا شانيل | أحذية فاخرة',
        'تصميم أيقوني بلونين', 'chanel-ballerinas-ar', 'ar', 24)
on conflict (description_id) do nothing;

-- Product 25: Nike Women's Tank Top (Manuf: 1, Type: 1)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (25, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.2, null, 'REF-NK-CL-TNK25', 25, 1,
        '65f023632bc46470c104b76f', 1)
on conflict (product_id) do nothing;

-- English Description (ID: 49)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (49, NOW(), NOW(),
        '<div>
           <h2>Nike Dri-FIT One Luxe Tank Top</h2>
           <p>Part of the Nike Luxe line, this tank feels incredibly soft and smooth. Made with Dri-FIT technology to keep you dry during workouts or everyday activities.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Nike Luxe Fabric:</strong> Buttery-soft and comfortable.</li>
             <li><strong>Dri-FIT Technology:</strong> Wicks away sweat.</li>
             <li><strong>Slim Fit:</strong> Tailored feel without being restrictive.</li>
             <li><strong>Racerback Design:</strong> Allows for natural movement.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> Polyester/Spandex Blend</li>
             <li><strong>Color:</strong> White/Reflective Silver</li>
           </ul>
         </div>',
        'Nike Dri-FIT One Luxe Tank', 'Nike Women''s Dri-FIT One Luxe Standard Fit Tank Top',
        'Shop the soft Nike Dri-FIT One Luxe tank top for women. Perfect for workouts and beyond.',
        'Nike, tank top, Dri-FIT, Nike Luxe, women clothing, sportswear',
        'Nike Women''s Dri-FIT One Luxe Tank Top | Soft & Comfortable', 'Buttery-soft Luxe fabric',
        'nike-dri-fit-one-luxe-tank-women', 'en', 25)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 50)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (50, NOW(), NOW(),
        '<div dir="rtl">
           <h2>تانك توب نايكي دراي-فت ون لوكس</h2>
           <p>جزء من خط نايكي لوكس، هذا التانك توب يمنح شعورًا بالنعومة والسلاسة بشكل لا يصدق. مصنوع بتقنية Dri-FIT للحفاظ على جفافك أثناء التدريبات أو الأنشطة اليومية.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>نسيج نايكي لوكس:</strong> ناعم كالزبدة ومريح.</li>
             <li><strong>تقنية Dri-FIT:</strong> تمتص العرق.</li>
             <li><strong>قصة ضيقة:</strong> شعور مفصل دون تقييد.</li>
             <li><strong>تصميم ريسرباك:</strong> يسمح بالحركة الطبيعية.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> مزيج بوليستر/سباندكس</li>
             <li><strong>اللون:</strong> أبيض/فضي عاكس</li>
           </ul>
         </div>',
        'تانك توب نايكي دراي-فت ون لوكس', 'تانك توب نسائي بقصة قياسية نايكي دراي-فت ون لوكس',
        'تسوقي تانك توب نايكي دراي-فت ون لوكس الناعم للنساء. مثالي للتدريبات وما بعدها.',
        'نايكي, تانك توب, دراي-فت, نايكي لوكس, ملابس نسائية, ملابس رياضية',
        'تانك توب نسائي نايكي دراي-فت ون لوكس | ناعم ومريح', 'نسيج لوكس ناعم كالزبدة',
        'nike-dri-fit-one-luxe-tank-women-ar', 'ar', 25)
on conflict (description_id) do nothing;

-- Product 26: Zara Men's Trousers (Manuf: 2, Type: 1)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (26, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.5, null, 'REF-ZR-CL-TRS26', 26, 2,
        '65f023632bc46470c104b76f', 1)
on conflict (product_id) do nothing;

-- English Description (ID: 51)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (51, NOW(), NOW(),
        '<div>
           <h2>Zara Slim Fit Chino Trousers</h2>
           <p>Versatile chino trousers from Zara in a modern slim fit. Made from comfortable stretch cotton fabric, perfect for smart-casual looks.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Slim Fit:</strong> Contemporary and tailored silhouette.</li>
             <li><strong>Stretch Cotton Fabric:</strong> Provides comfort and ease of movement.</li>
             <li><strong>Chino Styling:</strong> Classic design with side and back pockets.</li>
             <li><strong>Zip Fly and Button Fastening:</strong> Secure closure.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> Cotton/Elastane Blend</li>
             <li><strong>Color:</strong> Navy Blue</li>
           </ul>
         </div>',
        'Zara Slim Fit Chinos', 'Zara Men''s Slim Fit Chino Trousers',
        'Shop versatile slim fit chino trousers from Zara for men. Comfortable stretch cotton.',
        'Zara, chinos, trousers, slim fit, men clothing, smart casual, stretch cotton',
        'Zara Men''s Slim Fit Chino Trousers | Smart Casual', 'Comfortable stretch cotton', 'zara-slim-fit-chinos-men',
        'en', 26)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 52)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (52, NOW(), NOW(),
        '<div dir="rtl">
           <h2>بنطال تشينو بقصة ضيقة من زارا</h2>
           <p>بنطال تشينو متعدد الاستخدامات من زارا بقصة ضيقة عصرية. مصنوع من نسيج قطني مطاطي مريح، مثالي لإطلالات الكاجوال الأنيقة.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>قصة ضيقة:</strong> صورة ظلية معاصرة ومفصلة.</li>
             <li><strong>نسيج قطني مطاطي:</strong> يوفر الراحة وسهولة الحركة.</li>
             <li><strong>ستايل تشينو:</strong> تصميم كلاسيكي بجيوب جانبية وخلفية.</li>
             <li><strong>سحاب وزر للإغلاق:</strong> إغلاق آمن.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> مزيج قطن/إيلاستين</li>
             <li><strong>اللون:</strong> كحلي</li>
           </ul>
         </div>',
        'بنطال تشينو ضيق زارا', 'بنطال تشينو رجالي بقصة ضيقة من زارا',
        'تسوق بنطال تشينو متعدد الاستخدامات بقصة ضيقة من زارا للرجال. قطن مطاطي مريح.',
        'زارا, تشينو, بنطال, قصة ضيقة, ملابس رجالية, كاجوال أنيق, قطن مطاطي',
        'بنطال تشينو رجالي بقصة ضيقة من زارا | كاجوال أنيق', 'قطن مطاطي مريح', 'zara-slim-fit-chinos-men-ar', 'ar', 26)
on conflict (description_id) do nothing;

-- Product 27: Adidas Kids' Tracksuit (Manuf: 3, Type: 1)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (27, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.6, null, 'REF-AD-CL-KTS27', 27, 3,
        '65f023632bc46470c104b76f', 1)
on conflict (product_id) do nothing;

-- English Description (ID: 53)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (53, NOW(), NOW(),
        '<div>
           <h2>Adidas Essentials Tracksuit (Kids)</h2>
           <p>A sporty classic for kids. This Adidas tracksuit includes a full-zip jacket and matching pants, both featuring the iconic 3-Stripes.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Full-Zip Jacket:</strong> Easy on and off.</li>
             <li><strong>Matching Pants:</strong> Elastic waist for comfort.</li>
             <li><strong>3-Stripes Detail:</strong> Signature Adidas branding.</li>
             <li><strong>Recycled Materials:</strong> Made in part with recycled content.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> Recycled Polyester Tricot</li>
             <li><strong>Color:</strong> Black/White</li>
             <li><strong>Fit:</strong> Regular Fit</li>
           </ul>
         </div>',
        'Adidas Essentials Tracksuit (Kids)', 'Adidas Kids'' Essentials 3-Stripes Tracksuit',
        'Shop the classic Adidas Essentials tracksuit for kids. Comfortable and sporty.',
        'Adidas, tracksuit, kids clothing, sportswear, Essentials, 3-Stripes',
        'Adidas Kids'' Essentials Tracksuit | Sportswear', 'Iconic 3-Stripes design',
        'adidas-essentials-tracksuit-kids', 'en', 27)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 54)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (54, NOW(), NOW(),
        '<div dir="rtl">
           <h2>بدلة رياضية أديداس إسنشالز (للأطفال)</h2>
           <p>قطعة رياضية كلاسيكية للأطفال. تتضمن بدلة أديداس الرياضية هذه جاكيت بسحاب كامل وبنطال مطابق، وكلاهما يتميز بخطوط 3 الأيقونية.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>جاكيت بسحاب كامل:</strong> سهولة الارتداء والخلع.</li>
             <li><strong>بنطال مطابق:</strong> خصر مطاطي للراحة.</li>
             <li><strong>تفاصيل 3 خطوط:</strong> علامة أديداس التجارية المميزة.</li>
             <li><strong>مواد معاد تدويرها:</strong> مصنوعة جزئيًا من محتوى معاد تدويره.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> تريكو بوليستر معاد تدويره</li>
             <li><strong>اللون:</strong> أسود/أبيض</li>
             <li><strong>المقاس:</strong> قصة عادية</li>
           </ul>
         </div>',
        'بدلة رياضية أديداس إسنشالز (أطفال)', 'بدلة رياضية للأطفال أديداس إسنشالز 3 خطوط',
        'تسوق بدلة رياضية أديداس إسنشالز الكلاسيكية للأطفال. مريحة ورياضية.',
        'أديداس, بدلة رياضية, ملابس أطفال, ملابس رياضية, إسنشالز, 3 خطوط',
        'بدلة رياضية للأطفال أديداس إسنشالز | ملابس رياضية', 'تصميم 3 خطوط أيقوني',
        'adidas-essentials-tracksuit-kids-ar', 'ar', 27)
on conflict (description_id) do nothing;

-- Product 28: H&M Scarf (Manuf: 4, Type: 3)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (28, NOW(), NOW(), true, NOW(), false, null,
        false, 180.0, true, false, 0.2, 70.0, 'REF-HM-AC-SCF28', 28, 4,
        '65f023632bc46470c104b76f', 3)
on conflict (product_id) do nothing;

-- English Description (ID: 55)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (55, NOW(), NOW(),
        '<div>
           <h2>H&M Large Scarf</h2>
           <p>A large, soft scarf from H&M in a woven fabric with fringe trim along the short sides. Perfect for adding warmth and style.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Large Size:</strong> Can be styled in multiple ways.</li>
             <li><strong>Soft Woven Fabric:</strong> Comfortable against the skin.</li>
             <li><strong>Fringe Trim:</strong> Adds a classic detail.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> Polyester/Viscose Blend (Example)</li>
             <li><strong>Color:</strong> Light Beige</li>
             <li><strong>Dimensions:</strong> Approx. 180cm x 70cm</li>
           </ul>
         </div>',
        'H&M Large Scarf', 'H&M Large Woven Scarf with Fringe',
        'Shop the large, soft woven scarf from H&M. A versatile accessory for added warmth and style.',
        'H&M, scarf, large scarf, woven scarf, accessories, fringe', 'H&M Large Woven Scarf | Accessories',
        'Soft woven fabric', 'hm-large-scarf', 'en', 28)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 56)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (56, NOW(), NOW(),
        '<div dir="rtl">
           <h2>وشاح كبير إتش آند إم</h2>
           <p>وشاح كبير وناعم من إتش آند إم من نسيج منسوج بحواف شراشيب على الجوانب القصيرة. مثالي لإضافة الدفء والأناقة.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>حجم كبير:</strong> يمكن تنسيقه بعدة طرق.</li>
             <li><strong>نسيج منسوج ناعم:</strong> مريح على البشرة.</li>
             <li><strong>حواف شراشيب:</strong> تضيف تفاصيل كلاسيكية.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> مزيج بوليستر/فيسكوز (مثال)</li>
             <li><strong>اللون:</strong> بيج فاتح</li>
             <li><strong>الأبعاد:</strong> حوالي 180 سم × 70 سم</li>
           </ul>
         </div>',
        'وشاح كبير إتش آند إم', 'وشاح كبير منسوج بحواف شراشيب من إتش آند إم',
        'تسوقي الوشاح الكبير الناعم المنسوج من إتش آند إم. إكسسوار متعدد الاستخدامات لمزيد من الدفء والأناقة.',
        'إتش آند إم, وشاح, وشاح كبير, وشاح منسوج, إكسسوارات, شراشيب', 'وشاح كبير منسوج إتش آند إم | إكسسوارات',
        'نسيج منسوج ناعم', 'hm-large-scarf-ar', 'ar', 28)
on conflict (description_id) do nothing;

-- Product 29: Gucci Belt Bag (Manuf: 5, Type: 4)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (29, NOW(), NOW(), true, NOW(), false, 14.0,
        false, 24.0, true, false, 0.5, 5.5, 'REF-GU-BG-BBG29', 29, 5,
        '65f023632bc46470c104b76f', 4)
on conflict (product_id) do nothing;

-- English Description (ID: 57)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (57, NOW(), NOW(),
        '<div>
           <h2>Gucci GG Marmont Matelassé Belt Bag</h2>
           <p>The GG Marmont belt bag from Gucci, crafted in matelassé chevron leather with the iconic Double G hardware. Can be worn as a belt bag or crossbody.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Matelassé Chevron Leather:</strong> Signature quilted design.</li>
             <li><strong>Double G Hardware:</strong> Antique gold-toned finish.</li>
             <li><strong>Adjustable Belt Strap:</strong> Allows for versatile wear.</li>
             <li><strong>Zip Closure:</strong> Secures belongings.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> Leather</li>
             <li><strong>Color:</strong> Black</li>
             <li><strong>Lining:</strong> Microfiber</li>
             <li><strong>Made in Italy</strong></li>
           </ul>
         </div>',
        'Gucci GG Marmont Belt Bag', 'Gucci GG Marmont Matelassé Leather Belt Bag',
        'Shop the versatile Gucci GG Marmont belt bag in black matelassé leather.',
        'Gucci, belt bag, bum bag, GG Marmont, matelasse, leather bag, luxury',
        'Gucci GG Marmont Belt Bag | Luxury Bags', 'Matelassé chevron leather', 'gucci-gg-marmont-belt-bag', 'en', 29)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 58)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (58, NOW(), NOW(),
        '<div dir="rtl">
           <h2>حقيبة حزام غوتشي جي جي مارمونت ماتيلاسيه</h2>
           <p>حقيبة حزام جي جي مارمونت من غوتشي، مصنوعة من جلد شيفرون مبطن ماتيلاسيه مع قطع معدنية Double G الأيقونية. يمكن ارتداؤها كحقيبة حزام أو كروس بودي.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>جلد شيفرون مبطن ماتيلاسيه:</strong> تصميم مبطن مميز.</li>
             <li><strong>قطع معدنية Double G:</strong> لمسة نهائية بلون ذهبي عتيق.</li>
             <li><strong>حزام قابل للتعديل:</strong> يسمح بارتداء متعدد الاستخدامات.</li>
             <li><strong>إغلاق بسحاب:</strong> يؤمن المقتنيات.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> جلد</li>
             <li><strong>اللون:</strong> أسود</li>
             <li><strong>البطانة:</strong> ألياف دقيقة</li>
             <li><strong>صنع في إيطاليا</strong></li>
           </ul>
         </div>',
        'حقيبة حزام غوتشي جي جي مارمونت', 'حقيبة حزام جلد ماتيلاسيه غوتشي جي جي مارمونت',
        'تسوقي حقيبة حزام غوتشي جي جي مارمونت متعددة الاستخدامات من الجلد المبطن الأسود.',
        'غوتشي, حقيبة حزام, حقيبة خصر, جي جي مارمونت, ماتيلاسيه, حقيبة جلد, فخامة',
        'حقيبة حزام غوتشي جي جي مارمونت | حقائب فاخرة', 'جلد شيفرون مبطن ماتيلاسيه', 'gucci-gg-marmont-belt-bag-ar',
        'ar', 29)
on conflict (description_id) do nothing;

-- Product 30: Chanel Sneakers (Manuf: 6, Type: 2)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (30, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.9, null, 'REF-CH-SH-SNK30', 30, 6,
        '65f023632bc46470c104b76f', 2)
on conflict (product_id) do nothing;

-- English Description (ID: 59)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (59, NOW(), NOW(),
        '<div>
           <h2>Chanel Trainers</h2>
           <p>Luxury trainers from Chanel, often featuring a mix of materials like suede, calfskin, and technical fabrics, detailed with the iconic CC logo.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Mix of Materials:</strong> Creates a unique and luxurious texture.</li>
             <li><strong>CC Logo Detail:</strong> Signature Chanel branding.</li>
             <li><strong>Comfortable Sole:</strong> Designed for style and wearability.</li>
             <li><strong>Lace-up Fastening:</strong> Classic sneaker closure.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Materials:</strong> Suede Calfskin, Nylon & Mixed Fibres (Example)</li>
             <li><strong>Color:</strong> White/Black/Grey (Example)</li>
             <li><strong>Sole:</strong> Rubber</li>
           </ul>
         </div>',
        'Chanel Trainers', 'Chanel Suede Calfskin & Nylon Trainers',
        'Shop luxury trainers from Chanel, combining premium materials and iconic design.',
        'Chanel, trainers, sneakers, luxury sneakers, women shoes, CC logo', 'Chanel Trainers | Luxury Footwear',
        'Mix of premium materials', 'chanel-trainers', 'en', 30)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 60)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (60, NOW(), NOW(),
        '<div dir="rtl">
           <h2>سنيكرز شانيل (ترينرز)</h2>
           <p>سنيكرز فاخر من شانيل، غالبًا ما يتميز بمزيج من المواد مثل جلد السويد وجلد العجل والأقمشة التقنية، مع تفاصيل شعار CC الأيقوني.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>مزيج من المواد:</strong> يخلق ملمسًا فريدًا وفاخرًا.</li>
             <li><strong>تفاصيل شعار CC:</strong> علامة شانيل التجارية المميزة.</li>
             <li><strong>نعل مريح:</strong> مصمم للأناقة وقابلية الارتداء.</li>
             <li><strong>إغلاق برباط:</strong> إغلاق سنيكرز كلاسيكي.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>المواد:</strong> جلد عجل سويد، نايلون وألياف مختلطة (مثال)</li>
             <li><strong>اللون:</strong> أبيض/أسود/رمادي (مثال)</li>
             <li><strong>النعل:</strong> مطاط</li>
           </ul>
         </div>',
        'سنيكرز شانيل (ترينرز)', 'سنيكرز شانيل جلد عجل سويد ونايلون',
        'تسوقي سنيكرز شانيل الفاخر، الذي يجمع بين المواد الفاخرة والتصميم الأيقوني.',
        'شانيل, ترينرز, سنيكرز, سنيكرز فاخر, أحذية نسائية, شعار CC', 'سنيكرز شانيل | أحذية فاخرة',
        'مزيج من المواد الفاخرة', 'chanel-trainers-ar', 'ar', 30)
on conflict (description_id) do nothing;
/*
Generated SQL inserts for products 31-45 based on the provided template and instructions:
- 15 products for catalog.product.
- 2 descriptions (en, ar) for each product in catalog.product_description.
- store_id = '65f023632bc46470c104b76f'.
- manufacturer_id from (1, 2, 3, 4, 5, 6).
- product_type_id from (1, 2, 3, 4).
- product_id sequential from 31 to 45.
- description_id sequential from 61 to 90.
- Realistic SKUs, Ref SKUs generated.
- Realistic fashion-related names, titles, meta descriptions, etc. generated.
- Valid HTML generated for the description field.
- Timestamps replaced with NOW().
- Domain is fashion.
*/

-- Product 31: Nike Men's Windrunner Jacket (Manuf: 1, Type: 1)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (31, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.5, null, 'REF-NK-CL-JKT31', 31, 1,
        '65f023632bc46470c104b76f', 1)
on conflict (product_id) do nothing;

-- English Description (ID: 61)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (61, NOW(), NOW(),
        '<div>
           <h2>Nike Sportswear Windrunner Jacket</h2>
           <p>The iconic Nike Windrunner jacket, updated with lightweight materials. Features the classic chevron design at the chest and a breathable mesh lining.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Lightweight Ripstop Fabric:</strong> Durable and comfortable.</li>
             <li><strong>Chevron Design:</strong> Iconic Windrunner style.</li>
             <li><strong>Mesh Lining:</strong> Enhances breathability.</li>
             <li><strong>Adjustable Hood:</strong> Provides custom coverage.</li>
             <li><strong>Zip Pockets:</strong> Secure storage.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> 100% Polyester</li>
             <li><strong>Color:</strong> Black/White</li>
             <li><strong>Fit:</strong> Standard Fit</li>
           </ul>
         </div>',
        'Nike Windrunner Jacket', 'Nike Men''s Sportswear Windrunner Hooded Jacket',
        'Shop the iconic Nike Windrunner jacket for men. Lightweight, durable, and breathable.',
        'Nike, Windrunner, jacket, men clothing, sportswear, hooded jacket',
        'Nike Men''s Windrunner Jacket | Sportswear', 'Iconic chevron design', 'nike-windrunner-jacket-men', 'en', 31)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 62)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (62, NOW(), NOW(),
        '<div dir="rtl">
           <h2>جاكيت نايكي سبورتسوير ويندرانر</h2>
           <p>جاكيت نايكي ويندرانر الأيقوني، تم تحديثه بمواد خفيفة الوزن. يتميز بتصميم شيفرون الكلاسيكي عند الصدر وبطانة شبكية تسمح بالتهوية.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>نسيج ريبستوب خفيف الوزن:</strong> متين ومريح.</li>
             <li><strong>تصميم شيفرون:</strong> ستايل ويندرانر أيقوني.</li>
             <li><strong>بطانة شبكية:</strong> تعزز التهوية.</li>
             <li><strong>غطاء رأس قابل للتعديل:</strong> يوفر تغطية مخصصة.</li>
             <li><strong>جيوب بسحاب:</strong> تخزين آمن.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> 100% بوليستر</li>
             <li><strong>اللون:</strong> أسود/أبيض</li>
             <li><strong>المقاس:</strong> قصة قياسية</li>
           </ul>
         </div>',
        'جاكيت نايكي ويندرانر', 'جاكيت رجالي بغطاء رأس نايكي سبورتسوير ويندرانر',
        'تسوق جاكيت نايكي ويندرانر الأيقوني للرجال. خفيف الوزن، متين، ويسمح بالتهوية.',
        'نايكي, ويندرانر, جاكيت, ملابس رجالية, ملابس رياضية, جاكيت بغطاء رأس',
        'جاكيت رجالي نايكي ويندرانر | ملابس رياضية', 'تصميم شيفرون أيقوني', 'nike-windrunner-jacket-men-ar', 'ar', 31)
on conflict (description_id) do nothing;

-- Product 32: Zara Women's Jeans (Manuf: 2, Type: 1)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (32, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.6, null, 'REF-ZR-CL-JNS32', 32, 2,
        '65f023632bc46470c104b76f', 1)
on conflict (product_id) do nothing;

-- English Description (ID: 63)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (63, NOW(), NOW(),
        '<div>
           <h2>Zara High-Waisted Skinny Jeans</h2>
           <p>Classic high-waisted skinny jeans from Zara made with stretch denim for a comfortable and flattering fit. Features a five-pocket design.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>High-Waisted Fit:</strong> Flattering and on-trend.</li>
             <li><strong>Skinny Leg:</strong> Creates a sleek silhouette.</li>
             <li><strong>Stretch Denim:</strong> Provides comfort and flexibility.</li>
             <li><strong>Five-Pocket Design:</strong> Classic jeans styling.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> Cotton/Polyester/Elastane Blend</li>
             <li><strong>Color:</strong> Dark Blue Wash</li>
             <li><strong>Closure:</strong> Zip fly and button</li>
           </ul>
         </div>',
        'Zara High-Waisted Skinny Jeans', 'Zara Women''s High-Waisted Skinny Jeans',
        'Shop classic high-waisted skinny jeans from Zara. Comfortable stretch denim for a flattering fit.',
        'Zara, jeans, skinny jeans, high-waisted jeans, women clothing, denim',
        'Zara Women''s High-Waisted Skinny Jeans | Denim', 'Comfortable stretch denim',
        'zara-high-waisted-skinny-jeans-women', 'en', 32)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 64)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (64, NOW(), NOW(),
        '<div dir="rtl">
           <h2>بنطال جينز ضيق بخصر مرتفع من زارا</h2>
           <p>بنطال جينز ضيق كلاسيكي بخصر مرتفع من زارا مصنوع من دنيم مطاطي لمقاس مريح وجذاب. يتميز بتصميم خمسة جيوب.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>قصة بخصر مرتفع:</strong> جذابة وعصرية.</li>
             <li><strong>ساق ضيقة:</strong> تخلق صورة ظلية أنيقة.</li>
             <li><strong>دنيم مطاطي:</strong> يوفر الراحة والمرونة.</li>
             <li><strong>تصميم خمسة جيوب:</strong> ستايل جينز كلاسيكي.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> مزيج قطن/بوليستر/إيلاستين</li>
             <li><strong>اللون:</strong> غسيل أزرق داكن</li>
             <li><strong>الإغلاق:</strong> سحاب وزر</li>
           </ul>
         </div>',
        'بنطال جينز ضيق بخصر مرتفع زارا', 'بنطال جينز نسائي ضيق بخصر مرتفع من زارا',
        'تسوقي بنطال جينز ضيق كلاسيكي بخصر مرتفع من زارا. دنيم مطاطي مريح لمقاس جذاب.',
        'زارا, جينز, جينز ضيق, جينز خصر مرتفع, ملابس نسائية, دنيم', 'بنطال جينز نسائي ضيق بخصر مرتفع من زارا | دنيم',
        'دنيم مطاطي مريح', 'zara-high-waisted-skinny-jeans-women-ar', 'ar', 32)
on conflict (description_id) do nothing;

-- Product 33: Adidas Cap (Manuf: 3, Type: 3)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (33, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.1, null, 'REF-AD-AC-CAP33', 33, 3,
        '65f023632bc46470c104b76f', 3)
on conflict (product_id) do nothing;

-- English Description (ID: 65)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (65, NOW(), NOW(),
        '<div>
           <h2>Adidas Baseball Cap</h2>
           <p>A classic six-panel baseball cap from Adidas made of comfortable cotton twill. Features an embroidered Trefoil logo and an adjustable back strap.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Cotton Twill Fabric:</strong> Comfortable and durable.</li>
             <li><strong>Six-Panel Construction:</strong> Classic baseball cap design.</li>
             <li><strong>Embroidered Trefoil Logo:</strong> Signature Adidas Originals branding.</li>
             <li><strong>Adjustable Back Strap:</strong> Provides a custom fit.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> 100% Cotton Twill</li>
             <li><strong>Color:</strong> Black/White</li>
             <li><strong>Size:</strong> One Size Fits Most (Adjustable)</li>
           </ul>
         </div>',
        'Adidas Baseball Cap', 'Adidas Originals Trefoil Baseball Cap',
        'Shop the classic Adidas Trefoil baseball cap. Comfortable cotton twill with adjustable strap.',
        'Adidas, cap, baseball cap, hat, accessories, Trefoil, Originals', 'Adidas Trefoil Baseball Cap | Accessories',
        'Embroidered Trefoil logo', 'adidas-trefoil-baseball-cap', 'en', 33)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 66)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (66, NOW(), NOW(),
        '<div dir="rtl">
           <h2>قبعة بيسبول أديداس</h2>
           <p>قبعة بيسبول كلاسيكية بستة ألواح من أديداس مصنوعة من نسيج قطني مريح. تتميز بشعار تريفويل مطرز وحزام خلفي قابل للتعديل.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>نسيج قطني تويل:</strong> مريح ومتين.</li>
             <li><strong>بناء من ستة ألواح:</strong> تصميم قبعة بيسبول كلاسيكي.</li>
             <li><strong>شعار تريفويل مطرز:</strong> علامة أديداس أوريجينالز التجارية المميزة.</li>
             <li><strong>حزام خلفي قابل للتعديل:</strong> يوفر مقاسًا مخصصًا.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> 100% قطن تويل</li>
             <li><strong>اللون:</strong> أسود/أبيض</li>
             <li><strong>المقاس:</strong> مقاس واحد يناسب معظم الأشخاص (قابل للتعديل)</li>
           </ul>
         </div>',
        'قبعة بيسبول أديداس', 'قبعة بيسبول أديداس أوريجينالز تريفويل',
        'تسوق قبعة بيسبول أديداس تريفويل الكلاسيكية. نسيج قطني مريح مع حزام قابل للتعديل.',
        'أديداس, قبعة, قبعة بيسبول, إكسسوارات, تريفويل, أوريجينالز', 'قبعة بيسبول أديداس تريفويل | إكسسوارات',
        'شعار تريفويل مطرز', 'adidas-trefoil-baseball-cap-ar', 'ar', 33)
on conflict (description_id) do nothing;

-- Product 34: H&M Kids' Rain Jacket (Manuf: 4, Type: 1)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (34, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.4, null, 'REF-HM-CL-KRJ34', 34, 4,
        '65f023632bc46470c104b76f', 1)
on conflict (product_id) do nothing;

-- English Description (ID: 67)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (67, NOW(), NOW(),
        '<div>
           <h2>H&M Waterproof Rain Jacket (Kids)</h2>
           <p>Keep kids dry on rainy days with this waterproof jacket from H&M. Features a detachable hood, zip front, and reflective details for visibility.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Waterproof Fabric:</strong> Sealed seams for protection against rain.</li>
             <li><strong>Detachable Hood:</strong> Offers versatility.</li>
             <li><strong>Zip Front with Chin Guard:</strong> Easy closure and comfort.</li>
             <li><strong>Reflective Details:</strong> Enhances visibility in low light.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> Polyester with Polyurethane coating</li>
             <li><strong>Color:</strong> Yellow</li>
             <li><strong>Waterproof Rating:</strong> (Specify if available, e.g., 10,000 mm)</li>
           </ul>
         </div>',
        'H&M Waterproof Rain Jacket (Kids)', 'H&M Kids'' Waterproof Rain Jacket with Detachable Hood',
        'Shop the H&M waterproof rain jacket for kids. Keeps them dry and visible on rainy days.',
        'H&M, rain jacket, waterproof jacket, kids clothing, outerwear', 'H&M Kids'' Waterproof Rain Jacket',
        'Waterproof with sealed seams', 'hm-waterproof-rain-jacket-kids', 'en', 34)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 68)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (68, NOW(), NOW(),
        '<div dir="rtl">
           <h2>جاكيت مطر مقاوم للماء إتش آند إم (للأطفال)</h2>
           <p>حافظ على جفاف الأطفال في الأيام الممطرة مع هذا الجاكيت المقاوم للماء من إتش آند إم. يتميز بغطاء رأس قابل للفصل وسحاب أمامي وتفاصيل عاكسة للرؤية.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>نسيج مقاوم للماء:</strong> درزات محكمة للحماية من المطر.</li>
             <li><strong>غطاء رأس قابل للفصل:</strong> يوفر تنوعًا.</li>
             <li><strong>سحاب أمامي مع واقي ذقن:</strong> إغلاق سهل وراحة.</li>
             <li><strong>تفاصيل عاكسة:</strong> تعزز الرؤية في الإضاءة المنخفضة.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> بوليستر مع طلاء بولي يوريثان</li>
             <li><strong>اللون:</strong> أصفر</li>
             <li><strong>تصنيف مقاومة الماء:</strong> (حدد إذا كان متاحًا، مثل 10,000 مم)</li>
           </ul>
         </div>',
        'جاكيت مطر مقاوم للماء إتش آند إم (أطفال)', 'جاكيت مطر مقاوم للماء بغطاء رأس قابل للفصل للأطفال من إتش آند إم',
        'تسوق جاكيت المطر المقاوم للماء من إتش آند إم للأطفال. يبقيهم جافين ومرئيين في الأيام الممطرة.',
        'إتش آند إم, جاكيت مطر, جاكيت مقاوم للماء, ملابس أطفال, ملابس خارجية',
        'جاكيت مطر مقاوم للماء للأطفال إتش آند إم', 'مقاوم للماء بدرزات محكمة', 'hm-waterproof-rain-jacket-kids-ar',
        'ar', 34)
on conflict (description_id) do nothing;

-- Product 35: Gucci Scarf (Manuf: 5, Type: 3)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (35, NOW(), NOW(), true, NOW(), false, null,
        false, 140.0, true, false, 0.2, 140.0, 'REF-GU-AC-SCF35', 35, 5,
        '65f023632bc46470c104b76f', 3)
on conflict (product_id) do nothing;

-- English Description (ID: 69)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (69, NOW(), NOW(),
        '<div>
           <h2>Gucci GG Jacquard Wool Silk Scarf</h2>
           <p>A luxurious scarf crafted from a wool silk blend, featuring the iconic GG jacquard pattern and finished with fringed edges. Made in Italy.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>GG Jacquard Pattern:</strong> Signature Gucci motif.</li>
             <li><strong>Wool Silk Blend:</strong> Soft, lightweight, and warm.</li>
             <li><strong>Fringed Edges:</strong> Classic scarf detailing.</li>
             <li><strong>Large Size:</strong> Allows for versatile styling.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> 80% Wool, 20% Silk</li>
             <li><strong>Color:</strong> Beige/Brown GG Pattern</li>
             <li><strong>Dimensions:</strong> Approx. 140cm x 140cm</li>
             <li><strong>Made in Italy</strong></li>
           </ul>
         </div>',
        'Gucci GG Wool Silk Scarf', 'Gucci GG Jacquard Wool Silk Scarf',
        'Wrap yourself in luxury with the Gucci GG jacquard scarf in a wool silk blend.',
        'Gucci, scarf, wool scarf, silk scarf, GG pattern, jacquard, accessories, luxury',
        'Gucci GG Jacquard Wool Silk Scarf | Luxury Accessories', 'Iconic GG jacquard pattern',
        'gucci-gg-wool-silk-scarf', 'en', 35)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 70)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (70, NOW(), NOW(),
        '<div dir="rtl">
           <h2>وشاح غوتشي GG جاكار صوف وحرير</h2>
           <p>وشاح فاخر مصنوع من مزيج الصوف والحرير، يتميز بنمط جاكار GG الأيقوني وينتهي بحواف شراشيب. صنع في إيطاليا.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>نمط جاكار GG:</strong> شعار غوتشي المميز.</li>
             <li><strong>مزيج صوف وحرير:</strong> ناعم وخفيف الوزن ودافئ.</li>
             <li><strong>حواف شراشيب:</strong> تفاصيل وشاح كلاسيكية.</li>
             <li><strong>حجم كبير:</strong> يسمح بتنسيق متعدد الاستخدامات.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> 80% صوف، 20% حرير</li>
             <li><strong>اللون:</strong> نمط GG بيج/بني</li>
             <li><strong>الأبعاد:</strong> حوالي 140 سم × 140 سم</li>
             <li><strong>صنع في إيطاليا</strong></li>
           </ul>
         </div>',
        'وشاح غوتشي GG صوف وحرير', 'وشاح غوتشي GG جاكار صوف وحرير',
        'التفي بالفخامة مع وشاح غوتشي GG جاكار بمزيج الصوف والحرير.',
        'غوتشي, وشاح, وشاح صوف, وشاح حرير, نمط GG, جاكار, إكسسوارات, فخامة',
        'وشاح غوتشي GG جاكار صوف وحرير | إكسسوارات فاخرة', 'نمط جاكار GG الأيقوني', 'gucci-gg-wool-silk-scarf-ar', 'ar',
        35)
on conflict (description_id) do nothing;

-- Product 36: Chanel Wallet on Chain (WOC) (Manuf: 6, Type: 4)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (36, NOW(), NOW(), true, NOW(), false, 12.3,
        false, 19.2, true, false, 0.4, 3.5, 'REF-CH-BG-WOC36', 36, 6,
        '65f023632bc46470c104b76f', 4)
on conflict (product_id) do nothing;

-- English Description (ID: 71)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (71, NOW(), NOW(),
        '<div>
           <h2>Chanel Classic Wallet on Chain (WOC)</h2>
           <p>The highly coveted Wallet on Chain (WOC) from Chanel in classic quilted leather. A versatile piece that functions as a wallet, clutch, or crossbody bag.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Quilted Leather:</strong> Iconic Chanel quilting (Caviar or Lambskin).</li>
             <li><strong>CC Logo Snap Closure:</strong> Secure and signature detail.</li>
             <li><strong>Interlaced Chain Strap:</strong> Long chain for crossbody or shoulder wear.</li>
             <li><strong>Organized Interior:</strong> Card slots, zip pocket, and compartments.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> Caviar Leather (Example)</li>
             <li><strong>Color:</strong> Black with Gold-Tone Hardware (Example)</li>
             <li><strong>Dimensions:</strong> Approx. 19.2cm x 12.3cm x 3.5cm</li>
           </ul>
         </div>',
        'Chanel Wallet on Chain (WOC)', 'Chanel Classic Quilted Wallet on Chain',
        'Shop the iconic and versatile Chanel Wallet on Chain (WOC) in quilted leather.',
        'Chanel, WOC, Wallet on Chain, crossbody bag, clutch, luxury bag, quilted leather',
        'Chanel Classic Wallet on Chain (WOC) | Luxury Bags', 'Versatile quilted leather WOC',
        'chanel-wallet-on-chain-woc', 'en', 36)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 72)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (72, NOW(), NOW(),
        '<div dir="rtl">
           <h2>محفظة شانيل الكلاسيكية على سلسلة (WOC)</h2>
           <p>المحفظة المرغوبة للغاية على سلسلة (WOC) من شانيل من الجلد الكلاسيكي المبطن. قطعة متعددة الاستخدامات تعمل كمحفظة أو كلاتش أو حقيبة كروس بودي.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>جلد مبطن:</strong> تبطين شانيل الأيقوني (كافيار أو جلد حمل).</li>
             <li><strong>إغلاق بكبسون شعار CC:</strong> تفاصيل آمنة ومميزة.</li>
             <li><strong>حزام سلسلة متشابك:</strong> سلسلة طويلة للارتداء كروس بودي أو على الكتف.</li>
             <li><strong>تصميم داخلي منظم:</strong> فتحات بطاقات وجيب بسحاب ومقصورات.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> جلد كافيار (مثال)</li>
             <li><strong>اللون:</strong> أسود مع قطع معدنية ذهبية اللون (مثال)</li>
             <li><strong>الأبعاد:</strong> حوالي 19.2 سم × 12.3 سم × 3.5 سم</li>
           </ul>
         </div>',
        'محفظة شانيل على سلسلة (WOC)', 'محفظة شانيل الكلاسيكية المبطنة على سلسلة',
        'تسوقي محفظة شانيل الأيقونية والمتعددة الاستخدامات على سلسلة (WOC) من الجلد المبطن.',
        'شانيل, WOC, محفظة على سلسلة, حقيبة كروس, كلاتش, حقيبة فاخرة, جلد مبطن',
        'محفظة شانيل الكلاسيكية على سلسلة (WOC) | حقائب فاخرة', 'WOC جلد مبطن متعدد الاستخدامات',
        'chanel-wallet-on-chain-woc-ar', 'ar', 36)
on conflict (description_id) do nothing;

-- Product 37: Nike Women's Running Shorts (Manuf: 1, Type: 1)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (37, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.2, null, 'REF-NK-CL-WRS37', 37, 1,
        '65f023632bc46470c104b76f', 1)
on conflict (product_id) do nothing;

-- English Description (ID: 73)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (73, NOW(), NOW(),
        '<div>
           <h2>Nike Tempo Women''s Running Shorts</h2>
           <p>An iconic design for everyday runs. The Nike Tempo Shorts deliver lightweight, smooth comfort with Dri-FIT technology and convenient storage.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Dri-FIT Technology:</strong> Helps keep you dry and comfortable.</li>
             <li><strong>Lightweight Woven Fabric:</strong> Smooth and comfortable feel.</li>
             <li><strong>Built-in Briefs:</strong> Offer supportive comfort.</li>
             <li><strong>Internal Pocket:</strong> Stores small items like keys or cards.</li>
             <li><strong>Curved Hem:</strong> Allows for natural range of motion.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> 100% Recycled Polyester</li>
             <li><strong>Color:</strong> Black/White</li>
             <li><strong>Inseam:</strong> 3" (approx.)</li>
           </ul>
         </div>',
        'Nike Tempo Running Shorts (Women)', 'Nike Women''s Tempo Dri-FIT Running Shorts',
        'Shop the iconic Nike Tempo running shorts for women. Lightweight comfort with Dri-FIT technology.',
        'Nike, Tempo shorts, running shorts, women clothing, sportswear, Dri-FIT',
        'Nike Women''s Tempo Running Shorts | Sportswear', 'Lightweight Dri-FIT comfort',
        'nike-tempo-running-shorts-women', 'en', 37)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 74)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (74, NOW(), NOW(),
        '<div dir="rtl">
           <h2>شورت الجري نايكي تيمبو للنساء</h2>
           <p>تصميم أيقوني للجري اليومي. يوفر شورت نايكي تيمبو راحة خفيفة الوزن وسلسة مع تقنية Dri-FIT وتخزين مريح.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>تقنية Dri-FIT:</strong> تساعد في الحفاظ على جفافك وراحتك.</li>
             <li><strong>نسيج منسوج خفيف الوزن:</strong> شعور ناعم ومريح.</li>
             <li><strong>سروال داخلي مدمج:</strong> يوفر راحة داعمة.</li>
             <li><strong>جيب داخلي:</strong> يخزن الأشياء الصغيرة مثل المفاتيح أو البطاقات.</li>
             <li><strong>حافة منحنية:</strong> تسمح بنطاق حركة طبيعي.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> 100% بوليستر معاد تدويره</li>
             <li><strong>اللون:</strong> أسود/أبيض</li>
             <li><strong>طول الدرزة الداخلية:</strong> 3 بوصات (تقريبًا)</li>
           </ul>
         </div>',
        'شورت الجري نايكي تيمبو (نساء)', 'شورت جري نسائي نايكي تيمبو بتقنية Dri-FIT',
        'تسوقي شورت الجري الأيقوني نايكي تيمبو للنساء. راحة خفيفة الوزن مع تقنية Dri-FIT.',
        'نايكي, شورت تيمبو, شورت جري, ملابس نسائية, ملابس رياضية, Dri-FIT', 'شورت جري نسائي نايكي تيمبو | ملابس رياضية',
        'راحة Dri-FIT خفيفة الوزن', 'nike-tempo-running-shorts-women-ar', 'ar', 37)
on conflict (description_id) do nothing;

-- Product 38: Zara Kids' Sweater (Manuf: 2, Type: 1)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (38, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.3, null, 'REF-ZR-CL-KSW38', 38, 2,
        '65f023632bc46470c104b76f', 1)
on conflict (product_id) do nothing;

-- English Description (ID: 75)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (75, NOW(), NOW(),
        '<div>
           <h2>Zara Basic Knit Sweater (Kids)</h2>
           <p>A simple and soft knit sweater for kids from Zara. Features a round neckline, long sleeves, and ribbed trim. Perfect for layering.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Soft Knit Fabric:</strong> Comfortable for everyday wear.</li>
             <li><strong>Round Neckline:</strong> Classic sweater style.</li>
             <li><strong>Long Sleeves:</strong> Provides warmth.</li>
             <li><strong>Ribbed Trim:</strong> At neckline, cuffs, and hem.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> Cotton/Acrylic Blend (Example)</li>
             <li><strong>Color:</strong> Navy Blue</li>
             <li><strong>Fit:</strong> Regular Fit</li>
           </ul>
         </div>',
        'Zara Basic Knit Sweater (Kids)', 'Zara Kids'' Basic Knit Sweater',
        'Shop the soft basic knit sweater for kids from Zara. Perfect for layering.',
        'Zara, sweater, knitwear, kids clothing, basic sweater', 'Zara Kids'' Basic Knit Sweater | Everyday Wear',
        'Soft knit fabric', 'zara-basic-knit-sweater-kids', 'en', 38)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 76)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (76, NOW(), NOW(),
        '<div dir="rtl">
           <h2>سترة أساسية محبوكة من زارا (للأطفال)</h2>
           <p>سترة محبوكة بسيطة وناعمة للأطفال من زارا. تتميز برقبة دائرية وأكمام طويلة وحواف مضلعة. مثالية للارتداء كطبقات.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>نسيج محبوك ناعم:</strong> مريح للارتداء اليومي.</li>
             <li><strong>رقبة دائرية:</strong> ستايل سترة كلاسيكي.</li>
             <li><strong>أكمام طويلة:</strong> توفر الدفء.</li>
             <li><strong>حواف مضلعة:</strong> عند خط العنق والأساور والحافة.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> مزيج قطن/أكريليك (مثال)</li>
             <li><strong>اللون:</strong> كحلي</li>
             <li><strong>المقاس:</strong> قصة عادية</li>
           </ul>
         </div>',
        'سترة أساسية محبوكة زارا (أطفال)', 'سترة أساسية محبوكة للأطفال من زارا',
        'تسوق السترة الأساسية المحبوكة الناعمة للأطفال من زارا. مثالية للارتداء كطبقات.',
        'زارا, سترة, ملابس محبوكة, ملابس أطفال, سترة أساسية', 'سترة أساسية محبوكة للأطفال من زارا | ملابس يومية',
        'نسيج محبوك ناعم', 'zara-basic-knit-sweater-kids-ar', 'ar', 38)
on conflict (description_id) do nothing;

-- Product 39: Adidas Socks (3-Pack) (Manuf: 3, Type: 3)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (39, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.15, null, 'REF-AD-AC-SCK39', 39, 3,
        '65f023632bc46470c104b76f', 3)
on conflict (product_id) do nothing;

-- English Description (ID: 77)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (77, NOW(), NOW(),
        '<div>
           <h2>Adidas Cushioned Crew Socks (3 Pairs)</h2>
           <p>Stock up on comfortable socks with this three-pack from Adidas. Features cushioning in the footbed and arch support for all-day comfort.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Cushioned Footbed:</strong> Provides comfort and shock absorption.</li>
             <li><strong>Arch Support:</strong> Offers a snug, supportive fit.</li>
             <li><strong>Cotton Blend Fabric:</strong> Soft and breathable.</li>
             <li><strong>Crew Length:</strong> Classic sock height.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> Cotton/Polyester/Elastane Blend</li>
             <li><strong>Pack Size:</strong> 3 Pairs</li>
             <li><strong>Color:</strong> White/Black</li>
           </ul>
         </div>',
        'Adidas Cushioned Crew Socks (3-Pack)', 'Adidas Cushioned Crew Socks 3 Pairs',
        'Shop the comfortable Adidas cushioned crew socks in a convenient 3-pack.',
        'Adidas, socks, crew socks, cushioned socks, accessories, 3-pack',
        'Adidas Cushioned Crew Socks 3-Pack | Accessories', 'Cushioned footbed for comfort',
        'adidas-cushioned-crew-socks-3pack', 'en', 39)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 78)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (78, NOW(), NOW(),
        '<div dir="rtl">
           <h2>جوارب أديداس مبطنة بطول الطاقم (3 أزواج)</h2>
           <p>احصل على جوارب مريحة مع هذه العبوة المكونة من ثلاثة أزواج من أديداس. تتميز بتوسيد في وسادة القدم ودعم قوس القدم لراحة طوال اليوم.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>وسادة قدم مبطنة:</strong> توفر الراحة وامتصاص الصدمات.</li>
             <li><strong>دعم قوس القدم:</strong> يوفر مقاسًا محكمًا وداعمًا.</li>
             <li><strong>نسيج مزيج قطني:</strong> ناعم ويسمح بالتهوية.</li>
             <li><strong>طول الطاقم:</strong> ارتفاع جورب كلاسيكي.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> مزيج قطن/بوليستر/إيلاستين</li>
             <li><strong>حجم العبوة:</strong> 3 أزواج</li>
             <li><strong>اللون:</strong> أبيض/أسود</li>
           </ul>
         </div>',
        'جوارب أديداس مبطنة (3 أزواج)', 'جوارب أديداس مبطنة بطول الطاقم 3 أزواج',
        'تسوق جوارب أديداس المبطنة المريحة في عبوة مناسبة من 3 أزواج.',
        'أديداس, جوارب, جوارب طاقم, جوارب مبطنة, إكسسوارات, عبوة 3', 'عبوة 3 أزواج جوارب أديداس مبطنة | إكسسوارات',
        'وسادة قدم مبطنة للراحة', 'adidas-cushioned-crew-socks-3pack-ar', 'ar', 39)
on conflict (description_id) do nothing;

-- Product 40: H&M Men's Swim Shorts (Manuf: 4, Type: 1)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (40, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.2, null, 'REF-HM-CL-SWM40', 40, 4,
        '65f023632bc46470c104b76f', 1)
on conflict (product_id) do nothing;

-- English Description (ID: 79)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (79, NOW(), NOW(),
        '<div>
           <h2>H&M Swim Shorts</h2>
           <p>Short swim shorts from H&M in a woven fabric. Feature an elasticized drawstring waistband, side pockets, and a mesh liner.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Quick-Drying Fabric:</strong> Comfortable in and out of the water.</li>
             <li><strong>Elasticized Drawstring Waistband:</strong> Secure and adjustable fit.</li>
             <li><strong>Side Pockets:</strong> Convenient storage.</li>
             <li><strong>Mesh Liner:</strong> Provides support and comfort.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> Recycled Polyester</li>
             <li><strong>Color:</strong> Bright Blue</li>
             <li><strong>Length:</strong> Short</li>
           </ul>
         </div>',
        'H&M Swim Shorts', 'H&M Men''s Short Swim Shorts',
        'Shop quick-drying swim shorts for men from H&M. Perfect for the beach or pool.',
        'H&M, swim shorts, swimwear, men clothing, beachwear', 'H&M Men''s Swim Shorts | Swimwear',
        'Quick-drying fabric', 'hm-swim-shorts-men', 'en', 40)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 80)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (80, NOW(), NOW(),
        '<div dir="rtl">
           <h2>شورت سباحة إتش آند إم</h2>
           <p>شورت سباحة قصير من إتش آند إم من نسيج منسوج. يتميز بحزام خصر مطاطي برباط وجيوب جانبية وبطانة شبكية.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>نسيج سريع الجفاف:</strong> مريح داخل وخارج الماء.</li>
             <li><strong>حزام خصر مطاطي برباط:</strong> مقاس آمن وقابل للتعديل.</li>
             <li><strong>جيوب جانبية:</strong> تخزين مريح.</li>
             <li><strong>بطانة شبكية:</strong> توفر الدعم والراحة.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> بوليستر معاد تدويره</li>
             <li><strong>اللون:</strong> أزرق ساطع</li>
             <li><strong>الطول:</strong> قصير</li>
           </ul>
         </div>',
        'شورت سباحة إتش آند إم', 'شورت سباحة قصير رجالي من إتش آند إم',
        'تسوق شورت سباحة سريع الجفاف للرجال من إتش آند إم. مثالي للشاطئ أو المسبح.',
        'إتش آند إم, شورت سباحة, ملابس سباحة, ملابس رجالية, ملابس شاطئ', 'شورت سباحة رجالي إتش آند إم | ملابس سباحة',
        'نسيج سريع الجفاف', 'hm-swim-shorts-men-ar', 'ar', 40)
on conflict (description_id) do nothing;

-- Product 41: Gucci Horsebit Loafers (Manuf: 5, Type: 2)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (41, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.8, null, 'REF-GU-SH-HBL41', 41, 5,
        '65f023632bc46470c104b76f', 2)
on conflict (product_id) do nothing;

-- English Description (ID: 81)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (81, NOW(), NOW(),
        '<div>
           <h2>Gucci 1953 Horsebit Leather Loafer</h2>
           <p>A cornerstone of the Gucci collection, the 1953 Horsebit loafer in leather connects the House''s past and present. A timeless design.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Original 1953 Design:</strong> Iconic and historic silhouette.</li>
             <li><strong>Horsebit Detail:</strong> Signature gold-toned hardware.</li>
             <li><strong>Premium Leather:</strong> Crafted for durability and style.</li>
             <li><strong>Leather Sole:</strong> Classic construction.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> Leather</li>
             <li><strong>Color:</strong> Black</li>
             <li><strong>Heel Height:</strong> 15mm</li>
             <li><strong>Made in Italy</strong></li>
           </ul>
         </div>',
        'Gucci 1953 Horsebit Loafer', 'Gucci Men''s 1953 Horsebit Leather Loafer',
        'Shop the timeless Gucci 1953 Horsebit leather loafer for men. An iconic design.',
        'Gucci, loafers, Horsebit, 1953 loafer, men shoes, leather shoes, luxury',
        'Gucci Men''s 1953 Horsebit Loafer | Luxury Footwear', 'Iconic 1953 Horsebit design',
        'gucci-1953-horsebit-loafer-men', 'en', 41)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 82)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (82, NOW(), NOW(),
        '<div dir="rtl">
           <h2>حذاء لوفر غوتشي 1953 هورسبت الجلدي</h2>
           <p>حجر الزاوية في مجموعة غوتشي، حذاء لوفر 1953 هورسبت الجلدي يربط ماضي الدار وحاضرها. تصميم خالد.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>تصميم 1953 الأصلي:</strong> صورة ظلية أيقونية وتاريخية.</li>
             <li><strong>تفاصيل هورسبت:</strong> قطع معدنية مميزة بلون ذهبي.</li>
             <li><strong>جلد فاخر:</strong> مصنوع للمتانة والأناقة.</li>
             <li><strong>نعل جلدي:</strong> بناء كلاسيكي.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> جلد</li>
             <li><strong>اللون:</strong> أسود</li>
             <li><strong>ارتفاع الكعب:</strong> 15 مم</li>
             <li><strong>صنع في إيطاليا</strong></li>
           </ul>
         </div>',
        'لوفر غوتشي 1953 هورسبت', 'حذاء لوفر رجالي غوتشي 1953 هورسبت جلدي',
        'تسوق حذاء لوفر غوتشي 1953 هورسبت الجلدي الخالد للرجال. تصميم أيقوني.',
        'غوتشي, لوفر, هورسبت, لوفر 1953, أحذية رجالية, حذاء جلد, فخامة',
        'حذاء لوفر رجالي غوتشي 1953 هورسبت | أحذية فاخرة', 'تصميم هورسبت 1953 الأيقوني',
        'gucci-1953-horsebit-loafer-men-ar', 'ar', 41)
on conflict (description_id) do nothing;

-- Product 42: Chanel Earrings (Manuf: 6, Type: 3)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (42, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.05, null, 'REF-CH-AC-EAR42', 42, 6,
        '65f023632bc46470c104b76f', 3)
on conflict (product_id) do nothing;

-- English Description (ID: 83)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (83, NOW(), NOW(),
        '<div>
           <h2>Chanel CC Stud Earrings</h2>
           <p>Classic and elegant stud earrings from Chanel featuring the signature CC logo, often crafted in metal with crystal or pearl embellishments.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>CC Logo Design:</strong> Iconic Chanel branding.</li>
             <li><strong>Stud Style:</strong> Simple and elegant for everyday wear.</li>
             <li><strong>Premium Materials:</strong> Metal, Strass, Pearls (Varies by model).</li>
             <li><strong>Post Back Closure:</strong> Secure fastening.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> Metal & Glass Pearls (Example)</li>
             <li><strong>Color:</strong> Gold-Tone & White Pearl (Example)</li>
           </ul>
         </div>',
        'Chanel CC Stud Earrings', 'Chanel Metal & Pearl CC Stud Earrings',
        'Shop the classic Chanel CC stud earrings. An elegant addition to any jewelry collection.',
        'Chanel, earrings, stud earrings, CC logo, pearl earrings, accessories, luxury',
        'Chanel CC Stud Earrings | Luxury Jewelry', 'Signature CC logo studs', 'chanel-cc-stud-earrings', 'en', 42)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 84)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (84, NOW(), NOW(),
        '<div dir="rtl">
           <h2>أقراط شانيل CC دبوس</h2>
           <p>أقراط دبوس كلاسيكية وأنيقة من شانيل تتميز بشعار CC المميز، غالبًا ما تكون مصنوعة من المعدن مع زخارف من الكريستال أو اللؤلؤ.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>تصميم شعار CC:</strong> علامة شانيل التجارية الأيقونية.</li>
             <li><strong>ستايل دبوس:</strong> بسيط وأنيق للارتداء اليومي.</li>
             <li><strong>مواد فاخرة:</strong> معدن، ستراس، لؤلؤ (يختلف حسب الموديل).</li>
             <li><strong>إغلاق خلفي بدبوس:</strong> إغلاق آمن.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> معدن ولؤلؤ زجاجي (مثال)</li>
             <li><strong>اللون:</strong> لون ذهبي ولؤلؤ أبيض (مثال)</li>
           </ul>
         </div>',
        'أقراط شانيل CC دبوس', 'أقراط شانيل معدن ولؤلؤ CC دبوس',
        'تسوقي أقراط شانيل CC الدبوس الكلاسيكية. إضافة أنيقة لأي مجموعة مجوهرات.',
        'شانيل, أقراط, أقراط دبوس, شعار CC, أقراط لؤلؤ, إكسسوارات, فخامة', 'أقراط شانيل CC دبوس | مجوهرات فاخرة',
        'أقراط دبوس بشعار CC المميز', 'chanel-cc-stud-earrings-ar', 'ar', 42)
on conflict (description_id) do nothing;

-- Product 43: Nike Duffel Bag (Manuf: 1, Type: 4)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (43, NOW(), NOW(), true, NOW(), false, 28.0,
        false, 55.0, true, false, 0.6, 25.0, 'REF-NK-BG-DUF43', 43, 1,
        '65f023632bc46470c104b76f', 4)
on conflict (product_id) do nothing;

-- English Description (ID: 85)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (85, NOW(), NOW(),
        '<div>
           <h2>Nike Brasilia Duffel Bag (Medium)</h2>
           <p>The Nike Brasilia Duffel Bag has a spacious main compartment and specialized pockets to get you through your day from gym to office and home. Handles and a shoulder strap offer carrying options.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Spacious Main Compartment:</strong> Zips shut for secure storage.</li>
             <li><strong>Shoe Compartment:</strong> Stores footwear separately.</li>
             <li><strong>Multiple Pockets:</strong> Internal and external pockets for organization.</li>
             <li><strong>Coated Bottom:</strong> Helps shield your things from bumps, scrapes and spills.</li>
             <li><strong>Carrying Options:</strong> Dual handles and removable shoulder strap.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> 100% Polyester</li>
             <li><strong>Color:</strong> Black/Black/White</li>
             <li><strong>Dimensions:</strong> Approx. 55cm L x 28cm W x 28cm H</li>
             <li><strong>Volume:</strong> 60L (Medium size)</li>
           </ul>
         </div>',
        'Nike Brasilia Duffel Bag (Medium)', 'Nike Brasilia 9.5 Training Duffel Bag (Medium, 60L)',
        'Shop the durable Nike Brasilia medium duffel bag. Spacious storage for gym or travel.',
        'Nike, duffel bag, gym bag, sports bag, travel bag, Brasilia',
        'Nike Brasilia Medium Duffel Bag | Sports & Travel', 'Separate shoe compartment',
        'nike-brasilia-duffel-bag-medium', 'en', 43)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 86)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (86, NOW(), NOW(),
        '<div dir="rtl">
           <h2>حقيبة دفل نايكي برازيليا (متوسطة)</h2>
           <p>تحتوي حقيبة دفل نايكي برازيليا على مقصورة رئيسية واسعة وجيوب متخصصة لتنقلك خلال يومك من الصالة الرياضية إلى المكتب والمنزل. توفر المقابض وحزام الكتف خيارات للحمل.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>مقصورة رئيسية واسعة:</strong> تغلق بسحاب لتخزين آمن.</li>
             <li><strong>مقصورة أحذية:</strong> تخزن الأحذية بشكل منفصل.</li>
             <li><strong>جيوب متعددة:</strong> جيوب داخلية وخارجية للتنظيم.</li>
             <li><strong>قاعدة مطلية:</strong> تساعد في حماية أغراضك من الصدمات والخدوش والانسكابات.</li>
             <li><strong>خيارات الحمل:</strong> مقابض مزدوجة وحزام كتف قابل للإزالة.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> 100% بوليستر</li>
             <li><strong>اللون:</strong> أسود/أسود/أبيض</li>
             <li><strong>الأبعاد:</strong> حوالي 55 سم طول × 28 سم عرض × 28 سم ارتفاع</li>
             <li><strong>الحجم:</strong> 60 لتر (حجم متوسط)</li>
           </ul>
         </div>',
        'حقيبة دفل نايكي برازيليا (متوسطة)', 'حقيبة تدريب دفل نايكي برازيليا 9.5 (متوسطة، 60 لتر)',
        'تسوق حقيبة دفل نايكي برازيليا المتوسطة المتينة. تخزين واسع للصالة الرياضية أو السفر.',
        'نايكي, حقيبة دفل, حقيبة رياضية, حقيبة سفر, برازيليا', 'حقيبة دفل نايكي برازيليا متوسطة | رياضة وسفر',
        'مقصورة أحذية منفصلة', 'nike-brasilia-duffel-bag-medium-ar', 'ar', 43)
on conflict (description_id) do nothing;

-- Product 44: Zara Women's Blouse (Manuf: 2, Type: 1)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (44, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.3, null, 'REF-ZR-CL-BLS44', 44, 2,
        '65f023632bc46470c104b76f', 1)
on conflict (product_id) do nothing;

-- English Description (ID: 87)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (87, NOW(), NOW(),
        '<div>
           <h2>Zara Poplin Shirt</h2>
           <p>A crisp poplin shirt from Zara featuring a classic collar, long sleeves with buttoned cuffs, and a button-up front. A versatile wardrobe essential.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Cotton Poplin Fabric:</strong> Crisp, smooth, and breathable.</li>
             <li><strong>Classic Collar:</strong> Traditional shirt styling.</li>
             <li><strong>Button-Up Front:</strong> Easy closure.</li>
             <li><strong>Regular Fit:</strong> Comfortable and timeless.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> 100% Cotton</li>
             <li><strong>Color:</strong> White</li>
             <li><strong>Care:</strong> Machine wash</li>
           </ul>
         </div>',
        'Zara Poplin Shirt', 'Zara Women''s Cotton Poplin Shirt',
        'Shop the crisp cotton poplin shirt from Zara for women. A versatile wardrobe essential.',
        'Zara, shirt, poplin shirt, blouse, women clothing, cotton shirt, basic',
        'Zara Women''s Poplin Shirt | Wardrobe Essential', 'Crisp cotton poplin', 'zara-poplin-shirt-women', 'en', 44)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 88)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (88, NOW(), NOW(),
        '<div dir="rtl">
           <h2>قميص بوبلين من زارا</h2>
           <p>قميص بوبلين أنيق من زارا يتميز بياقة كلاسيكية وأكمام طويلة بأساور بأزرار وجزء أمامي بأزرار. قطعة أساسية متعددة الاستخدامات في خزانة الملابس.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>نسيج قطن بوبلين:</strong> أنيق وناعم ويسمح بالتهوية.</li>
             <li><strong>ياقة كلاسيكية:</strong> ستايل قميص تقليدي.</li>
             <li><strong>جزء أمامي بأزرار:</strong> إغلاق سهل.</li>
             <li><strong>قصة عادية:</strong> مريحة وخالدة.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> 100% قطن</li>
             <li><strong>اللون:</strong> أبيض</li>
             <li><strong>العناية:</strong> غسيل في الغسالة</li>
           </ul>
         </div>',
        'قميص بوبلين زارا', 'قميص نسائي قطن بوبلين من زارا',
        'تسوقي قميص البوبلين القطني الأنيق من زارا للنساء. قطعة أساسية متعددة الاستخدامات في خزانة الملابس.',
        'زارا, قميص, قميص بوبلين, بلوزة, ملابس نسائية, قميص قطن, أساسي', 'قميص نسائي بوبلين من زارا | قطعة أساسية',
        'قطن بوبلين أنيق', 'zara-poplin-shirt-women-ar', 'ar', 44)
on conflict (description_id) do nothing;

-- Product 45: Adidas Slides (Manuf: 3, Type: 2)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (45, NOW(), NOW(), true, NOW(), false, null,
        false, null, true, false, 0.4, null, 'REF-AD-SH-SLD45', 45, 3,
        '65f023632bc46470c104b76f', 2)
on conflict (product_id) do nothing;

-- English Description (ID: 89)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (89, NOW(), NOW(),
        '<div>
           <h2>Adidas Adilette Comfort Slides</h2>
           <p>Relax and recharge in these Adidas Adilette Comfort slides. Featuring a contoured Cloudfoam Plus footbed for super-soft cushioning.</p>
           <h3>Key Features:</h3>
           <ul>
             <li><strong>Cloudfoam Plus Footbed:</strong> Plush cushioning for step-in comfort.</li>
             <li><strong>Single-Bandage Synthetic Upper:</strong> Classic slide design with 3-Stripes.</li>
             <li><strong>Textile Lining:</strong> Soft feel.</li>
             <li><strong>EVA Outsole:</strong> Lightweight comfort.</li>
           </ul>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Material:</strong> Synthetic Upper / EVA Outsole</li>
             <li><strong>Color:</strong> Core Black / Cloud White / Core Black</li>
             <li><strong>Use:</strong> Casual, Post-Workout</li>
           </ul>
         </div>',
        'Adidas Adilette Comfort Slides', 'Adidas Adilette Comfort Slides',
        'Shop the ultra-comfortable Adidas Adilette Comfort slides with Cloudfoam Plus footbed.',
        'Adidas, slides, sandals, Adilette, Cloudfoam Plus, comfort slides',
        'Adidas Adilette Comfort Slides | Ultra-Soft Cushioning', 'Cloudfoam Plus footbed',
        'adidas-adilette-comfort-slides', 'en', 45)
on conflict (description_id) do nothing;

-- Arabic Description (ID: 90)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (90, NOW(), NOW(),
        '<div dir="rtl">
           <h2>شبشب أديداس أديليت كومفورت</h2>
           <p>استرخِ واستعد نشاطك مع شبشب أديداس أديليت كومفورت هذا. يتميز بوسادة قدم Cloudfoam Plus محددة لتوسيد فائق النعومة.</p>
           <h3>الميزات الرئيسية:</h3>
           <ul>
             <li><strong>وسادة قدم Cloudfoam Plus:</strong> توسيد فخم لراحة فورية عند الارتداء.</li>
             <li><strong>جزء علوي صناعي بشريط واحد:</strong> تصميم شبشب كلاسيكي مع 3 خطوط.</li>
             <li><strong>بطانة نسيجية:</strong> ملمس ناعم.</li>
             <li><strong>نعل سفلي EVA:</strong> راحة خفيفة الوزن.</li>
           </ul>
           <h3>المواصفات:</h3>
           <ul>
             <li><strong>الخامة:</strong> جزء علوي صناعي / نعل سفلي EVA</li>
             <li><strong>اللون:</strong> أسود أساسي / أبيض سحابي / أسود أساسي</li>
             <li><strong>الاستخدام:</strong> كاجوال، بعد التمرين</li>
           </ul>
         </div>',
        'شبشب أديداس أديليت كومفورت', 'شبشب أديداس أديليت كومفورت',
        'تسوق شبشب أديداس أديليت كومفورت فائق الراحة مع وسادة قدم Cloudfoam Plus.',
        'أديداس, شبشب, صندل, أديليت, كلاود فوم بلس, شبشب مريح', 'شبشب أديداس أديليت كومفورت | توسيد فائق النعومة',
        'وسادة قدم Cloudfoam Plus', 'adidas-adilette-comfort-slides-ar', 'ar', 45)
on conflict (description_id) do nothing;