import {Injectable, inject, signal} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {StoreService} from '../../services/store.service';
import {ErrorService} from '../../../shared/services/error.service';
import {sideMenuLinks} from '../../services/constents';
import {ReadableMerchantStoreWithPod} from '../../models/store-service.model';

@Injectable()
export class StoreBrandingFacade {
  private readonly storeService = inject(StoreService);
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly errorService = inject(ErrorService);

  readonly store = signal<ReadableMerchantStoreWithPod>(null);
  readonly loading = signal<boolean>(false);
  readonly selectedItem = signal<string>('0');
  readonly sideMenuLinks = sideMenuLinks;

  init(): void {
    const storeCode = this.activatedRoute.snapshot.paramMap.get('code');
    if (!storeCode) return;

    this.loading.set(true);
    this.storeService.getStore(storeCode).subscribe({
      next: (it) => {
        this.store.set(it);
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
}
