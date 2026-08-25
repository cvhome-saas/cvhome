package com.asrevo.cvhome.payment.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.Transaction;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateResult;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateStatus;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentResponse;
import com.asrevo.cvhome.payment.model.payment.ReadableTransaction;
import com.asrevo.cvhome.payment.model.payment.ReadableTransactionList;
import com.asrevo.cvhome.payment.models.TransactionSearchFilter;
import com.asrevo.cvhome.payment.repository.payment.TransactionRepository;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("store-1");

    private static final String REQUEST_REF = "order-1";

    private static final String INTERNAL_REF = "tx-1";

    private static final String EXTERNAL_ID = "cs_123";

    private static final String REDIRECT = "https://stripe.example/cs_123";

    private static final String AMOUNT = "12.50";

    private static final String EUR = "EUR";

    private static final String SUCCESS_URL = "https://shop.example/ok";

    private static final String CANCEL_URL = "https://shop.example/cancel";

    private static final String TRANSACTION_NO = "TXN-42";

    private static final String TRANSACTION_DATE = "transactionDate";

    private static final String MISSING = "missing";

    private static final TransactionSearchFilter NO_FILTER = new TransactionSearchFilter(null, null, null, null,
            null, null);

    @Mock
    private TransactionRepository repository;

    private TransactionServiceImpl service;

    private static PaymentRequest request() {
        return PaymentRequest.builder().ref(REQUEST_REF).amount(new BigDecimal(AMOUNT))
                .currency(new CurrencyCode("USD")).paymentType(PaymentType.STRIPE).expireAt(Instant.EPOCH)
                .successUrl(SUCCESS_URL).cancelUrl(CANCEL_URL).build();
    }

    private static Transaction pending() {
        Transaction tx = new Transaction();
        tx.setId(7L);
        tx.setInternalRef(INTERNAL_REF);
        tx.setRequestRef(REQUEST_REF);
        tx.setStoreMerchantId(STORE);
        tx.setStatus(PaymentStatus.PENDING);
        tx.setAmount(BigDecimal.ONE);
        tx.setCurrency(new CurrencyCode(EUR));
        tx.setPaymentType(PaymentType.MANUAL_TRANSFER);
        tx.setTransactionDate(Instant.EPOCH);
        tx.setRedirectUrl(REDIRECT);
        return tx;
    }

    @BeforeEach
    void setUp() {
        service = new TransactionServiceImpl(repository);
    }

    @Test
    void initialTransactionIsPendingWithAFreshInternalRef() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        String ref = service.createInitialTransaction(STORE, request());

        ArgumentCaptor<Transaction> saved = ArgumentCaptor.forClass(Transaction.class);
        verify(repository).save(saved.capture());
        Transaction tx = saved.getValue();
        assertThat(ref).isEqualTo(tx.getInternalRef()).isNotBlank();
        assertThat(tx.getRequestRef()).isEqualTo(REQUEST_REF);
        assertThat(tx.getStoreMerchantId()).isEqualTo(STORE);
        assertThat(tx.getAmount()).isEqualByComparingTo(AMOUNT);
        assertThat(tx.getPaymentType()).isEqualTo(PaymentType.STRIPE);
        assertThat(tx.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(tx.getExpireAt()).isEqualTo(Instant.EPOCH);
        assertThat(tx.getTransactionDate()).isNotNull();
    }

    @ParameterizedTest
    @CsvSource({"PENDING,PENDING", "FAILED,FAILED", "PAID,PAID"})
    void completingInitiationRecordsTheGatewayAnswer(PaymentInitiateStatus initiate, PaymentStatus expected) {
        Transaction tx = pending();
        when(repository.findByStoreMerchantIdAndInternalRef(STORE, INTERNAL_REF)).thenReturn(Optional.of(tx));

        service.completeInitiateTransaction(STORE, INTERNAL_REF, request(), PaymentInitiateResult.builder()
                .status(initiate).externalId(EXTERNAL_ID).redirectUrl(REDIRECT).build());

        assertThat(tx.getStatus()).isEqualTo(expected);
        assertThat(tx.getPaymentGatewayExternalId()).isEqualTo(EXTERNAL_ID);
        assertThat(tx.getRedirectUrl()).isEqualTo(REDIRECT);
        assertThat(tx.getSuccessUrl()).isEqualTo(SUCCESS_URL);
        assertThat(tx.getCancelUrl()).isEqualTo(CANCEL_URL);
        verify(repository).save(tx);
    }

    @Test
    void completingInitiationOfAnUnknownTransactionIsANoOp() {
        when(repository.findByStoreMerchantIdAndInternalRef(STORE, INTERNAL_REF)).thenReturn(Optional.empty());

        service.completeInitiateTransaction(STORE, INTERNAL_REF, request(), PaymentInitiateResult.pending());

        verify(repository, never()).save(any());
    }

    @Test
    void successFailedAndCanceledSettleTheTransaction() {
        Transaction tx = pending();
        when(repository.findByStoreMerchantIdAndInternalRef(STORE, INTERNAL_REF)).thenReturn(Optional.of(tx));

        service.completeSuccess(STORE, INTERNAL_REF);
        assertThat(tx.getStatus()).isEqualTo(PaymentStatus.PAID);
        service.completeFailed(STORE, INTERNAL_REF);
        assertThat(tx.getStatus()).isEqualTo(PaymentStatus.FAILED);
        service.completeCanceled(STORE, INTERNAL_REF);
        assertThat(tx.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
    }

    @Test
    void approvalRecordsTheTransactionNumberAndRejectionDoesNot() {
        Transaction tx = pending();
        when(repository.findByStoreMerchantIdAndInternalRef(STORE, INTERNAL_REF)).thenReturn(Optional.of(tx));

        service.approvePayment(STORE, INTERNAL_REF, TRANSACTION_NO);
        assertThat(tx.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(tx.getTransactionNo()).isEqualTo(TRANSACTION_NO);

        service.rejectPayment(STORE, INTERNAL_REF);
        assertThat(tx.getStatus()).isEqualTo(PaymentStatus.REJECTED);
    }

    @Test
    void settlingAnUnknownTransactionFails() {
        when(repository.findByStoreMerchantIdAndInternalRef(STORE, INTERNAL_REF)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.completeSuccess(STORE, INTERNAL_REF))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining(INTERNAL_REF);
        verify(repository, never()).save(any());
    }

    @Test
    void listMapsThePageAndDefaultsTheSortToNewestFirst() {
        Transaction tx = pending();
        tx.setTransactionNo(TRANSACTION_NO);
        when(repository.findAll(eq(STORE), eq(NO_FILTER), any())).thenReturn(new PageImpl<>(List.of(tx),
                PageRequest.of(1, 5), 6));

        ReadableTransactionList result = service.list(STORE, NO_FILTER, PageRequest.of(1, 5));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(eq(STORE), eq(NO_FILTER), pageable.capture());
        assertThat(pageable.getValue().getSort().getOrderFor(TRANSACTION_DATE).getDirection())
                .isEqualTo(Sort.Direction.DESC);
        assertThat(result.getTotalElements()).isEqualTo(6);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getPageNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(5);
        ReadableTransaction readable = result.getContent().getFirst();
        assertThat(readable.id()).isEqualTo(7L);
        assertThat(readable.internalRef()).isEqualTo(INTERNAL_REF);
        assertThat(readable.requestRef()).isEqualTo(REQUEST_REF);
        assertThat(readable.currency().code()).isEqualTo(EUR);
        assertThat(readable.paymentType()).isEqualTo(PaymentType.MANUAL_TRANSFER);
        assertThat(readable.status()).isEqualTo(PaymentStatus.PENDING);
        assertThat(readable.transactionNo()).isEqualTo(TRANSACTION_NO);
    }

    @Test
    void listKeepsAnExplicitSort() {
        when(repository.findAll(eq(STORE), eq(NO_FILTER), any())).thenReturn(new PageImpl<>(List.of()));

        service.list(STORE, NO_FILTER, PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, TRANSACTION_DATE)));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(eq(STORE), eq(NO_FILTER), pageable.capture());
        assertThat(pageable.getValue().getSort().getOrderFor(TRANSACTION_DATE).getDirection())
                .isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void statusReadsTheLatestTransactionOfTheRequestOrFails() {
        when(repository.findTopByStoreMerchantIdAndRequestRefOrderByTransactionDateDesc(STORE, REQUEST_REF))
                .thenReturn(Optional.of(pending()));
        when(repository.findTopByStoreMerchantIdAndRequestRefOrderByTransactionDateDesc(STORE, MISSING))
                .thenReturn(Optional.empty());

        PaymentResponse found = service.status(STORE, REQUEST_REF);
        PaymentResponse missing = service.status(STORE, MISSING);

        assertThat(found.status()).isEqualTo(PaymentStatus.PENDING);
        assertThat(found.gatewayRef()).isEqualTo(INTERNAL_REF);
        assertThat(found.redirectUrl()).isEqualTo(REDIRECT);
        assertThat(missing.status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(missing.gatewayRef()).isNull();
    }

    @ParameterizedTest
    @CsvSource({"PAID,PAID", "PENDING,PENDING", "PROCESSING,PENDING", "WAITING_VERIFICATION,PENDING",
            "AUTHORIZED,PENDING", "FAILED,FAILED", "EXPIRED,FAILED", "CANCELLED,FAILED", "REJECTED,FAILED",
            "REFUNDED,FAILED"})
    void existingResultFoldsEveryStatusIntoTheInitiateVocabulary(PaymentStatus stored, PaymentInitiateStatus expected) {
        Transaction tx = pending();
        tx.setStatus(stored);
        when(repository.findTopByStoreMerchantIdAndRequestRefOrderByTransactionDateDesc(STORE, REQUEST_REF))
                .thenReturn(Optional.of(tx));

        Optional<PaymentInitiateResult> result = service.findExistingInitialResultByRequestRef(STORE, REQUEST_REF);

        assertThat(result).isPresent();
        assertThat(result.get().status()).isEqualTo(expected);
        assertThat(result.get().gatewayRef()).isEqualTo(INTERNAL_REF);
    }

    @Test
    void noExistingResultForAnUnknownRequest() {
        when(repository.findTopByStoreMerchantIdAndRequestRefOrderByTransactionDateDesc(STORE, REQUEST_REF))
                .thenReturn(Optional.empty());

        assertThat(service.findExistingInitialResultByRequestRef(STORE, REQUEST_REF)).isEmpty();
    }

}
