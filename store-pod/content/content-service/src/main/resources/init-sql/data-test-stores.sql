-- Test-store seeds live in stores/<storeId>/01-store.sql; this file only guards the sequencer rows.
INSERT INTO content.sm_sequencer (seq_name, seq_count)
VALUES ('CONTENT_SEQ_NEXT_VAL', 40)
on conflict do nothing;
INSERT INTO content.sm_sequencer (seq_name, seq_count)
VALUES ('CONTENT_DESCRIPTION_SEQ_NEXT_VAL', 80)
on conflict do nothing;
