import {Injectable, inject} from '@angular/core';
import {FormControl, FormGroup, NonNullableFormBuilder, Validators} from '@angular/forms';
import {map} from 'rxjs';

import {ContentItemsService} from '@api/content/content-items.service';
import type {ContentListType, ContentTranslation, PersistableContent} from '@models/content';
import {uniqueAsync} from '@shared/forms/unique-async';

export const SLUG_PATTERN = /^[a-z0-9]+(?:-[a-z0-9]+)*$/;

/** One locale's copy. Which controls a given editor shows is the editor's business; the group carries them all. */
export interface TranslationForm extends FormGroup<{
  title: FormControl<string>;
  body: FormControl<string>;
  excerpt: FormControl<string>;
  metaTitle: FormControl<string>;
  metaDescription: FormControl<string>;
  keywords: FormControl<string>;
  altText: FormControl<string>;
  ctaLabel: FormControl<string>;
  subtitle: FormControl<string>;
  friendlyUrl: FormControl<string>;
}> {}

/** The fields every workflow item shares on write. */
export interface CommonForm extends FormGroup<{
  slug: FormControl<string>;
  publishAt: FormControl<string>;
  unpublishAt: FormControl<string>;
  noindex: FormControl<boolean>;
  canonicalUrl: FormControl<string>;
  ogMediaId: FormControl<number | null>;
}> {}

/**
 * Builds and maps the forms every content editor is made of: one `CommonForm`, one
 * `TranslationForm` per storefront language, and whatever type-specific group the editor adds.
 */
@Injectable({providedIn: 'root'})
export class ContentEditorFormService {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly api = inject(ContentItemsService);

  common(type: ContentListType, currentId: () => number | null): CommonForm {
    return this.fb.group({
      slug: this.fb.control('', {
        validators: [
          Validators.required,
          Validators.maxLength(100),
          Validators.pattern(SLUG_PATTERN),
        ],
        asyncValidators: [
          uniqueAsync(
            (slug) =>
              this.api
                .slugAvailable(type, slug, currentId() ?? undefined)
                .pipe(map((r) => !r.exists)),
            'slugTaken',
          ),
        ],
      }),
      publishAt: this.fb.control(''),
      unpublishAt: this.fb.control(''),
      noindex: this.fb.control(false),
      canonicalUrl: this.fb.control('', {validators: [Validators.maxLength(500)]}),
      ogMediaId: this.fb.control<number | null>(null),
    });
  }

  translation(): TranslationForm {
    return this.fb.group({
      title: this.fb.control('', {validators: [Validators.maxLength(120)]}),
      body: this.fb.control(''),
      excerpt: this.fb.control('', {validators: [Validators.maxLength(300)]}),
      metaTitle: this.fb.control('', {validators: [Validators.maxLength(255)]}),
      metaDescription: this.fb.control('', {validators: [Validators.maxLength(255)]}),
      keywords: this.fb.control('', {validators: [Validators.maxLength(255)]}),
      altText: this.fb.control('', {validators: [Validators.maxLength(255)]}),
      ctaLabel: this.fb.control('', {validators: [Validators.maxLength(60)]}),
      subtitle: this.fb.control('', {validators: [Validators.maxLength(300)]}),
      friendlyUrl: this.fb.control('', {validators: [Validators.maxLength(120)]}),
    });
  }

  /** One group per code, keeping the groups that already exist so typed copy survives a language list change. */
  translations(
    codes: readonly string[],
    existing: Readonly<Record<string, TranslationForm>> = {},
  ): Record<string, TranslationForm> {
    const out: Record<string, TranslationForm> = {};
    for (const code of codes) {
      out[code] = existing[code] ?? this.translation();
    }
    return out;
  }

  fillCommon(form: CommonForm, item: PersistableContent): void {
    form.reset({
      slug: item.slug,
      publishAt: item.publishAt ?? '',
      unpublishAt: item.unpublishAt ?? '',
      noindex: !!item.noindex,
      canonicalUrl: item.canonicalUrl ?? '',
      ogMediaId: item.ogMediaId ?? null,
    });
  }

  fillTranslations(
    forms: Readonly<Record<string, TranslationForm>>,
    translations: readonly ContentTranslation[],
  ): void {
    const byCode = new Map(translations.map((t) => [t.language, t]));
    for (const [code, form] of Object.entries(forms)) {
      const t = byCode.get(code);
      form.reset({
        title: t?.title ?? '',
        body: t?.body ?? '',
        excerpt: t?.excerpt ?? '',
        metaTitle: t?.metaTitle ?? '',
        metaDescription: t?.metaDescription ?? '',
        keywords: t?.keywords ?? '',
        altText: t?.altText ?? '',
        ctaLabel: t?.ctaLabel ?? '',
        subtitle: t?.subtitle ?? '',
        friendlyUrl: t?.friendlyUrl ?? '',
      });
    }
  }

  /** The body's translations: every language with a title or body; the server drops empty ones anyway. */
  toTranslations(
    forms: Readonly<Record<string, TranslationForm>>,
    existing: readonly ContentTranslation[] = [],
  ): ContentTranslation[] {
    const ids = new Map(existing.map((t) => [t.language, t.id]));
    const states = new Map(existing.map((t) => [t.language, t.state]));
    return Object.entries(forms)
      .map(([language, form]) => {
        const v = form.getRawValue();
        return {
          id: ids.get(language),
          language,
          state: states.get(language),
          title: v.title.trim(),
          body: v.body,
          excerpt: v.excerpt.trim() || undefined,
          metaTitle: v.metaTitle.trim() || undefined,
          metaDescription: v.metaDescription.trim() || undefined,
          keywords: v.keywords.trim() || undefined,
          altText: v.altText.trim() || undefined,
          ctaLabel: v.ctaLabel.trim() || undefined,
          subtitle: v.subtitle.trim() || undefined,
          friendlyUrl: v.friendlyUrl.trim() || undefined,
        };
      })
      .filter((t) => t.title.length > 0 || (t.body ?? '').trim().length > 0);
  }

  toCommon(form: CommonForm): Omit<PersistableContent, 'translations'> {
    const v = form.getRawValue();
    return {
      slug: v.slug.trim(),
      publishAt: v.publishAt || null,
      unpublishAt: v.unpublishAt || null,
      noindex: v.noindex,
      canonicalUrl: v.canonicalUrl.trim() || null,
      ogMediaId: v.ogMediaId,
    };
  }
}
