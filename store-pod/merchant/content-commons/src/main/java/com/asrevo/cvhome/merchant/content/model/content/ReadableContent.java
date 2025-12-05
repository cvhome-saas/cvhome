package com.asrevo.cvhome.merchant.content.model.content;

import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

/**
 * A simple piece of content
 *
 * @author carlsamson
 */
@Setter
@Getter
@Deprecated
public class ReadableContent extends Content {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private String content;

}
