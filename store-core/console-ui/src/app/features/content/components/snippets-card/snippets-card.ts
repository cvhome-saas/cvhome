import {Component, computed, inject, input, signal} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {ContentCache} from '@api/content/content-cache';
import {SnippetsService} from '@api/content/snippets.service';
import {ApiErrorService} from '@core/errors/api-error.service';
import type {ReferenceOption} from '@core/reference/reference-data.service';
import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import type {ContentTranslation, Snippet} from '@models/content';
import {Badge} from '@shared/ui/badge/badge';
import {Icon} from '@shared/ui/icon/icon';
import {LocaleSwitcher} from '@shared/ui/locale-switcher/locale-switcher';
import {Panel} from '@shared/ui/panel/panel';
import {RichText} from '@shared/ui/rich-text/rich-text';
import {TextField} from '@shared/ui/text-field/text-field';
import {ToastService} from '@shared/ui/toast/toast';
import {Toggle} from '@shared/ui/toggle/toggle';

/** The snippet codes the storefront reads, in the order they matter. */
const KNOWN_CODES = ['meta-title', 'meta-description', 'header-message', 'agreement'] as const;

interface Draft {
  code: string;
  visible: boolean;
  translations: Record<string, {title: string; body: string}>;
}

/**
 * "Store snippets" — the legacy content boxes the storefront reads by code: the site's meta title and
 * description, the announcement bar's fallback text and the checkout agreement. Edited inline, per
 * language, and saved with `PUT /snippets/{code}` (an upsert). Any other code the store already has
 * is listed too, so nothing in an existing database becomes unreachable.
 */
@Component({
  selector: 'app-snippets-card',
  imports: [Badge, Icon, LocaleSwitcher, Panel, RichText, TextField, Toggle, TranslocoDirective],
  template: `
    <app-panel
      [title]="t('content.snippets.title')"
      [subtitle]="t('content.snippets.subtitle')"
      *transloco="let t"
    >
      <ul class="snippet-list">
        @for (code of codes(); track code) {
          <li [class.open]="openCode() === code">
            <button
              class="snippet-row"
              type="button"
              (click)="toggle(code)"
              [attr.aria-expanded]="openCode() === code"
            >
              <span class="snippet-copy">
                <strong dir="ltr">{{ code }}</strong>
                <small>{{ describe(code) }}</small>
              </span>
              @if (byCode().get(code); as snippet) {
                <app-badge [tone]="snippet.visible ? 'green' : 'slate'" shape="square">{{
                  snippet.visible ? t('content.snippets.live') : t('content.snippets.hidden')
                }}</app-badge>
              } @else {
                <app-badge tone="amber" shape="square">{{
                  t('content.snippets.notSet')
                }}</app-badge>
              }
              <app-icon
                [name]="openCode() === code ? 'chevronDown' : 'chevronRight'"
                [size]="14"
                [flip]="openCode() !== code"
              />
            </button>
            @if (openCode() === code && draft(); as d) {
              <div class="snippet-editor">
                <div class="editor-head">
                  <app-locale-switcher
                    [languages]="locales()"
                    [(active)]="language"
                    [filled]="filled()"
                    display="label"
                    [label]="t('content.copy.writeIn')"
                  />
                  <app-toggle
                    [checked]="d.visible"
                    (checkedChange)="setVisible($event)"
                    [label]="t('content.snippets.visible')"
                    [disabled]="!canManage()"
                  />
                </div>
                <label class="field">
                  <span class="field-label">{{ t('content.snippets.titleField') }}</span>
                  <app-text-field
                    [value]="d.translations[language()]?.title ?? ''"
                    (valueChange)="setTitle($event)"
                    [maxLength]="120"
                    [disabled]="!canManage()"
                  />
                </label>
                <div class="field">
                  <span class="field-label">{{ t('content.snippets.bodyField') }}</span>
                  <app-rich-text
                    [value]="d.translations[language()]?.body ?? ''"
                    (valueChange)="setBody($event)"
                    [disabled]="!canManage()"
                    [ariaLabel]="t('content.snippets.bodyField')"
                  />
                </div>
                @if (canManage()) {
                  <div class="editor-actions">
                    <button
                      class="primary-action"
                      type="button"
                      [disabled]="saving()"
                      (click)="save()"
                    >
                      <app-icon name="check" />{{ t('content.action.save') }}
                    </button>
                    <button class="ghost-action" type="button" (click)="openCode.set(null)">
                      {{ t('shared.actions.cancel') }}
                    </button>
                  </div>
                }
              </div>
            }
          </li>
        }
      </ul>
    </app-panel>
  `,
  styleUrl: './snippets-card.css',
})
export class SnippetsCard {
  private readonly api = inject(SnippetsService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);
  private readonly shell = inject(ConsoleShellFacade);
  private readonly cache = inject(ContentCache);

