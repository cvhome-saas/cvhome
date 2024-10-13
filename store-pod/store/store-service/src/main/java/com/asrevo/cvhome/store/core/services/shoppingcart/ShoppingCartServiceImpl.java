package com.asrevo.cvhome.store.core.services.shoppingcart;

import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductAttribute;
import com.asrevo.cvhome.store.core.entity.catalog.product.price.FinalPrice;
import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCartAttributeItem;
import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.common.UserContext;
import com.asrevo.cvhome.store.core.model.shipping.ShippingProduct;
import com.asrevo.cvhome.store.core.repositories.shoppingcart.ShoppingCartAttributeRepository;
import com.asrevo.cvhome.store.core.repositories.shoppingcart.ShoppingCartItemRepository;
import com.asrevo.cvhome.store.core.repositories.shoppingcart.ShoppingCartRepository;
import com.asrevo.cvhome.store.core.services.catalog.pricing.PricingService;
import com.asrevo.cvhome.store.core.services.catalog.product.ProductService;
import com.asrevo.cvhome.store.core.services.catalog.product.attribute.ProductAttributeService;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service("shoppingCartService")
@Slf4j
public class ShoppingCartServiceImpl extends SalesManagerEntityServiceImpl<Long, ShoppingCart>
        implements ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final ProductService productService;
    private final ShoppingCartItemRepository shoppingCartItemRepository;
    private final ShoppingCartAttributeRepository shoppingCartAttributeItemRepository;
    private final PricingService pricingService;
    private final ProductAttributeService productAttributeService;

    @Autowired
    public ShoppingCartServiceImpl(
            ShoppingCartRepository shoppingCartRepository,
            ProductService productService,
            ShoppingCartItemRepository shoppingCartItemRepository,
            ShoppingCartAttributeRepository shoppingCartAttributeItemRepository,
            PricingService pricingService,
            ProductAttributeService productAttributeService) {
        super(shoppingCartRepository);
        this.shoppingCartRepository = shoppingCartRepository;

        this.productService = productService;
        this.shoppingCartItemRepository = shoppingCartItemRepository;
        this.shoppingCartAttributeItemRepository = shoppingCartAttributeItemRepository;
        this.pricingService = pricingService;
        this.productAttributeService = productAttributeService;
    }

    /**
     * Retrieve a {@link ShoppingCart} cart for a given customer
     */
    @Override
    @Transactional
    public ShoppingCart getShoppingCart(final Customer customer, MerchantStore store)
            throws ServiceException {

        try {

            List<ShoppingCart> shoppingCarts =
                    shoppingCartRepository.findByCustomer(customer.getId());

            // elect valid shopping cart
            List<ShoppingCart> validCart =
                    shoppingCarts.stream()
                            .filter((cart) -> cart.getOrderId() == null)
                            .collect(Collectors.toList());

            ShoppingCart shoppingCart = null;

            if (!CollectionUtils.isEmpty(validCart)) {
                shoppingCart = validCart.getFirst();
                getPopulatedShoppingCart(shoppingCart, store);
                if (shoppingCart != null && shoppingCart.isObsolete()) {
                    delete(shoppingCart);
                    shoppingCart = null;
                }
            }

            return shoppingCart;

        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Save or update a {@link ShoppingCart} for a given customer
     */
    @Override
    public void saveOrUpdate(ShoppingCart shoppingCart) throws ServiceException {

        Assert.notNull(shoppingCart, "ShoppingCart must not be null");
        Assert.notNull(
                shoppingCart.getMerchantStore(), "ShoppingCart.merchantStore must not be null");

        try {
            UserContext userContext = UserContext.getCurrentInstance();
            if (userContext != null) {
                shoppingCart.setIpAddress(userContext.getIpAddress());
            }
        } catch (Exception s) {
            log.error("Cannot add ip address to shopping cart ", s);
        }

        if (shoppingCart.getId() == null || shoppingCart.getId() == 0) {
            super.create(shoppingCart);
        } else {
            super.update(shoppingCart);
        }
    }

    /**
     * Get a {@link ShoppingCart} for a given id and MerchantStore. Will update the
     * shopping cart prices and items based on the actual inventory. This method
     * will remove the shopping cart if no items are attached.
     */
    @Override
    @Transactional
    public ShoppingCart getById(final Long id, final MerchantStore store) throws ServiceException {

        try {
            ShoppingCart shoppingCart = shoppingCartRepository.findById(store.getId(), id);
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
    public ShoppingCart getByCode(final String code, final MerchantStore store)
            throws ServiceException {

        try {
            ShoppingCart shoppingCart = shoppingCartRepository.findByCode(store.getId(), code);
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

    @Override
    @Transactional
    public void deleteCart(final ShoppingCart shoppingCart) throws ServiceException {
        ShoppingCart cart = this.getById(shoppingCart.getId());
        if (cart != null) {
            super.delete(cart);
        }
    }

    /*
     * @Override
     *
     * @Transactional public ShoppingCart getByCustomer(final Customer customer)
     * throws ServiceException {
     *
     * try { List<ShoppingCart> shoppingCart =
     * shoppingCartRepository.findByCustomer(customer.getId()); if (shoppingCart ==
     * null) { return null; } return getPopulatedShoppingCart(shoppingCart);
     *
     * } catch (Exception e) { throw new ServiceException(e); } }
     */

    // @TODO ASHRAF
    //	@Transactional(noRollbackFor = { org.springframework.dao.EmptyResultDataAccessException.class
    // })
    private ShoppingCart getPopulatedShoppingCart(
            final ShoppingCart shoppingCart, MerchantStore store) throws Exception {

        try {

            boolean cartIsObsolete = false;
            if (shoppingCart != null) {

                Set<ShoppingCartItem> items = shoppingCart.getLineItems();
                if (items == null || items.isEmpty()) {
                    shoppingCart.setObsolete(true);
                    return shoppingCart;
                }

                // Set<ShoppingCartItem> shoppingCartItems = new
                // HashSet<ShoppingCartItem>();
                for (ShoppingCartItem item : items) {
                    log.debug("Populate item {}", item.getId());
                    getPopulatedItem(item, store);
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
    public ShoppingCartItem populateShoppingCartItem(Product product, MerchantStore store)
            throws ServiceException {
        Assert.notNull(product, "Product should not be null");
        Assert.notNull(product.getMerchantStore(), "Product.merchantStore should not be null");
        Assert.notNull(store, "MerchantStore should not be null");

        ShoppingCartItem item = new ShoppingCartItem(product);
        item.setSku(product.getSku()); // already in the constructor

        // set item price
        FinalPrice price = pricingService.calculateProductPrice(product);
        item.setItemPrice(price.getFinalPrice());
        return item;
    }

    // @TODO ASHRAF
    //	@Transactional
    private void getPopulatedItem(final ShoppingCartItem item, MerchantStore store)
            throws Exception {

        Product product = productService.getBySku(item.getSku(), store, store.getDefaultLanguage());

        if (product == null) {
            item.setObsolete(true);
            return;
        }

        item.setProduct(product);
        item.setSku(product.getSku());

        if (product.isProductVirtual()) {
            item.setProductVirtual(true);
        }

        Set<ShoppingCartAttributeItem> cartAttributes = item.getAttributes();
        Set<ProductAttribute> productAttributes = product.getAttributes();
        List<ProductAttribute> attributesList = new ArrayList<>(); // attributes maintained
        List<ShoppingCartAttributeItem> removeAttributesList = new ArrayList<>(); // attributes
        // to remove
        // DELETE ORPHEANS MANUALLY
        if ((productAttributes != null && !productAttributes.isEmpty())
                || (cartAttributes != null && !cartAttributes.isEmpty())) {
            if (cartAttributes != null) {
                for (ShoppingCartAttributeItem attribute : cartAttributes) {
                    long attributeId = attribute.getProductAttributeId();
                    boolean existingAttribute = false;
                    for (ProductAttribute productAttribute : productAttributes) {

                        if (productAttribute.getId().equals(attributeId)) {
                            attribute.setProductAttribute(productAttribute);
                            attributesList.add(productAttribute);
                            existingAttribute = true;
                            break;
                        }
                    }

                    if (!existingAttribute) {
                        removeAttributesList.add(attribute);
                    }
                }
            }
        }

        // cleanup orphean item
        if (CollectionUtils.isNotEmpty(removeAttributesList)) {
            shoppingCartAttributeItemRepository.deleteAll(removeAttributesList);
        }

        // cleanup detached attributes
        if (CollectionUtils.isEmpty(attributesList)) {
            item.setAttributes(null);
        }

        // set item price
        FinalPrice price = pricingService.calculateProductPrice(product, attributesList);
        item.setItemPrice(price.getFinalPrice());
        item.setFinalPrice(price);

        BigDecimal subTotal = item.getItemPrice().multiply(new BigDecimal(item.getQuantity()));
        item.setSubTotal(subTotal);
    }

    @Override
    public List<ShippingProduct> createShippingProduct(final ShoppingCart cart)
            throws ServiceException {
        Set<ShoppingCartItem> items = cart.getLineItems();
        List<ShippingProduct> shippingProducts = null;
        for (ShoppingCartItem item : items) {
            Product product = item.getProduct();
            if (!product.isProductVirtual() && product.isProductShipeable()) {
                if (shippingProducts == null) {
                    shippingProducts = new ArrayList<>();
                }
                ShippingProduct shippingProduct = new ShippingProduct(product);
                shippingProduct.setQuantity(item.getQuantity());
                shippingProduct.setFinalPrice(item.getFinalPrice());
                shippingProducts.add(shippingProduct);
            }
        }

        return shippingProducts;
    }

    @Override
    public void removeShoppingCart(final ShoppingCart cart) throws ServiceException {
        shoppingCartRepository.delete(cart);
    }

    @Override
    public ShoppingCart mergeShoppingCarts(
            final ShoppingCart userShoppingModel,
            final ShoppingCart sessionCart,
            final MerchantStore store)
            throws Exception {
        if (sessionCart.getCustomerId() != null
                && sessionCart.getCustomerId().equals(userShoppingModel.getCustomerId())) {
            log.info("Session Shopping cart belongs to same logged in user");
            if (CollectionUtils.isNotEmpty(userShoppingModel.getLineItems())
                    && CollectionUtils.isNotEmpty(sessionCart.getLineItems())) {
                return userShoppingModel;
            }
        }

        log.info("Starting merging shopping carts");
        if (CollectionUtils.isNotEmpty(sessionCart.getLineItems())) {
            Set<ShoppingCartItem> shoppingCartItemsSet =
                    getShoppingCartItems(sessionCart, store, userShoppingModel);
            boolean duplicateFound = false;
            if (CollectionUtils.isNotEmpty(shoppingCartItemsSet)) {
                for (ShoppingCartItem sessionShoppingCartItem : shoppingCartItemsSet) {
                    if (CollectionUtils.isNotEmpty(userShoppingModel.getLineItems())) {
                        for (ShoppingCartItem cartItem : userShoppingModel.getLineItems()) {
                            if (cartItem.getProduct().getId().longValue()
                                    == sessionShoppingCartItem.getProduct().getId().longValue()) {
                                if (CollectionUtils.isNotEmpty(cartItem.getAttributes())) {
                                    if (!duplicateFound) {
                                        log.info(
                                                "Dupliate item found..updating exisitng product"
                                                        + " quantity");
                                        cartItem.setQuantity(
                                                cartItem.getQuantity()
                                                        + sessionShoppingCartItem.getQuantity());
                                        duplicateFound = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (!duplicateFound) {
                        log.info("New item found..adding item to Shopping cart");
                        userShoppingModel.getLineItems().add(sessionShoppingCartItem);
                    }
                }
            }
        }
        log.info("Shopping Cart merged successfully.....");
        saveOrUpdate(userShoppingModel);
        removeShoppingCart(sessionCart);

        return userShoppingModel;
    }

    private Set<ShoppingCartItem> getShoppingCartItems(
            final ShoppingCart sessionCart, final MerchantStore store, final ShoppingCart cartModel)
            throws Exception {

        Set<ShoppingCartItem> shoppingCartItemsSet = null;
        if (CollectionUtils.isNotEmpty(sessionCart.getLineItems())) {
            shoppingCartItemsSet = new HashSet<>();
            for (ShoppingCartItem shoppingCartItem : sessionCart.getLineItems()) {
                Product product =
                        productService.getBySku(
                                shoppingCartItem.getSku(), store, store.getDefaultLanguage());
                // .getById(shoppingCartItem.getProductId());
                if (product == null) {
                    throw new Exception(
                            "Item with sku " + shoppingCartItem.getSku() + " does not exist");
                }

                if (product.getMerchantStore().getId().intValue() != store.getId().intValue()) {
                    throw new Exception(
                            "Item with sku "
                                    + shoppingCartItem.getSku()
                                    + " does not belong to merchant "
                                    + store.getId());
                }

                ShoppingCartItem item = populateShoppingCartItem(product, store);
                item.setQuantity(shoppingCartItem.getQuantity());
                item.setShoppingCart(cartModel);

                if (!CollectionUtils.isEmpty(shoppingCartItem.getAttributes())) {
                    List<ShoppingCartAttributeItem> cartAttributes =
                            new ArrayList<>(shoppingCartItem.getAttributes());
                    if (CollectionUtils.isNotEmpty(cartAttributes)) {
                        for (ShoppingCartAttributeItem shoppingCartAttributeItem : cartAttributes) {
                            ProductAttribute productAttribute =
                                    productAttributeService.getById(
                                            shoppingCartAttributeItem.getId());
                            if (productAttribute != null
                                    && productAttribute.getProduct().getId().longValue()
                                            == product.getId().longValue()) {

                                ShoppingCartAttributeItem attributeItem =
                                        new ShoppingCartAttributeItem(item, productAttribute);
                                if (shoppingCartAttributeItem.getId() > 0) {
                                    attributeItem.setId(shoppingCartAttributeItem.getId());
                                }
                                item.addAttributes(attributeItem);
                            }
                        }
                    }
                }

                shoppingCartItemsSet.add(item);
            }
        }
        return shoppingCartItemsSet;
    }

    @Override
    @Transactional
    public void deleteShoppingCartItem(Long id) {

        ShoppingCartItem item = shoppingCartItemRepository.findOne(id);
        if (item != null) {

            if (item.getAttributes() != null) {
                item.getAttributes()
                        .forEach(a -> shoppingCartAttributeItemRepository.deleteById(a.getId()));
                item.getAttributes().clear();
            }

            // refresh
            item = shoppingCartItemRepository.findOne(id);

            // delete
            shoppingCartItemRepository.deleteById(id);
        }
    }
}
