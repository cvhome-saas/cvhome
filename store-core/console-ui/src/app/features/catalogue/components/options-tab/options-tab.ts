import {Component, computed, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';

import {EmptyState} from '@shared/ui/empty-state/empty-state';
import {FormField} from '@shared/ui/form-field/form-field';
import {Icon} from '@shared/ui/icon/icon';
import {LocaleSwitcher} from '@shared/ui/locale-switcher/locale-switcher';
import {NoticeBar} from '@shared/ui/notice-bar/notice-bar';
import {Panel} from '@shared/ui/panel/panel';
import {TextField} from '@shared/ui/text-field/text-field';
import {CatalogueFacade} from '../../facades/catalogue.facade';

/**
 * Store options — the vocabulary products vary by (Color, Size, …).
 *
 * Follows the brand tab's list-beside-editor shape, with one structural difference: an option is a
 * record *with children*. The editor manages the value rows inline — each row keeps its id so an
 * edit renames the value rather than re-creating it, which is what keeps the store-wide value ids
 * stable for every variant and facet that references them.
 *
 * Options are assigned to products in the product form's variants step, not here. Deleting an
 * option a product still uses is refused by the pod (409), and the toast names the refusal.
 */
@Component({
  selector: 'app-options-tab',
  imports: [
    EmptyState,
    FormField,
    Icon,
    LocaleSwitcher,
    NoticeBar,
    Panel,
    ReactiveFormsModule,
    TextField,
    TranslocoDirective,
  ],
  templateUrl: './options-tab.html',
  styleUrls: ['../../../../shared/styles/field.css', '../editor-card.css', './options-tab.css'],
})
export class OptionsTab {
  protected readonly facade = inject(CatalogueFacade);

  protected readonly form = this.facade.optionForm;
  protected readonly options = this.facade.options;
  protected readonly selected = this.facade.selectedOption;
  protected readonly creating = computed(() => this.facade.editorMode() === 'create');
  protected readonly unavailable = computed(() => this.facade.unavailable().includes('options'));

  protected onNameInput(value: string): void {
    this.facade.suggestCode('options', value);
  }

  /** A preview line for the list card: the value names, in display order. */
  protected valueSummary(names: readonly {name: string}[]): string {
    return names.map((value) => value.name).join(' · ');
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
