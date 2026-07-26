import {Component, OnInit, inject} from '@angular/core';
import {StoreSocialLoginFacade} from './facades/store-social-login.facade';
import {StoreSocialLoginFormService} from './services/store-social-login.form.service';

@Component({
  selector: 'ngx-store-social-login',
  standalone: false,
  templateUrl: './store-social-login.component.html',
  styleUrls: ['./store-social-login.component.scss'],
  providers: [StoreSocialLoginFacade, StoreSocialLoginFormService]
})
export class StoreSocialLoginComponent implements OnInit {
  protected readonly facade = inject(StoreSocialLoginFacade);

  ngOnInit() {
    this.facade.init();
  }
}
