/*
can you generate another sql inserts similar to this
where languages=['ar','en'] and store_id='65f023632bc46470c104b76f' and country_id='SA'
domain for this store is fashion
*/
INSERT INTO merchant.merchant_store (store_merchant_id, currency_format_national, in_business_since, org, theme,
                                     color_theme, seizeunitcode, store_email, store_logo, store_banner, store_address,
                                     store_city, store_name, store_phone, store_postal_code, store_state_prov,
                                     use_cache, weightunitcode, country_id, currency_id, language_code)
VALUES ('65f023632bc46470c104b76f', false, '2024-03-31', '21f023932bc66470c104b76f', 'DEFAULT', 'LIGHT',
        'CM', -- seizeunitcode for fashion
        'info@riyadhfashion.sa', 'logo.jpeg', 'banner.jpeg', '123 Olaya Street', 'Riyadh', 'Riyadh-Fashion-Hub',
        '+966 50 123 4567',
        '11564', 'Riyadh Province', false, 'KG', 'SA', 'SAR', 'ar')
on conflict (store_merchant_id) do nothing;


-- <<Begin Loop on $parameter.languages l
INSERT INTO merchant.merchant_language (store_merchant_id, language_code)
VALUES ('65f023632bc46470c104b76f', 'ar')
on conflict (store_merchant_id, language_code) do nothing;
INSERT INTO merchant.merchant_language (store_merchant_id, language_code)
VALUES ('65f023632bc46470c104b76f', 'en')
on conflict (store_merchant_id, language_code) do nothing;
-- End Loop>>

-- <<Begin Loop on l in ('0','1','2','3','4')
INSERT INTO merchant.merchant_slider_images (store_merchant_id, priority, name)
VALUES ('65f023632bc46470c104b76f', 0, 'slide-1.jpeg');
INSERT INTO merchant.merchant_slider_images (store_merchant_id, priority, name)
VALUES ('65f023632bc46470c104b76f', 1, 'slide-2.jpeg');
INSERT INTO merchant.merchant_slider_images (store_merchant_id, priority, name)
VALUES ('65f023632bc46470c104b76f', 2, 'slide-3.jpeg');
INSERT INTO merchant.merchant_slider_images (store_merchant_id, priority, name)
VALUES ('65f023632bc46470c104b76f', 3, 'slide-4.jpeg');
INSERT INTO merchant.merchant_slider_images (store_merchant_id, priority, name)
VALUES ('65f023632bc46470c104b76f', 4, 'slide-5.jpeg');
-- End Loop>>


-- <<Begin Loop on l in ('FACEBOOK','X','INSTAGRAM','TIKTOK')
INSERT INTO merchant.social_links (store_merchant_id, provider, url)
VALUES ('65f023632bc46470c104b76f', 'FACEBOOK', 'https://facebook.com/riyadhfashionhub');
INSERT INTO merchant.social_links (store_merchant_id, provider, url)
VALUES ('65f023632bc46470c104b76f', 'X', 'https://x.com/riyadhfashionhub');
INSERT INTO merchant.social_links (store_merchant_id, provider, url)
VALUES ('65f023632bc46470c104b76f', 'INSTAGRAM', 'https://instagram.com/riyadhfashionhub');
INSERT INTO merchant.social_links (store_merchant_id, provider, url)
VALUES ('65f023632bc46470c104b76f', 'TIKTOK', 'https://tiktok.com/@riyadhfashionhub');
-- End Loop>>



INSERT INTO merchant.store_domains(domain, domain_type, store_merchant_id)
VALUES ('org1-store1.asrevo.com', 'CUSTOM_DOMAIN', '65f023632bc46470c104b76f')
ON CONFLICT DO NOTHING;

INSERT INTO merchant.store_domains(domain, domain_type, store_merchant_id)
VALUES ('org1-store1', 'SUB_DOMAIN', '65f023632bc46470c104b76f')
ON CONFLICT DO NOTHING;

