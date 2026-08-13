with stores(store_no, store_id) as (
    values (1, '65f023632bc46470c104b76f'),
           (2, '65f023632bc46470c104b75f'),
           (3, '65f023632bc26470c104b75f'),
           (4, '65f020632bc46470c104b76f')
), types(type_no, content_type, code) as (
    values (1, 'PAGE', 'about'),
           (2, 'POST', 'welcome'),
           (3, 'BANNER', 'home-hero'),
           (4, 'FAQ', 'shipping-question'),
           (5, 'MENU', 'main-menu'),
           (6, 'POLICY', 'privacy-1-0')
)
insert into content.content (content_id, date_created, date_modified, store_merchant_id, code, content_type,
                             status, version)
select 1000 + stores.store_no * 100 + types.type_no, current_timestamp, current_timestamp, stores.store_id,
       types.code, types.content_type, 'PUBLISHED', 0
from stores cross join types
on conflict (content_id) do nothing;

with stores(store_no, store_id) as (
    values (1, '65f023632bc46470c104b76f'),
           (2, '65f023632bc46470c104b75f'),
           (3, '65f023632bc26470c104b75f'),
           (4, '65f020632bc46470c104b76f')
), types(type_no, content_type, code, name, body) as (
    values (1, 'PAGE', 'about', 'About us', 'About this demo store.'),
           (2, 'POST', 'welcome', 'Welcome', 'Welcome to this demo store.'),
           (3, 'BANNER', 'home-hero', 'Welcome banner', 'Discover this demo store.'),
           (4, 'FAQ', 'shipping-question', 'How does shipping work?', 'Shipping is calculated at checkout.'),
           (5, 'MENU', 'main-menu', 'Main menu', 'Primary navigation.'),
           (6, 'POLICY', 'privacy-1-0', 'Privacy policy', 'Demo privacy policy content.')
)
insert into content.content_description (description_id, content_id, store_merchant_id, content_type,
                                         language_code, translation_state, name, title, description, sef_url,
                                         no_index, date_created, date_modified)
select 2000 + stores.store_no * 100 + types.type_no, 1000 + stores.store_no * 100 + types.type_no,
       stores.store_id, types.content_type, 'en', 'CURRENT', types.name, types.name, types.body, types.code,
       false, current_timestamp, current_timestamp
from stores cross join types
on conflict (description_id) do nothing;

with stores(store_no, store_id) as (
    values (1, '65f023632bc46470c104b76f'),
           (2, '65f023632bc46470c104b75f'),
           (3, '65f023632bc26470c104b75f'),
           (4, '65f020632bc46470c104b76f')
)
insert into content.media_asset (asset_id, store_merchant_id, original_filename, normalized_filename,
                                 detected_mime, media_kind, byte_size, checksum, page_count, storage_key,
                                 processing_status, date_created, version)
select 3000 + store_no, store_id, 'demo-guide.pdf', 'demo-guide.pdf', 'application/pdf', 'DOCUMENT', 1,
       lpad(store_no::text, 64, '0'), 1, store_id || '/seed/demo-guide.pdf', 'READY', current_timestamp, 0
from stores
on conflict (asset_id) do nothing;

insert into content.content_page (content_id, template, show_in_sitemap)
select 1000 + store_no * 100 + 1, 'standard', true from generate_series(1, 4) store_no
on conflict (content_id) do nothing;

insert into content.content_post (content_id, author_snapshot, reading_minutes, featured, excerpt)
select 1000 + store_no * 100 + 2, 'Demo team', 1, true, 'Welcome to this demo store.'
from generate_series(1, 4) store_no
on conflict (content_id) do nothing;

insert into content.content_banner (content_id, placement, position, target_kind, target_value,
                                    logged_in_target)
select 1000 + store_no * 100 + 3, 'HOME_HERO', 0, 'URL', '/about', 'ANY'
from generate_series(1, 4) store_no
on conflict (content_id) do nothing;

insert into content.banner_artwork (content_id, language_code, desktop_media_id, alt_text)
select 1000 + store_no * 100 + 3, 'en', 3000 + store_no, 'Welcome to the demo store'
from generate_series(1, 4) store_no
on conflict (content_id, language_code) do nothing;

with stores(store_no, store_id) as (
    values (1, '65f023632bc46470c104b76f'),
           (2, '65f023632bc46470c104b75f'),
           (3, '65f023632bc26470c104b75f'),
           (4, '65f020632bc46470c104b76f')
)
insert into content.faq_group (group_id, store_merchant_id, code, position)
select 4000 + store_no, store_id, 'shipping', 0 from stores
on conflict (group_id) do nothing;

insert into content.faq_group_description (group_description_id, group_id, language_code, name)
select 4100 + store_no, 4000 + store_no, 'en', 'Shipping' from generate_series(1, 4) store_no
on conflict (group_description_id) do nothing;

insert into content.content_faq (content_id, group_id, position)
select 1000 + store_no * 100 + 4, 4000 + store_no, 0 from generate_series(1, 4) store_no
on conflict (content_id) do nothing;

with stores(store_no, store_id) as (
    values (1, '65f023632bc46470c104b76f'),
           (2, '65f023632bc46470c104b75f'),
           (3, '65f023632bc26470c104b75f'),
           (4, '65f020632bc46470c104b76f')
)
insert into content.content_menu (content_id, store_merchant_id, handle)
select 1000 + store_no * 100 + 5, store_id, 'main' from stores
on conflict (content_id) do nothing;

insert into content.menu_item (menu_item_id, menu_content_id, position, target_kind, target_value,
                               open_new_tab, visible, login_required)
select 5000 + store_no, 1000 + store_no * 100 + 5, 0, 'URL', '/about', false, true, false
from generate_series(1, 4) store_no
on conflict (menu_item_id) do nothing;

insert into content.menu_item_description (menu_item_id, language_code, label)
select 5000 + store_no, 'en', 'About' from generate_series(1, 4) store_no
on conflict (menu_item_id, language_code) do nothing;

with stores(store_no, store_id) as (
    values (1, '65f023632bc46470c104b76f'),
           (2, '65f023632bc46470c104b75f'),
           (3, '65f023632bc26470c104b75f'),
           (4, '65f020632bc46470c104b76f')
)
insert into content.content_policy (content_id, store_merchant_id, policy_type, policy_version,
                                    effective_date, acceptance_required, jurisdiction, active)
select 1000 + store_no * 100 + 6, store_id, 'PRIVACY', '1.0', current_date, true, 'SA', true from stores
on conflict (content_id) do nothing;

insert into content.policy_display_location (policy_content_id, display_location)
select 1000 + store_no * 100 + 6, 'FOOTER' from generate_series(1, 4) store_no
on conflict (policy_content_id, display_location) do nothing;

update content.sm_sequencer set seq_count = greatest(seq_count, 10000);
