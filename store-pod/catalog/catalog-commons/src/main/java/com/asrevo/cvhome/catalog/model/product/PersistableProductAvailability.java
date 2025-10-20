package com.asrevo.cvhome.catalog.model.product;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersistableProductAvailability implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private StoreMerchantId store;

	private List<PersistableProductAvailabilityEntry> list;

}
