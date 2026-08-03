package com.asrevo.cvhome.merchant.content.services.content;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.content.entity.content.Content;
import com.asrevo.cvhome.merchant.content.errors.ContentFileNotFoundException;
import com.asrevo.cvhome.merchant.content.errors.InvalidFolderPathException;
import com.asrevo.cvhome.merchant.content.repositories.content.ContentRepository;
import com.asrevo.cvhome.merchant.content.repositories.content.PageContentRepository;
import com.asrevo.cvhome.store.core.entity.content.ContentType;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.InputContentFile;
import com.asrevo.cvhome.store.core.entity.content.OutputContentFile;
import com.asrevo.cvhome.store.core.modules.cms.content.ContentAssetsManager;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetDeleteFailedException;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetListFailedException;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetNotFoundException;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetReadFailedException;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetUploadFailedException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;

import lombok.extern.slf4j.Slf4j;

@Service("contentService")
@Slf4j
public class ContentServiceImpl extends SalesManagerEntityServiceImpl<Long, Content> implements ContentService {

    private final ContentAssetsManager assetsManager;

    private final ContentRepository contentRepository;

    private final PageContentRepository pageContentRepository;

    @Autowired
    public ContentServiceImpl(ContentRepository contentRepository, ContentAssetsManager assetsManager,
                              PageContentRepository pageContentRepository) {
        super(contentRepository);

        this.contentRepository = contentRepository;
        this.assetsManager = assetsManager;
        this.pageContentRepository = pageContentRepository;
    }

    @Override
    public void delete(Content content) {

        Content c = this.getById(content.getId());
        super.delete(c);
    }

    @Override
    public Content getByCodeFetchAllLanguages(String code, StoreMerchantId store) {

        return contentRepository.findByCodeFetchAllLanguages(code, store);
    }

    @Override
    public Content getByCodeFetchNonLanguages(String code, StoreMerchantId store) {

        return contentRepository.findByCodeFetchNonLanguages(code, store);
    }

    @Override
    public Content getById(Long id) {
        return contentRepository.findOne(id);
    }

