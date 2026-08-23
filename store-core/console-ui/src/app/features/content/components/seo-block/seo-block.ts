import {Component, computed, inject, input} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {FormField} from '@shared/ui/form-field/form-field';
import {Panel} from '@shared/ui/panel/panel';
import {TextField} from '@shared/ui/text-field/text-field';
import {TextareaField} from '@shared/ui/textarea-field/textarea-field';
import {Toggle} from '@shared/ui/toggle/toggle';
import type {CommonForm, TranslationForm} from '../../services/content-editor-form.service';

/**
 * "URL & search" — the slug with its live availability check, the active language's meta title,
 * meta description and keywords, the canonical URL and the index toggle, and a search-result
 * preview built from what is typed.
 */
@Component({
  selector: 'app-seo-block',
  imports: [FormField, Panel, ReactiveFormsModule, TextField, TextareaField, Toggle, TranslocoDirective],
  template: `
    <app-panel [title]="t('content.seo.title')" [subtitle]="t('content.seo.subtitle')" padded *transloco="let t">
      <div class="field-grid">
        <app-form-field
          [label]="t('content.seo.slug')"
          [hint]="t('content.seo.slugHint', {prefix: pathPrefix()})"
          [control]="common().controls.slug"
          required
          controlId="seo-slug"
        >
          <div [formGroup]="common()">
            <app-text-field id="seo-slug" formControlName="slug" latin mono [prefix]="pathPrefix()" [maxLength]="100"
                            [check]="slugCheck()" [checkLabel]="slugCheckLabel()" />
          </div>
        </app-form-field>

        <app-form-field [label]="t('content.seo.metaTitle')" [hint]="t('content.seo.metaTitleHint')" [control]="translation().controls.metaTitle" controlId="seo-meta-title">
          <div [formGroup]="translation()">
            <app-text-field id="seo-meta-title" formControlName="metaTitle" [maxLength]="255" />
          </div>
        </app-form-field>

        <app-form-field wide [label]="t('content.seo.metaDescription')" [hint]="t('content.seo.metaDescriptionHint')" [control]="translation().controls.metaDescription" controlId="seo-meta-description">
          <div [formGroup]="translation()">
            <app-textarea id="seo-meta-description" formControlName="metaDescription" [rows]="2" [recommendedMin]="50" [recommendedMax]="160" [maxLength]="255" />
          </div>
        </app-form-field>

        <app-form-field [label]="t('content.seo.keywords')" [hint]="t('content.seo.keywordsHint')" [control]="translation().controls.keywords" controlId="seo-keywords">
          <div [formGroup]="translation()">
            <app-text-field id="seo-keywords" formControlName="keywords" [maxLength]="255" />
          </div>
        </app-form-field>

        <app-form-field [label]="t('content.seo.canonical')" [hint]="t('content.seo.canonicalHint')" [control]="common().controls.canonicalUrl" controlId="seo-canonical">
          <div [formGroup]="common()">
            <app-text-field id="seo-canonical" formControlName="canonicalUrl" type="url" latin [maxLength]="500" />
          </div>
        </app-form-field>

        <div class="field field-wide" [formGroup]="common()">
          <app-toggle formControlName="noindex" [label]="t('content.seo.noindex')" [description]="t('content.seo.noindexHint')" />
        </div>

        <div class="serp field-wide" aria-live="polite">
          <span class="serp-url" dir="ltr">{{ t('content.seo.serpHost') }} › {{ pathPrefix() }}{{ common().controls.slug.value || '…' }}</span>
          <span class="serp-title" dir="auto">{{ translation().controls.metaTitle.value || fallbackTitle() || t('content.seo.serpTitlePlaceholder') }}</span>
          <span class="serp-desc" dir="auto">{{ translation().controls.metaDescription.value || t('content.seo.serpDescriptionPlaceholder') }}</span>
        </div>
      </div>
    </app-panel>
  `,
  styleUrls: ['../../../../shared/styles/field.css', './seo-block.css'],
})
export class SeoBlock {
  private readonly transloco = inject(TranslocoService);

  readonly common = input.required<CommonForm>();
  readonly translation = input.required<TranslationForm>();
  /** `/content/` for pages, `/blog/` for posts — what the serp line and the slug hint show. */
  readonly pathPrefix = input('/content/');
  readonly fallbackTitle = input('');

  protected readonly slugCheck = computed(() => {
    const control = this.common().controls.slug;
    if (!control.value) {
      return 'idle' as const;
    }
    if (control.pending) {
      return 'pending' as const;
    }
    if (control.hasError('slugTaken')) {
      return 'taken' as const;
    }
    return control.valid ? ('free' as const) : ('idle' as const);
  });

  protected readonly slugCheckLabel = computed(() => {
    this.transloco.activeLang();
    const check = this.slugCheck();
    return check === 'idle' ? null : this.transloco.translate(`content.seo.slugCheck.${check}`);
  });
}
