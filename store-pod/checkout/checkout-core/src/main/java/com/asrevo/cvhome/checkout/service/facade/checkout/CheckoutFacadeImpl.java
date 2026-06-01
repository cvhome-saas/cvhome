package com.asrevo.cvhome.checkout.service.facade.checkout;

import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.services.product.ExternalPaymentGatewayService;
import com.asrevo.cvhome.catalog.services.product.ExternalProductReservationService;
import com.asrevo.cvhome.catalog.services.product.model.PaymentRequest;
import com.asrevo.cvhome.catalog.services.product.model.PaymentResponse;
import com.asrevo.cvhome.catalog.services.product.model.PaymentStatus;
import com.asrevo.cvhome.checkout.entity.customer.Customer;
import com.asrevo.cvhome.checkout.entity.order.Order;
import com.asrevo.cvhome.checkout.model.order.v1.PersistableOrder;
import com.asrevo.cvhome.checkout.service.facade.order.OrderFacade;
import com.asrevo.cvhome.checkout.service.facade.order.model.OrderProcessingResult;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.ProductReservationList;
import com.asrevo.cvhome.store.core.model.catalog.ReserveProductEntry;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

import lombok.extern.slf4j.Slf4j;

@Service("checkoutFacade")
@Slf4j
public class CheckoutFacadeImpl implements CheckoutFacade {

    private final OrderFacade orderFacade;
    private final ExternalPaymentGatewayService externalPaymentGatewayService;
    private final ExternalProductReservationService externalProductReservationService;

    public CheckoutFacadeImpl(OrderFacade orderFacade,
                              ExternalPaymentGatewayService externalPaymentGatewayService,
                              ExternalProductReservationService externalProductReservationService) {
        this.orderFacade = orderFacade;
        this.externalPaymentGatewayService = externalPaymentGatewayService;
        this.externalProductReservationService = externalProductReservationService;
    }

    private static ProductReservationList toProductReservationList(Order modelOrder) {
        return modelOrder.getOrderProducts()
                .stream()
                .map(it -> new ReserveProductEntry(it.getSku(), it.getProductQuantity()))
                .collect(Collectors.collectingAndThen(Collectors.toSet(), ProductReservationList::new));
    }

    @Override
    public OrderProcessingResult placeOrder(PersistableOrder order, Customer customer, StoreMerchantId store, LanguageCode language,
                                            Locale locale) throws ServiceException {

        Order modelOrder = orderFacade.saveOrder(order, customer, store, language);

        return handleGatewayPaymentOrders(store, modelOrder);
    }

    private OrderProcessingResult handleGatewayPaymentOrders(StoreMerchantId store, Order modelOrder) {
        PaymentRequest paymentRequest = new PaymentRequest(
                modelOrder.getId(),
                modelOrder.getTotal(),
                modelOrder.getCurrency(),
                modelOrder.getPaymentType()
        );

        log.debug("Initiating gateway payment for order {} type {}", modelOrder.getId(), modelOrder.getPaymentType());
        PaymentResponse paymentResponse = externalPaymentGatewayService.initiatePayment(paymentRequest);

        switch (paymentResponse.status()) {
            case SUCCESS:
                log.debug("Auto Committing inventory for order {} (Status: {})", modelOrder.getId(), paymentResponse.status());
                externalProductReservationService.autoCommit(store, modelOrder.getId(), toProductReservationList(modelOrder));
                break;

            case PENDING, REDIRECT_REQUIRED:
                log.debug("Reserving inventory for order {} (Status: {})", modelOrder.getId(), paymentResponse.status());
                externalProductReservationService.reserve(store, modelOrder.getId(), toProductReservationList(modelOrder));
                break;

            case FAILED:
                log.warn("Payment failed for order {}. No inventory reserved.", modelOrder.getId());
                break;
        }

        if (paymentResponse.status() == PaymentStatus.REDIRECT_REQUIRED) {
            return new OrderProcessingResult(modelOrder, paymentResponse.redirectUrl());
        }

        return new OrderProcessingResult(modelOrder);
    }


}
