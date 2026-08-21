import {Component, computed, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';

import {Autocomplete, type AutocompleteOption} from '@shared/ui/autocomplete/autocomplete';
import {FieldError} from '@shared/ui/form-field/field-error';
import {Icon} from '@shared/ui/icon/icon';
import {NoticeBar} from '@shared/ui/notice-bar/notice-bar';
import {Panel} from '@shared/ui/panel/panel';
import {Toggle} from '@shared/ui/toggle/toggle';
import {CopyFields} from '../copy-fields/copy-fields';
import {LocaleChips} from '../locale-chips/locale-chips';
import {CatalogueFacade} from '../../facades/catalogue.facade';

/**
 * Product groups — named, code-addressed sets of products.
 *
 * In seller-ui and in the backend, and in no template: `Catalog (standalone).html` has three tabs
 * and this is a fourth. It is here because dropping a working feature during a migration is how a
 * migration loses features, and because the storefront reads these groups to build its "featured"
 * and "new arrivals" strips.
 *
 * **Not tags.** A group is a membership set with its own identity and its own per-language name;
 * there is no free-form labelling on a product anywhere on this platform. See lessons.md.
 *
 * **Membership is not part of the save.** Adding and removing a member are their own endpoints and
 * take effect immediately, which is why the picker below sits outside the form: making them part
 * of Save would mean a copy edit could silently drop a member.
 */
@Component({
  selector: 'app-group-tab',
  imports: [
    Autocomplete,
    CopyFields,
    FieldError,
    Icon,
    LocaleChips,
    NoticeBar,
    Panel,
    ReactiveFormsModule,
    Toggle,
    TranslocoDirective,
  ],
  templateUrl: './group-tab.html',
  /*
   * The picker styles are shared with the product form's related-products block — the same
   * interaction in two places, and they had already drifted apart once.
   */
  styleUrls: [
    '../editor-card.css',
    '../../../../shared/styles/product-picker.css',
    './group-tab.css',
  ],
})
export class GroupTab {
  protected readonly facade = inject(CatalogueFacade);

  protected readonly form = this.facade.groupForm;
  protected readonly groups = this.facade.groups;
  protected readonly selected = this.facade.selectedGroup;
  protected readonly creating = computed(() => this.facade.editorMode() === 'create');
  protected readonly unavailable = computed(() => this.facade.unavailable().includes('groups'));

  /** Search results, minus whoever is already in the group — offering to add a member twice is noise. */
  protected readonly candidates = computed<readonly AutocompleteOption[]>(() => {
    const members = new Set((this.selected()?.members ?? []).map((member) => member.id));
    return this.facade.memberResults().filter((option) => !members.has(option.id));
  });

  protected onNameInput(value: string): void {
    this.facade.suggestCode('groups', value);
  }

  protected addMember(option: AutocompleteOption): void {
    const code = this.selected()?.code;
    if (code) {
      this.facade.addMember(code, option.id);
    }
  }
}
