package com.asrevo.cvhome.sso.audit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.commons.domain.RealmId;
import com.asrevo.cvhome.sso.realm.RealmContext;
import com.asrevo.cvhome.sso.realm.RealmRegistry;

import lombok.RequiredArgsConstructor;

/**
 * Trims each realm's audit log to its own {@code auditRetentionDays}, nightly.
 *
 * <p>
 * A realm at a time, because that is the only way it works. Audit rows are {@code @TenantId} rows, so the delete
 * is filtered by whatever realm the thread is in — and a scheduled thread is in none, which on a multi-realm
 * deployment resolves to the sentinel realm and deletes nothing at all. The retention a merchant configured did
 * nothing, silently, while the table grew. Each realm gets its own transaction because Hibernate fixes the tenant
 * when the session opens, not per query.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class AuditRetentionJob {

    private final RealmRegistry realms;

    private final AuditRetention retention;

    @Scheduled(cron = "0 17 3 * * *")
    public void trim() {
        for (RealmId realm : realms.all()) {
            RealmContext.runIn(realm, () -> retention.trim(realm));
        }
    }

}
