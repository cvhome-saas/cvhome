package com.asrevo.cvhome.payment.api.v1.payment;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.service.PaymentGatewayService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/v1/private")
@Tag(name = "Private Payment API", description = "Endpoints for admins to approve/reject payments")
@AllArgsConstructor
public class PrivatePaymentApi {

    private final PaymentGatewayService paymentGatewayService;

    @PostMapping("/payment/transaction/{transactionId}/approve")
    @PreAuthorize("hasPermission(#store,'StoreMerchantId','STORE-POD.PAYMENT.*')")
    public void approve(StoreMerchantId store, @PathVariable Long transactionId) {
        paymentGatewayService.approvePayment(store, transactionId);
    }

    @PostMapping("/payment/transaction/{transactionId}/reject")
    @PreAuthorize("hasPermission(#store,'StoreMerchantId','STORE-POD.PAYMENT.*')")
    public void reject(StoreMerchantId store, @PathVariable Long transactionId, @RequestParam String reason) {
        paymentGatewayService.rejectPayment(store, transactionId, reason);
    }
}
