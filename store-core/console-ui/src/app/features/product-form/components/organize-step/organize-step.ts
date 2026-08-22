import {Component, computed, inject, input} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {Autocomplete, type AutocompleteOption} from '@shared/ui/autocomplete/autocomplete';
import {Checkbox} from '@shared/ui/checkbox/checkbox';
import {Icon} from '@shared/ui/icon/icon';
import {NoticeBar} from '@shared/ui/notice-bar/notice-bar';
import {Panel} from '@shared/ui/panel/panel';
import {Select, type SelectOption} from '@shared/ui/select/select';
import {ProductFormFacade} from '../../facades/product-form.facade';
import type {ProductForm} from '../../services/product-draft-form.service';

/**
 * Step 4 — where the product sits and what it is related to.
 *
 * **Categories are applied by diffing.** The chosen set is compared against what the definition
 * returned and one `POST` or `DELETE …/product/{id}/category/{id}` is issued per difference. That
 * is possible only because the port fixed seller-core's stray trailing brace on the `POST` path,
 * which meant the endpoint had never once been reached from the old console — which makes this the
 * sharpest test in the module.
 *
 * **Related products need a saved product**, for the same reason images do: the relationship
 * endpoints are addressed by product id. Categories do *not* — they are applied straight after the
 * create — so only the related block is held back.
 *
 * **Gone**, each with an entry in lessons.md: collections, tags and supplier. None exists on the
 * product or anywhere else on the platform, and product groups — which do exist — are a different
 * concept, edited on `/catalogue`.
 */
@Component({
  selector: 'app-organize-step',
  imports: [
    Autocomplete,
    Checkbox,
    Icon,
    NoticeBar,
    Panel,
    ReactiveFormsModule,
    Select,
    TranslocoDirective,
  ],
  templateUrl: './organize-step.html',
  styleUrls: ['../editor-card.css', '../../../../shared/styles/product-picker.css', './organize-step.css'],
})
export class OrganizeStep {
  readonly form = input.required<ProductForm>();
  readonly saved = input.required<boolean>();

  protected readonly facade = inject(ProductFormFacade);
  private readonly transloco = inject(TranslocoService);

  /*
   * The empty option is built here rather than written in the template, because `app-select` takes
   * its choices as data. `''` is the "none" value the form holds and the API boundary maps back to
   * `null`.
   */
  protected readonly brandOptions = computed<readonly SelectOption[]>(() => {
    this.transloco.activeLang();
    return [
      {value: '', label: this.transloco.translate('productForm.organize.noBrand')},
      ...this.facade.brands().map((brand) => ({value: brand.code, label: brand.label})),
    ];
  });

  protected readonly typeOptions = computed<readonly SelectOption[]>(() => {
    this.transloco.activeLang();
    return [
      {value: '', label: this.transloco.translate('productForm.organize.noType')},
      ...this.facade.types().map((type) => ({value: type.code, label: type.label})),
    ];
  });

  /** The selected set, as a `Set` — the picker asks about membership once per row. */
  protected readonly selected = computed(() => new Set(this.facade.selectedCategories()));

  protected addRelated(option: AutocompleteOption): void {
    this.facade.addRelated(option.id);
  }
}
