package com.asrevo.cvhome.checkout.service.populator.order;

import com.asrevo.cvhome.catalog.model.product.ProductDetails;
import com.asrevo.cvhome.catalog.model.product.ReadableImage;
import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.checkout.entity.order.orderproduct.OrderProduct;
import com.asrevo.cvhome.checkout.entity.order.orderproduct.OrderProductAttribute;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderProduct;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderProductAttribute;
import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import com.asrevo.cvhome.store.utils.PriceUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

/**
 * Use mappers
 *
 * @author carlsamson
 */
@Getter
@AllArgsConstructor
@Component
public class ReadableOrderProductPopulator
		extends AbstractDataPopulator<OrderProduct, StoreMerchantId, ReadableOrderProduct> {

	private final ExternalProductService externalProductService;

	private final ImageFilePath imageUtils;

	private final ExternalMerchantStoreService externalMerchantStoreService;

	@Override
	public ReadableOrderProduct populate(OrderProduct source, ReadableOrderProduct target, StoreMerchantId store,
			LanguageCode language) throws ConversionException {

		Validate.notNull(externalProductService, "Requires ProductService");
		Validate.notNull(imageUtils, "Requires imageUtils");
		target.setId(source.getId());
		target.setOrderedQuantity(source.getProductQuantity());
		target.setPrice(PriceUtils.getStoreFormatedAmountWithCurrency(externalMerchantStoreService.getStore(store),
				source.getOneTimeCharge()));
		target.setProductName(source.getProductName());
		target.setSku(source.getSku());

		// subtotal = price * quantity
		BigDecimal subTotal = source.getOneTimeCharge();
		subTotal = subTotal.multiply(new BigDecimal(source.getProductQuantity()));

		try {
			String subTotalPrice = PriceUtils
				.getStoreFormatedAmountWithCurrency(externalMerchantStoreService.getStore(store), subTotal);
			target.setSubTotal(subTotalPrice);
		}
		catch (Exception e) {
			throw new ConversionException("Cannot format price", e);
		}

		if (source.getOrderAttributes() != null) {
			List<ReadableOrderProductAttribute> attributes = new ArrayList<>();
			for (OrderProductAttribute attr : source.getOrderAttributes()) {
				ReadableOrderProductAttribute readableAttribute = new ReadableOrderProductAttribute();
				String price = PriceUtils.getStoreFormatedAmountWithCurrency(
						externalMerchantStoreService.getStore(store), attr.getProductAttributePrice());
				readableAttribute.setAttributePrice(price);

				readableAttribute.setAttributeName(attr.getProductAttributeName());
				readableAttribute.setAttributeValue(attr.getProductAttributeValueName());
				attributes.add(readableAttribute);
			}
			target.setAttributes(attributes);
		}

		ProductDetails detailedProduct = externalProductService.getDetailedProduct(store, source.getSku(), language);
		ReadableMinimalProduct read = detailedProduct.product();
		target.setProduct(read);

		List<ReadableImage> images = read.getImages();
		ReadableImage defaultImage = null;
		if (images != null) {
			for (ReadableImage image : images) {
				if (defaultImage == null) {
					defaultImage = image;
				}
				if (image.isDefaultImage()) {
					defaultImage = image;
				}
			}
		}
		if (defaultImage != null) {
			target.setImage(defaultImage.getImageUrl());
		}

		return target;
	}

	@Override
	protected ReadableOrderProduct createTarget() {

		return null;
	}

}
