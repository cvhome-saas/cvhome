package com.asrevo.cvhome.payment.api.v1.payment;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.payment.models.PersistablePaymentConfiguration;
import com.asrevo.cvhome.payment.reads.PaymentTypeReads;
import com.asrevo.cvhome.payment.service.PaymentConfigurationService;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;
import com.asrevo.cvhome.testsupport.annotations.StorageIntegrationTest;
import com.asrevo.cvhome.testsupport.security.Tokens;
import com.asrevo.cvhome.testsupport.sql.SqlStatements;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The payment types a store accepts on a real payment database: the second read costs no statement, and a saved
 * configuration makes that store's next read pay and show the new type, while the other store's entry stays warm.
 * Mutates {@code STORE_2}'s configuration, the store this domain's tests own for writes.
 */
@StorageIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
@TestPropertySource(properties = PaymentApiTestSupport.POD_PROPERTY)
class PaymentTypeReadsIntegrationTest {

    private static final StoreMerchantId STORE_1 = new StoreMerchantId(Tokens.STORE_1);

    private static final StoreMerchantId STORE_2 = new StoreMerchantId(Tokens.STORE_2);

    @Autowired
    private PaymentTypeReads reads;

    @Autowired
    private PaymentConfigurationService configurations;

    @Test
    void aSecondReadCostsNoStatementAndASavedConfigurationDropsItsStoresEntryAlone() throws Exception {
        PaymentType[] before = reads.supported(STORE_2);
        reads.supported(STORE_1);

        SqlStatements.Recorded<PaymentType[]> again = SqlStatements.during(() -> reads.supported(STORE_2));
        assertThat(again.count()).as(again.toString()).isZero();
        assertThat(again.result()).isEqualTo(before);

        PaymentType added = PaymentType.COD;
        boolean hadIt = Arrays.asList(before).contains(added);
        if (hadIt) {
            configurations.deleteConfig(STORE_2, added);
        } else {
            configurations.saveConfig(STORE_2, PersistablePaymentConfiguration.builder().storeMerchantId(STORE_2)
                    .paymentType(added).enabled(true).build());
        }

        SqlStatements.Recorded<PaymentType[]> afterWrite = SqlStatements.during(() -> reads.supported(STORE_2));
        assertThat(afterWrite.count()).as("the store whose configuration changed reads the database again").isPositive();
        assertThat(Arrays.asList(afterWrite.result()).contains(added)).isEqualTo(!hadIt);
        SqlStatements.Recorded<PaymentType[]> other = SqlStatements.during(() -> reads.supported(STORE_1));
        assertThat(other.count()).as("the other store's entry stays warm").isZero();
    }
}
