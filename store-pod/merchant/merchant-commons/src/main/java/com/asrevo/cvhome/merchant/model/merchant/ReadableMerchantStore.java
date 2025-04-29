package com.asrevo.cvhome.merchant.model.merchant;

import com.asrevo.cvhome.store.core.model.content.ReadableImage;
import com.asrevo.cvhome.store.core.model.entity.ReadableAudit;
import com.asrevo.cvhome.store.core.model.entity.ReadableAuditable;
import com.asrevo.cvhome.store.model.references.ReadableAddress;
import com.asrevo.cvhome.store.model.references.ReadableLanguage;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReadableMerchantStore extends MerchantStoreEntity
        implements ReadableAuditable, Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private String currentUserLanguage;
    private ReadableAddress address;
    private ReadableImage logo;
    private ReadableImage banner;
    private ReadableAudit audit;
    private ReadableMerchantStore parent;

    private List<ReadableLanguage> supportedLanguages;

    public ReadableAudit getReadableAudit() {
        return this.audit;
    }

    public void setReadableAudit(ReadableAudit audit) {
        this.audit = audit;
    }
}
