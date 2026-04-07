-- Insert initial manager organizations
INSERT INTO manager.manager_org(id, created_date, email, version)
VALUES ('21f023932bc66470c104b76f', CURRENT_DATE, 'mail@example.com', 1)
ON CONFLICT DO NOTHING;

INSERT INTO manager.manager_org(id, created_date, email, version)
VALUES ('352023632b046970c104b76f', CURRENT_DATE, 'mail@example.com', 1)
ON CONFLICT DO NOTHING;

-- Insert initial manager stores
INSERT INTO manager.manager_store(id, name, created_date, org_id, pod_id, provisioning_state, version)
VALUES ('65f023632bc46470c104b76f', 'ORG1-STORE1', CURRENT_DATE, '21f023932bc66470c104b76f', '507f1f77bcf86cd799439011',
        'SUCCESSFULLY_PROVISIONING', 1)
ON CONFLICT DO NOTHING;

INSERT INTO manager.manager_store(id, name, created_date, org_id, pod_id, provisioning_state, version)
VALUES ('65f023632bc46470c104b75f', 'ORG1-STORE2', CURRENT_DATE, '21f023932bc66470c104b76f', '507f1f77bcf86cd799439011',
        'SUCCESSFULLY_PROVISIONING', 1)
ON CONFLICT DO NOTHING;

INSERT INTO manager.manager_store(id, name, created_date, org_id, pod_id, provisioning_state, version)
VALUES ('65f020632bc46470c104b76f', 'ORG2-STORE1', CURRENT_DATE, '352023632b046970c104b76f', '507f1f77bcf86cd799439011',
        'SUCCESSFULLY_PROVISIONING', 1)
ON CONFLICT DO NOTHING;

INSERT INTO manager.manager_store(id, name, created_date, org_id, pod_id, provisioning_state, version)
VALUES ('65f023632bc26470c104b75f', 'ORG2-STORE2', CURRENT_DATE, '352023632b046970c104b76f', '507f1f77bcf86cd799439011',
        'SUCCESSFULLY_PROVISIONING', 1)
ON CONFLICT DO NOTHING;

-- Insert initial subscriptions
INSERT INTO subscription.subscription(id, subscription_plan, recurring_plan, created_date, last_renewed_date, end_date,
                                       de_activated_date, subscription_status, version)
VALUES ('21f023932bc66470c104b76f', 'FREE', 'MONTH', CURRENT_DATE, CURRENT_DATE, CURRENT_DATE + INTERVAL '60 DAY',
        null, 'ACTIVE', 1)
ON CONFLICT DO NOTHING;

INSERT INTO subscription.subscription(id, subscription_plan, recurring_plan, created_date, last_renewed_date, end_date,
                                       de_activated_date, subscription_status, version)
VALUES ('352023632b046970c104b76f', 'FREE', 'MONTH', CURRENT_DATE, CURRENT_DATE, CURRENT_DATE + INTERVAL '60 DAY',
        null, 'ACTIVE', 1)
ON CONFLICT DO NOTHING;

-- Insert initial pod
INSERT INTO org.pod(id, name, endpoint, endpoint_type, org_id, version)
VALUES ('507f1f77bcf86cd799439011', 'pod-507f1f77', 'http://spg-507f1f77.gateway.com', 'EXTERNAL', null, 1)
ON CONFLICT DO NOTHING;

