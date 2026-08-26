/*
Generated SQL inserts similar to the provided template
where languages=['fr','en'] and store_id='65f020632bc46470c104b76f' and country_id='FR'
domain for this store is beauté
*/

-- Insert into merchant.merchant_store`
INSERT INTO merchant.merchant_store (store_merchant_id, currency_format_national, in_business_since, org, theme, color_theme, seizeunitcode, store_email, store_address, store_city, store_name, store_phone, store_postal_code, store_state_prov, use_cache, require_login_for_order_placement, weightunitcode, country_id, currency_id, language_code)
VALUES ('65f020632bc46470c104b76f', false, '2024-03-31', '352023632b046970c104b76f', 'JEWELERY', 'DEFAULT', 'CM', -- Changed seizeunitcode to CM
        'contact@beaute-elegante.fr', '15 Rue de la Paix', 'Paris', 'Beauté-Élégante-Paris', '+33 1 23 45 67 89', '75002', 'Île-de-France', false, true, 'KG', 'FR', 'EUR', 'fr') -- Changed weightunitcode to KG, set currency to EUR, first language to fr
on conflict (store_merchant_id) do nothing;

-- Insert into merchant.merchant_language for 'fr' and 'en'
INSERT INTO merchant.merchant_language (store_merchant_id, language_code)
VALUES ('65f020632bc46470c104b76f', 'fr')
on conflict (store_merchant_id, language_code) do nothing;

INSERT INTO merchant.merchant_language (store_merchant_id, language_code)
VALUES ('65f020632bc46470c104b76f', 'en')
on conflict (store_merchant_id, language_code) do nothing;

INSERT INTO merchant.store_domains(domain, domain_type, store_merchant_id)
VALUES ('org2-store1.asrevo.com', 'CUSTOM_DOMAIN', '65f020632bc46470c104b76f')
ON CONFLICT DO NOTHING;

INSERT INTO merchant.store_domains(domain, domain_type, store_merchant_id)
VALUES ('org2-store1', 'SUB_DOMAIN', '65f020632bc46470c104b76f')
ON CONFLICT DO NOTHING;