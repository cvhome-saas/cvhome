package com.asrevo.cvhome.content.api.v1.support;

/**
 * The {@code @PreAuthorize} expressions of the private content API. Writes need manage access on the store; reads
 * are open to anyone with read access (moderators included).
 */
public final class ContentPermissions {

    public static final String MANAGE = "hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CONTENT.*')";

    public static final String READ = "hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CONTENT.READ')";

    /**
     * Pod-internal writes to the media usage index. A separate token because {@link #MANAGE} resolves to
     * "org or store admin", which a calling service is not.
     */
    public static final String MEDIA_USAGE =
            "hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CONTENT.MEDIA-USAGE')";

    private ContentPermissions() {
    }

}
