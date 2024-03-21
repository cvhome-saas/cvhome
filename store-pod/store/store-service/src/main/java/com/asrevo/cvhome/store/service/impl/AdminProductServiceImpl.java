package com.asrevo.cvhome.store.service.impl;

import com.asrevo.cvhome.commons.utils.OperationExecution;
import com.asrevo.cvhome.store.commons.domain.SubProducts;
import com.asrevo.cvhome.store.commons.dto.*;
import com.asrevo.cvhome.store.entity.ProductDetailsEntity;
import com.asrevo.cvhome.store.entity.ProductEntity;
import com.asrevo.cvhome.store.mappers.ProductMapper;
import com.asrevo.cvhome.store.repository.ProductDetailsRepository;
import com.asrevo.cvhome.store.repository.ProductRepository;
import com.asrevo.cvhome.store.service.AdminCategoryService;
import com.asrevo.cvhome.store.service.AdminProductService;
import com.asrevo.cvhome.store.utils.ErrorCodes;
import com.asrevo.cvhome.storepod.commons.domain.ProductDetails;
import com.asrevo.cvhome.storepod.commons.domain.ProductId;
import com.asrevo.cvhome.storepod.commons.domain.ProductType;
import com.asrevo.cvhome.storepod.commons.domain.StoreId;
import com.asrevo.cvhome.storepod.commons.dto.DetailedProductDto;
import com.asrevo.cvhome.storepod.commons.dto.FindAllProductDto;
import com.asrevo.cvhome.storepod.commons.dto.ProductDto;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.ignoreCase;

@Service
@AllArgsConstructor
public class AdminProductServiceImpl implements AdminProductService {
    private final ProductRepository productRepository;
    private final AdminCategoryService categoryService;
    private final ProductDetailsRepository productDetailsRepository;
    private final ProductMapper productMapper;


    @Override
    public Page<ProductDto> findAll(StoreId storeId, FindAllProductDto dto, Pageable pageable) {
        ProductEntity product = productMapper.toEntity(dto);
        product.setStoreId(storeId);
        product.setDeleted(Boolean.FALSE);
        ExampleMatcher matcher = ExampleMatcher.matching();
        if (Objects.nonNull(dto.name())) {
            matcher = matcher.withMatcher("name", ignoreCase().contains());
        }
        Page<ProductEntity> all = productRepository.findAll(Example.of(product, matcher), pageable);
        return new PageImpl<>(all.stream().map(productMapper::toDto).toList(), all.getPageable(), all.getTotalElements());
    }

    private CreateProductResponseDto createProduct(StoreId storeId, CreateProductDto createProductDto) {
        CategoryDto category = categoryService.findCategory(createProductDto.category(), storeId);
        ProductEntity savedProduct = productRepository.save(ProductEntity.createProduct(storeId, category.id(), createProductDto));
        return productMapper.toCreateProductResponseDto(savedProduct);
    }

    private void addSubProduct(StoreId storeId, ProductId productId, List<DetailedProductDto> detailedProductDto) {
        SubProducts subProducts = detailedProductDto.stream().map(DetailedProductDto::id)
                .collect(Collectors.collectingAndThen(Collectors.toList(), SubProducts::new));

        ProductEntity productEntity = getProductEntity(storeId, productId);

        validateSubProducts(storeId, productEntity.getProductType(), subProducts);

        productEntity.setSubProducts(subProducts);
        productRepository.save(productEntity);
    }

    @Transactional
    @Override
    public DetailedProductDto createProduct(StoreId storeId, CreateDetailedProductDto detailedProductDto) {
        CreateProductResponseDto product = createProduct(storeId, detailedProductDto.dto());
        if (detailedProductDto.subProducts() != null && !detailedProductDto.subProducts().isEmpty()) {
            addSubProduct(storeId, product.id(), detailedProductDto.subProducts());
        }
        if (detailedProductDto.productDetails() != null) {
            addProductDetails(storeId, product.id(), detailedProductDto.productDetails());
        }
        return getDetailedProduct(storeId, product.id());
    }

