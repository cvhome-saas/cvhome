import {Component, OnInit, inject} from '@angular/core';
import {NbSpinnerModule} from '@nebular/theme';
import {CategoryDetailFacade} from '../facades/category-detail.facade';
import {CategoryFormComponent} from '../category-form/category-form.component';

@Component({
  selector: 'ngx-category-detail',
  standalone: true,
  imports: [NbSpinnerModule, CategoryFormComponent],
  templateUrl: './category-detail.component.html',
  styleUrls: ['./category-detail.component.scss'],
  providers: [CategoryDetailFacade]
})
export class CategoryDetailComponent implements OnInit {
  protected readonly facade = inject(CategoryDetailFacade);

  ngOnInit(): void {
    this.facade.init();
  }
}
