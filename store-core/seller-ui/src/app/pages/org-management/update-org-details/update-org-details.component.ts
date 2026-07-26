import {Component, Input, OnInit, inject} from '@angular/core';
import {UpdateOrgDetailsFacade} from './facades/update-org-details.facade';
import {UpdateOrgDetailsFormService} from './services/update-org-details-form.service';

@Component({
  selector: 'ngx-update-org-details',
  standalone: false,
  templateUrl: './update-org-details.component.html',
  styleUrls: ['./update-org-details.component.scss'],
  providers: [UpdateOrgDetailsFacade, UpdateOrgDetailsFormService]
})
export class UpdateOrgDetailsComponent implements OnInit {
  @Input() title: string;
  protected readonly facade = inject(UpdateOrgDetailsFacade);

  ngOnInit() {
    this.facade.init();
  }
}
