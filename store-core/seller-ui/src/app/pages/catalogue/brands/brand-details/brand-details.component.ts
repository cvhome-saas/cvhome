import {Component, OnInit, inject, DestroyRef} from '@angular/core';
import {BrandDetailsFacade} from '../facades/brand-details.facade';
import {BrandFormComponent} from '../brand-form/brand-form.component';

@Component({
  selector: 'ngx-brand-details',
  standalone: true,
  imports: [BrandFormComponent],
  templateUrl: './brand-details.component.html',
  styleUrls: ['./brand-details.component.scss'],
  providers: [BrandDetailsFacade]
})
export class BrandDetailsComponent implements OnInit {
  protected readonly facade = inject(BrandDetailsFacade);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.facade.init(this.destroyRef);
  }
}