    @Override
    public void saveOrUpdate(final Content content) {

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
     * Method responsible for adding content file for given merchant store in underlying
     * Infinispan tree cache. It will take {@link InputContentFile} and will store file
     * for given merchant store according to its type. it can save an image or any type of
     * file (pdf, css, js ...)
     *
     * @param merchantStoreCode Merchant store
     * @param contentFile       {@link InputContentFile} being stored
     * @throws AssetUploadFailedException when the object store refuses the write
     */
    @Override
    public void addContentFile(String merchantStoreCode, InputContentFile contentFile)
            throws AssetUploadFailedException {

        String mimeType = URLConnection.guessContentTypeFromName(contentFile.getFileName());
        contentFile.setMimeType(mimeType);

        if (contentFile.getFileContentType().name().equals(FileContentType.IMAGE.name())
                || contentFile.getFileContentType().name().equals(FileContentType.STATIC_FILE.name())) {
            addFile(merchantStoreCode, contentFile);
        } else if (contentFile.getFileContentType().name().equals(FileContentType.API_IMAGE.name())) {
            contentFile.setFileContentType(FileContentType.IMAGE);
            addImage(merchantStoreCode, contentFile);
        } else if (contentFile.getFileContentType().name().equals(FileContentType.API_FILE.name())) {
            contentFile.setFileContentType(FileContentType.STATIC_FILE);
            addFile(merchantStoreCode, contentFile);
        } else {
            addImage(merchantStoreCode, contentFile);
        }
    }

    @Override
    public void addLogo(String merchantStoreCode, InputContentFile cmsContentImage) throws AssetUploadFailedException {
        cmsContentImage.setFileContentType(FileContentType.LOGO);
        addImage(merchantStoreCode, cmsContentImage);
    }

    @Override
    public void addBanner(String merchantStoreCode, InputContentFile cmsContentImage) throws AssetUploadFailedException {

        cmsContentImage.setFileContentType(FileContentType.BANNER);
        addImage(merchantStoreCode, cmsContentImage);
    }

    @Override
    public void addOptionImage(String merchantStoreCode, InputContentFile cmsContentImage)
            throws AssetUploadFailedException {

        cmsContentImage.setFileContentType(FileContentType.PROPERTY);
        addImage(merchantStoreCode, cmsContentImage);
    }

    private void addImage(String merchantStoreCode, InputContentFile contentImage) throws AssetUploadFailedException {

        try {
            log.info("Adding content image for merchant id {}", merchantStoreCode);

            String p = contentImage.getPath();
            Optional<String> path = Optional.ofNullable(p);
            // The blanket catch that used to sit here re-flattened AssetUploadFailedException, which already names the
            // key that failed, back into an untyped ServiceException. Letting it through is the whole point of Step 4.
            assetsManager.addFile(merchantStoreCode, path, contentImage);

        } finally {
            closeQuietly(contentImage);
        }
    }

    private void addFile(final String merchantStoreCode, InputContentFile contentImage)
            throws AssetUploadFailedException {

        try {
            log.info("Adding content file for merchant id {}", merchantStoreCode);
            Optional<String> path = Optional.empty();

            assetsManager.addFile(merchantStoreCode, path, contentImage);

        } finally {
            closeQuietly(contentImage);
        }
    }

    /**
     * Closes an upload stream without letting a close failure mask the outcome. The upload has already either
     * succeeded or thrown by this point, so a failure here is not the caller's business.
     */
    private void closeQuietly(InputContentFile contentImage) {
        try {
            if (contentImage.getFile() != null) {
                contentImage.getFile().close();
            }
        } catch (IOException e) {
            log.warn("Could not close the upload stream for {}", contentImage.getFileName(), e);
        }
    }

    @Override
    public void addContentFiles(String merchantStoreCode, List<InputContentFile> contentFilesList)
            throws AssetUploadFailedException {

        Optional<String> path = Optional.empty();

        log.info("Adding content images for merchant....");
        assetsManager.addFiles(merchantStoreCode, path, contentFilesList);

        // Was: a close failure here failed the whole call, so a seller whose files all uploaded was told the upload
        // failed. Its two sibling methods already swallowed the same failure; this makes all three agree.
        contentFilesList.forEach(this::closeQuietly);
    }

    /**
     * Method to remove given content image.Images are stored in underlying system based
     * on there name. Name will be used to search given image for removal
     */
    @Override
    public void removeFile(String merchantStoreCode, FileContentType fileContentType, String fileName)
            throws AssetDeleteFailedException {

        Optional<String> path = Optional.empty();

        assetsManager.removeFile(merchantStoreCode, fileContentType, fileName, path);
    }

    @Override
    public void removeFile(String storeCode, String fileName) throws AssetDeleteFailedException {

        String fileType = "IMAGE";
        String mimetype = URLConnection.guessContentTypeFromName(fileName);
        String type = mimetype.split("/")[0];
        if (!"image".equals(type)) {
            fileType = "STATIC_FILE";
        }

        Optional<String> path = Optional.empty();

        assetsManager.removeFile(storeCode, FileContentType.valueOf(fileType), fileName, path);
    }

    /**
     * Method to remove all images for a given merchant.It will take merchant store as an
     * input and will remove all images associated with given merchant store.
     */
    @Override
    public void removeFiles(String merchantStoreCode) throws AssetDeleteFailedException {

        Optional<String> path = Optional.empty();

        assetsManager.removeFiles(merchantStoreCode, path);
    }

    /**
     * Implementation for getContentImage method defined in {@link ContentService}
     * interface. Methods will return Content image with given image name for the Merchant
     * store or will return null if no image with given name found for requested Merchant
     * Store in Infinispan tree cache.
     *
     * @return {@link OutputContentFile}
     */
    @Override
    public OutputContentFile getContentFile(String merchantStoreCode, FileContentType fileContentType, String fileName)
            throws AssetNotFoundException, AssetReadFailedException {

        Optional<String> path = Optional.empty();

        return assetsManager.getFile(merchantStoreCode, path, fileContentType, fileName);
    }

    /**
     * Implementation for getContentImages method defined in {@link ContentService}
     * interface. Methods will return list of all Content image associated with given
     * Merchant store or will return empty list if no image is associated with given
     * Merchant Store in Infinispan tree cache.
     *
     * @return list of {@link OutputContentFile}
     */
    @Override
    public List<OutputContentFile> getContentFiles(String merchantStoreCode, FileContentType fileContentType)
            throws AssetListFailedException {
        Optional<String> path = Optional.empty();
        return assetsManager.getFiles(merchantStoreCode, path, fileContentType);
    }

    /**
     * Returns the image names for a given merchant and store
     *
     * @return images name list
     */
    @Override
    public List<String> getContentFilesNames(String merchantStoreCode, FileContentType fileContentType)
            throws AssetListFailedException {
        Optional<String> path = Optional.empty();

        return assetsManager.getFileNames(merchantStoreCode, path, fileContentType);
    }

    @Override
    public Optional<Content> findBySeUrl(StoreMerchantId store, String seUrl, LanguageCode languageCode) {
        return contentRepository.findBySeUrl(store, seUrl, languageCode);
    }

    public Content getById(Long id, StoreMerchantId store) {

        Content content = contentRepository.findOne(id);

        if (content != null && !Objects.equals(content.getStoreMerchantId(), store)) {
            return null;
        }

        return content;
    }

    @Override
    public void addFolder(StoreMerchantId store, Optional<String> path, String folderName)
            throws InvalidFolderPathException {
        if (path.isPresent() && !this.isValidLinuxDirectory(path.get())) {
            throw InvalidFolderPathException.of(path.get());
        }
        assetsManager.addFolder(store.getId(), folderName, path);
    }

    @Override
    public List<String> listFolders(StoreMerchantId store, Optional<String> path) {
        return assetsManager.listFolders(store.getId(), path);
    }

    @Override
    public void removeFolder(StoreMerchantId store, Optional<String> path, String folderName) {
        assetsManager.removeFolder(store.getId(), folderName, path);
    }

    public boolean isValidLinuxDirectory(String path) {
        Pattern linuxDirectoryPattern = Pattern.compile("^(?:/|(?:/[a-zA-Z0-9_-]+)+)$");
        return path != null && !path.trim().isEmpty() && linuxDirectoryPattern.matcher(path).matches();
    }

    @Override
    public void renameFile(String merchantStoreCode, FileContentType fileContentType, Optional<String> path,
                           String originalName, String newName) throws ContentFileNotFoundException,
            AssetNotFoundException, AssetReadFailedException, AssetDeleteFailedException, AssetUploadFailedException {

        OutputContentFile file = assetsManager.getFile(merchantStoreCode, path, fileContentType, originalName);

        if (file == null) {
            throw ContentFileNotFoundException.of(originalName, merchantStoreCode);
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
    public Page<Content> listByType(ContentType contentType, StoreMerchantId store, LanguageCode language,
                                    Pageable pageable) {
        return pageContentRepository.findByContentType(contentType, store, language, pageable);
    }

    @Override
    public boolean exists(String code, ContentType type, StoreMerchantId store) {
        Content c = contentRepository.findByCodeAndType(code, type, store);
        return c != null;
    }

}
