package com.asrevo.cvhome.merchant.content.service.facade.content;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.content.entity.content.Content;
import com.asrevo.cvhome.merchant.content.entity.content.ContentDescription;
import com.asrevo.cvhome.merchant.content.errors.ContentFileUnreadableException;
import com.asrevo.cvhome.merchant.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.merchant.content.errors.DuplicateContentCodeException;
import com.asrevo.cvhome.merchant.content.model.content.ContentFile;
import com.asrevo.cvhome.merchant.content.model.content.ContentFolder;
import com.asrevo.cvhome.merchant.content.model.content.ContentImage;
import com.asrevo.cvhome.merchant.content.model.content.box.PersistableContentBox;
import com.asrevo.cvhome.merchant.content.model.content.box.ReadableContentBox;
import com.asrevo.cvhome.merchant.content.model.content.box.ReadableContentBoxList;
import com.asrevo.cvhome.merchant.content.model.content.page.PersistableContentPage;
import com.asrevo.cvhome.merchant.content.model.content.page.ReadableContentPage;
import com.asrevo.cvhome.merchant.content.model.content.page.ReadableContentPageList;
import com.asrevo.cvhome.merchant.content.service.populator.content.ReadableContentBoxPopulator;
import com.asrevo.cvhome.merchant.content.service.populator.content.ReadableContentPagePopulator;
import com.asrevo.cvhome.merchant.content.services.content.ContentService;
import com.asrevo.cvhome.store.core.entity.content.ContentType;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.InputContentFile;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetDeleteFailedException;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetListFailedException;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetUploadFailedException;
import com.asrevo.cvhome.store.utils.ImageFilePath;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("contentFacade")
@Slf4j
@AllArgsConstructor
public class ContentFacadeImpl implements ContentFacade {

    public static final String FILE_CONTENT_DELIMITER = "/";

    private static final String PAGE = "Page";
    private static final String CONTENT_BOX = "Content box";

    private final ContentService contentService;

    private final ImageFilePath imageUtils;

    @Override
    public ContentFolder getContentFolder(String folder, StoreMerchantId store) throws AssetListFailedException {
        List<String> imageNames = Optional
                .ofNullable(contentService.getContentFilesNames(store.getId(), FileContentType.IMAGE))
                .orElseGet(List::of);

        // images from CMS
        List<ContentImage> contentImages = imageNames.stream()
                .map(name -> convertToContentImage(name, store))
                .toList();

        ContentFolder contentFolder = new ContentFolder();
        if (folder != null && !folder.trim().isEmpty()) {
            contentFolder.setPath(URLEncoder.encode(folder, StandardCharsets.UTF_8));
        }
        contentFolder.getContent().addAll(contentImages);
        return contentFolder;
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
        return new StringBuilder().append(imageUtils.getContextPath())
                .append(imageUtils.buildStaticImageUtils(store, file))
                .toString();
    }

