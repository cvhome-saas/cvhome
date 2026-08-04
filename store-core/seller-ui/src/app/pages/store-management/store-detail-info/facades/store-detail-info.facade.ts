import {Injectable, inject, signal} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {StoreService} from '../../services/store.service';
import {ApiErrorService} from '../../../../core/errors/api-error.service';
import {ReadableMerchantStoreWithPod} from '../../models/store-service.model';

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
