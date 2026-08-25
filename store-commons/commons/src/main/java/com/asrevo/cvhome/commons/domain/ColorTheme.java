package com.asrevo.cvhome.commons.domain;

/**
 * The merchant's storefront colour theme.
 * <p>
 * {@link #DEFAULT} is not a palette: it means "the storefront theme's own default palette" — every theme
 * (see {@code store-pod/landing-ui/themes/<id>/src/colors.ts}) ships the colours it was designed for. Every
 * other value is a fixed preset ({@code store-pod/landing-ui/libs/types/src/color-schema.ts}) that replaces
 * the theme default whole. Names mirror the TypeScript {@code ColorTheme} enum exactly.
 */
public enum ColorTheme {

    DEFAULT, LIGHT, DARK, NATURE, OCEAN, MIDNIGHT, FOREST_WHISPER, DESERT_MIRAGE, MIDNIGHT_DUSK, ROSE, LAVENDER,
    AURORA_LIGHTS, CYBERPUNK, AUTUMN_HARVEST, CYBER_NEON, SUNSET, FOREST, DESERT, SKY, EARTH, FIRE, ICE, BLOSSOM, GOLDEN,
    GRAPE, PEACH, MINT, SAND, RAINBOW, NEON, PASTEL

}
