package com.asrevo.cvhome.content.model.media;

import java.io.Serializable;
import java.util.List;

import com.asrevo.cvhome.content.model.MediaOwnerKind;

/**
 * The <em>complete</em> set of media references held by one owner outside this service — a catalog product, a
 * category, a brand.
 *
 * <p>
 * There is deliberately no register/release pair. Stating the whole set means the server can replace the owner's
 * rows in one transaction, so a retry, a re-save and a partial failure all converge on the same state and no
 * count can drift. Releasing is the same call with an empty {@code refs}.
 * </p>
 *
 * @param ownerKind  what holds the references
 * @param ownerRef   the owner's id within its kind, as text
 * @param ownerTitle what the console shows beside the usage — supplied here so content never has to call back
 *                   into the owning service to name it
 * @param refs       the references, by field
 */
public record ExternalMediaUsage(MediaOwnerKind ownerKind, String ownerRef, String ownerTitle, List<Ref> refs)
        implements Serializable {

    /**
     * @param field   the owner's field name, e.g. {@code image[0]}
     * @param assetId the media asset it points at
     */
    public record Ref(String field, Long assetId) implements Serializable {
    }

}
