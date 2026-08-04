import {Component, OnInit, inject, DestroyRef} from '@angular/core';
import {BrandCreationFacade} from '../facades/brand-creation.facade';
import {BrandFormComponent} from '../brand-form/brand-form.component';

@Component({
  selector: 'ngx-brand-creation',
  standalone: true,
  imports: [BrandFormComponent],
  templateUrl: './brand-creation.component.html',
  styleUrls: ['./brand-creation.component.scss'],
  providers: [BrandCreationFacade]
})
export class BrandCreationComponent implements OnInit {
  protected readonly facade = inject(BrandCreationFacade);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.facade.init(this.destroyRef);
  }
}
