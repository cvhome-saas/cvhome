import {Component, OnInit, inject} from '@angular/core';
import {TranslateModule} from '@ngx-translate/core';
import {NbCardModule, NbOptionModule, NbSelectModule} from '@nebular/theme';
import {StoreBrandingFacade} from './facades/store-branding.facade';
import {StoreBrandingLogoComponent} from './store-branding-logo.componants';
import {StoreBrandingBannerComponent} from './store-branding-banner.componants';

@Component({
  selector: 'ngx-store-branding',
  standalone: true,
  imports: [TranslateModule, NbCardModule, NbOptionModule, NbSelectModule, StoreBrandingLogoComponent, StoreBrandingBannerComponent],
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
