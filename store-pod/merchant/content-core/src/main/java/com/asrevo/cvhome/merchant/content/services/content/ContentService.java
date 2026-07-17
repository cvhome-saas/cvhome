package com.asrevo.cvhome.merchant.content.services.content;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.content.entity.content.Content;
import com.asrevo.cvhome.store.core.entity.content.ContentType;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.InputContentFile;
import com.asrevo.cvhome.store.core.entity.content.OutputContentFile;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;


public interface ContentService extends SalesManagerEntityService<Long, Content> {

    Content getByCodeFetchAllLanguages(String code, StoreMerchantId store);

    Content getByCodeFetchNonLanguages(String code, StoreMerchantId store);

    void saveOrUpdate(Content content) throws ServiceException;

    boolean exists(String code, ContentType type, StoreMerchantId store);

    Content getByCode(String code, StoreMerchantId store, LanguageCode language);

    Content getById(Long id, StoreMerchantId store);


    void addContentFile(String merchantStoreCode, InputContentFile contentFile) throws ServiceException;

    void addContentFiles(String merchantStoreCode, List<InputContentFile> contentFilesList) throws ServiceException;

    void removeFile(String merchantStoreCode, FileContentType fileContentType, String fileName) throws ServiceException;

    void removeFile(String storeCode, String filename) throws ServiceException;

    void removeFiles(String merchantStoreCode) throws ServiceException;

    /**
     * Rename file
     */
    void renameFile(String merchantStoreCode, FileContentType fileContentType, Optional<String> path,
                    String originalName, String newName) throws ServiceException;

    OutputContentFile getContentFile(String merchantStoreCode, FileContentType fileContentType, String fileName)
            throws ServiceException;


    List<OutputContentFile> getContentFiles(String merchantStoreCode, FileContentType fileContentType)
            throws ServiceException;

    List<String> getContentFilesNames(String merchantStoreCode, FileContentType fileContentType)
            throws ServiceException;

    void addLogo(String merchantStoreCode, InputContentFile cmsContentImage) throws ServiceException;

    void addBanner(String merchantStoreCode, InputContentFile cmsContentImage) throws ServiceException;

    void addOptionImage(String merchantStoreCode, InputContentFile cmsContentImage) throws ServiceException;

    Page<Content> listByType(ContentType contentType, StoreMerchantId store, LanguageCode language, Pageable pageable);

    Optional<Content> findBySeUrl(StoreMerchantId store, String seUrl, LanguageCode languageCode);

    void addFolder(StoreMerchantId store, Optional<String> path, String folderName) throws ServiceException;

    List<String> listFolders(StoreMerchantId store, Optional<String> path);

    void removeFolder(StoreMerchantId store, Optional<String> path, String folderName);

}
