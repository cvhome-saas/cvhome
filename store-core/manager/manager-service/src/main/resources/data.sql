insert into manager.manager_store(id, name, created_date, country, email, phone, owner_id, synced_in_router, synced_in_store)
VALUES ('65f023632bc46470c104b76f', 'default', current_date, 'EG', 'mail@example.com','+201120266849',
        'd1952c95-312e-4bb9-9a2d-b703d031276f', true, true)
on conflict do nothing;