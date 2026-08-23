package com.asrevo.cvhome.content.model.legacy;

import java.io.Serial;
import java.util.List;

import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

/**
 * Legacy {@code GET /api/v1/content/boxes/{code}} shape: {@code {id, code, visible, contentType:"BOX",
 * description, descriptions}}.
 */
@Getter
@Setter
public class LegacyContentBox extends Entity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String code;

    private boolean visible;

    private String contentType = "BOX";

    private LegacyDescription description;

    private List<LegacyDescription> descriptions;

}
