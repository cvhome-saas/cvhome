/*
can you generate another sql inserts similar to this
where languages=['en','fr'] and store_id='65f023632bc46470c104b75f' and country_id='USA'
domain for this store is electronics
*/
INSERT INTO merchant.merchant_store (store_merchant_id, currency_format_national, in_business_since, org, theme,
                                     color_theme, seizeunitcode, store_email, store_logo, store_banner, store_address,
                                     store_city, store_name, store_phone, store_postal_code, store_state_prov,
                                     use_cache, weightunitcode, country_id, currency_id, language_code)
VALUES ($paramter.store_id, false, '2024-03-31', '21f023932bc66470c104b76f', 'DEFAULT', 'LIGHT', 'IN',
           $generator.store_email(), 'logo.jpeg', 'banner.jpeg', $generator.address(), $generator.city(), $generator.store_name(), $generator.store_phone(),
           $generator.postal_code(), $generator.state(), false, 'LB', $paramter.country_id, $generator.currencyForCountry($paramter.country_id), $parameter.languages[0])
on conflict do nothing;

-- <<Begin Loop on $parameter.languages l
INSERT INTO merchant.merchant_language (store_merchant_id, language_code)
VALUES ($paramter.store_id, $each.l)
on conflict do nothing;
-- End Loop>>

-- <<Begin Loop on l in ('0','1','2','3','4')
INSERT INTO merchant.merchant_slider_images (store_merchant_id, priority, url)
VALUES ($paramter.store_id, $each.l, 'slide-'$each.l+1'.jpeg')
on conflict do nothing;
-- End Loop>>


-- <<Begin Loop on l in ('FACEBOOK','X','INSTAGRAM','TIKTOK')
INSERT INTO merchant.social_links (store_merchant_id, provider, url)
VALUES ($paramter.store_id, $each.l, $generator.url())
on conflict do nothing;
-- End Loop>>
