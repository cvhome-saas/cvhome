package com.asrevo.cvhome.store.service.populator.order;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.image.ProductImage;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.order.orderproduct.OrderProduct;
import com.asrevo.cvhome.store.core.entity.order.orderproduct.OrderProductAttribute;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProduct;
import com.asrevo.cvhome.store.core.model.order.ReadableOrderProduct;
import com.asrevo.cvhome.store.core.model.order.ReadableOrderProductAttribute;
import com.asrevo.cvhome.store.core.services.catalog.pricing.PricingService;
import com.asrevo.cvhome.store.core.services.catalog.product.ProductService;
import com.asrevo.cvhome.store.service.populator.catalog.ReadableProductPopulator;
import com.asrevo.cvhome.store.utils.AbstractDataPopulator;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * Use mappers
 *
 * @author carlsamson
 */
@Setter
@Getter
@Deprecated
public class ReadableOrderProductPopulator
        extends AbstractDataPopulator<OrderProduct, ReadableOrderProduct> {

    private ProductService productService;
    private PricingService pricingService;
    private ImageFilePath imageUtils;

    @Override
    public ReadableOrderProduct populate(
            OrderProduct source,
            ReadableOrderProduct target,
            MerchantStore store,
            Language language)
            throws ConversionException {

        Validate.notNull(productService, "Requires ProductService");
        Validate.notNull(pricingService, "Requires PricingService");
        Validate.notNull(imageUtils, "Requires imageUtils");
        target.setId(source.getId());
        target.setOrderedQuantity(source.getProductQuantity());
        try {
            target.setPrice(pricingService.getDisplayAmount(source.getOneTimeCharge(), store));
        } catch (Exception e) {
            throw new ConversionException("Cannot convert price", e);
        }
        target.setProductName(source.getProductName());
        target.setSku(source.getSku());

        // subtotal = price * quantity
        BigDecimal subTotal = source.getOneTimeCharge();
        subTotal = subTotal.multiply(new BigDecimal(source.getProductQuantity()));

        try {
            String subTotalPrice = pricingService.getDisplayAmount(subTotal, store);
            target.setSubTotal(subTotalPrice);
        } catch (Exception e) {
            throw new ConversionException("Cannot format price", e);
        }

        if (source.getOrderAttributes() != null) {
            List<ReadableOrderProductAttribute> attributes = new ArrayList<>();
            for (OrderProductAttribute attr : source.getOrderAttributes()) {
                ReadableOrderProductAttribute readableAttribute =
                        new ReadableOrderProductAttribute();
                try {
                    String price =
                            pricingService.getDisplayAmount(attr.getProductAttributePrice(), store);
                    readableAttribute.setAttributePrice(price);
                } catch (ServiceException e) {
                    throw new ConversionException("Cannot format price", e);
                }

                readableAttribute.setAttributeName(attr.getProductAttributeName());
                readableAttribute.setAttributeValue(attr.getProductAttributeValueName());
                attributes.add(readableAttribute);
            }
            target.setAttributes(attributes);
        }

        String productSku = source.getSku();
        if (!StringUtils.isBlank(productSku)) {
            Product product;
            try {
                product = productService.getBySku(productSku, store, language);
            } catch (ServiceException e) {
                throw new ServiceRuntimeException(e);
            }
            if (product != null) {

                ReadableProductPopulator populator = new ReadableProductPopulator();
                populator.setPricingService(pricingService);
                populator.setImageUtils(imageUtils);

                ReadableProduct productProxy =
                        populator.populate(product, new ReadableProduct(), store, language);
                target.setProduct(productProxy);

                Set<ProductImage> images = product.getImages();
                ProductImage defaultImage = null;
                if (images != null) {
                    for (ProductImage image : images) {
                        if (defaultImage == null) {
                            defaultImage = image;
                        }
                        if (image.isDefaultImage()) {
                            defaultImage = image;
                        }
                    }
                }
                if (defaultImage != null) {
                    target.setImage(defaultImage.getProductImage());
                }
            }
        }

        return target;
    }

    @Override
    protected ReadableOrderProduct createTarget() {

        return null;
    }
}