/*
Generated content for store_id='65f023632bc46470c104b76f' (Fashion Domain) store name Riyadh Fashion Hub
Pages: ['about-us', 'contact-us', 'terms', 'privacy', 'location', 'faq']
Boxes: ['header-message', 'agreement','meta-title','meta-description']
Languages: ['ar', 'en']
Starting content_id: 1
Starting description_id: 1
Starting sort_order: 1
*/

-- Page: about-us
INSERT INTO merchant.content (content_id, code, content_type,
                             link_to_menu, sort_order, visible, store_merchant_id)
VALUES (1, 'about-us', 'PAGE', true, 1, true, '65f023632bc46470c104b76f')
on conflict (content_id) do nothing;

-- Language: ar
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (1, now(), now(),
        '<h1>عن رياض فاشن هاب</h1><p>مرحبًا بكم في رياض فاشن هاب، وجهتكم الأولى لأحدث صيحات الموضة والأزياء العصرية في قلب الرياض. نحن نؤمن بأن الأناقة تعكس شخصيتك ويجب أن تكون متاحة للجميع.</p><h2>قصتنا</h2><p>انطلقنا بشغف كبير نحو الموضة من مدينة الرياض، لنصبح اليوم منصة رائدة تجمع بين الأصالة والمعاصرة، مقدمين أزياء تناسب ذوق المرأة والرجل العصري في المملكة.</p><h2>رؤيتنا</h2><p>أن نكون الخيار الأول للموضة في الرياض والمملكة، ملهمين عملائنا للتعبير عن تفردهم وأناقتهم بثقة.</p>',
        'عن رياض فاشن هاب', 'عن رياض فاشن هاب',
        'تعرف على قصة ورؤية رياض فاشن هاب، وجهتك للأزياء العصرية في الرياض.',
        'موضة, أزياء, رياض فاشن هاب, عن الشركة, ملابس, تسوق أونلاين, الرياض', 'عن رياض فاشن هاب | أزياء عصرية',
        'about-us', 1, 'ar')
on conflict (description_id) do nothing;
-- Language: en
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (2, now(), now(),
        '<h1>About Riyadh Fashion Hub</h1><p>Welcome to Riyadh Fashion Hub, your premier destination for the latest trends and contemporary fashion in the heart of Riyadh. We believe that elegance reflects your personality and should be accessible to everyone.</p><h2>Our Story</h2><p>We started with a great passion for fashion from the city of Riyadh, growing to become a leading platform that combines authenticity and modernity, offering fashion that suits the taste of the modern man and woman in the Kingdom.</p><h2>Our Vision</h2><p>To be the first choice for fashion in Riyadh and the Kingdom, inspiring our customers to express their uniqueness and elegance with confidence.</p>',
        'About Riyadh Fashion Hub', 'About Riyadh Fashion Hub',
        'Learn about the story and vision of Riyadh Fashion Hub, your destination for contemporary fashion in Riyadh.',
        'fashion, style, clothing, about us, Riyadh Fashion Hub, online shopping, Riyadh',
        'About Riyadh Fashion Hub | Contemporary Fashion',
        'about-us', 1, 'en')
on conflict (description_id) do nothing;

-- Page: contact-us
INSERT INTO merchant.content (content_id, code, content_type,
                             link_to_menu, sort_order, visible, store_merchant_id)
VALUES (2, 'contact-us', 'PAGE', true, 2, true, '65f023632bc46470c104b76f')
on conflict (content_id) do nothing;

-- Language: ar
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (3, now(), now(),
        '<h1>تواصل معنا</h1><p>هل لديك استفسار حول تشكيلاتنا، المقاسات، أو حالة طلبك؟ فريق خدمة عملاء رياض فاشن هاب مستعد دائمًا للمساعدة.</p><h2>معلومات الاتصال:</h2><ul><li>البريد الإلكتروني: care@riyadhfashionhub.sa</li><li>الهاتف: 9200 XXXXX (السعودية)</li><li>ساعات العمل: السبت - الخميس، 10 صباحًا - 8 مساءً (بتوقيت السعودية)</li></ul><!-- Contact Form Placeholder -->',
        'تواصل معنا', 'تواصل مع رياض فاشن هاب',
        'تواصل مع خدمة عملاء رياض فاشن هاب للاستفسارات حول المنتجات، الطلبات، أو المساعدة في التسوق.',
        'اتصل بنا, رياض فاشن هاب, خدمة عملاء, مساعدة تسوق, استفسار أزياء, الرياض', 'تواصل معنا | رياض فاشن هاب',
        'contact-us', 2, 'ar')
