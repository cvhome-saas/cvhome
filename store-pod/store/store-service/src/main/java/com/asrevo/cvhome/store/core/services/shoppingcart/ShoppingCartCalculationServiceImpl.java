/**
 *
 */
package com.asrevo.cvhome.store.core.services.shoppingcart;


import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.order.OrderTotalSummary;
import com.asrevo.cvhome.store.core.services.order.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;


/**
 * <p>
 * Implementation class responsible for calculating state of shopping cart. This
 * class will take care of calculating price of each line items of shopping cart
 * as well any discount including sub-total and total amount.
 * </p>
 *
 * @author Umesh Awasthi
 * @version 1.2
 */
@Service("shoppingCartCalculationService")
@Slf4j
public class ShoppingCartCalculationServiceImpl implements ShoppingCartCalculationService {


    private final ShoppingCartService shoppingCartService;

    private final OrderService orderService;

    public ShoppingCartCalculationServiceImpl(ShoppingCartService shoppingCartService, OrderService orderService) {
        this.shoppingCartService = shoppingCartService;
        this.orderService = orderService;
    }

    /**
     * <p>
     * Method used to recalculate state of shopping cart every time any change
     * has been made to underlying {@link ShoppingCart} object in DB.
     * </p>
     * Following operations will be performed by this method.
     *
     * <li>Calculate price for each {@link ShoppingCartItem} and update it.</li>
     * <p>
     * This method is backbone method for all price calculation related to
     * shopping cart.
     * </p>
     *
     * @param cartModel
     * @param customer
     * @param store
     * @param language
     * @throws ServiceException
     * @see OrderServiceImpl
     */
    @Override
    public OrderTotalSummary calculate(final ShoppingCart cartModel, final Customer customer, final MerchantStore store,
                                       final Language language) throws ServiceException {

        Assert.notNull(cartModel, "cart cannot be null");
        Assert.notNull(cartModel.getLineItems(), "Cart should have line items.");
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(customer, "Customer cannot be null");
        OrderTotalSummary orderTotalSummary = orderService.calculateShoppingCartTotal(cartModel, customer, store,
                language);
        updateCartModel(cartModel);
        return orderTotalSummary;

    }

    /**
     * <p>
     * Method used to recalculate state of shopping cart every time any change
     * has been made to underlying {@link ShoppingCart} object in DB.
     * </p>
     * Following operations will be performed by this method.
     *
     * <li>Calculate price for each {@link ShoppingCartItem} and update it.</li>
     * <p>
     * This method is backbone method for all price calculation related to
     * shopping cart.
     * </p>
     *
     * @param cartModel
     * @param store
     * @param language
     * @throws ServiceException
     * @see OrderServiceImpl
     */
    @Override
    public OrderTotalSummary calculate(final ShoppingCart cartModel, final MerchantStore store, final Language language)
            throws ServiceException {

        Assert.notNull(cartModel, "cart cannot be null");
        Assert.notNull(cartModel.getLineItems(), "Cart should have line items.");
        Assert.notNull(store, "MerchantStore cannot be null");
        OrderTotalSummary orderTotalSummary = orderService.calculateShoppingCartTotal(cartModel, store, language);
        updateCartModel(cartModel);
        return orderTotalSummary;

    }

    private void updateCartModel(final ShoppingCart cartModel) throws ServiceException {
        shoppingCartService.saveOrUpdate(cartModel);
    }

}
