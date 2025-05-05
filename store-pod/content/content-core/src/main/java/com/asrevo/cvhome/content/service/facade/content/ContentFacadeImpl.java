package com.asrevo.cvhome.content.service.facade.content;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.content.Content;
import com.asrevo.cvhome.content.entity.content.ContentDescription;
import com.asrevo.cvhome.content.model.content.ContentFile;
import com.asrevo.cvhome.content.model.content.ContentFolder;
import com.asrevo.cvhome.content.model.content.ContentImage;
import com.asrevo.cvhome.content.model.content.box.PersistableContentBox;
import com.asrevo.cvhome.content.model.content.box.ReadableContentBox;
import com.asrevo.cvhome.content.model.content.box.ReadableContentBoxList;
import com.asrevo.cvhome.content.model.content.page.PersistableContentPage;
import com.asrevo.cvhome.content.model.content.page.ReadableContentPage;
import com.asrevo.cvhome.content.model.content.page.ReadableContentPageList;
import com.asrevo.cvhome.content.service.populator.content.ReadableContentBoxPopulator;
import com.asrevo.cvhome.content.service.populator.content.ReadableContentPagePopulator;
import com.asrevo.cvhome.content.services.content.ContentService;
import com.asrevo.cvhome.store.controller.exception.ConstraintException;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.entity.content.ContentType;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.InputContentFile;
import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

@Component("contentFacade")
@Slf4j
@AllArgsConstructor
public class ContentFacadeImpl implements ContentFacade {

    public static final String FILE_CONTENT_DELIMETER = "/";
    private final ContentService contentService;

    private final ImageFilePath imageUtils;

    @Override
    public ContentFolder getContentFolder(String folder, StoreMerchantId store) {
        try {
            List<String> imageNames =
                    Optional.ofNullable(
                                    contentService.getContentFilesNames(
                                            store.getId(), FileContentType.IMAGE))
                            .orElseGet(List::of);

            // images from CMS
            List<ContentImage> contentImages =
                    imageNames.stream().map(name -> convertToContentImage(name, store)).toList();

            ContentFolder contentFolder = new ContentFolder();
            if (folder != null && !folder.trim().isEmpty()) {
                contentFolder.setPath(URLEncoder.encode(folder, StandardCharsets.UTF_8));
            }
            contentFolder.getContent().addAll(contentImages);
            return contentFolder;

        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Error while getting folder " + e.getMessage(), e);
        }
    }

    private ContentImage convertToContentImage(String name, StoreMerchantId store) {
        String path = absolutePath(store, null);
        ContentImage contentImage = new ContentImage();
        contentImage.setName(name);
        contentImage.setPath(path);
        return contentImage;
    }

    @Override
    public String absolutePath(StoreMerchantId store, String file) {
        return new StringBuilder()
                .append(imageUtils.getContextPath())
                .append(imageUtils.buildStaticImageUtils(store, file))
                .toString();
    }