on conflict (description_id) do nothing;
-- Language: en
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (4, now(), now(),
        '<h1>Contact Us</h1><p>Have an inquiry about our collections, sizing, or your order status? The Riyadh Fashion Hub customer service team is always ready to help.</p><h2>Contact Information:</h2><ul><li>Email: care@riyadhfashionhub.sa</li><li>Phone: 9200 XXXXX (Saudi Arabia)</li><li>Hours: Sat - Thu, 10 AM - 8 PM (KSA Time)</li></ul><!-- Contact Form Placeholder -->',
        'Contact Us', 'Contact Riyadh Fashion Hub',
        'Reach out to Riyadh Fashion Hub customer service for inquiries about products, orders, or shopping assistance.',
        'contact us, Riyadh Fashion Hub, customer service, shopping help, fashion inquiry, Riyadh',
        'Contact Us | Riyadh Fashion Hub',
        'contact-us', 2, 'en')
on conflict (description_id) do nothing;

-- Page: terms
INSERT INTO merchant.content (content_id, code, content_type,
                             link_to_menu, sort_order, visible, store_merchant_id)
VALUES (3, 'terms', 'PAGE', false, 3, true, '65f023632bc46470c104b76f')
on conflict (content_id) do nothing;

-- Language: ar
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (5, now(), now(),
        '<h1>الشروط والأحكام</h1><p>يرجى قراءة شروط وأحكام استخدام موقع رياض فاشن هاب وشراء المنتجات بعناية.</p><h2>1. الطلبات والدفع</h2><p>تخضع جميع الطلبات للقبول وتوفر المنتج. نقبل الدفع عبر البطاقات الائتمانية (مدى، فيزا، ماستركارد) والتحويل البنكي داخل المملكة.</p><h2>2. الشحن والإرجاع</h2><p>نوفر الشحن لجميع مناطق المملكة العربية السعودية. تطبق سياسة الإرجاع والاستبدال خلال 7 أيام من استلام الطلب بشروط محددة. يرجى مراجعة صفحة الشحن والإرجاع.</p><h2>3. الملكية الفكرية</h2><p>جميع المحتويات المعروضة على هذا الموقع هي ملك لرياض فاشن هاب.</p>',
        'الشروط والأحكام', 'الشروط والأحكام | رياض فاشن هاب',
        'اقرأ الشروط والأحكام المتعلقة بالتسوق، الدفع، الشحن، والإرجاع في رياض فاشن هاب.',
        'شروط, أحكام, رياض فاشن هاب, قانوني, تسوق أونلاين, شحن, إرجاع, السعودية', 'الشروط والأحكام | رياض فاشن هاب',
        'terms', 3, 'ar')
on conflict (description_id) do nothing;
-- Language: en
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (6, now(), now(),
        '<h1>Terms and Conditions</h1><p>Please carefully read the terms and conditions for using the Riyadh Fashion Hub website and purchasing products.</p><h2>1. Orders and Payment</h2><p>All orders are subject to acceptance and availability. We accept payment via credit cards (Mada, Visa, MasterCard) and bank transfer within the Kingdom.</p><h2>2. Shipping and Returns</h2><p>We offer shipping to all regions within Saudi Arabia. Our return and exchange policy applies within 7 days of receiving the order under specific conditions. Please review the Shipping & Returns page.</p><h2>3. Intellectual Property</h2><p>All content displayed on this website is the property of Riyadh Fashion Hub.</p>',
        'Terms & Conditions', 'Terms & Conditions | Riyadh Fashion Hub',
        'Read the terms and conditions related to shopping, payment, shipping, and returns at Riyadh Fashion Hub.',
        'terms, conditions, Riyadh Fashion Hub, legal, online shopping, shipping, returns, Saudi Arabia',
        'Terms & Conditions | Riyadh Fashion Hub',
        'terms', 3, 'en')
