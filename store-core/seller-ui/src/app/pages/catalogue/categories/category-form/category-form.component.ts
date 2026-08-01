import {Component, Input, OnInit, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {
  NbActionsModule,
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbInputModule,
  NbOptionModule,
  NbSelectModule,
  NbSpinnerModule
} from '@nebular/theme';
import {QuillModule} from 'ngx-quill';
import {Store} from '../../../store-management/models/store';
import {CategoryFormFacade} from '../facades/category-form.facade';
import {CategoryFormService} from '../services/category-form.service';

@Component({
  selector: 'ngx-category-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    TranslateModule,
    NbActionsModule,
    NbButtonModule,
    NbCardModule,
    NbCheckboxModule,
    NbInputModule,
    NbOptionModule,
    NbSelectModule,
    NbSpinnerModule,
    QuillModule
  ],
  templateUrl: './category-form.component.html',
  styleUrls: ['./category-form.component.scss'],
  providers: [CategoryFormFacade, CategoryFormService]
})
export class CategoryFormComponent implements OnInit {
  @Input() category: any;
  @Input() store: Store;

  protected readonly facade = inject(CategoryFormFacade);

  ngOnInit(): void {
    this.facade.init(this.category, this.store);
  }
}
