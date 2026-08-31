import type {ComponentType} from 'react';
import type {SectionKind} from '@store-front/types';
import type {SectionRenderProps} from '@store-front/theme';
import {heroFallback} from './hero';
import {productsFallback} from './products';
import {categoriesFallback} from './categories';
import {promoFallback} from './promo';
import {imageFallback} from './image';
import {richtextFallback} from './richtext';
import {faqFallback} from './faq';
import {postsFallback} from './posts';
import {testimonialsFallback} from './testimonials';
import {Newsletter} from './newsletter';
import {uspFallback} from './usp';
import {videoFallback} from './video';
import {brandsFallback} from './brands';

/**
 * The shell's renderer for every kind and variant in the catalogue. This is why the builder can promise that
 * any layout renders on any theme: a theme overrides per kind via its `sections` registry, and everything
 * else lands here.
 */
export const FALLBACK_SECTIONS: Record<SectionKind, Record<string, ComponentType<SectionRenderProps>>> = {
    hero: heroFallback,
    products: productsFallback,
    categories: categoriesFallback,
    promo: promoFallback,
    image: imageFallback,
    richtext: richtextFallback,
    faq: faqFallback,
    posts: postsFallback,
    testimonials: testimonialsFallback,
    // Built here, not in the client module: an OBJECT exported from a 'use client' file arrives
    // on the server as an opaque client reference whose properties read as undefined.
    newsletter: {inline: Newsletter, boxed: Newsletter},
    usp: uspFallback,
    video: videoFallback,
    brands: brandsFallback,
};
