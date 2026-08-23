package com.asrevo.cvhome.content.model.legacy;

import java.io.Serial;

import com.asrevo.cvhome.store.core.model.catalog.NamedEntity;

/**
 * The {@code description} object of the legacy public content API — {@code NamedEntity}'s field set
 * ({@code id, language, name, description, friendlyUrl, keyWords, highlights, metaDescription, title}), unchanged
 * so the storefront's {@code Description} type keeps parsing it.
 */
public class LegacyDescription extends NamedEntity {

    @Serial
    private static final long serialVersionUID = 1L;

}
