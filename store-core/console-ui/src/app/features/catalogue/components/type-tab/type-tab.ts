import {Component, computed, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';

import {EmptyState, FormField, TextField, Icon, NoticeBar, Panel, Toggle, LocaleSwitcher} from '@cvhome-saas/ui-kit/ui';
import {CopyFields} from '../copy-fields/copy-fields';
import {CatalogueFacade} from '../../facades/catalogue.facade';

/**
 * Product types.
 *
 * **This is the tab the design and the platform disagree about most.** `Catalog (standalone).html`
 * gives a type an attribute list — name, kind, required, variant-defining — a "used by categories"
 * chip row and a product count, and builds its whole right-hand panel on them. None exists:
 * `ProductAttributeOptionApi` and `ProductPropertySetApi` are mapped, but nothing links a *type* to
 * the attributes a product of that type must carry, no relation joins a type to a category, and no
 * endpoint counts a type's products. See lessons.md, "Catalogue — a product type carries no
 * attribute definitions".
 *
 * What a type actually is: a name, a code, and two flags — whether the storefront shows it and
 * whether products of it can be added to a basket. That is what this tab edits, and the notice
 * below says so, because a type that looks this thin without explanation reads as a broken screen.
 */
@Component({
  selector: 'app-type-tab',
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
    Toggle,
    TranslocoDirective,
  ],
  templateUrl: './type-tab.html',
  styleUrls: ['../../../../shared/styles/field.css', '../editor-card.css', './type-tab.css'],
})
export class TypeTab {
  protected readonly facade = inject(CatalogueFacade);

  protected readonly form = this.facade.typeForm;
  protected readonly types = this.facade.types;
  protected readonly selected = this.facade.selectedType;
  protected readonly creating = computed(() => this.facade.editorMode() === 'create');
  protected readonly unavailable = computed(() => this.facade.unavailable().includes('types'));

  protected onNameInput(value: string): void {
    this.facade.suggestCode('types', value);
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
