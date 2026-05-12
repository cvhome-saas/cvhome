INSERT INTO merchant.sm_sequencer (seq_name, seq_count)
VALUES ('country_description_seq', 17)
on conflict do nothing;
INSERT INTO merchant.sm_sequencer (seq_name, seq_count)
VALUES ('COUNTRY_SEQ_NEXT_VAL', 5)
on conflict do nothing;
INSERT INTO merchant.sm_sequencer (seq_name, seq_count)
VALUES ('CURRENCY_SEQ_NEXT_VAL', 5)
on conflict do nothing;
INSERT INTO merchant.sm_sequencer (seq_name, seq_count)
VALUES ('GROUP_SEQ_NEXT_VAL', 8)
on conflict do nothing;
INSERT INTO merchant.sm_sequencer (seq_name, seq_count)
VALUES ('LANG_SEQ_NEXT_VAL', 5)
on conflict do nothing;
INSERT INTO merchant.sm_sequencer (seq_name, seq_count)
VALUES ('MOD_CONF_SEQ_NEXT_VAL', 1)
on conflict do nothing;
INSERT INTO merchant.sm_sequencer (seq_name, seq_count)
VALUES ('PERMISSION_SEQ_NEXT_VAL', 1)
on conflict do nothing;
INSERT INTO merchant.sm_sequencer (seq_name, seq_count)
VALUES ('USER_SEQ_NEXT_VAL', 1)
on conflict do nothing;
INSERT INTO merchant.sm_sequencer (seq_name, seq_count)
VALUES ('zone_description_seq', 1)
on conflict do nothing;
INSERT INTO merchant.sm_sequencer (seq_name, seq_count)
VALUES ('ZONE_SEQ_NEXT_VAL', 1)
on conflict do nothing;
INSERT INTO merchant.sm_sequencer (seq_name, seq_count)
VALUES ('CONTENT_SEQ_NEXT_VAL', 40)
on conflict do nothing;
INSERT INTO merchant.sm_sequencer (seq_name, seq_count)
VALUES ('CONTENT_DESCRIPTION_SEQ_NEXT_VAL', 80)
on conflict do nothing;
