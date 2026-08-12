package com.asrevo.cvhome.content.repositories.content;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.content.Content;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

public interface ContentRepository extends JpaRepository<Content, Long>, ContentRepositoryCustom {

    @Query("""
            select c from Content c
            where c.code = ?1 and c.contentType = ?2 and c.storeMerchantId = ?3""")
    Content findByCodeAndType(String code, ContentType contentType, StoreMerchantId storeMerchantId);

    @Query("""
            select c from Content c
            left join fetch c.descriptions cd
            where c.code = ?1 and c.storeMerchantId = ?2""")
    Content findByCodeFetchAllLanguages(String code, StoreMerchantId storeMerchantId);

    @Query("""
            select c from Content c
            where c.code = ?1 and c.storeMerchantId = ?2""")
    Content findByCodeFetchNonLanguages(String code, StoreMerchantId storeMerchantId);

    @Query("""
            select c from Content c
            left join fetch c.descriptions cd
            where c.code = ?1 and c.storeMerchantId = ?2 and cd.languageCode = ?3""")
    Content findByCode(String code, StoreMerchantId storeMerchantId, LanguageCode languageCode);

    @Query("""
            select c from Content c
            left join fetch c.descriptions cd
            where c.id = ?1""")
    Content findOne(Long contentId);

    @Query("""
            select c from Content c
                                    left join fetch c.descriptions cd
                                    where c.storeMerchantId =:storeMerchantId
                                    and cd.languageCode=:languageCode
                                    and cd.seUrl =:seUrl
                                    and c.visible =true
            """)
    Optional<Content> findBySeUrl(@Param("storeMerchantId") StoreMerchantId storeMerchantId,
                                  @Param("seUrl") String seUrl, @Param("languageCode") LanguageCode languageCode);

}
