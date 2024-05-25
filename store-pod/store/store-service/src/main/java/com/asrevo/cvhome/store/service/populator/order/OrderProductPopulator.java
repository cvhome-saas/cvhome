package com.asrevo.cvhome.store.service.populator.order;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductAttribute;
import com.asrevo.cvhome.store.core.entity.catalog.product.price.FinalPrice;
import com.asrevo.cvhome.store.core.entity.catalog.product.price.ProductPrice;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.order.orderproduct.OrderProduct;
import com.asrevo.cvhome.store.core.entity.order.orderproduct.OrderProductAttribute;
import com.asrevo.cvhome.store.core.entity.order.orderproduct.OrderProductPrice;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCartAttributeItem;
import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.store.core.services.catalog.product.ProductService;
import com.asrevo.cvhome.store.core.services.catalog.product.attribute.ProductAttributeService;
import com.asrevo.cvhome.store.utils.AbstractDataPopulator;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Setter
@Getter
public class OrderProductPopulator extends
        AbstractDataPopulator<ShoppingCartItem, OrderProduct> {

    private ProductService productService;
    /*
        private DigitalProductService digitalProductService;
    */
    private ProductAttributeService productAttributeService;


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
    public OrderProduct populate(ShoppingCartItem source, OrderProduct target,
                                 MerchantStore store, Language language) throws ConversionException {

        Assert.notNull(productService, "productService must be set");
//		Assert.notNull(digitalProductService,"digitalProductService must be set");
        Assert.notNull(productAttributeService, "productAttributeService must be set");


        try {
            Product modelProduct = productService.getBySku(source.getSku(), store, language);
            if (modelProduct == null) {
                throw new ConversionException("Cannot get product with sku " + source.getSku());
            }

            if (modelProduct.getMerchantStore().getId().intValue() != store.getId().intValue()) {
                throw new ConversionException("Invalid product with sku " + source.getSku());
            }

/*@TODO ASHRAF

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
            target.setProductName(source.getProduct().getDescriptions().iterator().next().getName());
            target.setProductQuantity(source.getQuantity());
            target.setSku(source.getProduct().getSku());

            FinalPrice finalPrice = source.getFinalPrice();
            if (finalPrice == null) {
                throw new ConversionException("Object final price not populated in shoppingCartItem (source)");
            }
            //Default price
            OrderProductPrice orderProductPrice = orderProductPrice(finalPrice);
            orderProductPrice.setOrderProduct(target);

            Set<OrderProductPrice> prices = new HashSet<>();
            prices.add(orderProductPrice);

            //Other prices
            List<FinalPrice> otherPrices = finalPrice.getAdditionalPrices();
            if (otherPrices != null) {
                for (FinalPrice otherPrice : otherPrices) {
                    OrderProductPrice other = orderProductPrice(otherPrice);
                    other.setOrderProduct(target);
                    prices.add(other);
                }
            }

            target.setPrices(prices);

            //OrderProductAttribute
            Set<ShoppingCartAttributeItem> attributeItems = source.getAttributes();
            if (!CollectionUtils.isEmpty(attributeItems)) {
                Set<OrderProductAttribute> attributes = new HashSet<>();
                for (ShoppingCartAttributeItem attribute : attributeItems) {
                    OrderProductAttribute orderProductAttribute = new OrderProductAttribute();
                    orderProductAttribute.setOrderProduct(target);
                    Long id = attribute.getProductAttributeId();
                    ProductAttribute attr = productAttributeService.getById(id);
                    if (attr == null) {
                        throw new ConversionException("Attribute id " + id + " does not exists");
                    }

                    if (attr.getProduct().getMerchantStore().getId().intValue() != store.getId().intValue()) {
                        throw new ConversionException("Attribute id " + id + " invalid for this store");
                    }

                    orderProductAttribute.setProductAttributeIsFree(attr.isProductAttributeIsFree());
                    orderProductAttribute.setProductAttributeName(attr.getProductOption().getDescriptionsSettoList().getFirst().getName());
                    orderProductAttribute.setProductAttributeValueName(attr.getProductOptionValue().getDescriptionsSettoList().getFirst().getName());
                    orderProductAttribute.setProductAttributePrice(attr.getProductAttributePrice());
                    orderProductAttribute.setProductAttributeWeight(attr.getProductAttributeWeight());
                    orderProductAttribute.setProductOptionId(attr.getProductOption().getId());
                    orderProductAttribute.setProductOptionValueId(attr.getProductOptionValue().getId());
                    attributes.add(orderProductAttribute);
                }
                target.setOrderAttributes(attributes);
            }


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

        ProductPrice productPrice = price.getProductPrice();

        orderProductPrice.setDefaultPrice(productPrice.isDefaultPrice());

        orderProductPrice.setProductPrice(price.getFinalPrice());
        orderProductPrice.setProductPriceCode(productPrice.getCode());
        if (productPrice.getDescriptions() != null && !productPrice.getDescriptions().isEmpty()) {
            orderProductPrice.setProductPriceName(productPrice.getDescriptions().iterator().next().getName());
        }
        if (price.isDiscounted()) {
            orderProductPrice.setProductPriceSpecial(productPrice.getProductPriceSpecialAmount());
            orderProductPrice.setProductPriceSpecialStartDate(productPrice.getProductPriceSpecialStartDate());
            orderProductPrice.setProductPriceSpecialEndDate(productPrice.getProductPriceSpecialEndDate());
        }

        return orderProductPrice;
    }


}
