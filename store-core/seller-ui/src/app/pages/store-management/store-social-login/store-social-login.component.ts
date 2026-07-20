import {Component, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormGroup} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {StoreService} from '../services/store.service';
import {NbToastrService} from "@nebular/theme";
import {TranslateService} from '@ngx-translate/core';
import {ErrorService} from "../../shared/services/error.service";
import {zip} from "rxjs";
import {sideMenuLinks} from "../services/constents";

@Component({
  selector: 'ngx-store-social-login',
  standalone: false,
  templateUrl: './store-social-login.component.html',
  styleUrls: ['./store-social-login.component.scss']
})
export class StoreSocialLoginComponent implements OnInit {

  protected readonly sideMenuLinks = sideMenuLinks;
  isSubmited = false
  store;
  providers: string[] = [];
  loading = false;
  selectedItem = '6';
  form: FormGroup;

  constructor(
    private storeService: StoreService,
    private fb: FormBuilder,
    private toastr: NbToastrService,
    private translate: TranslateService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private errorService: ErrorService
  ) {
  }

  get configForms() {
    return this.form.get('configs') as FormArray;
  }

  ngOnInit() {
    const storeCode = this.activatedRoute.snapshot.paramMap.get('code');
    zip(
      this.storeService.getStore(storeCode),
      this.storeService.getSupportedSocialLoginProviders(),
      this.storeService.getSocialLoginConfigs(storeCode)
    ).subscribe({
      next: ([store, providers, configs]) => {
        this.store = store;
        this.providers = providers;
        this.createForm(configs);
      },
      error: (err) => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  route(link) {
    this.router.navigate(['pages/store-management/' + link + "/", this.store.id]);
  }

  save() {
    this.isSubmited = true;
    if (!this.form.valid) {
      return;
    }
    const configs = this.form.value.configs.map(it => ({
      id: {providerId: it.providerId},
      appId: it.appId,
      appSecret: it.appSecret,
      enabled: it.enabled
    }));

    this.storeService.updateSocialLoginConfigs(this.store.id, configs)
      .subscribe(() => {
        this.toastr.success(this.translate.instant('STORE.SOCIAL_LOGIN_UPDATED'));
      }, err => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      });
  }

  private createForm(configs: any[]) {
    const configGroups = this.providers.map(provider => {
      const config = configs.find(c => c.id.providerId === provider) || {};
      return this.fb.group({
        providerId: [provider],
        appId: [config.appId || ''],
        appSecret: [config.appSecret || ''],
        enabled: [config.enabled !== undefined ? config.enabled : false]
      });
    });
    this.form = this.fb.group({
      configs: this.fb.array(configGroups)
    });
  }
}
