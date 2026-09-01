package com.asrevo.cvhome.catalog.services.image;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.catalog.entity.ProductImage;
import com.asrevo.cvhome.catalog.errors.ProductImageAssetUnknownException;
import com.asrevo.cvhome.catalog.errors.ProductImageNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.model.product.PersistableProductImage;
import com.asrevo.cvhome.catalog.model.product.ReadableImage;
import com.asrevo.cvhome.catalog.repositories.ProductImageRepository;
import com.asrevo.cvhome.catalog.repositories.ProductRepository;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.api.ExternalMediaService;
import com.asrevo.cvhome.content.model.MediaOwnerKind;
import com.asrevo.cvhome.content.model.media.ExternalMediaUsage;
import com.asrevo.cvhome.content.model.media.ReadableMediaAsset;
import com.asrevo.cvhome.errors.RemoteServiceTimeoutException;
import com.asrevo.cvhome.errors.RemoteServiceUnavailableException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductRepository productRepository;

    private final ProductImageRepository productImageRepository;

    private final ExternalMediaService media;

    private final ImageMapper imageMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ReadableImage> list(StoreMerchantId store, Long productId) throws ProductNotFoundException {
        return imageMapper.toReadable(requireProduct(store, productId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ReadableImage> attach(StoreMerchantId store, Long productId, List<PersistableProductImage> items)
            throws ProductNotFoundException, ProductImageAssetUnknownException, RemoteServiceUnavailableException,
            RemoteServiceTimeoutException {
        Product product = requireProduct(store, productId);
        Map<Long, String> paths = resolve(store, items);
        int position = product.getImages().stream().mapToInt(ProductImage::getSortOrder).max().orElse(-1) + 1;
        boolean needsDefault = product.getImages().stream().noneMatch(ProductImage::isDefaultImage);
        for (PersistableProductImage item : items) {
            ProductImage image = row(product, item, paths, position++);
            if (item.isDefaultImage() || needsDefault) {
                clearDefault(product);
                image.setDefaultImage(true);
                needsDefault = false;
            }
            product.getImages().add(productImageRepository.save(image));
        }
        productImageRepository.flush();
        publishUsage(store, product);
        return imageMapper.toReadable(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ReadableImage> replace(StoreMerchantId store, Long productId, List<PersistableProductImage> items)
            throws ProductNotFoundException, ProductImageAssetUnknownException, RemoteServiceUnavailableException,
            RemoteServiceTimeoutException {
        Product product = requireProduct(store, productId);
        Map<Long, String> paths = resolve(store, items);
        productImageRepository.deleteAll(product.getImages());
        product.getImages().clear();
        productImageRepository.flush();

        int position = 0;
        boolean defaulted = false;
        for (PersistableProductImage item : items) {
            ProductImage image = row(product, item, paths, position++);
            if (item.isDefaultImage() && !defaulted) {
                image.setDefaultImage(true);
                defaulted = true;
            }
            product.getImages().add(productImageRepository.save(image));
        }
        // Something has to be the default, or the storefront picks by sort order and the seller's choice is lost.
        if (!defaulted) {
            product.getImages().stream().min(java.util.Comparator.comparingInt(ProductImage::getSortOrder))
                    .ifPresent(first -> first.setDefaultImage(true));
        }
        productImageRepository.flush();
        publishUsage(store, product);
        return imageMapper.toReadable(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(StoreMerchantId store, Long productId, Long imageId) throws ProductImageNotFoundException {
        ProductImage image = requireImage(store, productId, imageId);
        Product product = image.getProduct();
        product.getImages().remove(image);
        productImageRepository.delete(image);
        productImageRepository.flush();
        publishUsage(store, product);
    }

    @Override
    public void forget(Product product) {
        publishUsage(product.getStore(), product);
    }

    /**
     * Confirms every asset belongs to this store and caches its path in the bucket.
     *
     * <p>
     * The path rather than the url content composed from it: the two services read the same CDN setting, so
     * catalog can build the address itself when a product is read and a CDN move needs no backfill here.
     * </p>
     *
     * <p>
     * Content answering with the asset omitted is how an id from another store is caught, so this doubles as the
     * ownership check. A transport failure is allowed to propagate: a row with no path renders as a broken image
     * on the storefront, which is worse than a save the seller can retry.
     * </p>
     */
    private Map<Long, String> resolve(StoreMerchantId store, List<PersistableProductImage> items)
            throws ProductImageAssetUnknownException, RemoteServiceUnavailableException,
            RemoteServiceTimeoutException {
        List<Long> ids = items.stream().map(PersistableProductImage::getMediaAssetId)
                .filter(java.util.Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> paths = new LinkedHashMap<>();
        for (ReadableMediaAsset asset : media.resolve(store, ids)) {
            paths.put(asset.getId(), asset.getPath());
        }
        for (Long id : ids) {
            if (!paths.containsKey(id)) {
                throw ProductImageAssetUnknownException.of(id, store);
            }
        }
        return paths;
    }

    /**
     * Tells content the product's complete set of assets.
     *
     * <p>
     * Deliberately inside the transaction, after the rows are flushed. A remote call in a transaction is normally
     * wrong, but the alternative is a silent hole: the rows commit, content never learns the assets are in use,
     * a seller deletes one from the library with a 200, and the cached url 404s. This is one small idempotent
     * PUT, and a failure rolls the image rows back and shows the console an error.
     * </p>
     */
    private void publishUsage(StoreMerchantId store, Product product) {
        List<ExternalMediaUsage.Ref> refs = new ArrayList<>();
        product.getImages().stream()
                .sorted(java.util.Comparator.comparingInt(ProductImage::getSortOrder))
                .forEach(image -> {
                    if (image.getMediaAssetId() != null) {
                        refs.add(new ExternalMediaUsage.Ref(
                                String.format("image[%d]", image.getSortOrder()), image.getMediaAssetId()));
                    }
                });
        media.replaceUsage(store, new ExternalMediaUsage(MediaOwnerKind.PRODUCT, String.valueOf(product.getId()),
                label(product), refs));
    }

    /** What the media library shows beside the usage. Supplied by us so content never calls back into catalog. */
    private static String label(Product product) {
        return product.defaultVariant().map(variant -> variant.getSku())
                .orElseGet(() -> "product-%d".formatted(product.getId()));
    }

    private static ProductImage row(Product product, PersistableProductImage item, Map<Long, String> paths,
                                    int position) {
        ProductImage image = new ProductImage(product, item.getMediaAssetId(),
                paths.get(item.getMediaAssetId()), item.getAltText(), position, false);
        if (item.getMediaAssetId() == null) {
            image.setImageType(ProductImage.TYPE_EXTERNAL_URL);
            image.setProductImageUrl(item.getVideoUrl() != null ? item.getVideoUrl() : item.getExternalUrl());
        } else {
            image.setImageType(ProductImage.TYPE_MEDIA_ASSET);
        }
        return image;
    }

    private static void clearDefault(Product product) {
        product.getImages().forEach(i -> i.setDefaultImage(false));
    }

    private Product requireProduct(StoreMerchantId store, Long productId) throws ProductNotFoundException {
        return productRepository.findByStoreAndId(store, productId)
                .orElseThrow(() -> ProductNotFoundException.of(productId, store));
    }

    private ProductImage requireImage(StoreMerchantId store, Long productId, Long imageId)
            throws ProductImageNotFoundException {
        return productImageRepository.findByStoreAndProductAndId(store, productId, imageId)
                .orElseThrow(() -> ProductImageNotFoundException.of(imageId, store));
    }
}
