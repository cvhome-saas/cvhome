package com.asrevo.cvhome.catalog.services.product;

import com.asrevo.cvhome.catalog.model.product.ProductReservationStatus;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.ProductReservationList;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/api/v1/private")
public interface ExternalProductReservationService {

	@PostExchange("/reserve")
	ProductReservationStatus reserve(StoreMerchantId store, @RequestBody ProductReservationList productReservation)
			throws ServiceException;

}
