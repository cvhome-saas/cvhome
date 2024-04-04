package com.asrevo.cvhome.store.service.facade.product;

import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.catalog.product.variant.ProductVariant;
import com.asrevo.cvhome.store.core.entity.catalog.product.variant.ProductVariantGroup;
import com.asrevo.cvhome.store.core.entity.catalog.product.variant.ProductVariantImage;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.InputContentFile;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.product.product.variantGroup.PersistableProductVariantGroup;
import com.asrevo.cvhome.store.core.model.catalog.product.product.variantGroup.ReadableProductVariantGroup;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;
import com.asrevo.cvhome.store.core.services.catalog.product.variant.ProductVariantGroupService;
import com.asrevo.cvhome.store.core.services.catalog.product.variant.ProductVariantImageService;
import com.asrevo.cvhome.store.core.services.catalog.product.variant.ProductVariantService;
import com.asrevo.cvhome.store.core.services.content.ContentService;
import com.asrevo.cvhome.store.service.mapper.catalog.product.PersistableProductVariantGroupMapper;
import com.asrevo.cvhome.store.service.mapper.catalog.product.ReadableProductVariantGroupMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.asrevo.cvhome.store.utils.ReadableEntityUtil.createReadableList;


@Component
public class ProductVariantGroupFacadeImpl implements ProductVariantGroupFacade {

    private final ProductVariantGroupService productVariantGroupService;

    private final ProductVariantService productVariantService;

    private final ProductVariantImageService productVariantImageService;

    private final PersistableProductVariantGroupMapper persistableProductIntanceGroupMapper;

    private final ReadableProductVariantGroupMapper readableProductVariantGroupMapper;

    private final ContentService contentService; //file management

    public ProductVariantGroupFacadeImpl(ProductVariantGroupService productVariantGroupService, ProductVariantService productVariantService, ProductVariantImageService productVariantImageService, PersistableProductVariantGroupMapper persistableProductIntanceGroupMapper, ReadableProductVariantGroupMapper readableProductVariantGroupMapper, ContentService contentService) {
        this.productVariantGroupService = productVariantGroupService;
        this.productVariantService = productVariantService;
        this.productVariantImageService = productVariantImageService;
        this.persistableProductIntanceGroupMapper = persistableProductIntanceGroupMapper;
        this.readableProductVariantGroupMapper = readableProductVariantGroupMapper;
        this.contentService = contentService;
    }

    @Override
    public ReadableProductVariantGroup get(Long instanceGroupId, MerchantStore store, Language language) {

        ProductVariantGroup group = this.group(instanceGroupId, store);
        return readableProductVariantGroupMapper.convert(group, store, language);
    }

    @Override
    public Long create(PersistableProductVariantGroup productVariantGroup, MerchantStore store, Language language) {

        ProductVariantGroup group = persistableProductIntanceGroupMapper.convert(productVariantGroup, store, language);
        try {
            group = productVariantGroupService.saveOrUpdate(group);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Cannot save product instance group [" + productVariantGroup + "] for store [" + store.getCode() + "]");
        }

        return group.getId();
    }

    @Override
    public void update(Long productVariantGroup, PersistableProductVariantGroup instance, MerchantStore store,
                       Language language) {
        ProductVariantGroup group = this.group(productVariantGroup, store);
        instance.setId(productVariantGroup);

        group = persistableProductIntanceGroupMapper.merge(instance, group, store, language);

        try {
            productVariantGroupService.saveOrUpdate(group);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Cannot save product instance group [" + productVariantGroup + "] for store [" + store.getCode() + "]");
        }

    }

    @Override
    public void delete(Long productVariantGroup, Long productId, MerchantStore store) {

        ProductVariantGroup group = this.group(productVariantGroup, store);

        if (group == null) {
            throw new ResourceNotFoundException("Product instance group [" + group.getId() + " not found for store [" + store.getCode() + "]");
        }

        try {

            //null all group from instances
            for (ProductVariant instance : group.getProductVariants()) {
                Optional<ProductVariant> p = productVariantService.getById(instance.getId(), store);
                if (p.isEmpty()) {
                    throw new ResourceNotFoundException("Product instance [" + instance.getId() + " not found for store [" + store.getCode() + "]");
                }
                instance.setProductVariantGroup(null);
                productVariantService.save(instance);
            }

            //now delete
            productVariantGroupService.delete(group);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Cannot remove product instance group [" + productVariantGroup + "] for store [" + store.getCode() + "]");
        }

    }

    @Override
    public ReadableEntityList<ReadableProductVariantGroup> list(Long productId, MerchantStore store, Language language,
                                                                int page, int count) {


        Page<ProductVariantGroup> groups = productVariantGroupService.getByProductId(store, productId, language, page, count);

        List<ReadableProductVariantGroup> readableInstances = groups.stream()
                .map(rp -> this.readableProductVariantGroupMapper.convert(rp, store, language)).collect(Collectors.toList());

        return createReadableList(groups, readableInstances);

    }


    private ProductVariantGroup group(Long productOptionGroupId, MerchantStore store) {
        Optional<ProductVariantGroup> group = productVariantGroupService.getById(productOptionGroupId, store);
        if (group.isEmpty()) {
            throw new ResourceNotFoundException("Product instance group [" + productOptionGroupId + "] not found");
        }

        return group.get();
    }

    @Override
    public void addImage(MultipartFile image, Long instanceGroupId,
                         MerchantStore store, Language language) {


        Assert.notNull(instanceGroupId, "productVariantGroupId must not be null");
        Assert.notNull(image, "Image must not be null");
        Assert.notNull(store, "MerchantStore must not be null");
        //get option group

        ProductVariantGroup group = this.group(instanceGroupId, store);
        ProductVariantImage instanceImage = new ProductVariantImage();

        try {

            String path = "group" + Constants.SLASH + instanceGroupId;


            instanceImage.setProductImage(image.getOriginalFilename());
            instanceImage.setProductVariantGroup(group);
            String imageName = image.getOriginalFilename();
            InputStream inputStream = image.getInputStream();
            InputContentFile cmsContentImage = new InputContentFile();
            cmsContentImage.setFileName(imageName);
            cmsContentImage.setMimeType(image.getContentType());
            cmsContentImage.setFile(inputStream);
            cmsContentImage.setPath(path);
            cmsContentImage.setFileContentType(FileContentType.VARIANT);

            contentService.addContentFile(store.getCode(), cmsContentImage);

            group.getImages().add(instanceImage);

            productVariantGroupService.saveOrUpdate(group);
        } catch (Exception e) {
            throw new ServiceRuntimeException("Exception while adding instance group image", e);
        }


    }

    @Override
    public void removeImage(Long imageId, Long productVariantGroupId, MerchantStore store) {

        Assert.notNull(productVariantGroupId, "productVariantGroupId must not be null");
        Assert.notNull(store, "MerchantStore must not be null");

        ProductVariantImage image = productVariantImageService.getById(imageId);

        if (image == null) {
            throw new ResourceNotFoundException("productVariantImage [" + imageId + "] was not found");
        }

        ProductVariantGroup group = this.group(productVariantGroupId, store);


        try {
            contentService.removeFile(Constants.SLASH + store.getCode() + Constants.SLASH + productVariantGroupId, FileContentType.VARIANT, image.getProductImage());
            group.getImages().removeIf(i -> (i.getId() == image.getId()));
            //update productVariantroup
            productVariantGroupService.update(group);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("An exception occured while removing instance image [" + imageId + "]", e);
        }

    }

}
