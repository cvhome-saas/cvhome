import {Component, Input, inject} from '@angular/core';
import {NbCheckboxModule} from '@nebular/theme';
import {UserStatusFacade} from './facades/user-status.facade';

@Component({
  selector: 'ngx-user-status',
  standalone: true,
  imports: [NbCheckboxModule],
  template: `
    <nb-checkbox [checked]="rowData?.active" (checkedChange)="facade.toggleStatus(rowData, store)"/>`,
  providers: [UserStatusFacade]
})
export class UserStatusComponent {
  @Input() store: string;
  @Input() rowData: any;
  protected readonly facade = inject(UserStatusFacade);
}
