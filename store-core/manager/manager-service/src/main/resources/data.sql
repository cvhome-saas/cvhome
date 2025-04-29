INSERT INTO manager.manager_org(id, created_date, email, version)
VALUES ('21f023932bc66470c104b76f', CURRENT_DATE, 'mail@example.com', 1)
ON CONFLICT DO NOTHING;

INSERT INTO manager.manager_org(id, created_date, email, version)
VALUES ('352023632b046970c104b76f', CURRENT_DATE, 'mail@example.com', 1)
ON CONFLICT DO NOTHING;

INSERT INTO manager.manager_store(id, name, created_date, org_id, pod_id, provisioning_state, version)
VALUES ('65f023632bc46470c104b76f', 'ORG1-STORE1', CURRENT_DATE, '21f023932bc66470c104b76f', '1',
        'SUCCESSFULLY_PROVISIONING', 1)
ON CONFLICT DO NOTHING;

INSERT INTO manager.manager_store(id, name, created_date, org_id, pod_id, provisioning_state, version)
VALUES ('65f023632bc46470c104b75f', 'ORG1-STORE2', CURRENT_DATE, '21f023932bc66470c104b76f', '1',
        'SUCCESSFULLY_PROVISIONING', 1)
ON CONFLICT DO NOTHING;

INSERT INTO manager.manager_store(id, name, created_date, org_id, pod_id, provisioning_state, version)
VALUES ('65f020632bc46470c104b76f', 'ORG2-STORE1', CURRENT_DATE, '352023632b046970c104b76f', '1',
        'SUCCESSFULLY_PROVISIONING', 1)
ON CONFLICT DO NOTHING;

INSERT INTO manager.manager_store(id, name, created_date, org_id, pod_id, provisioning_state, version)
VALUES ('65f023632bc26470c104b75f', 'ORG2-STORE2', CURRENT_DATE, '352023632b046970c104b76f', '1',
        'SUCCESSFULLY_PROVISIONING', 1)
ON CONFLICT DO NOTHING;


INSERT INTO manager.manager_store_domain(domain, domain_type, manager_store_id)
VALUES ('org1-store1.asrevo.com', 'CUSTOM_DOMAIN', '65f023632bc46470c104b76f')
ON CONFLICT DO NOTHING;

INSERT INTO manager.manager_store_domain(domain, domain_type, manager_store_id)
VALUES ('org1-store1', 'SUB_DOMAIN', '65f023632bc46470c104b76f')
ON CONFLICT DO NOTHING;


INSERT INTO manager.manager_store_domain(domain, domain_type, manager_store_id)
VALUES ('org1-store2.asrevo.com', 'CUSTOM_DOMAIN', '65f023632bc46470c104b75f')
ON CONFLICT DO NOTHING;

INSERT INTO manager.manager_store_domain(domain, domain_type, manager_store_id)
VALUES ('org1-store2', 'SUB_DOMAIN', '65f023632bc46470c104b75f')
ON CONFLICT DO NOTHING;


INSERT INTO manager.manager_store_domain(domain, domain_type, manager_store_id)
VALUES ('org2-store1.asrevo.com', 'CUSTOM_DOMAIN', '65f020632bc46470c104b76f')
ON CONFLICT DO NOTHING;

INSERT INTO manager.manager_store_domain(domain, domain_type, manager_store_id)
VALUES ('org2-store1', 'SUB_DOMAIN', '65f020632bc46470c104b76f')
ON CONFLICT DO NOTHING;


INSERT INTO manager.manager_store_domain(domain, domain_type, manager_store_id)
VALUES ('org2-store2.asrevo.com', 'CUSTOM_DOMAIN', '65f023632bc26470c104b75f')
ON CONFLICT DO NOTHING;

INSERT INTO manager.manager_store_domain(domain, domain_type, manager_store_id)
VALUES ('org2-store2', 'SUB_DOMAIN', '65f023632bc26470c104b75f')
ON CONFLICT DO NOTHING;
