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
 * builder does, twice — the canvas iframe and `/api/theme-manifest`.
 *
 * The authority is the pod's EXTERNAL endpoint: tenancy answers the pod, and its endpoint carries
 * scheme, host and — the local-stack wrinkle — the right port (`lcl` maps spg to a per-stack port
 * production never has). The store's subdomain label is prefixed onto that host, because the
 * storefront must be addressed per store by *hostname*: links inside the canvas iframe are
 * path-based, so a `?store=` query would silently fall back to the default store on the first
 * navigation. When the endpoint is missing or unusable, the hostname rule the domain section renders
 * (`{label}.{alis}-{pod}.{apex}`, console's own scheme) is the fallback; the manifest fetch itself is
 * the probe, so whichever origin actually answers is the one the canvas iframes.
 */
@Injectable({providedIn: 'root'})
export class StorefrontOriginService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(MerchantRouterService);
  private readonly saas = inject(SaasService);

  private candidates(): Observable<string[]> {
    return forkJoin({
      allocations: this.router.allocations().pipe(catchError(() => of([]))),
      saas: this.saas.saasProperties().pipe(catchError(() => of(null))),
      pod: this.saas.storePod().pipe(catchError(() => of(null))),
    }).pipe(
      map(({allocations, saas, pod}) => {
        const sub = allocations.find((record) => record.domainType === 'SUB_DOMAIN');
        if (!sub) {
          return [];
        }
        const origins: string[] = [];
        if (pod?.endpoint?.type === 'EXTERNAL' && pod.endpoint.endpoint) {
          try {
            const endpoint = new URL(pod.endpoint.endpoint);
            // host keeps the port; the store's label in front of it is the storefront's own hostname
            origins.push(`${endpoint.protocol}//${sub.domain}.${endpoint.host}`);
          } catch {
            // an unparsable endpoint just loses the primary candidate
          }
        }
        const target = podHostname(saas, pod);
        if (target) {
          origins.push(`${location.protocol}//${sub.domain}.${target}`);
        }
        return [...new Set(origins)];
      }),
    );
  }

  /**
   * What the active theme can render — the storefront's section catalogue merged with the theme's own
   * registry, fetched from the first candidate origin that answers (CORS-open, public). The builder's
   * forms are generated from the very registry the renderer resolves against, and the origin that
   * answered is the one the canvas iframes.
   */
  manifest(): Observable<{origin: string; manifest: ThemeManifest} | null> {
    return this.candidates().pipe(switchMap((origins) => this.tryOrigins(origins)));
  }

  private tryOrigins(origins: readonly string[]): Observable<{origin: string; manifest: ThemeManifest} | null> {
    const [origin, ...rest] = origins;
    if (!origin) {
      return of(null);
    }
    return this.http.get<ThemeManifest>(`${origin}/api/theme-manifest`).pipe(
      map((manifest) => ({origin, manifest})),
      catchError(() => this.tryOrigins(rest)),
    );
  }
}
