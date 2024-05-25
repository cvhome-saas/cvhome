/**
 *
 */
package com.asrevo.cvhome.store.service.populator.shoppingCart;

import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductAttribute;
import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCartAttributeItem;
import com.asrevo.cvhome.store.core.model.shoppingcart.ShoppingCartAttribute;
import com.asrevo.cvhome.store.core.model.shoppingcart.ShoppingCartData;
import com.asrevo.cvhome.store.core.model.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.store.core.services.catalog.product.ProductService;
import com.asrevo.cvhome.store.core.services.catalog.product.attribute.ProductAttributeService;
import com.asrevo.cvhome.store.core.services.shoppingcart.ShoppingCartService;
import com.asrevo.cvhome.store.utils.AbstractDataPopulator;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Umesh A
 */

@Setter
@Getter
@Service(value = "shoppingCartModelPopulator")
@Slf4j
public class ShoppingCartModelPopulator
        extends AbstractDataPopulator<ShoppingCartData, ShoppingCart> {


    private ShoppingCartService shoppingCartService;

    private Customer customer;
    private ProductService productService;
    private ProductAttributeService productAttributeService;


    @Override
    public ShoppingCart populate(ShoppingCartData shoppingCart, ShoppingCart cartMdel, final MerchantStore store, Language language) {


        // if id >0 get the original from the database, override products
        try {
            if (shoppingCart.getId() > 0 && StringUtils.isNotBlank(shoppingCart.getCode())) {
                cartMdel = shoppingCartService.getByCode(shoppingCart.getCode(), store);
                if (cartMdel == null) {
                    cartMdel = new ShoppingCart();
                    cartMdel.setShoppingCartCode(shoppingCart.getCode());
                    cartMdel.setMerchantStore(store);
                    if (customer != null) {
                        cartMdel.setCustomerId(customer.getId());
                    }
                    shoppingCartService.create(cartMdel);
                }
            } else {
                cartMdel.setShoppingCartCode(shoppingCart.getCode());
                cartMdel.setMerchantStore(store);
                if (customer != null) {
                    cartMdel.setCustomerId(customer.getId());
                }
                shoppingCartService.create(cartMdel);
            }

            List<ShoppingCartItem> items = shoppingCart.getShoppingCartItems();
            Set<com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCartItem> newItems =
                    new HashSet<>();
            if (items != null && !items.isEmpty()) {
                for (ShoppingCartItem item : items) {

                    Set<com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCartItem> cartItems = cartMdel.getLineItems();
                    if (cartItems != null && !cartItems.isEmpty()) {

                        for (com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCartItem dbItem : cartItems) {
                            if (dbItem.getId().longValue() == item.getId()) {
                                dbItem.setQuantity(item.getQuantity());
                                // compare attributes
                                Set<ShoppingCartAttributeItem> attributes =
                                        dbItem.getAttributes();
                                Set<ShoppingCartAttributeItem> newAttributes =
                                        new HashSet<>();
                                List<ShoppingCartAttribute> cartAttributes = item.getShoppingCartAttributes();
                                if (!CollectionUtils.isEmpty(cartAttributes)) {
                                    for (ShoppingCartAttribute attribute : cartAttributes) {
                                        for (ShoppingCartAttributeItem dbAttribute : attributes) {
                                            if (dbAttribute.getId().longValue() == attribute.getId()) {
                                                newAttributes.add(dbAttribute);
                                            }
                                        }
                                    }

                                    dbItem.setAttributes(newAttributes);
                                } else {
                                    dbItem.removeAllAttributes();
                                }
                                newItems.add(dbItem);
                            }
                        }
                    } else {// create new item
                        com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCartItem cartItem =
                                createCartItem(cartMdel, item, store);

                        Set<com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCartItem> lineItems = cartMdel.getLineItems();
                        if (lineItems == null) {
                            lineItems = new HashSet<>();
                            cartMdel.setLineItems(lineItems);
                        }
                        lineItems.add(cartItem);
                        shoppingCartService.update(cartMdel);
                    }
                }// end for
            }// end if
        } catch (Exception ex) {
            log.error("Error while converting cart data to cart model..{}", ex);
            throw new ConversionException("Unable to create cart model", ex);
        }

        return cartMdel;
    }


    private com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCartItem createCartItem(
            ShoppingCart cart,
            ShoppingCartItem shoppingCartItem,
            MerchantStore store) throws Exception {


        Product product = productService.getBySku(shoppingCartItem.getSku(), store, store.getDefaultLanguage());
        if (product == null) {
            throw new Exception("Item with sku " + shoppingCartItem.getSku() + " does not exist");
        }

        if (product.getMerchantStore().getId().intValue() != store.getId().intValue()) {
            throw new Exception("Item with sku " + shoppingCartItem.getSku() + " does not belong to merchant "
                    + store.getId());
        }


        com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCartItem item =
                new com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCartItem(cart, product);
        item.setQuantity(shoppingCartItem.getQuantity());
        item.setItemPrice(shoppingCartItem.getProductPrice());
        item.setShoppingCart(cart);
        item.setSku(shoppingCartItem.getSku());

        // attributes
        List<ShoppingCartAttribute> cartAttributes = shoppingCartItem.getShoppingCartAttributes();
        if (!CollectionUtils.isEmpty(cartAttributes)) {
            Set<ShoppingCartAttributeItem> newAttributes =
                    new HashSet<>();
            for (ShoppingCartAttribute attribute : cartAttributes) {
                ProductAttribute productAttribute = productAttributeService.getById(attribute.getAttributeId());
                if (productAttribute != null
                        && productAttribute.getProduct().getId().longValue() == product.getId().longValue()) {
                    ShoppingCartAttributeItem attributeItem =
                            new ShoppingCartAttributeItem(item,
                                    productAttribute);
                    if (attribute.getAttributeId() > 0) {
                        attributeItem.setId(attribute.getId());
                    }
                    item.addAttributes(attributeItem);
                    //newAttributes.add( attributeItem );
                }

            }

            //item.setAttributes( newAttributes );
        }

        return item;

    }


    @Override
    protected ShoppingCart createTarget() {

        return new ShoppingCart();
    }


}
