package com.asrevo.cvhome.store.core.entity.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class EntityList implements Serializable {


    /**
     *
     */
    private static final long serialVersionUID = 1L;
    //private int totalCount;
    private long totalCount;
    private int totalPages;

    /*	public int getTotalCount() {
            return totalCount;
        }*/
/*	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}*/
    public int getTotalPages() {
        return totalPages == 0 ? totalPages + 1 : totalPages;
    }

}
