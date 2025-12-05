package com.asrevo.cvhome.merchant.content.model.content.box;

import com.asrevo.cvhome.merchant.content.model.content.common.ContentDescription;
import java.io.Serial;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableContentBox extends ContentBox {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private List<ContentDescription> descriptions;

}
