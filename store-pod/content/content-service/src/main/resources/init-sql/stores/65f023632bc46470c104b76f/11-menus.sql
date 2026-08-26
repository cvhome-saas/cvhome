-- The two storefront menus, MAIN and FOOTER.
--
-- Demo content for the test store 65f023632bc46470c104b76f (Riyadh Fashion Hub), in every language the store sells in.
-- Generated seed: ids are negative on purpose — content, media and menu ids all come from sequences that
-- only grow upward, so a seed row below zero can never collide with one the running service creates.

-- MAIN menu
INSERT INTO content.menu (id, store_merchant_id, handle, names)
VALUES (-41, '65f023632bc46470c104b76f', 'MAIN', '{"ar": "القائمة الرئيسية", "en": "Main menu"}')
on conflict (store_merchant_id, handle) do update set names = excluded.names;
DELETE FROM content.menu_item
 WHERE menu_id = (select id from content.menu where store_merchant_id = '65f023632bc46470c104b76f' and handle = 'MAIN')
   AND not exists (select 1 from content.menu_item where id = -4001);
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-4001, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b76f' and handle = 'MAIN'), null, 0, '{"ar": "النساء", "en": "Women"}', 'CATEGORY', 'women', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-4002, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b76f' and handle = 'MAIN'), -4001, 0, '{"ar": "دليل المقاسات", "en": "Size guide"}', 'PAGE', 'size-guide', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-4003, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b76f' and handle = 'MAIN'), -4001, 1, '{"ar": "خزانة من اثنتي عشرة قطعة", "en": "A twelve-piece wardrobe"}', 'URL', '/blog/building-a-capsule-wardrobe', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-4004, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b76f' and handle = 'MAIN'), null, 1, '{"ar": "الأقمشة والعناية", "en": "Fabric & care"}', 'PAGE', 'fabric-and-care', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-4005, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b76f' and handle = 'MAIN'), null, 2, '{"ar": "بطاقات الهدايا", "en": "Gift cards"}', 'PAGE', 'gift-cards', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-4006, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b76f' and handle = 'MAIN'), null, 3, '{"ar": "المجلة", "en": "The Magazine"}', 'BLOG_INDEX', null, false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-4007, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b76f' and handle = 'MAIN'), null, 4, '{"ar": "المساعدة", "en": "Help"}', 'FAQ_INDEX', null, false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-4008, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b76f' and handle = 'MAIN'), -4007, 0, '{"ar": "معارضنا", "en": "Our stores"}', 'PAGE', 'our-stores', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-4009, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b76f' and handle = 'MAIN'), -4007, 1, '{"ar": "اتصل بنا", "en": "Contact us"}', 'PAGE', 'contact-us', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-4010, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b76f' and handle = 'MAIN'), null, 5, '{"ar": "إنستغرام", "en": "Instagram"}', 'URL', 'https://instagram.com/riyadhfashionhub', true, true)
on conflict (id) do nothing;

-- FOOTER menu
INSERT INTO content.menu (id, store_merchant_id, handle, names)
VALUES (-42, '65f023632bc46470c104b76f', 'FOOTER', '{"ar": "تذييل الصفحة", "en": "Footer menu"}')
on conflict (store_merchant_id, handle) do update set names = excluded.names;
DELETE FROM content.menu_item
 WHERE menu_id = (select id from content.menu where store_merchant_id = '65f023632bc46470c104b76f' and handle = 'FOOTER')
   AND not exists (select 1 from content.menu_item where id = -4011);
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-4011, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b76f' and handle = 'FOOTER'), null, 0, '{"ar": "من نحن", "en": "About us"}', 'PAGE', 'about-us', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-4012, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b76f' and handle = 'FOOTER'), null, 1, '{"ar": "معارضنا", "en": "Our stores"}', 'PAGE', 'our-stores', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-4013, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b76f' and handle = 'FOOTER'), null, 2, '{"ar": "الوظائف", "en": "Careers"}', 'PAGE', 'careers', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-4014, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b76f' and handle = 'FOOTER'), null, 3, '{"ar": "دليل المقاسات", "en": "Size guide"}', 'PAGE', 'size-guide', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-4015, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b76f' and handle = 'FOOTER'), null, 4, '{"ar": "الخصوصية", "en": "Privacy"}', 'POLICY', 'PRIVACY', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-4016, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b76f' and handle = 'FOOTER'), null, 5, '{"ar": "الكوكيز", "en": "Cookies"}', 'POLICY', 'COOKIES', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-4017, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b76f' and handle = 'FOOTER'), null, 6, '{"ar": "الشروط", "en": "Terms"}', 'POLICY', 'TERMS', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-4018, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b76f' and handle = 'FOOTER'), null, 7, '{"ar": "المساعدة", "en": "Help"}', 'FAQ_INDEX', null, false, true)
on conflict (id) do nothing;

