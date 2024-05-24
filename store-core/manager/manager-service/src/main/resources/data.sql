INSERT INTO manager.manager_store(id, name, created_date, country, email, phone, owner_id, synced_in_router, synced_in_store)
VALUES ('65f023632bc46470c104b76f', 'ORG1-STORE1', CURRENT_DATE, 'EG', 'mail@example.com', '+201120266849', 'd1952c95-312e-4bb9-9a2d-b703d031276f', TRUE, TRUE) ON CONFLICT DO NOTHING;

INSERT INTO manager.manager_store(id, name, created_date, country, email, phone, owner_id, synced_in_router, synced_in_store)
VALUES ('65f023632bc46470c104b75f', 'ORG1-STORE2', CURRENT_DATE, 'EG', 'mail@example.com', '+201120266849', 'd1952c95-312e-4bb9-9a2d-b703d031276f', TRUE, TRUE) ON CONFLICT DO NOTHING;

INSERT INTO manager.manager_store(id, name, created_date, country, email, phone, owner_id, synced_in_router, synced_in_store)
VALUES ('65f020632bc46470c104b76f', 'ORG2-STORE1', CURRENT_DATE, 'EG', 'mail@example.com', '+201120266849', 'd1952c95-312e-4bb6-9a2d-b703d031276f', TRUE, TRUE) ON CONFLICT DO NOTHING;

INSERT INTO manager.manager_store(id, name, created_date, country, email, phone, owner_id, synced_in_router, synced_in_store)
VALUES ('65f023632bc26470c104b75f', 'ORG2-STORE2', CURRENT_DATE, 'EG', 'mail@example.com', '+201120266849', 'd1952c95-312e-4bb6-9a2d-b703d031276f', TRUE, TRUE) ON CONFLICT DO NOTHING;


INSERT INTO manager.reference_alis(id, DOMAIN, reference_id)
VALUES ('65f023632bc46470c204b75f', 'org1-store1.gateway.com', '65f023632bc46470c104b76f') ON CONFLICT DO NOTHING;

INSERT INTO manager.reference_alis(id, DOMAIN, reference_id)
VALUES ('65f023632bc46470c204b25f', 'org1-store2.gateway.com', '65f023632bc46470c104b75f') ON CONFLICT DO NOTHING;

INSERT INTO manager.reference_alis(id, DOMAIN, reference_id)
VALUES ('65f023632bc16470c204b25f', 'org2-store1.gateway.com', '65f020632bc46470c104b76f') ON CONFLICT DO NOTHING;

INSERT INTO manager.reference_alis(id, DOMAIN, reference_id)
VALUES ('65f023632bc16410c204b25f', 'org2-store1.gateway.com', '65f023632bc26470c104b75f') ON CONFLICT DO NOTHING;
