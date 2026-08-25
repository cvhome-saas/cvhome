package com.asrevo.cvhome.content.api.v1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.api.v1.support.Actors;
import com.asrevo.cvhome.content.api.v1.support.ContentPermissions;
import com.asrevo.cvhome.content.config.ContentProperties;
import com.asrevo.cvhome.content.errors.ContentConflictException;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.errors.InvalidContentRequestException;
import com.asrevo.cvhome.content.errors.MediaLimitException;
import com.asrevo.cvhome.content.errors.MediaStorageException;
import com.asrevo.cvhome.content.model.MediaKind;
import com.asrevo.cvhome.content.model.media.MediaFolder;
import com.asrevo.cvhome.content.model.media.MediaUsage;
import com.asrevo.cvhome.content.model.media.PersistableMediaAsset;
import com.asrevo.cvhome.content.model.media.ReadableMediaAsset;
import com.asrevo.cvhome.content.model.media.ReadableMediaAssetList;
import com.asrevo.cvhome.content.service.MediaService;

import lombok.RequiredArgsConstructor;

/**
 * The media library. Uploads are multipart through the service (no presigned URLs on this platform).
 */
@RestController
@RequestMapping("/api/v1/private/content/media")
@RequiredArgsConstructor
public class MediaApi {

    private final MediaService media;

    private final ContentProperties properties;

    @GetMapping
    @PreAuthorize(ContentPermissions.READ)
    public ReadableMediaAssetList list(StoreMerchantId merchantStore, LanguageCode language,
                                       @RequestParam(required = false) Long folder,
                                       @RequestParam(required = false) MediaKind kind,
                                       @RequestParam(required = false) String q,
                                       @RequestParam(required = false) Boolean used,
                                       Pageable pageable) {
        return media.list(merchantStore, folder, kind, q, used, pageable);
    }

    @GetMapping("{id}")
    @PreAuthorize(ContentPermissions.READ)
    public ReadableMediaAsset get(StoreMerchantId merchantStore, LanguageCode language, @PathVariable Long id)
            throws ContentNotFoundException {
        return media.get(merchantStore, id);
    }

    @GetMapping("{id}/usage")
    @PreAuthorize(ContentPermissions.READ)
    public List<MediaUsage> usage(StoreMerchantId merchantStore, LanguageCode language, @PathVariable Long id)
            throws ContentNotFoundException {
        return media.usage(merchantStore, id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ContentPermissions.MANAGE)
    public List<ReadableMediaAsset> upload(StoreMerchantId merchantStore, LanguageCode language,
                                           @RequestParam("files") MultipartFile[] files,
                                           @RequestParam(required = false) Long folderId)
            throws InvalidContentRequestException, MediaLimitException, MediaStorageException,
            ContentNotFoundException {
        List<MediaService.Upload> uploads = new ArrayList<>();
        for (MultipartFile f : files) {
            try {
                uploads.add(new MediaService.Upload(f.getOriginalFilename(), f.getContentType(), f.getBytes()));
            } catch (IOException e) {
                throw InvalidContentRequestException.mediaUnreadable(f.getOriginalFilename(), e);
            }
        }
        return media.upload(merchantStore, uploads, folderId, properties.media().maxFileSize().toBytes(),
                properties.media().quota().toBytes(), Actors.current());
    }

    @PatchMapping("{id}")
    @PreAuthorize(ContentPermissions.MANAGE)
    public ReadableMediaAsset patch(StoreMerchantId merchantStore, LanguageCode language, @PathVariable Long id,
                                    @RequestBody @Valid PersistableMediaAsset body) throws ContentNotFoundException {
        return media.patch(merchantStore, id, body);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(ContentPermissions.MANAGE)
    public void delete(StoreMerchantId merchantStore, LanguageCode language, @PathVariable Long id,
                       @RequestParam(defaultValue = "false") boolean force)
            throws ContentNotFoundException, ContentConflictException, MediaStorageException {
        media.delete(merchantStore, id, force);
    }

    @GetMapping("folders")
    @PreAuthorize(ContentPermissions.READ)
    public List<MediaFolder> folders(StoreMerchantId merchantStore, LanguageCode language) {
        return media.folders(merchantStore);
    }

    @PostMapping("folders")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ContentPermissions.MANAGE)
    public MediaFolder createFolder(StoreMerchantId merchantStore, LanguageCode language,
                                    @RequestBody @Valid MediaFolder body) {
        return media.createFolder(merchantStore, body);
    }

    @PatchMapping("folders/{id}")
    @PreAuthorize(ContentPermissions.MANAGE)
    public MediaFolder renameFolder(StoreMerchantId merchantStore, LanguageCode language, @PathVariable Long id,
                                    @RequestBody @Valid MediaFolder body) throws ContentNotFoundException {
        return media.renameFolder(merchantStore, id, body);
    }

    @DeleteMapping("folders/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(ContentPermissions.MANAGE)
    public void deleteFolder(StoreMerchantId merchantStore, LanguageCode language, @PathVariable Long id,
                             @RequestParam(required = false) Long moveTo)
            throws ContentNotFoundException, ContentConflictException {
        media.deleteFolder(merchantStore, id, moveTo);
    }

}
