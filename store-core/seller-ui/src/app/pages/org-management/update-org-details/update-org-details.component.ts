import {Component, DestroyRef, OnInit, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {
  NbButtonModule,
  NbCardModule,
  NbOptionModule,
  NbSelectModule,
  NbSpinnerModule
} from '@nebular/theme';
import {UpdateOrgDetailsFacade} from './facades/update-org-details.facade';
import {UpdateOrgDetailsFormService} from './services/update-org-details-form.service';

@Component({
  selector: 'ngx-update-org-details',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    TranslateModule,
    NbButtonModule,
    NbCardModule,
    NbOptionModule,
    NbSelectModule,
    NbSpinnerModule
  ],
  templateUrl: './update-org-details.component.html',
  styleUrls: ['./update-org-details.component.scss'],
  providers: [UpdateOrgDetailsFacade, UpdateOrgDetailsFormService]
})
export class UpdateOrgDetailsComponent implements OnInit {
  protected readonly facade = inject(UpdateOrgDetailsFacade);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit() {
    this.facade.init(this.destroyRef);
  }
}
