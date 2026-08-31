package com.asrevo.cvhome.content.model.layout;

/**
 * Which storefront page a layout document arranges. Only the home page is buildable today; category and product
 * layouts would join here, which is why the layout tables key on {@code (store, page)} rather than assuming home.
 */
public enum PageKind {

    HOME

}
