-- One-off: the pre-rewrite checkout schema (the service deleted in the 2026-09 rewrite; git history before PR #328 has
-- it). No data was migrated by decision; every name here is legacy-only and the new schema reuses none of them, so after the first boot this is a no-op.
-- Delete this file once every environment has booted the new service once.
drop table if exists
    checkout.order_account_product, checkout.order_account, checkout.order_product_download,
    checkout.order_product_price, checkout.order_product_option, checkout.order_product, checkout.order_attribute,
    checkout.order_status_history, checkout.order_total, checkout.orders,
    checkout.shopping_cart_item, checkout.shopping_cart,
    checkout.customer_review_description, checkout.customer_review, checkout.customer_group, checkout.customer,
    checkout.file_history, checkout.optin,
    checkout.zone_description, checkout.zone, checkout.country_description, checkout.country,
    checkout.geozone_description, checkout.geozone, checkout.language, checkout.currency
    cascade;
