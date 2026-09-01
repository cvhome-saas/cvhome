/*
Generated SQL inserts for a new store based on the original template.
Store ID: '65f023632bc26470c104b75f'
Languages: ['ar', 'fr']
Country: 'EG'
Domain: cars
*/

-- Define parameters for clarity (These would typically be replaced by actual values or generator logic)
-- Let's assume:
-- $paramter.store_id = '65f023632bc26470c104b75f'
-- $paramter.country_id = 'EG'
-- $parameter.languages = ['ar', 'fr']
-- $generator.currencyForCountry('EG') resolves to the currency_id for EGP (e.g., 10 - assuming this ID exists)
-- New Org ID generated: '32a034a43cd77581d105c87a'

INSERT INTO merchant.merchant_store (store_merchant_id, currency_format_national, in_business_since, org, theme, color_theme, seizeunitcode, store_email, store_address, store_city, store_name, store_phone, store_postal_code, store_state_prov, use_cache, require_login_for_order_placement, weightunitcode, country_id, currency_id, language_code)
VALUES ('65f023632bc26470c104b75f', false, '2024-04-01', '352023632b046970c104b76f', 'FURNITURE', 'DEFAULT', 'CM', -- Changed seizeunitcode slightly
        'info@egyptcarsales.com', '123 Auto Road', 'Cairo', 'Egypt-Car-Sales', '+201001234567', -- Cars domain reflected
        '11511', 'Cairo', false, true, 'KG', 'EG', 'EGP', 'ar') -- Using 'ar' as the first language
on conflict (store_merchant_id) do nothing;
-- Specify conflict target for clarity

-- Loop over languages: 'ar', 'fr'
INSERT INTO merchant.merchant_language (store_merchant_id, language_code)
VALUES ('65f023632bc26470c104b75f', 'ar')
on conflict (store_merchant_id, language_code) do nothing; -- Specify conflict target

INSERT INTO merchant.merchant_language (store_merchant_id, language_code)
VALUES ('65f023632bc26470c104b75f', 'fr')
on conflict (store_merchant_id, language_code) do nothing;

INSERT INTO merchant.store_domains(domain, domain_type, store_merchant_id)
VALUES ('org2-store2.asrevo.com', 'CUSTOM_DOMAIN', '65f023632bc26470c104b75f')
ON CONFLICT DO NOTHING;

INSERT INTO merchant.store_domains(domain, domain_type, store_merchant_id)
VALUES ('org2-store2', 'SUB_DOMAIN', '65f023632bc26470c104b75f')
ON CONFLICT DO NOTHING;