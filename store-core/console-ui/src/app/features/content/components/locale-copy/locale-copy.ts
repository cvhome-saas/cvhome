import {Component, computed, inject, input, model} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import type {ReferenceOption} from '@core/reference/reference-data.service';
import {FormField} from '@shared/ui/form-field/form-field';
import {LocaleSwitcher} from '@shared/ui/locale-switcher/locale-switcher';
import {NoticeBar} from '@shared/ui/notice-bar/notice-bar';
import {Panel} from '@shared/ui/panel/panel';
import {RichText} from '@shared/ui/rich-text/rich-text';
import {TextField} from '@shared/ui/text-field/text-field';
import {TextareaField} from '@shared/ui/textarea-field/textarea-field';
import type {TranslationForm} from '../../services/content-editor-form.service';

/** Which of the translation's fields this editor shows, and what it calls them. */
export interface CopyFields {
  readonly titleKey: string;
  readonly bodyKey?: string;
  readonly excerptKey?: string;
  readonly subtitleKey?: string;
  readonly ctaLabelKey?: string;
  readonly altTextKey?: string;
  /** Rich text (pages, posts, FAQ answers, policies) or a plain textarea (banners have no body). */
  readonly richBody?: boolean;
}

/**
 * The per-language copy of a content item — the design's "Content" card: a locale strip, then the
 * fields the type needs, then the fallback note. One `TranslationForm` per language; switching the
 * strip swaps which group is bound without losing what was typed in the others.
 */
@Component({
  selector: 'app-locale-copy',
  imports: [FormField, LocaleSwitcher, NoticeBar, Panel, ReactiveFormsModule, RichText, TextField, TextareaField, TranslocoDirective],
  template: `
    <app-panel [title]="t('content.copy.title')" [subtitle]="t('content.copy.subtitle')" padded *transloco="let t">
      <app-locale-switcher
        panelAction
        [languages]="locales()"
        [(active)]="language"
        [filled]="written()"
        display="label"
        [label]="t('content.copy.writeIn')"
      />

      <div class="field-grid" [formGroup]="form()">
        <app-form-field wide [label]="t(fields().titleKey)" [control]="form().controls.title" required [controlId]="id('title')">
          <app-text-field [id]="id('title')" formControlName="title" [maxLength]="120" />
        </app-form-field>

        @if (fields().subtitleKey; as key) {
          <app-form-field wide [label]="t(key)" [control]="form().controls.subtitle" [controlId]="id('subtitle')">
            <app-text-field [id]="id('subtitle')" formControlName="subtitle" [maxLength]="300" />
          </app-form-field>
        }

        @if (fields().excerptKey; as key) {
          <app-form-field wide [label]="t(key)" [hint]="t('content.copy.excerptHint')" [control]="form().controls.excerpt" [controlId]="id('excerpt')">
            <app-textarea [id]="id('excerpt')" formControlName="excerpt" [rows]="2" [maxLength]="300" />
          </app-form-field>
        }

        @if (fields().bodyKey; as key) {
          <app-form-field wide [label]="t(key)" [control]="form().controls.body" [required]="!!fields().richBody" [controlId]="id('body')">
            @if (fields().richBody) {
              <app-rich-text [id]="id('body')" formControlName="body" [ariaLabel]="t(key)" />
            } @else {
              <app-textarea [id]="id('body')" formControlName="body" [rows]="3" autoGrow />
            }
          </app-form-field>
        }

        @if (fields().ctaLabelKey; as key) {
          <app-form-field [label]="t(key)" [control]="form().controls.ctaLabel" [controlId]="id('cta')">
            <app-text-field [id]="id('cta')" formControlName="ctaLabel" [maxLength]="60" />
          </app-form-field>
        }

        @if (fields().altTextKey; as key) {
          <app-form-field [label]="t(key)" [hint]="t('content.copy.altHint')" [control]="form().controls.altText" [controlId]="id('alt')">
            <app-text-field [id]="id('alt')" formControlName="altText" [maxLength]="255" />
          </app-form-field>
        }

        <app-notice-bar class="field-wide" tone="blue" icon="alertCircle" [message]="t('content.copy.fallbackNotice', {language: defaultName()})" />
      </div>
    </app-panel>
  `,
  styleUrls: ['../../../../shared/styles/field.css'],
})
export class LocaleCopy {
  private readonly transloco = inject(TranslocoService);

  readonly forms = input.required<Readonly<Record<string, TranslationForm>>>();
  readonly locales = input.required<readonly ReferenceOption[]>();
  readonly defaultLocale = input.required<string>();
  readonly written = input.required<ReadonlySet<string>>();
  readonly fields = input.required<CopyFields>();
  readonly language = model.required<string>();

  protected readonly form = computed(() => this.forms()[this.language()] ?? Object.values(this.forms())[0]);
  protected readonly defaultName = computed(
    () => this.locales().find((l) => l.code === this.defaultLocale())?.label ?? this.defaultLocale(),
  );

  protected id(field: string): string {
    return `copy-${field}-${this.language()}`;
  }
}
