package com.asrevo.cvhome.content.model.content;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Folder containing content images and other files
 *
 * @author carlsamson
 */
@Setter
@Getter
public class ContentFolder {

	List<Content> content = new ArrayList<>();

	private String path;

}
