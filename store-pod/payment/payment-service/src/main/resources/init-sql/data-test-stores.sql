INSERT INTO payment.sm_sequencer (seq_name, seq_count)
VALUES ('TRANSACTION_SEQ_NEXT_VAL', 0)
on conflict do nothing;
