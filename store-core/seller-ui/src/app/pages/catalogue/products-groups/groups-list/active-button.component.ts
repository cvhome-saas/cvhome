import {Component, Input, inject} from '@angular/core';
import {ActiveButtonFacade} from '../facades/active-button.facade';

@Component({
  selector: 'ngx-product-groups-active',
  standalone: false,
  template: `<nb-checkbox [checked]="value" (checkedChange)="clicked()"/>`,
  providers: [ActiveButtonFacade]
})
export class ActiveButtonComponent {
  @Input() value: boolean;
  @Input() rowData: any;
  @Input() store: string;

  private readonly facade = inject(ActiveButtonFacade);

  clicked(): void {
    this.value = this.facade.toggleActive(this.rowData, this.value);
  }
}
