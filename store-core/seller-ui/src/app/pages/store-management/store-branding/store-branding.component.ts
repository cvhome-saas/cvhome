import {Component, OnInit, inject} from '@angular/core';
import {StoreBrandingFacade} from './facades/store-branding.facade';

@Component({
  selector: 'ngx-store-branding',
  standalone: false,
  templateUrl: './store-branding.component.html',
  styleUrls: ['./store-branding.component.scss'],
  providers: [StoreBrandingFacade]
})
export class StoreBrandingComponent implements OnInit {
  protected readonly facade = inject(StoreBrandingFacade);

  ngOnInit() {
    this.facade.init();
  }
}
