-- Redirects for paths that moved.
--
-- Demo content for the test store 65f023632bc46470c104b76f (Riyadh Fashion Hub), in every language the store sells in.
-- Generated seed: ids are negative on purpose — content, media and menu ids all come from sequences that
-- only grow upward, so a seed row below zero can never collide with one the running service creates.

INSERT INTO content.redirect (id, store_merchant_id, from_path, to_path, created_at)
VALUES (-401, '65f023632bc46470c104b76f', '/content/sizes', '/content/size-guide', now())
on conflict (id) do nothing;
INSERT INTO content.redirect (id, store_merchant_id, from_path, to_path, created_at)
VALUES (-402, '65f023632bc46470c104b76f', '/content/care', '/content/fabric-and-care', now())
on conflict (id) do nothing;
INSERT INTO content.redirect (id, store_merchant_id, from_path, to_path, created_at)
VALUES (-403, '65f023632bc46470c104b76f', '/blog/riyadh-summer', '/blog/dressing-for-riyadh-summer', now())
on conflict (id) do nothing;
