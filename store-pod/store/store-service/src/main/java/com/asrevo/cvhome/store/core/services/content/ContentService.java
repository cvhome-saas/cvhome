package com.asrevo.cvhome.store.core.services.content;

import com.asrevo.cvhome.store.core.entity.content.*;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

/**
 * Interface defining methods responsible for CMSContentService.
 * ContentServive will be be entry point for CMS and take care of following functionalities.
 * <li>Adding,removing Content images for given merchant store</li>
 * <li>Get,Save,Update Content data for given merchant store</li>
 *
 * @author Umesh Awasthhi
 * @author Carl Samson
 */
public interface ContentService extends SalesManagerEntityService<Long, Content> {

    List<Content> listByType(ContentType contentType, MerchantStore store, Language language)
            throws ServiceException;

    List<Content> listByType(List<ContentType> contentType, MerchantStore store, Language language)
            throws ServiceException;

    Content getByCode(String code, MerchantStore store) throws ServiceException;

    void saveOrUpdate(Content content) throws ServiceException;

    boolean exists(String code, ContentType type, MerchantStore store);

    Content getByCode(String code, MerchantStore store, Language language) throws ServiceException;

    Content getById(Long id, MerchantStore store, Language language) throws ServiceException;

    Content getById(Long id, MerchantStore store) throws ServiceException;

    /**
     * Method responsible for storing content file for given Store.Files for given merchant store will be stored in
     * Infinispan.
     *
     * @param merchantStoreCode merchant store whose content images are being saved.
     * @param contentFile       content image being stored
     */
    void addContentFile(String merchantStoreCode, InputContentFile contentFile)
            throws ServiceException;

    /**
     * Method responsible for storing list of content image for given Store.Images for given merchant store will be stored in
     * Infinispan.
     *
     * @param merchantStoreCode merchant store whose content images are being saved.
     */
    void addContentFiles(String merchantStoreCode, List<InputContentFile> contentFilesList)
            throws ServiceException;

    /**
     * Method to remove given content image.Images are stored in underlying system based on there name.
     * Name will be used to search given image for removal
     *
     */
    void removeFile(String merchantStoreCode, FileContentType fileContentType, String fileName)
            throws ServiceException;

    /**
     * Removes static file
     * FileType is no more important
     *
     */
    void removeFile(String storeCode, String filename) throws ServiceException;

    /**
     * Method to remove all images for a given merchant.It will take merchant store as an input and will
     * remove all images associated with given merchant store.
     *
     */
    void removeFiles(String merchantStoreCode) throws ServiceException;

    /**
     * Rename file
     *
     */
    void renameFile(
            String merchantStoreCode,
            FileContentType fileContentType,
            Optional<String> path,
            String originalName,
            String newName)
            throws ServiceException;

    /**
     * Method responsible for fetching particular content image for a given merchant store. Requested image will be
     * search in Infinispan tree cache and OutputContentImage will be sent, in case no image is found null will
     * returned.
     *
     * @return {@link OutputContentFile}
     */
    OutputContentFile getContentFile(
            String merchantStoreCode, FileContentType fileContentType, String fileName)
            throws ServiceException;

    /**
     * Method to get list of all images associated with a given merchant store.In case of no image method will return an empty list.
     *
     * @return list of {@link List<OutputContentFile>}
     */
    List<OutputContentFile> getContentFiles(
            String merchantStoreCode, FileContentType fileContentType) throws ServiceException;

    List<String> getContentFilesNames(String merchantStoreCode, FileContentType fileContentType)
            throws ServiceException;

    /**
     * Add the store logo
     *
     */
    void addLogo(String merchantStoreCode, InputContentFile cmsContentImage)
            throws ServiceException;

    /**
     * Add the store banner
     *
     */
    void addBanner(String merchantStoreCode, InputContentFile cmsContentImage)
            throws ServiceException;

    /**
     * Adds a property (option) image
     *
     */
    void addOptionImage(String merchantStoreCode, InputContentFile cmsContentImage)
            throws ServiceException;

    List<Content> listByType(List<ContentType> contentType, MerchantStore store)
            throws ServiceException;

    Page<Content> listByType(ContentType contentType, MerchantStore store, int page, int count)
            throws ServiceException;

    Page<Content> listByType(
            ContentType contentType, MerchantStore store, Language language, int page, int count)
            throws ServiceException;

    List<ContentDescription> listNameByType(
            List<ContentType> contentType, MerchantStore store, Language language)
            throws ServiceException;

    Content getByLanguage(Long id, Language language) throws ServiceException;

    ContentDescription getBySeUrl(MerchantStore store, String seUrl);

    /**
     * Finds content for a specific Merchant for a specific ContentType where content
     * code is like a given prefix in a specific language
     *
     */
    List<Content> getByCodeLike(
            ContentType type, String codeLike, MerchantStore store, Language language);

    void addFolder(MerchantStore store, Optional<String> path, String folderName)
            throws ServiceException;

    List<String> listFolders(MerchantStore store, Optional<String> path) throws ServiceException;

    void removeFolder(MerchantStore store, Optional<String> path, String folderName)
            throws ServiceException;
}
