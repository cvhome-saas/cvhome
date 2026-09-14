-- Runs last, in every profile: each sequence starts above the highest id its table holds (the seeds write their own
-- ids) and never moves backwards, since another task of this service may already hold a block of ids.
select setval('catalog.category_seq', greatest((select coalesce(max(category_id), 0) + 1 from catalog.category),
    (select case when is_called then last_value + 50 else last_value end from catalog.category_seq)), false);
select setval('catalog.category_description_seq', greatest((select coalesce(max(description_id), 0) + 1 from catalog.category_description),
    (select case when is_called then last_value + 50 else last_value end from catalog.category_description_seq)), false);
select setval('catalog.manufacturer_seq', greatest((select coalesce(max(manufacturer_id), 0) + 1 from catalog.manufacturer),
    (select case when is_called then last_value + 50 else last_value end from catalog.manufacturer_seq)), false);
select setval('catalog.manufacturer_description_seq', greatest((select coalesce(max(description_id), 0) + 1 from catalog.manufacturer_description),
    (select case when is_called then last_value + 50 else last_value end from catalog.manufacturer_description_seq)), false);
select setval('catalog.product_seq', greatest((select coalesce(max(product_id), 0) + 1 from catalog.product),
    (select case when is_called then last_value + 50 else last_value end from catalog.product_seq)), false);
select setval('catalog.product_description_seq', greatest((select coalesce(max(description_id), 0) + 1 from catalog.product_description),
    (select case when is_called then last_value + 50 else last_value end from catalog.product_description_seq)), false);
select setval('catalog.product_group_seq', greatest((select coalesce(max(product_group_id), 0) + 1 from catalog.product_group),
    (select case when is_called then last_value + 50 else last_value end from catalog.product_group_seq)), false);
select setval('catalog.product_group_description_seq', greatest((select coalesce(max(description_id), 0) + 1 from catalog.product_group_description),
    (select case when is_called then last_value + 50 else last_value end from catalog.product_group_description_seq)), false);
select setval('catalog.product_image_seq', greatest((select coalesce(max(product_image_id), 0) + 1 from catalog.product_image),
    (select case when is_called then last_value + 50 else last_value end from catalog.product_image_seq)), false);
select setval('catalog.product_option_seq', greatest((select coalesce(max(product_option_id), 0) + 1 from catalog.product_option),
    (select case when is_called then last_value + 50 else last_value end from catalog.product_option_seq)), false);
select setval('catalog.product_option_description_seq', greatest((select coalesce(max(description_id), 0) + 1 from catalog.product_option_description),
    (select case when is_called then last_value + 50 else last_value end from catalog.product_option_description_seq)), false);
select setval('catalog.product_option_value_seq', greatest((select coalesce(max(product_option_value_id), 0) + 1 from catalog.product_option_value),
    (select case when is_called then last_value + 50 else last_value end from catalog.product_option_value_seq)), false);
select setval('catalog.product_option_value_description_seq', greatest((select coalesce(max(description_id), 0) + 1 from catalog.product_option_value_description),
    (select case when is_called then last_value + 50 else last_value end from catalog.product_option_value_description_seq)), false);
select setval('catalog.product_type_seq', greatest((select coalesce(max(product_type_id), 0) + 1 from catalog.product_type),
    (select case when is_called then last_value + 50 else last_value end from catalog.product_type_seq)), false);
select setval('catalog.product_type_description_seq', greatest((select coalesce(max(description_id), 0) + 1 from catalog.product_type_description),
    (select case when is_called then last_value + 50 else last_value end from catalog.product_type_description_seq)), false);
select setval('catalog.product_variant_seq', greatest((select coalesce(max(product_variant_id), 0) + 1 from catalog.product_variant),
    (select case when is_called then last_value + 50 else last_value end from catalog.product_variant_seq)), false);
