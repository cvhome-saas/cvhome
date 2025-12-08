package com.asrevo.cvhome.checkout.service.mapper.order;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.checkout.entity.order.OrderTotal;
import com.asrevo.cvhome.checkout.model.order.total.ReadableOrderTotal;
import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.utils.LabelUtils;
import com.asrevo.cvhome.store.utils.LocaleUtils;
import com.asrevo.cvhome.store.utils.PriceUtils;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

@Component
public class ReadableOrderTotalMapper implements Mapper<OrderTotal, ReadableOrderTotal> {

	private final ExternalMerchantStoreService externalMerchantStoreService;

	private final LabelUtils messages;

	public ReadableOrderTotalMapper(ExternalMerchantStoreService externalMerchantStoreService, LabelUtils messages) {
		this.externalMerchantStoreService = externalMerchantStoreService;
		this.messages = messages;
	}

	@Override
	public ReadableOrderTotal convert(OrderTotal source, StoreMerchantId store, LanguageCode language) {
		ReadableOrderTotal destination = new ReadableOrderTotal();
		return this.merge(source, destination, store, language);
	}

	@Override
	public ReadableOrderTotal merge(OrderTotal source, ReadableOrderTotal target, StoreMerchantId store,
			LanguageCode language) {

		Validate.notNull(source, "OrderTotal must not be null");
		Validate.notNull(target, "ReadableTotal must not be null");
		Validate.notNull(store, "MerchantStore must not be null");
		Validate.notNull(language, "Language must not be null");

		Locale locale = LocaleUtils.getLocale(language);

		try {

			target.setCode(source.getOrderTotalCode());
			target.setId(source.getId());
			target.setModule(source.getModule());
			target.setOrder(source.getSortOrder());

			target.setTitle(messages.getMessage(source.getOrderTotalCode(), locale, source.getOrderTotalCode()));
			target.setText(source.getText());

			target.setValue(source.getValue());
			target.setTotal(PriceUtils.getStoreFormatedAmountWithCurrency(externalMerchantStoreService.getStore(store),
					source.getValue()));

			if (!StringUtils.isBlank(source.getOrderTotalCode())) {
				if (Constants.OT_DISCOUNT_TITLE.equals(source.getOrderTotalCode())) {
					target.setDiscounted(true);
				}
			}

		}
		catch (Exception e) {
			throw new ConversionRuntimeException(e);
		}

		return target;
	}

}
