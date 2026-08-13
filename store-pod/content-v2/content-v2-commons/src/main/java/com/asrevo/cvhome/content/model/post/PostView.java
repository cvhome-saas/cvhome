package com.asrevo.cvhome.content.model.post;

import com.asrevo.cvhome.content.model.ContentView;

public record PostView(ContentView content, String excerpt, Long heroMediaId, String author, int readingMinutes,
                       boolean featured) {
}
