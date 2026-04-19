/**
 *
 */
package com.asrevo.cvhome.checkout.service.facade.cart;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.NoResultException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.model.product.ProductDetails;
import com.asrevo.cvhome.catalog.model.product.ReadableProductAvailability;
import com.asrevo.cvhome.catalog.model.product.product.price.FinalPrice;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.checkout.model.shoppingcart.PersistableShoppingCartItem;
import com.asrevo.cvhome.checkout.model.shoppingcart.ReadableShoppingCart;
import com.asrevo.cvhome.checkout.service.mapper.cart.ReadableShoppingCartMapper;
import com.asrevo.cvhome.checkout.services.shoppingcart.ShoppingCartService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Umesh Awasthi
 * @author Carl Samson
 * @version 3.2.0
 * @since 1.0
 */
@Service
@Slf4j
@AllArgsConstructor
public class ShoppingCartFacadeImpl implements ShoppingCartFacade {

    private final ShoppingCartService shoppingCartService;

    private final ReadableShoppingCartMapper readableShoppingCartMapper;

    private final ExternalProductService externalProductService;

    // KEEP -- ENTRY
    private ShoppingCartItem createCartItem(ShoppingCart cartModel, PersistableShoppingCartItem shoppingCartItem,
                                            StoreMerchantId store, LanguageCode language) throws Exception {

        // @TODO we need to merge availability+price in one call
        ProductDetails detailedProduct = externalProductService.getDetailedProduct(store, shoppingCartItem.getProduct(),
                language);
        ReadableProductAvailability availability = detailedProduct.availability();

        if (!availability.isCanBePurchased()) {
            throw new Exception("Product with sku " + availability.getSku()
                    + " is not available or is not properly configured. It contains no" + " inventory");
        }

        if (Objects.nonNull(availability.getDateAvailable()) && Instant.now().isAfter(availability.getDateAvailable())) {
            throw new Exception("Item with sku " + availability.getSku() + " is not available");
        }

        FinalPrice price = detailedProduct.price();

        ShoppingCartItem item = shoppingCartService.populateShoppingCartItem(availability.getSku(),
                price.getFinalPrice(), store);

        item.setQuantity(shoppingCartItem.getQuantity());
        item.setShoppingCart(cartModel);
        item.setSku(availability.getSku());

        return item;
    }

    private ShoppingCartItem getEntryToUpdate(final long entryId, final ShoppingCart cartModel) {
        if (CollectionUtils.isNotEmpty(cartModel.getLineItems())) {
            for (ShoppingCartItem shoppingCartItem : cartModel.getLineItems()) {
                if (shoppingCartItem.getId() == entryId) {
                    log.info("Found line item  for given entry id: {}", entryId);
                    return shoppingCartItem;
                }
            }
        }
        log.info("Unable to find any entry for given Id: {}", entryId);
        return null;
    }

    private ShoppingCart getCartModel(final String cartId, final StoreMerchantId store, LanguageCode language) {
        if (StringUtils.isNotBlank(cartId)) {
            try {
                return shoppingCartService.loadCartByCode(cartId, store, language);
            } catch (ServiceException e) {
                log.error("unable to find any cart asscoiated with this Id: {}", cartId);
                log.error("error while fetching cart model...", e);
                return null;
            } catch (NoResultException _) {
                // nothing
            }
        }
        return null;
    }

    @Override
    public void saveOrUpdateShoppingCart(ShoppingCart cart) throws Exception {
        shoppingCartService.saveOrUpdate(cart);
    }

    @Override
    // KEEP ** ENTRY POINT **
    public ReadableShoppingCart addToCart(PersistableShoppingCartItem item, StoreMerchantId store,
                                          LanguageCode language) {

        Validate.notNull(item, "PersistableShoppingCartItem cannot be null");

        // if cart does not exist create a new one

        ShoppingCart cartModel = new ShoppingCart();
        cartModel.setStoreMerchantId(store);
        cartModel.setShoppingCartCode(uniqueShoppingCartCode());

        if (!StringUtils.isBlank(item.getPromoCode())) {
            cartModel.setPromoCode(item.getPromoCode());
            cartModel.setPromoAdded(Instant.now());
        }

        try {
            return readableShoppingCart(cartModel, item, store, language);
        } catch (Exception e) {
            if (e instanceof ResourceNotFoundException) {
                throw (ResourceNotFoundException) e;
            } else {
                throw new ServiceRuntimeException(e.getMessage(), e);
            }
        }
    }

    @Override
    // KEEP
    public @Nullable ReadableShoppingCart removeShoppingCartItem(String cartCode, String sku, StoreMerchantId merchant,
                                                                 LanguageCode language, boolean returnCart) throws Exception {
        Validate.notNull(cartCode, "Shopping cart code must not be null");
        Validate.notNull(sku, "product sku must not be null");
        Validate.notNull(merchant, "store cannot be null");

        // get cart
        ShoppingCart cart = getCartModel(cartCode, merchant, language);

        if (cart == null) {
            throw new ResourceNotFoundException("Cart code [ " + cartCode + " ] not found");
        }

        Set<ShoppingCartItem> items = new HashSet<>();
        ShoppingCartItem itemToDelete = null;
        for (ShoppingCartItem shoppingCartItem : cart.getLineItems()) {
            if (shoppingCartItem.getSku().equals(sku)) {
                // get cart item
                itemToDelete = getEntryToUpdate(shoppingCartItem.getId(), cart);

                // break;

            } else {
                items.add(shoppingCartItem);
            }
        }
        // delete item
        if (itemToDelete != null) {
            shoppingCartService.deleteShoppingCartItem(itemToDelete.getId());
        }

        // remaining items
        if (!items.isEmpty()) {
            cart.setLineItems(items);
        } else {
            cart.getLineItems().clear();
        }

        shoppingCartService.saveOrUpdate(cart); // update cart with remaining items
        if (!items.isEmpty() & returnCart) {
            return this.getByCode(cartCode, merchant, language);
        }
        return null;
    }

