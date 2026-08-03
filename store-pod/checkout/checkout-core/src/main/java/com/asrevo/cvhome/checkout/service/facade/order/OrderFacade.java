package com.asrevo.cvhome.checkout.service.facade.order;

import java.util.List;

import com.asrevo.cvhome.catalog.api.errors.CatalogApiUnavailableException;
import com.asrevo.cvhome.checkout.entity.customer.Customer;
import com.asrevo.cvhome.checkout.entity.order.Order;
import com.asrevo.cvhome.checkout.errors.OrderNotConvertibleException;
import com.asrevo.cvhome.checkout.errors.OrderNotFoundException;
import com.asrevo.cvhome.checkout.errors.OrderProductNotConvertibleException;
import com.asrevo.cvhome.checkout.errors.OrderProductPriceMissingException;
import com.asrevo.cvhome.checkout.errors.PriceNotFormattableException;
import com.asrevo.cvhome.checkout.errors.ShoppingCartNotFoundException;
import com.asrevo.cvhome.checkout.model.order.OrderCriteria;
import com.asrevo.cvhome.checkout.model.order.history.PersistableOrderStatusHistory;
import com.asrevo.cvhome.checkout.model.order.history.ReadableOrderStatusHistory;
import com.asrevo.cvhome.checkout.model.order.v0.ReadableOrder;
import com.asrevo.cvhome.checkout.model.order.v0.ReadableOrderList;
import com.asrevo.cvhome.checkout.model.order.v1.PersistableOrder;
import com.asrevo.cvhome.checkout.model.order.v1.ReadableOrderConfirmation;
import com.asrevo.cvhome.checkout.model.order.v1.ReadableOrderStatus;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.entity.common.InventoryStatus;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;

public interface OrderFacade {

    /**
     * Turns a cart into a persisted order.
     *
     * @throws ShoppingCartNotFoundException       the cart id on the order matches nothing in this store
     * @throws OrderNotConvertibleException        the payload could not be assembled into an order
     * @throws OrderProductNotConvertibleException a cart line could not be turned into an order line
     * @throws OrderProductPriceMissingException   the catalog returned no price for a cart line
     *                                             methods still declare it; it disappears when that root is retired
     */
    Order saveOrder(PersistableOrder order, Customer customer, StoreMerchantId store, LanguageCode language)
            throws ShoppingCartNotFoundException, OrderNotConvertibleException,
            OrderProductNotConvertibleException, OrderProductPriceMissingException;

    ReadableOrderConfirmation orderConfirmation(Order order, Customer customer, StoreMerchantId store,
                                                LanguageCode language) throws PriceNotFormattableException;

    ReadableOrderList getReadableOrderList(OrderCriteria criteria, StoreMerchantId store);

    ReadableOrder getReadableOrder(Long orderId, StoreMerchantId store, LanguageCode language)
            throws OrderNotFoundException, PriceNotFormattableException;

    /**
     * Lookup used by the checkout success/cancel redirect pages. Whether this requires
     * authentication is decided by the caller based on the store's requireLoginForOrderPlacement flag.
     */
    ReadableOrderStatus getOrderStatus(Long orderId, StoreMerchantId store) throws OrderNotFoundException;

    ReadableOrder getReadableOrder(Long orderId, Long customerId, StoreMerchantId store, LanguageCode language)
            throws OrderNotFoundException, PriceNotFormattableException;

    List<ReadableOrderStatusHistory> getReadableOrderHistory(Long orderId, StoreMerchantId store,
                                                             LanguageCode language) throws OrderNotFoundException;

    List<ReadableOrderStatusHistory> getReadableOrderHistory(Long orderId, Long customerId, StoreMerchantId store,
                                                             LanguageCode language) throws OrderNotFoundException;

    /**
     * Records a status change, committing or releasing the order's reservation when the new status calls for it.
     *
     * @throws CatalogApiUnavailableException catalog never answered, so the reservation's fate is unknown; the status
     *                                        change itself is already recorded
     */
    void createOrderStatus(PersistableOrderStatusHistory status, Long id, StoreMerchantId store)
            throws OrderNotFoundException, CatalogApiUnavailableException;

    void updateOrderStatus(Long orderId, OrderStatus orderStatus, InventoryStatus inventoryStatus, PaymentStatus paymentStatus);

    void updateOrderStatus(Long orderId, OrderStatus orderStatus, InventoryStatus inventoryStatus, PaymentStatus paymentStatus,
                           String redirectUri);
}