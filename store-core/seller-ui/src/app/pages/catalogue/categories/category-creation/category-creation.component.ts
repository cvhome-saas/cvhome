import {Component, OnInit, inject} from '@angular/core';
import {CategoryCreationFacade} from '../facades/category-creation.facade';
import {CategoryFormComponent} from '../category-form/category-form.component';

@Component({
  selector: 'ngx-category-creation',
  standalone: true,
  imports: [CategoryFormComponent],
  templateUrl: './category-creation.component.html',
  styleUrls: ['./category-creation.component.scss'],
  providers: [CategoryCreationFacade]
})
export class CategoryCreationComponent implements OnInit {
  protected readonly facade = inject(CategoryCreationFacade);

  ngOnInit(): void {
    this.facade.init();
  }
}
