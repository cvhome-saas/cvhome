package com.asrevo.cvhome.content.repositories.content;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.content.ContentDescription;
import com.asrevo.cvhome.store.core.entity.content.ContentType;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import java.util.List;

public interface ContentRepositoryCustom {

    List<ContentDescription> listNameByType(
            List<ContentType> contentType, StoreMerchantId store, LanguageCode language);
}
