package com.asrevo.cvhome.commons.domain;

import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;

/**
 * The storefront theme a store runs. Stored as a string on {@code merchant.merchant_store.theme} and
 * handed to landing-ui as the {@code Theme} routing header by {@code MerchantRoutingService}.
 *
 * {@code implemented} is narrower than "landing-ui can render it": it means a designed theme stands
 * behind the value, and only these are offered for selection ({@link #getImplementedThemes()}). The
 * rest still resolve — through the registry or landing-ui's {@code LEGACY_THEME_MAP} — so a store
 * already set to one keeps loading, and {@code ?theme=&lt;id&gt;} still previews them wherever the storefront
 * allows previews. See {@code store-pod/landing-ui/themes/README.md} for what each one is.
 *
 * Promoting a theme to selectable is three edits, all needed: {@code implemented=true} here, the value
 * in the {@code merchant_store.theme} check constraint
 * ({@code merchant-service/src/main/resources/init-sql/schema.sql}), and a {@code legacy-theme-map.ts}
 * entry in landing-ui when the enum name is not the theme id.
 */
@AllArgsConstructor
public enum Theme {

    /* Designed themes, one package each under themes/<lowercased name>. */
    BASIC(true), BEAUTY(true), FASHION(true), FURNITURE(true), GROCERY(true), HUNGER(true), PINK(true),

    /* Scaffolded but not yet designed: `npm run new-theme` output — starter with a generated palette.
     * Declared so they can be previewed and finished; not offered while they are still starter clones. */
    COSMETICS(false), GLASSES(false), JEWELLERY(false), SPORTS(false),

    /* No package of their own; every one resolves to `starter`. DEFAULT names that outcome; the rest are
     * legacy values kept so existing rows still read. JEWELERY is the old misspelling and is not an alias
     * for JEWELLERY — it predates that theme. */
    DEFAULT(false), BASIS(false), MODERN(false), JEWELERY(false), ELECTRONICS(false), FOOD(false),
    WATCHES(false), BABY(false), TOOLS(false);

    private final boolean implemented;

    public static List<Theme> getImplementedThemes() {
        return Arrays.stream(Theme.values()).filter(theme -> theme.implemented).toList();
    }

}
