package com.asrevo.cvhome.content.service;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.TestcontainersConfiguration;
import com.asrevo.cvhome.content.errors.BannerCapacityExceededException;
import com.asrevo.cvhome.content.errors.FaqGroupNotFoundException;
import com.asrevo.cvhome.content.errors.InvalidFaqReorderException;
import com.asrevo.cvhome.content.model.ContentType;
import com.asrevo.cvhome.content.model.ContentWriteRequest;
import com.asrevo.cvhome.content.model.banner.BannerArtworkSpec;
import com.asrevo.cvhome.content.model.banner.BannerPlacement;
import com.asrevo.cvhome.content.model.banner.BannerTargetKind;
import com.asrevo.cvhome.content.model.banner.BannerWriteRequest;
import com.asrevo.cvhome.content.model.banner.LoginTarget;
import com.asrevo.cvhome.content.model.faq.FaqGroupView;
import com.asrevo.cvhome.content.model.faq.FaqGroupWriteRequest;
import com.asrevo.cvhome.content.model.faq.FaqReorderRequest;
import com.asrevo.cvhome.content.model.faq.FaqView;
import com.asrevo.cvhome.content.model.faq.FaqWriteRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Tag("integration-test")
@Transactional
class BannerFaqServiceIntegrationTest {
    private static final StoreMerchantId STORE = new StoreMerchantId("phase-five-store");
    private static final StoreMerchantId OTHER_STORE = new StoreMerchantId("phase-five-other-store");
    private static final LanguageCode ENGLISH = LanguageCode.defaultLanguage();
    private static final String ACTOR = "phase-five-test";

    @Autowired
    private BannerService bannerService;
    @Autowired
    private FaqService faqService;

    @Test
    void enforcesBannerCapacityPerStoreAndKeepsOrderingDeterministic() throws Exception {
        for (int position = 4; position >= 0; position--) {
            bannerService.create(STORE, ENGLISH, bannerRequest(position), ACTOR);
        }

        assertThat(bannerService.list(STORE, ENGLISH, BannerPlacement.HOME_HERO))
                .extracting(it -> it.position()).containsExactly(0, 1, 2, 3, 4);
        assertThatThrownBy(() -> bannerService.create(STORE, ENGLISH, bannerRequest(5), ACTOR))
                .isInstanceOf(BannerCapacityExceededException.class);
        assertThat(bannerService.create(OTHER_STORE, ENGLISH, bannerRequest(0), ACTOR)).isNotNull();
    }

    @Test
    void reordersFaqAtomicallyAndRejectsForeignStoreGroup() throws Exception {
        FaqGroupView group = faqService.createGroup(STORE, ENGLISH,
                new FaqGroupWriteRequest("shipping", "Shipping", 0));
        FaqView first = faqService.create(STORE, ENGLISH, faqRequest(group.id(), "first", 0), ACTOR);
        FaqView second = faqService.create(STORE, ENGLISH, faqRequest(group.id(), "second", 1), ACTOR);

        List<FaqView> reordered = faqService.reorder(STORE,
                new FaqReorderRequest(group.id(), List.of(second.content().id(), first.content().id())));

        assertThat(reordered).extracting(it -> it.content().id())
                .containsExactly(second.content().id(), first.content().id());
        assertThatThrownBy(() -> faqService.reorder(STORE,
                new FaqReorderRequest(group.id(), List.of(first.content().id(), first.content().id()))))
                .isInstanceOf(InvalidFaqReorderException.class);
        assertThatThrownBy(() -> faqService.create(OTHER_STORE, ENGLISH,
                faqRequest(group.id(), "foreign", 0), ACTOR))
                .isInstanceOf(FaqGroupNotFoundException.class);
    }

    private static BannerWriteRequest bannerRequest(int position) {
        return new BannerWriteRequest(content("banner-%s".formatted(position), ContentType.BANNER),
                BannerPlacement.HOME_HERO, position, BannerTargetKind.URL, "/summer", "#FFFFFF", "#111111",
                LoginTarget.ANY, Set.of("SA"), new BannerArtworkSpec(null, null, "Summer collection"));
    }

    private static FaqWriteRequest faqRequest(Long groupId, String code, int position) {
        return new FaqWriteRequest(content("faq-%s".formatted(code), ContentType.FAQ), groupId, position, List.of());
    }

    private static ContentWriteRequest content(String code, ContentType type) {
        return new ContentWriteRequest(code, type, code, code, "body", code, null, null, null, null,
                false, null, null);
    }
}
