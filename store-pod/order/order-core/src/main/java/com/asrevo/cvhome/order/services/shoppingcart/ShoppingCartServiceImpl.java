package com.asrevo.cvhome.order.services.shoppingcart;

import com.asrevo.cvhome.catalog.model.product.product.price.FinalPrice;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.order.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.order.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.order.repositories.shoppingcart.ShoppingCartItemRepository;
import com.asrevo.cvhome.order.repositories.shoppingcart.ShoppingCartRepository;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service("shoppingCartService")
@Slf4j
public class ShoppingCartServiceImpl extends SalesManagerEntityServiceImpl<Long, ShoppingCart>
        implements ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final ExternalProductService productService;
    private final ShoppingCartItemRepository shoppingCartItemRepository;

    @Autowired
    public ShoppingCartServiceImpl(
            ShoppingCartRepository shoppingCartRepository,
            ExternalProductService productService,
            ShoppingCartItemRepository shoppingCartItemRepository) {
        super(shoppingCartRepository);
        this.shoppingCartRepository = shoppingCartRepository;

        this.productService = productService;
        this.shoppingCartItemRepository = shoppingCartItemRepository;
    }

    /**
     * Save or update a {@link ShoppingCart} for a given customer
     */
    @Override
    public void saveOrUpdate(ShoppingCart shoppingCart) throws ServiceException {

        Assert.notNull(shoppingCart, "ShoppingCart must not be null");
        Assert.notNull(
                shoppingCart.getStoreMerchantId(), "ShoppingCart.merchantStore must not be null");

        // @TODO check if we can store ip address
        // shoppingCart.setIpAddress(userContext.getIpAddress());
        if (shoppingCart.getId() == null || shoppingCart.getId() == 0) {
            super.create(shoppingCart);
        } else {
            super.update(shoppingCart);
        }
    }

    /**
     * Get a {@link ShoppingCart} for a given id and Store. Will update the
     * shopping cart prices and items based on the actual inventory. This method
     * will remove the shopping cart if no items are attached.
     */
    @Override
    @Transactional
    public ShoppingCart getById(final Long id, final StoreMerchantId store)
            throws ServiceException {

        try {
            ShoppingCart shoppingCart = shoppingCartRepository.findById(store, id);
            if (shoppingCart == null) {
                return null;
            }
            getPopulatedShoppingCart(shoppingCart, store);

            if (shoppingCart.isObsolete()) {
                delete(shoppingCart);
                return null;
            } else {
                return shoppingCart;
            }

        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    /*
     * @Override
     *
     * @Transactional public ShoppingCart getById(final Long id, MerchantStore
     * store) throws {
     *
     * try { ShoppingCart shoppingCart = shoppingCartRepository.findOne(id); if
     * (shoppingCart == null) { return null; }
     * getPopulatedShoppingCart(shoppingCart);
     *
     * if (shoppingCart.isObsolete()) { delete(shoppingCart); return null; } else {
     * return shoppingCart; } } catch (Exception e) { // TODO Auto-generated catch
     * block e.printStackTrace(); } return null;
     *
     * }
     */

    /**
     * Get a {@link ShoppingCart} for a given code. Will update the shopping cart
     * prices and items based on the actual inventory. This method will remove the
     * shopping cart if no items are attached.
     */
    @Override
    @Transactional
    public ShoppingCart getByCode(final String code, final StoreMerchantId store)
            throws ServiceException {

        try {
            ShoppingCart shoppingCart = shoppingCartRepository.findByCode(store, code);
            if (shoppingCart == null) {
                return null;
            }
            getPopulatedShoppingCart(shoppingCart, store);

            if (shoppingCart.isObsolete()) {
                delete(shoppingCart);
                return null;
            } else {
                return shoppingCart;
            }

        } catch (jakarta.persistence.NoResultException nre) {
            return null;
        } catch (Throwable e) {
            throw new ServiceException(e);
        }
    }

    private ShoppingCart getPopulatedShoppingCart(
            final ShoppingCart shoppingCart, StoreMerchantId store) throws Exception {

        try {

            boolean cartIsObsolete = false;
            if (shoppingCart != null) {

                Set<ShoppingCartItem> items = shoppingCart.getLineItems();
                if (items == null || items.isEmpty()) {
                    shoppingCart.setObsolete(true);
                    return shoppingCart;
                }

                List<String> skus = items.stream().map(ShoppingCartItem::getSku).toList();
                Map<String, FinalPrice> priceMap =
                        productService.getProductsPrice(store, skus).stream()
                                .collect(Collectors.toMap(FinalPrice::getSku, Function.identity()));

                for (ShoppingCartItem item : items) {
                    log.debug("Populate item {}", item.getId());
                    item.setItemPrice(priceMap.get(item.getSku()).getFinalPrice());

                    BigDecimal subTotal =
                            item.getItemPrice().multiply(new BigDecimal(item.getQuantity()));
                    item.setSubTotal(subTotal);

                    log.debug("Obsolete item ? {}", item.isObsolete());
                    if (item.isObsolete()) {
                        cartIsObsolete = true;
                    }
                }

                Set<ShoppingCartItem> refreshedItems = new HashSet<>(items);

                shoppingCart.setLineItems(refreshedItems);
                update(shoppingCart);

                if (cartIsObsolete) {
                    shoppingCart.setObsolete(true);
                }
                return shoppingCart;
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ServiceException(e);
        }

        return shoppingCart;
    }

    @Override
    public ShoppingCartItem populateShoppingCartItem(String sku, BigDecimal price, StoreMerchantId store)
            throws ServiceException {
        Assert.notNull(sku, "Product should not be null");
        Assert.notNull(store, "Store should not be null");

        ShoppingCartItem item = new ShoppingCartItem(sku);
        item.setItemPrice(price);
        return item;
    }

    @Override
    @Transactional
    public void deleteShoppingCartItem(Long id) {

        ShoppingCartItem item = shoppingCartItemRepository.findOne(id);
        if (item != null) {

            // delete
            shoppingCartItemRepository.deleteById(id);
        }
    }
}
