import {Component, Input, OnInit, inject, DestroyRef} from '@angular/core';
import {Store} from '../../../store-management/models/store';
import {BrandFormFacade} from '../facades/brand-form.facade';
import {BrandFormService} from '../services/brand-form.service';

@Component({
  selector: 'ngx-brand-form',
  standalone: false,
  templateUrl: './brand-form.component.html',
  styleUrls: ['./brand-form.component.scss'],
  providers: [BrandFormFacade, BrandFormService]
})
export class BrandFormComponent implements OnInit {
  @Input() brand: any;
  @Input() title: string;
  @Input() store: Store;

  protected readonly facade = inject(BrandFormFacade);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.facade.init(this.brand, this.store, this.destroyRef);
  }
}
