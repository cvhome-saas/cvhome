package com.asrevo.cvhome.store.service.facade.content;

import com.asrevo.cvhome.store.core.entity.content.ContentType;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.OutputContentFile;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.content.ContentFile;
import com.asrevo.cvhome.store.core.model.content.ContentFolder;
import com.asrevo.cvhome.store.core.model.content.ReadableContentEntity;
import com.asrevo.cvhome.store.core.model.content.ReadableContentFull;
import com.asrevo.cvhome.store.core.model.content.box.PersistableContentBox;
import com.asrevo.cvhome.store.core.model.content.box.ReadableContentBox;
import com.asrevo.cvhome.store.core.model.content.page.PersistableContentPage;
import com.asrevo.cvhome.store.core.model.content.page.ReadableContentPage;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

import java.util.List;
import java.util.Optional;


/**
 * Images and files management
 *
 * @author carlsamson
 */
public interface ContentFacade {


    ContentFolder getContentFolder(String folder, MerchantStore store) throws Exception;

    /**
     * File pth
     *
     */
    String absolutePath(MerchantStore store, String file);

    /**
     * Deletes a file from CMS
     *
     */
    void delete(MerchantStore store, String fileName, String fileType);

    /**
     * Delete content page
     *
     */
    void delete(MerchantStore store, Long id);


    /**
     * Returns page names and urls configured for a given MerchantStore
     *
     */
    ReadableEntityList<ReadableContentPage> getContentPages(MerchantStore store, Language language, int page, int count);


    /**
     * Returns page name by code
     *
     */
    ReadableContentPage getContentPage(String code, MerchantStore store, Language language);

    /**
     * Returns page by name
     *
     */
    ReadableContentPage getContentPageByName(String name, MerchantStore store, Language language);


    /**
     * Returns a content box for a given code and merchant store
     *
     */
    ReadableContentBox getContentBox(String code, MerchantStore store, Language language);


    /**
     */
    boolean codeExist(String code, String type, MerchantStore store);


    /**
     * Returns content boxes created with code prefix
     * for example return boxes with code starting with <code>_
     *
     */
    ReadableEntityList<ReadableContentBox> getContentBoxes(ContentType type, String codePrefix, MerchantStore store, Language language, int start, int count);

    ReadableEntityList<ReadableContentBox> getContentBoxes(ContentType type, MerchantStore store, Language language, int start, int count);

    void addContentFile(com.asrevo.cvhome.store.core.model.content.ContentFile file, String merchantStoreCode);

    /**
     * Add multiple files
     *
     */
    void addContentFiles(List<ContentFile> file, String merchantStoreCode);

    /**
     * Creates content page
     *
     */
    Long saveContentPage(PersistableContentPage page, MerchantStore merchantStore, Language language);

    void updateContentPage(Long id, PersistableContentPage page, MerchantStore merchantStore, Language language);

    void deleteContent(Long id, MerchantStore merchantStore);

    /**
     * Creates content box
     *
     */
    Long saveContentBox(PersistableContentBox box, MerchantStore merchantStore, Language language);

    void updateContentBox(Long id, PersistableContentBox box, MerchantStore merchantStore, Language language);


    @Deprecated
    ReadableContentFull getContent(String code, MerchantStore store, Language language);

    /**
     * Get all content types
     *
     */
    List<ReadableContentEntity> getContents(Optional<String> type, MerchantStore store, Language language);

    /**
     * Rename file
     *
     */
    void renameFile(MerchantStore store, FileContentType fileType, String originalName, String newName);

    /**
     * Download file
     *
     */
    OutputContentFile download(MerchantStore store, FileContentType fileType, String fileName);

}