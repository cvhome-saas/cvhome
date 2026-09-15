-- Runs last, in every profile: each sequence starts above the highest id its table holds (the seeds write their own
-- ids) and never moves backwards, since another task of this service may already hold a block of ids.
select setval('checkout.cart_seq', greatest((select coalesce(max(cart_id), 0) + 1 from checkout.cart),
    (select case when is_called then last_value + 50 else last_value end from checkout.cart_seq)), false);
select setval('checkout.cart_line_seq', greatest((select coalesce(max(line_id), 0) + 1 from checkout.cart_line),
    (select case when is_called then last_value + 50 else last_value end from checkout.cart_line_seq)), false);
select setval('checkout.customer_account_seq', greatest((select coalesce(max(customer_id), 0) + 1 from checkout.customer_account),
    (select case when is_called then last_value + 50 else last_value end from checkout.customer_account_seq)), false);
select setval('checkout.sales_order_seq', greatest((select coalesce(max(order_id), 0) + 1 from checkout.sales_order),
    (select case when is_called then last_value + 50 else last_value end from checkout.sales_order_seq)), false);
select setval('checkout.sales_order_event_seq', greatest((select coalesce(max(event_id), 0) + 1 from checkout.sales_order_event),
    (select case when is_called then last_value + 50 else last_value end from checkout.sales_order_event_seq)), false);
select setval('checkout.sales_order_line_seq', greatest((select coalesce(max(line_id), 0) + 1 from checkout.sales_order_line),
    (select case when is_called then last_value + 50 else last_value end from checkout.sales_order_line_seq)), false);
select setval('checkout.sales_order_line_option_seq', greatest((select coalesce(max(option_id), 0) + 1 from checkout.sales_order_line_option),
    (select case when is_called then last_value + 50 else last_value end from checkout.sales_order_line_option_seq)), false);
select setval('checkout.sales_order_history_seq', greatest((select coalesce(max(history_id), 0) + 1 from checkout.sales_order_history),
    (select case when is_called then last_value + 50 else last_value end from checkout.sales_order_history_seq)), false);
select setval('checkout.sales_order_total_seq', greatest((select coalesce(max(total_id), 0) + 1 from checkout.sales_order_total),
    (select case when is_called then last_value + 50 else last_value end from checkout.sales_order_total_seq)), false);
