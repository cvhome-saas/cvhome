/*
can you generate another sql inserts similar to this
where languages=['ar','en'] and store_id='65f023632bc46470c104b76f' and country_id='SA'
domain for this store is fashion
*/
INSERT INTO merchant.merchant_store (store_merchant_id, currency_format_national, in_business_since, org, theme, color_theme, seizeunitcode, store_email, store_address, store_city, store_name, store_phone, store_postal_code, store_state_prov, use_cache, require_login_for_order_placement, weightunitcode, country_id, currency_id, language_code)
VALUES ('65f023632bc46470c104b76f', false, '2024-03-31', '21f023932bc66470c104b76f', 'BASIS', 'DEFAULT', 'CM', -- seizeunitcode for fashion
        'info@riyadhfashion.sa', '123 Olaya Street', 'Riyadh', 'Riyadh-Fashion-Hub', '+966 50 123 4567', '11564', 'Riyadh Province', false, true, 'KG', 'SA', 'SAR', 'ar')
on conflict (store_merchant_id) do nothing;

-- <<Begin Loop on $parameter.languages l
INSERT INTO merchant.merchant_language (store_merchant_id, language_code)
VALUES ('65f023632bc46470c104b76f', 'ar')
on conflict (store_merchant_id, language_code) do nothing;
INSERT INTO merchant.merchant_language (store_merchant_id, language_code)
VALUES ('65f023632bc46470c104b76f', 'en')
on conflict (store_merchant_id, language_code) do nothing;
-- End Loop>>

INSERT INTO merchant.store_domains(domain, domain_type, store_merchant_id)
VALUES ('org1-store1.asrevo.com', 'CUSTOM_DOMAIN', '65f023632bc46470c104b76f')
ON CONFLICT DO NOTHING;

INSERT INTO merchant.store_domains(domain, domain_type, store_merchant_id)
VALUES ('org1-store1', 'SUB_DOMAIN', '65f023632bc46470c104b76f')
ON CONFLICT DO NOTHING;