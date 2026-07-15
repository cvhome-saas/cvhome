package com.asrevo.cvhome.merchant.content.repositories.content;

import java.util.List;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.content.entity.content.ContentDescription;
import com.asrevo.cvhome.store.core.entity.content.ContentType;
import com.asrevo.cvhome.commons.domain.LanguageCode;

public interface ContentRepositoryCustom {

    List<ContentDescription> listNameByType(List<ContentType> contentType, StoreMerchantId store,
                                            LanguageCode language);

}
