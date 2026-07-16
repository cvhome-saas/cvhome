import {Component, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormGroup} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {StoreService} from '../services/store.service';
import {NbToastrService} from "@nebular/theme";
import {TranslateService} from '@ngx-translate/core';
import {ErrorService} from "../../shared/services/error.service";
import {zip} from "rxjs";

@Component({
  selector: 'ngx-store-payment-configuration',
  standalone: false,
  templateUrl: './store-payment-configuration.component.html',
  styleUrls: ['./store-payment-configuration.component.scss']
})
export class StorePaymentConfigurationComponent implements OnInit {
  isSubmited = false
  store;
  paymentTypes: string[] = [];
  loading = false;
  selectedItem = '7';
  sidemenuLinks = [
    {
      id: '0',
      title: 'Store branding',
      key: 'COMPONENTS.STORE_BRANDING',
      link: 'store-branding'
    },
    {
      id: '1',
      title: 'Store home page',
      key: 'COMPONENTS.STORE_LANDING',
      link: 'store-landing'
    },
    {
      id: '2',
      title: 'Store domain',
      key: 'COMPONENTS.STORE_DOMAIN',
      link: 'store-domain'
    },
    {
      id: '3',
      title: 'Store Social Links',
      key: 'COMPONENTS.STORE_SOCIAL_LINKS',
      link: 'store-social-links'
    },
    {
      id: '4',
      title: 'Store Slider Images',
      key: 'COMPONENTS.STORE_SLIDER_IMAGES',
      link: 'store-slider-images'
    },
    {
      id: '5',
      title: 'Store details',
      key: 'COMPONENTS.STORE_DETAILS',
      link: 'store'
    },
    {
      id: '6',
      title: 'Store Social Login',
      key: 'COMPONENTS.STORE_SOCIAL_LOGIN',
      link: 'store-social-login'
    },
    {
      id: '7',
      title: 'Store Payment Configuration',
      key: 'COMPONENTS.STORE_PAYMENT_CONFIGURATION',
      link: 'store-payment-configuration'
    }
  ];
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
      this.storeService.getSupportedPaymentTypes(),
      this.storeService.getPaymentConfigs(storeCode)
    ).subscribe({
      next: ([store, types, configs]) => {
        this.store = store;
        this.paymentTypes = types;
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

    const requests = this.form.value.configs.map(it => {
      const payload = {
        paymentType: it.paymentType,
        apiKey: it.apiKey,
        secretKey: it.secretKey,
        webhookSecret: it.webhookSecret,
        enabled: it.enabled
      };

      if (it.id) {
        return this.storeService.updatePaymentConfig(this.store.id, it.id, payload);
      } else {
        return this.storeService.savePaymentConfig(this.store.id, payload);
      }
    });

    zip(...requests).subscribe({
      next: () => {
        this.toastr.success(this.translate.instant('STORE.PAYMENT_CONFIGURATION_UPDATED'));
        this.ngOnInit(); // Reload to get IDs for new configs
      },
      error: (err) => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  private createForm(configs: any[]) {
    const configGroups = this.paymentTypes.map(type => {
      const config = configs.find(c => c.paymentType === type) || {};
      return this.fb.group({
        id: [config.id || null],
        paymentType: [type],
        apiKey: [config.apiKey || ''],
        secretKey: [config.secretKey || ''],
        webhookSecret: [config.webhookSecret || ''],
        enabled: [config.enabled !== undefined ? config.enabled : false]
      });
    });
    this.form = this.fb.group({
      configs: this.fb.array(configGroups)
    });
  }
}
