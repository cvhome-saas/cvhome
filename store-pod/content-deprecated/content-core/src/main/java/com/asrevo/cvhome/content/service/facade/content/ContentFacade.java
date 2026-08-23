package com.asrevo.cvhome.content.service.facade.content;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.errors.ContentFileUnreadableException;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.errors.DuplicateContentCodeException;
import com.asrevo.cvhome.content.model.content.ContentFile;
import com.asrevo.cvhome.content.model.content.ContentFolder;
import com.asrevo.cvhome.content.model.content.box.PersistableContentBox;
import com.asrevo.cvhome.content.model.content.box.ReadableContentBox;
import com.asrevo.cvhome.content.model.content.box.ReadableContentBoxList;
import com.asrevo.cvhome.content.model.content.page.PersistableContentPage;
import com.asrevo.cvhome.content.model.content.page.ReadableContentPage;
import com.asrevo.cvhome.content.model.content.page.ReadableContentPageList;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetDeleteFailedException;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetListFailedException;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetUploadFailedException;

/**
 * Images and files management
 *
 * @author carlsamson
 */
public interface ContentFacade {

    ContentFolder getContentFolder(String folder, StoreMerchantId store) throws AssetListFailedException;

    /**
     * File pth
     */
    String absolutePath(StoreMerchantId store, String file);

    /**
     * Deletes a file from CMS
     */
    void delete(StoreMerchantId store, String fileName, String fileType) throws AssetDeleteFailedException;

    /**
     * Delete content page
     */
    void delete(StoreMerchantId store, Long id) throws ContentNotFoundException;

    /**
     * Returns page names and urls configured for a given StoreMerchantId
     */
    ReadableContentPageList getContentPages(StoreMerchantId store, LanguageCode language, Pageable pageable);

    /**
     * Returns page name by code
     */
    ReadableContentPage getContentPage(String code, StoreMerchantId store, LanguageCode language)
            throws ContentNotFoundException;

    /**
     * Returns page by name
     */
    ReadableContentPage getContentPageByName(String name, StoreMerchantId store, LanguageCode language)
            throws ContentNotFoundException;

    /**
     * Returns a content box for a given code and merchant store
     */
    ReadableContentBox getContentBox(String code, StoreMerchantId store, LanguageCode language)
            throws ContentNotFoundException;

    /**
     *
     */
    boolean codeExist(String code, String type, StoreMerchantId store);

    ReadableContentBoxList getContentBoxes(StoreMerchantId store, LanguageCode language, Pageable pageable);

    void addContentFile(ContentFile file, String merchantStoreCode)
            throws ContentFileUnreadableException, AssetUploadFailedException;

    /**
     * Creates content page
     */
    Long saveContentPage(PersistableContentPage page, StoreMerchantId merchantStore, LanguageCode language)
            throws DuplicateContentCodeException;

    void updateContentPage(Long id, PersistableContentPage page, StoreMerchantId merchantStore, LanguageCode language)
            throws ContentNotFoundException;

    /**
     * Creates content box
     */
    Long saveContentBox(PersistableContentBox box, StoreMerchantId merchantStore, LanguageCode language)
            throws DuplicateContentCodeException;

    void updateContentBox(Long id, PersistableContentBox box, StoreMerchantId merchantStore, LanguageCode language)
            throws ContentNotFoundException;

}
