import {Component, Input, inject} from '@angular/core';
import {NbCheckboxModule} from '@nebular/theme';
import {ActiveButtonFacade} from '../facades/active-button.facade';
import {ReadableProductGroup} from '../models/product-group.model';

@Component({
  selector: 'ngx-product-groups-active',
  standalone: true,
  imports: [NbCheckboxModule],
  template: `<nb-checkbox [checked]="value" (checkedChange)="clicked()"/>`,
  providers: [ActiveButtonFacade]
})
export class ActiveButtonComponent {
  @Input() value: boolean;
  @Input() rowData: ReadableProductGroup;
  @Input() store: string;

  private readonly facade = inject(ActiveButtonFacade);

  clicked(): void {
    this.value = this.facade.toggleActive(this.rowData, this.value);
  }
}
