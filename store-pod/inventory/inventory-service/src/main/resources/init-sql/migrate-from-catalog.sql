-- One-off migration: copy availability, price and reservation data out of the catalog schema (same Postgres
-- database, schema per service). Runs on every startup; every statement is idempotent and the whole block is a
-- no-op once the catalog tables are gone or already copied.
--
-- Two backfills are load-bearing:
--   * product_availability.sku is NULL in pre-split data (the old code joined through catalog.product) — it is
--     copied as COALESCE(availability.sku, product.sku, variant.sku) because after the split the sku column is the
--     reservation path's only key.
--   * product_price.store_merchant_id did not exist — tenancy was inherited via the availability FK.
DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'catalog'
                     AND table_name = 'product_availability') THEN

            INSERT INTO inventory.product_availability (product_avail_id, date_created, date_modified, updt_id,
                                                        available, height, length, weight, width, owner,
                                                        date_available, free_shipping, quantity, quantity_ord_max,
                                                        quantity_ord_min, status, region, region_variant, sku,
                                                        store_merchant_id, product_id, product_variant)
            SELECT a.product_avail_id,
                   a.date_created,
                   a.date_modified,
                   a.updt_id,
                   a.available,
                   a.height,
                   a.length,
                   a.weight,
                   a.width,
                   a.owner,
                   a.date_available,
                   a.free_shipping,
                   a.quantity,
                   a.quantity_ord_max,
                   a.quantity_ord_min,
                   a.status,
                   a.region,
                   a.region_variant,
                   COALESCE(a.sku, p.sku, v.sku),
                   a.store_merchant_id,
                   a.product_id,
                   a.product_variant
            FROM catalog.product_availability a
                     LEFT JOIN catalog.product p ON p.product_id = a.product_id
                     LEFT JOIN catalog.product_variant v ON v.product_variant_id = a.product_variant
            ON CONFLICT (product_avail_id) DO NOTHING;

            INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                                 product_identifier_id, store_merchant_id, product_price_amount,
                                                 product_price_special_amount, product_price_special_end_date,
                                                 product_price_special_st_date, product_price_type, product_avail_id)
            SELECT pp.product_price_id,
                   pp.product_price_code,
                   pp.default_price,
                   pp.product_identifier_id,
                   a.store_merchant_id,
                   pp.product_price_amount,
                   pp.product_price_special_amount,
                   pp.product_price_special_end_date,
                   pp.product_price_special_st_date,
                   pp.product_price_type,
                   pp.product_avail_id
            FROM catalog.product_price pp
                     JOIN catalog.product_availability a ON a.product_avail_id = pp.product_avail_id
            ON CONFLICT (product_price_id) DO NOTHING;

            INSERT INTO inventory.product_reservation (id, date_created, date_modified, updt_id, ref, expire_at,
                                                       status, store_merchant_id)
            SELECT r.id, r.date_created, r.date_modified, r.updt_id, r.ref, r.expire_at, r.status, r.store_merchant_id
            FROM catalog.product_reservation r
            ON CONFLICT (id) DO NOTHING;

            INSERT INTO inventory.product_reservation_line (id, date_created, date_modified, updt_id,
                                                            product_reservation_id, sku, quantity, product_avail_id)
            SELECT l.id, l.date_created, l.date_modified, l.updt_id, l.product_reservation_id, l.sku, l.quantity,
                   l.product_avail_id
            FROM catalog.product_reservation_line l
            ON CONFLICT (id) DO NOTHING;

            -- The id generators must start above every copied primary key, or the first insert collides.
            INSERT INTO inventory.sm_sequencer (seq_name, seq_count)
            VALUES ('PRODUCT_AVAILABILITY_SEQ_NEXT_VAL',
                    (SELECT COALESCE(MAX(product_avail_id), 0) FROM inventory.product_availability)),
                   ('PRODUCT_PRICE_SEQ_NEXT_VAL',
                    (SELECT COALESCE(MAX(product_price_id), 0) FROM inventory.product_price)),
                   ('PRODUCT_RESERVATION_SEQ_NEXT_VAL',
                    (SELECT COALESCE(MAX(id), 0) FROM inventory.product_reservation)),
                   ('PRODUCT_RESERVATION_LINE_SEQ_NEXT_VAL',
                    (SELECT COALESCE(MAX(id), 0) FROM inventory.product_reservation_line))
            ON CONFLICT (seq_name) DO UPDATE SET seq_count = GREATEST(inventory.sm_sequencer.seq_count,
                                                                      excluded.seq_count);
        END IF;
    END
$$;
