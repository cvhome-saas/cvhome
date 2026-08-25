package com.asrevo.cvhome.content.model.media;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

/**
 * Body of {@code PATCH /media/{id}}: the editable metadata. Null fields are left alone.
 */
@Getter
@Setter
public class PersistableMediaAsset implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long folderId;

    private Map<String, String> altTexts;

    @Size(max = 200)
    private String title;

    private List<@Size(max = 40) String> tags;

}
