insert into manager.manager_store(id, name, created_date, country, email, owner_id, synced_in_router, synced_in_store)
VALUES ('65f023632bc46470c104b76f', 'ashraf', current_date, 'EG', 'mail@example.com', 'system', true, true)
on conflict do nothing;