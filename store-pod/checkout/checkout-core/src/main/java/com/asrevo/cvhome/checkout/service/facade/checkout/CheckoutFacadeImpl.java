package com.asrevo.cvhome.checkout.service.facade.checkout;

import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.model.product.ProductReservationResult;
import com.asrevo.cvhome.catalog.services.product.ExternalProductReservationService;
import com.asrevo.cvhome.checkout.entity.customer.Customer;
import com.asrevo.cvhome.checkout.entity.order.Order;
import com.asrevo.cvhome.checkout.model.order.v1.PersistableOrder;
import com.asrevo.cvhome.checkout.service.facade.order.OrderFacade;
import com.asrevo.cvhome.checkout.service.facade.order.model.OrderProcessingResult;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentResponse;
import com.asrevo.cvhome.payment.services.payment.ExternalPaymentGatewayService;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
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

    @Override
    public OrderProcessingResult placeOrder(PersistableOrder order, Customer customer, StoreMerchantId store, LanguageCode language,
                                            Locale locale) throws ServiceException {

        Order modelOrder = orderFacade.saveOrder(order, customer, store, language);

        ProductReservationResult result =
                externalProductReservationService.reserve(store, modelOrder.getId().toString(), toProductReservationList(modelOrder));

        if (!result.status()) {
            orderFacade.updateOrderStatus(modelOrder.getId(), OrderStatus.FAILED);
            return new OrderProcessingResult(modelOrder);
        }


        PaymentRequest paymentRequest = new PaymentRequest(
                modelOrder.getId(),
                modelOrder.getTotal(),
                modelOrder.getCurrency(),
                modelOrder.getPaymentType(),
                result.expireAt()
        );

        log.debug("Initiating gateway payment for order {} type {}", modelOrder.getId(), modelOrder.getPaymentType());
        PaymentResponse paymentResponse = externalPaymentGatewayService.initiatePayment(modelOrder.getStoreMerchantId(), paymentRequest);

        switch (paymentResponse.status()) {
            case PAID:
                log.info("Payment PAID for order {}. Marking as PAID.", modelOrder.getId());
                externalProductReservationService.commit(store, modelOrder.getId().toString());
                orderFacade.updateOrderStatus(modelOrder.getId(), OrderStatus.PAID);
                break;

            case PAY_LATER:
                log.info("Payment PAY_LATER (COD) for order {}. Marking as ORDERED.", modelOrder.getId());
                externalProductReservationService.commit(store, modelOrder.getId().toString());
                orderFacade.updateOrderStatus(modelOrder.getId(), OrderStatus.ORDERED);
                break;

            case PENDING:
                log.info("Payment Pending order {}.", modelOrder.getId());
                break;

            case FAILED:
                log.warn("Payment failed for order {}. Updating status.", modelOrder.getId());
                orderFacade.updateOrderStatus(modelOrder.getId(), OrderStatus.FAILED);
                externalProductReservationService.release(store, modelOrder.getId().toString());
                break;
        }

        if (paymentResponse.isRedirect()) {
            return new OrderProcessingResult(modelOrder, paymentResponse.redirectUrl());
        }

        return new OrderProcessingResult(modelOrder);
    }


    private ProductReservationList toProductReservationList(Order modelOrder) {
        return modelOrder.getOrderProducts()
                .stream()
                .map(it -> new ReserveProductEntry(it.getSku(), it.getProductQuantity()))
                .collect(Collectors.collectingAndThen(Collectors.toSet(), ProductReservationList::new));
    }


}
