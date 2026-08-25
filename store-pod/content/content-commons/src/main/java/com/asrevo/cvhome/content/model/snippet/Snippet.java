package com.asrevo.cvhome.content.model.snippet;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import com.asrevo.cvhome.content.model.common.ContentTranslation;

import lombok.Getter;
import lombok.Setter;

/**
 * A store-level text fragment the storefront reads by code — the legacy {@code BOX} rows ({@code meta-title},
 * {@code meta-description}, {@code header-message}, {@code agreement}, {@code LANDING_PAGE}). Snippets have no
 * workflow: they are always live and {@code visible} toggles them.
 */
@Getter
@Setter
public class Snippet implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String code;

    private boolean visible = true;

    @Valid
    @NotEmpty
    private List<ContentTranslation> translations = new ArrayList<>();

}
