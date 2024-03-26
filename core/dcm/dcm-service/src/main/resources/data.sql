insert into dcm.owner(id, email)
VALUES ('system', 'mail@example.com')
on conflict do nothing;