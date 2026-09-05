package com.asrevo.cvhome.checkout.service.facade.product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.checkout.model.product.ProductDetails;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.model.SkuInventory;
import com.asrevo.cvhome.inventory.model.SkuPrice;
import com.asrevo.cvhome.inventory.services.ExternalInventoryService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The bulk composition: a whole sku set costs exactly one catalog call and one inventory call; a sku the
 * catalog no longer knows is absent from the answer, and one without an inventory row comes back not stocked.
 */
@ExtendWith(MockitoExtension.class)
class ProductDetailsComposerImplTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("store-1");

    private static final LanguageCode EN = new LanguageCode("en");

    private static final String SKU_A = "SKU-A";

    private static final String SKU_B = "SKU-B";

    private static final String SKU_GONE = "SKU-GONE";

    @Mock
    private ExternalProductService externalProductService;

    @Mock
    private ExternalInventoryService externalInventoryService;

    @InjectMocks
    private ProductDetailsComposerImpl composer;

    private static ReadableMinimalProduct product(String sku) {
        ReadableMinimalProduct product = new ReadableMinimalProduct();
        product.setId(1L);
        product.setSku(sku);
        return product;
    }

    private static SkuInventory stocked(String sku) {
        return new SkuInventory(sku, 1L, true, true, 5, 1, 0,
                new SkuPrice(BigDecimal.TEN, BigDecimal.TEN, false, 0, null, null, null));
    }

    @Test
    void aWholeSkuSetCostsExactlyTwoCalls() {
        when(externalProductService.getDetailedProducts(STORE, List.of(SKU_A, SKU_B, SKU_GONE), EN))
                .thenReturn(List.of(product(SKU_A), product(SKU_B)));
        when(externalInventoryService.getBySkus(STORE, List.of(SKU_A, SKU_B, SKU_GONE)))
                .thenReturn(List.of(stocked(SKU_A)));

        Map<String, ProductDetails> details =
                composer.getDetailedProducts(STORE, List.of(SKU_A, SKU_B, SKU_GONE, SKU_A), EN);

        verify(externalProductService, times(1)).getDetailedProducts(any(), anyList(), any());
        verify(externalInventoryService, times(1)).getBySkus(any(), anyList());
        assertThat(details).containsOnlyKeys(SKU_A, SKU_B);
        assertThat(details.get(SKU_A).inventory().canBePurchased()).isTrue();
        // no inventory row means not stocked, never an error — the cart still renders the line
        assertThat(details.get(SKU_B).inventory().canBePurchased()).isFalse();
        assertThat(details.get(SKU_B).inventory().quantity()).isZero();
    }

    @Test
    void anEmptySkuSetNeverLeavesTheService() {
        assertThat(composer.getDetailedProducts(STORE, List.of(), EN)).isEmpty();
        verify(externalProductService, times(0)).getDetailedProducts(any(), anyList(), any());
        verify(externalInventoryService, times(0)).getBySkus(any(), anyList());
    }
}
