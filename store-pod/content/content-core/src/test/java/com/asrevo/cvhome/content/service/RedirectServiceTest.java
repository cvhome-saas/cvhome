package com.asrevo.cvhome.content.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.content.ContentFixtures;
import com.asrevo.cvhome.content.entity.Redirect;
import com.asrevo.cvhome.content.repository.RedirectRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Storefront paths that moved. The new path must never redirect anywhere itself, or a slug that swings back and
 * forth would build a loop.
 */
class RedirectServiceTest {

    private static final String STORE_ID = ContentFixtures.STORE.getId();

    private static final String OLD_PATH = "/content/about";

    private static final String NEW_PATH = "/content/about-us";

    private RedirectRepository repository;

    private RedirectService service;

    @BeforeEach
    void setUp() {
        repository = mock(RedirectRepository.class);
        service = new RedirectService(repository);
    }

    @Test
    void aMoveToTheSamePathOrFromNowhereWritesNothing() {
        service.moved(ContentFixtures.STORE, null, NEW_PATH);
        service.moved(ContentFixtures.STORE, NEW_PATH, NEW_PATH);

        verify(repository, never()).save(any());
    }

    @Test
    void aMoveClearsAnyRedirectOffTheNewPathFirst() {
        when(repository.findByStoreMerchantIdAndFromPath(STORE_ID, OLD_PATH)).thenReturn(Optional.empty());

        service.moved(ContentFixtures.STORE, OLD_PATH, NEW_PATH);

        verify(repository).deleteByStoreMerchantIdAndFromPath(STORE_ID, NEW_PATH);
        var captor = org.mockito.ArgumentCaptor.forClass(Redirect.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getFromPath()).isEqualTo(OLD_PATH);
        assertThat(captor.getValue().getToPath()).isEqualTo(NEW_PATH);
    }

    @Test
    void movingBackRewritesTheExistingRowRatherThanAddingOne() {
        Redirect existing = new Redirect();
        existing.setId(1L);
        existing.setStoreMerchantId(STORE_ID);
        existing.setFromPath(OLD_PATH);
        existing.setToPath("/content/somewhere-else");
        when(repository.findByStoreMerchantIdAndFromPath(STORE_ID, OLD_PATH)).thenReturn(Optional.of(existing));

        service.moved(ContentFixtures.STORE, OLD_PATH, NEW_PATH);

        assertThat(existing.getToPath()).isEqualTo(NEW_PATH);
        verify(repository).save(existing);
    }

    @Test
    void resolvingAPathThatNeverMovedIsEmpty() {
        when(repository.findByStoreMerchantIdAndFromPath(STORE_ID, OLD_PATH)).thenReturn(Optional.empty());

        assertThat(service.resolve(ContentFixtures.STORE, OLD_PATH)).isEmpty();
    }

    @Test
    void theListIsWhateverTheStoreHas() {
        when(repository.findByStoreMerchantIdOrderByCreatedAtDesc(STORE_ID)).thenReturn(List.of(new Redirect()));

        assertThat(service.list(ContentFixtures.STORE)).hasSize(1);
    }

}
