INSERT INTO subscription.subscription(id, subscription_plan, recurring_plan, created_date, last_renewed_date, end_date,
                                      de_activated_date,
                                      subscription_status,
                                      version)
VALUES ('21f023932bc66470c104b76f', 'FREE', 'MONTH', CURRENT_DATE, CURRENT_DATE, CURRENT_DATE + INTERVAL '60 DAY',
        null,
        'ACTIVE', 1)
ON CONFLICT DO NOTHING;

INSERT INTO subscription.subscription(id, subscription_plan, recurring_plan, created_date, last_renewed_date, end_date,
                                      de_activated_date,
                                      subscription_status,
                                      version)
VALUES ('352023632b046970c104b76f', 'FREE', 'MONTH', CURRENT_DATE, CURRENT_DATE, CURRENT_DATE + INTERVAL '60 DAY',
        null,
        'ACTIVE', 1)
ON CONFLICT DO NOTHING;


INSERT INTO subscription.subscription_price_plan (id, product_id, currency, price,
                                                  subscription_plan, recurring_plan, version)
VALUES ('price_1QkShh2M1P3uzrFi3x8bNMl5', 'prod_RdkCpLlZWkaeLN',
        'usd', 500, 'LIMITED', 'MONTH', 0)
ON CONFLICT DO NOTHING;

INSERT INTO subscription.subscription_price_plan (id, product_id, currency, price,
                                                  subscription_plan, recurring_plan, version)
VALUES ('price_1QkSiH2M1P3uzrFiajX6cPK6', 'prod_RdkCpLlZWkaeLN',
        'usd', 5000, 'LIMITED', 'YEAR', 0)
ON CONFLICT DO NOTHING;

INSERT INTO subscription.subscription_price_plan (id, product_id, currency, price,
                                                  subscription_plan, recurring_plan, version)
VALUES ('price_1QjSw92M1P3uzrFileCPnuDE', 'prod_RciMYQlpPwZFsk',
        'usd', 1000, 'BASIC', 'MONTH', 0)
ON CONFLICT DO NOTHING;

INSERT INTO subscription.subscription_price_plan (id, product_id, currency, price,
                                                  subscription_plan, recurring_plan, version)
VALUES ('price_1QjSwA2M1P3uzrFijQigb0FM', 'prod_RciMYQlpPwZFsk',
        'usd', 10000, 'BASIC', 'YEAR', 0)
ON CONFLICT DO NOTHING;

INSERT INTO subscription.subscription_price_plan (id, product_id, currency, price,
                                                  subscription_plan, recurring_plan, version)
VALUES ('price_1QjSwB2M1P3uzrFiR3CLZ9ES', 'prod_RciMMVBhMGdxTw',
        'usd', 3000, 'PERFORMANCE', 'MONTH', 0)
ON CONFLICT DO NOTHING;

INSERT INTO subscription.subscription_price_plan (id, product_id, currency, price,
                                                  subscription_plan, recurring_plan, version)
VALUES ('price_1QjSwC2M1P3uzrFiVBSX2S0t', 'prod_RciMMVBhMGdxTw',
        'usd', 30000, 'PERFORMANCE', 'YEAR', 0)
ON CONFLICT DO NOTHING;

