import {Component, computed, effect, input, output} from '@angular/core';
import {toObservable, toSignal} from '@angular/core/rxjs-interop';
import {ReactiveFormsModule, type FormControl, type FormGroup} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';
import {startWith, switchMap} from 'rxjs';

import {FormField} from '@shared/ui/form-field/form-field';
import {Icon} from '@shared/ui/icon/icon';
import {RichText} from '@shared/ui/rich-text/rich-text';
import {TextField} from '@shared/ui/text-field/text-field';
import {TextareaField} from '@shared/ui/textarea-field/textarea-field';
import {SEO_DESCRIPTION_LIMIT, SEO_TITLE_LIMIT} from '@models/taxonomy';

/** The copy `FormGroup` every catalogue editor carries. */
export type CopyFormGroup = FormGroup<{
  name: FormControl<string>;
  friendlyUrl: FormControl<string>;
  description: FormControl<string>;
  title: FormControl<string>;
  metaDescription: FormControl<string>;
}>;

/**
 * One language's copy, for whichever of the four things is being edited.
 *
 * Written once because the four records carry the same `NamedEntity` underneath — the fields differ
 * only in which of them are worth showing, which is what `showSlug` and `showSeo` decide. A product
 * group has a name and nothing else; a category has all five.
 *
 * **The counters warn and never block.** Nothing server-side rejects a long title, and refusing to
 * save a category because its meta description is four characters over a search engine's display
 * limit would be the console inventing a rule. The number turns amber; the form still saves.
 */
@Component({
  selector: 'app-copy-fields',
  imports: [
    FormField,
    Icon,
    ReactiveFormsModule,
    RichText,
    TextField,
    TextareaField,
    TranslocoDirective,
  ],
  template: `
    <div class="field-grid" [formGroup]="form()" *transloco="let t">
      <!--
        The translation state for the language on screen, live off the form rather than off the last
        response — this is the one place the operator needs to see their own typing reflected
        immediately, because it is the answer to "have I done this language yet".
      -->
      <p
        class="translation-state field-wide"
        [class.present]="hasName()"
        [class.missing]="!hasName()"
      >
        <app-icon [name]="hasName() ? 'checkCircle' : 'alertCircle'" [size]="13" />
        <span>
          {{ hasName() ? t('catalogue.copy.translated', {language: languageName()}) : t('catalogue.copy.notTranslated', {language: languageName()}) }}
        </span>
      </p>

      <!-- Automatic direction throughout: a store trading in Arabic and English writes both into
           the same control, and the value decides which way it runs — so none of these is marked
           latin, unlike the slug below. -->
      <app-form-field
        [wide]="!showSlug()"
        [label]="t('catalogue.copy.name')"
        [control]="form().controls.name"
        required
        [controlId]="idFor('name')"
        [fallback]="t('catalogue.copy.nameRequired')"
      >
        <app-text-field [id]="idFor('name')" formControlName="name" />
      </app-form-field>

      @if (showSlug()) {
        <!-- A URL segment, so always left-to-right even inside the Arabic console. -->
        <app-form-field
          [label]="t('catalogue.copy.slug')"
          [controlId]="idFor('slug')"
          [hint]="t('catalogue.copy.slugHint')"
        >
          <app-text-field
            [id]="idFor('slug')"
            formControlName="friendlyUrl"
            [prefix]="slugPrefix()"
            latin
          />
        </app-form-field>
      }

      <app-form-field wide [label]="t('catalogue.copy.description')" [controlId]="idFor('description')">
        @if (richDescription()) {
          <!--
            A rich editor where the storefront renders the description as markup. Product types and
            groups keep a plain textarea, because nothing renders theirs.
          -->
          <app-rich-text
            [id]="idFor('description')"
            formControlName="description"
            [ariaLabel]="t('catalogue.copy.description')"
            [placeholder]="t('catalogue.copy.descriptionPlaceholder')"
            [contentDir]="contentDir()"
          />
        } @else {
          <app-textarea [id]="idFor('description')" formControlName="description" [rows]="3" />
        }
      </app-form-field>

      @if (showSeo()) {
        <div class="field field-wide">
          <hr class="divider" />
        </div>

        <!--
          The counters are the control's own now. They used to be a field-foot row this component
          drew, with the hint beside them — which meant a limit lived in two places, the markup and
          the maxLength input, and only one of them stopped you typing.
        -->
        <app-form-field
          wide
          [label]="t('catalogue.copy.seoTitle')"
          [controlId]="idFor('seo-title')"
          [hint]="t('catalogue.copy.seoTitleHint')"
        >
          <app-text-field [id]="idFor('seo-title')" formControlName="title" [maxLength]="titleLimit" />
        </app-form-field>

        <app-form-field
          wide
          [label]="t('catalogue.copy.metaDescription')"
          [controlId]="idFor('meta')"
          [hint]="t('catalogue.copy.metaHint')"
        >
          <app-textarea
            [id]="idFor('meta')"
            formControlName="metaDescription"
            [rows]="2"
            [maxLength]="metaLimit"
          />
        </app-form-field>
      }
    </div>
  `,
  styleUrls: ['../../../../shared/styles/field.css', '../editor-card.css'],
})
export class CopyFields {
  readonly form = input.required<CopyFormGroup>();
  /** Which record this is, so two editors on one page do not both claim `#name`. */
  readonly scope = input.required<string>();
  readonly languageName = input.required<string>();
  readonly showSlug = input(true);
  readonly showSeo = input(false);
  /**
   * Whether the description is rendered as HTML by the storefront.
   *
   * True for a category and a brand, whose descriptions seller-ui edited with Quill. False for a
   * product type and a group, where nothing renders the text and a rich editor would only invite an
   * operator to add markup that goes nowhere.
   */
  readonly richDescription = input(false);
  /**
   * The direction the copy is written in — the language being edited, not the console's.
   *
   * `'auto'` where the caller does not know; a known Arabic language passes `'rtl'`, which is how a
   * newly written description acquires the `<div dir="rtl">` wrapper the seeded data has.
   */
  readonly contentDir = input<'auto' | 'ltr' | 'rtl'>('auto');
  /** What the storefront prefixes the slug with, e.g. `/c/`. Shown, never sent. */
  readonly slugPrefix = input<string | null>(null);

