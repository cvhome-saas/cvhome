package com.asrevo.cvhome.catalog.service.facade.product;

import static com.asrevo.cvhome.store.utils.ReadableEntityUtil.createReadableList;

import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariant;
import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariantGroup;
import com.asrevo.cvhome.catalog.model.product.product.variantGroup.PersistableProductVariantGroup;
import com.asrevo.cvhome.catalog.model.product.product.variantGroup.ReadableProductVariantGroup;
import com.asrevo.cvhome.catalog.service.mapper.catalog.product.PersistableProductVariantGroupMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.product.ReadableProductVariantGroupMapper;
import com.asrevo.cvhome.catalog.services.product.variant.ProductVariantGroupService;
import com.asrevo.cvhome.catalog.services.product.variant.ProductVariantService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class ProductVariantGroupFacadeImpl implements ProductVariantGroupFacade {

	private final ProductVariantGroupService productVariantGroupService;

	private final ProductVariantService productVariantService;

	private final PersistableProductVariantGroupMapper persistableProductIntanceGroupMapper;

	private final ReadableProductVariantGroupMapper readableProductVariantGroupMapper;

	public ProductVariantGroupFacadeImpl(ProductVariantGroupService productVariantGroupService,
			ProductVariantService productVariantService,
			PersistableProductVariantGroupMapper persistableProductIntanceGroupMapper,
			ReadableProductVariantGroupMapper readableProductVariantGroupMapper) {
		this.productVariantGroupService = productVariantGroupService;
		this.productVariantService = productVariantService;
		this.persistableProductIntanceGroupMapper = persistableProductIntanceGroupMapper;
		this.readableProductVariantGroupMapper = readableProductVariantGroupMapper;
	}

	@Override
	public ReadableProductVariantGroup get(Long instanceGroupId, StoreMerchantId store, LanguageCode language) {

		ProductVariantGroup group = this.group(instanceGroupId, store);
		return readableProductVariantGroupMapper.convert(group, store, language);
	}

	@Override
	public Long create(PersistableProductVariantGroup productVariantGroup, StoreMerchantId store,
			LanguageCode language) {

		ProductVariantGroup group = persistableProductIntanceGroupMapper.convert(productVariantGroup, store, language);
		group = productVariantGroupService.saveOrUpdate(group);

		return group.getId();
	}

	@Override
	public void update(Long productVariantGroup, PersistableProductVariantGroup instance, StoreMerchantId store,
			LanguageCode language) {
		ProductVariantGroup group = this.group(productVariantGroup, store);
		instance.setId(productVariantGroup);

		group = persistableProductIntanceGroupMapper.merge(instance, group, store, language);

		productVariantGroupService.saveOrUpdate(group);
	}

	@Override
	public void delete(Long productVariantGroup, Long productId, StoreMerchantId store) {

		ProductVariantGroup group = this.group(productVariantGroup, store);

		try {

			// null all group from instances
			for (ProductVariant instance : group.getProductVariants()) {
				Optional<ProductVariant> p = productVariantService.getById(instance.getId(), store);
				if (p.isEmpty()) {
					throw new ResourceNotFoundException(
							"Product instance [" + instance.getId() + " not found for store [" + store + "]");
				}
				instance.setProductVariantGroup(null);
				productVariantService.save(instance);
			}

			// now delete
			productVariantGroupService.delete(group);
		}
		catch (ServiceException e) {
			throw new ServiceRuntimeException(
					"Cannot remove product instance group [" + productVariantGroup + "] for store [" + store + "]");
		}
	}

	@Override
	public ReadableEntityList<ReadableProductVariantGroup> list(Long productId, StoreMerchantId store,
			LanguageCode language, Pageable pageable) {

		Page<ProductVariantGroup> groups = productVariantGroupService.getByProductId(store, productId, language,
				pageable);

		List<ReadableProductVariantGroup> readableInstances = groups.stream()
			.map(rp -> this.readableProductVariantGroupMapper.convert(rp, store, language))
			.collect(Collectors.toList());

		return createReadableList(groups, readableInstances);
	}

	private ProductVariantGroup group(Long productOptionGroupId, StoreMerchantId store) {
		Optional<ProductVariantGroup> group = productVariantGroupService.getById(productOptionGroupId, store);
		if (group.isEmpty()) {
			throw new ResourceNotFoundException("Product instance group [" + productOptionGroupId + "] not found");
		}

		return group.get();
	}

	/*
	 * @TODO not needed for now
	 *
	 * @Override public void addImage(MultipartFile image, Long instanceGroupId,
	 * StoreMerchantId store, LanguageCode language) {
	 *
	 *
	 * Assert.notNull(instanceGroupId, "productVariantGroupId must not be null");
	 * Assert.notNull(image, "Image must not be null"); Assert.notNull(store,
	 * "store cannot be null"); //get option group
	 *
	 * ProductVariantGroup group = this.group(instanceGroupId, store); ProductVariantImage
	 * instanceImage = new ProductVariantImage();
	 *
	 * try {
	 *
	 * String path = "group" + Constants.SLASH + instanceGroupId;
	 *
	 *
	 * instanceImage.setProductImage(image.getOriginalFilename());
	 * instanceImage.setProductVariantGroup(group); String imageName =
	 * image.getOriginalFilename(); InputStream inputStream = image.getInputStream();
	 * InputContentFile cmsContentImage = new InputContentFile();
	 * cmsContentImage.setFileName(imageName);
	 * cmsContentImage.setMimeType(image.getContentType());
	 * cmsContentImage.setFile(inputStream); cmsContentImage.setPath(path);
	 * cmsContentImage.setFileContentType(FileContentType.VARIANT);
	 *
	 * contentService.addContentFile(store.getId(), cmsContentImage);
	 *
	 * group.getImages().add(instanceImage);
	 *
	 * productVariantGroupService.saveOrUpdate(group); } catch (Exception e) { throw new
	 * ServiceRuntimeException("Exception while adding instance group image", e); }
	 *
	 *
	 * }
	 *
	 * @Override public void removeImage(Long imageId, Long productVariantGroupId,
	 * StoreMerchantId store) {
	 *
	 * Assert.notNull(productVariantGroupId, "productVariantGroupId must not be null");
	 * Assert.notNull(store, "store cannot be null");
	 *
	 * ProductVariantImage image = productVariantImageService.getById(imageId);
	 *
	 * if (image == null) { throw new ResourceNotFoundException("productVariantImage [" +
	 * imageId + "] was not found"); }
	 *
	 * ProductVariantGroup group = this.group(productVariantGroupId, store);
	 *
	 *
	 * try { contentService.removeFile(Constants.SLASH + store + Constants.SLASH +
	 * productVariantGroupId, FileContentType.VARIANT, image.getProductImage());
	 * group.getImages().removeIf(i -> (i.getId().equals(image.getId()))); //update
	 * productVariantroup productVariantGroupService.update(group); } catch
	 * (ServiceException e) { throw new
	 * ServiceRuntimeException("An exception occured while removing instance image [" +
	 * imageId + "]", e); }
	 *
	 * }
	 */

}
