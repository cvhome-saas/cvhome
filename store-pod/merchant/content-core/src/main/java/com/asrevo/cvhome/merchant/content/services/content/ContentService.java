package com.asrevo.cvhome.merchant.content.services.content;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.content.entity.content.Content;
import com.asrevo.cvhome.store.core.entity.content.*;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface defining methods responsible for CMSContentService. ContentServive will be be
 * entry point for CMS and take care of following functionalities.
 * <li>Adding,removing Content images for given merchant store</li>
 * <li>Get,Save,Update Content data for given merchant store</li>
 *
 * @author Umesh Awasthhi
 * @author Carl Samson
 */
public interface ContentService extends SalesManagerEntityService<Long, Content> {

	Content getByCodeFetchAllLanguages(String code, StoreMerchantId store);

	Content getByCodeFetchNonLanguages(String code, StoreMerchantId store);

	void saveOrUpdate(Content content) throws ServiceException;

	boolean exists(String code, ContentType type, StoreMerchantId store);

	Content getByCode(String code, StoreMerchantId store, LanguageCode language);

	Content getById(Long id, StoreMerchantId store);

	/**
	 * Method responsible for storing content file for given Store.Files for given
	 * merchant store will be stored in Infinispan.
	 * @param merchantStoreCode merchant store whose content images are being saved.
	 * @param contentFile content image being stored
	 */
	void addContentFile(String merchantStoreCode, InputContentFile contentFile) throws ServiceException;

	/**
	 * Method responsible for storing list of content image for given Store.Images for
	 * given merchant store will be stored in Infinispan.
	 * @param merchantStoreCode merchant store whose content images are being saved.
	 */
	void addContentFiles(String merchantStoreCode, List<InputContentFile> contentFilesList) throws ServiceException;

	/**
	 * Method to remove given content image.Images are stored in underlying system based
	 * on there name. Name will be used to search given image for removal
	 */
	void removeFile(String merchantStoreCode, FileContentType fileContentType, String fileName) throws ServiceException;

	/**
	 * Removes static file FileType is no more important
	 */
	void removeFile(String storeCode, String filename) throws ServiceException;

	/**
	 * Method to remove all images for a given merchant.It will take merchant store as an
	 * input and will remove all images associated with given merchant store.
	 */
	void removeFiles(String merchantStoreCode) throws ServiceException;

	/**
	 * Rename file
	 */
	void renameFile(String merchantStoreCode, FileContentType fileContentType, Optional<String> path,
			String originalName, String newName) throws ServiceException;

	/**
	 * Method responsible for fetching particular content image for a given merchant
	 * store. Requested image will be search in Infinispan tree cache and
	 * OutputContentImage will be sent, in case no image is found null will returned.
	 * @return {@link OutputContentFile}
	 */
	OutputContentFile getContentFile(String merchantStoreCode, FileContentType fileContentType, String fileName)
			throws ServiceException;

	/**
	 * Method to get list of all images associated with a given merchant store.In case of
	 * no image method will return an empty list.
	 * @return list of {@link List<OutputContentFile>}
	 */
	List<OutputContentFile> getContentFiles(String merchantStoreCode, FileContentType fileContentType)
			throws ServiceException;

	List<String> getContentFilesNames(String merchantStoreCode, FileContentType fileContentType)
			throws ServiceException;

	/**
	 * Add the store logo
	 */
	void addLogo(String merchantStoreCode, InputContentFile cmsContentImage) throws ServiceException;

	/**
	 * Add the store banner
	 */
	void addBanner(String merchantStoreCode, InputContentFile cmsContentImage) throws ServiceException;

	/**
	 * Adds a property (option) image
	 */
	void addOptionImage(String merchantStoreCode, InputContentFile cmsContentImage) throws ServiceException;

	Page<Content> listByType(ContentType contentType, StoreMerchantId store, LanguageCode language, Pageable pageable);

	Optional<Content> findBySeUrl(StoreMerchantId store, String seUrl, LanguageCode languageCode);

	void addFolder(StoreMerchantId store, Optional<String> path, String folderName) throws ServiceException;

	List<String> listFolders(StoreMerchantId store, Optional<String> path);

	void removeFolder(StoreMerchantId store, Optional<String> path, String folderName);

}
