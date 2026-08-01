import {Component, OnInit, inject} from '@angular/core';
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
import {QuillModule} from 'ngx-quill';
import {AddPageFacade} from './facades/add-page.facade';
import {AddPageFormService} from './services/add-page.form.service';

@Component({
  selector: 'app-add-page',
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
    NbSpinnerModule,
    QuillModule
  ],
  templateUrl: './add-page.component.html',
  styleUrls: ['./add-page.component.scss'],
  providers: [AddPageFacade, AddPageFormService]
})
export class AddPageComponent implements OnInit {
  protected readonly facade = inject(AddPageFacade);

  ngOnInit(): void {
    this.facade.init();
  }
}
