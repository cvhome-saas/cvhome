import {Component, OnInit, inject} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
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
import {AddBoxFacade} from './facades/add-box.facade';
import {AddBoxFormService} from './services/add-box.form.service';

@Component({
  selector: 'app-add-box',
  standalone: true,
  imports: [
    FormsModule,
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
  templateUrl: './add-box.component.html',
  styleUrls: ['./add-box.component.scss'],
  providers: [AddBoxFacade, AddBoxFormService]
})
export class AddBoxComponent implements OnInit {
  protected readonly facade = inject(AddBoxFacade);

  ngOnInit(): void {
    this.facade.init();
  }
}
