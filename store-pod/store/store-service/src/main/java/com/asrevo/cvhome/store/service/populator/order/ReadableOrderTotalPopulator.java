package com.asrevo.cvhome.store.service.populator.order;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.order.OrderTotal;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.order.total.ReadableOrderTotal;
import com.asrevo.cvhome.store.core.services.catalog.pricing.PricingService;
import com.asrevo.cvhome.store.utils.AbstractDataPopulator;
import com.asrevo.cvhome.store.utils.LabelUtils;
import com.asrevo.cvhome.store.utils.LocaleUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.Locale;

@Setter
@Getter
public class ReadableOrderTotalPopulator extends
		AbstractDataPopulator<OrderTotal, ReadableOrderTotal> {
	
	
	private PricingService pricingService;


	private LabelUtils messages;




	@Override
	public ReadableOrderTotal populate(OrderTotal source,
									   ReadableOrderTotal target, MerchantStore store, Language language)
			throws ConversionException {
		
			Assert.notNull(pricingService,"PricingService must be set");
			Assert.notNull(messages,"LabelUtils must be set");
			
			Locale locale = LocaleUtils.getLocale(language);
		
			try {
				
				target.setCode(source.getOrderTotalCode());
				target.setId(source.getId());
				target.setModule(source.getModule());
				target.setOrder(source.getSortOrder());
				

				target.setTitle(messages.getMessage(source.getOrderTotalCode(), locale, source.getOrderTotalCode()));
				target.setText(source.getText());
				
				target.setValue(source.getValue());
				target.setTotal(pricingService.getDisplayAmount(source.getValue(), store));
				
				if(!StringUtils.isBlank(source.getOrderTotalCode())) {
					if(Constants.OT_DISCOUNT_TITLE.equals(source.getOrderTotalCode())) {
						target.setDiscounted(true);
					}
				}
				
			} catch(Exception e) {
				throw new ConversionException(e);
			}
			
			return target;
		
	}

	@Override
	protected ReadableOrderTotal createTarget() {
		return new ReadableOrderTotal();
	}

}