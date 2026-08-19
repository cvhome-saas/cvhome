import {Component, computed, input, model} from '@angular/core';
import {toObservable, toSignal} from '@angular/core/rxjs-interop';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';
import {startWith, switchMap} from 'rxjs';

import type {ReferenceOption} from '@core/reference/reference-data.service';
import {FieldError} from '@shared/ui/form-field/field-error';
import {NoticeBar} from '@shared/ui/notice-bar/notice-bar';
import {Panel} from '@shared/ui/panel/panel';
import {TabSwitcher, type TabItem} from '@shared/ui/tab-switcher/tab-switcher';
import {TagInput} from '@shared/ui/tag-input/tag-input';
import {HOME_TITLE_MAX, META_DESCRIPTION_MAX, META_DESCRIPTION_MIN} from '@models/store-settings';
import type {HomeCopyForm} from '../../services/store-settings-form.service';

/**
 * The storefront's landing copy and search metadata, written per language.
 *
 * The track is the languages the *store* publishes in, not the two the console itself runs in.
 * Conflating them was wrong in both directions: a store trading in French had nowhere to write its
 * French copy, and an operator could write Arabic copy for a storefront that does not serve it.
 * Each language holds its own group, so switching the track swaps which controls are bound without
 * discarding what was typed in the others.
 *
 * All of it is one `LANDING_PAGE` content box — one description per language — which is why saving
 * sends every language at once rather than only the one on screen.
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
          <!--
            Not required. A storefront publishes in several languages and a seller fills them in
            over time; demanding a headline in every one would make the section unsavable until the
            last translation arrived. A language with no title is marked empty on the track instead.
          -->
          <label for="home-title">{{ t('storeSettings.home.landingTitle') }}</label>
          <input id="home-title" class="control" type="text" formControlName="title" />
          <p class="field-foot">
            <span>{{ t('storeSettings.home.landingTitleHint') }}</span>
            <span class="counter">{{ titleLength() }}/{{ titleMax }}</span>
          </p>
          <app-field-error [control]="title()" [fallback]="t('storeSettings.home.landingTitleFallback')" />
          <!--
            The rule reads three controls, so it lives on the group — but the field to fix is this
            one, so the message sits under it. A description with no name cannot be stored at all.
          -->
          @if (needsTitle()) {
            <p class="cross-field-error" role="alert">{{ t('storeSettings.home.titleForCopy') }}</p>
          }
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

        <!--
          Designed, and dropped by the platform in both directions: neither mapper on the server
          touches the description's keywords column. Rendered disabled with the reason rather than
          accepting input that goes nowhere.
          TODO(lessons.md): see lessons.md, "Store management — a content description's keywords are
          dropped by both mappers".
        -->
        <div class="field unbacked">
          <p class="field-label">{{ t('storeSettings.home.tags') }}</p>
          <app-tag-input
            [tags]="tags().value"
            [disabled]="true"
            [label]="t('storeSettings.home.tagsLabel')"
          />
          <p class="field-hint">{{ t('storeSettings.home.tagsNotStored') }}</p>
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
  /** One group per storefront language, keyed by code. */
  readonly forms = input.required<Readonly<Record<string, HomeCopyForm>>>();
  readonly locales = input.required<readonly ReferenceOption[]>();
  readonly language = model.required<string>();

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
      // Named rather than coded: five storefront languages as "EN FR AR ES RU" is a puzzle.
      label: locale.label,
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

  /**
   * Whether this language has copy but no headline to file it under.
   *
   * Held back until something has been typed, the rule `FieldError` follows: a store that arrives
   * in this state should not be shouted at before the operator has touched anything.
   */
  protected readonly needsTitle = computed(() => {
    this.value();
    const group = this.copy();
    return group.hasError('titleForCopy') && (group.dirty || group.touched);
  });

  protected readonly metaOutsideRange = computed(() => {
    const length = this.metaLength();
    return length > 0 && (length < META_DESCRIPTION_MIN || length > META_DESCRIPTION_MAX);
  });

}
