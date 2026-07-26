import {Component, OnInit, inject} from '@angular/core';
import {StoreSocialLinksFacade} from './facades/store-social-links.facade';
import {StoreSocialLinksFormService} from './services/store-social-links.form.service';

@Component({
  selector: 'ngx-store-social-links',
  standalone: false,
  templateUrl: './store-social-links.component.html',
  styleUrls: ['./store-social-links.component.scss'],
  providers: [StoreSocialLinksFacade, StoreSocialLinksFormService]
})
export class StoreSocialLinksComponent implements OnInit {
  protected readonly facade = inject(StoreSocialLinksFacade);

  ngOnInit() {
    this.facade.init();
  }
}
