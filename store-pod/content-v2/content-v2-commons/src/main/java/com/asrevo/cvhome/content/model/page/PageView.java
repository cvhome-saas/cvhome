package com.asrevo.cvhome.content.model.page;

import java.util.List;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.content.model.ContentView;

public record PageView(ContentView content, String template, boolean showInSitemap, Long parentPageId,
                       List<PageBlockSpec> blocks, LanguageCode requestedLanguage, LanguageCode resolvedLanguage,
                       boolean fallback) {
}
