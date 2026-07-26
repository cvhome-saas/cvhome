import {Component, OnInit, inject} from '@angular/core';
import {BrandCreationFacade} from '../facades/brand-creation.facade';

@Component({
  selector: 'ngx-brand-creation',
  standalone: false,
  templateUrl: './brand-creation.component.html',
  styleUrls: ['./brand-creation.component.scss'],
  providers: [BrandCreationFacade]
})
export class BrandCreationComponent implements OnInit {
  protected readonly facade = inject(BrandCreationFacade);

  ngOnInit(): void {
    this.facade.init();
  }
}
