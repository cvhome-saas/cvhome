package com.asrevo.cvhome.catalog.services.product;

import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.ProductCriteria;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductReservation;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductReservationStatusEnum;
import com.asrevo.cvhome.catalog.entity.product.image.ProductImage;
import com.asrevo.cvhome.catalog.model.product.ProductDetails;
import com.asrevo.cvhome.catalog.model.product.ProductReservationStatus;
import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductAvailability;
import com.asrevo.cvhome.catalog.model.product.product.price.FinalPriceCalc;
import com.asrevo.cvhome.catalog.repositories.product.ProductRepository;
import com.asrevo.cvhome.catalog.repositories.product.availability.ProductReservationRepository;
import com.asrevo.cvhome.catalog.service.mapper.catalog.ReadableMinimalProductMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.ReadableProductAvailabilityMapper;
import com.asrevo.cvhome.catalog.services.pricing.PricingServiceImpl;
import com.asrevo.cvhome.catalog.services.product.image.ProductImageService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.ImageContentFile;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.ProductReservationList;
import com.asrevo.cvhome.store.core.model.catalog.ReserveProductEntry;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service("productService")
@Slf4j
public class ProductServiceImpl extends SalesManagerEntityServiceImpl<Long, Product> implements ProductService {

    private final ProductRepository productRepository;

    private final ProductImageService productImageService;

    private final PricingServiceImpl pricingService;

    private final ReadableMinimalProductMapper readableMinimalProductMapper;

    private final ReadableProductAvailabilityMapper readableProductAvailabilityMapper;

    private final ProductReservationRepository productReservationRepository;

