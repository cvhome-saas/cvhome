import {Injectable, inject} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ValidationErrors,
  Validators,
  type AsyncValidatorFn,
} from '@angular/forms';
import {Observable, catchError, first, map, of, switchMap, timer} from 'rxjs';

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
import {CatalogueApi} from './catalogue.api.service';

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
   * The shape `create-store.facade.ts`'s `uniqueName` and `store-settings-form.service.ts`'s
   * `dnsPointsToPod` established: `timer` at the head of the stream is the debounce — Angular's own
   * cancellation of the previous run does the rest — then one call, then a **named** error key.
   *
   * **A check that could not be made is not a failed check.** An unreachable endpoint answers
   * `null`, so the field stays usable and the server has the last word on the save. Locking a field
   * because a lookup timed out is the worse failure.
   */
  private uniqueCode(check: (code: string) => Observable<boolean>): AsyncValidatorFn {
    return (control: AbstractControl): Observable<ValidationErrors | null> => {
      const code = String(control.value ?? '').trim();
      if (!code || !CODE_PATTERN.test(code)) {
        // Nothing to ask about, and the sync validators are already saying so.
        return of(null);
      }
      return timer(UNIQUENESS_DEBOUNCE_MS).pipe(
        switchMap(() => check(code)),
        /*
         * `control.enabled` is not belt and braces — it is the whole bug.
         *
         * The code of an existing record is disabled, because a code identifies the record. But the
         * form is filled while the control is still enabled, so the check starts, and the facade
         * disables it a tick later. `disable()` nulls the errors it finds; it cannot null an error
         * that has not arrived yet. The answer landed afterwards and marked every existing category,
         * brand, type and group as a duplicate of itself — a red "already taken" against a code the
         * operator cannot even edit.
         */
        map((taken) => (taken && control.enabled ? {codeTaken: true} : null)),
        catchError(() => of(null)),
        first(),
      );
    };
  }
}

/**
 * What a code may be.
 *
 * Letters, digits, hyphen and underscore. The pod does not validate this — it accepts anything a
 * `String` can hold — but a code with a slash or a space in it becomes a path segment on
 * `…/groups/{code}` and on the storefront's URLs, where it either breaks routing or arrives
 * percent-encoded and unrecognisable. Refusing it here is cheaper than discovering it there.
 */
export const CODE_PATTERN = /^[A-Za-z0-9_-]+$/;

/** Combining diacritics, stripped after NFKD so "Café" slugifies to "cafe" rather than "caf". */
const COMBINING_MARKS = /[\u0300-\u036f]/g;

/**
 * A code derived from a name.
 *
 * Nothing on the platform generates one and every create needs one, so the console offers the name
 * slugified, which is what seller-ui did. It stays editable: this is a suggestion, not a rule, and
 * a name written only in Arabic slugifies to nothing — which the caller falls back from.
 */
export function slugify(value: string): string {
  return value
    .normalize('NFKD')
    .replace(COMBINING_MARKS, '')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
    .slice(0, CODE_MAX);
}
