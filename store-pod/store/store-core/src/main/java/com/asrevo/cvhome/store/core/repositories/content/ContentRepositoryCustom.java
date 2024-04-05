package com.asrevo.cvhome.store.core.repositories.content;

import com.asrevo.cvhome.store.core.entity.content.ContentDescription;
import com.asrevo.cvhome.store.core.entity.content.ContentType;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;

import java.util.List;


public interface ContentRepositoryCustom {

    List<ContentDescription> listNameByType(List<ContentType> contentType,
                                            MerchantStore store, Language language);

    ContentDescription getBySeUrl(MerchantStore store, String seUrl);


}
