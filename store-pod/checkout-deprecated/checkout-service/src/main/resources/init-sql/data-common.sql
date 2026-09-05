INSERT INTO checkout.language (code, date_created, date_modified, updt_id, sort_order)
VALUES ('en', '2024-03-31 08:45:18.000000', '2024-03-31 08:45:18.000000', null, null)
on conflict do nothing;
INSERT INTO checkout.language (code, date_created, date_modified, updt_id, sort_order)
VALUES ('fr', '2024-03-31 08:45:18.000000', '2024-03-31 08:45:18.000000', null, null)
on conflict do nothing;
INSERT INTO checkout.language (code, date_created, date_modified, updt_id, sort_order)
VALUES ('ar', '2024-03-31 08:45:18.000000', '2024-03-31 08:45:18.000000', null, null)
on conflict do nothing;
INSERT INTO checkout.language (code, date_created, date_modified, updt_id, sort_order)
VALUES ('ru', '2024-03-31 08:45:18.000000', '2024-03-31 08:45:18.000000', null, null)
on conflict do nothing;

INSERT INTO checkout.currency (currency_code, currency_name, currency_supported)
VALUES ('EUR', 'EUR', true)
on conflict do nothing;
INSERT INTO checkout.currency (currency_code, currency_name, currency_supported)
VALUES ('EGP', 'EGP', true)
on conflict do nothing;
INSERT INTO checkout.currency (currency_code, currency_name, currency_supported)
VALUES ('USD', 'USD', true)
on conflict do nothing;
INSERT INTO checkout.currency (currency_code, currency_name, currency_supported)
VALUES ('RUB', 'RUB', true)
on conflict do nothing;


INSERT INTO checkout.country (country_isocode, country_supported, geozone_id)
VALUES ('FR', true, null)
on conflict do nothing;
INSERT INTO checkout.country (country_isocode, country_supported, geozone_id)
VALUES ('EG', true, null)
on conflict do nothing;
INSERT INTO checkout.country (country_isocode, country_supported, geozone_id)
VALUES ('RU', true, null)
on conflict do nothing;
INSERT INTO checkout.country (country_isocode, country_supported, geozone_id)
VALUES ('US', true, null)
on conflict do nothing;



INSERT INTO checkout.country_description (description_id, date_created, date_modified, updt_id, description, name,
                                          title,
                                          language_code, country_id)
VALUES (1, '2024-03-31 08:45:22.000000', '2024-03-31 08:45:22.000000', null, null, 'France', null, 'en', 'FR')
on conflict do nothing;
INSERT INTO checkout.country_description (description_id, date_created, date_modified, updt_id, description, name,
                                          title,
                                          language_code, country_id)
VALUES (2, '2024-03-31 08:45:22.000000', '2024-03-31 08:45:22.000000', null, null, 'France', null, 'fr', 'FR')
on conflict do nothing;
INSERT INTO checkout.country_description (description_id, date_created, date_modified, updt_id, description, name,
                                          title,
                                          language_code, country_id)
VALUES (3, '2024-03-31 08:45:22.000000', '2024-03-31 08:45:22.000000', null, null, 'Francia', null, 'ar', 'FR')
on conflict do nothing;
INSERT INTO checkout.country_description (description_id, date_created, date_modified, updt_id, description, name,
                                          title,
                                          language_code, country_id)
VALUES (4, '2024-03-31 08:45:22.000000', '2024-03-31 08:45:22.000000', null, null, 'Francia', null, 'ru', 'FR')
on conflict do nothing;



INSERT INTO checkout.country_description (description_id, date_created, date_modified, updt_id, description, name,
                                          title,
                                          language_code, country_id)
VALUES (5, '2024-03-31 08:45:21.000000', '2024-03-31 08:45:21.000000', null, null, 'Egypt', null, 'en', 'EG')
on conflict do nothing;
INSERT INTO checkout.country_description (description_id, date_created, date_modified, updt_id, description, name,
                                          title,
                                          language_code, country_id)
