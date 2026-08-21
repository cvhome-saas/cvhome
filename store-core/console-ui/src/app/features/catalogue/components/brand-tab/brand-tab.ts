import {Component, computed, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';

import {FieldError} from '@shared/ui/form-field/field-error';
import {Icon} from '@shared/ui/icon/icon';
import {NoticeBar} from '@shared/ui/notice-bar/notice-bar';
import {Panel} from '@shared/ui/panel/panel';
import {CopyFields} from '../copy-fields/copy-fields';
import {LocaleChips} from '../locale-chips/locale-chips';
import {CatalogueFacade} from '../../facades/catalogue.facade';

/**
 * Brands — manufacturers, as the backend calls them.
 *
 * **Three blocks the design draws are gone**, because `ReadableManufacturer` is `{id, code, order,
 * descriptions}` and nothing else: the logo upload, the "publish brand page" toggle, and the
 * product count on each card. There is no image on the entity, no publish flag, and no endpoint
 * that counts a brand's products — the products list can filter by `manufacturerId`, but reading a
 * count per brand would be one request per card. The card carries the brand's initials instead of
 * a logo, which is at least honest about being a stand-in. See lessons.md.
 */
@Component({
  selector: 'app-brand-tab',
  imports: [
    CopyFields,
    FieldError,
    Icon,
    LocaleChips,
    NoticeBar,
    Panel,
    ReactiveFormsModule,
    TranslocoDirective,
  ],
  templateUrl: './brand-tab.html',
  styleUrls: ['../editor-card.css', './brand-tab.css'],
})
export class BrandTab {
  protected readonly facade = inject(CatalogueFacade);

  protected readonly form = this.facade.brandForm;
  protected readonly brands = this.facade.brands;
  protected readonly selected = this.facade.selectedBrand;
  protected readonly creating = computed(() => this.facade.editorMode() === 'create');
  protected readonly unavailable = computed(() => this.facade.unavailable().includes('brands'));

  protected onNameInput(value: string): void {
    this.facade.suggestCode('brands', value);
  }
}
