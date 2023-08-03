INSERT INTO public.x_user (id, name, email)
VALUES ('1', 'jhon', 'jhon@email.com')
ON CONFLICT DO NOTHING;
