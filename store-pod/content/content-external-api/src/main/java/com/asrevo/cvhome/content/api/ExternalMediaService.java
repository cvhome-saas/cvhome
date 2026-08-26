package com.asrevo.cvhome.content.api;

import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PutExchange;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.model.media.ExternalMediaUsage;
import com.asrevo.cvhome.content.model.media.ReadableMediaAsset;

/**
 * What another pod service needs from the media library: resolve assets it holds ids for, and tell content which
 * of them it is using.
 *
 * <p>
 * Content is the bottom of this dependency — it never calls back out. That is why {@link ExternalMediaUsage}
 * carries the owner's display title rather than content looking it up.
 * </p>
 *
 * <p>
 * {@code StoreMerchantId} carries no annotation on purpose: an argument resolver serialises it, so tenant context
 * travels on every call automatically.
 * </p>
 */
@HttpExchange("/api/v1")
public interface ExternalMediaService {

    /**
     * The assets of this store among {@code ids}. An id belonging to another store is simply absent, which is
     * what makes this double as the caller's ownership check.
     */
    @GetExchange("/private/content/external/media")
    List<ReadableMediaAsset> resolve(StoreMerchantId merchantStore, @RequestParam("ids") List<Long> ids);

    /**
     * Replaces every reference held by {@code (ownerKind, ownerRef)}. Idempotent by construction.
     */
    @PutExchange("/private/content/external/media/usage")
    void replaceUsage(StoreMerchantId merchantStore, @RequestBody ExternalMediaUsage body);

}
