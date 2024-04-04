package com.asrevo.cvhome.store.service.populator.order.transaction;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.order.Order;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;
import com.asrevo.cvhome.store.core.entity.payments.Transaction;
import com.asrevo.cvhome.store.core.entity.payments.TransactionType;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.order.transaction.PersistableTransaction;
import com.asrevo.cvhome.store.core.model.order.transaction.ReadableTransaction;
import com.asrevo.cvhome.store.core.services.catalog.pricing.PricingService;
import com.asrevo.cvhome.store.core.services.order.OrderService;
import com.asrevo.cvhome.store.utils.AbstractDataPopulator;
import com.asrevo.cvhome.store.utils.DateUtil;
import org.springframework.util.Assert;


public class ReadableTransactionPopulator extends AbstractDataPopulator<Transaction, ReadableTransaction> {

	
	private OrderService orderService;
	private PricingService pricingService;
	
	@Override
	public ReadableTransaction populate(Transaction source, ReadableTransaction target, MerchantStore store,
			Language language) throws ConversionException {

		
		Assert.notNull(source,"PersistableTransaction must not be null");
		Assert.notNull(orderService,"OrderService must not be null");
		Assert.notNull(pricingService,"OrderService must not be null");
		
		if(target == null) {
			target = new ReadableTransaction();
		}
		
		
		try {
			

			target.setAmount(pricingService.getDisplayAmount(source.getAmount(), store));
			target.setDetails(source.getDetails());
			target.setPaymentType(source.getPaymentType());
			target.setTransactionType(source.getTransactionType());
			target.setTransactionDate(DateUtil.formatDate(source.getTransactionDate()));
			target.setId(source.getId());
			
			if(source.getOrder() != null) {
				target.setOrderId(source.getOrder().getId());

			}
			
			return target;
			
			
		
		} catch(Exception e) {
			throw new ConversionException(e);
		}
		
	}

	@Override
	protected ReadableTransaction createTarget() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public OrderService getOrderService() {
		return orderService;
	}

	public void setOrderService(OrderService orderService) {
		this.orderService = orderService;
	}

	public PricingService getPricingService() {
		return pricingService;
	}

	public void setPricingService(PricingService pricingService) {
		this.pricingService = pricingService;
	}

}
