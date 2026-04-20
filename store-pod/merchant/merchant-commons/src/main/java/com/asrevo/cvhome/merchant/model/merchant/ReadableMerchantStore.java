package com.asrevo.cvhome.merchant.model.merchant;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.asrevo.cvhome.commons.domain.ManagerStoreDomain;
import com.asrevo.cvhome.commons.domain.ReadableSliderImage;
import com.asrevo.cvhome.commons.domain.SocialLink;
import com.asrevo.cvhome.store.core.model.content.ReadableImage;
import com.asrevo.cvhome.store.core.model.entity.ReadableAudit;
import com.asrevo.cvhome.store.core.model.entity.ReadableAuditable;
import com.asrevo.cvhome.store.model.references.ReadableBaseAddress;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReadableMerchantStore extends MerchantStoreEntity implements ReadableAuditable, Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private String currentUserLanguage;

    private ReadableBaseAddress address;

    private ReadableImage logo;

    private ReadableImage banner;

    private ReadableAudit audit;

    private ReadableMerchantStore parent;

    private Set<ReadableSliderImage> sliderImages;

    private Set<SocialLink> socialLinks;

    private Set<ManagerStoreDomain> storeDomains;

    private List<String> supportedLanguages;

    public ReadableAudit getReadableAudit() {
        return this.audit;
    }

    public void setReadableAudit(ReadableAudit audit) {
        this.audit = audit;
    }

}
