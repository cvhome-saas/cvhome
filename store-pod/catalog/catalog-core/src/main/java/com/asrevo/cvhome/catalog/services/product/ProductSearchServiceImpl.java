package com.asrevo.cvhome.catalog.services.product;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.catalog.entity.Category;
import com.asrevo.cvhome.catalog.entity.Manufacturer;
import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.catalog.entity.ProductType;
import com.asrevo.cvhome.catalog.model.product.ProductSearchCriteria;
import com.asrevo.cvhome.catalog.model.product.ProductSearchSort;
import com.asrevo.cvhome.catalog.model.product.ReadableFacetBucket;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductSearchResult;
import com.asrevo.cvhome.catalog.model.product.ReadableProductSuggestion;
import com.asrevo.cvhome.catalog.model.product.ReadableSearchFacets;
import com.asrevo.cvhome.catalog.repositories.CategoryRepository;
import com.asrevo.cvhome.catalog.repositories.ManufacturerRepository;
import com.asrevo.cvhome.catalog.repositories.ProductFacetRepository;
import com.asrevo.cvhome.catalog.repositories.ProductRepository;
import com.asrevo.cvhome.catalog.repositories.ProductSearchIndexRepository;
import com.asrevo.cvhome.catalog.repositories.ProductSpecifications;
import com.asrevo.cvhome.catalog.repositories.ProductTypeRepository;
import com.asrevo.cvhome.catalog.services.image.ImageMapper;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Product search over Postgres full text.
 *
 * @see ProductSearchService for why this is behind an interface
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProductSearchServiceImpl implements ProductSearchService {

    /**
     * Below this a trigram match stops looking like the shopper's own typo and starts looking like a different
     * product. It is read against whole words — see {@link ProductSearchIndexRepository#bestNearMiss}, which is
     * where a floor this low stopped being safe on a query of three or four characters.
     */
    private static final float SIMILARITY_FLOOR = 0.3f;

    private static final int MAX_SUGGESTIONS = 10;

    private static final String ID = "id";

    private static final String DATE_AVAILABLE = "dateAvailable";

    private final ProductRepository productRepository;

    private final ProductSearchIndexRepository searchIndexRepository;

    private final ProductFacetRepository facetRepository;

    private final CategoryRepository categoryRepository;

    private final ManufacturerRepository manufacturerRepository;

    private final ProductTypeRepository productTypeRepository;

    private final ProductSearchIndexer indexer;

    private final ProductMapper productMapper;

    private final ImageMapper imageMapper;

    @Override
    @Transactional(readOnly = true)
    public ReadableProductSearchResult search(StoreMerchantId store, ProductSearchCriteria criteria,
                                              LanguageCode language, Pageable pageable) {
        ReadableProductSearchResult result = runSearch(store, criteria, language, pageable, criteria.trimmedQuery());

        if (result.getTotalElements() > 0 || !criteria.hasQuery()) {
            return result;
        }

        // Nothing matched. Before telling a shopper their shop has none of what they asked for, try the two
        // things that are usually actually wrong: a typo, or a language the merchant has not written copy in.
        Optional<String> nearMiss = searchIndexRepository.bestNearMiss(store.getId(), language.code(),
                criteria.trimmedQuery(), SIMILARITY_FLOOR);
        if (nearMiss.isPresent()) {
            ReadableProductSearchResult corrected = runSearch(store, criteria, language, pageable, nearMiss.get());
            if (corrected.getTotalElements() > 0) {
                corrected.setDidYouMean(nearMiss.get());
                return corrected;
            }
        }

        Optional<String> otherLanguage = searchIndexRepository.richestLanguageOtherThan(store.getId(), language.code());
        if (otherLanguage.isPresent()) {
            LanguageCode fallback = new LanguageCode(otherLanguage.get());
            ReadableProductSearchResult translated =
                    runSearch(store, criteria, fallback, pageable, criteria.trimmedQuery());
            if (translated.getTotalElements() > 0) {
                return translated;
            }
        }
        return result;
    }

    private ReadableProductSearchResult runSearch(StoreMerchantId store, ProductSearchCriteria criteria,
                                                  LanguageCode language, Pageable pageable, String queryText) {
        Specification<Product> spec = filters(store, criteria)
                .and(ProductSpecifications.matchesText(queryText, store, language));

        ProductSearchSort sort = ProductSearchSort.orDefault(criteria.getSort());
        boolean byRelevance = sort == ProductSearchSort.RELEVANCE && queryText != null && !queryText.isBlank();

        Page<Product> page = productRepository.findAll(
                byRelevance ? spec.and(orderByRelevance(queryText, store, language)) : spec,
                byRelevance ? unsorted(pageable) : sorted(pageable, sort));

        ReadableProductSearchResult result = new ReadableProductSearchResult();
        result.setContent(map(hydrate(page.getContent()), language));
        result.setSize(result.getContent().size());
        result.setTotalElements(page.getTotalElements());
        result.setTotalPages(page.getTotalPages());
        result.setPageNumber(page.getNumber());
        result.setLanguage(language.code());
        if (criteria.isFacets()) {
            result.setFacets(facets(spec, criteria, language));
        }
        return result;
    }

    /**
     * Everything except the text match — the same predicate the facet counts run over, so the numbers on the
     * rail and the rows on the page cannot disagree.
     */
    private Specification<Product> filters(StoreMerchantId store, ProductSearchCriteria criteria) {
        return Specification.allOf(
                ProductSpecifications.inStore(store),
                ProductSpecifications.available(criteria.getAvailable()),
                ProductSpecifications.inCategories(criteria.getCategoryIds()),
                ProductSpecifications.byManufacturers(criteria.getManufacturerIds()),
                ProductSpecifications.byTypes(criteria.getProductTypeIds()));
    }

    /**
     * Relevance is a function, not a column, so it cannot travel in a {@code Pageable}. It is applied here
     * instead — and only to the real query: Spring Data runs the same specification to build the {@code count},
     * where an {@code order by} is at best wasted and on some drivers invalid.
     */
    private Specification<Product> orderByRelevance(String queryText, StoreMerchantId store, LanguageCode language) {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                query.orderBy(cb.desc(ProductSpecifications.relevance(root, query, cb, queryText, store, language)),
                        cb.asc(root.get(ID)));
            }
            return null;
        };
    }

    private static Pageable unsorted(Pageable pageable) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
    }

    private static Pageable sorted(Pageable pageable, ProductSearchSort sort) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), switch (sort) {
            case NEWEST -> Sort.by(Sort.Direction.DESC, DATE_AVAILABLE);
            case OLDEST -> Sort.by(Sort.Direction.ASC, DATE_AVAILABLE);
            // Relevance without a query to be relevant to; the merchant's own order is the honest answer.
            default -> Sort.by(Sort.Direction.ASC, "sortOrder", ID);
        });
    }

    /**
     * Reloads the page's products with their copy, images, brand and type attached.
     *
     * <p>
     * A second query rather than a fetch join on the first, because fetch-joining a collection and paginating in
     * the same statement makes Hibernate page in memory — it would read the whole catalogue to return one page.
     * Without this the mapper would N+1 instead, once per row, per association.
     * </p>
     */
    private List<Product> hydrate(List<Product> page) {
        if (page.isEmpty()) {
            return List.of();
        }
        List<Long> ids = page.stream().map(Product::getId).toList();
        Map<Long, Product> loaded = productRepository.findAllHydrated(ids).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a));
        return ids.stream().map(loaded::get).filter(Objects::nonNull).toList();
    }

    private List<ReadableProduct> map(List<Product> products, LanguageCode language) {
        return products.stream().map(p -> productMapper.toReadable(p, language)).toList();
    }

    private ReadableSearchFacets facets(Specification<Product> spec, ProductSearchCriteria criteria,
                                        LanguageCode language) {
        ReadableSearchFacets facets = new ReadableSearchFacets();
        facets.setCategories(buckets(facetRepository.countByCategory(spec), criteria.getCategoryIds(),
                ids -> categoryRepository.findAllById(ids).stream()
                        .collect(Collectors.toMap(Category::getId, c -> name(c, language)))));
        facets.setBrands(buckets(facetRepository.countByManufacturer(spec), criteria.getManufacturerIds(),
                ids -> manufacturerRepository.findAllById(ids).stream()
                        .collect(Collectors.toMap(Manufacturer::getId, m -> name(m, language)))));
        facets.setTypes(buckets(facetRepository.countByType(spec), criteria.getProductTypeIds(),
                ids -> productTypeRepository.findAllById(ids).stream()
                        .collect(Collectors.toMap(ProductType::getId, t -> name(t, language)))));
        return facets;
    }

    private List<ReadableFacetBucket> buckets(Map<Long, Long> counts, List<Long> selected,
                                              Function<List<Long>, Map<Long, String>> names) {
        if (counts.isEmpty()) {
            return List.of();
        }
        Map<Long, String> labels = names.apply(List.copyOf(counts.keySet()));
        List<ReadableFacetBucket> buckets = new ArrayList<>();
        counts.forEach((id, count) -> {
            String label = labels.get(id);
            if (label != null) {
                buckets.add(new ReadableFacetBucket(id, label, count,
                        selected != null && selected.contains(id)));
            }
        });
        buckets.sort(Comparator.comparingLong(ReadableFacetBucket::getCount).reversed()
                .thenComparing(ReadableFacetBucket::getName));
        return buckets;
    }

    private static String name(Category category, LanguageCode language) {
        return category.description(language).map(d -> d.getName()).orElse(category.getCode());
    }

    private static String name(Manufacturer manufacturer, LanguageCode language) {
        return manufacturer.description(language).map(d -> d.getName()).orElse(manufacturer.getCode());
    }

    private static String name(ProductType type, LanguageCode language) {
        return type.description(language).map(d -> d.getName()).orElse(type.getCode());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReadableProductSuggestion> suggest(StoreMerchantId store, String query, LanguageCode language,
                                                   int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        int capped = Math.clamp(limit, 1, MAX_SUGGESTIONS);
        List<Long> ids = searchIndexRepository.suggestProductIds(store.getId(), language.code(), query.trim(), capped);
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Long, Product> loaded = productRepository.findAllHydrated(ids).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a));
        // The ids came back ranked; the hydrating query did not promise an order, so restore it.
        Map<Long, ReadableProductSuggestion> ordered = new LinkedHashMap<>();
        for (Long id : ids) {
            Product product = loaded.get(id);
            if (product != null) {
                ordered.put(id, toSuggestion(product, language));
            }
        }
        return List.copyOf(ordered.values());
    }

    /**
     * Built by hand rather than through {@link ProductMapper}, which would drag the dimensions, the unit lookup
     * and every image along for a row that renders a thumbnail and a name. This runs on every keystroke.
     */
    private ReadableProductSuggestion toSuggestion(Product product, LanguageCode language) {
        ReadableProductSuggestion suggestion = new ReadableProductSuggestion();
        suggestion.setId(product.getId());
        suggestion.setSku(product.getSku());
        product.description(language).ifPresent(d -> {
            suggestion.setName(d.getName());
            suggestion.setFriendlyUrl(d.getSeUrl());
        });
        if (product.getManufacturer() != null) {
            product.getManufacturer().description(language).ifPresent(d -> suggestion.setBrand(d.getName()));
        }
        product.defaultImage().map(imageMapper::url).ifPresent(suggestion::setImageUrl);
        return suggestion;
    }

    @Override
    @Transactional
    public void rebuildIndex(StoreMerchantId store) {
        indexer.rebuild(store);
    }
}
