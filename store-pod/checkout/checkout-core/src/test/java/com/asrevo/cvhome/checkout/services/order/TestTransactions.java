package com.asrevo.cvhome.checkout.services.order;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.mockito.Mockito.mock;

/**
 * A {@link TransactionTemplate} over a mocked manager: the callback runs inline, nothing is committed anywhere.
 */
public final class TestTransactions {

    private TestTransactions() {
    }

    public static TransactionTemplate inline() {
        return new TransactionTemplate(mock(PlatformTransactionManager.class));
    }
}
