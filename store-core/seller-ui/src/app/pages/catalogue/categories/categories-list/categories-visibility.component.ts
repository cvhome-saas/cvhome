import {Component, Input, inject} from '@angular/core';
import {NbCheckboxModule} from '@nebular/theme';
import {CategoriesVisibilityFacade} from '../facades/categories-visibility.facade';

@Component({
  selector: 'ngx-categories-visibility',
  standalone: true,
  imports: [NbCheckboxModule],
  template: `
    <nb-checkbox [checked]="value" (checkedChange)="facade.toggleVisibility(rowData, value)"/>`,
  providers: [CategoriesVisibilityFacade]
})
export class CategoriesVisibilityComponent {
  @Input() value: boolean;
  @Input() store: string;
  @Input() rowData: any;

  protected readonly facade = inject(CategoriesVisibilityFacade);
}
