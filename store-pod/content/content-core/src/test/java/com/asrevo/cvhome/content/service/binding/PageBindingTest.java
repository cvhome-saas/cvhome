package com.asrevo.cvhome.content.service.binding;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.content.ContentFixtures;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.model.PageTemplate;
import com.asrevo.cvhome.content.model.page.PersistablePage;
import com.asrevo.cvhome.content.model.page.ReadablePage;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Static pages. A page whose template was never chosen renders as STANDARD in both directions, so a legacy row
 * with a null column never reaches the storefront without one.
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
    void anUnchosenTemplateBecomesStandardOnWrite() {
        Content c = ContentFixtures.content(1L, ContentType.PAGE, SLUG);

        binding.apply(c, new PersistablePage());

        assertThat(c.getTemplate()).isEqualTo(PageTemplate.STANDARD);
    }

    @Test
    void applyCopiesTheNavigationFlagsAndParent() {
        Content c = ContentFixtures.content(1L, ContentType.PAGE, SLUG);
        PersistablePage dto = new PersistablePage();
        dto.setTemplate(PageTemplate.LANDING);
        dto.setParentId(4L);
        dto.setShowInFooter(true);
        dto.setLinkToMenu(true);

        binding.apply(c, dto);

        assertThat(c.getTemplate()).isEqualTo(PageTemplate.LANDING);
        assertThat(c.getParentId()).isEqualTo(4L);
        assertThat(c.isShowInFooter()).isTrue();
        assertThat(c.isLinkToMenu()).isTrue();
    }

    @Test
    void aLegacyRowWithoutATemplateStillReadsAsStandard() {
        Content c = ContentFixtures.published(1L, ContentType.PAGE, SLUG, ABOUT_TITLE);
        ReadablePage dto = new ReadablePage();

        binding.populate(c, dto);

        assertThat(dto.getTemplate()).isEqualTo(PageTemplate.STANDARD);
        assertThat(dto.getLocales()).hasSize(1);
        assertThat(dto.getAudit()).isNotNull();
    }

    @Test
    void aChosenTemplateSurvivesTheRoundTrip() {
        Content c = ContentFixtures.published(1L, ContentType.PAGE, SLUG, ABOUT_TITLE);
        c.setTemplate(PageTemplate.LANDING);
        ReadablePage dto = new ReadablePage();

        binding.populate(c, dto);

        assertThat(dto.getTemplate()).isEqualTo(PageTemplate.LANDING);
    }

}
