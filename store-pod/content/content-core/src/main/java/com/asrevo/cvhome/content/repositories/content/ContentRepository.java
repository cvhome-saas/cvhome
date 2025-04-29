package com.asrevo.cvhome.content.repositories.content;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.content.Content;
import com.asrevo.cvhome.store.core.entity.content.ContentType;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ContentRepository extends JpaRepository<Content, Long>, ContentRepositoryCustom {

    @Query(
            """
                    select c from Content c
                    left join fetch c.descriptions cd
                    where c.contentType = ?1 and c.storeMerchantId = ?2 and cd.languageCode = ?3
                    order by c.sortOrder asc""")
    List<Content> findByType(
            ContentType contentType, StoreMerchantId storeMerchantId, LanguageCode languageCode);

    @Query(
            """
                    select c from Content c
                    where c.code = ?1 and c.contentType = ?2 and c.storeMerchantId = ?3""")
    Content findByCodeAndType(
            String code, ContentType contentType, StoreMerchantId storeMerchantId);

    @Query(
            """
                    select c from Content c
                    left join fetch c.descriptions cd
                    where c.contentType in (?1) and c.storeMerchantId = ?2 and cd.languageCode = ?3
                    order by c.sortOrder asc""")
    List<Content> findByTypes(
            List<ContentType> contentTypes,
            StoreMerchantId storeMerchantId,
            LanguageCode languageCode);

    @Query(
            """
                    select c from Content c
                    left join fetch c.descriptions cd
                    where c.contentType in (?1) and c.storeMerchantId = ?2
                    order by c.sortOrder asc""")
    List<Content> findByTypes(List<ContentType> contentTypes, StoreMerchantId storeMerchantId);

    @Query(
            """
                    select c from Content c
                    left join fetch c.descriptions cd
                    where c.code = ?1 and c.storeMerchantId = ?2""")
    Content findByCode(String code, StoreMerchantId storeMerchantId);

    @Query(
            """
                    select c from Content c
                    left join fetch c.descriptions cd
                    where c.contentType = ?1 and c.storeMerchantId=?3
                    and c.code like ?2 and cd.languageCode= ?4""")
    List<Content> findByCodeLike(
            ContentType contentType,
            String code,
            StoreMerchantId storeMerchantId,
            LanguageCode languageCode);

    @Query(
            """
                    select c from Content c
                    left join fetch c.descriptions cd
                    where c.code = ?1 and c.storeMerchantId = ?2 and cd.languageCode = ?3""")
    Content findByCode(String code, StoreMerchantId storeMerchantId, LanguageCode languageCode);

    @Query(
            """
                    select c from Content c
                    left join fetch c.descriptions cd
                    where c.id = ?1 and cd.languageCode = ?2""")
    Content findByIdAndLanguage(Long contentId, LanguageCode languageCode);

    @Query(
            """
                    select c from Content c
                    left join fetch c.descriptions cd
                    where c.id = ?1""")
    Content findOne(Long contentId);
}
