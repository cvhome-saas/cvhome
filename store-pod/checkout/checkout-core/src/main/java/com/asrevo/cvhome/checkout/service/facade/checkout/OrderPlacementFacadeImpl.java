package com.asrevo.cvhome.checkout.service.facade.checkout;

import java.util.Locale;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.model.product.ProductReservationReserveResult;
import com.asrevo.cvhome.checkout.entity.customer.Customer;
import com.asrevo.cvhome.checkout.entity.order.Order;
import com.asrevo.cvhome.checkout.model.order.v1.PersistableOrder;
import com.asrevo.cvhome.checkout.service.facade.order.OrderFacade;
import com.asrevo.cvhome.checkout.service.facade.order.OrderInventoryOrchestrator;
import com.asrevo.cvhome.checkout.service.facade.order.model.OrderProcessingResult;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateResult;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.services.payment.ExternalPaymentGatewayService;
import com.asrevo.cvhome.store.core.entity.common.InventoryStatus;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
import com.asrevo.cvhome.store.core.exception.ServiceException;

import lombok.extern.slf4j.Slf4j;

@Service("orderPlacementFacade")
@Slf4j
public class OrderPlacementFacadeImpl implements OrderPlacementFacade {

    private static final String QUERY_PARAM_SEPARATOR = "?";

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
                                            Locale locale, String successUrl, String cancelUrl) throws ServiceException {

        Order modelOrder = orderFacade.saveOrder(order, customer, store, language);

        ProductReservationReserveResult result = orderInventoryOrchestrator.reserveProduct(store, modelOrder);

        if (!result.status()) {
            updateOrderStatus(modelOrder, OrderStatus.CANCELLED, InventoryStatus.RESERVATION_FAILED, PaymentStatus.FAILED);
            return new OrderProcessingResult(modelOrder);
        }
        updateOrderStatus(modelOrder, OrderStatus.CREATED, InventoryStatus.RESERVED, PaymentStatus.PENDING);


        PaymentInitiateResult paymentResponse = doOrderPaymentInitiate(modelOrder, result, successUrl, cancelUrl);

        switch (paymentResponse.status()) {
            case PAID:
                log.info("Payment PAID for order {}. Marking as CONFIRMED.", modelOrder.getId());
                try {
                    orderInventoryOrchestrator.updateOrderStatusWithReservationCommit(modelOrder.getId(), store, OrderStatus.CONFIRMED,
                            PaymentStatus.PAID);
                    modelOrder.setStatus(OrderStatus.CONFIRMED);
                    modelOrder.setInventoryStatus(InventoryStatus.COMMITTED);
                    modelOrder.setPaymentStatus(PaymentStatus.PAID);
                } catch (Exception e) {
                    log.error("Failed to commit reservation for PAID order {}. Manual intervention required.", modelOrder.getId(), e);
                    // Ensure local status reflects payment even if catalog commit failed
                    updateOrderStatus(modelOrder, OrderStatus.PENDING_PAYMENT, InventoryStatus.RESERVED, PaymentStatus.PAID);
                }
                break;

            case PENDING:
                log.info("Payment Pending order {}.", modelOrder.getId());
                updateOrderStatus(modelOrder, paymentResponse, OrderStatus.PENDING_PAYMENT, InventoryStatus.RESERVED,
                        PaymentStatus.PENDING);
                break;

            case FAILED:
                log.warn("Payment failed for order {}. Updating status.", modelOrder.getId());
                try {
                    orderInventoryOrchestrator.updateOrderStatusWithReservationRelease(modelOrder.getId(), store, OrderStatus.CANCELLED,
                            PaymentStatus.FAILED);
                    modelOrder.setStatus(OrderStatus.CANCELLED);
                    modelOrder.setInventoryStatus(InventoryStatus.RELEASED);
                    modelOrder.setPaymentStatus(PaymentStatus.FAILED);
                } catch (Exception e) {
                    log.error("Failed to release reservation for FAILED order {}.", modelOrder.getId(), e);
                    updateOrderStatus(modelOrder, OrderStatus.CANCELLED, InventoryStatus.RESERVATION_FAILED, PaymentStatus.FAILED);
                }
                break;

            default:
                log.warn("Unhandled payment status {} for order {}.", paymentResponse.status(), modelOrder.getId());
                break;
        }

        if (paymentResponse.shouldRedirect()) {
            return new OrderProcessingResult(modelOrder, paymentResponse.redirectUrl());
        }

        return new OrderProcessingResult(modelOrder);
    }

    private PaymentInitiateResult doOrderPaymentInitiate(Order modelOrder, ProductReservationReserveResult result, String successUrl,
                                                         String cancelUrl) {
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .ref(modelOrder.getId().toString())
                .amount(modelOrder.getTotal())
                .currency(modelOrder.getCurrency())
                .paymentType(modelOrder.getPaymentType())
                .expireAt(result.expireAt())
                .successUrl(appendOrderId(successUrl, modelOrder.getId()))
                .cancelUrl(appendOrderId(cancelUrl, modelOrder.getId()))
                .build();

        log.debug("Initiating gateway payment for order {} type {}", modelOrder.getId(), modelOrder.getPaymentType());
        return externalPaymentGatewayService.initiatePayment(modelOrder.getStoreMerchantId(), paymentRequest);

    }

    private String appendOrderId(String url, Long orderId) {
        String separator = url.contains(QUERY_PARAM_SEPARATOR) ? "&" : QUERY_PARAM_SEPARATOR;
        return "%s%sorderId=%d".formatted(url, separator, orderId);
    }

    private void updateOrderStatus(Order modelOrder, OrderStatus orderStatus, InventoryStatus inventoryStatus,
                                   PaymentStatus paymentStatus) {
        orderFacade.updateOrderStatus(modelOrder.getId(), orderStatus, inventoryStatus, paymentStatus);
        modelOrder.setStatus(orderStatus);
        modelOrder.setInventoryStatus(inventoryStatus);
        modelOrder.setPaymentStatus(paymentStatus);
    }

    private void updateOrderStatus(Order modelOrder, PaymentInitiateResult paymentInitiateResult, OrderStatus orderStatus,
                                   InventoryStatus inventoryStatus,
                                   PaymentStatus paymentStatus) {
        String redirectUri = paymentInitiateResult.redirectUrl();
        orderFacade.updateOrderStatus(modelOrder.getId(), orderStatus, inventoryStatus, paymentStatus, redirectUri);
        modelOrder.setStatus(orderStatus);
        modelOrder.setInventoryStatus(inventoryStatus);
        modelOrder.setPaymentStatus(paymentStatus);
        modelOrder.setRedirectUri(redirectUri);
    }
}
