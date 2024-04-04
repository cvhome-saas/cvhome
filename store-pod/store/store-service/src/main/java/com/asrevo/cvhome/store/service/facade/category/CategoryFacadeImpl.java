package com.asrevo.cvhome.store.service.facade.category;

import com.asrevo.cvhome.store.controller.exception.*;
import com.asrevo.cvhome.store.core.entity.catalog.category.Category;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductAttribute;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOption;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOptionDescription;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOptionValueDescription;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.category.PersistableCategory;
import com.asrevo.cvhome.store.core.model.catalog.category.ReadableCategory;
import com.asrevo.cvhome.store.core.model.catalog.category.ReadableCategoryList;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ProductOptionValue;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ReadableProductVariant;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ReadableProductVariantValue;
import com.asrevo.cvhome.store.core.model.entity.ListCriteria;
import com.asrevo.cvhome.store.core.services.catalog.category.CategoryService;
import com.asrevo.cvhome.store.core.services.catalog.product.attribute.ProductAttributeService;
import com.asrevo.cvhome.store.core.services.merchant.MerchantStoreService;
import com.asrevo.cvhome.store.service.mapper.catalog.ReadableCategoryMapper;
import com.asrevo.cvhome.store.service.populator.catalog.PersistableCategoryPopulator;
import com.asrevo.cvhome.store.service.populator.catalog.ReadableCategoryPopulator;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service(value = "categoryFacade")
public class CategoryFacadeImpl implements CategoryFacade {

    private static final String FEATURED_CATEGORY = "featured";
    private static final String VISIBLE_CATEGORY = "visible";
    private static final String ADMIN_CATEGORY = "admin";
    private final CategoryService categoryService;
    private final MerchantStoreService merchantStoreService;
    private final PersistableCategoryPopulator persistableCatagoryPopulator;
    private final ReadableCategoryMapper readableCategoryMapper;
    private final ProductAttributeService productAttributeService;

    public CategoryFacadeImpl(CategoryService categoryService, MerchantStoreService merchantStoreService, PersistableCategoryPopulator persistableCatagoryPopulator, ReadableCategoryMapper readableCategoryMapper, ProductAttributeService productAttributeService) {
        this.categoryService = categoryService;
        this.merchantStoreService = merchantStoreService;
        this.persistableCatagoryPopulator = persistableCatagoryPopulator;
        this.readableCategoryMapper = readableCategoryMapper;
        this.productAttributeService = productAttributeService;
    }

    @Override
    public ReadableCategoryList getCategoryHierarchy(MerchantStore store, ListCriteria criteria, int depth,
                                                     Language language, List<String> filter, int page, int count) {

        Assert.notNull(store, "MerchantStore can not be null");


        //get parent store
        try {

            MerchantStore parent = merchantStoreService.getParent(store.getCode());


            List<Category> categories = null;
            ReadableCategoryList returnList = new ReadableCategoryList();
            if (!CollectionUtils.isEmpty(filter) && filter.contains(FEATURED_CATEGORY)) {
                categories = categoryService.getListByDepthFilterByFeatured(parent, depth, language);
                returnList.setRecordsTotal(categories.size());
                returnList.setNumber(categories.size());
                returnList.setTotalPages(1);
            } else {
                org.springframework.data.domain.Page<Category> pageable = categoryService.getListByDepth(parent, language,
                        criteria != null ? criteria.getName() : null, depth, page, count);
                categories = pageable.getContent();
                returnList.setRecordsTotal(pageable.getTotalElements());
                returnList.setTotalPages(pageable.getTotalPages());
                returnList.setNumber(categories.size());
            }


            List<ReadableCategory> readableCategories = null;
            if (filter != null && filter.contains(VISIBLE_CATEGORY)) {
                readableCategories = categories.stream().filter(Category::isVisible)
                        .map(cat -> readableCategoryMapper.convert(cat, store, language))
                        .collect(Collectors.toList());
            } else {
                readableCategories = categories.stream()
                        .map(cat -> readableCategoryMapper.convert(cat, store, language))
                        .collect(Collectors.toList());
            }

            Map<Long, ReadableCategory> readableCategoryMap = readableCategories.stream()
                    .collect(Collectors.toMap(ReadableCategory::getId, Function.identity()));

            readableCategories.stream()
                    // .filter(ReadableCategory::isVisible)
                    .filter(cat -> Objects.nonNull(cat.getParent()))
                    .filter(cat -> readableCategoryMap.containsKey(cat.getParent().getId())).forEach(readableCategory -> {
                        ReadableCategory parentCategory = readableCategoryMap.get(readableCategory.getParent().getId());
                        if (parentCategory != null) {
                            parentCategory.getChildren().add(readableCategory);
                        }
                    });

            List<ReadableCategory> filteredList = new ArrayList<>(readableCategoryMap.values());

            //execute only if not admin filtered
            if (filter == null || (filter != null && !filter.contains(ADMIN_CATEGORY))) {
                filteredList = readableCategoryMap.values().stream().filter(cat -> cat.getDepth() == 0)
                        .sorted(Comparator.comparing(ReadableCategory::getSortOrder)).collect(Collectors.toList());

                returnList.setNumber(filteredList.size());

            }

            returnList.setCategories(filteredList);


            return returnList;

        } catch (ServiceException e) {
            throw new ServiceRuntimeException(e);
        }

    }

