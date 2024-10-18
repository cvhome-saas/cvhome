package com.asrevo.cvhome.store.core.entity.common;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EntityList implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    // private int totalCount;
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
