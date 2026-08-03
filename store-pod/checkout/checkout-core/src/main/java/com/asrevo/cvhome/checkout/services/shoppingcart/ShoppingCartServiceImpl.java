package com.asrevo.cvhome.checkout.services.shoppingcart;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.catalog.model.product.ProductDetails;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.checkout.repositories.shoppingcart.ShoppingCartItemRepository;
import com.asrevo.cvhome.checkout.repositories.shoppingcart.ShoppingCartRepository;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;

import lombok.extern.slf4j.Slf4j;

@Service("shoppingCartService")
@Slf4j
public class ShoppingCartServiceImpl extends SalesManagerEntityServiceImpl<Long, ShoppingCart>
        implements ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;

    private final ExternalProductService externalProductService;

    private final ShoppingCartItemRepository shoppingCartItemRepository;

    @Autowired
    public ShoppingCartServiceImpl(ShoppingCartRepository shoppingCartRepository,
                                   ExternalProductService externalProductService, ShoppingCartItemRepository shoppingCartItemRepository) {
        super(shoppingCartRepository);
        this.shoppingCartRepository = shoppingCartRepository;

        this.externalProductService = externalProductService;
        this.shoppingCartItemRepository = shoppingCartItemRepository;
    }

    /**
     * Save or update a {@link ShoppingCart} for a given customer
     */
    @Override
    public void saveOrUpdate(ShoppingCart shoppingCart) throws ServiceException {

        if (shoppingCart.getId() == null || shoppingCart.getId() == 0) {
            super.create(shoppingCart);
        } else {
            super.update(shoppingCart);
        }
    }

    /**
     * Get a {@link ShoppingCart} for a given id and Store. Will update the shopping cart
     * prices and items based on the actual inventory. This method will remove the
     * shopping cart if no items are attached.
     */
    @Override
    public ShoppingCart findCart(Long id, StoreMerchantId store) {
        return shoppingCartRepository.findById(store, id);
    }

    @Override
    public ShoppingCart findCart(String code, StoreMerchantId store) {
        return shoppingCartRepository.findByCode(store, code);
    }


    /**
     * Get a {@link ShoppingCart} for a given code. Will update the shopping cart prices
     * and items based on the actual inventory. This method will remove the shopping cart
     * if no items are attached.
     */
    @Override
    @Transactional
    public ShoppingCart loadCartByCode(final String code, final StoreMerchantId store, LanguageCode languageCode)
            throws ServiceException {

        try {
            ShoppingCart shoppingCart = shoppingCartRepository.findByCode(store, code);
            if (shoppingCart == null) {
                return null;
            }
            getPopulatedShoppingCart(shoppingCart, store, languageCode);

            if (shoppingCart.isObsolete()) {
                delete(shoppingCart);
                return null;
            }
            return shoppingCart;

        } catch (jakarta.persistence.NoResultException _) {
            return null;
        }
    }

    private ShoppingCart getPopulatedShoppingCart(final ShoppingCart shoppingCart, StoreMerchantId store,
                                                  LanguageCode language) throws ServiceException {

        boolean cartIsObsolete = false;
        if (shoppingCart != null) {

            Set<ShoppingCartItem> items = shoppingCart.getLineItems();
            if (items == null || items.isEmpty()) {
                shoppingCart.setObsolete(true);
                return shoppingCart;
            }

            for (ShoppingCartItem item : items) {
                log.debug("Populate item {}", item.getId());
                ProductDetails detailedProduct = externalProductService.getDetailedProduct(store, item.getSku(),
                        language);
                item.setItemPrice(detailedProduct.price().getFinalPrice());

                BigDecimal subTotal = item.getItemPrice().multiply(new BigDecimal(item.getQuantity()));
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

        return shoppingCart;
    }

    @Override
    public ShoppingCartItem populateShoppingCartItem(String sku, BigDecimal price, StoreMerchantId store) {
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
