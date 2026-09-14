-- Runs last, in every profile: each sequence starts above the highest id its table holds (the seeds write their own
-- ids) and never moves backwards, since another task of this service may already hold a block of ids.
select setval('inventory.product_availability_seq', greatest((select coalesce(max(product_avail_id), 0) + 1 from inventory.product_availability),
    (select case when is_called then last_value + 50 else last_value end from inventory.product_availability_seq)), false);
select setval('inventory.product_price_seq', greatest((select coalesce(max(product_price_id), 0) + 1 from inventory.product_price),
    (select case when is_called then last_value + 50 else last_value end from inventory.product_price_seq)), false);
select setval('inventory.product_reservation_seq', greatest((select coalesce(max(id), 0) + 1 from inventory.product_reservation),
    (select case when is_called then last_value + 50 else last_value end from inventory.product_reservation_seq)), false);
select setval('inventory.product_reservation_line_seq', greatest((select coalesce(max(id), 0) + 1 from inventory.product_reservation_line),
    (select case when is_called then last_value + 50 else last_value end from inventory.product_reservation_line_seq)), false);
