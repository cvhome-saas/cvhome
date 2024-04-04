package com.asrevo.cvhome.store.core.services.shipping;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.shipping.Quote;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.shipping.ShippingSummary;
import com.asrevo.cvhome.store.core.repositories.shipping.ShippingQuoteRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;


@Service("shippingQuoteService")
@Slf4j
public class ShippingQuoteServiceImpl extends SalesManagerEntityServiceImpl<Long, Quote> implements ShippingQuoteService {


    private final ShippingQuoteRepository shippingQuoteRepository;

    private final ShippingService shippingService;

    @Autowired
    public ShippingQuoteServiceImpl(ShippingQuoteRepository repository, ShippingService shippingService) {
        super(repository);
        this.shippingQuoteRepository = repository;
        // TODO Auto-generated constructor stub
        this.shippingService = shippingService;
    }

    @Override
    public ShippingSummary getShippingSummary(Long quoteId, MerchantStore store) throws ServiceException {

        Assert.notNull(quoteId, "quoteId must not be null");

        Quote q = shippingQuoteRepository.getOne(quoteId);


        ShippingSummary quote = null;

        if (q != null) {

            quote = new ShippingSummary();
            quote.setDeliveryAddress(q.getDelivery());
            quote.setShipping(q.getPrice());
            quote.setShippingModule(q.getModule());
            quote.setShippingOption(q.getOptionName());
            quote.setShippingOptionCode(q.getOptionCode());
            quote.setHandling(q.getHandling());

            if (shippingService.hasTaxOnShipping(store)) {
                quote.setTaxOnShipping(true);
            }


        }


        return quote;

    }


}
