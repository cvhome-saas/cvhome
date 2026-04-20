package com.asrevo.cvhome.merchant.content.repositories.content;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.content.entity.content.Content;
import com.asrevo.cvhome.merchant.content.entity.content.ContentDescription;
import com.asrevo.cvhome.store.core.entity.content.ContentType;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

public class ContentRepositoryImpl implements ContentRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<ContentDescription> listNameByType(List<ContentType> contentType, StoreMerchantId store,
                                                   LanguageCode language) {

        String hql = """
                select c from Content c
                left join fetch c.descriptions cd
                where c.contentType in (:ct)
                and c.storeMerchantId =:cm
                and cd.languageCode =:cl
                and c.visible=true
                order by c.sortOrder""";
        Query q = this.em.createQuery(hql);

        q.setParameter("ct", contentType);
        q.setParameter("cm", store);
        q.setParameter("cl", language);

        @SuppressWarnings("unchecked")
        List<Content> contents = q.getResultList();

        List<ContentDescription> descriptions = new ArrayList<>();
        for (Content c : contents) {
            String name = c.getDescription().getName();
            String url = c.getDescription().getSeUrl();
            ContentDescription contentDescription = new ContentDescription();
            contentDescription.setName(name);
            contentDescription.setSeUrl(url);
            contentDescription.setContent(c);
            descriptions.add(contentDescription);
        }

        return descriptions;
    }

}
