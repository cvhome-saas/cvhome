package com.asrevo.cvhome.store.core.model.store;

import com.asrevo.cvhome.store.core.model.references.PersistableAddress;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.List;

@Setter
@Getter
public class PersistableMerchantStore extends MerchantStoreEntity {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private PersistableAddress address;
    //code of parent store (can be null if retailer)
    private String retailerStore;
    private List<String> supportedLanguages;

}
