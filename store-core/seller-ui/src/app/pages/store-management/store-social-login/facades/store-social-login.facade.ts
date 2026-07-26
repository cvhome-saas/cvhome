import {Injectable, inject, signal} from '@angular/core';
import {FormArray, FormGroup} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {NbToastrService} from '@nebular/theme';
import {TranslateService} from '@ngx-translate/core';
import {StoreService} from '../../services/store.service';
import {ErrorService} from '../../../shared/services/error.service';
import {StoreSocialLoginFormService} from '../services/store-social-login.form.service';
import {zip} from 'rxjs';
import {sideMenuLinks} from '../../services/constents';

@Injectable()
export class StoreSocialLoginFacade {
  private readonly formService = inject(StoreSocialLoginFormService);
  private readonly storeService = inject(StoreService);
  private readonly toastr = inject(NbToastrService);
  private readonly translate = inject(TranslateService);
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly errorService = inject(ErrorService);

  readonly isSubmited = signal<boolean>(false);
  readonly store = signal<any>(null);
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
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
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

    const configs = this.form.value.configs.map((it: any) => ({
      id: {providerId: it.providerId},
      appId: it.appId,
      appSecret: it.appSecret,
      enabled: it.enabled
    }));

    this.storeService.updateSocialLoginConfigs(st.id, configs).subscribe({
      next: () => {
        this.toastr.success(this.translate.instant('STORE.SOCIAL_LOGIN_UPDATED'));
      },
      error: (err) => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }
}
