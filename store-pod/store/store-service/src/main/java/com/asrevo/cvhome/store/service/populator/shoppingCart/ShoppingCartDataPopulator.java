/**
 *
 */
package com.asrevo.cvhome.store.service.populator.shoppingCart;

import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOptionDescription;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOptionValueDescription;
import com.asrevo.cvhome.store.core.entity.catalog.product.description.ProductDescription;
import com.asrevo.cvhome.store.core.entity.catalog.product.image.ProductImage;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.order.OrderTotal;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCartAttributeItem;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.order.OrderSummary;
import com.asrevo.cvhome.store.core.model.order.OrderTotalSummary;
import com.asrevo.cvhome.store.core.model.shoppingcart.ShoppingCartAttribute;
import com.asrevo.cvhome.store.core.model.shoppingcart.ShoppingCartData;
import com.asrevo.cvhome.store.core.model.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.store.core.services.catalog.pricing.PricingService;
import com.asrevo.cvhome.store.core.services.shoppingcart.ShoppingCartCalculationService;
import com.asrevo.cvhome.store.utils.AbstractDataPopulator;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author Umesh A
 */

@Deprecated
@Slf4j
public class ShoppingCartDataPopulator extends AbstractDataPopulator<ShoppingCart, ShoppingCartData> {


    private PricingService pricingService;

    private ShoppingCartCalculationService shoppingCartCalculationService;

    private ImageFilePath imageUtils;

    public ImageFilePath getimageUtils() {
        return imageUtils;
    }


    public void setimageUtils(ImageFilePath imageUtils) {
        this.imageUtils = imageUtils;
    }


    @Override
    public ShoppingCartData createTarget() {

        return new ShoppingCartData();
    }


    public ShoppingCartCalculationService getOrderService() {
        return shoppingCartCalculationService;
    }


    public PricingService getPricingService() {
        return pricingService;
    }

    public void setPricingService(final PricingService pricingService) {
        this.pricingService = pricingService;
    }


