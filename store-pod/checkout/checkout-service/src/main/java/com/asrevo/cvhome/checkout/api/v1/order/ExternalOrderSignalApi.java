package com.asrevo.cvhome.checkout.api.v1.order;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.checkout.errors.OrderNotFoundException;
import com.asrevo.cvhome.checkout.model.signal.PaymentSignal;
import com.asrevo.cvhome.checkout.model.signal.ReservationExpiredSignal;
import com.asrevo.cvhome.checkout.model.signal.SignalOutcome;
import com.asrevo.cvhome.checkout.services.order.IOrderSignalService;
import com.asrevo.cvhome.checkout.services.order.OrderSignalService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

/**
 * Payment and inventory reporting what became of an order. Same-pod service principals only
 * ({@code STORE-POD.CHECKOUT.SIGNAL}); a shopper or seller token is refused. The paths must match
 * {@code ExternalOrderSignalService} by eye — {@code ExternalOrderSignalServiceContractTest} checks.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Order signals (service-to-service)")
@RequiredArgsConstructor
public class ExternalOrderSignalApi implements IOrderSignalService {

    private static final String SIGNAL = "hasPermission(#store,'StoreMerchantId','STORE-POD.CHECKOUT.SIGNAL')";

    private final OrderSignalService signals;

    @Override
    @PostMapping("/private/orders/{orderRef}/signals/payment")
    @PreAuthorize(SIGNAL)
    public SignalOutcome signalPayment(StoreMerchantId store, @PathVariable("orderRef") String orderRef,
                                       @Valid @RequestBody PaymentSignal signal) throws OrderNotFoundException {
        return signals.paymentSignal(store, orderRef, signal);
    }

    @Override
    @PostMapping("/private/orders/{orderRef}/signals/reservation-expired")
    @PreAuthorize(SIGNAL)
    public SignalOutcome signalReservationExpired(StoreMerchantId store, @PathVariable("orderRef") String orderRef,
                                                  @Valid @RequestBody ReservationExpiredSignal signal)
            throws OrderNotFoundException {
        return signals.reservationExpired(store, orderRef, signal);
    }
}
