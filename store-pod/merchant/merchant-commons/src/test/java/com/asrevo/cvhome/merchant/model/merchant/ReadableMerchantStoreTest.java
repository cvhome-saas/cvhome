package com.asrevo.cvhome.merchant.model.merchant;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.store.core.model.entity.ReadableAudit;

import static org.assertj.core.api.Assertions.assertThat;

class ReadableMerchantStoreTest {

    @Test
    void readableAuditIsTheSameFieldAsAudit() {
        ReadableMerchantStore store = new ReadableMerchantStore();
        ReadableAudit audit = new ReadableAudit();
        audit.setUser("admin");

        store.setReadableAudit(audit);

        assertThat(store.getAudit()).isSameAs(audit);
        assertThat(store.getReadableAudit()).isSameAs(audit);
    }

}
