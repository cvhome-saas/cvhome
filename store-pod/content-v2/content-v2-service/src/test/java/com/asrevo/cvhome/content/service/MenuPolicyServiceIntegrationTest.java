package com.asrevo.cvhome.content.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.TestcontainersConfiguration;
import com.asrevo.cvhome.content.errors.InvalidMenuTreeException;
import com.asrevo.cvhome.content.errors.PublishedPolicyImmutableException;
import com.asrevo.cvhome.content.model.ContentType;
import com.asrevo.cvhome.content.model.ContentWriteRequest;
import com.asrevo.cvhome.content.model.menu.MenuItemSpec;
import com.asrevo.cvhome.content.model.menu.MenuTargetKind;
import com.asrevo.cvhome.content.model.menu.MenuView;
import com.asrevo.cvhome.content.model.menu.MenuWriteRequest;
import com.asrevo.cvhome.content.model.policy.PolicyDisplayLocation;
import com.asrevo.cvhome.content.model.policy.PolicyType;
import com.asrevo.cvhome.content.model.policy.PolicyView;
import com.asrevo.cvhome.content.model.policy.PolicyWriteRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Tag("integration-test")
@Transactional
class MenuPolicyServiceIntegrationTest {
    private static final StoreMerchantId STORE = new StoreMerchantId("phase-six-store");
    private static final StoreMerchantId OTHER_STORE = new StoreMerchantId("phase-six-other-store");
    private static final LanguageCode ENGLISH = LanguageCode.defaultLanguage();
    private static final String ACTOR = "phase-six-test";
    private static final String BROKEN_CONTENT_ID = "999999";
    private static final String MENU_HANDLE = "main";
    private static final String SECOND_POLICY_VERSION = "2.0";

    @Autowired
    private MenuService menuService;
    @Autowired
    private PolicyService policyService;
    @Autowired
    private EntityManager entityManager;

    @Test
    void replacesMenuAtomicallyAndReportsBrokenContentReferences() throws Exception {
        MenuView created = menuService.create(STORE, ENGLISH, menuRequest(validItems()), ACTOR);

        assertThat(created.items()).hasSize(1);
        assertThat(created.items().getFirst().children()).hasSize(1);
        assertThat(created.brokenReferences()).containsExactly(BROKEN_CONTENT_ID);
        assertThatThrownBy(() -> menuService.replace(STORE, ENGLISH, created.content().id(),
                menuRequest(overDepthItems()))).isInstanceOf(InvalidMenuTreeException.class);
        assertThat(menuService.find(STORE, ENGLISH, MENU_HANDLE).items()).hasSize(1);
        assertThatThrownBy(() -> menuService.find(OTHER_STORE, ENGLISH, MENU_HANDLE))
                .isInstanceOf(com.asrevo.cvhome.content.errors.ContentNotFoundException.class);
    }

    @Test
    void publishingNewPolicyDeactivatesPriorVersionAndPublishedVersionIsImmutable() throws Exception {
        PolicyView first = policyService.create(STORE, ENGLISH, policyRequest("1.0"), ACTOR);
        PolicyView firstPublished = policyService.publish(STORE, first.content().id(), first.content().version(),
                ACTOR);
        entityManager.flush();
        entityManager.clear();
        PolicyView second = policyService.create(STORE, ENGLISH, policyRequest(SECOND_POLICY_VERSION), ACTOR);
        PolicyView secondPublished = policyService.publish(STORE, second.content().id(), second.content().version(),
                ACTOR);

        assertThat(firstPublished.active()).isTrue();
        assertThat(secondPublished.active()).isTrue();
        assertThat(policyService.list(STORE, PolicyType.PRIVACY)).filteredOn(PolicyView::active)
                .singleElement().extracting(PolicyView::policyVersion).isEqualTo(SECOND_POLICY_VERSION);
        assertThatThrownBy(() -> policyService.publish(STORE, second.content().id(),
                secondPublished.content().version(), ACTOR))
                .isInstanceOf(PublishedPolicyImmutableException.class);
        assertThat(policyService.list(OTHER_STORE, PolicyType.PRIVACY)).isEmpty();
    }

    private static MenuWriteRequest menuRequest(List<MenuItemSpec> items) {
        return new MenuWriteRequest(content("main-menu", ContentType.MENU), MENU_HANDLE, items);
    }

    private static List<MenuItemSpec> validItems() {
        MenuItemSpec child = new MenuItemSpec("Account", MenuTargetKind.URL, "/account", false, true, true,
                List.of());
        return List.of(new MenuItemSpec("Help", MenuTargetKind.CONTENT, BROKEN_CONTENT_ID, false, true, false,
                List.of(child)));
    }

    private static List<MenuItemSpec> overDepthItems() {
        MenuItemSpec grandchild = new MenuItemSpec("Third", MenuTargetKind.URL, "/third", false, true, false,
                List.of());
        MenuItemSpec child = new MenuItemSpec("Second", MenuTargetKind.URL, "/second", false, true, false,
                List.of(grandchild));
        return List.of(new MenuItemSpec("First", MenuTargetKind.URL, "/first", false, true, false,
                List.of(child)));
    }

    private static PolicyWriteRequest policyRequest(String version) {
        return new PolicyWriteRequest(content("privacy-%s".formatted(version), ContentType.POLICY),
                PolicyType.PRIVACY, version, LocalDate.of(2026, 8, 13), true, "SA",
                Set.of(PolicyDisplayLocation.FOOTER, PolicyDisplayLocation.CHECKOUT));
    }

    private static ContentWriteRequest content(String code, ContentType type) {
        return new ContentWriteRequest(code, type, code, code, "body", code, null, null, null, null,
                false, null, null);
    }
}