  /**
   * The name as it is typed.
   *
   * The editors above use it to offer a code derived from the name while creating. Emitted from
   * here rather than listened for with a bubbling `(input)` on the host, which would fire for the
   * description and the SEO fields too.
   */
  readonly nameChanged = output<string>();

  protected readonly titleLimit = SEO_TITLE_LIMIT;
  protected readonly metaLimit = SEO_DESCRIPTION_LIMIT;

  /**
   * What the form currently holds.
   *
   * Read off `valueChanges` rather than through a `computed`: a `FormControl`'s value is not a
   * signal, so a `computed` reading it would run once and never again, and the counters would sit
   * at whatever they were when the component mounted. `startWith` supplies the value before the
   * first keystroke, and the `switchMap` re-subscribes when the facade hands over a different form
   * — which it does on every language switch.
   */
  private readonly value = toSignal(
    toObservable(this.form).pipe(
      switchMap((form) => form.valueChanges.pipe(startWith(form.getRawValue()))),
    ),
    {initialValue: {} as Partial<{name: string; title: string; metaDescription: string}>},
  );

  protected readonly hasName = computed(() => (this.value().name ?? '').trim() !== '');
  protected readonly titleLength = computed(() => (this.value().title ?? '').length);
  protected readonly metaLength = computed(() => (this.value().metaDescription ?? '').length);

  constructor() {
    effect(() => this.nameChanged.emit(this.value().name ?? ''));
  }

  protected idFor(field: string): string {
    return `${this.scope()}-${field}`;
  }
}