  readonly locales = input.required<readonly ReferenceOption[]>();
  readonly defaultLocale = input.required<string>();
  readonly canManage = input(true);

  protected readonly openCode = signal<string | null>(null);
  protected readonly language = signal('en');
  protected readonly draft = signal<Draft | null>(null);
  protected readonly saving = signal(false);
  private readonly stamp = signal(0);

  private readonly resource = rxResource({
    params: () => {
      this.stamp();
      this.cache.stamp();
      return this.shell.currentStoreId() ?? undefined;
    },
    stream: () => this.api.list(),
  });

  protected readonly byCode = computed(() => {
    const map = new Map<string, Snippet>();
    for (const snippet of this.resource.hasValue() ? this.resource.value() : []) {
      if (snippet.code) {
        map.set(snippet.code, snippet);
      }
    }
    return map;
  });

  /** The known codes first, then anything else the store holds (LANDING_PAGE is edited in store management). */
  protected readonly codes = computed(() => {
    const extra = [...this.byCode().keys()].filter(
      (code) => !(KNOWN_CODES as readonly string[]).includes(code) && code !== 'LANDING_PAGE',
    );
    return [...KNOWN_CODES, ...extra];
  });

  protected readonly filled = computed<ReadonlySet<string>>(() => {
    const d = this.draft();
    return new Set(
      d
        ? Object.entries(d.translations)
            .filter(([, t]) => t.title.trim() || t.body.trim())
            .map(([code]) => code)
        : [],
    );
  });

  protected describe(code: string): string {
    this.transloco.activeLang();
    const key = `content.snippets.describe.${code}`;
    const text = this.transloco.translate(key);
    return text === key ? this.transloco.translate('content.snippets.describe.other') : text;
  }

  protected toggle(code: string): void {
    if (this.openCode() === code) {
      this.openCode.set(null);
      return;
    }
    const existing = this.byCode().get(code);
    const translations: Draft['translations'] = {};
    for (const locale of this.locales()) {
      translations[locale.code] = {title: '', body: ''};
    }
    for (const t of existing?.translations ?? []) {
      translations[t.language] = {title: t.title ?? '', body: t.body ?? ''};
    }
    this.draft.set({code, visible: existing?.visible ?? true, translations});
    this.language.set(this.defaultLocale());
    this.openCode.set(code);
  }

  protected setVisible(visible: boolean): void {
    this.draft.update((d) => (d ? {...d, visible} : d));
  }

  protected setTitle(title: string): void {
    this.patch({title});
  }

  protected setBody(body: string): void {
    this.patch({body});
  }

  private patch(change: Partial<{title: string; body: string}>): void {
    const lang = this.language();
    this.draft.update((d) =>
      d
        ? {
            ...d,
            translations: {
              ...d.translations,
              [lang]: {...(d.translations[lang] ?? {title: '', body: ''}), ...change},
            },
          }
        : d,
    );
  }

  protected save(): void {
    const d = this.draft();
    if (!d) {
      return;
    }
    const existing = this.byCode().get(d.code);
    const translations: ContentTranslation[] = Object.entries(d.translations)
      .filter(([, t]) => t.title.trim() || t.body.trim())
      .map(([language, t]) => ({
        ...existing?.translations.find((e) => e.language === language),
        language,
        title: t.title.trim() || d.code,
        body: t.body,
      }));
    this.saving.set(true);
    this.api
      .put(d.code, {id: existing?.id, code: d.code, visible: d.visible, translations})
      .subscribe({
        next: () => {
          this.saving.set(false);
          this.openCode.set(null);
          this.stamp.update((v) => v + 1);
          this.toast.success(this.transloco.translate('content.snippets.saved', {code: d.code}));
        },
        error: (failure: unknown) => {
          this.saving.set(false);
          this.apiErrors.notify(failure);
        },
      });
  }
}
