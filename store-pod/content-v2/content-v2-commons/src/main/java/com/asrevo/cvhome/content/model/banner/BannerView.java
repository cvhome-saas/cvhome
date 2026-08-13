package com.asrevo.cvhome.content.model.banner;

import java.util.Set;

import com.asrevo.cvhome.content.model.ContentView;

public record BannerView(
        ContentView content,
        BannerPlacement placement,
        int position,
        BannerTargetKind targetKind,
        String targetValue,
        String backgroundColor,
        String foregroundColor,
        LoginTarget loginTarget,
        Set<String> countryCodes,
        BannerArtworkSpec artwork
) {
}
