insert into manager.manager_store(id, name, country, email, owner_id)
VALUES ('65f023632bc46470c104b76f', 'ashraf', 'EG', 'mail@example.com', 'system')
on conflict do nothing;