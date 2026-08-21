import {Component, computed, inject, input, signal} from '@angular/core';
import {toObservable, toSignal} from '@angular/core/rxjs-interop';
import {of, startWith, switchMap} from 'rxjs';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';

import {LocaleService} from '@core/i18n/locale.service';
import type {ReferenceOption} from '@core/reference/reference-data.service';
import {DatePicker} from '@shared/ui/date-picker/date-picker';
import {FieldError} from '@shared/ui/form-field/field-error';
import {Icon} from '@shared/ui/icon/icon';
import {NoticeBar} from '@shared/ui/notice-bar/notice-bar';
import {Panel} from '@shared/ui/panel/panel';
import {TagInput} from '@shared/ui/tag-input/tag-input';
import {RichText} from '@shared/ui/rich-text/rich-text';
import {Toggle} from '@shared/ui/toggle/toggle';
import {NumberField} from '@shared/ui/number-field/number-field';
import {ProductFormFacade} from '../../facades/product-form.facade';
import type {ProductForm} from '../../services/product-form.service';

/**
 * Step 1 — what the product is called and whether it is on sale.
 *
 * Two blocks with a real difference between them: the identifiers block is shared across every
 * language, and the copy block is per-language. The design says so in a sentence and this keeps it,
 * because it is the single most confusing thing about a multilingual product form.
 *
 * **Barcode / GTIN is gone.** The design's second identifier field has no column on the product,
 * no DTO field and no endpoint. So is the "generated from category" SKU hint: nothing on the
 * platform derives a SKU, and `GET …/product/unique?code=` only answers whether one is taken. See
 * lessons.md.
 */
@Component({
  selector: 'app-essentials-step',
  imports: [
    DatePicker,
    FieldError,
    Icon,
    NoticeBar,
    Panel,
    ReactiveFormsModule,
    RichText,
    TagInput,
    Toggle,
    TranslocoDirective,
    NumberField,
  ],
  templateUrl: './essentials-step.html',
  styleUrls: ['../editor-card.css', './essentials-step.css'],
})
export class EssentialsStep {
  readonly form = input.required<ProductForm>();
  readonly languages = input.required<readonly ReferenceOption[]>();
  /** Whether the product exists yet. The SKU is fixed once it does, so the check stops mattering. */
  readonly saved = input.required<boolean>();

  private readonly locale = inject(LocaleService);

  protected readonly facade = inject(ProductFormFacade);

  /** Which language is being written. Local: it is a view preference, not part of the product. */
  protected readonly activeLanguage = signal(0);

  protected readonly copyRows = computed(() => this.form().controls.copy.controls);

  /**
   * The active language's keywords, as chips.
   *
   * `meta_keywords` is one comma-separated `varchar(255)` on the wire and stays that way; this is
   * only how it is edited. Read off `valueChanges` because a `FormControl` is not a signal.
   */
  protected readonly keywords = computed(() => {
    return this.keywordsValue()
      .split(',')
      .map((tag: string) => tag.trim())
      .filter(Boolean);
  });

  protected onKeywords(tags: readonly string[]): void {
    this.activeRow()?.controls.keyWords.setValue(tags.join(', '));
  }

  protected readonly activeRow = computed(
    () => this.copyRows()[this.activeLanguage()] ?? this.copyRows()[0],
  );

  /**
   * The active language's keyword string, as it changes.
   *
   * A `FormControl` is not a signal, so the chips would otherwise freeze at whatever the row held
   * when the component mounted. The `switchMap` re-subscribes when the language switches.
   */
  private readonly keywordsValue = toSignal(
    toObservable(this.activeRow).pipe(
      switchMap((row) => {
        const control = row?.controls.keyWords;
        return control ? control.valueChanges.pipe(startWith(control.value)) : of('');
      }),
    ),
    {initialValue: ''},
  );

  protected readonly activeLanguageName = computed(
    () => this.languages()[this.activeLanguage()]?.label ?? '',
  );

  /**
   * Which way the language being written runs.
   *
   * The description is stored as HTML and the seeded Arabic rows are wrapped in
   * `<div dir="rtl">`. Passing the direction is what lets a newly written Arabic description
   * acquire the same wrapper instead of arriving unmarked.
   */
  protected readonly activeLanguageDir = computed<'auto' | 'ltr' | 'rtl'>(() => {
    const code = this.languages()[this.activeLanguage()]?.code;
    return this.locale.locales.find((entry) => entry.code === code)?.dir ?? 'auto';
  });

}
