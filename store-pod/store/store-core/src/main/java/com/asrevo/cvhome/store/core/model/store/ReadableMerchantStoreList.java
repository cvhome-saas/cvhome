package com.asrevo.cvhome.store.core.model.store;

import com.asrevo.cvhome.store.core.model.entity.ReadableList;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ReadableMerchantStoreList extends ReadableList {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private List<ReadableMerchantStore> data = new ArrayList<ReadableMerchantStore>();

}
