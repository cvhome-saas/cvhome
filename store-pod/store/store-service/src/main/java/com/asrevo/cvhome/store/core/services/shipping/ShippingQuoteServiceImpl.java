package com.asrevo.cvhome.store.core.services.shipping;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.shipping.Quote;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.shipping.ShippingSummary;
import com.asrevo.cvhome.store.core.repositories.shipping.ShippingQuoteRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import org.springframework.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("shippingQuoteService")
public class ShippingQuoteServiceImpl extends SalesManagerEntityServiceImpl<Long, Quote> implements ShippingQuoteService {

	
	private static final Logger LOGGER = LoggerFactory.getLogger(ShippingQuoteServiceImpl.class);
	
	private ShippingQuoteRepository shippingQuoteRepository;
	
	@Autowired
	private ShippingService shippingService;
	
	@Autowired
	public ShippingQuoteServiceImpl(ShippingQuoteRepository repository) {
		super(repository);
		this.shippingQuoteRepository = repository;
		// TODO Auto-generated constructor stub
	}

	@Override
	public ShippingSummary getShippingSummary(Long quoteId, MerchantStore store) throws ServiceException {
		
		Assert.notNull(quoteId,"quoteId must not be null");
		
		Quote q = shippingQuoteRepository.getOne(quoteId);

		
		ShippingSummary quote = null;
		
		if(q != null) {
			
			quote = new ShippingSummary();
			quote.setDeliveryAddress(q.getDelivery());
			quote.setShipping(q.getPrice());
			quote.setShippingModule(q.getModule());
			quote.setShippingOption(q.getOptionName());
			quote.setShippingOptionCode(q.getOptionCode());
			quote.setHandling(q.getHandling());
			
			if(shippingService.hasTaxOnShipping(store)) {
				quote.setTaxOnShipping(true);
			}
			
			
			
		}
		
		
		return quote;
		
	}


}
