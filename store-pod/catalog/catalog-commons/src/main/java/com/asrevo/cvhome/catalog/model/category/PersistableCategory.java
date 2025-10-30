package com.asrevo.cvhome.catalog.model.category;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableCategory extends CategoryEntity implements Serializable {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private List<CategoryDescription> descriptions; // always persist description

	private List<PersistableCategory> children = new ArrayList<>();

}