on conflict (description_id) do nothing;

-- Page: privacy
INSERT INTO merchant.content (content_id, code, content_type,
                             link_to_menu, sort_order, visible, store_merchant_id)
VALUES (4, 'privacy', 'PAGE', false, 4, true, '65f023632bc46470c104b76f')
on conflict (content_id) do nothing;

-- Language: ar
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (7, now(), now(),
        '<h1>سياسة الخصوصية</h1><p>خصوصيتك وأمان معلوماتك أولوية قصوى في رياض فاشن هاب. توضح هذه السياسة كيف نجمع ونستخدم ونحمي بياناتك الشخصية عند استخدامك لموقعنا.</p><h2>جمع المعلومات</h2><p>نقوم بجمع المعلومات التي تزودنا بها عند إنشاء حساب أو إتمام عملية شراء (الاسم، العنوان، البريد الإلكتروني، رقم الهاتف)، بالإضافة إلى بيانات التصفح لتحسين تجربتك.</p><h2>استخدام المعلومات</h2><p>تستخدم معلوماتك لمعالجة الطلبات، تخصيص تجربتك التسويقية، التواصل معك بخصوص طلباتك وعروضنا، وتحسين خدماتنا.</p><h2>حماية البيانات</h2><p>نتخذ إجراءات أمنية مشددة لحماية بياناتك الشخصية من الوصول غير المصرح به.</p>',
        'سياسة الخصوصية', 'سياسة الخصوصية | رياض فاشن هاب',
        'تعرف على كيفية حماية رياض فاشن هاب لبياناتك الشخصية ومعلومات التسوق الخاصة بك.',
        'خصوصية, رياض فاشن هاب, بيانات شخصية, حماية بيانات, تسوق أزياء, أمان', 'سياسة الخصوصية | رياض فاشن هاب',
        'privacy', 4, 'ar')
on conflict (description_id) do nothing;
-- Language: en
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (8, now(), now(),
        '<h1>Privacy Policy</h1><p>Your privacy and the security of your information are a top priority at Riyadh Fashion Hub. This policy explains how we collect, use, and protect your personal data when you use our website.</p><h2>Information Collection</h2><p>We collect information you provide when creating an account or completing a purchase (name, address, email, phone number), as well as browsing data to improve your experience.</p><h2>Use of Information</h2><p>Your information is used to process orders, personalize your marketing experience, communicate with you about your orders and our offers, and improve our services.</p><h2>Data Protection</h2><p>We take strict security measures to protect your personal data from unauthorized access.</p>',
        'Privacy Policy', 'Privacy Policy | Riyadh Fashion Hub',
        'Learn how Riyadh Fashion Hub protects your personal data and shopping information.',
        'privacy, Riyadh Fashion Hub, personal data, data protection, fashion shopping, security',
        'Privacy Policy | Riyadh Fashion Hub',
        'privacy', 4, 'en')
on conflict (description_id) do nothing;

-- Page: location
INSERT INTO merchant.content (content_id, code, content_type,
                             link_to_menu, sort_order, visible, store_merchant_id)
VALUES (5, 'location', 'PAGE', false, 5, true, '65f023632bc46470c104b76f')
on conflict (content_id) do nothing;

-- Language: ar
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (9, now(), now(),
        '<h1>موقعنا</h1><p>تفضل بزيارتنا في معرضنا الرئيسي بالرياض لتجربة تسوق مميزة.</p><h2>معرض الرياض</h2><p>شارع العليا، حي العليا، الرياض، المملكة العربية السعودية<br>مقابل [معلم بارز]<br>ساعات العمل: السبت - الخميس، 11 صباحًا - 10 مساءً</p><p><i>نحن متواجدون بشكل أساسي عبر الإنترنت لخدمتكم في جميع أنحاء المملكة.</i></p><!-- Map Placeholder -->',
        'موقعنا', 'موقع معرض رياض فاشن هاب',
        'اعثر على موقع معرض رياض فاشن هاب الرئيسي في الرياض وساعات العمل.',
        'موقع, معرض, رياض فاشن هاب, عنوان, ساعات العمل, الرياض, العليا', 'موقعنا | رياض فاشن هاب',
        'location', 5, 'ar')
