-- Redirects for paths that moved.
--
-- Demo content for the test store 65f020632bc46470c104b76f (Beauté Élégante Paris), in every language the store sells in.
-- Generated seed: ids are negative on purpose — content, media and menu ids all come from sequences that
-- only grow upward, so a seed row below zero can never collide with one the running service creates.

INSERT INTO content.redirect (id, store_merchant_id, from_path, to_path, created_at)
VALUES (-101, '65f020632bc46470c104b76f', '/content/livraison', '/content/livraison-et-retours', now())
on conflict (id) do nothing;
INSERT INTO content.redirect (id, store_merchant_id, from_path, to_path, created_at)
VALUES (-102, '65f020632bc46470c104b76f', '/content/ingredients', '/content/nos-ingredients', now())
on conflict (id) do nothing;
INSERT INTO content.redirect (id, store_merchant_id, from_path, to_path, created_at)
VALUES (-103, '65f020632bc46470c104b76f', '/blog/routine-du-soir', '/blog/routine-du-soir-en-quatre-gestes', now())
on conflict (id) do nothing;
