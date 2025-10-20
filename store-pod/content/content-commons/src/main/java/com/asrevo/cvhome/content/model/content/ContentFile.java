package com.asrevo.cvhome.content.model.content;

import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

/**
 * Model object used in webservice when creatin files
 *
 * @author carlsamson
 */
@Setter
@Getter
public class ContentFile extends ContentPath {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private byte[] file;

}
