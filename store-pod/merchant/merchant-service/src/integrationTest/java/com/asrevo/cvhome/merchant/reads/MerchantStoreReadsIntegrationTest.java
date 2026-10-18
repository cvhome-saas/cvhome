package com.asrevo.cvhome.merchant.reads;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.entity.merchant.MerchantStore;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.merchant.repositories.merchant.MerchantRepository;
import com.asrevo.cvhome.testsupport.annotations.StorageIntegrationTest;
import com.asrevo.cvhome.testsupport.security.Tokens;
import com.asrevo.cvhome.testsupport.sql.SqlStatements;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The store record on a real merchant database: the second read costs no statement, and a committed save of the
 * store makes its next read pay and show the change, while the other store's entry stays warm.
 */
@StorageIntegrationTest
class MerchantStoreReadsIntegrationTest {

    private static final StoreMerchantId STORE_1 = new StoreMerchantId(Tokens.STORE_1);

    private static final StoreMerchantId STORE_2 = new StoreMerchantId(Tokens.STORE_2);

    private static final LanguageCode EN = new LanguageCode("en");

    @Autowired
    private MerchantStoreReads reads;

    @Autowired
    private MerchantRepository stores;

    @Autowired
    private TransactionTemplate transactions;

    @Test
    void aSecondReadCostsNoStatementAndASaveOfTheStoreDropsItsEntriesAlone() throws Exception {
        ReadableMerchantStore before = reads.store(STORE_1);
        reads.store(STORE_1, EN);
        reads.languages(STORE_1);
        reads.store(STORE_2);
        assertThat(before).isNotNull();

        SqlStatements.Recorded<Object> again = SqlStatements.during(() -> {
            reads.store(STORE_1);
            reads.store(STORE_1, EN);
            return reads.languages(STORE_1);
        });
        assertThat(again.count()).as(again.toString()).isZero();

        transactions.executeWithoutResult(status -> {
            MerchantStore store = stores.findById(STORE_1).orElseThrow();
            store.setStorename(String.format("%s (renamed)", store.getStorename()));
            stores.save(store);
        });

        SqlStatements.Recorded<ReadableMerchantStore> afterWrite = SqlStatements.during(() -> reads.store(STORE_1));
        assertThat(afterWrite.count()).as("the store that was saved reads the database again").isPositive();
        assertThat(afterWrite.result()).isNotNull();
        SqlStatements.Recorded<ReadableMerchantStore> other = SqlStatements.during(() -> reads.store(STORE_2));
        assertThat(other.count()).as("the other store's entry stays warm").isZero();
    }
}
