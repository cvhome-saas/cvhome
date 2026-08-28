-- Home-page sections, one of every kind.
--
-- Demo content for the test store 65f023632bc46470c104b76f (Riyadh Fashion Hub), in every language the store sells in.
-- Generated seed: ids are negative on purpose — content, media and menu ids all come from sequences that
-- only grow upward, so a seed row below zero can never collide with one the running service creates.

-- section: home-banner-ref (BANNER_REF)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-40037, now(), now(), 'seed', 'home-banner-ref', 'SECTION', 0, true, '65f023632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "BANNER_REF", "targetValue": "free-alterations", "mediaId": null, "itemLimit": null, "layout": null, "cta": null}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400078, now(), now(), 'seed', null, 'هذا الأسبوع', 'هذا الأسبوع', null, null, null, null, 'ar', -40037, 'TRANSLATED', null, null, null, 'البانر المعروض حاليًا.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400079, now(), now(), 'seed', null, 'This week', 'This week', null, null, null, null, 'en', -40037, 'TRANSLATED', null, null, null, 'The banner currently live.')
on conflict (description_id) do nothing;

-- section: home-featured (PRODUCT_GROUP)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-40038, now(), now(), 'seed', 'home-featured', 'SECTION', 1, true, '65f023632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "PRODUCT_GROUP", "targetValue": "FEATURED_ITEMS", "mediaId": null, "itemLimit": 8, "layout": "grid", "cta": null}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400080, now(), now(), 'seed', null, 'مختارات الموسم', 'مختارات الموسم', null, null, null, null, 'ar', -40038, 'TRANSLATED', null, null, null, 'اختيار فريق المعرض هذا الأسبوع.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400081, now(), now(), 'seed', null, 'This season''s picks', 'This season''s picks', null, null, null, null, 'en', -40038, 'TRANSLATED', null, null, null, 'Chosen this week by the store team.')
on conflict (description_id) do nothing;

-- section: home-categories (CATEGORY_GRID)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-40039, now(), now(), 'seed', 'home-categories', 'SECTION', 2, true, '65f023632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "CATEGORY_GRID", "targetValue": "WOMEN", "mediaId": null, "itemLimit": 6, "layout": "grid", "cta": null}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400082, now(), now(), 'seed', null, 'تسوّقي حسب القسم', 'تسوّقي حسب القسم', null, null, null, null, 'ar', -40039, 'TRANSLATED', null, null, null, 'فساتين، قمصان، أحذية.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400083, now(), now(), 'seed', null, 'Shop by department', 'Shop by department', null, null, null, null, 'en', -40039, 'TRANSLATED', null, null, null, 'Dresses, tops, shoes.')
on conflict (description_id) do nothing;

-- section: home-new (PRODUCT_GROUP)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-40040, now(), now(), 'seed', 'home-new', 'SECTION', 3, true, '65f023632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "PRODUCT_GROUP", "targetValue": "NEWLY_ADDED", "mediaId": null, "itemLimit": 8, "layout": "carousel", "cta": null}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400084, now(), now(), 'seed', null, 'وصل حديثًا', 'وصل حديثًا', null, null, null, null, 'ar', -40040, 'TRANSLATED', null, null, null, 'آخر ما دخل المعرض.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400085, now(), now(), 'seed', null, 'New arrivals', 'New arrivals', null, null, null, null, 'en', -40040, 'TRANSLATED', null, null, null, 'The latest into the store.')
on conflict (description_id) do nothing;

-- section: home-fit-promise (RICH_TEXT)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-40041, now(), now(), 'seed', 'home-fit-promise', 'SECTION', 4, true, '65f023632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "RICH_TEXT", "targetValue": null, "mediaId": null, "itemLimit": null, "layout": null, "cta": {"kind": "PAGE", "value": "size-guide", "broken": null}}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400086, now(), now(), 'seed', '<p>التعديل مجاني على أي قطعة من المعرض، لأن القطعة التي تناسبك فعلًا لا تعود إلينا مرتجعة — ولا تبقى في خزانتك دون أن تُلبس.</p>', 'المقاس الصحيح، لا المقاس القريب', 'المقاس الصحيح، لا المقاس القريب', null, null, null, null, 'ar', -40041, 'TRANSLATED', null, null, 'دليل المقاسات', 'تعهّدنا في سطر واحد.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400087, now(), now(), 'seed', '<p>Alterations are free on anything bought in store, because a garment that actually fits does not come back as a return — and does not sit unworn in a wardrobe either.</p>', 'The right size, not the near-enough size', 'The right size, not the near-enough size', null, null, null, null, 'en', -40041, 'TRANSLATED', null, null, 'Size guide', 'Our promise in one line.')
on conflict (description_id) do nothing;

