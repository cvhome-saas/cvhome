package com.asrevo.cvhome.catalog.model.product;

import com.asrevo.cvhome.commons.domain.Entity;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableImage extends Entity implements Serializable {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private String imageName;

	private String imageUrl;

	private String externalUrl;

	private String videoUrl;

	private int imageType;

	private int order;

	private boolean defaultImage;

}
