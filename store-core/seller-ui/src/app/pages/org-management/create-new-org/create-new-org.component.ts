import {Component, OnInit, inject} from '@angular/core';
import {CreateNewOrgFacade} from './facades/create-new-org.facade';
import {CreateNewOrgFormService} from './services/create-new-org-form.service';

@Component({
  selector: 'ngx-create-new-org',
  standalone: false,
  templateUrl: './create-new-org.component.html',
  styleUrls: ['./create-new-org.component.scss'],
  providers: [CreateNewOrgFacade, CreateNewOrgFormService]
})
export class CreateNewOrgComponent implements OnInit {
  protected readonly facade = inject(CreateNewOrgFacade);

  ngOnInit() {
    this.facade.init();
  }
}
