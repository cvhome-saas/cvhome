import {Component, OnInit, inject} from '@angular/core';
import {BrandDetailsFacade} from '../facades/brand-details.facade';

@Component({
  selector: 'ngx-brand-details',
  standalone: false,
  templateUrl: './brand-details.component.html',
  styleUrls: ['./brand-details.component.scss'],
  providers: [BrandDetailsFacade]
})
export class BrandDetailsComponent implements OnInit {
  protected readonly facade = inject(BrandDetailsFacade);

  ngOnInit(): void {
    this.facade.init();
  }
}
