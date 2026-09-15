package com.asrevo.cvhome.content.aot;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/**
 * Puts the starter layout's copy ({@code layout-defaults/messages*.properties}, read by {@code LayoutDefaults} as
 * classpath resources) in a native image; imported by the service that builds starter layouts.
 */
public class LayoutCopyRuntimeHints implements RuntimeHintsRegistrar {

    static final String COPY_FILES = "layout-defaults/*.properties";

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.resources().registerPattern(COPY_FILES);
    }

}