    @Override
    public void delete(StoreMerchantId store, String fileName, String fileType) {
        Assert.notNull(store, "StoreMerchantId cannot be null");
        Assert.notNull(fileName, "File name cannot be null");
        try {
            FileContentType t = FileContentType.valueOf(fileType);
            contentService.removeFile(store.getId(), t, fileName);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public ReadableContentPageList getContentPages(
            StoreMerchantId store, LanguageCode language, Pageable pageable) {
        Assert.notNull(store, "store cannot be null");

        @SuppressWarnings("rawtypes")
        ReadableContentPageList items = new ReadableContentPageList();
        Page<Content> contentPages =
                contentService.listByType(ContentType.PAGE, store, language, pageable);

        items.setTotalPages(contentPages.getTotalPages());
        items.setSize(contentPages.getNumberOfElements());
        items.setTotalElements(contentPages.getTotalElements());
        ReadableContentPagePopulator populator = new ReadableContentPagePopulator();
        List<ReadableContentPage> pages =
                contentPages.getContent().stream()
                        .map(
                                content -> {
                                    try {
                                        return populator.populate(content, store, language);
                                    } catch (ConversionException e) {
                                        return null;
                                    }
                                })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

        items.setContent(pages);
        return items;
    }

    private Content convertContentPageToContent(
            StoreMerchantId store, Content model, PersistableContentPage content) {

        Content contentModel = new Content();
        if (model != null) {
            contentModel = model;
        }

        List<ContentDescription> descriptions =
                buildDescriptions(contentModel, content.getDescriptions());
        contentModel.setCode(content.getCode());
        contentModel.setContentType(ContentType.PAGE);
        contentModel.setStoreMerchantId(store);
        contentModel.setLinkToMenu(content.isLinkToMenu());
        contentModel.setVisible(content.isVisible());
        contentModel.setDescriptions(descriptions);
        contentModel.setId(content.getId());
        return contentModel;
    }

    private Content convertContentBoxToContent(
            StoreMerchantId store, Content model, PersistableContentBox content) {
        Content contentModel = new Content();
        if (model != null) {
            contentModel = model;
        }

        List<ContentDescription> descriptions =
                buildDescriptions(contentModel, content.getDescriptions());
        for (ContentDescription cd : descriptions) {
            cd.setContent(contentModel);
        }

        contentModel.setCode(content.getCode());
        contentModel.setContentType(ContentType.BOX);
        contentModel.setStoreMerchantId(store);
        contentModel.setVisible(content.isVisible());
        contentModel.setDescriptions(descriptions);
        contentModel.setId(content.getId());
        return contentModel;
    }

    private List<ContentDescription> buildDescriptions(
            Content contentModel,
            List<com.asrevo.cvhome.content.model.content.common.ContentDescription>
                    persistableDescriptions) {
        List<ContentDescription> descriptions = new ArrayList<>();
        for (com.asrevo.cvhome.content.model.content.common.ContentDescription objectContent :
                persistableDescriptions) {
            ContentDescription contentDescription = null;
            if (!CollectionUtils.isEmpty(contentModel.getDescriptions())) {
                for (ContentDescription descriptionModel : contentModel.getDescriptions()) {
                    if (descriptionModel.getLanguageCode().equals(objectContent.getLanguage())) {
                        contentDescription = descriptionModel;
                        break;
                    }
                }
            }

            if (contentDescription == null) {
                contentDescription = new ContentDescription();
            }

            contentDescription.setMetatagDescription(objectContent.getMetaDescription());
            contentDescription.setTitle(objectContent.getTitle());
            contentDescription.setName(objectContent.getName());
            contentDescription.setSeUrl(objectContent.getFriendlyUrl());
            contentDescription.setDescription(objectContent.getDescription());
            contentDescription.setMetatagTitle(objectContent.getTitle());
            contentDescription.setContent(contentModel);
            contentDescription.setLanguageCode(objectContent.getLanguage());
            descriptions.add(contentDescription);
            // contentDescription.setId(objectContent.getId());
        }
        return descriptions;
    }

    private Content getContent(String code, StoreMerchantId store, LanguageCode language) {
        Assert.notNull(code, "Content code cannot be null");
        Assert.notNull(store, "StoreMerchantId cannot be null");

        Content content;

        if (LanguageCode.isLanguage(language)) {
            content =
                    Optional.ofNullable(contentService.getByCode(code, store, language))
                            .orElseThrow(
                                    () -> new ResourceNotFoundException("No page found : " + code));
        } else if (LanguageCode.isAllLanguage(language)) {
            content =
                    Optional.ofNullable(contentService.getByCodeFetchAllLanguages(code, store))
                            .orElseThrow(
                                    () -> new ResourceNotFoundException("No page found : " + code));
        } else {
            content =
                    Optional.ofNullable(contentService.getByCodeFetchNonLanguages(code, store))
                            .orElseThrow(
                                    () -> new ResourceNotFoundException("No page found : " + code));
        }
        return content;
    }

    @SneakyThrows
    @Override
    public ReadableContentPage getContentPage(
            String code, StoreMerchantId store, LanguageCode language) {
        Content content = getContent(code, store, language);
        ReadableContentPagePopulator populator = new ReadableContentPagePopulator();
        return populator.populate(content, store, language);
    }

    @Override
    public ReadableContentBoxList getContentBoxes(
            StoreMerchantId store, LanguageCode language, Pageable pageable) {

        Assert.notNull(store, "store cannot be null");

        ReadableContentBoxList items = new ReadableContentBoxList();
        Page<Content> contentBoxes =
                contentService.listByType(ContentType.BOX, store, language, pageable);

        items.setTotalPages(contentBoxes.getTotalPages());
        items.setSize(contentBoxes.getNumberOfElements());
        items.setTotalElements(contentBoxes.getTotalElements());
        ReadableContentBoxPopulator readableContentBoxPopulator = new ReadableContentBoxPopulator();
        List<ReadableContentBox> boxes =
                contentBoxes.getContent().stream()
                        .map(
                                content -> {
                                    try {
                                        return readableContentBoxPopulator.populate(
                                                content, store, language);
                                    } catch (ConversionException e) {
                                        return null;
                                    }
                                })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
        items.setContent(boxes);

        return items;
    }

    @Override
    public void addContentFile(ContentFile file, String merchantStoreCode) {
        try {
            byte[] payload = file.getFile();
            String fileName = file.getName();

            try (InputStream targetStream = new ByteArrayInputStream(payload)) {

                String type = file.getContentType().split(FILE_CONTENT_DELIMETER)[0];
                FileContentType fileType = getFileContentType(type);

                InputContentFile cmsContent = new InputContentFile();
                cmsContent.setFileName(fileName);
                cmsContent.setMimeType(file.getContentType());
                cmsContent.setFile(targetStream);
                cmsContent.setFileContentType(fileType);

                contentService.addContentFile(merchantStoreCode, cmsContent);
            }
        } catch (ServiceException | IOException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    private FileContentType getFileContentType(String type) {
        FileContentType fileType = FileContentType.STATIC_FILE;
        if (type.equals("image")) { // for now we consider this route from api
            // only
            fileType = FileContentType.API_IMAGE;
        }
        return fileType;
    }

    @SneakyThrows
    @Override
    public ReadableContentBox getContentBox(
            String code, StoreMerchantId store, LanguageCode language) {
        Assert.notNull(code, "Content code cannot be null");
        Assert.notNull(store, "StoreMerchantId cannot be null");

        Content content = getContent(code, store, language);
        ReadableContentBoxPopulator populator = new ReadableContentBoxPopulator();
        return populator.populate(content, store, language);
    }

    @Override
    public Long saveContentPage(
            PersistableContentPage page, StoreMerchantId merchantStore, LanguageCode language) {
        Assert.notNull(page, "page can't be null");
        Assert.notNull(page.getCode(), "Content code must not be null");
        Assert.notNull(merchantStore, "store can't be null");

        try {
            Content content;

            content = contentService.getByCodeFetchAllLanguages(page.getCode(), merchantStore);
            if (content != null) {
                throw new ConstraintException(
                        "Page with code ["
                                + page.getCode()
                                + "] already exist for store ["
                                + merchantStore
                                + "]");
            }

            content = convertContentPageToContent(merchantStore, content, page);
            contentService.saveOrUpdate(content);
            return content.getId();
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    @Override
    public Long saveContentBox(
            PersistableContentBox box, StoreMerchantId merchantStore, LanguageCode language) {
        Assert.notNull(box, "box can't be null");
        Assert.notNull(box.getCode(), "Content box must not be null");
        Assert.notNull(merchantStore, "store can't be null");

        try {
            Content content;

            content = contentService.getByCodeFetchAllLanguages(box.getCode(), merchantStore);
            if (content != null) {
                throw new ConstraintException(
                        "Content box with code ["
                                + box.getCode()
                                + "] already exist for store ["
                                + merchantStore
                                + "]");
            }
            box.setId(null);
            content = convertContentBoxToContent(merchantStore, content, box);
            contentService.saveOrUpdate(content);
            return content.getId();
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    @Override
    public void delete(StoreMerchantId store, Long id) {
        Assert.notNull(store, "StoreMerchantId not null");
        Assert.notNull(id, "Content id must not be null");
        // select content first
        Content content = contentService.getById(id);
        if (content != null) {
            if (!Objects.equals(content.getStoreMerchantId(), store)) {
                throw new ResourceNotFoundException(
                        "No content found with id [" + id + "] for store [" + store + "]");
            }
        }

        try {
            contentService.delete(content);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(
                    "Exception while deleting content " + e.getMessage(), e);
        }
    }

    @SneakyThrows
    @Override
    public ReadableContentPage getContentPageByName(
            String name, StoreMerchantId store, LanguageCode language) {
        Assert.notNull(name, "Content name cannot be null");
        Assert.notNull(store, "StoreMerchantId cannot be null");
        Assert.notNull(language, "LanguageCode cannot be null");

        Content content =
                contentService
                        .findBySeUrl(store, name, language)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("No page found : " + name));

        ReadableContentPagePopulator populator = new ReadableContentPagePopulator();
        return populator.populate(content, store, language);
    }

    @Override
    public void updateContentPage(
            Long id,
            PersistableContentPage page,
            StoreMerchantId merchantStore,
            LanguageCode language) {
        Assert.notNull(page, "page can't be null");
        Assert.notNull(id, "Content id must not be null");
        Assert.notNull(merchantStore, "store can't be null");

        try {
            Content content;

            content = contentService.getById(id, merchantStore);
            if (content == null) {
                throw new ConstraintException(
                        "Page with id ["
                                + id
                                + "] does not exist for store ["
                                + merchantStore
                                + "]");
            }

            page.setId(id);
            content = convertContentPageToContent(merchantStore, content, page);
            contentService.saveOrUpdate(content);

        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    @Override
    public void updateContentBox(
            Long id,
            PersistableContentBox box,
            StoreMerchantId merchantStore,
            LanguageCode language) {
        Assert.notNull(box, "bix can't be null");
        Assert.notNull(id, "Content id must not be null");
        Assert.notNull(merchantStore, "store can't be null");

        try {
            Content content;

            content = contentService.getById(id, merchantStore);
            if (content == null) {
                throw new ConstraintException(
                        "Page with id ["
                                + id
                                + "] does not exist for store ["
                                + merchantStore
                                + "]");
            }

            box.setId(id);
            content = convertContentBoxToContent(merchantStore, content, box);
            contentService.saveOrUpdate(content);

        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    @Override
    public boolean codeExist(String code, String type, StoreMerchantId store) {
        return contentService.exists(code, ContentType.valueOf(type), store);
    }
}