    @Override
    public boolean existByCode(MerchantStore store, String code) {
        try {
            Category c = categoryService.getByCode(store, code);
            return c != null;
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    @Override
    public PersistableCategory saveCategory(MerchantStore store, PersistableCategory category) {
        try {

            Long categoryId = category.getId();
            Category target = Optional.ofNullable(categoryId)
                    .filter(merchant -> store != null)
                    .filter(id -> id > 0)
                    .map(categoryService::getById)
                    .orElse(new Category());

            Category dbCategory = populateCategory(store, category, target);
            saveCategory(store, dbCategory, null);

            // set category id
            category.setId(dbCategory.getId());
            return category;
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Error while updating category", e);
        }
    }

    private Category populateCategory(MerchantStore store, PersistableCategory category, Category target) {
        try {
            return persistableCatagoryPopulator.populate(category, target, store, store.getDefaultLanguage());
        } catch (ConversionException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    private void saveCategory(MerchantStore store, Category category, Category parent) throws ServiceException {

        /**
         * c.children1
         *
         * <p>
         * children1.children1.children2
         *
         * <p>
         * children1.children2.children1
         */

        /** set lineage * */
        if (parent != null) {
            category.setParent(category);

            String lineage = parent.getLineage();
            int depth = parent.getDepth();

            category.setDepth(depth + 1);
            category.setLineage(lineage);// service
            // will
            // adjust
            // lineage
        }

        category.setMerchantStore(store);

        // remove children
        List<Category> children = category.getCategories();
        List<Category> saveAfter = children.stream().filter(c -> c.getId() == null || c.getId().longValue() == 0).collect(Collectors.toList());
        List<Category> saveNow = children.stream().filter(c -> c.getId() != null && c.getId().longValue() > 0).collect(Collectors.toList());
        category.setCategories(saveNow);

        /** set parent * */
        if (parent != null) {
            category.setParent(parent);
        }

        categoryService.saveOrUpdate(category);

        if (!CollectionUtils.isEmpty(saveAfter)) {
            parent = category;
            for (Category c : saveAfter) {
                if (c.getId() == null || c.getId().longValue() == 0) {
                    for (Category sub : children) {
                        saveCategory(store, sub, parent);
                    }
                }
            }
        }

    }

    @Override
    public ReadableCategory getById(MerchantStore store, Long id, Language language) {

        Category categoryModel = null;
        if (language != null) {
            categoryModel = getCategoryById(id, language);
        } else {// all langs
            categoryModel = getById(store, id);
        }

        if (categoryModel == null)
            throw new ResourceNotFoundException("Categori id [" + id + "] not found");

        String lineage = categoryModel.getLineage();

        ReadableCategory readableCategory = readableCategoryMapper.convert(categoryModel, store,
                language);

        // get children
        List<Category> children = getListByLineage(store, lineage);

        List<ReadableCategory> childrenCats = children.stream()
                .map(cat -> readableCategoryMapper.convert(cat, store, language))
                .collect(Collectors.toList());

        addChildToParent(readableCategory, childrenCats);
        return readableCategory;

    }

    private void addChildToParent(ReadableCategory readableCategory, List<ReadableCategory> childrenCats) {
        Map<Long, ReadableCategory> categoryMap = childrenCats.stream()
                .collect(Collectors.toMap(ReadableCategory::getId, Function.identity()));
        categoryMap.put(readableCategory.getId(), readableCategory);

        // traverse map and add child to parent
        for (ReadableCategory readable : childrenCats) {

            if (readable.getParent() != null) {

                ReadableCategory rc = categoryMap.get(readable.getParent().getId());
                if (rc != null) {
                    rc.getChildren().add(readable);
                }
            }
        }
    }

    private List<Category> getListByLineage(MerchantStore store, String lineage) {
        try {
            return categoryService.getListByLineage(store, lineage);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(String.format("Error while getting root category %s", e.getMessage()), e);
        }
    }

    private Category getCategoryById(Long id, Language language) {
        return Optional.ofNullable(categoryService.getOneByLanguage(id, language))
                .orElseThrow(() -> new ResourceNotFoundException("Category id [" + id + "] not found"));
    }

    @Override
    public void deleteCategory(Category category) {
        try {
            categoryService.delete(category);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Error while deleting category", e);
        }
    }

    @Override
    public ReadableCategory getByCode(MerchantStore store, String code, Language language) throws Exception {

        Assert.notNull(code, "category code must not be null");
        ReadableCategoryPopulator categoryPopulator = new ReadableCategoryPopulator();
        ReadableCategory readableCategory = new ReadableCategory();

        Category category = categoryService.getByCode(store, code);
        categoryPopulator.populate(category, readableCategory, store, language);

        return readableCategory;
    }

    @Override
    public ReadableCategory getCategoryByFriendlyUrl(MerchantStore store, String friendlyUrl, Language language) throws Exception {
        Assert.notNull(friendlyUrl, "Category search friendly URL must not be null");


        Category category = categoryService.getBySeUrl(store, friendlyUrl, language);

        if (category == null) {
            throw new ResourceNotFoundException("Category with friendlyUrl [" + friendlyUrl + "] was not found");
        }

        ReadableCategoryPopulator categoryPopulator = new ReadableCategoryPopulator();
        ReadableCategory readableCategory = new ReadableCategory();


        categoryPopulator.populate(category, readableCategory, store, language);

        return readableCategory;
    }

    private Category getById(MerchantStore store, Long id) {
        Assert.notNull(id, "category id must not be null");
        Assert.notNull(store, "MerchantStore must not be null");
        Category category = categoryService.getById(id, store.getId());
        if (category == null) {
            throw new ResourceNotFoundException("Category with id [" + id + "] not found");
        }
        if (category.getMerchantStore().getId().intValue() != store.getId().intValue()) {
            throw new UnauthorizedException("Unauthorized");
        }
        return category;
    }

    @Override
    public void deleteCategory(Long categoryId, MerchantStore store) {
        Category category = getOne(categoryId, store.getId());
        deleteCategory(category);
    }

    private Category getOne(Long categoryId, int storeId) {
        return Optional.ofNullable(categoryService.getById(categoryId)).orElseThrow(
                () -> new ResourceNotFoundException(String.format("No Category found for ID : %s", categoryId)));
    }

    @Override
    public List<ReadableProductVariant> categoryProductVariants(Long categoryId, MerchantStore store,
                                                                Language language) {
        Category category = categoryService.getById(categoryId, store.getId());

        List<ReadableProductVariant> variants = new ArrayList<ReadableProductVariant>();

        if (category == null) {
            throw new ResourceNotFoundException("Category [" + categoryId + "] not found");
        }

        try {
            List<ProductAttribute> attributes = productAttributeService.getProductAttributesByCategoryLineage(store,
                    category.getLineage(), language);

            /**
             * Option NAME OptionValueName OptionValueName
             **/
            Map<String, List<ProductOptionValue>> rawFacet = new HashMap<String, List<ProductOptionValue>>();
            Map<String, ProductOption> references = new HashMap<String, ProductOption>();
            for (ProductAttribute attr : attributes) {
                references.put(attr.getProductOption().getCode(), attr.getProductOption());
                List<ProductOptionValue> values = rawFacet.get(attr.getProductOption().getCode());
                if (values == null) {
                    values = new ArrayList<ProductOptionValue>();
                    rawFacet.put(attr.getProductOption().getCode(), values);
                }

                if (attr.getProductOptionValue() != null) {
                    Optional<ProductOptionValueDescription> desc = attr.getProductOptionValue().getDescriptions()
                            .stream().filter(o -> o.getLanguage().getId() == language.getId()).findFirst();

                    ProductOptionValue val = new ProductOptionValue();
                    val.setCode(attr.getProductOption().getCode());
                    String order = attr.getAttributeSortOrder();
                    val.setSortOrder(order == null ? attr.getId().intValue() : Integer.parseInt(attr.getAttributeSortOrder()));
                    if (desc.isPresent()) {
                        val.setName(desc.get().getName());
                    } else {
                        val.setName(attr.getProductOption().getCode());
                    }
                    values.add(val);
                }
            }

            // for each reference set Option
            Iterator<Entry<String, ProductOption>> it = references.entrySet().iterator();
            while (it.hasNext()) {
                @SuppressWarnings("rawtypes")
                Map.Entry pair = it.next();
                ProductOption option = (ProductOption) pair.getValue();
                List<ProductOptionValue> values = rawFacet.get(option.getCode());

                ReadableProductVariant productVariant = new ReadableProductVariant();
                Optional<ProductOptionDescription> optionDescription = option.getDescriptions().stream().filter(o -> o.getLanguage().getId() == language.getId()).findFirst();
                if (optionDescription.isPresent()) {
                    productVariant.setName(optionDescription.get().getName());
                    productVariant.setId(optionDescription.get().getId());
                    productVariant.setCode(optionDescription.get().getProductOption().getCode());
                    List<ReadableProductVariantValue> optionValues = new ArrayList<ReadableProductVariantValue>();
                    for (ProductOptionValue value : values) {
                        ReadableProductVariantValue v = new ReadableProductVariantValue();
                        v.setCode(value.getCode());
                        v.setName(value.getName());
                        v.setDescription(value.getName());
                        v.setOption(option.getId());
                        v.setValue(value.getId());
                        v.setOrder(option.getProductOptionSortOrder());
                        optionValues.add(v);
                    }

                    Comparator<ReadableProductVariantValue> orderComparator
                            = Comparator.comparingInt(ReadableProductVariantValue::getOrder);

                    //Arrays.sort(employees, employeeSalaryComparator);

                    List<ReadableProductVariantValue> readableValues = optionValues.stream()
                            .sorted(orderComparator)
                            .collect(Collectors.toList());


                    //sort by name
                    // remove duplicates
                    readableValues = optionValues.stream().distinct().collect(Collectors.toList());
                    readableValues.sort(Comparator.comparing(ReadableProductVariantValue::getName));

                    productVariant.setOptions(readableValues);
                    variants.add(productVariant);
                }
            }


            return variants;
        } catch (Exception e) {
            throw new ServiceRuntimeException("An error occured while retrieving ProductAttributes", e);
        }
    }

    @Override
    public void move(Long child, Long parent, MerchantStore store) {

        Assert.notNull(child, "Child category must not be null");
        Assert.notNull(parent, "Parent category must not be null");
        Assert.notNull(store, "Merhant must not be null");


        try {

            Category c = categoryService.getById(child, store.getId());

            if (c == null) {
                throw new ResourceNotFoundException("Category with id [" + child + "] for store [" + store.getCode() + "]");
            }

            if (parent.longValue() == -1) {
                categoryService.addChild(null, c);
                return;

            }

            Category p = categoryService.getById(parent, store.getId());

            if (p == null) {
                throw new ResourceNotFoundException("Category with id [" + parent + "] for store [" + store.getCode() + "]");
            }

            if (c.getParent() != null && c.getParent().getId() == parent) {
                return;
            }

            if (c.getMerchantStore().getId().intValue() != store.getId().intValue()) {
                throw new OperationNotAllowedException(
                        "Invalid identifiers for Merchant [" + c.getMerchantStore().getCode() + "]");
            }

            if (p.getMerchantStore().getId().intValue() != store.getId().intValue()) {
                throw new OperationNotAllowedException(
                        "Invalid identifiers for Merchant [" + c.getMerchantStore().getCode() + "]");
            }

            p.getAuditSection().setModifiedBy("Api");
            categoryService.addChild(p, c);
        } catch (ResourceNotFoundException re) {
            throw re;
        } catch (OperationNotAllowedException oe) {
            throw oe;
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }

    }

    @Override
    public Category getByCode(String code, MerchantStore store) {
        try {
            return categoryService.getByCode(store, code);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Exception while reading category code [" + code + "]", e);
        }
    }

    @Override
    public void setVisible(PersistableCategory category, MerchantStore store) {
        Assert.notNull(category, "Category must not be null");
        Assert.notNull(store, "Store must not be null");
        try {
            Category c = this.getById(store, category.getId());
            c.setVisible(category.isVisible());
            categoryService.saveOrUpdate(c);
        } catch (Exception e) {
            throw new ServiceRuntimeException("Error while getting category [" + category.getId() + "]", e);
        }
    }

    @Override
    public ReadableCategoryList listByProduct(MerchantStore store, Long product, Language language) {
        Assert.notNull(product, "Product id must not be null");
        Assert.notNull(store, "Store must not be null");

        List<ReadableCategory> readableCategories = new ArrayList<ReadableCategory>();

        List<Category> categories = categoryService.getByProductId(product, store);

        readableCategories = categories.stream()
                .map(cat -> readableCategoryMapper.convert(cat, store, language))
                .collect(Collectors.toList());

        ReadableCategoryList readableList = new ReadableCategoryList();
        readableList.setCategories(readableCategories);
        readableList.setTotalPages(1);
        readableList.setNumber(readableCategories.size());
        readableList.setRecordsTotal(readableCategories.size());


        return readableList;
    }
}
