-- Runs last, in every profile: each sequence starts above the highest id its table holds (the seeds write their own
-- ids) and never moves backwards, since another task of this service may already hold a block of ids.
select setval('content.content_seq', greatest((select coalesce(max(content_id), 0) + 1 from content.content),
    (select case when is_called then last_value + 50 else last_value end from content.content_seq)), false);
select setval('content.content_description_seq', greatest((select coalesce(max(description_id), 0) + 1 from content.content_description),
    (select case when is_called then last_value + 50 else last_value end from content.content_description_seq)), false);
