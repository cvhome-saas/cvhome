package com.asrevo.cvhome.content.service.binding;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.content.ContentFixtures;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.model.page.PersistablePage;
import com.asrevo.cvhome.content.model.page.ReadablePage;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Static pages. A page is a slug, copy and two placement flags — the {@code template} choice this used to cover is
 * gone: it stored a layout no theme ever read.
 */
class PageBindingTest {

    private static final String ABOUT_TITLE = "About";

    private static final String SLUG = "about-us";

    private final PageBinding binding = new PageBinding();

    @Test
    void theTypeContractIsThePageOne() {
        assertThat(binding.type()).isEqualTo(ContentType.PAGE);
        assertThat(binding.persistableClass()).isEqualTo(PersistablePage.class);
        assertThat(binding.newReadable()).isInstanceOf(ReadablePage.class);
        assertThat(binding.storefrontPath(ContentFixtures.content(1L, ContentType.PAGE, SLUG)))
                .isEqualTo("/content/about-us");
    }

    @Test
    void applyCopiesTheFooterFlagAndParent() {
        Content c = ContentFixtures.content(1L, ContentType.PAGE, SLUG);
        PersistablePage dto = new PersistablePage();
        dto.setParentId(4L);
        dto.setShowInFooter(true);

        binding.apply(c, dto);

        assertThat(c.getParentId()).isEqualTo(4L);
        assertThat(c.isShowInFooter()).isTrue();
    }

    @Test
    void populateCarriesTheStatusLocalesAndAudit() {
        Content c = ContentFixtures.published(1L, ContentType.PAGE, SLUG, ABOUT_TITLE);
        c.setShowInFooter(true);
        ReadablePage dto = new ReadablePage();

        binding.populate(c, dto);

        assertThat(dto.isShowInFooter()).isTrue();
        assertThat(dto.getLocales()).hasSize(1);
        assertThat(dto.getAudit()).isNotNull();
    }

}
