INSERT INTO content.sm_sequencer (seq_name, seq_count)
VALUES ('CONTENT_SEQ_NEXT_VAL', 40)
on conflict do nothing;
INSERT INTO content.sm_sequencer (seq_name, seq_count)
VALUES ('CONTENT_DESCRIPTION_SEQ_NEXT_VAL', 80)
on conflict do nothing;
