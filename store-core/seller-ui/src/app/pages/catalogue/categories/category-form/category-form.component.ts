import {Component, Input, OnInit, inject} from '@angular/core';
import {Store} from '../../../store-management/models/store';
import {CategoryFormFacade} from '../facades/category-form.facade';
import {CategoryFormService} from '../services/category-form.service';

@Component({
  selector: 'ngx-category-form',
  standalone: false,
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
