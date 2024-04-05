package com.asrevo.cvhome.store.core.model.content;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Folder containing content
 * images and other files
 *
 * @author carlsamson
 */
@Setter
@Getter
public class ContentFolder {

    List<Content> content = new ArrayList<Content>();
    private String path;

}