on conflict (description_id) do nothing;
-- Language: en
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (10, now(), now(),
        '<h1>Our Location</h1><p>Visit us at our main showroom in Riyadh for a unique shopping experience.</p><h2>Riyadh Showroom</h2><p>Olaya Street, Al Olaya District, Riyadh, Saudi Arabia<br>Opposite [Landmark]<br>Hours: Sat - Thu, 11 AM - 10 PM</p><p><i>We primarily operate online to serve you across the Kingdom.</i></p><!-- Map Placeholder -->',
        'Our Location', 'Riyadh Fashion Hub Showroom Location',
        'Find the location and opening hours of the main Riyadh Fashion Hub showroom in Riyadh.',
        'location, showroom, Riyadh Fashion Hub, address, hours, Riyadh, Olaya', 'Our Location | Riyadh Fashion Hub',
        'location', 5, 'en')
on conflict (description_id) do nothing;

-- Page: faq
INSERT INTO merchant.content (content_id, code, content_type,
                             link_to_menu, sort_order, visible, store_merchant_id)
VALUES (6, 'faq', 'PAGE', false, 6, true, '65f023632bc46470c104b76f')
on conflict (content_id) do nothing;

-- Language: ar
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (11, now(), now(),
        '<h1>الأسئلة الشائعة</h1><p>تجد هنا إجابات للأسئلة المتكررة حول التسوق في رياض فاشن هاب.</p><h2>المقاسات</h2><p><strong>س: كيف أتأكد من المقاس الصحيح؟</strong><br>ج: نوفر دليل مقاسات مفصل لكل منتج. يمكنك مقارنة قياساتك بالجدول أو التواصل مع خدمة العملاء للمشورة.</p><h2>الشحن</h2><p><strong>س: كم يستغرق وصول الطلب داخل الرياض وخارجها؟</strong><br>ج: الشحن داخل الرياض يستغرق عادة 1-2 أيام عمل، وخارج الرياض 3-5 أيام عمل.</p><h2>الإرجاع والاستبدال</h2><p><strong>س: هل يمكنني إرجاع أو استبدال قطعة اشتريتها؟</strong><br>ج: نعم، يمكنك ذلك خلال 7 أيام من الاستلام بشرط أن تكون القطعة بحالتها الأصلية وغير مستخدمة ومع كافة ملحقاتها. تطبق بعض الاستثناءات، يرجى مراجعة سياسة الإرجاع.</p>',
        'الأسئلة الشائعة', 'الأسئلة الشائعة | رياض فاشن هاب',
        'إجابات للأسئلة المتكررة حول المقاسات، الشحن، الإرجاع، وغيرها في رياض فاشن هاب.',
        'أسئلة شائعة, رياض فاشن هاب, مساعدة, مقاسات, شحن, إرجاع, السعودية', 'الأسئلة الشائعة | رياض فاشن هاب',
        'faq', 6, 'ar')
on conflict (description_id) do nothing;
-- Language: en
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (12, now(), now(),
        '<h1>Frequently Asked Questions (FAQ)</h1><p>Find answers here to frequently asked questions about shopping at Riyadh Fashion Hub.</p><h2>Sizing</h2><p><strong>Q: How do I ensure the correct size?</strong><br>A: We provide a detailed size guide for each product. You can compare your measurements with the chart or contact customer service for advice.</p><h2>Shipping</h2><p><strong>Q: How long does it take for an order to arrive within and outside Riyadh?</strong><br>A: Shipping within Riyadh usually takes 1-2 business days, and outside Riyadh 3-5 business days.</p><h2>Returns & Exchanges</h2><p><strong>Q: Can I return or exchange an item I purchased?</strong><br>A: Yes, you can do so within 7 days of receipt, provided the item is in its original condition, unused, and with all tags attached. Some exceptions apply, please review our return policy.</p>',
        'FAQ', 'FAQ | Riyadh Fashion Hub',
        'Answers to frequently asked questions about sizing, shipping, returns, and more at Riyadh Fashion Hub.',
        'faq, Riyadh Fashion Hub, help, sizing, shipping, returns, Saudi Arabia', 'FAQ | Riyadh Fashion Hub',
        'faq', 6, 'en')
