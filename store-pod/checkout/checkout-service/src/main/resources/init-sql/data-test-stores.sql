-- Id generators for the demo stack. Nothing is seeded into checkout: carts and orders are made by QA, not by SQL.
INSERT INTO checkout.sm_sequencer (seq_name, seq_count) VALUES ('CUSTOMER_ACCOUNT_SEQ_NEXT_VAL', 0) on conflict do nothing;
INSERT INTO checkout.sm_sequencer (seq_name, seq_count) VALUES ('CART_SEQ_NEXT_VAL', 0) on conflict do nothing;
INSERT INTO checkout.sm_sequencer (seq_name, seq_count) VALUES ('CART_LINE_SEQ_NEXT_VAL', 0) on conflict do nothing;
INSERT INTO checkout.sm_sequencer (seq_name, seq_count) VALUES ('SALES_ORDER_SEQ_NEXT_VAL', 1000) on conflict do nothing;
