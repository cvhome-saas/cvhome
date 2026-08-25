import {Component, computed, input, model} from '@angular/core';
import {toObservable, toSignal} from '@angular/core/rxjs-interop';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';
import {startWith, switchMap} from 'rxjs';

import type {ReferenceOption} from '@core/reference/reference-data.service';
import {FormField} from '@shared/ui/form-field/form-field';
import {NoticeBar} from '@shared/ui/notice-bar/notice-bar';
import {Panel} from '@shared/ui/panel/panel';
import {LocaleSwitcher} from '@shared/ui/locale-switcher/locale-switcher';
import {TagInput} from '@shared/ui/tag-input/tag-input';
import {TextField} from '@shared/ui/text-field/text-field';
import {TextareaField} from '@shared/ui/textarea-field/textarea-field';
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
  imports: [
    FormField,
    LocaleSwitcher,
    NoticeBar,
    Panel,
    ReactiveFormsModule,
    TagInput,
    TextField,
    TextareaField,
    TranslocoDirective,
  ],
  template: `
    <app-panel
      [title]="t('storeSettings.home.title')"
      [subtitle]="t('storeSettings.home.subtitle')"
      *transloco="let t"
    >
      <!--
        A radio group, not a tablist. These are values of one field on the page, not views of it —
        which is what app-locale-switcher is for, and what the catalogue and the product form now
        also use. Pressing app-tab-switcher into service here announced a tablist for something that
        is not one, and gave the strip a different shape from the same control two pages away.
      -->
      <app-locale-switcher
        panelAction
        [languages]="localeOptions()"
        [(active)]="language"
        [filled]="written()"
        display="label"
        [label]="t('storeSettings.home.writeIn')"
      />

      <div class="section-body field-grid" id="home-copy" [formGroup]="copy()">
        <!--
          Three ordinary fields, in the shape every other section uses.

          They were one bordered "composer" sheet before, with borderless transparent controls, a
          hand-written counter and a bespoke fill meter for the recommended length — a third field
          pattern that existed on this page alone. Two pages away the same three kinds of value are
          a label, a framed control and a message, and a seller moving between them had to learn
          the surface twice. Everything the composer did that was worth keeping — the counters, the
          headline limit, the advised meta length, the growing box — the shared controls now do.
        -->
        <app-form-field
          wide
          [label]="t('storeSettings.home.landingTitle')"
          [hint]="t('storeSettings.home.landingTitleHint')"
          [control]="title()"
          [fallback]="t('storeSettings.home.landingTitleFallback')"
          controlId="home-title"
        >
          <!--
            Not required. A storefront publishes in several languages and a seller fills them in
            over time; demanding a headline in every one would make the section unsavable until the
            last translation arrived. A language with no title is marked empty on the track.

            The control resolves its own direction from what is typed, which is not cosmetic here:
            this section is written in languages the console is not necessarily running in, and
            Arabic copy typed while the console is in English would otherwise read as nonsense.
          -->
          <app-text-field
            id="home-title"
            formControlName="title"
            [maxLength]="titleMax"
            [placeholder]="t('storeSettings.home.landingTitlePlaceholder')"
          />
        </app-form-field>

        <app-form-field
          wide
          [label]="t('storeSettings.home.landingText')"
          [hint]="t('storeSettings.home.landingTextHint')"
          [control]="text()"
          controlId="home-text"
        >
          <app-textarea
            id="home-text"
            autoGrow
            formControlName="text"
            [rows]="5"
            [placeholder]="t('storeSettings.home.landingTextPlaceholder')"
          />
        </app-form-field>

        <app-form-field
          wide
          [label]="t('storeSettings.home.metaDescription')"
          [hint]="t('storeSettings.home.metaRecommended', {min: metaMin, max: metaMax})"
          [control]="metaDescription()"
          controlId="home-meta"
        >
          <!--
            A recommendation, so the counter changes tone outside the window and nothing blocks the
            save. It is not a a hard limit: a description over the advised length is still a
            description, and truncating one mid-word would be worse than a long one.
          -->
          <app-textarea
            id="home-meta"
            formControlName="metaDescription"
            [rows]="2"
            [recommendedMin]="metaMin"
            [recommendedMax]="metaMax"
            [placeholder]="t('storeSettings.home.metaPlaceholder')"
          />
        </app-form-field>

        <!--
          The rule reads three controls, so it lives on the group — but the field to fix is the
          headline, and a description with no name cannot be stored at all.
        -->
        @if (needsTitle()) {
          <p class="cross-field-error field-wide" role="alert">{{ t('storeSettings.home.titleForCopy') }}</p>
        }

        <div class="field field-wide">
          <p class="field-label">{{ t('storeSettings.home.tags') }}</p>
          <app-tag-input
            [tags]="tags().value"
            (tagsChange)="tags().setValue($event)"
            [label]="t('storeSettings.home.tagsLabel')"
          />
        </div>

        <app-notice-bar
          class="fallback-notice field-wide"
          tone="blue"
          icon="alertCircle"
          [message]="t('storeSettings.home.fallbackNotice')"
        />
      </div>
    </app-panel>
  `,
  styleUrls: ['../../../../shared/styles/field.css', '../settings-card.css', './home-section.css'],
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
  /** The storefront's languages, as the switcher's options. */
  protected readonly localeOptions = computed<readonly ReferenceOption[]>(() =>
    this.locales().map((locale) => ({code: locale.code, label: locale.label})),
  );

  /** Which languages already carry a headline. The switcher marks the rest as untranslated. */
  protected readonly written = computed<ReadonlySet<string>>(() => {
    const forms = this.forms();
    this.value();
    return new Set(
      this.locales()
        .map((locale) => locale.code)
        .filter((code) => !!forms[code]?.controls.title.value),
    );
  });

  protected readonly copy = computed(() => this.forms()[this.language()]);
  protected readonly title = computed(() => this.copy().controls.title);
  protected readonly text = computed(() => this.copy().controls.text);
  protected readonly metaDescription = computed(() => this.copy().controls.metaDescription);
  protected readonly tags = computed(() => this.copy().controls.tags);


  /**
   * The active group's value, as a signal.
   *
   * The empty-language dots and the cross-field rule both have to track what is being typed, and
   * a control's `value` is not reactive.
   * Following the group through `toObservable` rather than subscribing once matters because
   * the group itself is swapped by the language track.
   */
  private readonly value = toSignal(
    toObservable(this.copy).pipe(
      switchMap((group) => group.valueChanges.pipe(startWith(group.getRawValue()))),
    ),
    {initialValue: null},
  );

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

}
