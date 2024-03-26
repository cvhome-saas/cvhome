package com.asrevo.cvhome.store.service.impl;

import com.asrevo.cvhome.commons.utils.OperationExecution;
import com.asrevo.cvhome.store.entity.ProductDetailsEntity;
import com.asrevo.cvhome.store.entity.ProductEntity;
import com.asrevo.cvhome.store.mappers.ProductMapper;
import com.asrevo.cvhome.store.repository.ProductDetailsRepository;
import com.asrevo.cvhome.store.repository.ProductRepository;
import com.asrevo.cvhome.store.service.ProductService;
import com.asrevo.cvhome.store.utils.ErrorCodes;
import com.asrevo.cvhome.storepod.commons.domain.ProductDetails;
import com.asrevo.cvhome.storepod.commons.domain.ProductId;
import com.asrevo.cvhome.storepod.commons.domain.ProductType;
import com.asrevo.cvhome.storepod.commons.domain.StoreId;
import com.asrevo.cvhome.storepod.commons.dto.DetailedProductDto;
import com.asrevo.cvhome.storepod.commons.dto.ProductDto;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductDetailsRepository productDetailsRepository;
    private final ProductMapper productMapper;


    @Override
    public Page<ProductDto> findAll(StoreId storeId, Pageable pageable) {
        ProductEntity product = new ProductEntity();
        product.setStoreId(storeId);
        product.setDeleted(Boolean.FALSE);
        product.setPublished(Boolean.TRUE);
        ExampleMatcher matcher = ExampleMatcher.matching();

        Page<ProductEntity> all = productRepository.findAll(Example.of(product, matcher), pageable);
        return new PageImpl<>(all.stream().map(productMapper::toDto).toList(), all.getPageable(), all.getTotalElements());
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

    private ProductEntity getProductEntity(StoreId storeId, ProductId productId) {
        return productRepository.findOneByStoreIdAndIdAndDeletedIsFalse(storeId, productId)
                .orElseThrow(() -> new OperationExecution(ErrorCodes.product_not_exist));
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
