-- The two storefront menus, MAIN and FOOTER.
--
-- Demo content for the test store 65f020632bc46470c104b76f (Beauté Élégante Paris), in every language the store sells in.
-- Generated seed: ids are negative on purpose — content, media and menu ids all come from sequences that
-- only grow upward, so a seed row below zero can never collide with one the running service creates.

-- MAIN menu
INSERT INTO content.menu (id, store_merchant_id, handle, names)
VALUES (-11, '65f020632bc46470c104b76f', 'MAIN', '{"fr": "Menu principal", "en": "Main menu"}')
on conflict (store_merchant_id, handle) do update set names = excluded.names;
DELETE FROM content.menu_item
 WHERE menu_id = (select id from content.menu where store_merchant_id = '65f020632bc46470c104b76f' and handle = 'MAIN')
   AND not exists (select 1 from content.menu_item where id = -1001);
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-1001, (select id from content.menu where store_merchant_id = '65f020632bc46470c104b76f' and handle = 'MAIN'), null, 0, '{"fr": "Soins", "en": "Skincare"}', 'CATEGORY', 'soins-peau', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-1002, (select id from content.menu where store_merchant_id = '65f020632bc46470c104b76f' and handle = 'MAIN'), -1001, 0, '{"fr": "Nos ingrédients", "en": "Our ingredients"}', 'PAGE', 'nos-ingredients', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-1003, (select id from content.menu where store_merchant_id = '65f020632bc46470c104b76f' and handle = 'MAIN'), -1001, 1, '{"fr": "Rituels du Journal", "en": "Rituals on the Journal"}', 'URL', '/blog?category=rituels', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-1004, (select id from content.menu where store_merchant_id = '65f020632bc46470c104b76f' and handle = 'MAIN'), null, 1, '{"fr": "Le Journal", "en": "The Journal"}', 'BLOG_INDEX', null, false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-1005, (select id from content.menu where store_merchant_id = '65f020632bc46470c104b76f' and handle = 'MAIN'), null, 2, '{"fr": "Cartes cadeaux", "en": "Gift cards"}', 'PAGE', 'cartes-cadeaux', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-1006, (select id from content.menu where store_merchant_id = '65f020632bc46470c104b76f' and handle = 'MAIN'), null, 3, '{"fr": "Aide", "en": "Help"}', 'FAQ_INDEX', null, false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-1007, (select id from content.menu where store_merchant_id = '65f020632bc46470c104b76f' and handle = 'MAIN'), -1006, 0, '{"fr": "Livraison et retours", "en": "Shipping & returns"}', 'PAGE', 'livraison-et-retours', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-1008, (select id from content.menu where store_merchant_id = '65f020632bc46470c104b76f' and handle = 'MAIN'), -1006, 1, '{"fr": "Nous contacter", "en": "Contact us"}', 'PAGE', 'contact-us', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-1009, (select id from content.menu where store_merchant_id = '65f020632bc46470c104b76f' and handle = 'MAIN'), null, 4, '{"fr": "À propos", "en": "About"}', 'PAGE', 'about-us', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-1010, (select id from content.menu where store_merchant_id = '65f020632bc46470c104b76f' and handle = 'MAIN'), null, 5, '{"fr": "Instagram", "en": "Instagram"}', 'URL', 'https://instagram.com/beauteelegante', true, true)
on conflict (id) do nothing;

-- FOOTER menu
INSERT INTO content.menu (id, store_merchant_id, handle, names)
VALUES (-12, '65f020632bc46470c104b76f', 'FOOTER', '{"fr": "Pied de page", "en": "Footer menu"}')
on conflict (store_merchant_id, handle) do update set names = excluded.names;
DELETE FROM content.menu_item
 WHERE menu_id = (select id from content.menu where store_merchant_id = '65f020632bc46470c104b76f' and handle = 'FOOTER')
   AND not exists (select 1 from content.menu_item where id = -1011);
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-1011, (select id from content.menu where store_merchant_id = '65f020632bc46470c104b76f' and handle = 'FOOTER'), null, 0, '{"fr": "À propos", "en": "About us"}', 'PAGE', 'about-us', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-1012, (select id from content.menu where store_merchant_id = '65f020632bc46470c104b76f' and handle = 'FOOTER'), null, 1, '{"fr": "Carrières", "en": "Careers"}', 'PAGE', 'carrieres', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-1013, (select id from content.menu where store_merchant_id = '65f020632bc46470c104b76f' and handle = 'FOOTER'), null, 2, '{"fr": "Espace presse", "en": "Press room"}', 'PAGE', 'presse', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-1014, (select id from content.menu where store_merchant_id = '65f020632bc46470c104b76f' and handle = 'FOOTER'), null, 3, '{"fr": "Livraison et retours", "en": "Shipping & returns"}', 'PAGE', 'livraison-et-retours', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-1015, (select id from content.menu where store_merchant_id = '65f020632bc46470c104b76f' and handle = 'FOOTER'), null, 4, '{"fr": "Confidentialité", "en": "Privacy"}', 'POLICY', 'PRIVACY', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-1016, (select id from content.menu where store_merchant_id = '65f020632bc46470c104b76f' and handle = 'FOOTER'), null, 5, '{"fr": "Cookies", "en": "Cookies"}', 'POLICY', 'COOKIES', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-1017, (select id from content.menu where store_merchant_id = '65f020632bc46470c104b76f' and handle = 'FOOTER'), null, 6, '{"fr": "CGV", "en": "Terms"}', 'POLICY', 'TERMS', false, true)
on conflict (id) do nothing;
INSERT INTO content.menu_item (id, menu_id, parent_id, position, labels, target_kind, target_value, open_in_new_tab, visible)
VALUES (-1018, (select id from content.menu where store_merchant_id = '65f020632bc46470c104b76f' and handle = 'FOOTER'), null, 7, '{"fr": "Aide", "en": "Help"}', 'FAQ_INDEX', null, false, true)
on conflict (id) do nothing;

