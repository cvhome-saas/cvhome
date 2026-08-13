package com.asrevo.cvhome.content.model.menu;

import java.util.List;

import com.asrevo.cvhome.content.model.ContentView;

public record MenuView(ContentView content, String handle, List<MenuItemSpec> items,
                       List<String> brokenReferences) {
}
