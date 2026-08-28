-- Redirects for paths that moved.
--
-- Demo content for the test store 65f023632bc46470c104b75f (USA Electronics Hub), in every language the store sells in.
-- Generated seed: ids are negative on purpose — content, media and menu ids all come from sequences that
-- only grow upward, so a seed row below zero can never collide with one the running service creates.

INSERT INTO content.redirect (id, store_merchant_id, from_path, to_path, created_at)
VALUES (-301, '65f023632bc46470c104b75f', '/content/guarantee', '/content/warranty', now())
on conflict (id) do nothing;
INSERT INTO content.redirect (id, store_merchant_id, from_path, to_path, created_at)
VALUES (-302, '65f023632bc46470c104b75f', '/content/tradein', '/content/trade-in-program', now())
on conflict (id) do nothing;
INSERT INTO content.redirect (id, store_merchant_id, from_path, to_path, created_at)
VALUES (-303, '65f023632bc46470c104b75f', '/blog/laptop-thermals', '/blog/laptop-thermals-what-reviews-miss', now())
on conflict (id) do nothing;
