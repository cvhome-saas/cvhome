INSERT INTO content.sm_sequencer (seq_name, seq_count)
VALUES ('content_description_seq', 16)
on conflict do nothing;
INSERT INTO content.sm_sequencer (seq_name, seq_count)
VALUES ('CONTENT_SEQ_NEXT_VAL', 16)
on conflict do nothing;
