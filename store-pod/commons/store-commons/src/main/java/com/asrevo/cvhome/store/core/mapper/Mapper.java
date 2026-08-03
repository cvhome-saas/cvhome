package com.asrevo.cvhome.store.core.mapper;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.errors.ConversionException;

/*
 * The second deliberate category-base declaration, for the same reason as DataPopulator: a mapper SPI generic over two
 * type parameters has no condition to name, and every implementation narrows to its own — PersistableInventoryMapper
 * to InventoryNotConvertibleException, ReadableProductMapper to ProductNotConvertibleException. That narrowing is what
 * callers compile against, so the base never reaches a call site.
 *
 * Before this, convert/merge declared nothing, which is why every mapper in the repo ended in
 * `catch (Exception) -> ConversionRuntimeException` — an unchecked legacy type was the only thing that fitted through
 * the signature. Widening here is what let those 22 sites become named types.
 */
public interface Mapper<S, T> {

    T convert(S source, StoreMerchantId store, LanguageCode language) throws ConversionException;

    T merge(S source, T destination, StoreMerchantId store, LanguageCode language) throws ConversionException;

}
