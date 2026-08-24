package com.asrevo.cvhome.content.model.storefront;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.asrevo.cvhome.content.model.MenuTargetKind;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StorefrontMenuNode implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String label;

    private MenuTargetKind kind;

    private String value;

    private String href;

    private boolean openInNewTab;

    private List<StorefrontMenuNode> children = new ArrayList<>();

}
