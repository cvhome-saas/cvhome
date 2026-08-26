package com.asrevo.cvhome.merchant.model.merchant;

import java.io.Serial;
import java.util.List;
import java.util.Set;

import com.asrevo.cvhome.commons.domain.ManagerStoreDomain;
import com.asrevo.cvhome.store.model.references.PersistableBaseAddress;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableMerchantStore extends MerchantStoreDetails {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private PersistableBaseAddress address;

    // code of parent store (can be null if retailer)
    private String retailerStore;

    private List<String> supportedLanguages;

    private Set<ManagerStoreDomain> storeDomains;

}
