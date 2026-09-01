import {Injectable, inject} from '@angular/core';
import {
  AsyncValidatorFn,
  FormBuilder,
  Validators,
  type FormArray,
  type FormControl,
  type FormGroup,
} from '@angular/forms';
import {Observable} from 'rxjs';

import {
  CATEGORY_SLUG_MAX,
  CODE_MAX,
  META_DESCRIPTION_MAX,
  NAME_MAX,
  SLUG_MAX,
  TITLE_MAX,
  TYPE_CODE_MAX,
  UNIQUENESS_DEBOUNCE_MS,
} from '@models/taxonomy';
import {uniqueAsync} from '@shared/forms/unique-async';
import {CODE_PATTERN, slugify} from '@shared/validators/slug';
import {CatalogueApi} from './catalogue.api.service';

/** One value row of the option editor. */
export type OptionValueForm = FormGroup<{
  id: FormControl<number | null>;
  key: FormControl<string>;
  code: FormControl<string>;
  name: FormControl<string>;
}>;

/** The option editor: code, the active language's name, and the value rows. */
export type OptionForm = FormGroup<{
  code: FormControl<string>;
  name: FormControl<string>;
  values: FormArray<OptionValueForm>;
}>;

/**
 * The forms behind `/catalogue`'s four editors.
 *
 * One service rather than four, because the four differ by three controls between them and the
 * copy block is identical in all of them. Follows `store-settings-form.service.ts`: the service
 * builds, the facade owns the instance, and the component only binds.
 *
 * **`code` is only editable while creating.** Every one of these entities is addressed by its code
 * — a product group *only* by its code — and changing it is not an edit but a different record. The
 * facade disables the control once a record exists rather than hiding it, so the value stays
 * visible and stays out of the payload. That is also what scopes the uniqueness check below to
 * creation: Angular does not run validators on a disabled control.
 *
 * **Every length matches its column.** `schema.sql` bounds all of these — `name varchar(120)`,
 * `title varchar(100)`, `meta_description varchar(255)` — and an over-long value is a 500 from the
 * driver rather than a validation error, which reads to the operator as the console breaking. The
 * SEO *counters* are a separate, softer thing: they warn at the length a search engine will display,
 * well before the column runs out.
 */
@Injectable({providedIn: 'root'})
export class CatalogueFormService {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(CatalogueApi);

  /**
   * The per-language half of every editor on the page.
   *
   * Categories, brands, product types and product groups all hang their copy off the same
   * `NamedEntity` — the same seven fields under four names — so one control set serves all four.
   * Only `name` is required, and only because a record with no name in any language is unfindable.
   *
   * `slugMax` differs by entity and is not cosmetic: `category_description.sef_url` is
   * `varchar(120)` where `product_description.sef_url` is `varchar(255)`.
   */
  copy(slugMax = SLUG_MAX) {
    return this.fb.nonNullable.group({
      name: ['', [Validators.required, Validators.maxLength(NAME_MAX)]],
      friendlyUrl: ['', [Validators.maxLength(slugMax)]],
      description: [''],
      title: ['', [Validators.maxLength(TITLE_MAX)]],
      metaDescription: ['', [Validators.maxLength(META_DESCRIPTION_MAX)]],
    });
  }

  category() {
    return this.fb.nonNullable.group({
      code: [
        '',
        [Validators.required, Validators.maxLength(CODE_MAX), Validators.pattern(CODE_PATTERN)],
        [this.uniqueCode((code) => this.api.categoryCodeTaken(code))],
      ],
      visible: [false],
      sortOrder: [0, [Validators.min(0)]],
      copy: this.copy(CATEGORY_SLUG_MAX),
    });
  }

  /**
   * A brand.
   *
   * Two controls and its copy, because two is what persists. `order` used to be here; the populator
   * never reads it. See `BrandCard` in `@models/taxonomy`.
   */
  brand() {
    return this.fb.nonNullable.group({
      code: [
        '',
        [Validators.required, Validators.maxLength(CODE_MAX), Validators.pattern(CODE_PATTERN)],
        [this.uniqueCode((code) => this.api.brandCodeTaken(code))],
      ],
      copy: this.copy(),
    });
  }

  type() {
    return this.fb.nonNullable.group({
      // `prd_type_code` is `varchar(255)` where the other three codes are `varchar(100)`.
      code: [
        '',
        [Validators.required, Validators.maxLength(TYPE_CODE_MAX), Validators.pattern(CODE_PATTERN)],
        [this.uniqueCode((code) => this.api.typeCodeTaken(code))],
      ],
      visible: [true],
      allowAddToCart: [true],
      copy: this.copy(),
    });
  }

  /**
   * A store option and its values.
   *
   * Unlike the other four editors this one carries no `copy` block: an option's per-language
   * payload is one name, for itself and for each value, so the form holds the **active language's**
   * names directly and the facade parks the other languages the same way it parks copy drafts.
   *
   * Value rows carry three identities: `id` (the server row — kept so an edit is not a re-create,
   * which would retire a store-wide value id every variant references), `key` (a client-side handle
   * that survives add/remove, which is what the language drafts are parked under), and `code`.
   */
  option(): OptionForm {
    return this.fb.nonNullable.group({
      code: [
        '',
        [Validators.required, Validators.maxLength(CODE_MAX), Validators.pattern(CODE_PATTERN)],
        [this.uniqueCode((code) => this.api.optionCodeTaken(code))],
      ],
      name: ['', [Validators.required, Validators.maxLength(NAME_MAX)]],
      values: this.fb.array<OptionValueForm>([]),
    }) as OptionForm;
  }

  /** One value row. `code` is disabled by the facade once the value exists — a code is identity. */
  optionValue(seed: {id: number | null; key: string; code: string; name: string}): OptionValueForm {
    return this.fb.nonNullable.group({
      id: this.fb.control<number | null>(seed.id),
      key: [seed.key],
      code: [
        seed.code,
        [Validators.required, Validators.maxLength(CODE_MAX), Validators.pattern(CODE_PATTERN)],
      ],
      name: [seed.name, [Validators.required, Validators.maxLength(NAME_MAX)]],
    }) as OptionValueForm;
  }

  group() {
    return this.fb.nonNullable.group({
      code: [
        '',
        [Validators.required, Validators.maxLength(CODE_MAX), Validators.pattern(CODE_PATTERN)],
        [this.uniqueCode((code) => this.api.groupCodeTaken(code))],
      ],
      active: [true],
      copy: this.copy(),
    });
  }

  /**
   * Whether a code is already taken in this store.
   *
   * The pipeline itself is `@shared/forms/unique-async`, which is where the two behaviours that
   * matter live: the debounce at the head of the stream, and the `control.enabled` check at the
   * point of *reporting* rather than of asking. That second one is the bug this module shipped —
   * an existing record's code is disabled once loaded, so an answer arriving afterwards marked
   * every category, brand, type and group a duplicate of itself.
   *
   * What stays here is the part that is the catalogue's: which endpoint answers, and the fact that
   * a code failing `CODE_PATTERN` is not worth a round trip.
   */
  private uniqueCode(check: (code: string) => Observable<boolean>): AsyncValidatorFn {
    return uniqueAsync(check, 'codeTaken', {
      debounceMs: UNIQUENESS_DEBOUNCE_MS,
      when: (code) => CODE_PATTERN.test(code),
    });
  }
}

/*
 * Re-exported: what a code may be, and how to suggest one, are shared — every taxonomy the
 * console has is a coded record created from a typed name — but the catalogue is where they are
 * used and where readers look for them.
 */
export {CODE_PATTERN, slugify};