-- section: home-full-catalogue (PRODUCT_GROUP)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-40042, now(), now(), 'seed', 'home-full-catalogue', 'SECTION', 5, true, '65f023632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "PRODUCT_GROUP", "targetValue": "HOME_PAGE", "mediaId": null, "itemLimit": 24, "layout": "grid", "cta": null}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400088, now(), now(), 'seed', null, 'كل المعروض', 'كل المعروض', null, null, null, null, 'ar', -40042, 'TRANSLATED', null, null, null, 'المجموعة كاملة، رجال ونساء وأطفال.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400089, now(), now(), 'seed', null, 'The whole range', 'The whole range', null, null, null, null, 'en', -40042, 'TRANSLATED', null, null, null, 'The full collection — men, women and kids.')
on conflict (description_id) do nothing;

-- section: home-image-atelier (IMAGE)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-40043, now(), now(), 'seed', 'home-image-atelier', 'SECTION', 6, true, '65f023632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "IMAGE", "targetValue": null, "mediaId": -490005, "itemLimit": null, "layout": null, "cta": {"kind": "URL", "value": "/blog/inside-the-alterations-room", "broken": null}}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400090, now(), now(), 'seed', null, 'غرفة التعديل', 'غرفة التعديل', null, null, null, null, 'ar', -40043, 'TRANSLATED', null, null, 'ادخل الغرفة', 'ثلاث ماكينات، خياطان، أربعون قطعة كل أسبوع.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400091, now(), now(), 'seed', null, 'The alterations room', 'The alterations room', null, null, null, null, 'en', -40043, 'TRANSLATED', null, null, 'Step inside', 'Three machines, two tailors, forty garments a week.')
on conflict (description_id) do nothing;
INSERT INTO content.media_usage (id, asset_id, owner_kind, owner_ref, owner_title, content_id, content_type, field)
VALUES (-4011, -490005, 'CONTENT', '-40043', 'غرفة التعديل', -40043, 'SECTION', 'image')
on conflict (id) do nothing;

-- section: home-recommended (PRODUCT_GROUP)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-40044, now(), now(), 'seed', 'home-recommended', 'SECTION', 7, true, '65f023632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "PRODUCT_GROUP", "targetValue": "RECOMMENDED", "mediaId": null, "itemLimit": 12, "layout": "carousel", "cta": null}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400092, now(), now(), 'seed', null, 'مقترحة لك', 'مقترحة لك', null, null, null, null, 'ar', -40044, 'TRANSLATED', null, null, null, 'بناءً على ما ينصح به فريق المعرض.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400093, now(), now(), 'seed', null, 'Recommended for you', 'Recommended for you', null, null, null, null, 'en', -40044, 'TRANSLATED', null, null, null, 'Based on what the store team suggests most.')
on conflict (description_id) do nothing;

-- section: home-magazine (POST_FEED)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-40045, now(), now(), 'seed', 'home-magazine', 'SECTION', 8, true, '65f023632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "POST_FEED", "targetValue": null, "mediaId": null, "itemLimit": 3, "layout": null, "cta": {"kind": "BLOG_INDEX", "value": null, "broken": null}}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400094, now(), now(), 'seed', null, 'المجلة', 'المجلة', null, null, null, null, 'ar', -40045, 'TRANSLATED', null, null, 'كل المقالات', 'إطلالات وأقمشة وما خلف الخياطة.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400095, now(), now(), 'seed', null, 'The Magazine', 'The Magazine', null, null, null, null, 'en', -40045, 'TRANSLATED', null, null, 'All articles', 'Style, fabric, and what happens behind the seams.')
on conflict (description_id) do nothing;

-- section: home-faq (FAQ_REF)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-40046, now(), now(), 'seed', 'home-faq', 'SECTION', 9, true, '65f023632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "FAQ_REF", "targetValue": "returns", "mediaId": null, "itemLimit": null, "layout": null, "cta": {"kind": "FAQ_INDEX", "value": null, "broken": null}}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400096, now(), now(), 'seed', null, 'الاستبدال والاسترجاع باختصار', 'الاستبدال والاسترجاع باختصار', null, null, null, null, 'ar', -40046, 'TRANSLATED', null, null, 'كل المساعدة', 'أكثر ثلاثة أسئلة تكرارًا.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400097, now(), now(), 'seed', null, 'Returns & exchanges, briefly', 'Returns & exchanges, briefly', null, null, null, null, 'en', -40046, 'TRANSLATED', null, null, 'All help', 'The three questions we get most.')
on conflict (description_id) do nothing;

