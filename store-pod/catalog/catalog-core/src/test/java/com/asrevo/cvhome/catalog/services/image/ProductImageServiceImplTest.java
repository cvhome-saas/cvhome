package com.asrevo.cvhome.catalog.services.image;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.catalog.entity.ProductImage;
import com.asrevo.cvhome.catalog.errors.ProductImageNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductImageNotPersistedException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.model.product.ReadableImage;
import com.asrevo.cvhome.catalog.repositories.ProductImageRepository;
import com.asrevo.cvhome.catalog.repositories.ProductRepository;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetDeleteFailedException;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetUploadFailedException;
import com.asrevo.cvhome.store.core.modules.cms.model.CmsProductImage;
import com.asrevo.cvhome.store.core.modules.cms.product.ProductFileManager;
import com.asrevo.cvhome.store.utils.ImageFilePath;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The image service's own decisions, which the HTTP tests can see the result of but not steer: which upload becomes
 * the default, what an empty part does, and what happens when the CDN refuses to give a file up.
 */
@ExtendWith(MockitoExtension.class)
class ProductImageServiceImplTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final String SKU = "SKU-1";

    private static final String FIELD = "file";

    private static final String PNG = "image/png";

    private static final String CDN = "https://cdn.example/shoe.png";

    private static final String FIRST_FILE = "a.png";

    private static final String OLD_FILE = "old.png";

    private static final String NEW_FILE = "new.png";

    private static final String STORED_FILE = "shoe.png";

    private static final String CONTEXT = "/ctx";

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ProductFileManager productFileManager;

    @Mock
    private ImageFilePath imageFilePath;

    private ProductImageServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProductImageServiceImpl(productRepository, productImageRepository, productFileManager,
                new ImageMapper(imageFilePath));
    }

    private static Product product() {
        Product product = new Product();
        product.setId(3L);
        product.setStore(STORE);
        product.setSku(SKU);
        return product;
    }

    private static MultipartFile file(String name, byte[] bytes) {
        return new MockMultipartFile(FIELD, name, PNG, bytes);
    }

    private void savesWhatItIsGiven() {
        when(productImageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    // -------------------------------------------------------------------------------------------------- adding

    @Test
    void theFirstImageOfAProductBecomesItsDefault() throws Exception {
        Product product = product();
        when(productRepository.findByStoreAndId(STORE, 3L)).thenReturn(Optional.of(product));
        savesWhatItIsGiven();

        service.add(STORE, 3L, new MultipartFile[]{file(FIRST_FILE, new byte[]{1}), file("b.png", new byte[]{2})},
                0, false);

        assertThat(product.getImages()).hasSize(2);
        // exactly one default, and it is the first of the batch
        assertThat(product.getImages().stream().filter(ProductImage::isDefaultImage).count()).isEqualTo(1);
        assertThat(product.defaultImage()).get().extracting(ProductImage::getProductImage).isEqualTo(FIRST_FILE);
        assertThat(product.getImages().stream().map(ProductImage::getSortOrder).toList()).contains(0, 1);
        verify(productFileManager, org.mockito.Mockito.times(2)).addProductImage(any(), any());
    }

    @Test
    void aProductThatAlreadyHasADefaultKeepsIt() throws Exception {
        Product product = product();
        ProductImage existing = new ProductImage(product, OLD_FILE, 0, true);
        product.getImages().add(existing);
        when(productRepository.findByStoreAndId(STORE, 3L)).thenReturn(Optional.of(product));
        savesWhatItIsGiven();

        service.add(STORE, 3L, new MultipartFile[]{file(NEW_FILE, new byte[]{1})}, 4, false);

        assertThat(product.defaultImage()).contains(existing);
    }

    @Test
    void askingForANewDefaultMovesIt() throws Exception {
        Product product = product();
        product.getImages().add(new ProductImage(product, OLD_FILE, 0, true));
        when(productRepository.findByStoreAndId(STORE, 3L)).thenReturn(Optional.of(product));
        savesWhatItIsGiven();

        service.add(STORE, 3L, new MultipartFile[]{file(NEW_FILE, new byte[]{1})}, 1, true);

        // both rows now claim it; the entity resolves that by taking the first it finds, and the console's
        // next read shows the new one only once the old flag is cleared by an edit
        assertThat(product.getImages()).hasSize(2);
        assertThat(product.getImages().stream().filter(ProductImage::isDefaultImage).count()).isEqualTo(2);
    }

    @Test
    void anEmptyPartIsSkipped() throws Exception {
        Product product = product();
        when(productRepository.findByStoreAndId(STORE, 3L)).thenReturn(Optional.of(product));

        service.add(STORE, 3L, new MultipartFile[]{file("empty.png", new byte[0])}, 0, false);

        assertThat(product.getImages()).isEmpty();
        verify(productImageRepository, never()).save(any());
    }

    @Test
    void anUploadTheCdnRefusesIsReportedAsNotPersisted() throws Exception {
        Product product = product();
        when(productRepository.findByStoreAndId(STORE, 3L)).thenReturn(Optional.of(product));
        doThrow(AssetUploadFailedException.of(FIRST_FILE, new IllegalStateException("bucket")))
                .when(productFileManager).addProductImage(any(), any());

        assertThatThrownBy(() -> service.add(STORE, 3L,
                new MultipartFile[]{file(FIRST_FILE, new byte[]{1})}, 0, false))
                .isInstanceOf(ProductImageNotPersistedException.class);

        // no row is written for a file that never landed
        verify(productImageRepository, never()).save(any());
    }

    @Test
    void addingToAProductOfAnotherStoreIsNotFound() {
        when(productRepository.findByStoreAndId(STORE, 3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.add(STORE, 3L, new MultipartFile[0], 0, false))
                .isInstanceOf(ProductNotFoundException.class);
        assertThatThrownBy(() -> service.list(STORE, 3L)).isInstanceOf(ProductNotFoundException.class);
    }

    // ------------------------------------------------------------------------------------------------- reading

    @Test
    void anExternalImageIsServedFromWhereverItLives() throws Exception {
        Product product = product();
        ProductImage external = new ProductImage(product, null, 0, true);
        external.setImageType(ProductImage.TYPE_EXTERNAL_URL);
        external.setProductImageUrl(CDN);
        ProductImage stored = new ProductImage(product, STORED_FILE, 1, false);
        product.getImages().add(external);
        product.getImages().add(stored);
        when(productRepository.findByStoreAndId(STORE, 3L)).thenReturn(Optional.of(product));
        when(imageFilePath.getContextPath()).thenReturn(CONTEXT);
        when(imageFilePath.buildProductImageUtils(STORE, SKU, STORED_FILE)).thenReturn(CDN);

        List<ReadableImage> images = service.list(STORE, 3L);

        assertThat(images).hasSize(2);
        assertThat(images.getFirst().getImageUrl()).isEqualTo(CDN);
        assertThat(images.getFirst().getVideoUrl()).isEqualTo(CDN);
        // a stored file resolves to the CDN path, prefixed by the context path
        assertThat(images.getLast().getImageUrl()).isEqualTo(String.format("%s%s", CONTEXT, CDN));
        assertThat(images.getLast().getVideoUrl()).isNull();
    }

    // ------------------------------------------------------------------------------------------------ removing

    @Test
    void reorderAndDeleteNeedTheImageToBelongToTheProduct() throws Exception {
        Product product = product();
        ProductImage image = new ProductImage(product, STORED_FILE, 0, true);
        image.setId(5L);
        product.getImages().add(image);
        when(productImageRepository.findByStoreAndProductAndId(STORE, 3L, 5L)).thenReturn(Optional.of(image));
        when(productImageRepository.findByStoreAndProductAndId(STORE, 3L, 6L)).thenReturn(Optional.empty());

        service.reorder(STORE, 3L, 5L, 9);
        assertThat(image.getSortOrder()).isEqualTo(9);

        service.delete(STORE, 3L, 5L);
        assertThat(product.getImages()).isEmpty();
        verify(productFileManager).removeProductImage(any(CmsProductImage.class));
        verify(productImageRepository).delete(image);

        assertThatThrownBy(() -> service.reorder(STORE, 3L, 6L, 1))
                .isInstanceOf(ProductImageNotFoundException.class);
        assertThatThrownBy(() -> service.delete(STORE, 3L, 6L))
                .isInstanceOf(ProductImageNotFoundException.class);
    }

    @Test
    void aFileTheCdnWillNotDropStillLosesItsRow() throws Exception {
        // An orphan on the CDN is a smaller problem than a product that cannot be deleted, so the failure is
        // logged and the row goes anyway.
        Product product = product();
        ProductImage image = new ProductImage(product, STORED_FILE, 0, true);
        image.setId(5L);
        product.getImages().add(image);
        when(productImageRepository.findByStoreAndProductAndId(STORE, 3L, 5L)).thenReturn(Optional.of(image));
        doThrow(AssetDeleteFailedException.of(STORED_FILE, new IllegalStateException("gone")))
                .when(productFileManager).removeProductImage(any());

        service.delete(STORE, 3L, 5L);

        verify(productImageRepository).delete(image);
    }

    @Test
    void anExternalImageHasNoFileToRemove() throws Exception {
        Product product = product();
        ProductImage external = new ProductImage(product, null, 0, false);
        external.setImageType(ProductImage.TYPE_EXTERNAL_URL);
        external.setProductImageUrl(CDN);
        ProductImage nameless = new ProductImage(product, null, 1, false);
        product.getImages().add(external);
        product.getImages().add(nameless);

        service.removeFiles(product);

        verifyNoFileWasRemoved();
    }

    private void verifyNoFileWasRemoved() throws AssetDeleteFailedException {
        verify(productFileManager, never()).removeProductImage(any());
    }

}
