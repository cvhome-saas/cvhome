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
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;
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

        if (PaymentType.COD.equals(order.getPaymentType())) {
            return handleCashOnDeliveryOrders(store, modelOrder);
        } else {
            return handlePrePaidOnlinePaymentOrders(store, modelOrder);
        }


    }

    private OrderProcessingResult handlePrePaidOnlinePaymentOrders(StoreMerchantId store, Order modelOrder) {
        PaymentRequest paymentRequest = new PaymentRequest(
                modelOrder.getId(),
                modelOrder.getTotal(),
                modelOrder.getCurrency()
        );

        log.debug("Initiating payment for order {} with amount {}", modelOrder.getId(), modelOrder.getTotal());
        PaymentResponse paymentResponse = externalPaymentGatewayService.initiatePayment(paymentRequest);
        if (paymentResponse.status() == PaymentStatus.REDIRECT_REQUIRED) {
            log.debug("Payment requires redirect for order {} to: {}", modelOrder.getId(), paymentResponse.redirectUrl());

            // External call (Inventory)
            log.debug("Reserving inventory for order {}", modelOrder.getId());
            externalProductReservationService.reserve(store, modelOrder.getId(), toProductReservationList(modelOrder));

            return new OrderProcessingResult(modelOrder, paymentResponse.redirectUrl());
        } else {
            throw new IllegalStateException("Unexpected payment status " + paymentResponse.status() + " for order " + modelOrder.getId());
        }
    }

    private OrderProcessingResult handleCashOnDeliveryOrders(StoreMerchantId store, Order modelOrder) {
        // Handle Cash On Delivery (COD)
        log.debug("COD order {} processed successfully.", modelOrder.getId());

        // External call (Inventory)
        log.debug("Auto Reserving inventory for order {}", modelOrder.getId());
        externalProductReservationService.autoCommit(store, modelOrder.getId(), toProductReservationList(modelOrder));

        return new OrderProcessingResult(modelOrder);
    }

}
