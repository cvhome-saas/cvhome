package com.asrevo.cvhome.payment.api.v1.payment;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateResult;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentResponse;
import com.asrevo.cvhome.payment.service.PaymentGatewayService;
import com.asrevo.cvhome.payment.services.payment.ExternalPaymentGatewayService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;


@RestController
@RequestMapping("/api/v1")
@Tag(name = "Payment Gateway")
@Slf4j
@AllArgsConstructor
public class ExternalPaymentGatewayApi implements ExternalPaymentGatewayService {

    private final PaymentGatewayService paymentGatewayService;

    @PostMapping("/private/payments/initiate")
    @Operation(method = "POST", description = "Initiate Payment",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = PaymentInitiateResult.class))))
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))

    @Override
    public PaymentInitiateResult initiatePayment(StoreMerchantId store, PaymentRequest paymentRequest) {
        return paymentGatewayService.initiatePayment(store, paymentRequest);
    }

    @GetMapping("/payments/{requestRef}/status")
    @Operation(method = "GET", description = "Payment Status",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = PaymentResponse.class))))
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))

    @Override
    public PaymentResponse status(StoreMerchantId store, @PathVariable("requestRef") String requestRef) {
        return paymentGatewayService.status(store, requestRef);
    }


}
