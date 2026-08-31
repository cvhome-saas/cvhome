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
 * builder does, twice — the canvas iframe and `/api/theme-manifest`. The hostname is the subdomain
 * rule the domain section renders (`{label}.{alis}-{pod}.{apex}`); the scheme is the console's own.
 *
 * The port is the local-stack wrinkle: in production the storefront answers on the default port, but
 * an `lcl` stack maps spg to `80 + offset` while serving the console through the gateway at
 * `8000 + offset` — so when the console's own location carries a port, the offset is read off it and
 * the derived `:80+offset` origin is probed first, with the bare origin as the fallback. The probe is
 * the manifest fetch itself, so whichever origin actually answers is the one the canvas iframes.
 */
@Injectable({providedIn: 'root'})
export class StorefrontOriginService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(MerchantRouterService);
  private readonly saas = inject(SaasService);

  /** Gateway's default port locally; the spg sits at 80 with the same stack offset. */
  private static readonly GATEWAY_BASE_PORT = 8000;

  private candidates(): Observable<string[]> {
    return forkJoin({
      allocations: this.router.allocations().pipe(catchError(() => of([]))),
      saas: this.saas.saasProperties().pipe(catchError(() => of(null))),
      pod: this.saas.storePod().pipe(catchError(() => of(null))),
    }).pipe(
      map(({allocations, saas, pod}) => {
        const sub = allocations.find((record) => record.domainType === 'SUB_DOMAIN');
        const target = podHostname(saas, pod);
        if (!sub || !target) {
          return [];
        }
        const bare = `${location.protocol}//${sub.domain}.${target}`;
        const consolePort = Number(location.port);
        const offset = consolePort - StorefrontOriginService.GATEWAY_BASE_PORT;
        if (Number.isFinite(consolePort) && offset > 0) {
          return [`${bare}:${80 + offset}`, bare];
        }
        return [bare];
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
