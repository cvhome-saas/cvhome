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

