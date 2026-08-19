import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {ContentEntityId, PersistableContentBox, ReadableContentBox} from '@models/content';

/**
 * The store's content boxes — fragments of storefront copy, each identified by a code.
 *
 * Written against `content-service`'s `ContentApi` rather than ported from seller-core, because the
 * three endpoints seller-core calls for this do not exist. See lessons.md, "Store management — the
 * landing-page endpoints seller-ui calls do not exist".
 *
 * `GET /private/content/boxes/{code}` ignores its own `lang` parameter and always asks the facade
 * for `LanguageCode.allLanguage()`, which is what makes a per-language editor possible: one read
 * returns every translation the box has.
 *
 * `GET /private/content/box/{code}/exists` is deliberately not ported. It answers the same question
 * the read already answers — a box that is not there is a 404 — and the read is the call that has to
 * happen anyway, because `PUT` needs the box's numeric id and only the read carries it.
 */
const CONTENT_API_BASE = '/spg/content/api/v1';

@Injectable({providedIn: 'root'})
export class ContentBoxService {
  private readonly crudService = inject(CrudService);

  /** One box, with every language's copy. 404 when the store has never saved this code. */
  box(code: string): Observable<ReadableContentBox> {
    return this.crudService.get(`${CONTENT_API_BASE}/private/content/boxes/${code}`);
  }

  /** Creates the box. Refuses a code the store already has (`DuplicateContentCodeException`). */
  create(box: PersistableContentBox): Observable<ContentEntityId> {
    return this.crudService.post(`${CONTENT_API_BASE}/private/content/box`, box);
  }

  /** Replaces a box by its numeric id — the code is not an identifier here, unlike on the read. */
  update(id: number, box: PersistableContentBox): Observable<void> {
    return this.crudService.put(`${CONTENT_API_BASE}/private/content/box/${id}`, box);
  }
}
