import {Component, computed, input, model} from '@angular/core';
import {toObservable, toSignal} from '@angular/core/rxjs-interop';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';
import {startWith, switchMap} from 'rxjs';

import type {ConsoleLocale} from '@core/i18n/locale.service';
import {FieldError} from '@shared/ui/form-field/field-error';
import {NoticeBar} from '@shared/ui/notice-bar/notice-bar';
import {Panel} from '@shared/ui/panel/panel';
import {TabSwitcher, type TabItem} from '@shared/ui/tab-switcher/tab-switcher';
import {TagInput} from '@shared/ui/tag-input/tag-input';
import {
  HOME_TITLE_MAX,
  META_DESCRIPTION_MAX,
  META_DESCRIPTION_MIN,
  type LocaleCode,
} from '@models/store-settings';
import type {HomeCopyForm} from '../../services/store-settings-form.service';

/**
 * The storefront's landing copy and search metadata, written per language.
 *
 * The language track comes from the console's own locale list, so a language is never
 * hardcoded twice. Each language holds its own group, so switching the track swaps which
 * controls are bound without discarding what was typed in the others.
 */
@Component({
  selector: 'app-home-section',
  imports: [FieldError, NoticeBar, Panel, ReactiveFormsModule, TabSwitcher, TagInput, TranslocoDirective],
  template: `
    <app-panel
      [title]="t('storeSettings.home.title')"
      [subtitle]="t('storeSettings.home.subtitle')"
      *transloco="let t"
    >
      <app-tab-switcher
        panelAction
        class="lang-track"
        [tabs]="tabs(t)"
        [active]="language()"
        (activeChange)="language.set($any($event))"
        [label]="t('storeSettings.home.writeIn')"
        panelId="home-copy"
      />

      <div class="section-body" id="home-copy" [formGroup]="copy()">
        <div class="field">
          <label for="home-title">
            {{ t('storeSettings.home.landingTitle') }} <span class="required" aria-hidden="true">*</span>
          </label>
          <input
            id="home-title"
            class="control"
            type="text"
            formControlName="title"
            required
          />
          <p class="field-foot">
            <span>{{ t('storeSettings.home.landingTitleHint') }}</span>
            <span class="counter">{{ titleLength() }}/{{ titleMax }}</span>
          </p>
          <app-field-error [control]="title()" [fallback]="t('storeSettings.home.landingTitleFallback')" />
        </div>

        <div class="field">
          <label for="home-text">{{ t('storeSettings.home.landingText') }}</label>
          <textarea id="home-text" class="control" formControlName="text"></textarea>
        </div>

        <div class="field">
          <label for="home-meta">{{ t('storeSettings.home.metaDescription') }}</label>
          <textarea id="home-meta" class="control meta" formControlName="metaDescription"></textarea>
          <p class="field-foot">
            <span>{{ t('storeSettings.home.metaRecommended', {min: metaMin, max: metaMax}) }}</span>
            <!-- A recommendation, so it is a counter that changes tone, never a blocking error. -->
            <span class="counter" [class.over]="metaOutsideRange()">
              {{ t('storeSettings.home.characters', {count: metaLength()}) }}
            </span>
          </p>
        </div>

        <div class="field">
          <p class="field-label">{{ t('storeSettings.home.tags') }}</p>
          <app-tag-input
            [tags]="tags().value"
            (tagsChange)="setTags($event)"
            [label]="t('storeSettings.home.tagsLabel')"
          />
        </div>

        <app-notice-bar
          class="fallback-notice"
          tone="blue"
          icon="alertCircle"
          [message]="t('storeSettings.home.fallbackNotice')"
        />
      </div>
    </app-panel>
  `,
  styleUrls: ['../settings-card.css', './home-section.css'],
})
export class HomeSection {
  /** One group per locale, keyed by code. */
  readonly forms = input.required<Readonly<Record<string, HomeCopyForm>>>();
  readonly locales = input.required<readonly ConsoleLocale[]>();
  readonly language = model.required<LocaleCode>();

  protected readonly titleMax = HOME_TITLE_MAX;
  protected readonly metaMin = META_DESCRIPTION_MIN;
  protected readonly metaMax = META_DESCRIPTION_MAX;

  /**
   * The track marks which languages have copy, so an operator can see at a glance that a
   * language is empty without opening it.
   */
  protected tabs(t: (key: string) => string): readonly TabItem[] {
    return this.locales().map((locale) => ({
      key: locale.code,
      label: locale.code.toUpperCase(),
      badge: this.forms()[locale.code]?.controls.title.value ? undefined : t('storeSettings.home.empty'),
      badgeTone: 'slate' as const,
    }));
  }

  protected readonly copy = computed(() => this.forms()[this.language()]);
  protected readonly title = computed(() => this.copy().controls.title);
  protected readonly tags = computed(() => this.copy().controls.tags);

  /**
   * The active group's value, as a signal.
   *
   * The counters have to track what is being typed, and a control's `value` is not reactive.
   * Following the group through `toObservable` rather than subscribing once matters because
   * the group itself is swapped by the language track.
   */
  private readonly value = toSignal(
    toObservable(this.copy).pipe(
      switchMap((group) => group.valueChanges.pipe(startWith(group.getRawValue()))),
    ),
    {initialValue: null},
  );

  protected readonly titleLength = computed(() => {
    this.value();
    return this.title().value.length;
  });

  protected readonly metaLength = computed(() => {
    this.value();
    return this.copy().controls.metaDescription.value.length;
  });

  protected readonly metaOutsideRange = computed(() => {
    const length = this.metaLength();
    return length > 0 && (length < META_DESCRIPTION_MIN || length > META_DESCRIPTION_MAX);
  });

  /**
   * `setValue` alone would leave the form pristine, and *Save changes* keys off dirty — so a
   * control written by a component rather than by a keystroke has to say it changed.
   */
  protected setTags(tags: readonly string[]): void {
    this.write(this.tags(), tags);
  }

  private write<T>(control: FormControl<T>, value: T): void {
    control.setValue(value);
    control.markAsDirty();
  }
}
