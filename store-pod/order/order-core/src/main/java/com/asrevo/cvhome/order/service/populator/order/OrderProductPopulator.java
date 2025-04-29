package com.asrevo.cvhome.order.service.populator.order;

import com.asrevo.cvhome.catalog.model.product.product.price.FinalPrice;
import com.asrevo.cvhome.catalog.model.product.product.price.SimpleProductPrice;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.order.entity.order.orderproduct.OrderProduct;
import com.asrevo.cvhome.order.entity.order.orderproduct.OrderProductPrice;
import com.asrevo.cvhome.order.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Setter
@Getter
@Component
@AllArgsConstructor
public class OrderProductPopulator
        extends AbstractDataPopulator<ShoppingCartItem, StoreMerchantId, OrderProduct> {
    private final ExternalProductService productService;

    //    private ProductService productService;
    /*
        private DigitalProductService digitalProductService;
    */
    //    private ProductAttributeService productAttributeService;

    /*
    	public DigitalProductService getDigitalProductService() {
    		return digitalProductService;
    	}

    	public void setDigitalProductService(DigitalProductService digitalProductService) {
    		this.digitalProductService = digitalProductService;
    	}
    */

    /**
     * Converts a ShoppingCartItem carried in the ShoppingCart to an OrderProduct
     * that will be saved in the system
     */
    @Override
    public OrderProduct populate(
            ShoppingCartItem source,
            OrderProduct target,
            StoreMerchantId store,
            LanguageCode language)
            throws ConversionException {

        Assert.notNull(productService, "productService must be set");
        //		Assert.notNull(digitalProductService,"digitalProductService must be set");
        //        Assert.notNull(productAttributeService, "productAttributeService must be set");

        try {

            /*@TODO ASHRAF

                        Product modelProduct = productService.getBySku(source.getSku(), store.getId(), language);
                        if (modelProduct == null) {
                            throw new ConversionException("Cannot get product with sku " + source.getSku());
                        }

                        if (!Objects.equals(modelProduct.getStore(), store.getId())) {
                            throw new ConversionException("Invalid product with sku " + source.getSku());
                        }

            			DigitalProduct digitalProduct = digitalProductService.getByProduct(store, modelProduct);

            			if(digitalProduct!=null) {
            				OrderProductDownload orderProductDownload = new OrderProductDownload();
            				orderProductDownload.setOrderProductFilename(digitalProduct.getProductFileName());
            				orderProductDownload.setOrderProduct(target);
            				orderProductDownload.setDownloadCount(0);
            				orderProductDownload.setMaxdays(Constants.MAX_DOWNLOAD_DAYS);
            				target.getDownloads().add(orderProductDownload);
            			}
            */
            target.setOneTimeCharge(source.getItemPrice());
            target.setProductName("Product " + source.getSku());
            target.setProductQuantity(source.getQuantity());
            target.setSku(source.getSku());

            FinalPrice finalPrice = productService.getProductPrice(store, source.getSku());
            if (finalPrice == null) {
                throw new ConversionException(
                        "Object final price not populated in shoppingCartItem (source)");
            }
            // Default price
            OrderProductPrice orderProductPrice = orderProductPrice(finalPrice);
            orderProductPrice.setOrderProduct(target);

            Set<OrderProductPrice> prices = new HashSet<>();
            prices.add(orderProductPrice);

            // Other prices
            List<FinalPrice> otherPrices = finalPrice.getAdditionalPrices();
            if (otherPrices != null) {
                for (FinalPrice otherPrice : otherPrices) {
                    OrderProductPrice other = orderProductPrice(otherPrice);
                    other.setOrderProduct(target);
                    prices.add(other);
                }
            }

            target.setPrices(prices);

        } catch (Exception e) {
            throw new ConversionException(e);
        }

        return target;
    }

    @Override
    protected OrderProduct createTarget() {
        return null;
    }

    private OrderProductPrice orderProductPrice(FinalPrice price) {

        OrderProductPrice orderProductPrice = new OrderProductPrice();

        SimpleProductPrice productPrice = price.getProductPrice();

        orderProductPrice.setDefaultPrice(productPrice.isDefaultPrice());

        orderProductPrice.setProductPrice(price.getFinalPrice());
        orderProductPrice.setProductPriceCode(productPrice.getCode());
        //        if (productPrice.getDescriptions() != null &&
        // !productPrice.getDescriptions().isEmpty()) {
        //
        // orderProductPrice.setProductPriceName(productPrice.getDescriptions().iterator().next().getName());
        //        }
        if (price.isDiscounted()) {
            orderProductPrice.setProductPriceSpecial(productPrice.getProductPriceSpecialAmount());
            orderProductPrice.setProductPriceSpecialStartDate(
                    productPrice.getProductPriceSpecialStartDate());
            orderProductPrice.setProductPriceSpecialEndDate(
                    productPrice.getProductPriceSpecialEndDate());
        }

        return orderProductPrice;
    }
}
