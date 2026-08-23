package com.asrevo.cvhome.content.model.menu;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import com.asrevo.cvhome.content.model.MenuHandle;

import lombok.Getter;
import lombok.Setter;

/**
 * A whole menu tree. {@code PUT /menus/{handle}} replaces it atomically.
 */
@Getter
@Setter
public class Menu implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private MenuHandle handle;

    private Map<String, String> names = new LinkedHashMap<>();

    @Valid
    private List<MenuItem> items = new ArrayList<>();

    private int itemCount;

}