on conflict (description_id) do nothing;

-- Box: header-message
INSERT INTO merchant.content (content_id, code, content_type,
                             link_to_menu, sort_order, visible, store_merchant_id)
VALUES (7, 'header-message', 'BOX', false, 7, true, '65f023632bc46470c104b76f')
on conflict (content_id) do nothing;

-- Language: ar
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (13, now(), now(),
        'تشكيلة العيد وصلت! تسوقي الآن لإطلالة عيد مميزة. شحن مجاني للطلبات فوق 350 ريال.',
        'رسالة الترويسة', 'رسالة الترويسة',
        '', '', '', '', 7, 'ar')
on conflict (description_id) do nothing;
-- Language: en
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (14, now(), now(),
        'Eid Collection Has Arrived! Shop now for a special Eid look. Free shipping on orders over 350 SAR.',
        'Header Message', 'Header Message',
        '', '', '', '', 7, 'en')
on conflict (description_id) do nothing;

-- Box: agreement
INSERT INTO merchant.content (content_id, code, content_type,
                             link_to_menu, sort_order, visible, store_merchant_id)
VALUES (8, 'agreement', 'BOX', false, 8, true, '65f023632bc46470c104b76f')
on conflict (content_id) do nothing;

-- Language: ar
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (15, now(), now(),
        '<p style="text-align:center; font-size:0.9em; padding:5px;">باستخدامك للموقع، فإنك توافق على <a href="/terms">الشروط والأحكام</a>.</p>',
        'اتفاقية الاستخدام', 'اتفاقية الاستخدام',
        '', '', '', '', 8, 'ar')
on conflict (description_id) do nothing;
-- Language: en
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (16, now(), now(),
        '<p style="text-align:center; font-size:0.9em; padding:5px;">By using the site, you agree to our <a href="/terms">Terms & Conditions</a>.</p>',
        'User Agreement', 'User Agreement',
        '', '', '', '', 8, 'en')
on conflict (description_id) do nothing;

-- Box: meta-title
INSERT INTO merchant.content (content_id, code, content_type,
                             link_to_menu, sort_order, visible, store_merchant_id)
VALUES (9, 'meta-title', 'BOX', false, 9, true, '65f023632bc46470c104b76f')
on conflict (content_id) do nothing;

-- Language: ar
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (17, now(), now(),
        'رياض فاشن هاب | أزياء عصرية وتشكيلات حصرية في الرياض', '', '', '', '', '', '', 9, 'ar')
on conflict (description_id) do nothing;
-- Language: en
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (18, now(), now(),
        'Riyadh Fashion Hub | Trendy Fashion & Exclusive Collections in Riyadh', '', '', '', '', '', '', 9, 'en')
on conflict (description_id) do nothing;

-- Box: meta-description
INSERT INTO merchant.content (content_id, code, content_type,
                             link_to_menu, sort_order, visible, store_merchant_id)
VALUES (10, 'meta-description', 'BOX', false, 10, true, '65f023632bc46470c104b76f')
on conflict (content_id) do nothing;

-- Language: ar
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (19, now(), now(),
        'اكتشف أحدث صيحات الموضة والأزياء النسائية والرجالية في رياض فاشن هاب. تسوق أونلاين تشكيلات حصرية من الملابس، الأحذية، والإكسسوارات. توصيل لجميع أنحاء المملكة.',
        '', '', '', '', '', '', 10, 'ar')
on conflict (description_id) do nothing;
-- Language: en
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (20, now(), now(),
        'Discover the latest fashion trends for women and men at Riyadh Fashion Hub. Shop online for exclusive collections of clothing, shoes, and accessories. Delivery across Saudi Arabia.',
        '', '', '', '', '', '', 10, 'en')
on conflict (description_id) do nothing;