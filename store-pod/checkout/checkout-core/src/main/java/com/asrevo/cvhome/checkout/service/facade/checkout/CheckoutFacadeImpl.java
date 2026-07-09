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
import com.asrevo.cvhome.store.core.entity.common.InventoryStatus;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
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

        ProductReservationResult result = doOrderReservation(store, modelOrder);

        if (!result.status()) {
            orderFacade.updateOrderStatus(modelOrder.getId(), OrderStatus.CANCELLED, InventoryStatus.RESERVATION_FAILED,
                    PaymentStatus.FAILED);
            return new OrderProcessingResult(modelOrder);
        }
        orderFacade.updateOrderStatus(modelOrder.getId(), OrderStatus.CREATED, InventoryStatus.RESERVED, PaymentStatus.PENDING);


        PaymentResponse paymentResponse = doOrderPaymentInitiate(modelOrder, result);

        switch (paymentResponse.status()) {
            case PAID:
                log.info("Payment PAID for order {}. Marking as PAID.", modelOrder.getId());
                orderFacade.updateOrderStatus(modelOrder.getId(), OrderStatus.PROCESSING, InventoryStatus.COMMITTED, PaymentStatus.PAID);
                externalProductReservationService.commit(store, modelOrder.getId().toString());
                break;

            case PAY_LATER:
                log.info("Payment PAY_LATER (COD) for order {}. Marking as ORDERED.", modelOrder.getId());
                orderFacade.updateOrderStatus(modelOrder.getId(), OrderStatus.CREATED, InventoryStatus.COMMITTED, PaymentStatus.PENDING);
                externalProductReservationService.commit(store, modelOrder.getId().toString());
                break;

            case PENDING:
                log.info("Payment Pending order {}.", modelOrder.getId());
                orderFacade.updateOrderStatus(modelOrder.getId(), OrderStatus.CREATED, InventoryStatus.RESERVED, PaymentStatus.PENDING);
                break;

            case FAILED:
                log.warn("Payment failed for order {}. Updating status.", modelOrder.getId());
                orderFacade.updateOrderStatus(modelOrder.getId(), OrderStatus.CANCELLED, InventoryStatus.RELEASED, PaymentStatus.FAILED);
                externalProductReservationService.release(store, modelOrder.getId().toString());
                break;
        }

        if (paymentResponse.isRedirect()) {
            return new OrderProcessingResult(modelOrder, paymentResponse.redirectUrl());
        }

        return new OrderProcessingResult(modelOrder);
    }

    private PaymentResponse doOrderPaymentInitiate(Order modelOrder, ProductReservationResult result) {
        try {
            PaymentRequest paymentRequest = new PaymentRequest(
                    modelOrder.getId(),
                    modelOrder.getTotal(),
                    modelOrder.getCurrency(),
                    modelOrder.getPaymentType(),
                    result.expireAt()
            );

            log.debug("Initiating gateway payment for order {} type {}", modelOrder.getId(), modelOrder.getPaymentType());
            return externalPaymentGatewayService.initiatePayment(modelOrder.getStoreMerchantId(), paymentRequest);

        } catch (Exception _) {
            return PaymentResponse.failed();
        }
    }

    private ProductReservationResult doOrderReservation(StoreMerchantId store, Order modelOrder) {
        try {
            return externalProductReservationService.reserve(store, modelOrder.getId().toString(), toProductReservationList(modelOrder));
        } catch (Exception _) {
            return new ProductReservationResult(false);
        }
    }


    private ProductReservationList toProductReservationList(Order modelOrder) {
        return modelOrder.getOrderProducts()
                .stream()
                .map(it -> new ReserveProductEntry(it.getSku(), it.getProductQuantity()))
                .collect(Collectors.collectingAndThen(Collectors.toSet(), ProductReservationList::new));
    }


}
