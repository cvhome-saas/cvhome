INSERT INTO orders.sm_sequencer (seq_name, seq_count)
VALUES ('category_description_seq', 8)
on conflict do nothing;
INSERT INTO orders.sm_sequencer (seq_name, seq_count)
VALUES ('CATEGORY_SEQ_NEXT_VAL', 8)
on conflict do nothing;
INSERT INTO orders.sm_sequencer (seq_name, seq_count)
VALUES ('content_description_seq', 16)
on conflict do nothing;
INSERT INTO orders.sm_sequencer (seq_name, seq_count)
VALUES ('CONTENT_SEQ_NEXT_VAL', 16)
on conflict do nothing;
INSERT INTO orders.sm_sequencer (seq_name, seq_count)
VALUES ('MANUFACT_SEQ_NEXT_VAL', 8)
on conflict do nothing;
INSERT INTO orders.sm_sequencer (seq_name, seq_count)
VALUES ('manufacturer_description_seq', 8)
on conflict do nothing;
INSERT INTO orders.sm_sequencer (seq_name, seq_count)
VALUES ('MERCH_CONF_SEQ_NEXT_VAL', 4)
on conflict do nothing;
INSERT INTO orders.sm_sequencer (seq_name, seq_count)
VALUES ('OPTIN_SEQ_NEXT_VAL', 4)
on conflict do nothing;
INSERT INTO orders.sm_sequencer (seq_name, seq_count)
VALUES ('PRD_TYPE_SEQ_NEXT_VAL', 4)
on conflict do nothing;
INSERT INTO orders.sm_sequencer (seq_name, seq_count)
VALUES ('PRODUCT_AVAIL_SEQ_NEXT_VAL', 8)
on conflict do nothing;
INSERT INTO orders.sm_sequencer (seq_name, seq_count)
VALUES ('product_description_seq', 8)
on conflict do nothing;
INSERT INTO orders.sm_sequencer (seq_name, seq_count)
VALUES ('PRODUCT_IMG_SEQ_NEXT_VAL', 8)
on conflict do nothing;
INSERT INTO orders.sm_sequencer (seq_name, seq_count)
VALUES ('product_price_description_seq', 8)
on conflict do nothing;
INSERT INTO orders.sm_sequencer (seq_name, seq_count)
VALUES ('PRODUCT_PRICE_SEQ_NEXT_VAL', 8)
on conflict do nothing;
INSERT INTO orders.sm_sequencer (seq_name, seq_count)
VALUES ('PRODUCT_RELATION_SEQ_NEXT_VAL', 20)
on conflict do nothing;
INSERT INTO orders.sm_sequencer (seq_name, seq_count)
VALUES ('PRODUCT_SEQ_NEXT_VAL', 8)
on conflict do nothing;
INSERT INTO orders.sm_sequencer (seq_name, seq_count)
VALUES ('STORE_SEQ_NEXT_VAL', 4)
on conflict do nothing;
INSERT INTO orders.sm_sequencer (seq_name, seq_count)
VALUES ('TX_CLASS_SEQ_NEXT_VAL', 4)
on conflict do nothing;
INSERT INTO orders.sm_sequencer (seq_name, seq_count)
VALUES ('ORDER_TOTAL_ID_NEXT_VALUE', 16)
on conflict do nothing;
INSERT INTO orders.sm_sequencer (seq_name, seq_count)
VALUES ('SHP_CRT_SEQ_NEXT_VAL', 8)
on conflict do nothing;
INSERT INTO orders.sm_sequencer (seq_name, seq_count)
VALUES ('SHP_CRT_ITM_SEQ_NEXT_VAL', 8)
on conflict do nothing;
INSERT INTO orders.sm_sequencer (seq_name, seq_count)
VALUES ('CUSTOMER_SEQ_NEXT_VAL', 8)
on conflict do nothing;
INSERT INTO orders.sm_sequencer (seq_name, seq_count)
VALUES ('ORDER_ID_SEQ_NEXT_VAL', 8)
on conflict do nothing;
INSERT INTO orders.sm_sequencer (seq_name, seq_count)
VALUES ('STATUS_HIST_ID_NEXT_VALUE', 8)
on conflict do nothing;
INSERT INTO orders.sm_sequencer (seq_name, seq_count)
VALUES ('ORDER_PRODUCT_ID_NEXT_VALUE', 8)
on conflict do nothing;
INSERT INTO orders.sm_sequencer (seq_name, seq_count)
VALUES ('ORDER_PRD_PRICE_ID_NEXT_VAL', 8)
on conflict do nothing;
