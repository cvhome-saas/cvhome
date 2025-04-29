INSERT INTO content.sm_sequencer (seq_name, seq_count)
VALUES ('CONTENT_SEQ_NEXT_VAL', 32)
on conflict do nothing;
INSERT INTO content.sm_sequencer (seq_name, seq_count)
VALUES ('CONTENT_DESCRIPTION_SEQ_NEXT_VAL', 64)
on conflict do nothing;
