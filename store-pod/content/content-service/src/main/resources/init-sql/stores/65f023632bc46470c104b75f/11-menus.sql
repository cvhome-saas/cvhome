-- The two storefront menus, MAIN and FOOTER.
--
-- Demo content for the test store 65f023632bc46470c104b75f (USA Electronics Hub), in every language the store sells in.
-- Generated seed: ids are negative on purpose — content, media and menu ids all come from sequences that
-- only grow upward, so a seed row below zero can never collide with one the running service creates.

-- MAIN menu
INSERT INTO content.menu (id, store_merchant_id, handle, names)
VALUES (-31, '65f023632bc46470c104b75f', 'MAIN', '{"en": "Main menu", "fr": "Menu principal"}')
on conflict (store_merchant_id, handle) do update set names = excluded.names;
DELETE FROM content.menu_item
 WHERE menu_id = (select id from content.menu where store_merchant_id = '65f023632bc46470c104b75f' and handle = 'MAIN')
   AND not exists (select 1 from content.menu_item where id = -3001);
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-3001, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b75f' and handle = 'MAIN'), null, 0, '{"en": "Smartphones", "fr": "Smartphones"}', 'CATEGORY', 'smartphones', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-3002, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b75f' and handle = 'MAIN'), -3001, 0, '{"en": "How we choose", "fr": "Comment nous choisissons"}', 'PAGE', 'buying-guides', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-3003, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b75f' and handle = 'MAIN'), -3001, 1, '{"en": "Make a battery last", "fr": "Faire durer une batterie"}', 'URL', '/blog/make-your-battery-last', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-3004, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b75f' and handle = 'MAIN'), null, 1, '{"en": "Warranty", "fr": "Garantie"}', 'PAGE', 'warranty', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-3005, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b75f' and handle = 'MAIN'), null, 2, '{"en": "Trade in", "fr": "Reprise"}', 'PAGE', 'trade-in-program', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-3006, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b75f' and handle = 'MAIN'), null, 3, '{"en": "The Bench", "fr": "Le banc d''essai"}', 'BLOG_INDEX', null, false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-3007, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b75f' and handle = 'MAIN'), null, 4, '{"en": "Help", "fr": "Aide"}', 'FAQ_INDEX', null, false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-3008, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b75f' and handle = 'MAIN'), -3007, 0, '{"en": "Store hours", "fr": "Horaires"}', 'PAGE', 'store-hours', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-3009, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b75f' and handle = 'MAIN'), -3007, 1, '{"en": "Contact us", "fr": "Nous contacter"}', 'PAGE', 'contact-us', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-3010, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b75f' and handle = 'MAIN'), null, 5, '{"en": "Support on X", "fr": "Support sur X"}', 'URL', 'https://x.com/usaelectronics', true, true)
on conflict (id) do nothing;

-- FOOTER menu
INSERT INTO content.menu (id, store_merchant_id, handle, names)
VALUES (-32, '65f023632bc46470c104b75f', 'FOOTER', '{"en": "Footer menu", "fr": "Pied de page"}')
on conflict (store_merchant_id, handle) do update set names = excluded.names;
DELETE FROM content.menu_item
 WHERE menu_id = (select id from content.menu where store_merchant_id = '65f023632bc46470c104b75f' and handle = 'FOOTER')
   AND not exists (select 1 from content.menu_item where id = -3011);
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-3011, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b75f' and handle = 'FOOTER'), null, 0, '{"en": "About us", "fr": "À propos"}', 'PAGE', 'about-us', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-3012, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b75f' and handle = 'FOOTER'), null, 1, '{"en": "Store hours", "fr": "Horaires et accès"}', 'PAGE', 'store-hours', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-3013, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b75f' and handle = 'FOOTER'), null, 2, '{"en": "Warranty & repairs", "fr": "Garantie et réparations"}', 'PAGE', 'warranty', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-3014, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b75f' and handle = 'FOOTER'), null, 3, '{"en": "Gift cards", "fr": "Cartes cadeaux"}', 'PAGE', 'gift-cards', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-3015, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b75f' and handle = 'FOOTER'), null, 4, '{"en": "Privacy", "fr": "Confidentialité"}', 'POLICY', 'PRIVACY', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-3016, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b75f' and handle = 'FOOTER'), null, 5, '{"en": "Cookies", "fr": "Cookies"}', 'POLICY', 'COOKIES', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-3017, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b75f' and handle = 'FOOTER'), null, 6, '{"en": "Terms", "fr": "CGV"}', 'POLICY', 'TERMS', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-3018, (select id from content.menu where store_merchant_id = '65f023632bc46470c104b75f' and handle = 'FOOTER'), null, 7, '{"en": "Help", "fr": "Aide"}', 'FAQ_INDEX', null, false, true)
on conflict (id) do nothing;

