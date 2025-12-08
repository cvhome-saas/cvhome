package com.asrevo.cvhome.checkout.service.mapper.cart;

import com.asrevo.cvhome.catalog.model.product.ProductDetails;
import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.checkout.entity.order.OrderTotal;
import com.asrevo.cvhome.checkout.entity.order.OrderTotalSummary;
import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.checkout.model.order.total.ReadableOrderTotal;
import com.asrevo.cvhome.checkout.model.shoppingcart.ReadableShoppingCart;
import com.asrevo.cvhome.checkout.model.shoppingcart.ReadableShoppingCartItem;
import com.asrevo.cvhome.checkout.services.shoppingcart.ShoppingCartCalculationService;
import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.utils.PriceUtils;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@Slf4j
public class ReadableShoppingCartMapper implements Mapper<ShoppingCart, ReadableShoppingCart> {

	private final ShoppingCartCalculationService shoppingCartCalculationService;

	private final ExternalMerchantStoreService externalMerchantStoreService;

	private final ExternalProductService externalProductService;

	public ReadableShoppingCartMapper(ShoppingCartCalculationService shoppingCartCalculationService,
			ExternalMerchantStoreService externalMerchantStoreService, ExternalProductService externalProductService) {
		this.shoppingCartCalculationService = shoppingCartCalculationService;
		this.externalMerchantStoreService = externalMerchantStoreService;
		this.externalProductService = externalProductService;
	}

	@Override
	public ReadableShoppingCart convert(ShoppingCart source, StoreMerchantId store, LanguageCode language) {
		ReadableShoppingCart destination = new ReadableShoppingCart();
		return this.merge(source, destination, store, language);
	}

	@Override
	public ReadableShoppingCart merge(ShoppingCart source, ReadableShoppingCart destination, StoreMerchantId store,
			LanguageCode language) {
		Assert.notNull(source, "ShoppingCart cannot be null");
		Assert.notNull(destination, "ReadableShoppingCart cannot be null");
		Assert.notNull(store, "store cannot be null");
		Assert.notNull(language, "Language cannot be null");

		destination.setCode(source.getShoppingCartCode());
		int cartQuantity = 0;

		destination.setCustomer(source.getCustomerId());

		try {

			if (!StringUtils.isBlank(source.getPromoCode())) {
				Date promoDateAdded = source.getPromoAdded(); // promo valid 1 day
				if (promoDateAdded == null) {
					promoDateAdded = new Date();
				}
				Instant instant = promoDateAdded.toInstant();
				ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
				LocalDate date = zdt.toLocalDate();
				// date added < date + 1 day
				LocalDate tomorrow = LocalDate.now().plusDays(1);
				if (date.isBefore(tomorrow)) {
					destination.setPromoCode(source.getPromoCode());
				}
			}

			Set<ShoppingCartItem> items = Optional.ofNullable(source.getLineItems()).orElse(Set.of());

			ReadableMerchantStore merchantStore = externalMerchantStoreService.getStore(store);
			for (ShoppingCartItem item : items) {
				ProductDetails detailedProduct = externalProductService.getDetailedProduct(store, item.getSku(),
						language);
				ReadableMinimalProduct minimalProduct = detailedProduct.product();
				if (minimalProduct != null) {
					ReadableShoppingCartItem shoppingCartItem = new ReadableShoppingCartItem();
					BeanUtils.copyProperties(shoppingCartItem, minimalProduct);

					shoppingCartItem.setPrice(item.getItemPrice());
					shoppingCartItem.setFinalPrice(
							PriceUtils.getStoreFormatedAmountWithCurrency(merchantStore, item.getItemPrice()));

					shoppingCartItem.setQuantity(item.getQuantity());

					cartQuantity = cartQuantity + item.getQuantity();

					BigDecimal subTotal = PriceUtils.calculatePriceQuantity(item.getItemPrice(), item.getQuantity());

					// calculate sub total (price * quantity)
					shoppingCartItem.setSubTotal(subTotal);

					shoppingCartItem
						.setDisplaySubTotal(PriceUtils.getStoreFormatedAmountWithCurrency(merchantStore, subTotal));
					destination.getProducts().add(shoppingCartItem);
				}
			}

			// OrdetTotalSummary contains all calculations

			OrderTotalSummary orderSummary = shoppingCartCalculationService.calculate(source, store, language);

			if (CollectionUtils.isNotEmpty(orderSummary.getTotals())) {

				if (orderSummary.getTotals()
					.stream()
					.noneMatch(t -> Constants.OT_DISCOUNT_TITLE.equals(t.getOrderTotalCode()))) {
					// no promo coupon applied
					destination.setPromoCode(null);
				}

				List<ReadableOrderTotal> totals = new ArrayList<>();
				for (OrderTotal t : orderSummary.getTotals()) {
					ReadableOrderTotal total = new ReadableOrderTotal();
					total.setCode(t.getOrderTotalCode());
					total.setValue(t.getValue());
					total.setText(t.getText());
					totals.add(total);
				}
				destination.setTotals(totals);
			}

			destination.setSubtotal(orderSummary.getSubTotal());
			destination.setDisplaySubTotal(
					PriceUtils.getStoreFormatedAmountWithCurrency(merchantStore, orderSummary.getSubTotal()));

			destination.setTotal(orderSummary.getTotal());
			destination
				.setDisplayTotal(PriceUtils.getStoreFormatedAmountWithCurrency(merchantStore, orderSummary.getTotal()));

			destination.setQuantity(cartQuantity);
			destination.setId(source.getId());

			if (source.getOrderId() != null) {
				destination.setOrder(source.getOrderId());
			}

		}
		catch (Exception e) {
			throw new ConversionRuntimeException("An error occurred while converting ReadableShoppingCart", e);
		}

		return destination;
	}

}
