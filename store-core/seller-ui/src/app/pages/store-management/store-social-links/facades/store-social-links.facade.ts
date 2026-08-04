import {Injectable, inject, signal} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {StoreService} from '../../services/store.service';
import {ApiErrorService} from '../../../../core/errors/api-error.service';
import {NotificationService} from '../../../../core/notifications/notification.service';
import {StoreSocialLinksFormService} from '../services/store-social-links.form.service';
import {zip} from 'rxjs';
import {sideMenuLinks} from '../../services/constents';
import {ReadableMerchantStoreWithPod, SocialLink} from '../../models/store-service.model';

@Injectable()
export class StoreSocialLinksFacade {
  private readonly formService = inject(StoreSocialLinksFormService);
  private readonly storeService = inject(StoreService);
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly notify = inject(NotificationService);

  readonly isSubmited = signal<boolean>(false);
  readonly store = signal<ReadableMerchantStoreWithPod>(null);
  readonly providers = signal<string[]>([]);
  readonly loading = signal<boolean>(false);
  readonly selectedItem = signal<string>('3');
  readonly sideMenuLinks = sideMenuLinks;

  form!: FormGroup;

  init(): void {
    const storeCode = this.activatedRoute.snapshot.paramMap.get('code');
    if (!storeCode) return;

    this.loading.set(true);
    zip(
      this.storeService.getStore(storeCode),
      this.storeService.getSupportedSocialLinkProviders()
    ).subscribe({
      next: ([st, provs]) => {
        this.store.set(st);
        this.providers.set(provs);
        this.form = this.formService.createForm(st, provs);
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

    const socialLinks: SocialLink[] = [];
    Object.keys(this.form.value).forEach((it) => {
      socialLinks.push({provider: it, url: this.form.value[it]});
    });

    this.storeService.updateStoreSocialLinks(st.id, { socialLinks }).subscribe({
      next: () => {
        this.notify.success('STORE.SOCIAL_LINKS_UPDATED');
      },
      error: (err) => {
        this.apiErrors.notify(err);
      }
    });
  }
}
