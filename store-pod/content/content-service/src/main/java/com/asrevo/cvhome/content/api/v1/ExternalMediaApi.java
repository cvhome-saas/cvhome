package com.asrevo.cvhome.content.api.v1;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.api.ExternalMediaService;
import com.asrevo.cvhome.content.api.v1.support.ContentPermissions;
import com.asrevo.cvhome.content.model.media.ExternalMediaUsage;
import com.asrevo.cvhome.content.model.media.ReadableMediaAsset;
import com.asrevo.cvhome.content.service.MediaService;
import com.asrevo.cvhome.content.service.MediaUsageTracker;

import lombok.RequiredArgsConstructor;

/**
 * The media library as another pod service sees it.
 *
 * <p>
 * Implements {@link ExternalMediaService} so the route and the client contract cannot drift.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/private/content/external/media")
@RequiredArgsConstructor
public class ExternalMediaApi implements ExternalMediaService {

    private final MediaService media;

    private final MediaUsageTracker usage;

    /**
     * Reads use {@code CONTENT.READ}, which already falls through to "same store pod" for a service caller.
     */
    @Override
    @GetMapping
    @PreAuthorize(ContentPermissions.READ)
    public List<ReadableMediaAsset> resolve(StoreMerchantId merchantStore, @RequestParam("ids") List<Long> ids) {
        return media.assets(merchantStore, ids);
    }

    @Override
    @PutMapping("usage")
    @PreAuthorize(ContentPermissions.MEDIA_USAGE)
    public void replaceUsage(StoreMerchantId merchantStore, @RequestBody ExternalMediaUsage body) {
        Map<String, Long> refs = new java.util.LinkedHashMap<>();
        if (body.refs() != null) {
            body.refs().forEach(r -> refs.put(r.field(), r.assetId()));
        }
        usage.replace(merchantStore, body.ownerKind(), body.ownerRef(), null, null, body.ownerTitle(), refs);
    }

}
