package com.asrevo.cvhome.catalog.services.product;

import com.asrevo.cvhome.catalog.model.product.*;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.ProductReservationList;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/api/v1")
public interface ExternalProductService {

	@GetExchange("/detailed-product")
	ProductDetails getDetailedProduct(StoreMerchantId store, @RequestParam("sku") String sku, LanguageCode lang);

	@PostExchange("/reserve")
	ProductReservationStatus reserve(StoreMerchantId store, @RequestBody ProductReservationList productReservation)
			throws ServiceException;

}
