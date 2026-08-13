package com.asrevo.cvhome.content.model.faq;

import com.asrevo.cvhome.commons.domain.LanguageCode;

public record FaqGroupView(Long id, String code, int position, LanguageCode language, String name) {
}
