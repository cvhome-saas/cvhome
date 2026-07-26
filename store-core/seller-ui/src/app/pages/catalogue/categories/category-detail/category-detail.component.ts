import {Component, OnInit, inject} from '@angular/core';
import {CategoryDetailFacade} from '../facades/category-detail.facade';

@Component({
  selector: 'ngx-category-detail',
  standalone: false,
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
