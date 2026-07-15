package com.asrevo.cvhome.checkout.service.facade.checkout;

import java.util.Locale;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.model.product.ProductReservationResult;
import com.asrevo.cvhome.checkout.entity.customer.Customer;
import com.asrevo.cvhome.checkout.entity.order.Order;
import com.asrevo.cvhome.checkout.model.order.v1.PersistableOrder;
import com.asrevo.cvhome.checkout.service.facade.order.OrderFacade;
import com.asrevo.cvhome.checkout.service.facade.order.OrderInventoryOrchestrator;
import com.asrevo.cvhome.checkout.service.facade.order.model.OrderProcessingResult;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentResponse;
import com.asrevo.cvhome.payment.services.payment.ExternalPaymentGatewayService;
import com.asrevo.cvhome.store.core.entity.common.InventoryStatus;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.commons.domain.LanguageCode;

import lombok.extern.slf4j.Slf4j;

@Service("orderPlacementFacade")
@Slf4j
public class OrderPlacementFacadeImpl implements OrderPlacementFacade {

    private final OrderFacade orderFacade;
    private final OrderInventoryOrchestrator orderInventoryOrchestrator;
    private final ExternalPaymentGatewayService externalPaymentGatewayService;

    public OrderPlacementFacadeImpl(OrderFacade orderFacade,
                                    OrderInventoryOrchestrator orderInventoryOrchestrator,
                                    ExternalPaymentGatewayService externalPaymentGatewayService) {
        this.orderFacade = orderFacade;
        this.orderInventoryOrchestrator = orderInventoryOrchestrator;
        this.externalPaymentGatewayService = externalPaymentGatewayService;
    }

    @Override
    public OrderProcessingResult placeOrder(PersistableOrder order, Customer customer, StoreMerchantId store, LanguageCode language,
                                            Locale locale) throws ServiceException {

        Order modelOrder = orderFacade.saveOrder(order, customer, store, language);

        ProductReservationResult result = orderInventoryOrchestrator.reserveProduct(store, modelOrder);

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
                try {
                    orderInventoryOrchestrator.updateOrderStatusWithReservationCommit(modelOrder.getId(), store, OrderStatus.PROCESSING,
                            PaymentStatus.PAID);
                } catch (Exception e) {
                    log.error("Failed to commit reservation for PAID order {}. Manual intervention required.", modelOrder.getId(), e);
                    // Ensure local status reflects payment even if catalog commit failed
                    orderFacade.updateOrderStatus(modelOrder.getId(), OrderStatus.CREATED, InventoryStatus.RESERVED, PaymentStatus.PAID);
                }
                break;

            case PAY_LATER:
                log.info("Payment PAY_LATER (COD) for order {}. Marking as ORDERED.", modelOrder.getId());
                try {
                    orderInventoryOrchestrator.updateOrderStatusWithReservationCommit(modelOrder.getId(), store, OrderStatus.CREATED,
                            PaymentStatus.PENDING);
                } catch (Exception e) {
                    log.error("Failed to commit reservation for PAY_LATER order {}.", modelOrder.getId(), e);
                    orderFacade.updateOrderStatus(modelOrder.getId(), OrderStatus.CREATED, InventoryStatus.RESERVED, PaymentStatus.PENDING);
                }
                break;

            case PENDING:
                log.info("Payment Pending order {}.", modelOrder.getId());
                orderFacade.updateOrderStatus(modelOrder.getId(), OrderStatus.CREATED, InventoryStatus.RESERVED, PaymentStatus.PENDING);
                break;

            case FAILED:
                log.warn("Payment failed for order {}. Updating status.", modelOrder.getId());
                try {
                    orderInventoryOrchestrator.updateOrderStatusWithReservationRelease(modelOrder.getId(), store, OrderStatus.CANCELLED,
                            PaymentStatus.FAILED);
                } catch (Exception e) {
                    log.error("Failed to release reservation for FAILED order {}.", modelOrder.getId(), e);
                    orderFacade.updateOrderStatus(modelOrder.getId(), OrderStatus.CANCELLED, InventoryStatus.RESERVATION_FAILED,
                            PaymentStatus.FAILED);
                }
                break;
        }

        if (paymentResponse.shouldRedirect()) {
            return new OrderProcessingResult(modelOrder, paymentResponse.redirectUrl());
        }

        return new OrderProcessingResult(modelOrder);
    }

    private PaymentResponse doOrderPaymentInitiate(Order modelOrder, ProductReservationResult result) {
        try {
            PaymentRequest paymentRequest = PaymentRequest.builder()
                    .ref(modelOrder.getId().toString())
                    .amount(modelOrder.getTotal())
                    .currency(modelOrder.getCurrency())
                    .paymentType(modelOrder.getPaymentType())
                    .expireAt(result.expireAt())
                    .build();

            log.debug("Initiating gateway payment for order {} type {}", modelOrder.getId(), modelOrder.getPaymentType());
            return externalPaymentGatewayService.initiatePayment(modelOrder.getStoreMerchantId(), paymentRequest);

        } catch (Exception e) {
            log.error("Payment initiation error for order {}. Setting status to PENDING for reconciliation.", modelOrder.getId(), e);
            return PaymentResponse.pending();
        }
    }
}
