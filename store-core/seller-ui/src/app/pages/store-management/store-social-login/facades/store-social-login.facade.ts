import {Injectable, inject, signal} from '@angular/core';
import {FormArray, FormGroup} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {StoreService} from 'seller-core/stores';
import {ApiErrorService} from 'seller-core';
import {NotificationService} from '../../../../core/notifications/notification.service';
import {StoreSocialLoginFormService} from '../services/store-social-login.form.service';
import {zip} from 'rxjs';
import {sideMenuLinks} from 'seller-core/stores';
import {ReadableMerchantStoreWithPod} from 'seller-core/stores';

interface SocialLoginConfigFormValue {
  providerId: string;
  appId: string;
  appSecret: string;
  enabled: boolean;
}

@Injectable()
export class StoreSocialLoginFacade {
  private readonly formService = inject(StoreSocialLoginFormService);
  private readonly storeService = inject(StoreService);
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly notify = inject(NotificationService);

  readonly isSubmited = signal<boolean>(false);
  readonly store = signal<ReadableMerchantStoreWithPod>(null);
  readonly providers = signal<string[]>([]);
  readonly loading = signal<boolean>(false);
  readonly selectedItem = signal<string>('6');
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
      this.storeService.getSupportedSocialLoginProviders(),
      this.storeService.getSocialLoginConfigs(storeCode)
    ).subscribe({
      next: ([st, provs, cfgs]) => {
        this.store.set(st);
        this.providers.set(provs);
        this.form = this.formService.createForm(provs, cfgs);
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

    const configs = this.form.value.configs.map((it: SocialLoginConfigFormValue) => ({
      providerId: it.providerId,
      appId: it.appId,
      appSecret: it.appSecret,
      enabled: it.enabled
    }));

    this.storeService.updateSocialLoginConfigs(st.id, configs).subscribe({
      next: () => {
        this.notify.success('STORE.SOCIAL_LOGIN_UPDATED');
      },
      error: (err) => {
        this.apiErrors.notify(err);
      }
    });
  }
}
