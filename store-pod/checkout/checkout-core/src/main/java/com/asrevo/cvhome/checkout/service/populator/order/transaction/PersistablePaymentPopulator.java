package com.asrevo.cvhome.checkout.service.populator.order.transaction;

import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.checkout.model.order.transaction.PersistablePayment;
import com.asrevo.cvhome.checkout.model.payments.Payment;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;
import com.asrevo.cvhome.store.core.entity.payments.TransactionType;
import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;
import com.asrevo.cvhome.store.utils.PriceUtils;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Setter
@Getter
@Component
@AllArgsConstructor
public class PersistablePaymentPopulator extends AbstractDataPopulator<PersistablePayment, StoreMerchantId, Payment> {

	private final ExternalProductService externalProductService;

	@Override
	public Payment populate(PersistablePayment source, Payment target, StoreMerchantId store, LanguageCode language)
			throws ConversionException {

		Assert.notNull(source, "PersistablePayment cannot be null");
		if (target == null) {
			target = new Payment();
		}

		try {

			target.setAmount(PriceUtils.getAmount(source.getAmount()));
			target.setModuleName(source.getPaymentModule());
			target.setPaymentType(PaymentType.valueOf(source.getPaymentType()));
			target.setTransactionType(TransactionType.valueOf(source.getTransactionType()));

			Map<String, String> metadata = new HashMap<>();
			metadata.put("paymentToken", source.getPaymentToken());
			target.setPaymentMetaData(metadata);

			return target;

		}
		catch (Exception e) {
			throw new ConversionException(e);
		}
	}

	@Override
	protected Payment createTarget() {
		// TODO Auto-generated method stub
		return null;
	}

}
