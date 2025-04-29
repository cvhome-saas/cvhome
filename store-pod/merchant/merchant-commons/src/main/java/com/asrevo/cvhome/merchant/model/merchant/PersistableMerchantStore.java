package com.asrevo.cvhome.merchant.model.merchant;

import com.asrevo.cvhome.store.model.references.PersistableAddress;
import java.io.Serial;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableMerchantStore extends MerchantStoreEntity {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private PersistableAddress address;
    // code of parent store (can be null if retailer)
    private String retailerStore;
    private List<String> supportedLanguages;
}
