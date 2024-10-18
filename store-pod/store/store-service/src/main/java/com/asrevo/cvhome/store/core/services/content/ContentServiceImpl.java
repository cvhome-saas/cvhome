package com.asrevo.cvhome.store.core.services.content;

import com.asrevo.cvhome.store.core.entity.content.*;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.modules.cms.content.ContentAssetsManager;
import com.asrevo.cvhome.store.core.repositories.content.ContentRepository;
import com.asrevo.cvhome.store.core.repositories.content.PageContentRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    public List<Content> listByType(ContentType contentType, MerchantStore store, Language language)
            throws ServiceException {

        return contentRepository.findByType(contentType, store.getId(), language.getId());
    }

    @Override
    public void delete(Content content) throws ServiceException {

        Content c = this.getById(content.getId());
        super.delete(c);
    }

    @Override
    public Content getByLanguage(Long id, Language language) throws ServiceException {
        return contentRepository.findByIdAndLanguage(id, language.getId());
    }

    @Override
    public List<Content> listByType(
            List<ContentType> contentType, MerchantStore store, Language language)
            throws ServiceException {

        /*
         * List<String> contentTypes = new ArrayList<String>(); for (int i = 0;
         * i < contentType.size(); i++) {
         * contentTypes.add(contentType.get(i).name()); }
         */

        return contentRepository.findByTypes(contentType, store.getId(), language.getId());
    }

    @Override
    public List<ContentDescription> listNameByType(
            List<ContentType> contentType, MerchantStore store, Language language)
            throws ServiceException {

        return contentRepository.listNameByType(contentType, store, language);
    }

    @Override
    public List<Content> listByType(List<ContentType> contentType, MerchantStore store)
            throws ServiceException {

        return contentRepository.findByTypes(contentType, store.getId());
    }

    @Override
    public Content getByCode(String code, MerchantStore store) throws ServiceException {

        return contentRepository.findByCode(code, store.getId());
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
    public Content getByCode(String code, MerchantStore store, Language language)
            throws ServiceException {
        return contentRepository.findByCode(code, store.getId(), language.getId());
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

            String p = null;
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

    /**
     * Method responsible for adding list of content images for given merchant
     * store in underlying Infinispan tree cache. It will take list of
     * {@link CMSContentImage} and will store them for given merchant store.
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

        String p = null;
        Optional<String> path = Optional.ofNullable(p);

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
     *
     */
    @Override
    public void removeFile(
            String merchantStoreCode, FileContentType fileContentType, String fileName)
            throws ServiceException {
        Assert.notNull(merchantStoreCode, "Merchant Store Id can not be null");
        Assert.notNull(fileContentType, "Content file type can not be null");
        Assert.notNull(fileName, "Content Image type can not be null");

        String p = null;
        Optional<String> path = Optional.ofNullable(p);

        assetsManager.removeFile(merchantStoreCode, fileContentType, fileName, path);
    }

    @Override
    public void removeFile(String storeCode, String fileName) throws ServiceException {

        String fileType = "IMAGE";
        String mimetype = URLConnection.guessContentTypeFromName(fileName);
        String type = mimetype.split("/")[0];
        if (!type.equals("image")) fileType = "STATIC_FILE";

        String p = null;
        Optional<String> path = Optional.ofNullable(p);

        assetsManager.removeFile(storeCode, FileContentType.valueOf(fileType), fileName, path);
    }

    /**
     * Method to remove all images for a given merchant.It will take merchant
     * store as an input and will remove all images associated with given
     * merchant store.
     *
     */
    @Override
    public void removeFiles(String merchantStoreCode) throws ServiceException {
        Assert.notNull(merchantStoreCode, "Merchant Store Id can not be null");

        String p = null;
        Optional<String> path = Optional.ofNullable(p);

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

        String p = null;
        Optional<String> path = Optional.ofNullable(p);

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
        String p = null;
        Optional<String> path = Optional.ofNullable(p);
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

        String p = null;
        Optional<String> path = Optional.ofNullable(p);

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
    public ContentDescription getBySeUrl(MerchantStore store, String seUrl) {
        return contentRepository.getBySeUrl(store, seUrl);
    }

    @Override
    public List<Content> getByCodeLike(
            ContentType type, String codeLike, MerchantStore store, Language language) {
        return contentRepository.findByCodeLike(
                type, '%' + codeLike + '%', store.getId(), language.getId());
    }

    @Override
    public Content getById(Long id, MerchantStore store, Language language)
            throws ServiceException {

        Content content = contentRepository.findOne(id);

        if (content != null) {
            if (content.getMerchantStore().getId().intValue() != store.getId().intValue()) {
                return null;
            }
        }

        return content;
    }

    public Content getById(Long id, MerchantStore store) throws ServiceException {

        Content content = contentRepository.findOne(id);

        if (content != null) {
            if (content.getMerchantStore().getId().intValue() != store.getId().intValue()) {
                return null;
            }
        }

        return content;
    }

    @Override
    public void addFolder(MerchantStore store, Optional<String> path, String folderName)
            throws ServiceException {
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(folderName, "Folder name cannot be null");

        if (path.isPresent()) {
            if (!this.isValidLinuxDirectory(path.get())) {
                throw new ServiceException(
                        "Path format [" + path.get() + "] not a valid directory format");
            }
        }
        assetsManager.addFolder(store.getCode(), folderName, path);
    }

    @Override
    public List<String> listFolders(MerchantStore store, Optional<String> path)
            throws ServiceException {
        Assert.notNull(store, "MerchantStore cannot be null");

        return assetsManager.listFolders(store.getCode(), path);
    }

    @Override
    public void removeFolder(MerchantStore store, Optional<String> path, String folderName)
            throws ServiceException {
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(folderName, "Folder name cannot be null");

        assetsManager.removeFolder(store.getCode(), folderName, path);
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
            ContentType contentType, MerchantStore store, int page, int count)
            throws ServiceException {
        Pageable pageRequest = PageRequest.of(page, count);
        return pageContentRepository.findByContentType(contentType, store.getId(), pageRequest);
    }

    @Override
    public Page<Content> listByType(
            ContentType contentType, MerchantStore store, Language language, int page, int count)
            throws ServiceException {
        Pageable pageRequest = PageRequest.of(page, count);
        return pageContentRepository.findByContentType(
                contentType, store.getId(), language.getId(), pageRequest);
    }

    @Override
    public boolean exists(String code, ContentType type, MerchantStore store) {
        Content c = contentRepository.findByCodeAndType(code, type, store.getId());
        return c != null ? true : false;
    }
}
