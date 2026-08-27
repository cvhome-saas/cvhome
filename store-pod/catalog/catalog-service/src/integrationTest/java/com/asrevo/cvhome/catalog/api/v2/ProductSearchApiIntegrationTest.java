package com.asrevo.cvhome.catalog.api.v2;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import com.asrevo.cvhome.catalog.api.CatalogApiSupport;
import com.asrevo.cvhome.catalog.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.catalog.model.product.event.BrandRenamedEvent;
import com.asrevo.cvhome.catalog.model.product.event.ProductSearchIndexPurgedEvent;
import com.asrevo.cvhome.catalog.model.product.event.ProductSearchIndexStaleEvent;
import com.asrevo.cvhome.catalog.service.CatalogSearchOutboxHandler;
import com.asrevo.cvhome.testsupport.annotations.StorageIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.ADMIN;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.CONTENT;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.DESCRIPTION;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.ID;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.MODERATOR;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.NAME;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.SKU;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.STORE_A;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.STORE_B;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.TOTAL_ELEMENTS;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.V1_PRIVATE;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.V2;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.V2_PRIVATE;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.expect;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.json;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.path;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.query;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.scoped;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Product search end to end, against the real schema: the functions, the generated document, the GIN indexes and
 * the Criteria predicates that reach them.
 *
 * <p>
 * The index is maintained through the outbox, which polls, so nothing here races the poller: a test that changes
 * a product hands the event straight to {@link CatalogSearchOutboxHandler}. That an event is registered at all is
 * asserted in the unit tests instead.
 * </p>
 */
@StorageIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class ProductSearchApiIntegrationTest {

    private static final String PRODUCTS = "products";

    private static final String PRODUCT = "product";

    private static final String MANUFACTURER = "manufacturer";

    private static final String EN = "en";

    private static final String AR = "ar";

    /** A page big enough to hold everything these cases look for. */
    private static final String A_PAGE = "count=24";

    private static final String EVERYTHING_WITH_FACETS = "q=&facets=true&count=1";

    /** Larger than store A's catalogue, so a leak from another store would have room to show up. */
    private static final String EVERY_RESULT = "count=50";

    /** The products the write cases create for themselves, so they never disturb the seeded fixtures. */
    private static final String BRAND_BODY = """
            {"code":"%s","order":2,
             "descriptions":[{"language":"en","name":"%s","title":"T","description":"d","friendlyUrl":"brand"}]}""";

    private static final String TRAINER = "%s Trainer";

    private static final String PURGEABLE = "Purgeable";

    private static final String REBUILDABLE = "Rebuildable";

    private static final String SEARCH = path(V2, PRODUCTS, "search");

    private static final String SUGGEST = path(V2, PRODUCTS, "suggest");

    private static final String REBUILD = path(V2_PRIVATE, PRODUCTS, "search-index", "rebuild");

    private static final String FACETS = "facets";

    private static final String BRANDS = "brands";

    private static final String CATEGORIES = "categories";

    private static final String COUNT = "count";

    private static final String DID_YOU_MEAN = "didYouMean";

    /** Seeded in store A: "Nike ZoomX Invincible Run 3", sku SKU-NK-RUN-001, brand NIKE, type SHOES. */
    private static final String SEEDED_SKU = "SKU-NK-RUN-001";

    private static final String SEEDED_NAME_FRAGMENT = "Invincible";

    /**
     * The same product's Arabic copy, as the merchant wrote it: "أحذية رجالية" appears in its keywords, with a
     * hamza-carrying alef and a teh marbuta. A shopper types neither.
     */
    private static final String ARABIC_UNVOCALISED = "احذيه رجاليه";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    /**
     * The bean the outbox calls, driven directly.
     *
     * <p>
     * Going through the handler rather than the indexer underneath it means these cases exercise the same
     * path a delivered event would, wiring included — without waiting on the poller, which would make every
     * one of them a race.
     * </p>
     */
    @Autowired
    private CatalogSearchOutboxHandler outboxHandler;

    private CatalogApiSupport api;

    @BeforeEach
    void setUp() {
        api = new CatalogApiSupport(port, signer);
    }

    // ------------------------------------------------------------------------------------------------- helpers

    private JsonNode search(String store, String language, String queryString) {
        var response = api.get(query(scoped(SEARCH, store, language), queryString), null);
        expect(response, HttpStatus.OK);
        return json(response);
    }

    /**
     * The term goes in raw. {@code RestClient.uri(String)} encodes the template it is given, so pre-encoding here
     * would send the escapes themselves — the server would receive a literal "%D8%A7..." and search for that.
     * The corollary is that a term cannot contain a query-string delimiter, which is why the malformed-input case
     * below uses punctuation rather than an ampersand.
     */
    private JsonNode search(String store, String language, String term, String extra) {
        return search(store, language, "q=%s&%s".formatted(term, extra));
    }

    private static List<String> names(JsonNode result) {
        List<String> names = new ArrayList<>();
        result.get(CONTENT).forEach(node -> names.add(node.get(DESCRIPTION).get(NAME).asString()));
        return names;
    }

    private static List<String> skus(JsonNode result) {
        List<String> skus = new ArrayList<>();
        result.get(CONTENT).forEach(node -> skus.add(node.get(SKU).asString()));
        return skus;
    }

    // -------------------------------------------------------------------------------------------------- search

    @Test
    void findsAProductByAWordInItsName() {
        JsonNode result = search(STORE_A, EN, SEEDED_NAME_FRAGMENT, A_PAGE);

        assertThat(result.get(TOTAL_ELEMENTS).asLong()).isPositive();
        assertThat(skus(result)).contains(SEEDED_SKU);
    }

    @Test
    void findsAProductBySku() {
        JsonNode result = search(STORE_A, EN, SEEDED_SKU, A_PAGE);

        assertThat(skus(result)).contains(SEEDED_SKU);
    }

    @Test
    void findsAProductByItsBrandName() {
        JsonNode result = search(STORE_A, EN, "nike", A_PAGE);

        assertThat(result.get(TOTAL_ELEMENTS).asLong()).isPositive();
        assertThat(skus(result)).contains(SEEDED_SKU);
    }

    /**
     * The case the whole normalisation layer exists for. The merchant wrote "أحذية رجالية"; the shopper types it
     * without the hamza and with a plain heh. Postgres' arabic stemmer alone does not bridge that.
     */
    @Test
    void arabicIgnoresHamzaTehMarbutaAndTashkeel() {
        JsonNode result = search(STORE_A, AR, ARABIC_UNVOCALISED, A_PAGE);

        assertThat(result.get(TOTAL_ELEMENTS).asLong()).isPositive();
        assertThat(skus(result)).contains(SEEDED_SKU);
    }

    @Test
    void aQueryMatchingNothingReturnsAnEmptyPageRatherThanAnError() {
        JsonNode result = search(STORE_A, EN, "zzzzqqqqxxxx", A_PAGE);

        assertThat(result.get(TOTAL_ELEMENTS).asLong()).isZero();
        assertThat(result.get(CONTENT)).isEmpty();
    }

    /**
     * Punctuation a shopper might paste in must not be able to reach the tsquery parser as syntax —
     * websearch_to_tsquery is what makes that true, and this is the case that would 500 under to_tsquery.
     */
    @Test
    void malformedQueryTextIsNotAnError() {
        JsonNode result = search(STORE_A, EN, "!!! ((( \"unclosed |", A_PAGE);

        assertThat(result.get(TOTAL_ELEMENTS).asLong()).isNotNegative();
    }

    @Test
    void aBlankQueryDegradesToAPlainListing() {
        JsonNode result = search(STORE_A, EN, A_PAGE);

        assertThat(result.get(TOTAL_ELEMENTS).asLong()).isPositive();
    }

    @Test
    void aTypoComesBackAsADidYouMean() {
        JsonNode result = search(STORE_A, EN, "invincibl", A_PAGE);

        assertThat(result.get(TOTAL_ELEMENTS).asLong()).isPositive();
        assertThat(result.get(DID_YOU_MEAN).isNull()).isFalse();
        assertThat(result.get(DID_YOU_MEAN).asString()).containsIgnoringCase(SEEDED_NAME_FRAGMENT);
    }

    /**
     * Several mistyped words against a long product name.
     *
     * <p>
     * The case that plain {@code similarity} cannot answer: it scores the query against the whole name, so
     * "runing shoos" against "Nike Tempo Running Shorts (Women)" comes out at 0.25 and falls under any floor
     * worth having. Scored against the best-matching run of words inside the name it is 0.60.
     * </p>
     */
    @Test
    void aTypoInSeveralWordsStillFindsALongName() {
        JsonNode result = search(STORE_A, EN, "runing shoos", A_PAGE);

        assertThat(result.get(DID_YOU_MEAN).isNull()).isFalse();
        assertThat(result.get(TOTAL_ELEMENTS).asLong()).isPositive();
    }

    // ------------------------------------------------------------------------------------------------ tenancy

    /**
     * Both seeded stores carry products, and a term common to both must never leak across. This is the case the
     * store column in the GIN index exists to make both correct and cheap.
     */
    @Test
    void aStoreNeverSeesAnotherStoresProducts() {
        List<String> fromA = skus(search(STORE_A, EN, SEEDED_SKU, EVERY_RESULT));
        List<String> fromB = skus(search(STORE_B, EN, SEEDED_SKU, EVERY_RESULT));

        assertThat(fromA).contains(SEEDED_SKU);
        assertThat(fromB).doesNotContain(SEEDED_SKU);
    }

    // ------------------------------------------------------------------------------------------------- facets

    /**
     * Buckets are labelled, non-empty, and never claim more products than the result set holds. Not an equality:
     * the brand dimension is an inner join, so a product with no brand is legitimately in no bucket.
     */
    @Test
    void facetsCountTheSameProductsThePageIsDrawnFrom() {
        JsonNode result = search(STORE_A, EN, EVERYTHING_WITH_FACETS);
        JsonNode brands = result.get(FACETS).get(BRANDS);

        assertThat(brands).isNotEmpty();
        long counted = 0;
        for (JsonNode bucket : brands) {
            assertThat(bucket.get(COUNT).asLong()).isPositive();
            assertThat(bucket.get(NAME).asString()).isNotBlank();
            counted += bucket.get(COUNT).asLong();
        }
        assertThat(counted).isLessThanOrEqualTo(result.get(TOTAL_ELEMENTS).asLong());
    }

    @Test
    void narrowingByABrandLeavesExactlyThatBrandsCount() {
        JsonNode all = search(STORE_A, EN, EVERYTHING_WITH_FACETS);
        JsonNode brand = all.get(FACETS).get(BRANDS).get(0);
        long brandId = brand.get(ID).asLong();
        long expected = brand.get(COUNT).asLong();

        JsonNode narrowed = search(STORE_A, EN, "%s&manufacturerIds=%d".formatted(EVERYTHING_WITH_FACETS, brandId));

        assertThat(narrowed.get(TOTAL_ELEMENTS).asLong()).isEqualTo(expected);
        for (JsonNode bucket : narrowed.get(FACETS).get(BRANDS)) {
            if (bucket.get(ID).asLong() == brandId) {
                assertThat(bucket.get("selected").asBoolean()).isTrue();
            }
        }
    }

    @Test
    void facetsAreSkippedWhenNotAskedFor() {
        JsonNode result = search(STORE_A, EN, "q=&facets=false&count=1");

        assertThat(result.get(FACETS) == null || result.get(FACETS).isNull()).isTrue();
    }

    @Test
    void aCategoryFilterNarrowsTheResults() {
        JsonNode all = search(STORE_A, EN, EVERYTHING_WITH_FACETS);
        JsonNode category = all.get(FACETS).get(CATEGORIES).get(0);

        JsonNode narrowed = search(STORE_A, EN,
                "%s&categoryIds=%d".formatted(EVERYTHING_WITH_FACETS, category.get(ID).asLong()));

        assertThat(narrowed.get(TOTAL_ELEMENTS).asLong()).isEqualTo(category.get(COUNT).asLong());
        assertThat(narrowed.get(TOTAL_ELEMENTS).asLong()).isLessThanOrEqualTo(all.get(TOTAL_ELEMENTS).asLong());
    }

    // ---------------------------------------------------------------------------------------------- pagination

    @Test
    void pagesDoNotOverlap() {
        JsonNode first = search(STORE_A, EN, "q=&count=5&page=0");
        JsonNode second = search(STORE_A, EN, "q=&count=5&page=1");

        assertThat(skus(first)).hasSize(5).doesNotContainAnyElementsOf(skus(second));
        assertThat(second.get("pageNumber").asInt()).isEqualTo(1);
        assertThat(second.get(TOTAL_ELEMENTS).asLong()).isEqualTo(first.get(TOTAL_ELEMENTS).asLong());
    }

    // ------------------------------------------------------------------------------------------------- suggest

    @Test
    void suggestAnswersAPartiallyTypedWord() {
        var response = api.get(query(scoped(SUGGEST, STORE_A, EN), "q=invinc&limit=8"), null);
        expect(response, HttpStatus.OK);
        JsonNode suggestions = json(response);

        assertThat(suggestions).isNotEmpty();
        assertThat(suggestions.get(0).get(NAME).asString()).isNotBlank();
        assertThat(suggestions.get(0).get("friendlyUrl").asString()).isNotBlank();
    }

    @Test
    void suggestIsCappedNoMatterWhatIsAskedFor() {
        var response = api.get(query(scoped(SUGGEST, STORE_A, EN), "q=a&limit=500"), null);
        expect(response, HttpStatus.OK);

        assertThat(json(response).size()).isLessThanOrEqualTo(10);
    }

    @Test
    void suggestOnABlankQueryIsEmptyRatherThanEverything() {
        var response = api.get(query(scoped(SUGGEST, STORE_A, EN), "q=&limit=8"), null);
        expect(response, HttpStatus.OK);

        assertThat(json(response)).isEmpty();
    }

    // --------------------------------------------------------------------------------------- index maintenance

    /**
     * What the outbox handler does when it picks up a stale event, without waiting for the poller: a product's
     * new copy has to become findable.
     *
     * <p>
     * On a product of its own, not the seeded one. These cases change what the catalogue holds, and the seeded
     * fixtures are shared by every test in the class.
     * </p>
     */
    @Test
    void reindexingAProductPicksUpItsNewCopy() {
        String invented = "Zorblatt";
        assertThat(search(STORE_A, EN, invented, A_PAGE).get(TOTAL_ELEMENTS).asLong()).isZero();
        String sku = "SKU-SEARCH-REINDEX";
        long productId = createProduct(sku, TRAINER.formatted("Ordinary"));

        expect(api.send(HttpMethod.PUT, scoped(path(V2_PRIVATE, PRODUCT, productId), STORE_A), admin(),
                productBody(sku, "%s Runner".formatted(invented))), HttpStatus.OK);
        reindex(productId);

        JsonNode after = search(STORE_A, EN, invented, A_PAGE);
        assertThat(skus(after)).contains(sku);
        assertThat(names(after)).anyMatch(name -> name.contains(invented));
    }

    @Test
    void purgingAProductRemovesItFromTheResults() {
        String sku = "SKU-SEARCH-PURGE";
        long productId = createProduct(sku, TRAINER.formatted(PURGEABLE));
        assertThat(skus(search(STORE_A, EN, PURGEABLE, A_PAGE))).contains(sku);

        purge(productId);

        assertThat(skus(search(STORE_A, EN, PURGEABLE, A_PAGE))).doesNotContain(sku);
    }

    /**
     * A brand's name is part of every one of its products' search documents, so renaming it has to make them
     * findable under the new name — and that is one event covering many products, not one event each.
     */
    @Test
    void renamingABrandMakesItsProductsFindableUnderTheNewName() {
        String invented = "Zephyrine";
        assertThat(search(STORE_A, EN, invented, A_PAGE).get(TOTAL_ELEMENTS).asLong()).isZero();

        String sku = "SKU-SEARCH-BRAND";
        String brandCode = "SEARCH-BRAND";
        long brandId = createBrand(brandCode, "Plainname");
        long productId = createProduct(sku, TRAINER.formatted("Branded"), brandCode);
        reindex(productId);

        expect(api.send(HttpMethod.PUT, scoped(path(V1_PRIVATE, MANUFACTURER, brandId), STORE_A), admin(),
                BRAND_BODY.formatted(brandCode, invented)), HttpStatus.OK);
        outboxHandler.handleBrandRenamedEvent(BrandRenamedEvent.from(brandId, STORE_A));

        assertThat(skus(search(STORE_A, EN, invented, A_PAGE))).contains(sku);
    }

    // -------------------------------------------------------------------------------------------------- rebuild

    @Test
    void rebuildingTheIndexNeedsTheCatalogPermission() {
        expect(api.send(HttpMethod.POST, scoped(REBUILD, STORE_A), api.token(MODERATOR, STORE_A), ""),
                HttpStatus.FORBIDDEN);
        expect(api.send(HttpMethod.POST, scoped(REBUILD, STORE_A), null, ""), HttpStatus.UNAUTHORIZED);
    }

    @Test
    void rebuildingRestoresWhatWasPurged() {
        String sku = "SKU-SEARCH-REBUILD";
        long productId = createProduct(sku, TRAINER.formatted(REBUILDABLE));
        purge(productId);
        assertThat(skus(search(STORE_A, EN, REBUILDABLE, A_PAGE))).doesNotContain(sku);

        expect(api.send(HttpMethod.POST, scoped(REBUILD, STORE_A), admin(), ""), HttpStatus.ACCEPTED);

        assertThat(skus(search(STORE_A, EN, REBUILDABLE, A_PAGE))).contains(sku);
    }

    // ------------------------------------------------------------------------------ fixtures for the write cases

    private String admin() {
        return api.token(ADMIN, STORE_A);
    }

    /** Exactly what the outbox delivers when a product is saved. */
    private void reindex(long productId) {
        outboxHandler.handleProductSearchIndexStaleEvent(ProductSearchIndexStaleEvent.from(productId, STORE_A));
    }

    /** Exactly what the outbox delivers when a product is deleted. */
    private void purge(long productId) {
        outboxHandler.handleProductSearchIndexPurgedEvent(ProductSearchIndexPurgedEvent.from(productId, STORE_A));
    }

    /**
     * Creates a product and indexes it, standing in for the outbox round trip the poller would otherwise make.
     */
    private long createProduct(String sku, String name) {
        return createProduct(sku, name, null);
    }

    private long createProduct(String sku, String name, String brandCode) {
        var created = api.send(HttpMethod.POST, scoped(path(V2_PRIVATE, PRODUCT), STORE_A), admin(),
                productBody(sku, name, brandCode));
        expect(created, HttpStatus.CREATED);
        long id = json(created).get(ID).asLong();
        reindex(id);
        return id;
    }

    private static String productBody(String sku, String name) {
        return productBody(sku, name, null);
    }

    private static String productBody(String sku, String name, String brandCode) {
        return """
                {"sku":"%s","visible":true,"shipeable":true,"virtual":false,"sortOrder":1,%s
                 "descriptions":[{"language":"%s","name":"%s","friendlyUrl":"%s"}]}"""
                .formatted(sku, brandCode == null ? "" : " \"manufacturer\":\"%s\",".formatted(brandCode),
                        EN, name, sku.toLowerCase());
    }

    private long createBrand(String code, String name) {
        var created = api.send(HttpMethod.POST, scoped(path(V1_PRIVATE, MANUFACTURER), STORE_A), admin(),
                BRAND_BODY.formatted(code, name));
        expect(created, HttpStatus.CREATED);
        return json(created).get(ID).asLong();
    }
}
