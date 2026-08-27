-- Redirects for paths that moved.
--
-- Demo content for the test store 65f023632bc26470c104b75f (Egypt Car Sales), in every language the store sells in.
-- Generated seed: ids are negative on purpose — content, media and menu ids all come from sequences that
-- only grow upward, so a seed row below zero can never collide with one the running service creates.

INSERT INTO content.redirect (id, store_merchant_id, from_path, to_path, created_at)
VALUES (-201, '65f023632bc26470c104b75f', '/content/finance', '/content/financing', now())
on conflict (id) do nothing;
INSERT INTO content.redirect (id, store_merchant_id, from_path, to_path, created_at)
VALUES (-202, '65f023632bc26470c104b75f', '/content/inspection', '/content/inspection-report', now())
on conflict (id) do nothing;
INSERT INTO content.redirect (id, store_merchant_id, from_path, to_path, created_at)
VALUES (-203, '65f023632bc26470c104b75f', '/blog/used-car-checks', '/blog/seven-checks-before-buying-used', now())
on conflict (id) do nothing;
