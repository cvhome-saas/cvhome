import {Component, OnInit, inject, DestroyRef} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbInputModule,
  NbOptionModule,
  NbSelectModule,
  NbSpinnerModule
} from '@nebular/theme';
import {TypeDetailsFacade} from '../facades/type-details.facade';
import {TypeFormService} from '../services/type-form.service';

@Component({
  selector: 'ngx-type-details',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    TranslateModule,
    NbButtonModule,
    NbCardModule,
    NbCheckboxModule,
    NbInputModule,
    NbOptionModule,
    NbSelectModule,
    NbSpinnerModule
  ],
  templateUrl: './type-details.component.html',
  styleUrls: ['./type-details.component.scss'],
  providers: [TypeDetailsFacade, TypeFormService]
})
export class TypeDetailsComponent implements OnInit {
  protected readonly facade = inject(TypeDetailsFacade);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.facade.init(this.destroyRef);
  }
}
