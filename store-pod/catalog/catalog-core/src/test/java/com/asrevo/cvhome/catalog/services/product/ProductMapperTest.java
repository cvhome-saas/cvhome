package com.asrevo.cvhome.catalog.services.product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.asrevo.cvhome.catalog.entity.Category;
import com.asrevo.cvhome.catalog.entity.Manufacturer;
import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.catalog.entity.ProductDescription;
import com.asrevo.cvhome.catalog.entity.ProductImage;
import com.asrevo.cvhome.catalog.entity.ProductType;
import com.asrevo.cvhome.catalog.model.product.PersistableProductDefinition;
import com.asrevo.cvhome.catalog.model.product.ProductSpecification;
import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductDefinition;
import com.asrevo.cvhome.catalog.services.image.ImageMapper;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.store.model.references.MeasureUnit;
import com.asrevo.cvhome.store.model.references.WeightUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * The three shapes a product is read in, and the one write.
 *
 * <p>
 * The branches an HTTP test cannot steer are all here: a product with no brand or type, a language the product has
 * no copy in, a store whose units of measure are unset, an entity title that falls back to the name, and the
 * description merge that has to keep an existing row's id when the console edits it.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class ProductMapperTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final LanguageCode EN = new LanguageCode("en");

    private static final LanguageCode AR = new LanguageCode("ar");

    private static final LanguageCode FR = new LanguageCode("fr");

    private static final String SKU = "SKU-1";

    private static final String NAME = "Running shoe";

    private static final String CDN = "https://cdn.example/";

    private static final String NEW_SKU = "SKU-2";

    private static final String RENAMED = "Renamed";

    private static final String TITLE = "T";

    private static final String NIKE = "NIKE";

    private static final String SHOES = "SHOES";

    private static final String IMAGE_FILE = "shoe.jpg";

    @Mock
    private ExternalMerchantStoreService merchantStoreService;


    private ProductMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProductMapper(new ImageMapper("https://cdn.example/bucket"), merchantStoreService);
    }

    // ------------------------------------------------------------------------------------------------- fixtures

    private static ProductDescription description(Product product, LanguageCode language, String name, String title) {
        ProductDescription description = new ProductDescription(product);
        description.setLanguageCode(language);
        description.setName(name);
        description.setTitle(title);
        description.setSeUrl("running-shoe");
        description.setMetaKeywords("k");
        description.setMetaDescription("m");
        description.setHighlight("h");
        product.getDescriptions().add(description);
        return description;
    }

    private static Product product() {
        Product product = new Product();
        product.setId(7L);
        product.setStore(STORE);
        com.asrevo.cvhome.catalog.entity.ProductVariant defaultVariant =
                new com.asrevo.cvhome.catalog.entity.ProductVariant(product, SKU);
        defaultVariant.setDefaultVariant(true);
        product.getVariants().add(defaultVariant);
        product.setHeight(BigDecimal.ONE);
        product.setWidth(BigDecimal.TWO);
        product.setLength(BigDecimal.TEN);
        product.setWeight(BigDecimal.ONE);
        description(product, EN, NAME, "Title");
        description(product, AR, "حذاء", null);
        return product;
    }

    private void storeWithUnits(MeasureUnit dimension, WeightUnit weight) {
        ReadableMerchantStore store = new ReadableMerchantStore();
        store.setDimension(dimension);
        store.setWeight(weight);
        when(merchantStoreService.getStore(any())).thenReturn(store);
    }

    private static PersistableProductDefinition definition() {
        PersistableProductDefinition source = new PersistableProductDefinition();
        source.setSku(NEW_SKU);
        source.setVisible(false);
        source.setVirtual(true);
        source.setShipeable(false);
        source.setSortOrder(9);
        com.asrevo.cvhome.catalog.model.product.ProductDescription copy =
                new com.asrevo.cvhome.catalog.model.product.ProductDescription();
        copy.setLanguage(EN);
        copy.setName(RENAMED);
        copy.setTitle(TITLE);
        copy.setDescription("d");
        copy.setFriendlyUrl("renamed");
        source.setDescriptions(List.of(copy));
        return source;
    }

    private static Manufacturer manufacturer() {
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setId(1L);
        manufacturer.setCode(NIKE);
        manufacturer.setStoreMerchantId(STORE);
        com.asrevo.cvhome.catalog.entity.ManufacturerDescription description =
                new com.asrevo.cvhome.catalog.entity.ManufacturerDescription(manufacturer);
        description.setLanguageCode(EN);
        description.setName("Nike");
        manufacturer.getDescriptions().add(description);
        return manufacturer;
    }

    private static ProductType type() {
        ProductType type = new ProductType();
        type.setId(2L);
        type.setCode(SHOES);
        type.setStoreMerchantId(STORE);
        return type;
    }

    private static Category category() {
        Category category = new Category();
        category.setId(5L);
        category.setCode("MEN");
        category.setStoreMerchantId(STORE);
        category.placeUnder(null);
        return category;
    }


    // -------------------------------------------------------------------------------------------------- reading

    @Nested
    class Reading {

        @Test
        void theMinimalShapeCarriesCopyImagesAndTheBox() {
            storeWithUnits(MeasureUnit.CM, WeightUnit.KG);
            Product product = product();
            ProductImage image = new ProductImage(product, 5L, CDN, null, 1, false);
            image.setId(3L);
            product.getImages().add(image);

            ReadableMinimalProduct readable = mapper.toMinimal(product, EN);

            assertThat(readable.getId()).isEqualTo(7L);
            assertThat(readable.getSku()).isEqualTo(SKU);
            assertThat(readable.getDescription().getName()).isEqualTo(NAME);
            assertThat(readable.getImages()).hasSize(1);
            // no image is flagged default, so the first by sort order stands in for one
            assertThat(readable.getImage().getImageUrl()).isEqualTo(CDN);
            ProductSpecification specification = readable.getProductSpecifications();
            assertThat(specification.getHeight()).isEqualTo(BigDecimal.ONE);
            assertThat(specification.getDimensionUnitOfMeasure().name()).isEqualTo("cm");
            assertThat(specification.getWeightUnitOfMeasure().name()).isEqualTo("kg");
        }

        @Test
        void aLanguageTheProductHasNoCopyInLeavesTheDescriptionUnset() {
            storeWithUnits(MeasureUnit.CM, WeightUnit.KG);

            ReadableMinimalProduct readable = mapper.toMinimal(product(), FR);

            // better an absent description than one in a language the shopper did not ask for
            assertThat(readable.getDescription()).isNull();
            assertThat(readable.getImage()).isNull();
            assertThat(readable.getImages()).isEmpty();
        }

        @Test
        void aStoreWithoutUnitsLeavesThemUnset() {
            // The units are the merchant's, and a store record that has never been completed has neither. Reading
            // them unconditionally used to be an NPE on the storefront's busiest call.
            storeWithUnits(null, null);

            ProductSpecification specification = mapper.toMinimal(product(), EN).getProductSpecifications();

            assertThat(specification.getDimensionUnitOfMeasure()).isNull();
            assertThat(specification.getWeightUnitOfMeasure()).isNull();
            assertThat(specification.getWeight()).isEqualTo(BigDecimal.ONE);
        }

        @Test
        void aBlankTitleFallsBackToTheName() {
            storeWithUnits(MeasureUnit.IN, WeightUnit.LB);
            Product product = new Product();
            product.setStore(STORE);
            description(product, EN, NAME, "  ");

            assertThat(mapper.toMinimal(product, EN).getDescription().getTitle()).isEqualTo(NAME);
        }

        @Test
        void theListingShapeAddsBrandTypeAndCategories() {
            storeWithUnits(MeasureUnit.CM, WeightUnit.KG);
            Product product = product();
            product.setManufacturer(manufacturer());
            product.setType(type());
            product.setCategories(Set.of(category()));

            ReadableProduct readable = mapper.toReadable(product, EN);

            assertThat(readable.getManufacturer().getCode()).isEqualTo(NIKE);
            // the listing shows the brand in one language only; the console's read shows every language
            assertThat(readable.getManufacturer().getDescriptions()).isEmpty();
            assertThat(readable.getType().getCode()).isEqualTo(SHOES);
            assertThat(readable.getCategories()).hasSize(1);
        }

        @Test
        void aProductWithNoBrandOrTypeStillReads() {
            storeWithUnits(MeasureUnit.CM, WeightUnit.KG);

            ReadableProduct readable = mapper.toReadable(product(), EN);

            assertThat(readable.getManufacturer()).isNull();
            assertThat(readable.getType()).isNull();
            assertThat(readable.getCategories()).isEmpty();
        }

        @Test
        void theDefinitionCarriesEveryLanguageAndTheRelationsInFull() {
            storeWithUnits(MeasureUnit.CM, WeightUnit.KG);
            Product product = product();
            product.setManufacturer(manufacturer());
            product.setType(type());
            product.setCategories(Set.of(category()));

            ReadableProductDefinition definition = mapper.toDefinition(product, EN);

            assertThat(definition.getSku()).isEqualTo(SKU);
            assertThat(definition.getIdentifier()).isEqualTo(SKU);
            assertThat(definition.getDescriptions()).hasSize(2);
            assertThat(definition.getDescription().getName()).isEqualTo(NAME);
            assertThat(definition.getManufacturer().getDescriptions()).hasSize(1);
            assertThat(definition.getType().getDescriptions()).isEmpty();
            assertThat(definition.getCategories()).hasSize(1);
        }
    }

    // -------------------------------------------------------------------------------------------------- writing

    @Nested
    class Writing {

        @Test
        void anEditKeepsTheIdOfTheLanguageItRewrites() {
            // Descriptions are merged by language rather than replaced wholesale: a console edit that renamed a
            // product used to delete the row and insert a new one, losing its id and its audit dates.
            Product product = product();
            Long englishId = 11L;
            product.description(EN).orElseThrow().setId(englishId);

            PersistableProductDefinition source = definition();
            ProductMapper.apply(source, product);

            assertThat(product.getDescriptions()).hasSize(1);
            ProductDescription english = product.description(EN).orElseThrow();
            assertThat(english.getId()).isEqualTo(englishId);
            assertThat(english.getName()).isEqualTo(RENAMED);
            // the meta title mirrors the title the console sends
            assertThat(english.getMetaTitle()).isEqualTo(TITLE);
            // a language absent from the body is dropped
            assertThat(product.description(AR)).isEmpty();
        }

        @Test
        void theBoxAndTheDateAreOnlyTouchedWhenTheBodyCarriesThem() {
            Product product = product();
            Instant original = product.getDateAvailable();
            PersistableProductDefinition source = definition();
            source.setProductSpecifications(null);
            source.setDateAvailable(null);

            ProductMapper.apply(source, product);

            assertThat(product.getDateAvailable()).isEqualTo(original);
            assertThat(product.getHeight()).isEqualTo(BigDecimal.ONE);
            // the sku belongs to the variant, not the definition body — apply leaves it alone
            assertThat(product.defaultVariant().orElseThrow().getSku()).isEqualTo(SKU);
            assertThat(product.isAvailable()).isFalse();
            assertThat(product.isProductVirtual()).isTrue();
        }

        @Test
        void theBoxIsCopiedWhenItIsSent() {
            Product product = product();
            PersistableProductDefinition source = definition();
            ProductSpecification specification = new ProductSpecification();
            specification.setHeight(BigDecimal.valueOf(4));
            specification.setWidth(BigDecimal.valueOf(5));
            specification.setLength(BigDecimal.valueOf(6));
            specification.setWeight(BigDecimal.valueOf(7));
            source.setProductSpecifications(specification);
            Instant available = Instant.parse("2026-01-01T00:00:00Z");
            source.setDateAvailable(available);

            ProductMapper.apply(source, product);

            assertThat(product.getHeight()).isEqualTo(BigDecimal.valueOf(4));
            assertThat(product.getWeight()).isEqualTo(BigDecimal.valueOf(7));
            assertThat(product.getDateAvailable()).isEqualTo(available);
        }
    }

}
