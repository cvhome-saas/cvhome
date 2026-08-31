INSERT INTO catalog.sm_sequencer (seq_name, seq_count)
VALUES ('CATEGORY_SEQ_NEXT_VAL', 48)
on conflict do nothing;
INSERT INTO catalog.sm_sequencer (seq_name, seq_count)
VALUES ('CATEGORY_DESCRIPTION_SEQ_NEXT_VAL', 96)
on conflict do nothing;
INSERT INTO catalog.sm_sequencer (seq_name, seq_count)
VALUES ('MANUFACTURER_SEQ_NEXT_VAL', 24)
on conflict do nothing;
INSERT INTO catalog.sm_sequencer (seq_name, seq_count)
VALUES ('MANUFACTURER_DESCRIPTION_SEQ_NEXT_VAL', 48)
on conflict do nothing;
INSERT INTO catalog.sm_sequencer (seq_name, seq_count)
VALUES ('PRODUCT_TYPE_SEQ_NEXT_VAL', 16)
on conflict do nothing;
INSERT INTO catalog.sm_sequencer (seq_name, seq_count)
VALUES ('PRODUCT_TYPE_DESCRIPTION_SEQ_NEXT_VAL', 0)
on conflict do nothing;
INSERT INTO catalog.sm_sequencer (seq_name, seq_count)
VALUES ('PRODUCT_SEQ_NEXT_VAL', 180)
on conflict do nothing;
INSERT INTO catalog.sm_sequencer (seq_name, seq_count)
VALUES ('PRODUCT_CATEGORY_SEQ_NEXT_VAL', 0)
on conflict do nothing;
INSERT INTO catalog.sm_sequencer (seq_name, seq_count)
VALUES ('PRODUCT_DESCRIPTION_SEQ_NEXT_VAL', 360)
on conflict do nothing;
INSERT INTO catalog.sm_sequencer (seq_name, seq_count)
VALUES ('PRODUCT_IMAGE_SEQ_NEXT_VAL', 900)
on conflict do nothing;
INSERT INTO catalog.sm_sequencer (seq_name, seq_count)
VALUES ('PRODUCT_GROUP_SEQ_NEXT_VAL', 17)
on conflict do nothing;
INSERT INTO catalog.sm_sequencer (seq_name, seq_count)
VALUES ('PRODUCT_GROUP_DESC_SEQ_NEXT_VAL', 33)
on conflict do nothing;
INSERT INTO catalog.sm_sequencer (seq_name, seq_count)
VALUES ('PRODUCT_OPTION_SEQ_NEXT_VAL', 16)
on conflict do nothing;
INSERT INTO catalog.sm_sequencer (seq_name, seq_count)
VALUES ('PRODUCT_OPTION_DESCRIPTION_SEQ_NEXT_VAL', 32)
on conflict do nothing;
INSERT INTO catalog.sm_sequencer (seq_name, seq_count)
VALUES ('PRODUCT_OPTION_VALUE_SEQ_NEXT_VAL', 32)
on conflict do nothing;
INSERT INTO catalog.sm_sequencer (seq_name, seq_count)
VALUES ('PRODUCT_OPTION_VALUE_DESCRIPTION_SEQ_NEXT_VAL', 64)
on conflict do nothing;
INSERT INTO catalog.sm_sequencer (seq_name, seq_count)
VALUES ('PRODUCT_VARIANT_SEQ_NEXT_VAL', 600)
on conflict do nothing;
