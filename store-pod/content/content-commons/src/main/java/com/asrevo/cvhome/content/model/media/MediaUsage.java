package com.asrevo.cvhome.content.model.media;

import java.io.Serial;
import java.io.Serializable;

import com.asrevo.cvhome.content.model.MediaOwnerKind;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MediaUsage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** What holds the reference. {@code itemType} and {@code itemId} are set only when this is {@code CONTENT}. */
    private MediaOwnerKind ownerKind;

    /** The owner's identity within its kind — a content id as text, a store id, a product id. */
    private String ownerRef;

    private ContentType itemType;

    private Long itemId;

    private String itemTitle;

    private String field;

}
