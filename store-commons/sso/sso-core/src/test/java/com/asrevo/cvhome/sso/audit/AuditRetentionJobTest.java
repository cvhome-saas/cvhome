package com.asrevo.cvhome.sso.audit;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.RealmId;
import com.asrevo.cvhome.sso.realm.RealmContext;
import com.asrevo.cvhome.sso.realm.RealmRegistry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The sweep has to enter each realm, because the delete it performs is filtered by whichever realm the thread is
 * in. Run from a scheduler thread — which is in none — the multi-realm deployment resolves the sentinel realm and
 * the delete matches nothing at all: retention silently stops happening while the table grows.
 */
class AuditRetentionJobTest {

    private static final RealmId STORE_A = RealmId.of("65f023632bc46470c104b76f");

    private static final RealmId STORE_B = RealmId.of("65f023632bc46470c104b75f");

    private final RealmRegistry realms = mock(RealmRegistry.class);

    private final AuditRetention retention = mock(AuditRetention.class);

    private final AuditRetentionJob job = new AuditRetentionJob(realms, retention);

    @Test
    void everyRealmIsTrimmedInsideItsOwnRealm() {
        when(realms.all()).thenReturn(List.of(RealmId.PLATFORM, STORE_A, STORE_B));
        List<RealmId> entered = new ArrayList<>();
        doAnswer(invocation -> entered.add(RealmContext.require())).when(retention).trim(any());

        job.trim();

        assertThat(entered).containsExactly(RealmId.PLATFORM, STORE_A, STORE_B);
        assertThat(RealmContext.current()).as("and the thread is left as it was found").isEmpty();
    }

}
