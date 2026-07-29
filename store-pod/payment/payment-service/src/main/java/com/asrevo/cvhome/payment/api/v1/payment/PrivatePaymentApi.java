package com.asrevo.cvhome.payment.api.v1.payment;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.api.v1.payment.model.PaymentApprovalRequest;
import com.asrevo.cvhome.payment.service.PaymentApprovalService;

import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/v1/private")
@Tag(name = "Private Payment API", description = "Endpoints for admins to approve/reject payments")
@AllArgsConstructor
public class PrivatePaymentApi {

    private final PaymentApprovalService paymentApprovalService;

    @PostMapping("/payment/transaction/{transactionId}/approve")
    @PreAuthorize("hasPermission(#store,'StoreMerchantId','STORE-POD.PAYMENT.*')")
    public void approve(StoreMerchantId store, @PathVariable Long transactionId, @Valid @RequestBody PaymentApprovalRequest approvalModel) {
        paymentApprovalService.approvePayment(store, transactionId, approvalModel.getTransactionNo());
    }

    @PostMapping("/payment/transaction/{transactionId}/reject")
    @PreAuthorize("hasPermission(#store,'StoreMerchantId','STORE-POD.PAYMENT.*')")
    public void reject(StoreMerchantId store, @PathVariable Long transactionId) {
        paymentApprovalService.rejectPayment(store, transactionId);
    }
}
