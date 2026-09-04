package com.asrevo.cvhome.sso.audit;

import java.util.Map;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class AuditDiffTest {

    private static final String NAME = "name";

    private static final String A = "a";

    private static final String B = "b";

    private final AuditDiff diff = new AuditDiff(new ObjectMapper());

    record Snapshot(String name, boolean enabled, String passwordHash) {
    }

    @Test
    void onlyChangedFieldsSurviveAndSecretsNever() {
        AuditDiff.Diff result = diff.of(new Snapshot(A, true, "h1"), new Snapshot(B, true, "h2"));

        assertThat(result.before()).isEqualTo(Map.of(NAME, A));
        assertThat(result.after()).isEqualTo(Map.of(NAME, B));
    }

    @Test
    void aCreationHasNoBeforeAndADeletionNoAfter() {
        AuditDiff.Diff created = diff.of(null, new Snapshot(A, true, null));
        AuditDiff.Diff deleted = diff.of(new Snapshot(A, true, null), null);

        assertThat(created.before()).isNull();
        assertThat(created.after()).containsEntry(NAME, A).containsEntry("enabled", true).doesNotContainKey("passwordHash");
        assertThat(deleted.after()).isNull();
        assertThat(deleted.before()).containsEntry(NAME, A);
    }

    @Test
    void identicalSnapshotsProduceNothing() {
        AuditDiff.Diff result = diff.of(new Snapshot(A, true, null), new Snapshot(A, true, null));

        assertThat(result.before()).isNull();
        assertThat(result.after()).isNull();
    }

}
