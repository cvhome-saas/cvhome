package com.asrevo.cvhome.payment.api.v1.payment;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.api.v1.payment.model.PaymentApprovalRequest;
import com.asrevo.cvhome.payment.model.payment.ReadableTransactionList;
import com.asrevo.cvhome.payment.models.TransactionSearchFilter;
import com.asrevo.cvhome.payment.service.PaymentApprovalService;
import com.asrevo.cvhome.payment.service.TransactionService;

import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/v1/private")
@Tag(name = "Private Payment API", description = "Endpoints for admins to approve/reject payments")
@AllArgsConstructor
public class PrivatePaymentApi {

    private final PaymentApprovalService paymentApprovalService;
    private final TransactionService transactionService;

    @GetMapping("/payment/transactions")
    @PreAuthorize("hasPermission(#store,'StoreMerchantId','STORE-POD.PAYMENT.*')")
    public ReadableTransactionList list(StoreMerchantId store, TransactionSearchFilter filter, Pageable pageable) {
        return transactionService.list(store, filter, pageable);
    }

    @PostMapping("/payment/transaction/{internalRef}/approve")
    @PreAuthorize("hasPermission(#store,'StoreMerchantId','STORE-POD.PAYMENT.*')")
    public void approve(StoreMerchantId store, @PathVariable String internalRef, @Valid @RequestBody PaymentApprovalRequest approvalModel) {
        paymentApprovalService.approvePayment(store, internalRef, approvalModel.getTransactionNo());
    }

    @PostMapping("/payment/transaction/{internalRef}/reject")
    @PreAuthorize("hasPermission(#store,'StoreMerchantId','STORE-POD.PAYMENT.*')")
    public void reject(StoreMerchantId store, @PathVariable String internalRef) {
        paymentApprovalService.rejectPayment(store, internalRef);
    }
}
