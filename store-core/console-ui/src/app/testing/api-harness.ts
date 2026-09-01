import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {Provider, ProviderToken} from '@angular/core';
import {TestBed} from '@angular/core/testing';

import {UI_KIT_CONFIG, REQUEST_CONTEXT} from '@cvhome-saas/ui-kit';

/** The store every ported service is scoped to, and the one these specs assert is stamped. */
export const TEST_STORE = 'ORG1-STORE1';

/** The base the console's config puts in front of every path. Empty, so URLs read as written. */
const API_BASE = '';

/**
 * Stands up one api-tier service against `HttpTestingController`.
 *
 * **What these specs are for.** The api tier is where the console's knowledge of the backend lives
 * — a path, a verb, a parameter name, the shape of a body — and it was the least tested part of the
 * app: one spec for twenty-four files. None of that knowledge is checked by the compiler, because a
 * wrong path is still a string. A spec here turns a contract change into a red test rather than a
 * QA finding two modules later.
 *
 * **`?store=` is stamped by `CrudService`, not by callers**, through `REQUEST_CONTEXT`. Faking the
 * token rather than the whole selected-store service keeps that in the picture — a service that
 * stops going through `CrudService` would silently lose its tenant scope, and these assertions are
 * what would notice.
 */
export function apiHarness<T>(
  service: ProviderToken<T>,
  extraProviders: Provider[] = [],
): {service: T; http: HttpTestingController} {
  TestBed.configureTestingModule({
    providers: [
      provideHttpClient(),
      provideHttpClientTesting(),
      {provide: UI_KIT_CONFIG, useValue: {apiUrl: API_BASE, loginUrl: '', logoutUrl: ''}},
      /*
       * Mirrors `SelectedStoreRequestContext`: an explicit store wins over the open one. A fake
       * that ignored the argument made `storeInfo('ORG1-STORE2')` look like it queried the open
       * store — the spec caught the fake, not the service, which is the right way round.
       */
      {
        provide: REQUEST_CONTEXT,
        useValue: {params: (explicit?: string) => ({store: explicit ?? TEST_STORE})},
      },
      ...extraProviders,
    ],
  });
  return {
    service: TestBed.inject(service),
    http: TestBed.inject(HttpTestingController),
  };
}

/** Fails the spec if a request was made that no case accounted for. Call it in `afterEach`. */
export function verifyNoPendingRequests(): void {
  TestBed.inject(HttpTestingController).verify();
}
