/*
Generated SQL inserts for a new store based on the original template.
Store ID: '65f023632bc46470c104b75f'
Languages: ['en', 'fr']
Country: 'USA'
Domain: electronics
*/

-- Define parameters for clarity (These would typically be replaced by actual values or generator logic)
-- Let's assume:
-- $paramter.store_id = '65f023632bc46470c104b75f'
-- $paramter.country_id = 'USA'
-- $parameter.languages = ['en', 'fr']
-- $generator.currencyForCountry('USA') resolves to the currency_id for USD (e.g., 1 - assuming this ID exists)
-- New Org ID generated: '41a034a43cd77581d105c87b'

INSERT INTO merchant.merchant_store (store_merchant_id, currency_format_national, in_business_since, org, theme, color_theme, seizeunitcode, store_email, store_address, store_city, store_name, store_phone, store_postal_code, store_state_prov, use_cache, require_login_for_order_placement, weightunitcode, country_id, currency_id, language_code)
VALUES ('65f023632bc46470c104b75f', false, '2024-04-01', '21f023932bc66470c104b76f', 'BASIC', 'DEFAULT', 'IN', 'info@usaelectronics.com', '456 Tech Avenue', 'New York', 'USA-Electronics-Hub', '+1-212-555-0123', '10001', 'NY', false, true, 'LB', 'USA', 'USD', 'en') -- Using 'en' as the first language
on conflict (store_merchant_id) do nothing;
-- Specify conflict target for clarity

-- Loop over languages: 'en', 'fr'
INSERT INTO merchant.merchant_language (store_merchant_id, language_code)
VALUES ('65f023632bc46470c104b75f', 'en')
on conflict (store_merchant_id, language_code) do nothing; -- Specify conflict target

INSERT INTO merchant.merchant_language (store_merchant_id, language_code)
VALUES ('65f023632bc46470c104b75f', 'fr')
on conflict (store_merchant_id, language_code) do nothing;

INSERT INTO merchant.store_domains(domain, domain_type, store_merchant_id)
VALUES ('org1-store2.asrevo.com', 'CUSTOM_DOMAIN', '65f023632bc46470c104b75f')
ON CONFLICT DO NOTHING;

INSERT INTO merchant.store_domains(domain, domain_type, store_merchant_id)
VALUES ('org1-store2', 'SUB_DOMAIN', '65f023632bc46470c104b75f')
ON CONFLICT DO NOTHING;