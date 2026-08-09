import {Injectable, inject, signal} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {StoreService} from 'seller-core/stores';
import {ApiErrorService} from 'seller-core';
import {ReadableMerchantStoreWithPod} from 'seller-core/stores';

@Injectable()
export class StoreDetailInfoFacade {
  private readonly storeService = inject(StoreService);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly apiErrors = inject(ApiErrorService);

  readonly store = signal<ReadableMerchantStoreWithPod>(null);

  init(): void {
    const storeCode = this.activatedRoute.snapshot.paramMap.get('code');
    if (!storeCode) return;

    this.storeService.getStore(storeCode).subscribe({
      next: (res) => this.store.set(res),
      error: (err) => this.apiErrors.notify(err)
    });
  }
}
