package com.asrevo.cvhome.payment.api.v1.payment;

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
@RequestMapping("/api/v1")
@Tag(name = "Public Payment API", description = "Endpoints for customers to submit payment proof")
@AllArgsConstructor
public class PublicPaymentApi {

    private final PaymentGatewayService paymentGatewayService;

    @PostMapping("/payment/transaction/{transactionId}/proof")
    public void submitProof(StoreMerchantId store, @PathVariable Long transactionId,
                           @RequestParam String transactionNo, @RequestParam String proofImage) {
        paymentGatewayService.submitProof(transactionId, transactionNo, proofImage);
    }
}
