package com.asrevo.cvhome.catalog.services.variant;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.catalog.entity.ProductOption;
import com.asrevo.cvhome.catalog.entity.ProductOptionValue;
import com.asrevo.cvhome.catalog.entity.ProductVariant;
import com.asrevo.cvhome.catalog.errors.DuplicateVariantCombinationException;
import com.asrevo.cvhome.catalog.errors.DuplicateVariantSkuException;
import com.asrevo.cvhome.catalog.errors.VariantLimitExceededException;
import com.asrevo.cvhome.catalog.errors.VariantOptionsInvalidException;
import com.asrevo.cvhome.catalog.model.product.PersistableProductVariant;
import com.asrevo.cvhome.catalog.model.product.PersistableVariantSet;
import com.asrevo.cvhome.catalog.repositories.ProductOptionRepository;
import com.asrevo.cvhome.catalog.repositories.ProductRepository;
import com.asrevo.cvhome.catalog.repositories.ProductVariantRepository;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The atomic axes+set replace: the validation matrix, the id-addressed diff, the default-flag normalization,
 * the guardrails, and the empty-set path that restores the single default variant.
 */
@ExtendWith(MockitoExtension.class)
class ProductVariantServiceImplTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("store-1");

    private static final String COLOR = "color";

    private static final String SIZE = "size";

    private static final String SKU_RED_M = "SKU-RED-M";

    private static final String SKU_RED_L = "SKU-RED-L";

    private static final String SKU_SAME = "SAME";

    private static final String SKU_TAKEN = "TAKEN";

    private static final String SKU_SIMPLE = "SKU-SIMPLE";

    private static final String SKU_A = "A";

    private static final String SKU_B = "B";

    private static final String SKU_X = "X";

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository variantRepository;

    @Mock
    private ProductOptionRepository optionRepository;

    @InjectMocks
    private ProductVariantServiceImpl service;

    private final Product product = product();

    private static Product product() {
        Product product = new Product();
        product.setId(9L);
        product.setStore(STORE);
        ProductVariant defaultVariant = new ProductVariant(product, "SKU-BASE");
        defaultVariant.setId(1L);
        defaultVariant.setDefaultVariant(true);
        product.getVariants().add(defaultVariant);
        return product;
    }

    /** color: red(11) / blue(12) — size: m(21) / l(22). */
    private void vocabulary() {
        ProductOption color = option(1L, COLOR, 11L, "red", 12L, "blue");
        ProductOption size = option(2L, SIZE, 21L, "m", 22L, "l");
        lenient().when(optionRepository.findByStoreAndCode(STORE, COLOR)).thenReturn(Optional.of(color));
        lenient().when(optionRepository.findByStoreAndCode(STORE, SIZE)).thenReturn(Optional.of(size));
    }

    private static ProductOption option(long id, String code, long valueOneId, String valueOne,
                                        long valueTwoId, String valueTwo) {
        ProductOption option = new ProductOption();
        option.setId(id);
        option.setStoreMerchantId(STORE);
        option.setCode(code);
        ProductOptionValue one = new ProductOptionValue(option);
        one.setId(valueOneId);
        one.setCode(valueOne);
        ProductOptionValue two = new ProductOptionValue(option);
        two.setId(valueTwoId);
        two.setCode(valueTwo);
        option.getValues().add(one);
        option.getValues().add(two);
        return option;
    }

    private static PersistableProductVariant variant(Long id, String sku, boolean defaultVariant,
                                                     Long... valueIds) {
        PersistableProductVariant variant = new PersistableProductVariant();
        variant.setId(id);
        variant.setSku(sku);
        variant.setDefaultVariant(defaultVariant);
        variant.setOptionValueIds(List.of(valueIds));
        return variant;
    }

    private static PersistableVariantSet set(List<String> options, PersistableProductVariant... variants) {
        PersistableVariantSet set = new PersistableVariantSet();
        set.setOptions(options);
        set.setVariants(List.of(variants));
        return set;
    }

    private void productFound() {
        when(productRepository.findByStoreAndId(STORE, 9L)).thenReturn(Optional.of(product));
        lenient().when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void replacesTheSetAndKeepsIdAddressedRows() throws Exception {
        productFound();
        vocabulary();

        service.replaceAll(STORE, 9L, set(List.of(COLOR, SIZE),
                variant(1L, SKU_RED_M, true, 11L, 21L),
                variant(null, SKU_RED_L, false, 22L, 11L)));

        assertThat(product.getVariants()).hasSize(2);
        ProductVariant kept = product.getVariants().stream()
                .filter(v -> SKU_RED_M.equals(v.getSku())).findFirst().orElseThrow();
        assertThat(kept.getId()).as("the id-addressed row keeps its identity").isEqualTo(1L);
        assertThat(kept.getOptionSignature()).isEqualTo("11-21");
        ProductVariant created = product.getVariants().stream()
                .filter(v -> SKU_RED_L.equals(v.getSku())).findFirst().orElseThrow();
        assertThat(created.getOptionSignature()).as("signature is canonical whatever the input order")
                .isEqualTo("11-22");
        assertThat(product.getOptionAssignments()).hasSize(2);
        verify(productRepository).save(product);
    }

    @Test
    void aVariantMustCoverEveryAxisWithItsOwnValues() {
        productFound();
        vocabulary();

        assertThatThrownBy(() -> service.replaceAll(STORE, 9L, set(List.of(COLOR, SIZE),
                variant(null, "HALF", true, 11L))))
                .isInstanceOf(VariantOptionsInvalidException.class);

        assertThatThrownBy(() -> service.replaceAll(STORE, 9L, set(List.of(COLOR),
                variant(null, "FOREIGN", true, 21L))))
                .as("a value of an undeclared option is refused")
                .isInstanceOf(VariantOptionsInvalidException.class);

        assertThatThrownBy(() -> service.replaceAll(STORE, 9L, set(List.of(COLOR, SIZE),
                variant(null, "DOUBLED", true, 11L, 12L))))
                .as("two values of one option is refused")
                .isInstanceOf(VariantOptionsInvalidException.class);
    }

    @Test
    void duplicateSkusAndDuplicateCombinationsAreConflicts() {
        productFound();
        vocabulary();

        assertThatThrownBy(() -> service.replaceAll(STORE, 9L, set(List.of(COLOR),
                variant(null, SKU_SAME, true, 11L), variant(null, SKU_SAME, false, 12L))))
                .isInstanceOf(DuplicateVariantSkuException.class);

        assertThatThrownBy(() -> service.replaceAll(STORE, 9L, set(List.of(COLOR),
                variant(null, SKU_A, true, 11L), variant(null, SKU_B, false, 11L))))
                .isInstanceOf(DuplicateVariantCombinationException.class);
    }

    @Test
    void aSkuOwnedByAnotherProductIsAConflict() {
        productFound();
        vocabulary();
        Product other = new Product();
        other.setId(77L);
        ProductVariant taken = new ProductVariant(other, SKU_TAKEN);
        when(variantRepository.findByStoreAndSku(STORE, SKU_TAKEN)).thenReturn(Optional.of(taken));

        assertThatThrownBy(() -> service.replaceAll(STORE, 9L, set(List.of(COLOR),
                variant(null, SKU_TAKEN, true, 11L))))
                .isInstanceOf(DuplicateVariantSkuException.class);
    }

    @Test
    void theGuardrailsCapAxesAndCombinations() {
        productFound();
        PersistableVariantSet tooManyOptions = set(List.of("a", "b", "c", "d", "e"),
                variant(null, SKU_X, true, 1L));
        assertThatThrownBy(() -> service.replaceAll(STORE, 9L, tooManyOptions))
                .isInstanceOf(VariantLimitExceededException.class);

        vocabulary();
        PersistableVariantSet tooManyVariants = new PersistableVariantSet();
        tooManyVariants.setOptions(List.of(COLOR));
        tooManyVariants.setVariants(IntStream.rangeClosed(1, 101)
                .mapToObj(i -> variant(null, "S%d".formatted(i), false, 11L)).toList());
        assertThatThrownBy(() -> service.replaceAll(STORE, 9L, tooManyVariants))
                .isInstanceOf(VariantLimitExceededException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    void exactlyOneDefaultSurvivesWithSortOrderFallback() throws Exception {
        productFound();
        vocabulary();

        // none flagged: display-order first is promoted
        service.replaceAll(STORE, 9L, set(List.of(COLOR),
                variant(1L, SKU_A, false, 11L), variant(null, SKU_B, false, 12L)));
        assertThat(product.getVariants().stream().filter(ProductVariant::isDefaultVariant))
                .singleElement().extracting(ProductVariant::getSku).isEqualTo(SKU_A);

        // two flagged: the first flagged wins, the rest are unflagged
        service.replaceAll(STORE, 9L, set(List.of(COLOR),
                variant(null, "C", true, 11L), variant(null, "D", true, 12L)));
        assertThat(product.getVariants().stream().filter(ProductVariant::isDefaultVariant)).hasSize(1);
    }

    @Test
    void emptyAxesRestoreTheSingleDefaultVariant() throws Exception {
        productFound();
        vocabulary();
        service.replaceAll(STORE, 9L, set(List.of(COLOR),
                variant(1L, SKU_A, true, 11L), variant(null, SKU_B, false, 12L)));
        assertThat(product.getVariants()).hasSize(2);

        service.replaceAll(STORE, 9L, set(List.of(), variant(null, SKU_SIMPLE, true)));

        assertThat(product.getVariants()).hasSize(1);
        ProductVariant only = product.getVariants().iterator().next();
        assertThat(only.getSku()).isEqualTo(SKU_SIMPLE);
        assertThat(only.isDefaultVariant()).isTrue();
        assertThat(only.getOptionSignature()).isEqualTo(ProductVariant.DEFAULT_SIGNATURE);
        assertThat(only.getOptionValues()).isEmpty();
        assertThat(product.getOptionAssignments()).isEmpty();
    }

    @Test
    void axesWithoutVariantsAndCombinationsWithoutAxesAreRefused() {
        productFound();
        vocabulary();

        assertThatThrownBy(() -> service.replaceAll(STORE, 9L, set(List.of(COLOR))))
                .isInstanceOf(VariantOptionsInvalidException.class);

        assertThatThrownBy(() -> service.replaceAll(STORE, 9L, set(List.of(),
                variant(null, SKU_X, true, 11L))))
                .isInstanceOf(VariantOptionsInvalidException.class);
    }
}
