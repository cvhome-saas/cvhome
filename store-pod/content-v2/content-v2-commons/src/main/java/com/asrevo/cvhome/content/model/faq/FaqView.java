package com.asrevo.cvhome.content.model.faq;

import java.util.List;

import com.asrevo.cvhome.content.model.ContentView;

public record FaqView(ContentView content, Long groupId, int position, List<FaqReferenceSpec> references) {
}
