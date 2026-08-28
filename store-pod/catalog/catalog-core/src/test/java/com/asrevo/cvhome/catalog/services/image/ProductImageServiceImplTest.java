package com.asrevo.cvhome.catalog.services.image;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.catalog.entity.ProductImage;
import com.asrevo.cvhome.catalog.errors.ProductImageAssetUnknownException;
import com.asrevo.cvhome.catalog.errors.ProductImageNotFoundException;
import com.asrevo.cvhome.catalog.model.product.PersistableProductImage;
import com.asrevo.cvhome.catalog.model.product.ReadableImage;
import com.asrevo.cvhome.catalog.repositories.ProductImageRepository;
import com.asrevo.cvhome.catalog.repositories.ProductRepository;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.api.ExternalMediaService;
import com.asrevo.cvhome.content.model.MediaOwnerKind;
import com.asrevo.cvhome.content.model.media.ExternalMediaUsage;
import com.asrevo.cvhome.content.model.media.ReadableMediaAsset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The gallery's own decisions, which the HTTP tests can see the result of but not steer: which image becomes the
 * default, what happens to an asset id that is not this store's, and what content is told afterwards.
 */
@ExtendWith(MockitoExtension.class)
class ProductImageServiceImplTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final String SKU = "SKU-1";

    private static final long PRODUCT_ID = 7L;

    private static final String CDN = "https://cdn.example/bucket";

    /** How the mapper joins the two, so the expectations here are not a second copy of that rule. */
    private static final String UNDER_CDN = "%s/%s";

    private static final String PATH_ONE = "files/store/media/1/a.png";

    private static final String PATH_TWO = "files/store/media/2/b.png";

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ExternalMediaService media;

    private ProductImageServiceImpl service;

    private Product product;

    @BeforeEach
    void setUp() {
        service = new ProductImageServiceImpl(productRepository, productImageRepository, media, new ImageMapper(CDN));
        product = new Product();
        product.setId(PRODUCT_ID);
        product.setSku(SKU);
        product.setStore(STORE);
    }

    private void productExists() {
        when(productRepository.findByStoreAndId(STORE, PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productImageRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    private void libraryHas(Long id, String path) {
        when(media.resolve(any(), anyList())).thenReturn(List.of(libraryAsset(id, path)));
    }

    /** Content answers with the path; the url it composes from it is for a browser, not for catalog to keep. */
    private static ReadableMediaAsset libraryAsset(Long id, String path) {
        ReadableMediaAsset asset = new ReadableMediaAsset();
        asset.setId(id);
        asset.setPath(path);
        asset.setUrl(String.format(UNDER_CDN, CDN, path));
        return asset;
    }

    private static PersistableProductImage item(Long assetId, boolean isDefault) {
        PersistableProductImage p = new PersistableProductImage();
        p.setMediaAssetId(assetId);
        p.setDefaultImage(isDefault);
        return p;
    }

    @Test
    void theFirstImageOfAnEmptyGalleryBecomesTheDefault() throws Exception {
        productExists();
        libraryHas(1L, PATH_ONE);

        List<ReadableImage> out = service.attach(STORE, PRODUCT_ID, List.of(item(1L, false)));

        assertThat(out).singleElement().satisfies(i -> {
            assertThat(i.isDefaultImage()).isTrue();
            assertThat(i.getMediaAssetId()).isEqualTo(1L);
            // The path is cached at attach time, so reading a product needs no call into content; the url is
            // that path under the CDN this environment is configured with.
            assertThat(i.getImageUrl()).isEqualTo(String.format(UNDER_CDN, CDN, PATH_ONE));
        });
    }

    /**
     * Content answering without the asset is how an id from another store is caught, so nothing is written and
     * the seller is told which id was wrong.
     */
    @Test
    void anAssetThatIsNotThisStoresIsRefused() {
        when(productRepository.findByStoreAndId(STORE, PRODUCT_ID)).thenReturn(Optional.of(product));
        when(media.resolve(any(), anyList())).thenReturn(List.of());

        assertThatThrownBy(() -> service.attach(STORE, PRODUCT_ID, List.of(item(99L, false))))
                .isInstanceOf(ProductImageAssetUnknownException.class);

        verify(productImageRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void replacingTheGalleryRenumbersAndHonoursTheChosenDefault() throws Exception {
        product.getImages().add(new ProductImage(product, 1L, PATH_ONE, null, 0, true));
        productExists();
        when(media.resolve(any(), anyList()))
                .thenReturn(List.of(libraryAsset(1L, PATH_ONE), libraryAsset(2L, PATH_TWO)));

        List<ReadableImage> out = service.replace(STORE, PRODUCT_ID,
                List.of(item(2L, true), item(1L, false)));

        assertThat(out).extracting(ReadableImage::getMediaAssetId).containsExactly(2L, 1L);
        assertThat(out.getFirst().isDefaultImage()).isTrue();
        assertThat(out.getLast().isDefaultImage()).isFalse();
    }

    /**
     * Something has to be the default, or the storefront falls back to sort order and the seller's choice is lost.
     */
    @Test
    void aGalleryWithNoChosenDefaultDefaultsToTheFirst() throws Exception {
        productExists();
        libraryHas(1L, PATH_ONE);

        List<ReadableImage> out = service.replace(STORE, PRODUCT_ID, List.of(item(1L, false)));

        assertThat(out).singleElement().satisfies(i -> assertThat(i.isDefaultImage()).isTrue());
    }

    @Test
    void contentIsToldTheCompleteSetAndWhichProductHoldsIt() throws Exception {
        productExists();
        libraryHas(1L, PATH_ONE);

        service.attach(STORE, PRODUCT_ID, List.of(item(1L, false)));

        ArgumentCaptor<ExternalMediaUsage> captor = ArgumentCaptor.forClass(ExternalMediaUsage.class);
        verify(media).replaceUsage(any(), captor.capture());
        ExternalMediaUsage usage = captor.getValue();
        assertThat(usage.ownerKind()).isEqualTo(MediaOwnerKind.PRODUCT);
        assertThat(usage.ownerRef()).isEqualTo(String.valueOf(PRODUCT_ID));
        // The label travels with the call so content never has to ask catalog what a product is called.
        assertThat(usage.ownerTitle()).isEqualTo(SKU);
        assertThat(usage.refs()).singleElement()
                .satisfies(r -> assertThat(r.assetId()).isEqualTo(1L));
    }

    /**
     * Detaching drops the row and restates what is left; the asset stays in the library, where other products may
     * still be using it.
     */
    @Test
    void detachingAnImageReleasesOnlyThatReference() throws Exception {
        ProductImage image = new ProductImage(product, 1L, PATH_ONE, null, 0, true);
        image.setId(11L);
        product.getImages().add(image);
        when(productImageRepository.findByStoreAndProductAndId(STORE, PRODUCT_ID, 11L))
                .thenReturn(Optional.of(image));

        service.delete(STORE, PRODUCT_ID, 11L);

        verify(productImageRepository).delete(image);
        ArgumentCaptor<ExternalMediaUsage> captor = ArgumentCaptor.forClass(ExternalMediaUsage.class);
        verify(media).replaceUsage(any(), captor.capture());
        assertThat(captor.getValue().refs()).isEmpty();
    }

    @Test
    void detachingAnImageOfAnotherProductIsNotFound() {
        when(productImageRepository.findByStoreAndProductAndId(STORE, PRODUCT_ID, 11L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(STORE, PRODUCT_ID, 11L))
                .isInstanceOf(ProductImageNotFoundException.class);
    }

}