VALUES (6, '2024-03-31 08:45:21.000000', '2024-03-31 08:45:21.000000', null, null, 'Égypte', null, 'fr', 'EG')
on conflict do nothing;
INSERT INTO checkout.country_description (description_id, date_created, date_modified, updt_id, description, name,
                                          title,
                                          language_code, country_id)
VALUES (7, '2024-03-31 08:45:21.000000', '2024-03-31 08:45:21.000000', null, null, 'Egipto', null, 'ar', 'EG')
on conflict do nothing;
INSERT INTO checkout.country_description (description_id, date_created, date_modified, updt_id, description, name,
                                          title,
                                          language_code, country_id)
VALUES (8, '2024-03-31 08:45:21.000000', '2024-03-31 08:45:21.000000', null, null, 'Egipto', null, 'ru', 'EG')
on conflict do nothing;


INSERT INTO checkout.country_description (description_id, date_created, date_modified, updt_id, description, name,
                                          title,
                                          language_code, country_id)
VALUES (9, '2024-03-31 08:45:26.000000', '2024-03-31 08:45:26.000000', null, null, 'Russia', null, 'en', 'RU')
on conflict do nothing;
INSERT INTO checkout.country_description (description_id, date_created, date_modified, updt_id, description, name,
                                          title,
                                          language_code, country_id)
VALUES (10, '2024-03-31 08:45:26.000000', '2024-03-31 08:45:26.000000', null, null, 'Russie', null, 'fr', 'RU')
on conflict do nothing;
INSERT INTO checkout.country_description (description_id, date_created, date_modified, updt_id, description, name,
                                          title,
                                          language_code, country_id)
VALUES (11, '2024-03-31 08:45:26.000000', '2024-03-31 08:45:26.000000', null, null, 'Rusia', null, 'ar', 'RU')
on conflict do nothing;
INSERT INTO checkout.country_description (description_id, date_created, date_modified, updt_id, description, name,
                                          title,
                                          language_code, country_id)
VALUES (12, '2024-03-31 08:45:26.000000', '2024-03-31 08:45:26.000000', null, null, 'Rusia', null, 'ru', 'RU')
on conflict do nothing;

INSERT INTO checkout.country_description (description_id, date_created, date_modified, updt_id, description, name,
                                          title,
                                          language_code, country_id)
VALUES (13, '2024-03-31 08:45:29.000000', '2024-03-31 08:45:29.000000', null, null, 'United States', null, 'en', 'US')
on conflict do nothing;
INSERT INTO checkout.country_description (description_id, date_created, date_modified, updt_id, description, name,
                                          title,
                                          language_code, country_id)
VALUES (14, '2024-03-31 08:45:29.000000', '2024-03-31 08:45:29.000000', null, null, 'États-Unis', null, 'fr', 'US')
on conflict do nothing;
INSERT INTO checkout.country_description (description_id, date_created, date_modified, updt_id, description, name,
                                          title,
                                          language_code, country_id)
VALUES (15, '2024-03-31 08:45:29.000000', '2024-03-31 08:45:29.000000', null, null, 'Estados Unidos', null, 'ar', 'US')
on conflict do nothing;
INSERT INTO checkout.country_description (description_id, date_created, date_modified, updt_id, description, name,
                                          title,
                                          language_code, country_id)
VALUES (16, '2024-03-31 08:45:29.000000', '2024-03-31 08:45:29.000000', null, null, 'Estados Unidos', null, 'ru', 'US')
on conflict do nothing;



INSERT INTO checkout.sm_sequencer (seq_name, seq_count)
VALUES ('COUNTRY_DESCRIPTION_SEQ_NEXT_VAL', 16)
on conflict do nothing;
INSERT INTO checkout.sm_sequencer (seq_name, seq_count)
VALUES ('GEOZONE_SEQ_NEXT_VAL', 0)
on conflict do nothing;
INSERT INTO checkout.sm_sequencer (seq_name, seq_count)
VALUES ('GEOZONE_DESCRIPTION_SEQ_NEXT_VAL', 0)
on conflict do nothing;
INSERT INTO checkout.sm_sequencer (seq_name, seq_count)
VALUES ('ZONE_DESCRIPTION_SEQ_NEXT_VAL', 0)
on conflict do nothing;
