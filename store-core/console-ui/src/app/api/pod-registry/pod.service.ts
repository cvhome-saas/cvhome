import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {Pod} from '@models/pod';

/** Ported from seller-ui/projects/seller-core/stores/src/lib/services/pod.service.ts (the `listPods` half). */
export const POD_API_BASE = '/pod-registry/api/v1/pod';

@Injectable({providedIn: 'root'})
export class PodService {
  private readonly crudService = inject(CrudService);

  /**
   * The pods this caller may place a store into.
   *
   * **Usually empty.** `PodApi.listPods` gives a super admin every pod and an org admin only its *own
   * private* pods; the shared pods a normal merchant is actually placed into are matched internally by
   * `listPlaceablePublicPods()`, which is exposed on no endpoint. So callers must treat an empty list as
   * the normal case and let the registry choose — see lessons.md, "Shell — no merchant-readable list of
   * placeable pods".
   */
  list(): Observable<Pod[]> {
    return this.crudService.get(`${POD_API_BASE}/list`);
  }
}