    @Override
    public void delete(StoreMerchantId store, String fileName, String fileType) throws AssetDeleteFailedException {
        FileContentType t = FileContentType.valueOf(fileType);
        contentService.removeFile(store.getId(), t, fileName);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ReadableContentPageList getContentPages(StoreMerchantId store, LanguageCode language, Pageable pageable) {
        @SuppressWarnings("rawtypes")
        ReadableContentPageList items = new ReadableContentPageList();
        Page<Content> contentPages = contentService.listByType(ContentType.PAGE, store, language, pageable);

        items.setTotalPages(contentPages.getTotalPages());
        items.setSize(contentPages.getNumberOfElements());
        items.setTotalElements(contentPages.getTotalElements());
        items.setPageNumber(contentPages.getNumber());
        ReadableContentPagePopulator populator = new ReadableContentPagePopulator();
        List<ReadableContentPage> pages = contentPages.getContent().stream()
                .map(content -> populator.populate(content, store, language))
                .toList();

        items.setContent(pages);
        return items;
    }

    private Content convertContentPageToContent(StoreMerchantId store, Content model, PersistableContentPage content) {

        Content contentModel = new Content();
        if (model != null) {
            contentModel = model;
        }

        List<ContentDescription> descriptions = buildDescriptions(contentModel, content.getDescriptions());
        contentModel.setCode(content.getCode());
        contentModel.setContentType(ContentType.PAGE);
        contentModel.setStoreMerchantId(store);
        contentModel.setLinkToMenu(content.isLinkToMenu());
        contentModel.setVisible(content.isVisible());
        contentModel.setDescriptions(descriptions);
        contentModel.setId(content.getId());
        return contentModel;
    }

    private Content convertContentBoxToContent(StoreMerchantId store, Content model, PersistableContentBox content) {
        Content contentModel = new Content();
        if (model != null) {
            contentModel = model;
        }

        List<ContentDescription> descriptions = buildDescriptions(contentModel, content.getDescriptions());
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
            List<com.asrevo.cvhome.merchant.content.model.content.common.ContentDescription> persistableDescriptions) {
        List<ContentDescription> descriptions = new ArrayList<>();
        for (com.asrevo.cvhome.merchant.content.model.content.common.ContentDescription objectContent : persistableDescriptions) {
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
        }
        return descriptions;
    }

    private Content getContent(String code, StoreMerchantId store, LanguageCode language)
            throws ContentNotFoundException {
        Optional<Content> content;

        if (LanguageCode.isLanguage(language)) {
            content = Optional.ofNullable(contentService.getByCode(code, store, language));
        } else if (LanguageCode.isAllLanguage(language)) {
            content = Optional.ofNullable(contentService.getByCodeFetchAllLanguages(code, store));
        } else {
            content = Optional.ofNullable(contentService.getByCodeFetchNonLanguages(code, store));
        }
        return content.orElseThrow(() -> ContentNotFoundException.byCode(code, store));
    }

    @Override
    public ReadableContentPage getContentPage(String code, StoreMerchantId store, LanguageCode language)
            throws ContentNotFoundException {
        Content content = getContent(code, store, language);
        ReadableContentPagePopulator populator = new ReadableContentPagePopulator();
        return populator.populate(content, store, language);
    }

    @Override
    public ReadableContentBoxList getContentBoxes(StoreMerchantId store, LanguageCode language, Pageable pageable) {

        ReadableContentBoxList items = new ReadableContentBoxList();
        Page<Content> contentBoxes = contentService.listByType(ContentType.BOX, store, language, pageable);

        items.setTotalPages(contentBoxes.getTotalPages());
        items.setSize(contentBoxes.getNumberOfElements());
        items.setTotalElements(contentBoxes.getTotalElements());
        items.setPageNumber(contentBoxes.getNumber());
        ReadableContentBoxPopulator readableContentBoxPopulator = new ReadableContentBoxPopulator();
        List<ReadableContentBox> boxes = contentBoxes.getContent().stream()
                .map(content -> readableContentBoxPopulator.populate(content, store, language))
                .toList();
        items.setContent(boxes);

        return items;
    }

    @Override
    public void addContentFile(ContentFile file, String merchantStoreCode)
            throws ContentFileUnreadableException, AssetUploadFailedException {
        try {
            byte[] payload = file.getFile();
            String fileName = file.getName();

            try (InputStream targetStream = new ByteArrayInputStream(payload)) {

                String type = file.getContentType().split(FILE_CONTENT_DELIMITER)[0];
                FileContentType fileType = getFileContentType(type);

                InputContentFile cmsContent = new InputContentFile();
                cmsContent.setFileName(fileName);
                cmsContent.setMimeType(file.getContentType());
                cmsContent.setFile(targetStream);
                cmsContent.setFileContentType(fileType);

                contentService.addContentFile(merchantStoreCode, cmsContent);
            }
        } catch (IOException e) {
            throw ContentFileUnreadableException.of(file.getName(), e);
        }
    }

    private FileContentType getFileContentType(String type) {
        FileContentType fileType = FileContentType.STATIC_FILE;
        if ("image".equals(type)) { // for now we consider this route from api
            // only
            fileType = FileContentType.API_IMAGE;
        }
        return fileType;
    }

    @Override
    public ReadableContentBox getContentBox(String code, StoreMerchantId store, LanguageCode language)
            throws ContentNotFoundException {
        Content content = getContent(code, store, language);
        ReadableContentBoxPopulator populator = new ReadableContentBoxPopulator();
        return populator.populate(content, store, language);
    }

    @Override
    public Long saveContentPage(PersistableContentPage page, StoreMerchantId merchantStore, LanguageCode language)
            throws DuplicateContentCodeException {

        Content content = contentService.getByCodeFetchAllLanguages(page.getCode(), merchantStore);
        if (content != null) {
            // The catch (Exception) that used to wrap this block swallowed the duplicate check it contains, so the
            // 409 never left the facade and every outcome here looked like the same generic failure.
            throw DuplicateContentCodeException.of(PAGE, page.getCode(), merchantStore);
        }

        content = convertContentPageToContent(merchantStore, content, page);
        contentService.saveOrUpdate(content);
        return content.getId();
    }

    @Override
    public Long saveContentBox(PersistableContentBox box, StoreMerchantId merchantStore, LanguageCode language)
            throws DuplicateContentCodeException {

        Content content = contentService.getByCodeFetchAllLanguages(box.getCode(), merchantStore);
        if (content != null) {
            throw DuplicateContentCodeException.of(CONTENT_BOX, box.getCode(), merchantStore);
        }
        box.setId(null);
        content = convertContentBoxToContent(merchantStore, content, box);
        contentService.saveOrUpdate(content);
        return content.getId();
    }

    @Override
    public void delete(StoreMerchantId store, Long id) throws ContentNotFoundException {
        Content content = contentService.getById(id);
        if (content != null && !Objects.equals(content.getStoreMerchantId(), store)) {
            throw ContentNotFoundException.byId(id, store);
        }

        contentService.delete(content);
    }

    @Override
    public ReadableContentPage getContentPageByName(String name, StoreMerchantId store, LanguageCode language)
            throws ContentNotFoundException {
        Content content = contentService.findBySeUrl(store, name, language)
                .orElseThrow(() -> ContentNotFoundException.byName(name, store));

        ReadableContentPagePopulator populator = new ReadableContentPagePopulator();
        return populator.populate(content, store, language);
    }

    @Override
    public void updateContentPage(Long id, PersistableContentPage page, StoreMerchantId merchantStore,
                                  LanguageCode language) throws ContentNotFoundException {

        Content content = contentService.getById(id, merchantStore);
        if (content == null) {
            // Was a ConstraintException, i.e. a 409, for a row that simply is not there.
            throw ContentNotFoundException.byId(id, merchantStore);
        }

        page.setId(id);
        content = convertContentPageToContent(merchantStore, content, page);
        contentService.saveOrUpdate(content);
    }

    @Override
    public void updateContentBox(Long id, PersistableContentBox box, StoreMerchantId merchantStore,
                                 LanguageCode language) throws ContentNotFoundException {

        Content content = contentService.getById(id, merchantStore);
        if (content == null) {
            throw ContentNotFoundException.byId(id, merchantStore);
        }

        box.setId(id);
        content = convertContentBoxToContent(merchantStore, content, box);
        contentService.saveOrUpdate(content);
    }

    @Override
    public boolean codeExist(String code, String type, StoreMerchantId store) {
        return contentService.exists(code, ContentType.valueOf(type), store);
    }

}
