import {Component, OnInit, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {NbButtonModule, NbCardModule, NbInputModule, NbOptionModule, NbSelectModule} from '@nebular/theme';
import {StoreSocialLinksFacade} from './facades/store-social-links.facade';
import {StoreSocialLinksFormService} from './services/store-social-links.form.service';

@Component({
  selector: 'ngx-store-social-links',
  standalone: true,
  imports: [ReactiveFormsModule, TranslateModule, NbButtonModule, NbCardModule, NbInputModule, NbOptionModule, NbSelectModule],
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
