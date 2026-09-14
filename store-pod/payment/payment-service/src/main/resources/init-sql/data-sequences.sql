-- Runs last, in every profile: each sequence starts above the highest id its table holds (the seeds write their own
-- ids) and never moves backwards, since another task of this service may already hold a block of ids.
select setval('payment.transaction_seq', greatest((select coalesce(max(transaction_id), 0) + 1 from payment.transaction),
    (select case when is_called then last_value + 50 else last_value end from payment.transaction_seq)), false);