    @Value("${reservation.expiry.minutes:15}")
    private int reservationExpiryMinutes;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, ProductImageService productImageService,
                              PricingServiceImpl pricingService, ReadableMinimalProductMapper readableMinimalProductMapper,
                              ReadableProductAvailabilityMapper readableProductAvailabilityMapper,
                              ProductReservationRepository productReservationRepository) {
        super(productRepository);
        this.productRepository = productRepository;
        this.productImageService = productImageService;
        this.pricingService = pricingService;
        this.readableMinimalProductMapper = readableMinimalProductMapper;
        this.readableProductAvailabilityMapper = readableProductAvailabilityMapper;
        this.productReservationRepository = productReservationRepository;
    }

    @Override
    public Optional<Product> retrieveById(Long id, StoreMerchantId store) {
        return Optional.ofNullable(findOne(id, store));
    }

    @Override
    public List<Product> getProducts(List<Long> categoryIds) {
        Set<Long> ids = new HashSet<>(categoryIds);
        return productRepository.getProductsListByCategories(ids);
    }

    @Override
    public Product getBySeUrl(StoreMerchantId store, String seUrl, Locale locale) {
        return productRepository.getByFriendlyUrl(store, seUrl, locale);
    }

    @Override
    public List<Product> listByStore(StoreMerchantId store) {

        return productRepository.listByStore(store);
    }

    @Override
    public void delete(Product product) throws ServiceException {
        product = this.getById(product.getId());
        product.setCategories(null);

        Set<ProductImage> images = product.getImages();

        for (ProductImage image : images) {
            productImageService.removeProductImage(image);
        }

        product.setImages(null);

        super.delete(product);

    }

    @Override
    public void create(Product product) throws ServiceException {
        saveOrUpdate(product);
    }

    @Override
    public void update(Product product) throws ServiceException {
        saveOrUpdate(product);
    }

    private Product saveOrUpdate(Product product) throws ServiceException {
        Set<ProductImage> originalProductImages = new HashSet<>(product.getImages());

        if (product.getId() != null && product.getId() > 0) {
            super.update(product);
        } else {
            super.create(product);
        }

        List<Long> newImageIds = new ArrayList<>();
        Set<ProductImage> images = product.getImages();

        try {

            if (images != null && !images.isEmpty()) {
                for (ProductImage image : images) {
                    if (image.getImage() != null && (image.getId() == null || image.getId() == 0L)) {
                        image.setProduct(product);

                        InputStream inputStream = image.getImage();
                        ImageContentFile cmsContentImage = new ImageContentFile();
                        cmsContentImage.setFileName(image.getProductImage());
                        cmsContentImage.setFile(inputStream);
                        cmsContentImage.setFileContentType(FileContentType.PRODUCT);

                        productImageService.addProductImage(product, image, cmsContentImage);
                        newImageIds.add(image.getId());
                    } else {
                        if (image.getId() != null) {
                            productImageService.save(image);
                            newImageIds.add(image.getId());
                        }
                    }
                }
            }

            // cleanup old and new images
            for (ProductImage image : originalProductImages) {

                if (image.getImage() != null && image.getId() == null) {
                    image.setProduct(product);

                    InputStream inputStream = image.getImage();
                    ImageContentFile cmsContentImage = new ImageContentFile();
                    cmsContentImage.setFileName(image.getProductImage());
                    cmsContentImage.setFile(inputStream);
                    cmsContentImage.setFileContentType(FileContentType.PRODUCT);

                    productImageService.addProductImage(product, image, cmsContentImage);
                    newImageIds.add(image.getId());
                } else {
                    if (!newImageIds.contains(image.getId())) {
                        productImageService.delete(image);
                    }
                }
            }

        } catch (Exception e) {
            log.error("Cannot save images {}", e.getMessage());
        }

        return product;
    }

    @Override
    public Product findOne(Long id, StoreMerchantId merchant) {
        return productRepository.getById(id, merchant);
    }

    @Transactional
    @Override
    public ProductReservationStatus reserve(StoreMerchantId store, Long orderId, ProductReservationList productReservation)
            throws ServiceException {
        doReserveWithStatus(store, orderId, productReservation, ProductReservationStatusEnum.RESERVED);
        return new ProductReservationStatus(true);
    }

    @Transactional
    @Override
    public ProductReservationStatus autoCommit(StoreMerchantId store, Long orderId, ProductReservationList productReservation)
            throws ServiceException {
        doReserveWithStatus(store, orderId, productReservation, ProductReservationStatusEnum.COMPLETED);
        return new ProductReservationStatus(true);
    }

    private void doReserveWithStatus(StoreMerchantId store, Long orderId, ProductReservationList productReservation,
                                     ProductReservationStatusEnum status) throws ServiceException {
        if (Objects.isNull(productReservation.entries()) || productReservation.entries().isEmpty()) {
            throw new ServiceException("No entries to reserve");
        }

        for (ReserveProductEntry entry : productReservation.entries()) {
            // Idempotency check: if reservation already exists for this order and SKU, skip
            Optional<ProductReservation> existingReservation = productReservationRepository.findByOrderIdAndSku(orderId, entry.sku());
            if (existingReservation.isPresent()) {
                log.info("Reservation for order {} and sku {} already exists. Skipping.", orderId, entry.sku());
                continue;
            }

            Product product = productRepository
                    .getByProductIdFetchAvailabilities(findProductIdByCode(entry.sku(), store), store);

            for (ProductAvailability availability : product.getAvailabilities()) {
                if (availability.getProductQuantity() < entry.reserveQty()) {
                    throw new ServiceException(ServiceException.EXCEPTION_INVENTORY_MISMATCH);
                }

                // Deduct quantity
                availability.setProductQuantity(availability.getProductQuantity() - entry.reserveQty());

                // Create Reservation
                ProductReservation reservation = new ProductReservation();
                reservation.setSku(entry.sku());
                reservation.setQuantity(entry.reserveQty());
                reservation.setOrderId(orderId);
                reservation.setExpireAt(Instant.now().plus(Duration.ofMinutes(reservationExpiryMinutes)));
                reservation.setStoreMerchantId(store);
                reservation.setProductAvailability(availability);
                reservation.setStatus(status);

                productReservationRepository.save(reservation);
            }
            productRepository.save(product);
        }
    }

    @Transactional
    @Override
    public void commit(StoreMerchantId store, Long orderId) {
        List<ProductReservation> reservations =
                productReservationRepository.findAllByOrderId(orderId);
        for (ProductReservation res : reservations) {
            if (res.getStatus() == ProductReservationStatusEnum.RESERVED) {
                res.setStatus(ProductReservationStatusEnum.COMPLETED);
                productReservationRepository.save(res);
                log.info("Committed reservation for order {} and sku {}", orderId, res.getSku());
            }
        }
    }

    @Transactional
    @Override
    public void release(StoreMerchantId store, Long orderId) {
        List<ProductReservation> reservations =
                productReservationRepository.findAllByOrderId(orderId);
        for (ProductReservation res : reservations) {
            // Restore quantity for RESERVED and COMPLETED status
            if (res.getStatus() == ProductReservationStatusEnum.RESERVED || res.getStatus() == ProductReservationStatusEnum.COMPLETED) {
                var availability = res.getProductAvailability();
                availability.setProductQuantity(availability.getProductQuantity() + res.getQuantity());

                // Mark as expired/cancelled
                res.setStatus(ProductReservationStatusEnum.EXPIRED);
                productReservationRepository.save(res);
                log.info("Released reservation for order {} and sku {}. Status was {}", orderId, res.getSku(), res.getStatus());
            }
        }
    }

    @Override
    public Page<Product> findAll(ProductCriteria criteria, StoreMerchantId store) {
        return productRepository.findAll(criteria, store, criteria.getPageable());
    }

    @SneakyThrows
    @Override
    public ProductDetails getDetailedProduct(StoreMerchantId store, String sku, LanguageCode language) {
        Product p = getMinimalProductBySku(sku, store, language);
        ReadableMinimalProduct product = readableMinimalProductMapper.convert(p, store, language);
        FinalPriceCalc price = pricingService.calculateProductPrice(p);
        ReadableProductAvailability availability = readableProductAvailabilityMapper.convert(p, store, null);
        return new ProductDetails(product, price, availability);
    }

    @Override
    public Product saveProduct(Product product) throws ServiceException {
        try {
            return this.saveOrUpdate(product);
        } catch (ServiceException e) {
            throw new ServiceException("Cannot create product [" + product.getId() + "]", e);
        }
    }

    public Product getMinimalProductBySku(String productCode, StoreMerchantId merchant, LanguageCode language)
            throws ServiceException {

        try {
            Long productId = findProductIdByCode(productCode, merchant);
            return productRepository.getMinimalProductById(productId, merchant, language);
        } catch (Exception e) {
            throw new ServiceException("Cannot get product with sku [" + productCode + "]", e);
        }
    }

    @Override
    public Product getBySku(String productCode, StoreMerchantId merchant, LanguageCode language)
            throws ServiceException {

        try {
            Long productId = findProductIdByCode(productCode, merchant);
            return productRepository.getById(productId, merchant, language);
        } catch (Exception e) {
            throw new ServiceException("Cannot get product with sku [" + productCode + "]", e);
        }
    }

    public Product getBySku(String productCode, StoreMerchantId merchant) throws ServiceException {

        try {
            Long productId = findProductIdByCode(productCode, merchant);
            return this.findOne(productId, merchant);
        } catch (Exception e) {
            throw new ServiceException("Cannot get product with sku [" + productCode + "]", e);
        }
    }

    private Long findProductIdByCode(String productCode, StoreMerchantId merchant) throws ServiceException {
        List<Long> products = productRepository.findBySku(productCode, merchant);
        if (products.isEmpty()) {
            throw new ServiceException("Cannot get product with sku [" + productCode + "]");
        }
        return products.getFirst();
    }

    @Override
    public boolean exists(String sku, StoreMerchantId store) {
        return productRepository.existsBySku(sku, store);
    }

}
