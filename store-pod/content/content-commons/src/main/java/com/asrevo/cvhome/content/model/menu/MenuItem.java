package com.asrevo.cvhome.content.model.menu;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

/**
 * A node of a storefront menu. One level of children.
 */
@Getter
@Setter
public class MenuItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Integer position;

    private Map<String, String> labels = new LinkedHashMap<>();

    @NotNull
    private MenuTarget target;

    private boolean openInNewTab;

    private boolean visible = true;

    @Valid
    private List<MenuItem> children = new ArrayList<>();

}
