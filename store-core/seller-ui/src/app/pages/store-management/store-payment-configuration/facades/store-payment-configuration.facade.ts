import {Injectable, inject, signal} from '@angular/core';
import {FormArray, FormGroup} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {StoreService} from 'seller-core/stores';
import {ApiErrorService} from 'seller-core';
import {NotificationService} from '../../../../core/notifications/notification.service';
import {StorePaymentConfigurationFormService} from '../services/store-payment-configuration.form.service';
import {zip} from 'rxjs';
import {sideMenuLinks} from 'seller-core/stores';
import {ReadableMerchantStoreWithPod} from 'seller-core/stores';

interface PaymentConfigFormValue {
  paymentType: string;
  apiKey: string;
  secretKey: string;
  webhookSecret: string;
  enabled: boolean;
  exists: boolean;
}

@Injectable()
export class StorePaymentConfigurationFacade {
  private readonly formService = inject(StorePaymentConfigurationFormService);
  private readonly storeService = inject(StoreService);
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly notify = inject(NotificationService);

  readonly isSubmited = signal<boolean>(false);
  readonly store = signal<ReadableMerchantStoreWithPod>(null);
  readonly paymentTypes = signal<string[]>([]);
  readonly loading = signal<boolean>(false);
  readonly selectedItem = signal<string>('7');
  readonly sideMenuLinks = sideMenuLinks;

  form!: FormGroup;

  get configForms(): FormArray {
    return this.form.get('configs') as FormArray;
  }

  init(): void {
    const storeCode = this.activatedRoute.snapshot.paramMap.get('code');
    if (!storeCode) return;

    this.loading.set(true);
    zip(
      this.storeService.getStore(storeCode),
      this.storeService.getSupportedPaymentTypes(),
      this.storeService.getPaymentConfigs(storeCode)
    ).subscribe({
      next: ([st, types, cfgs]) => {
        this.store.set(st);
        this.paymentTypes.set(types);
        this.form = this.formService.createForm(types, cfgs);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.apiErrors.notify(err);
      }
    });
  }

  route(link: string): void {
    const st = this.store();
    if (st) {
      this.router.navigate(['pages/store-management/' + link + '/', st.id]);
    }
  }

  save(): void {
    this.isSubmited.set(true);
    if (!this.form || !this.form.valid) {
      return;
    }

    const st = this.store();
    if (!st) return;

    const requests = this.form.value.configs.map((it: PaymentConfigFormValue) => {
      const payload = {
        paymentType: it.paymentType,
        apiKey: it.apiKey,
        secretKey: it.secretKey,
        webhookSecret: it.webhookSecret,
        enabled: it.enabled
      };

      if (it.exists) {
        return this.storeService.updatePaymentConfig(st.id, it.paymentType, payload);
      } else {
        return this.storeService.savePaymentConfig(st.id, payload);
      }
    });

    zip(...requests).subscribe({
      next: () => {
        this.notify.success('STORE.PAYMENT_CONFIGURATION_UPDATED');
        this.init();
      },
      error: (err) => {
        this.apiErrors.notify(err);
      }
    });
  }
}
