package com.asrevo.cvhome.content.model.site;

import java.io.Serial;
import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

/**
 * The appearance record as the console reads it: the ids it will write back, plus the resolved branding so the
 * form can show thumbnails without a second round trip.
 */
@Getter
@Setter
public class ReadableSiteSettings extends PersistableSiteSettings {

    @Serial
    private static final long serialVersionUID = 1L;

    private SiteBranding branding;

    private Instant updatedAt;

    private String updatedBy;

}