    private void validateSubProducts(StoreId storeId, ProductType productType, SubProducts productIds) {
        if (ProductType.SINGLE.equals(productType)) {
            if (!CollectionUtils.isEmpty(productIds.productIds())) {
                throw new OperationExecution(ErrorCodes.single_product_should_not_have_sub_product);
            }
        } else {
            if (!CollectionUtils.isEmpty(productIds.productIds())) {
                List<ProductEntity> subProducts = productRepository.findAllById(productIds.productIds());
                if (!subProducts.stream().allMatch(it -> it.getStoreId().equals(storeId))) {
                    throw new OperationExecution(ErrorCodes.one_or_more_sub_product_not_from_this_store);
                }
            }
        }
    }

    @Transactional
    @Override
    public DeleteProductResponseDto deleteProduct(StoreId storeId, ProductId productId) {
        ProductEntity productEntity = getProductEntity(storeId, productId);

        productEntity.delete();
        productRepository.save(productEntity);
        return new DeleteProductResponseDto(productEntity.getId(), Boolean.TRUE);
    }

    private void addProductDetails(StoreId storeId, ProductId productId, ProductDetails productDetails) {
        productDetailsRepository.save(ProductDetailsEntity.create(storeId, productId, productDetails));
    }

    @Transactional
    @Override
    public PublishProductResponseDto publishProduct(StoreId storeId, ProductId productId) {
        ProductEntity productEntity = getProductEntity(storeId, productId);
        ProductDetailsEntity productDetailsEntity = productDetailsRepository.findByStoreIdAndAndProduct(storeId, productId)
                .orElseThrow(() -> new OperationExecution(ErrorCodes.product_details_not_created_yet));
        List<ProductDetails> productDetails = getSubProductDetails(storeId, productEntity);
        productEntity.publish(productDetailsEntity.getProductDetails(), productDetails);
        productRepository.save(productEntity);
        return new PublishProductResponseDto(productId, Boolean.TRUE);
    }

    private List<ProductDetails> getSubProductDetails(StoreId storeId, ProductEntity productEntity) {
        List<ProductDetails> productDetails;
        if (ProductType.GROUP.equals(productEntity.getProductType()) || ProductType.VARIANT.equals(productEntity.getProductType())) {
            productDetails = productDetailsRepository.findAllByStoreIdAndAndProductIn(storeId, productEntity.getSubProducts().productIds())
                    .stream()
                    .map(ProductDetailsEntity::getProductDetails)
                    .toList();
            if (productDetails.size() != productEntity.getSubProducts().size()) {
                throw new OperationExecution(ErrorCodes.one_of_your_sub_products_not_have_product_details_yet);
            }
        } else {
            productDetails = List.of();
        }
        return productDetails;
    }

    @Transactional
    @Override
    public PublishProductResponseDto unPublishProduct(StoreId storeId, ProductId productId) {
        ProductEntity productEntity = getProductEntity(storeId, productId);
        productEntity.unPublish();
        productRepository.save(productEntity);
        return new PublishProductResponseDto(productEntity.getId(), Boolean.FALSE);
    }

    private ProductEntity getProductEntity(StoreId storeId, ProductId productId) {
        return productRepository.findOneByStoreIdAndIdAndDeletedIsFalse(storeId, productId)
                .orElseThrow(() -> new OperationExecution(ErrorCodes.product_not_exist));
    }

    @Override
    public ProductDto getProduct(StoreId storeId, ProductId productId) {
        ProductEntity productEntity = getProductEntity(storeId, productId);
        return productMapper.toDto(productEntity);
    }

    @Override
    public DetailedProductDto getDetailedProduct(StoreId storeId, ProductId productId) {
        ProductEntity productEntity = getProductEntity(storeId, productId);
        ProductDto dto = productMapper.toDto(productEntity);
        ProductDetails productDetails = productDetailsRepository.findByStoreIdAndAndProduct(storeId, productId).map(ProductDetailsEntity::getProductDetails).orElse(null);
        List<DetailedProductDto> list = productRepository.findOneByStoreIdAndIdInAndDeletedIsFalse(storeId, productEntity.getSubProducts().productIds())
                .stream().map(productMapper::toDto)
                .map(it -> {
                    ProductDetails d = productDetailsRepository.findByStoreIdAndAndProduct(storeId, it.id()).map(ProductDetailsEntity::getProductDetails).orElse(null);
                    return new DetailedProductDto(it.id(), it, d, List.of());
                })
                .toList();
        return new DetailedProductDto(productId, dto, productDetails, list);
    }

}
