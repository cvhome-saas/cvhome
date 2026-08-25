package com.asrevo.cvhome.payment.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.model.payment.PaymentUseCase;
import com.asrevo.cvhome.payment.model.payment.WebhookResult;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class WebhookUseCaseHandlerTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("store-1");

    private static final String INTERNAL_REF = "tx-1";

    private static final String TRANSACTION_NO = "TXN-1";

    @Mock
    private TransactionService transactions;

    private static WebhookResult result(PaymentUseCase useCase) {
        return WebhookResult.builder().internalReference(INTERNAL_REF).paymentUseCase(useCase).build();
    }

    @Test
    void eachUseCaseSettlesTheTransactionAccordingly() {
        WebhookUseCaseHandler handler = new WebhookUseCaseHandler(transactions);

        handler.handleUseCase(STORE, result(PaymentUseCase.PAYMENT_SUCCEEDED));
        handler.handleUseCase(STORE, result(PaymentUseCase.PAYMENT_FAILED));
        handler.handleUseCase(STORE, result(PaymentUseCase.PAYMENT_CANCELED));
        handler.handleUseCase(STORE, WebhookResult.noneUseCase());

        verify(transactions).completeSuccess(STORE, INTERNAL_REF);
        verify(transactions).completeFailed(STORE, INTERNAL_REF);
        verify(transactions).completeCanceled(STORE, INTERNAL_REF);
        verifyNoMoreInteractions(transactions);
    }

    @Test
    void approvalServiceDelegatesBothDecisions() {
        PaymentApprovalService approvals = new PaymentApprovalService(transactions);

        approvals.approvePayment(STORE, INTERNAL_REF, TRANSACTION_NO);
        approvals.rejectPayment(STORE, INTERNAL_REF);

        verify(transactions).approvePayment(STORE, INTERNAL_REF, TRANSACTION_NO);
        verify(transactions).rejectPayment(STORE, INTERNAL_REF);
        verifyNoMoreInteractions(transactions);
    }

    @Test
    void noneTouchesNothing() {
        new WebhookUseCaseHandler(transactions).handleUseCase(STORE, WebhookResult.noneUseCase());

        verifyNoInteractions(transactions);
    }

}
