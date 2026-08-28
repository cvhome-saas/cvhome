-- The two storefront menus, MAIN and FOOTER.
--
-- Demo content for the test store 65f023632bc26470c104b75f (Egypt Car Sales), in every language the store sells in.
-- Generated seed: ids are negative on purpose — content, media and menu ids all come from sequences that
-- only grow upward, so a seed row below zero can never collide with one the running service creates.

-- MAIN menu
INSERT INTO content.menu (id, store_merchant_id, handle, names)
VALUES (-21, '65f023632bc26470c104b75f', 'MAIN', '{"ar": "القائمة الرئيسية", "fr": "Menu principal"}')
on conflict (store_merchant_id, handle) do update set names = excluded.names;
DELETE FROM content.menu_item
 WHERE menu_id = (select id from content.menu where store_merchant_id = '65f023632bc26470c104b75f' and handle = 'MAIN')
   AND not exists (select 1 from content.menu_item where id = -2001);
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-2001, (select id from content.menu where store_merchant_id = '65f023632bc26470c104b75f' and handle = 'MAIN'), null, 0, '{"ar": "سيارات جديدة", "fr": "Véhicules neufs"}', 'CATEGORY', 'berlines', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-2002, (select id from content.menu where store_merchant_id = '65f023632bc26470c104b75f' and handle = 'MAIN'), -2001, 0, '{"ar": "تقرير الفحص", "fr": "Rapport d''inspection"}', 'PAGE', 'inspection-report', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-2003, (select id from content.menu where store_merchant_id = '65f023632bc26470c104b75f' and handle = 'MAIN'), -2001, 1, '{"ar": "دليل الشراء", "fr": "Guide d''achat"}', 'URL', '/blog?category=buying-guide', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-2004, (select id from content.menu where store_merchant_id = '65f023632bc26470c104b75f' and handle = 'MAIN'), null, 1, '{"ar": "التمويل", "fr": "Financement"}', 'PAGE', 'financing', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-2005, (select id from content.menu where store_merchant_id = '65f023632bc26470c104b75f' and handle = 'MAIN'), null, 2, '{"ar": "استبدال سيارتك", "fr": "Reprise"}', 'PAGE', 'trade-in', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-2006, (select id from content.menu where store_merchant_id = '65f023632bc26470c104b75f' and handle = 'MAIN'), null, 3, '{"ar": "المدونة", "fr": "Le blog"}', 'BLOG_INDEX', null, false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-2007, (select id from content.menu where store_merchant_id = '65f023632bc26470c104b75f' and handle = 'MAIN'), null, 4, '{"ar": "المساعدة", "fr": "Aide"}', 'FAQ_INDEX', null, false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-2008, (select id from content.menu where store_merchant_id = '65f023632bc26470c104b75f' and handle = 'MAIN'), -2007, 0, '{"ar": "فروعنا", "fr": "Nos showrooms"}', 'PAGE', 'showrooms', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-2009, (select id from content.menu where store_merchant_id = '65f023632bc26470c104b75f' and handle = 'MAIN'), -2007, 1, '{"ar": "اتصل بنا", "fr": "Nous contacter"}', 'PAGE', 'contact-us', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-2010, (select id from content.menu where store_merchant_id = '65f023632bc26470c104b75f' and handle = 'MAIN'), null, 5, '{"ar": "واتساب", "fr": "WhatsApp"}', 'URL', 'https://wa.me/201001234567', true, true)
on conflict (id) do nothing;

-- FOOTER menu
INSERT INTO content.menu (id, store_merchant_id, handle, names)
VALUES (-22, '65f023632bc26470c104b75f', 'FOOTER', '{"ar": "تذييل الصفحة", "fr": "Pied de page"}')
on conflict (store_merchant_id, handle) do update set names = excluded.names;
DELETE FROM content.menu_item
 WHERE menu_id = (select id from content.menu where store_merchant_id = '65f023632bc26470c104b75f' and handle = 'FOOTER')
   AND not exists (select 1 from content.menu_item where id = -2011);
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-2011, (select id from content.menu where store_merchant_id = '65f023632bc26470c104b75f' and handle = 'FOOTER'), null, 0, '{"ar": "من نحن", "fr": "À propos"}', 'PAGE', 'about-us', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-2012, (select id from content.menu where store_merchant_id = '65f023632bc26470c104b75f' and handle = 'FOOTER'), null, 1, '{"ar": "فروعنا", "fr": "Nos showrooms"}', 'PAGE', 'showrooms', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-2013, (select id from content.menu where store_merchant_id = '65f023632bc26470c104b75f' and handle = 'FOOTER'), null, 2, '{"ar": "التمويل", "fr": "Financement"}', 'PAGE', 'financing', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-2014, (select id from content.menu where store_merchant_id = '65f023632bc26470c104b75f' and handle = 'FOOTER'), null, 3, '{"ar": "شحن السيارات الكهربائية", "fr": "Recharge électrique"}', 'PAGE', 'ev-charging', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-2015, (select id from content.menu where store_merchant_id = '65f023632bc26470c104b75f' and handle = 'FOOTER'), null, 4, '{"ar": "الخصوصية", "fr": "Confidentialité"}', 'POLICY', 'PRIVACY', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-2016, (select id from content.menu where store_merchant_id = '65f023632bc26470c104b75f' and handle = 'FOOTER'), null, 5, '{"ar": "الكوكيز", "fr": "Cookies"}', 'POLICY', 'COOKIES', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-2017, (select id from content.menu where store_merchant_id = '65f023632bc26470c104b75f' and handle = 'FOOTER'), null, 6, '{"ar": "الشروط", "fr": "CGV"}', 'POLICY', 'TERMS', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-2018, (select id from content.menu where store_merchant_id = '65f023632bc26470c104b75f' and handle = 'FOOTER'), null, 7, '{"ar": "المساعدة", "fr": "Aide"}', 'FAQ_INDEX', null, false, true)
on conflict (id) do nothing;

