package com.asrevo.cvhome.catalog.services.variant;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.catalog.entity.ProductOption;
import com.asrevo.cvhome.catalog.entity.ProductOptionAssignment;
import com.asrevo.cvhome.catalog.entity.ProductOptionValue;
import com.asrevo.cvhome.catalog.entity.ProductVariant;
import com.asrevo.cvhome.catalog.entity.ProductVariantOptionValue;
import com.asrevo.cvhome.catalog.errors.DuplicateVariantCombinationException;
import com.asrevo.cvhome.catalog.errors.DuplicateVariantSkuException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductOptionNotFoundException;
import com.asrevo.cvhome.catalog.errors.VariantLimitExceededException;
import com.asrevo.cvhome.catalog.errors.VariantOptionsInvalidException;
import com.asrevo.cvhome.catalog.model.product.PersistableProductVariant;
import com.asrevo.cvhome.catalog.model.product.PersistableVariantSet;
import com.asrevo.cvhome.catalog.model.product.ReadableProductVariantDefinition;
import com.asrevo.cvhome.catalog.repositories.ProductOptionRepository;
import com.asrevo.cvhome.catalog.repositories.ProductRepository;
import com.asrevo.cvhome.catalog.repositories.ProductVariantRepository;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {

    /**
     * At most this many axes per product — protects the console matrix and keeps every combination query small.
     */
    public static final int MAX_OPTIONS = 4;

    /**
     * At most this many combinations per product — bounds the PDP availability call and the matrix UI.
     */
    public static final int MAX_VARIANTS = 100;

    private final ProductRepository productRepository;

    private final ProductVariantRepository variantRepository;

    private final ProductOptionRepository optionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ReadableProductVariantDefinition> list(StoreMerchantId store, Long productId,
                                                       LanguageCode language) throws ProductNotFoundException {
        require(store, productId);
        return variantRepository.findByProductIdHydrated(productId).stream()
                .sorted(ProductVariantMapper.DISPLAY_ORDER)
                .map(variant -> ProductVariantMapper.toDefinition(variant, language)).toList();
    }

    @Override
    @Transactional
    public void replaceAll(StoreMerchantId store, Long productId, PersistableVariantSet set)
            throws ProductNotFoundException, ProductOptionNotFoundException, VariantOptionsInvalidException,
            DuplicateVariantSkuException, DuplicateVariantCombinationException, VariantLimitExceededException {
        Product product = require(store, productId);
        List<String> optionCodes = set.getOptions() == null ? List.of() : set.getOptions();
        List<PersistableProductVariant> variants = set.getVariants() == null ? List.of() : set.getVariants();

        if (optionCodes.isEmpty()) {
            restoreDefaultVariant(store, product, variants);
        } else {
            applyCombinations(store, product, optionCodes, variants);
        }
        normalizeDefaultFlag(product);
        productRepository.save(product.searchIndexStale());
    }

    /**
     * No axes: the product sells by its single default variant again. The sku comes from the payload's only
     * entry when one is sent, else the retiring set's default keeps it.
     */
    private void restoreDefaultVariant(StoreMerchantId store, Product product,
                                       List<PersistableProductVariant> variants)
            throws VariantOptionsInvalidException, DuplicateVariantSkuException {
        if (variants.size() > 1 || variants.size() == 1 && !variants.getFirst().getOptionValueIds().isEmpty()) {
            throw VariantOptionsInvalidException.of(variants.getFirst().getSku(),
                    "a product with no options owns exactly one variant with no option values");
        }
        String sku = variants.isEmpty()
                ? product.defaultVariant().map(ProductVariant::getSku).orElse(null)
                : variants.getFirst().getSku();
        if (sku == null) {
            throw VariantOptionsInvalidException.of(null, "no sku for the default variant");
        }
        requireSkuFree(store, product, sku);
        // Reuse an existing row (a same-sku one first, so no delete/insert pair ever races over the sku's
        // unique constraint — Hibernate flushes inserts before deletes).
        ProductVariant keep = product.getVariants().stream()
                .filter(v -> sku.equals(v.getSku())).findFirst()
                .or(product::defaultVariant)
                .orElseGet(() -> new ProductVariant(product, sku));
        product.getVariants().removeIf(v -> v != keep);
        if (!product.getVariants().contains(keep)) {
            product.getVariants().add(keep);
        }
        keep.setSku(sku);
        keep.setSortOrder(0);
        keep.setDefaultVariant(true);
        keep.setOptionSignature(ProductVariant.DEFAULT_SIGNATURE);
        keep.getOptionValues().clear();
        product.getOptionAssignments().clear();
    }

    private void applyCombinations(StoreMerchantId store, Product product, List<String> optionCodes,
                                   List<PersistableProductVariant> variants)
            throws ProductOptionNotFoundException, VariantOptionsInvalidException, DuplicateVariantSkuException,
            DuplicateVariantCombinationException, VariantLimitExceededException {
        requireWithinGuardrails(optionCodes, variants);

        Map<Long, ProductOption> optionsById = new LinkedHashMap<>();
        Map<Long, ProductOptionValue> valuesById = new HashMap<>();
        for (String code : optionCodes) {
            ProductOption option = optionRepository.findByStoreAndCode(store, code)
                    .orElseThrow(() -> ProductOptionNotFoundException.of(code, store));
            optionsById.put(option.getId(), option);
            option.getValues().forEach(value -> valuesById.put(value.getId(), value));
        }

        requireDistinctSkus(store, product, variants);
        requireDistinctSignatures(product, variants);

        // Existing rows are matched by id, else by sku — reusing a same-sku row is what keeps "promote the
        // simple product to combinations, keeping its sku" from racing the sku unique constraint at flush
        // (Hibernate writes inserts before deletes).
        Map<Long, ProductVariant> byId = product.getVariants().stream()
                .filter(v -> v.getId() != null)
                .collect(Collectors.toMap(ProductVariant::getId, Function.identity()));
        Map<String, ProductVariant> bySku = product.getVariants().stream()
                .collect(Collectors.toMap(ProductVariant::getSku, Function.identity(), (a, b) -> a));
        Set<ProductVariant> used = new HashSet<>();
        List<ProductVariant> resolved = new java.util.ArrayList<>();
        for (PersistableProductVariant source : variants) {
            ProductVariant variant = source.getId() == null ? null : byId.get(source.getId());
            if (variant == null) {
                variant = bySku.get(source.getSku());
            }
            if (variant == null || !used.add(variant)) {
                variant = new ProductVariant(product, source.getSku());
                used.add(variant);
            }
            variant.setSku(source.getSku());
            variant.setSortOrder(source.getSortOrder());
            variant.setDefaultVariant(source.isDefaultVariant());
            variant.setOptionSignature(ProductVariant.signatureOf(source.getOptionValueIds()));
            applyChosenValues(variant, source, optionsById, valuesById);
            resolved.add(variant);
        }
        product.getVariants().removeIf(v -> !used.contains(v));
        resolved.forEach(variant -> {
            if (!product.getVariants().contains(variant)) {
                product.getVariants().add(variant);
            }
        });

        applyAssignments(product, optionsById);
    }

    private void requireWithinGuardrails(List<String> optionCodes, List<PersistableProductVariant> variants)
            throws VariantOptionsInvalidException, VariantLimitExceededException {
        if (optionCodes.stream().distinct().count() != optionCodes.size()) {
            throw VariantOptionsInvalidException.of(null, "the same option is declared twice");
        }
        if (optionCodes.size() > MAX_OPTIONS) {
            throw VariantLimitExceededException.options(optionCodes.size(), MAX_OPTIONS);
        }
        if (variants.isEmpty()) {
            throw VariantOptionsInvalidException.of(null, "a product with options needs at least one variant");
        }
        if (variants.size() > MAX_VARIANTS) {
            throw VariantLimitExceededException.variants(variants.size(), MAX_VARIANTS);
        }
    }

    /**
     * Merges the axes in place — the composite key is {@code (product, option)}, so a clear-then-re-add of an
     * unchanged axis would collide with itself at flush time.
     */
    private void applyAssignments(Product product, Map<Long, ProductOption> optionsById) {
        product.getOptionAssignments().removeIf(a -> !optionsById.containsKey(a.getKey().getOptionId()));
        int order = 0;
        for (ProductOption option : optionsById.values()) {
            int sortOrder = order++;
            ProductOptionAssignment kept = product.getOptionAssignments().stream()
                    .filter(a -> option.getId().equals(a.getKey().getOptionId())).findFirst().orElse(null);
            if (kept != null) {
                kept.setSortOrder(sortOrder);
            } else {
                product.getOptionAssignments().add(new ProductOptionAssignment(product, option, sortOrder));
            }
        }
    }

    /**
     * A variant must hold exactly one value of every declared axis — no missing axis, no foreign value, no
     * doubled option. Existing rows are merged in place (the composite key is {@code (variant, option)}, and a
     * clear-then-re-add of the same key would collide at flush time).
     */
    private void applyChosenValues(ProductVariant variant, PersistableProductVariant source,
                                   Map<Long, ProductOption> optionsById, Map<Long, ProductOptionValue> valuesById)
            throws VariantOptionsInvalidException {
        List<Long> valueIds = source.getOptionValueIds();
        if (valueIds == null || valueIds.size() != optionsById.size()) {
            throw VariantOptionsInvalidException.of(source.getSku(),
                    "expected one value per declared option (%d), got %d"
                            .formatted(optionsById.size(), valueIds == null ? 0 : valueIds.size()));
        }
        Map<Long, ProductOptionValue> desired = new LinkedHashMap<>();
        for (Long valueId : valueIds) {
            ProductOptionValue value = valuesById.get(valueId);
            if (value == null) {
                throw VariantOptionsInvalidException.of(source.getSku(),
                        "value %d does not belong to a declared option".formatted(valueId));
            }
            ProductOption option = optionsById.get(value.getOption().getId());
            if (desired.put(option.getId(), value) != null) {
                throw VariantOptionsInvalidException.of(source.getSku(),
                        "two values of option %s".formatted(option.getCode()));
            }
        }
        variant.getOptionValues().removeIf(chosen -> !desired.containsKey(chosen.getKey().getOptionId()));
        for (Map.Entry<Long, ProductOptionValue> entry : desired.entrySet()) {
            ProductVariantOptionValue kept = variant.getOptionValues().stream()
                    .filter(chosen -> entry.getKey().equals(chosen.getKey().getOptionId()))
                    .findFirst().orElse(null);
            if (kept != null) {
                kept.setOptionValue(entry.getValue());
            } else {
                variant.getOptionValues().add(new ProductVariantOptionValue(variant,
                        optionsById.get(entry.getKey()), entry.getValue()));
            }
        }
    }

    private void requireDistinctSkus(StoreMerchantId store, Product product,
                                     List<PersistableProductVariant> variants)
            throws DuplicateVariantSkuException {
        Set<String> seen = new HashSet<>();
        for (PersistableProductVariant variant : variants) {
            if (!seen.add(variant.getSku())) {
                throw DuplicateVariantSkuException.of(variant.getSku(), store);
            }
            requireSkuFree(store, product, variant.getSku());
        }
    }

    /**
     * The sku may already exist only when it belongs to this product (a kept or renamed row); anything else in
     * the store owning it is a conflict — caught here so the caller gets a 409 instead of the DB constraint's
     * opaque 500.
     */
    private void requireSkuFree(StoreMerchantId store, Product product, String sku)
            throws DuplicateVariantSkuException {
        boolean ownedHere = product.getVariants().stream().anyMatch(v -> sku.equals(v.getSku()));
        if (!ownedHere && variantRepository.findByStoreAndSku(store, sku)
                .filter(v -> !Objects.equals(v.getProduct().getId(), product.getId())).isPresent()) {
            throw DuplicateVariantSkuException.of(sku, store);
        }
    }

    private void requireDistinctSignatures(Product product, List<PersistableProductVariant> variants)
            throws DuplicateVariantCombinationException {
        Set<String> seen = new HashSet<>();
        for (PersistableProductVariant variant : variants) {
            String signature = ProductVariant.signatureOf(variant.getOptionValueIds());
            if (!seen.add(signature)) {
                throw DuplicateVariantCombinationException.of(signature, product.getId());
            }
        }
    }

    /**
     * Exactly one default, whatever the payload said: the first flagged one wins, and when none is flagged the
     * display-order first is promoted — the DB's partial unique index would reject anything else anyway.
     */
    private void normalizeDefaultFlag(Product product) {
        List<ProductVariant> ordered = product.getVariants().stream()
                .sorted(ProductVariantMapper.DISPLAY_ORDER).toList();
        ProductVariant chosen = ordered.stream().filter(ProductVariant::isDefaultVariant).findFirst()
                .orElseGet(ordered::getFirst);
        ordered.forEach(variant -> variant.setDefaultVariant(variant == chosen));
    }

    private Product require(StoreMerchantId store, Long id) throws ProductNotFoundException {
        return productRepository.findByStoreAndId(store, id)
                .orElseThrow(() -> ProductNotFoundException.of(id, store));
    }
}
