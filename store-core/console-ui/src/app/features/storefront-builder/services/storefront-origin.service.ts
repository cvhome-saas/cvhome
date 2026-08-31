/** Console-native; not a port from seller-core. */
import {Injectable, inject} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, forkJoin, map, of, switchMap} from 'rxjs';
import {catchError} from 'rxjs/operators';

import {MerchantRouterService} from '@api/merchant/router.service';
import {SaasService, podHostname} from '@api/tenancy/saas.service';
import type {ThemeManifest} from '@models/layout';

/**
 * Where the current store's storefront lives, as an origin the builder can iframe and fetch from.
 *
 * The console has never needed the storefront's address before (the gap `page-editor` noted); the
 * builder does, twice — the canvas iframe and `/api/theme-manifest`. The subdomain is the storefront
 * (`{label}.{alis}-{pod}.{apex}`, the same rule the domain section renders); the scheme is the
 * console's own, which holds in both the local http stack and production https.
 */
@Injectable({providedIn: 'root'})
export class StorefrontOriginService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(MerchantRouterService);
  private readonly saas = inject(SaasService);

  origin(): Observable<string | null> {
    return forkJoin({
      allocations: this.router.allocations().pipe(catchError(() => of([]))),
      saas: this.saas.saasProperties().pipe(catchError(() => of(null))),
      pod: this.saas.storePod().pipe(catchError(() => of(null))),
    }).pipe(
      map(({allocations, saas, pod}) => {
        const sub = allocations.find((record) => record.domainType === 'SUB_DOMAIN');
        const target = podHostname(saas, pod);
        if (!sub || !target) {
          return null;
        }
        return `${location.protocol}//${sub.domain}.${target}`;
      }),
    );
  }

  /**
   * What the active theme can render — the storefront's section catalogue merged with the theme's own
   * registry. Fetched from the storefront origin itself (CORS-open, public), so the builder's forms are
   * generated from the very registry the renderer resolves against.
   */
  manifest(): Observable<{origin: string; manifest: ThemeManifest} | null> {
    return this.origin().pipe(
      switchMap((origin) => {
        if (!origin) {
          return of(null);
        }
        return this.http
          .get<ThemeManifest>(`${origin}/api/theme-manifest`)
          .pipe(map((manifest) => ({origin, manifest})), catchError(() => of(null)));
      }),
    );
  }
}