    @Override
    public ShoppingCartData populate(final ShoppingCart shoppingCart,
                                     final ShoppingCartData cart, final MerchantStore store, final Language language) {

        Assert.notNull(shoppingCart, "Requires ShoppingCart");
        Assert.notNull(language, "Requires Language not null");
        int cartQuantity = 0;
        cart.setCode(shoppingCart.getShoppingCartCode());
        Set<com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCartItem> items = shoppingCart.getLineItems();
        List<ShoppingCartItem> shoppingCartItemsList = Collections.emptyList();
        try {
            if (items != null) {
                shoppingCartItemsList = new ArrayList<ShoppingCartItem>();
                for (com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCartItem item : items) {

                    ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
                    shoppingCartItem.setCode(cart.getCode());
                    shoppingCartItem.setSku(item.getProduct().getSku());
                    shoppingCartItem.setProductVirtual(item.isProductVirtual());

                    shoppingCartItem.setId(item.getId());

                    String itemName = item.getProduct().getProductDescription().getName();
                    if (!CollectionUtils.isEmpty(item.getProduct().getDescriptions())) {
                        for (ProductDescription productDescription : item.getProduct().getDescriptions()) {
                            if (language != null && language.getId().intValue() == productDescription.getLanguage().getId().intValue()) {
                                itemName = productDescription.getName();
                                break;
                            }
                        }
                    }

                    shoppingCartItem.setName(itemName);

                    shoppingCartItem.setPrice(pricingService.getDisplayAmount(item.getItemPrice(), store));
                    shoppingCartItem.setQuantity(item.getQuantity());


                    cartQuantity = cartQuantity + item.getQuantity();

                    shoppingCartItem.setProductPrice(item.getItemPrice());
                    shoppingCartItem.setSubTotal(pricingService.getDisplayAmount(item.getSubTotal(), store));
                    ProductImage image = item.getProduct().getProductImage();
                    if (image != null && imageUtils != null) {
                        String imagePath = imageUtils.buildProductImageUtils(store, item.getProduct().getSku(), image.getProductImage());
                        shoppingCartItem.setImage(imagePath);
                    }
                    Set<ShoppingCartAttributeItem> attributes = item.getAttributes();
                    if (attributes != null) {
                        List<ShoppingCartAttribute> cartAttributes = new ArrayList<>();
                        for (ShoppingCartAttributeItem attribute : attributes) {
                            ShoppingCartAttribute cartAttribute = new ShoppingCartAttribute();
                            cartAttribute.setId(attribute.getId());
                            cartAttribute.setAttributeId(attribute.getProductAttributeId());
                            cartAttribute.setOptionId(attribute.getProductAttribute().getProductOption().getId());
                            cartAttribute.setOptionValueId(attribute.getProductAttribute().getProductOptionValue().getId());
                            List<com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOptionDescription> optionDescriptions = attribute.getProductAttribute().getProductOption().getDescriptionsSettoList();
                            List<com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOptionValueDescription> optionValueDescriptions = attribute.getProductAttribute().getProductOptionValue().getDescriptionsSettoList();
                            if (!CollectionUtils.isEmpty(optionDescriptions) && !CollectionUtils.isEmpty(optionValueDescriptions)) {

                                String optionName = optionDescriptions.get(0).getName();
                                String optionValue = optionValueDescriptions.get(0).getName();

                                for (ProductOptionDescription optionDescription : optionDescriptions) {
                                    if (optionDescription.getLanguage() != null && optionDescription.getLanguage().getId().intValue() == language.getId().intValue()) {
                                        optionName = optionDescription.getName();
                                        break;
                                    }
                                }

                                for (ProductOptionValueDescription optionValueDescription : optionValueDescriptions) {
                                    if (optionValueDescription.getLanguage() != null && optionValueDescription.getLanguage().getId().intValue() == language.getId().intValue()) {
                                        optionValue = optionValueDescription.getName();
                                        break;
                                    }
                                }
                                cartAttribute.setOptionName(optionName);
                                cartAttribute.setOptionValue(optionValue);
                                cartAttributes.add(cartAttribute);
                            }
                        }
                        shoppingCartItem.setShoppingCartAttributes(cartAttributes);
                    }
                    shoppingCartItemsList.add(shoppingCartItem);
                }
            }
            if (CollectionUtils.isNotEmpty(shoppingCartItemsList)) {
                cart.setShoppingCartItems(shoppingCartItemsList);
            }

            if (shoppingCart.getOrderId() != null) {
                cart.setOrderId(shoppingCart.getOrderId());
            }

            OrderSummary summary = new OrderSummary();
            List<com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCartItem> productsList = new ArrayList<>(shoppingCart.getLineItems());
            summary.setProducts(productsList.stream().filter(p -> p.getProduct().isAvailable()).collect(Collectors.toList()));
            OrderTotalSummary orderSummary = shoppingCartCalculationService.calculate(shoppingCart, store, language);

            if (CollectionUtils.isNotEmpty(orderSummary.getTotals())) {
                List<com.asrevo.cvhome.store.core.model.order.total.OrderTotal> totals = new ArrayList<>();
                for (OrderTotal t : orderSummary.getTotals()) {
                    com.asrevo.cvhome.store.core.model.order.total.OrderTotal total = new com.asrevo.cvhome.store.core.model.order.total.OrderTotal();
                    total.setCode(t.getOrderTotalCode());
                    total.setText(t.getText());
                    total.setValue(t.getValue());
                    totals.add(total);
                }
                cart.setTotals(totals);
            }

            cart.setSubTotal(pricingService.getDisplayAmount(orderSummary.getSubTotal(), store));
            cart.setTotal(pricingService.getDisplayAmount(orderSummary.getTotal(), store));
            cart.setQuantity(cartQuantity);
            cart.setId(shoppingCart.getId());
        } catch (ServiceException ex) {
            log.error("Error while converting cart Model to cart Data.." + ex);
            throw new ConversionException("Unable to create cart data", ex);
        }
        return cart;


    }

    public void setShoppingCartCalculationService(final ShoppingCartCalculationService shoppingCartCalculationService) {
        this.shoppingCartCalculationService = shoppingCartCalculationService;
    }


}
