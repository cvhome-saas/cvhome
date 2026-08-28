package com.asrevo.cvhome.content.model.media;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.asrevo.cvhome.content.model.MediaKind;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReadableMediaAsset implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String filename;

    private String originalFilename;

    private String mimeType;

    private MediaKind kind;

    private long bytes;

    private Integer width;

    private Integer height;

    /**
     * Where the object lives, relative to the CDN base. What another service caches when it wants a reference
     * that keeps working after the CDN moves; {@link #url} is this path already resolved, for a browser.
     */
    private String path;

    private String url;

    private Long folderId;

    private Map<String, String> altTexts;

    private String title;

    private List<String> tags;

    private Instant uploadedAt;

    private String uploadedBy;

    private int usageCount;

    /**
     * Where the asset is referenced; only on the single read and the usage endpoint.
     */
    private List<MediaUsage> usage;

}
