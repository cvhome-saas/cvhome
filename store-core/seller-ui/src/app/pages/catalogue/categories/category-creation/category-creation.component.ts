import {Component, OnInit, inject} from '@angular/core';
import {CategoryCreationFacade} from '../facades/category-creation.facade';

@Component({
  selector: 'ngx-category-creation',
  standalone: false,
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
