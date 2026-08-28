/** Console-native; not a port from seller-core. */
import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {BannerPlacement} from '@models/content';
import {CONTENT_PRIVATE} from './content-api';

/** What the storefront would show right now for a placement. */
export interface EffectiveBanner {
  readonly id: number;
  readonly placement: BannerPlacement;
  readonly position: number;
  readonly title?: string | null;
  readonly subtitle?: string | null;
  readonly ctaLabel?: string | null;
  readonly desktopUrl?: string | null;
  readonly startsAt?: string | null;
  readonly endsAt?: string | null;
}

@Injectable({providedIn: 'root'})
export class BannersService {
  private readonly crudService = inject(CrudService);

  effective(placement?: BannerPlacement): Observable<readonly EffectiveBanner[]> {
    return this.crudService.get(`${CONTENT_PRIVATE}/banners/effective`, {placement});
  }
}
