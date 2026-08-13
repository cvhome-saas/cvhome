package com.asrevo.cvhome.content.api.v2;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.asrevo.cvhome.billing.commons.errors.EntitlementExceededException;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.errors.InvalidMediaException;
import com.asrevo.cvhome.content.errors.MediaStorageException;
import com.asrevo.cvhome.content.model.MediaAssetView;
import com.asrevo.cvhome.content.service.MediaAssetService;

@RestController
@RequestMapping("/api/v2/private/media/assets")
public class MediaAssetApi {
    private final MediaAssetService mediaService;

    public MediaAssetApi(MediaAssetService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CONTENT.*')")
    public MediaAssetView upload(@RequestPart MultipartFile file, StoreMerchantId merchantStore,
                                 LanguageCode language) throws IOException, InvalidMediaException,
            MediaStorageException, EntitlementExceededException {
        return mediaService.upload(merchantStore, file.getOriginalFilename(), file.getInputStream(), file.getSize());
    }

    @GetMapping
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CONTENT.*')")
    public List<MediaAssetView> list(StoreMerchantId merchantStore, LanguageCode language) {
        return mediaService.list(merchantStore);
    }
}
