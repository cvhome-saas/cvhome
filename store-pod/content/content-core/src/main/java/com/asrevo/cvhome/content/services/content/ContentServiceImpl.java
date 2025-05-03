package com.asrevo.cvhome.content.services.content;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.content.Content;
import com.asrevo.cvhome.content.entity.content.ContentDescription;
import com.asrevo.cvhome.content.repositories.content.ContentRepository;
import com.asrevo.cvhome.content.repositories.content.PageContentRepository;
import com.asrevo.cvhome.store.core.entity.content.*;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.modules.cms.content.ContentAssetsManager;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service("contentService")
@Slf4j
public class ContentServiceImpl extends SalesManagerEntityServiceImpl<Long, Content>
        implements ContentService {

    private final ContentAssetsManager assetsManager;
    private final ContentRepository contentRepository;
    private final PageContentRepository pageContentRepository;

    @Autowired
    public ContentServiceImpl(
            ContentRepository contentRepository,
            ContentAssetsManager assetsManager,
            PageContentRepository pageContentRepository) {
        super(contentRepository);

        this.contentRepository = contentRepository;
        this.assetsManager = assetsManager;
        this.pageContentRepository = pageContentRepository;
    }

    @Override
    public List<Content> listByType(
            ContentType contentType, StoreMerchantId store, LanguageCode language) {

        return contentRepository.findByType(contentType, store, language);
    }

    @Override
    public void delete(Content content) throws ServiceException {

        Content c = this.getById(content.getId());
        super.delete(c);
    }

    @Override
    public Content getByLanguage(Long id, LanguageCode language) {
        return contentRepository.findByIdAndLanguage(id, language);
    }

    @Override
    public List<Content> listByType(
            List<ContentType> contentType, StoreMerchantId store, LanguageCode language) {

        /*
         * List<String> contentTypes = new ArrayList<String>(); for (int i = 0;
         * i < contentType.size(); i++) {
         * contentTypes.add(contentType.get(i).name()); }
         */

        return contentRepository.findByTypes(contentType, store, language);
    }

    @Override
    public List<ContentDescription> listNameByType(
            List<ContentType> contentType, StoreMerchantId store, LanguageCode language) {

        return contentRepository.listNameByType(contentType, store, language);
    }

    @Override
    public List<Content> listByType(List<ContentType> contentType, StoreMerchantId store) {

        return contentRepository.findByTypes(contentType, store);
    }

    @Override
    public Content getByCode(String code, StoreMerchantId store) {

        return contentRepository.findByCode(code, store);
    }

    @Override
    public Content getById(Long id) {
        return contentRepository.findOne(id);
    }

    @Override
    public void saveOrUpdate(final Content content) throws ServiceException {

        // save or update (persist and attach entities
        if (content.getId() != null && content.getId() > 0) {
            super.update(content);
        } else {
            super.save(content);
        }
    }

    @Override
    public Content getByCode(String code, StoreMerchantId store, LanguageCode language) {
        return contentRepository.findByCode(code, store, language);
    }

    /**
     * Method responsible for adding content file for given merchant store in
     * underlying Infinispan tree cache. It will take {@link InputContentFile}
     * and will store file for given merchant store according to its type. it
     * can save an image or any type of file (pdf, css, js ...)
     *
     * @param merchantStoreCode Merchant store
     * @param contentFile       {@link InputContentFile} being stored
     * @throws ServiceException service exception
     */
    @Override
    public void addContentFile(String merchantStoreCode, InputContentFile contentFile)
            throws ServiceException {
        Assert.notNull(merchantStoreCode, "Merchant store Id can not be null");
        Assert.notNull(contentFile, "InputContentFile image can not be null");
        Assert.notNull(contentFile.getFileName(), "InputContentFile.fileName can not be null");
        Assert.notNull(
                contentFile.getFileContentType(),
                "InputContentFile.fileContentType can not be null");

        String mimeType = URLConnection.guessContentTypeFromName(contentFile.getFileName());
        contentFile.setMimeType(mimeType);

        if (contentFile.getFileContentType().name().equals(FileContentType.IMAGE.name())
                || contentFile
                        .getFileContentType()
                        .name()
                        .equals(FileContentType.STATIC_FILE.name())) {
            addFile(merchantStoreCode, contentFile);
        } else if (contentFile
                .getFileContentType()
                .name()
                .equals(FileContentType.API_IMAGE.name())) {
            contentFile.setFileContentType(FileContentType.IMAGE);
            addImage(merchantStoreCode, contentFile);
        } else if (contentFile
                .getFileContentType()
                .name()
                .equals(FileContentType.API_FILE.name())) {
            contentFile.setFileContentType(FileContentType.STATIC_FILE);
            addFile(merchantStoreCode, contentFile);
        } else {
            addImage(merchantStoreCode, contentFile);
        }
    }

    @Override
    public void addLogo(String merchantStoreCode, InputContentFile cmsContentImage)
            throws ServiceException {

        Assert.notNull(merchantStoreCode, "Merchant store Id can not be null");
        Assert.notNull(cmsContentImage, "CMSContent image can not be null");

        cmsContentImage.setFileContentType(FileContentType.LOGO);
        addImage(merchantStoreCode, cmsContentImage);
    }

    @Override
    public void addBanner(String merchantStoreCode, InputContentFile cmsContentImage)
            throws ServiceException {

        Assert.notNull(merchantStoreCode, "Merchant store Id can not be null");
        Assert.notNull(cmsContentImage, "CMSContent image can not be null");

        cmsContentImage.setFileContentType(FileContentType.BANNER);
        addImage(merchantStoreCode, cmsContentImage);
    }

    @Override
    public void addOptionImage(String merchantStoreCode, InputContentFile cmsContentImage)
            throws ServiceException {

        Assert.notNull(merchantStoreCode, "Merchant store Id can not be null");
        Assert.notNull(cmsContentImage, "CMSContent image can not be null");
        cmsContentImage.setFileContentType(FileContentType.PROPERTY);
        addImage(merchantStoreCode, cmsContentImage);
    }

    private void addImage(String merchantStoreCode, InputContentFile contentImage)
            throws ServiceException {

        try {
            log.info("Adding content image for merchant id {}", merchantStoreCode);

            String p = contentImage.getPath();
            Optional<String> path = Optional.ofNullable(p);
            assetsManager.addFile(merchantStoreCode, path, contentImage);

        } catch (Exception e) {
            log.error("Error while trying to convert input stream to buffered image", e);
            throw new ServiceException(e);

        } finally {

            try {
                if (contentImage.getFile() != null) {
                    contentImage.getFile().close();
                }
            } catch (Exception ignore) {
            }
        }
    }

    private void addFile(final String merchantStoreCode, InputContentFile contentImage)
            throws ServiceException {

        try {
            log.info("Adding content file for merchant id {}", merchantStoreCode);
            // staticassetsManager.addFile(merchantStoreCode,
            // contentImage);

            Optional<String> path = Optional.empty();

            assetsManager.addFile(merchantStoreCode, path, contentImage);

        } catch (Exception e) {
            log.error("Error while trying to convert input stream to buffered image", e);
            throw new ServiceException(e);

        } finally {

            try {
                if (contentImage.getFile() != null) {
                    contentImage.getFile().close();
                }
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * Method responsible for adding list of content images for given merchant
     * store in underlying Infinispan tree cache. It will take list of
     *
     * @param merchantStoreCode Merchant store
     * @throws ServiceException service exception
     */
    @Override
    public void addContentFiles(String merchantStoreCode, List<InputContentFile> contentFilesList)
            throws ServiceException {

        Assert.notNull(merchantStoreCode, "Merchant store ID can not be null");
        Assert.notEmpty(contentFilesList, "File list can not be empty");
        log.info("Adding total {} images for given merchant", contentFilesList.size());

        Optional<String> path = Optional.empty();

        log.info("Adding content images for merchant....");
        assetsManager.addFiles(merchantStoreCode, path, contentFilesList);
        // staticassetsManager.addFiles(merchantStoreCode,
        // contentFilesList);

        try {
            for (InputContentFile file : contentFilesList) {
                if (file.getFile() != null) {
                    file.getFile().close();
                }
            }
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Method to remove given content image.Images are stored in underlying
     * system based on there name. Name will be used to search given image for
     * removal
     */
    @Override
    public void removeFile(
            String merchantStoreCode, FileContentType fileContentType, String fileName)
            throws ServiceException {
        Assert.notNull(merchantStoreCode, "Merchant Store Id can not be null");
        Assert.notNull(fileContentType, "Content file type can not be null");
        Assert.notNull(fileName, "Content Image type can not be null");

        Optional<String> path = Optional.empty();

        assetsManager.removeFile(merchantStoreCode, fileContentType, fileName, path);
    }

    @Override
    public void removeFile(String storeCode, String fileName) throws ServiceException {

        String fileType = "IMAGE";
        String mimetype = URLConnection.guessContentTypeFromName(fileName);
        String type = mimetype.split("/")[0];
        if (!type.equals("image")) fileType = "STATIC_FILE";

        Optional<String> path = Optional.empty();

        assetsManager.removeFile(storeCode, FileContentType.valueOf(fileType), fileName, path);
    }

    /**
     * Method to remove all images for a given merchant.It will take merchant
     * store as an input and will remove all images associated with given
     * merchant store.
     */
    @Override
    public void removeFiles(String merchantStoreCode) throws ServiceException {
        Assert.notNull(merchantStoreCode, "Merchant Store Id can not be null");

        Optional<String> path = Optional.empty();

        assetsManager.removeFiles(merchantStoreCode, path);
    }

    /**
     * Implementation for getContentImage method defined in
     * {@link ContentService} interface. Methods will return Content image with
     * given image name for the Merchant store or will return null if no image
     * with given name found for requested Merchant Store in Infinispan tree
     * cache.
     *
     * @return {@link OutputContentFile}
     */
    @Override
    public OutputContentFile getContentFile(
            String merchantStoreCode, FileContentType fileContentType, String fileName)
            throws ServiceException {
        Assert.notNull(merchantStoreCode, "Merchant store ID can not be null");
        Assert.notNull(fileName, "File name can not be null");

        Optional<String> path = Optional.empty();

        return assetsManager.getFile(merchantStoreCode, path, fileContentType, fileName);
    }

    /**
     * Implementation for getContentImages method defined in
     * {@link ContentService} interface. Methods will return list of all Content
     * image associated with given Merchant store or will return empty list if
     * no image is associated with given Merchant Store in Infinispan tree
     * cache.
     *
     * @return list of {@link List<OutputContentFile>}
     */
    @Override
    public List<OutputContentFile> getContentFiles(
            String merchantStoreCode, FileContentType fileContentType) throws ServiceException {
        Assert.notNull(merchantStoreCode, "Merchant store Id can not be null");
        // return staticassetsManager.getFiles(merchantStoreCode,
        // fileContentType);
        Optional<String> path = Optional.empty();
        return assetsManager.getFiles(merchantStoreCode, path, fileContentType);
    }

    /**
     * Returns the image names for a given merchant and store
     *
     * @return images name list
     */
    @Override
    public List<String> getContentFilesNames(
            String merchantStoreCode, FileContentType fileContentType) throws ServiceException {
        Assert.notNull(merchantStoreCode, "Merchant store Id can not be null");

        Optional<String> path = Optional.empty();

        return assetsManager.getFileNames(merchantStoreCode, path, fileContentType);

        /*
         * if(fileContentType.name().equals(FileContentType.IMAGE.name()) ||
         * fileContentType.name().equals(FileContentType.STATIC_FILE.name())) {
         * return assetsManager.getFileNames(merchantStoreCode,
         * fileContentType); } else { return
         * assetsManager.getFileNames(merchantStoreCode, fileContentType);
         * }
         */
    }

    @Override
    public ContentDescription getBySeUrl(StoreMerchantId store, String seUrl) {
        return contentRepository.getBySeUrl(store, seUrl);
    }

    @Override
    public List<Content> getByCodeLike(
            ContentType type, String codeLike, StoreMerchantId store, LanguageCode language) {
        return contentRepository.findByCodeLike(type, '%' + codeLike + '%', store, language);
    }

    @Override
    public Content getById(Long id, StoreMerchantId store, LanguageCode language) {

        Content content = contentRepository.findOne(id);

        if (content != null) {
            if (!Objects.equals(content.getStoreMerchantId(), store)) {
                return null;
            }
        }

        return content;
    }

    public Content getById(Long id, StoreMerchantId store) {

        Content content = contentRepository.findOne(id);

        if (content != null) {
            if (!Objects.equals(content.getStoreMerchantId(), store)) {
                return null;
            }
        }

        return content;
    }

    @Override
    public void addFolder(StoreMerchantId store, Optional<String> path, String folderName)
            throws ServiceException {
        Assert.notNull(store, "Store cannot be null");
        Assert.notNull(folderName, "Folder name cannot be null");

        if (path.isPresent()) {
            if (!this.isValidLinuxDirectory(path.get())) {
                throw new ServiceException(
                        "Path format [" + path.get() + "] not a valid directory format");
            }
        }
        assetsManager.addFolder(store.getId(), folderName, path);
    }

    @Override
    public List<String> listFolders(StoreMerchantId store, Optional<String> path) {
        Assert.notNull(store, "Store cannot be null");

        return assetsManager.listFolders(store.getId(), path);
    }

    @Override
    public void removeFolder(StoreMerchantId store, Optional<String> path, String folderName) {
        Assert.notNull(store, "Store cannot be null");
        Assert.notNull(folderName, "Folder name cannot be null");

        assetsManager.removeFolder(store.getId(), folderName, path);
    }

    public boolean isValidLinuxDirectory(String path) {
        Pattern linuxDirectoryPattern = Pattern.compile("^/|(/[a-zA-Z0-9_-]+)+$");
        return path != null
                && !path.trim().isEmpty()
                && linuxDirectoryPattern.matcher(path).matches();
    }

    @Override
    public void renameFile(
            String merchantStoreCode,
            FileContentType fileContentType,
            Optional<String> path,
            String originalName,
            String newName)
            throws ServiceException {

        OutputContentFile file =
                assetsManager.getFile(merchantStoreCode, path, fileContentType, originalName);

        if (file == null) {
            throw new ServiceException(
                    "File name ["
                            + originalName
                            + "] not found for merchant ["
                            + merchantStoreCode
                            + "]");
        }

        ByteArrayOutputStream os = file.getFile();
        InputStream is = new ByteArrayInputStream(os.toByteArray());

        // remove file
        assetsManager.removeFile(merchantStoreCode, fileContentType, originalName, path);

        // recreate file
        InputContentFile inputFile = new InputContentFile();
        inputFile.setFileContentType(fileContentType);
        inputFile.setFileName(newName);
        inputFile.setMimeType(file.getMimeType());
        inputFile.setFile(is);

        assetsManager.addFile(merchantStoreCode, path, inputFile);
    }

    @Override
    public Page<Content> listByType(
            ContentType contentType, StoreMerchantId store, Pageable pageable) {
        return pageContentRepository.findByContentType(contentType, store, pageable);
    }

    @Override
    public Page<Content> listByType(
            ContentType contentType,
            StoreMerchantId store,
            LanguageCode language,
            Pageable pageable) {
        return pageContentRepository.findByContentType(contentType, store, language, pageable);
    }

    @Override
    public boolean exists(String code, ContentType type, StoreMerchantId store) {
        Content c = contentRepository.findByCodeAndType(code, type, store);
        return c != null;
    }
}
