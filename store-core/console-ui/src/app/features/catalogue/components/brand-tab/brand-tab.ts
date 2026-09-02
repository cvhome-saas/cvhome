import {Component, computed, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';

import {EmptyState, FormField, TextField, Icon, NoticeBar, Panel, LocaleSwitcher} from '@cvhome-saas/ui-kit/ui';
import {CopyFields} from '../copy-fields/copy-fields';
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
    EmptyState,
    FormField,
    TextField,
    Icon,
    LocaleSwitcher,
    NoticeBar,
    Panel,
    ReactiveFormsModule,
    TranslocoDirective,
  ],
  templateUrl: './brand-tab.html',
  styleUrls: ['../../../../shared/styles/field.css', '../editor-card.css', './brand-tab.css'],
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
  /**
   * Where the code's uniqueness check has got to, in the shared vocabulary.
   *
   * `taken` only ever shows while creating: an existing record's code is disabled, and the check
   * that lands on a disabled control is the bug `uniqueAsync` guards against.
   */
  protected codeCheck(): 'idle' | 'pending' | 'free' | 'taken' {
    const code = this.form.controls.code;
    if (code.pending) {
      return 'pending';
    }
    if (code.hasError('codeTaken')) {
      return 'taken';
    }
    return this.creating() && code.valid && code.value ? 'free' : 'idle';
  }

}
