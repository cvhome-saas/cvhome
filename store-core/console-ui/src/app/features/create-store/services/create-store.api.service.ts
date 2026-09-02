import {Injectable, inject} from '@angular/core';
import {EMPTY, Observable, catchError, forkJoin} from 'rxjs';

import {PodService} from '@api/pod-registry/pod.service';
import {ManagerStoreService} from '@api/tenancy/manager-store.service';
import {optionalList} from '@cvhome-saas/ui-kit';
import type {Pod} from '@models/pod';
import type {CreateStoreRequest, ManagerStore} from '@models/tenancy';

/** The reference lists the form's selects are built from. */
export interface CreateStoreReference {
  readonly themes: readonly string[];
  readonly colorThemes: readonly string[];
}

/**
 * Store creation's calls, in the seam every other feature has.
 *
 * The facade used to inject `ManagerStoreService` and `PodService` straight from the api tier — one
 * of the two features that skipped the `page -> facade -> api service -> @api/*` layer, and the one
 * where it cost the most: at 578 lines it was the longest facade in the app, and roughly a fifth of
 * it was request plumbing rather than page state.
 *
 * The `catchError` on the two theme lists is the standing rule made explicit here rather than left
 * inline: a select that falls back to showing only the current value is still a working page,
 * whereas a failed `forkJoin` is a blank one.
 */
@Injectable({providedIn: 'root'})
export class CreateStoreApi {
  private readonly stores = inject(ManagerStoreService);
  private readonly pods = inject(PodService);

  /**
   * The pods this operator may place a store in.
   *
   * Usually empty for an ordinary merchant, and that is correct rather than broken — see lessons.md,
   * "Shell — no merchant-readable list of placeable pods". The page renders the region section only
   * when there is something to choose between.
   */
  listPods(): Observable<readonly Pod[]> {
    return this.pods.list();
  }

  /** Both storefront theme lists, each optional. */
  loadReference(): Observable<CreateStoreReference> {
    return forkJoin({
      themes: this.stores.themes().pipe(optionalList<string>()),
      colorThemes: this.stores.colorThemes().pipe(optionalList<string>()),
    });
  }

  create(request: CreateStoreRequest): Observable<ManagerStore> {
    return this.stores.create(request);
  }

  /**
   * Re-reads a store while it provisions.
   *
   * A poll that fails is not a provisioning failure — the pod may simply not be answering this
   * second — so a failed read emits *nothing* and the next tick tries again. `EMPTY` rather than
   * `of(null)` matters: a null would reach the subscriber and have to be distinguished there from a
   * real answer. The page gives up after two minutes and says it lost track, which is a different
   * statement from "it failed".
   */
  storeInfo(id: string): Observable<ManagerStore> {
    return this.stores.storeInfo(id).pipe(catchError(() => EMPTY));
  }

  /** Whether a store name is already taken. Feeds the form's async validator. */
  nameExists(name: string): Observable<boolean> {
    return this.stores.nameExists(name);
  }
}