    // KEEP
    private ReadableShoppingCart readableShoppingCart(ShoppingCart cartModel, PersistableShoppingCartItem item,
                                                      StoreMerchantId store, LanguageCode language) throws Exception {

        ShoppingCartItem itemModel = createCartItem(cartModel, item, store, language);

        // need to check if the item is already in the cart
        boolean duplicateFound = false;

        if (!duplicateFound) {
            cartModel.getLineItems().add(itemModel);
        }

        saveShoppingCart(cartModel);

        return readableShoppingCartMapper.convert(cartModel, store, language);
    }

    private ReadableShoppingCart modifyCart(ShoppingCart cartModel, PersistableShoppingCartItem item,
                                            StoreMerchantId store, LanguageCode language) throws Exception {

        ShoppingCartItem itemModel = createCartItem(cartModel, item, store, language);

        boolean itemModified = false;
        // check if existing product
        Set<ShoppingCartItem> items = cartModel.getLineItems();
        if (!CollectionUtils.isEmpty(items)) {
            Set<ShoppingCartItem> newItems = new HashSet<>();
            Set<ShoppingCartItem> removeItems = new HashSet<>();
            for (ShoppingCartItem anItem : items) { // take care of existing
                // product
                if (itemModel.getSku().equals(anItem.getSku())) {
                    if (item.getQuantity() == 0) {
                        // left aside item to be removed
                        // don't add it to new list of item
                        removeItems.add(anItem);
                    } else {
                        // new quantity
                        anItem.setQuantity(item.getQuantity());
                        newItems.add(anItem);
                    }
                    itemModified = true;
                } else {
                    newItems.add(anItem);
                }
            }

            if (!removeItems.isEmpty()) {
                for (ShoppingCartItem emptyItem : removeItems) {
                    shoppingCartService.deleteShoppingCartItem(emptyItem.getId());
                }
            }

            if (!itemModified) {
                newItems.add(itemModel);
            }

            if (newItems.isEmpty()) {
                newItems = null;
            }

            cartModel.setLineItems(newItems);
        } else {
            // new item
            if (item.getQuantity() > 0) {
                cartModel.getLineItems().add(itemModel);
            }
        }

        // if cart items are null just return cart with no items

        // promo code added to the cart but no promo cart exists
        if (!StringUtils.isBlank(item.getPromoCode()) && StringUtils.isBlank(cartModel.getPromoCode())) {
            cartModel.setPromoCode(item.getPromoCode());
            cartModel.setPromoAdded(Instant.now());
        }

        saveShoppingCart(cartModel);

        // refresh cart
        cartModel = shoppingCartService.loadCartByCode(cartModel.getShoppingCartCode(), store, language);

        if (cartModel == null) {
            return null;
        }

        return readableShoppingCartMapper.convert(cartModel, store, language);
    }

    @Override
    // KEEP
    public ReadableShoppingCart modifyCart(String cartCode, PersistableShoppingCartItem item, StoreMerchantId store,
                                           LanguageCode language) throws Exception {

        Validate.notNull(cartCode, "String cart code cannot be null");
        Validate.notNull(item, "PersistableShoppingCartItem cannot be null");

        ShoppingCart cartModel = shoppingCartService.findCart(cartCode, store);
        if (cartModel == null) {
            throw new ResourceNotFoundException("Cart code [" + cartCode + "] not found");
        }

        return modifyCart(cartModel, item, store, language);
    }

    private void saveShoppingCart(ShoppingCart shoppingCart) {
        shoppingCartService.save(shoppingCart);
    }

    private String uniqueShoppingCartCode() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    @Override
    // KEEP
    public ReadableShoppingCart getByCode(String code, StoreMerchantId store, LanguageCode language) throws Exception {

        ShoppingCart cart = shoppingCartService.loadCartByCode(code, store, language);
        ReadableShoppingCart readableCart = null;

        if (cart != null) {

            readableCart = readableShoppingCartMapper.convert(cart, store, language);

            if (!StringUtils.isBlank(cart.getPromoCode())) {
                Instant promoDateAdded = cart.getPromoAdded(); // promo valid 1 day
                if (promoDateAdded == null) {
                    promoDateAdded = Instant.now();
                }
                ZonedDateTime zdt = promoDateAdded.atZone(ZoneId.systemDefault());
                LocalDate date = zdt.toLocalDate();
                // date added < date + 1 day
                LocalDate tomorrow = LocalDate.now().plusDays(1);
                if (date.isBefore(tomorrow)) {
                    readableCart.setPromoCode(cart.getPromoCode());
                }
            }
        }

        return readableCart;
    }

}
