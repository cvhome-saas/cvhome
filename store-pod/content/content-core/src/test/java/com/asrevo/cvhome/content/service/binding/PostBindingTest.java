package com.asrevo.cvhome.content.service.binding;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.content.ContentFixtures;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.model.post.PersistablePost;
import com.asrevo.cvhome.content.model.post.PostMeta;
import com.asrevo.cvhome.content.model.post.ReadablePost;
import com.asrevo.cvhome.content.service.MediaService;
import com.asrevo.cvhome.content.support.JsonCodec;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Blog posts. Tags are trimmed and deduplicated on write, and the reading estimate is taken from the longest
 * locale so a translation in progress does not shrink it.
 */
class PostBindingTest {

    private static final String POST_PATH = "/blog/hello-world";

    private static final String NEWS_SLUG = "news";

    private static final String GUIDES_SLUG = "guides";

    private static final String HELLO_TITLE = "Hello";

    private static final String AUTHOR = "Ada";

    private static final String DESKTOP_URL = "https://cdn.test/5.png";

    private static final String SLUG = "hello-world";

    private MediaService media;

    private PostBinding binding;

    @BeforeEach
    void setUp() {
        media = mock(MediaService.class);
        binding = new PostBinding(media);
    }

    @Test
    void theTypeContractIsThePostOne() {
        assertThat(binding.type()).isEqualTo(ContentType.POST);
        assertThat(binding.persistableClass()).isEqualTo(PersistablePost.class);
        assertThat(binding.newReadable()).isInstanceOf(ReadablePost.class);
        assertThat(binding.storefrontPath(ContentFixtures.content(1L, ContentType.POST, SLUG)))
                .isEqualTo(POST_PATH);
        assertThat(binding.subtitle(ContentFixtures.content(1L, ContentType.POST, SLUG), ContentFixtures.EN))
                .isEqualTo(POST_PATH);
    }

    @Test
    void tagsAreTrimmedDeduplicatedAndEmptiesDropped() {
        Content c = ContentFixtures.content(1L, ContentType.POST, SLUG);
        PersistablePost dto = new PersistablePost();
        dto.setTags(List.of(" news ", NEWS_SLUG, "  ", GUIDES_SLUG));

        binding.apply(c, dto);

        assertThat(PostBinding.meta(c).tags()).containsExactly(NEWS_SLUG, GUIDES_SLUG);
        assertThat(PostBinding.meta(c).categoryIds()).isEmpty();
    }

    @Test
    void aRowWithoutMetaReadsAsAnEmptyPostMeta() {
        PostMeta meta = PostBinding.meta(ContentFixtures.content(1L, ContentType.POST, SLUG));

        assertThat(meta.heroMediaId()).isNull();
        assertThat(meta.tags()).isEmpty();
        assertThat(meta.featured()).isFalse();
    }

    @Test
    void populateResolvesTheHeroUrlAndCopiesTheMeta() {
        Content c = ContentFixtures.published(1L, ContentType.POST, SLUG, HELLO_TITLE);
        c.setMeta(JsonCodec.write(new PostMeta(5L, List.of(2L), List.of(NEWS_SLUG), AUTHOR, true)));
        when(media.url(any(), org.mockito.ArgumentMatchers.eq(5L)))
                .thenReturn(Optional.of(DESKTOP_URL));
        ReadablePost dto = new ReadablePost();

        binding.populate(c, dto);

        assertThat(dto.getHeroMediaUrl()).isEqualTo(DESKTOP_URL);
        assertThat(dto.getCategoryIds()).containsExactly(2L);
        assertThat(dto.getTags()).containsExactly(NEWS_SLUG);
        assertThat(dto.getAuthorName()).isEqualTo(AUTHOR);
        assertThat(dto.isFeatured()).isTrue();
        assertThat(dto.getReadingMinutes()).isEqualTo(1);
    }

    @Test
    void aPostWithNoMetaListsPopulatesEmptyCollections() {
        Content c = ContentFixtures.published(1L, ContentType.POST, SLUG, HELLO_TITLE);
        c.setMeta(JsonCodec.write(new PostMeta(null, null, null, null, false)));
        when(media.url(any(), any())).thenReturn(Optional.empty());
        ReadablePost dto = new ReadablePost();

        binding.populate(c, dto);

        assertThat(dto.getCategoryIds()).isEmpty();
        assertThat(dto.getTags()).isEmpty();
        assertThat(dto.getHeroMediaUrl()).isNull();
    }

    @Test
    void theReadingEstimateComesFromTheLongestLocale() {
        Content c = ContentFixtures.content(1L, ContentType.POST, SLUG);
        c.getDescriptions().add(ContentFixtures.description(c, ContentFixtures.EN, HELLO_TITLE,
                String.format("<p>%s</p>", "word ".repeat(440))));
        c.getDescriptions().add(ContentFixtures.description(c, ContentFixtures.AR, "مرحبا", "<p>كلمة</p>"));

        assertThat(PostBinding.readingMinutes(c)).isEqualTo(2);
    }

    @Test
    void anEmptyOrAbsentBodyReadsAsZeroMinutes() {
        Content empty = ContentFixtures.content(1L, ContentType.POST, SLUG);
        assertThat(PostBinding.readingMinutes(empty)).isZero();

        empty.getDescriptions().add(ContentFixtures.description(empty, ContentFixtures.EN, HELLO_TITLE, "<p></p>"));
        assertThat(PostBinding.readingMinutes(empty)).isZero();

        Content nullBody = ContentFixtures.content(2L, ContentType.POST, SLUG);
        nullBody.getDescriptions().add(ContentFixtures.description(nullBody, ContentFixtures.EN, HELLO_TITLE, null));
        assertThat(PostBinding.readingMinutes(nullBody)).isZero();
    }

    @Test
    void onlyASetHeroJoinsTheMediaUsageIndex() {
        Content c = ContentFixtures.content(1L, ContentType.POST, SLUG);
        assertThat(binding.mediaReferences(c)).isEmpty();

        c.setMeta(JsonCodec.write(new PostMeta(5L, List.of(), List.of(), null, false)));
        assertThat(binding.mediaReferences(c)).containsExactly(Map.entry("hero", 5L));
    }

}
