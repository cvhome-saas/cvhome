package com.asrevo.cvhome.content.repositories.content;

import java.util.List;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.content.ContentDescription;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

public interface ContentRepositoryCustom {

    List<ContentDescription> listNameByType(List<ContentType> contentType, StoreMerchantId store,
                                            LanguageCode language);

}
