-- Id generators for the seeded demo data; mirrors the highest ids used in stores/*/17-inventory-availability-price.sql.
INSERT INTO inventory.sm_sequencer (seq_name, seq_count)
VALUES ('PRODUCT_AVAILABILITY_SEQ_NEXT_VAL', 5000)
on conflict do nothing;
INSERT INTO inventory.sm_sequencer (seq_name, seq_count)
VALUES ('PRODUCT_PRICE_SEQ_NEXT_VAL', 5000)
on conflict do nothing;
INSERT INTO inventory.sm_sequencer (seq_name, seq_count)
VALUES ('PRODUCT_RESERVATION_SEQ_NEXT_VAL', 0)
on conflict do nothing;
INSERT INTO inventory.sm_sequencer (seq_name, seq_count)
VALUES ('PRODUCT_RESERVATION_LINE_SEQ_NEXT_VAL', 0)
on conflict do nothing;
