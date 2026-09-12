package com.asrevo.cvhome.store.core.model.catalog;

import com.asrevo.cvhome.commons.domain.Sku;

public record ReserveProductEntry(Sku sku, Integer reserveQty) {
}
