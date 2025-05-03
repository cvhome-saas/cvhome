package com.asrevo.cvhome.content.service.facade.content;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.content.Content;
import com.asrevo.cvhome.content.entity.content.ContentDescription;
import com.asrevo.cvhome.content.model.content.*;
import com.asrevo.cvhome.content.model.content.box.PersistableContentBox;
import com.asrevo.cvhome.content.model.content.box.ReadableContentBox;
import com.asrevo.cvhome.content.model.content.box.ReadableContentBoxFull;
import com.asrevo.cvhome.content.model.content.page.PersistableContentPage;
import com.asrevo.cvhome.content.model.content.page.ReadableContentPage;
import com.asrevo.cvhome.content.model.content.page.ReadableContentPageFull;
import com.asrevo.cvhome.content.services.content.ContentService;
import com.asrevo.cvhome.store.controller.exception.ConstraintException;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.entity.content.ContentType;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.InputContentFile;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;
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
    public ReadableEntityList<ReadableContentPage> getContentPages(
            StoreMerchantId store, LanguageCode language, Pageable pageable) {
        Assert.notNull(store, "store cannot be null");

        @SuppressWarnings("rawtypes")
        ReadableEntityList items = new ReadableEntityList();
        Page<Content> contentPages;
        contentPages = contentService.listByType(ContentType.PAGE, store, pageable);

        items.setTotalPages(contentPages.getTotalPages());
        items.setSize(contentPages.getContent().size());
        items.setTotalElements(contentPages.getTotalElements());

        List<ReadableContentPage> pages =
                contentPages.getContent().stream()
                        .map(
                                content ->
                                        convertContentToReadableContentPage(
                                                store, language, content))
                        .collect(Collectors.toList());

        items.setContent(pages);
        return items;
    }

    @Deprecated
    private ReadableContentFull convertContentToReadableContentFull(
            StoreMerchantId store, LanguageCode language, Content content) {
        ReadableContentFull contentFull = new ReadableContentFull();

        List<ContentDescriptionEntity> descriptions =
                this.createContentDescriptionEntitys(store, content, language);

        contentFull.setDescriptions(descriptions);
        contentFull.setId(content.getId());
        contentFull.setDisplayedInMenu(content.isLinkToMenu());
        contentFull.setContentType(content.getContentType().name());
        contentFull.setCode(content.getCode());
        contentFull.setId(content.getId());
        contentFull.setVisible(content.isVisible());

        return contentFull;
    }

    private Content convertContentPageToContent(
            StoreMerchantId store, Content model, PersistableContentPage content) throws Exception {

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
            StoreMerchantId store, Content model, PersistableContentBox content) throws Exception {
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

    /*
     * private Content convertContentPageToContent(StoreMerchantId store, Language
     * language, Content content, PersistableContentEntity contentPage) throws
     * ServiceException {
     *
     * ContentType contentType =
     * ContentType.valueOf(contentPage.getContentType()); if (contentType ==
     * null) { throw new
     * ServiceRuntimeException("Invalid specified contentType [" +
     * contentPage.getContentType() + "]"); }
     *
     * List<ContentDescription> descriptions = createContentDescription(store,
     * content, contentPage); descriptions.stream().forEach(c ->
     * c.setContent(content));
     *
     * content.setDescriptions(descriptions);
     *
     * // ContentDescription contentDescription = //
     * createContentDescription(store, contentPage, language); //
     * setContentDescriptionToContentModel(content,contentDescription,language);
     *
     * // contentDescription.setContent(content);
     *
     * if (contentPage.getId() != null && contentPage.getId().longValue() > 0) {
     * content.setId(contentPage.getId()); }
     * content.setVisible(contentPage.isVisible());
     * content.setLinkToMenu(contentPage.isDisplayedInMenu());
     * content.setContentType(ContentType.valueOf(contentPage.getContentType()))
     * ; content.setStoreMerchantId(store);
     *
     * return content; }
     */

    @Deprecated
    private List<ContentDescriptionEntity> createContentDescriptionEntitys(
            StoreMerchantId store, Content contentModel, LanguageCode language) {

        List<ContentDescriptionEntity> descriptions = new ArrayList<>();

        if (!CollectionUtils.isEmpty(contentModel.getDescriptions())) {
            for (ContentDescription description : contentModel.getDescriptions()) {
                if (language != null && !language.equals(description.getLanguageCode())) {
                    continue;
                }

                ContentDescriptionEntity contentDescription = create(description);
                descriptions.add(contentDescription);
            }
        }

        return descriptions;
    }

    @Deprecated
    private ContentDescriptionEntity create(ContentDescription description) {

        ContentDescriptionEntity contentDescription = new ContentDescriptionEntity();
        contentDescription.setLanguage(description.getLanguageCode());
        contentDescription.setTitle(description.getTitle());
        contentDescription.setName(description.getName());
        contentDescription.setFriendlyUrl(description.getSeUrl());
        contentDescription.setDescription(description.getDescription());
        if (description.getId() != null && description.getId() > 0) {
            contentDescription.setId(description.getId());
        }

        return contentDescription;
    }

    /*
     * private List<ContentDescription> createContentDescription(
     * PersistableContentPage content) throws ServiceException {
     * Assert.notNull(contentModel, "Content cannot be null");
     *
     * List<ContentDescription> descriptions = new
     * ArrayList<ContentDescription>(); for (NamedEntity objectContent :
     * content.getDescriptions()) { LanguageCode lang =
     * languageService.getByCode(objectContent.getLanguage());
     * ContentDescription contentDescription = new ContentDescription(); if
     * (contentModel != null) {
     * setContentDescriptionToContentModel(contentModel, contentDescription,
     * lang); } contentDescription.setLanguage(lang);
     * contentDescription.setMetatagDescription(objectContent.getMetaDescription
     * ()); contentDescription.setTitle(objectContent.getTitle());
     * contentDescription.setName(objectContent.getName());
     * contentDescription.setSeUrl(objectContent.getFriendlyUrl());
     * contentDescription.setDescription(objectContent.getDescription());
     * contentDescription.setMetatagTitle(objectContent.getTitle());
     * descriptions.add(contentDescription); } return descriptions; }
     */
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

            // if (contentModel != null) {
            //	setContentDescriptionToContentModel(contentModel, contentDescription, lang);
            // }
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

    @Override
    public ReadableContentPage getContentPage(
            String code, StoreMerchantId store, LanguageCode language) {

        Assert.notNull(code, "Content code cannot be null");
        Assert.notNull(store, "StoreMerchantId cannot be null");

        Content content;

        if (language == null) {
            content =
                    Optional.ofNullable(contentService.getByCode(code, store))
                            .orElseThrow(
                                    () -> new ResourceNotFoundException("No page found : " + code));
        } else {
            content =
                    Optional.ofNullable(contentService.getByCode(code, store, language))
                            .orElseThrow(
                                    () -> new ResourceNotFoundException("No page found : " + code));
        }

        return convertContentToReadableContentPage(store, language, content);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public ReadableEntityList<ReadableContentBox> getContentBoxes(
            ContentType type, StoreMerchantId store, LanguageCode language, Pageable pageable) {

        Assert.notNull(store, "store cannot be null");

        ReadableEntityList items = new ReadableEntityList();
        Page<Content> contentBoxes;
        contentBoxes = contentService.listByType(type, store, pageable);

        items.setTotalPages(contentBoxes.getTotalPages());
        items.setSize(contentBoxes.getContent().size());
        items.setTotalElements(contentBoxes.getTotalElements());

        List<ReadableContentBox> boxes =
                contentBoxes.getContent().stream()
                        .map(content -> convertContentToReadableContentBox(language, content))
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

    private ReadableContentBox convertContentToReadableContentBox(
            LanguageCode language, Content content) {
        ReadableContentBox box = new ReadableContentBox();
        this.setDescription(content, box, language);
        box.setCode(content.getCode());
        box.setId(content.getId());
        box.setVisible(content.isVisible());
        return box;
        // TODO revise this
        // String staticImageFilePath = imageUtils.buildStaticImageUtils(store,
        // content.getCode() + ".jpg");
        // box.setImage(staticImageFilePath);
    }

    private void setDescription(Content content, ReadableContentBox box, LanguageCode lang) {

        Optional<ContentDescription> contentDescription =
                findAppropriateContentDescription(content.getDescriptions(), lang);
        if (contentDescription.isPresent()) {
            com.asrevo.cvhome.content.model.content.common.ContentDescription desc =
                    this.contentDescription(contentDescription.get());
            box.setDescription(desc);
        }
    }

    private ReadableContentPage convertContentToReadableContentPage(
            StoreMerchantId store, LanguageCode language, Content content) {
        if (language != null) {
            ReadableContentPage page = new ReadableContentPage();
            Optional<ContentDescription> contentDescription =
                    findAppropriateContentDescription(content.getDescriptions(), language);
            if (contentDescription.isPresent()) {
                com.asrevo.cvhome.content.model.content.common.ContentDescription desc =
                        this.contentDescription(contentDescription.get());
                page.setDescription(desc);
            }
            page.setCode(content.getCode());
            page.setId(content.getId());
            page.setVisible(content.isVisible());
            page.setLinkToMenu(content.isLinkToMenu());
            return page;
        } else {
            ReadableContentPageFull page = new ReadableContentPageFull();
            List<com.asrevo.cvhome.content.model.content.common.ContentDescription> descriptions =
                    content.getDescriptions().stream()
                            .map(this::contentDescription)
                            .collect(Collectors.toList());
            page.setDescriptions(descriptions);
            page.setCode(content.getCode());
            page.setId(content.getId());
            page.setVisible(content.isVisible());
            page.setLinkToMenu(content.isLinkToMenu());
            return page;
        }
    }

    private com.asrevo.cvhome.content.model.content.common.ContentDescription contentDescription(
            ContentDescription description) {
        Assert.notNull(description, "ContentDescription cannot be null");
        com.asrevo.cvhome.content.model.content.common.ContentDescription desc =
                new com.asrevo.cvhome.content.model.content.common.ContentDescription();
        desc.setDescription(description.getDescription()); // return description as is
        desc.setName(description.getName());
        desc.setTitle(description.getTitle());
        desc.setFriendlyUrl(description.getSeUrl());
        desc.setId(description.getId());
        desc.setLanguage(description.getLanguageCode());
        return desc;
    }

    private Optional<ContentDescription> findAppropriateContentDescription(
            List<ContentDescription> contentDescriptions, LanguageCode language) {
        return contentDescriptions.stream()
                .filter(description -> description.getLanguageCode().equals(language))
                .findFirst();
    }

    @Override
    public ReadableContentBox getContentBox(
            String code, StoreMerchantId store, LanguageCode language) {
        Assert.notNull(code, "Content code cannot be null");
        Assert.notNull(store, "StoreMerchantId cannot be null");

        Content content;

        if (language != null) {

            content =
                    Optional.ofNullable(contentService.getByCode(code, store, language))
                            .orElseThrow(
                                    () ->
                                            new ResourceNotFoundException(
                                                    "Resource not found ["
                                                            + code
                                                            + "] for store ["
                                                            + store
                                                            + "]"));

            Optional<ContentDescription> contentDescription =
                    findAppropriateContentDescription(content.getDescriptions(), language);
            ReadableContentBox box = new ReadableContentBox();
            box.setId(content.getId());
            box.setCode(content.getCode());
            box.setContentType(content.getContentType().name());
            box.setVisible(content.isVisible());

            if (contentDescription.isPresent()) {
                com.asrevo.cvhome.content.model.content.common.ContentDescription desc =
                        this.contentDescription(
                                contentDescription.get()); // return cdata description
                desc.setDescription(this.fixContentDescription(desc.getDescription()));
                box.setDescription(desc);
            }

            return box;

        } else {

            content =
                    Optional.ofNullable(contentService.getByCode(code, store))
                            .orElseThrow(
                                    () ->
                                            new ResourceNotFoundException(
                                                    "Resource not found ["
                                                            + code
                                                            + "] for store ["
                                                            + store
                                                            + "]"));

            ReadableContentBoxFull full = new ReadableContentBoxFull(); // all languages

            List<com.asrevo.cvhome.content.model.content.common.ContentDescription> descriptions =
                    content.getDescriptions().stream()
                            .map(this::contentDescription)
                            .collect(Collectors.toList());

            full.setDescriptions(descriptions);
            full.setCode(content.getCode());
            full.setId(content.getId());
            full.setVisible(content.isVisible());

            return full;
        }
    }

    private String fixContentDescription(String description) {
        Assert.notNull(description, "description cannot be empty");
        //        return "<![CDATA[" + description.replaceAll("\r\n", "").replaceAll("\t", "") +
        // "]]>";
        return description;
    }

    @Override
    public Long saveContentPage(
            PersistableContentPage page, StoreMerchantId merchantStore, LanguageCode language) {
        Assert.notNull(page, "page can't be null");
        Assert.notNull(page.getCode(), "Content code must not be null");
        Assert.notNull(merchantStore, "store can't be null");

        try {
            Content content;

            content = contentService.getByCode(page.getCode(), merchantStore);
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

            content = contentService.getByCode(box.getCode(), merchantStore);
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

    @Override
    public ReadableContentFull getContent(
            String code, StoreMerchantId store, LanguageCode language) {
        Assert.notNull(store, "StoreMerchantId not null");
        Assert.notNull(code, "Content code must not be null");

        Content content = contentService.getByCode(code, store);
        if (content == null) {
            throw new ResourceNotFoundException(
                    "No content found with code [" + code + "] for store [" + store + "]");
        }

        return this.convertContentToReadableContentFull(store, language, content);
    }

    @Override
    public ReadableContentPage getContentPageByName(
            String name, StoreMerchantId store, LanguageCode language) {
        Assert.notNull(name, "Content name cannot be null");
        Assert.notNull(store, "StoreMerchantId cannot be null");
        Assert.notNull(language, "LanguageCode cannot be null");

        try {

            ContentDescription contentDescription =
                    Optional.ofNullable(contentService.getBySeUrl(store, name))
                            .orElseThrow(
                                    () -> new ResourceNotFoundException("No page found : " + name));

            return convertContentToReadableContentPage(
                    store, language, contentDescription.getContent());

        } catch (Exception e) {
            throw new ServiceRuntimeException("Error while getting page " + e.getMessage(), e);
        }
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
