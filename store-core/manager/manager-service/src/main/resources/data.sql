insert into manager.manager_store(id, name, created_date, country, email, phone, owner_id, synced_in_router, synced_in_store)
VALUES ('65f023632bc46470c104b76f', 'default', current_date, 'EG', 'mail@example.com','+201120266849',
        'd1952c95-312e-4bb9-9a2d-b703d031276f', true, true)
on conflict do nothing;


insert into manager.reference_alis(id, domain, reference_id)
values ('65f023632bc46470c204b75f', 'ashraf.com', '65f023632bc46470c104b76f')
on conflict do nothing;

insert into manager.reference_alis(id, domain, reference_id)
values ('65f023632bc46470c204b25f', 'ali.com', '65f023632bc46470c104b76f')
on conflict do nothing;

insert into manager.reference_alis(id, domain, reference_id)
values ('65f023632bc16470c204b25f', 'ashraf.onecvhome.click', '65f023632bc46470c104b76f')
on conflict do nothing;

insert into manager.reference_alis(id, domain, reference_id)
values ('65f023632bc16410c204b25f', 'localhost', '65f023632bc46470c104b76f')
on conflict do nothing;

insert into manager.reference_alis(id, domain, reference_id)
values ('65f023632b416410c204b25f', '127.0.0.1', '65f023632bc46470c104b76f')
on conflict do nothing;

insert into manager.reference_alis(id, domain, reference_id)
values ('65f023632b416410c203b25f', 'me.asrevo.com', '65f023632bc46470c104b76f')
on conflict do nothing;
