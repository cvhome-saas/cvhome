package com.asrevo.cvhome.content.services.content;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.content.Content;
import com.asrevo.cvhome.content.errors.ContentFileNotFoundException;
import com.asrevo.cvhome.content.errors.InvalidFolderPathException;
import com.asrevo.cvhome.store.core.entity.content.ContentType;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.InputContentFile;
import com.asrevo.cvhome.store.core.entity.content.OutputContentFile;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetDeleteFailedException;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetListFailedException;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetNotFoundException;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetReadFailedException;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetUploadFailedException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;


public interface ContentService extends SalesManagerEntityService<Long, Content> {

    Content getByCodeFetchAllLanguages(String code, StoreMerchantId store);

    Content getByCodeFetchNonLanguages(String code, StoreMerchantId store);

    void saveOrUpdate(Content content);

    boolean exists(String code, ContentType type, StoreMerchantId store);

    Content getByCode(String code, StoreMerchantId store, LanguageCode language);

    Content getById(Long id, StoreMerchantId store);


    void addContentFile(String merchantStoreCode, InputContentFile contentFile) throws AssetUploadFailedException;

    void addContentFiles(String merchantStoreCode, List<InputContentFile> contentFilesList)
            throws AssetUploadFailedException;

    void removeFile(String merchantStoreCode, FileContentType fileContentType, String fileName)
            throws AssetDeleteFailedException;

    void removeFile(String storeCode, String filename) throws AssetDeleteFailedException;

    void removeFiles(String merchantStoreCode) throws AssetDeleteFailedException;

    /**
     * Rename file
     */
    void renameFile(String merchantStoreCode, FileContentType fileContentType, Optional<String> path,
                    String originalName, String newName) throws ContentFileNotFoundException, AssetNotFoundException,
            AssetReadFailedException, AssetDeleteFailedException, AssetUploadFailedException;

    OutputContentFile getContentFile(String merchantStoreCode, FileContentType fileContentType, String fileName)
            throws AssetNotFoundException, AssetReadFailedException;


    List<OutputContentFile> getContentFiles(String merchantStoreCode, FileContentType fileContentType)
            throws AssetListFailedException;

    List<String> getContentFilesNames(String merchantStoreCode, FileContentType fileContentType)
            throws AssetListFailedException;

    void addLogo(String merchantStoreCode, InputContentFile cmsContentImage) throws AssetUploadFailedException;

    void addBanner(String merchantStoreCode, InputContentFile cmsContentImage) throws AssetUploadFailedException;

    void addOptionImage(String merchantStoreCode, InputContentFile cmsContentImage) throws AssetUploadFailedException;

    Page<Content> listByType(ContentType contentType, StoreMerchantId store, LanguageCode language, Pageable pageable);

    Optional<Content> findBySeUrl(StoreMerchantId store, String seUrl, LanguageCode languageCode);

    void addFolder(StoreMerchantId store, Optional<String> path, String folderName) throws InvalidFolderPathException;

    List<String> listFolders(StoreMerchantId store, Optional<String> path);

    void removeFolder(StoreMerchantId store, Optional<String> path, String folderName);

}
