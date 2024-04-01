package com.asrevo.cvhome.store.core.model.content.box;

import java.util.List;

import com.asrevo.cvhome.store.core.model.content.common.ContentDescription;

public class PersistableContentBox extends ContentBox {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
	private List<ContentDescription> descriptions;

	public List<ContentDescription> getDescriptions() {
		return descriptions;
	}

	public void setDescriptions(List<ContentDescription> descriptions) {
		this.descriptions = descriptions;
	}

}
