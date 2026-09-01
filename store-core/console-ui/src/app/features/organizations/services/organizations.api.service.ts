import {Injectable, inject} from '@angular/core';
import {Observable, map} from 'rxjs';

import {OrgService, type CreateOrgUser} from '@api/tenancy/org.service';
import type {PageRequest} from '@cvhome-saas/ui-kit';
import {toOrgRow, type OrgRow} from '@models/platform';

/** What the page asks the registry for: a page, a term and a status. */
export interface OrgsQuery extends PageRequest {
  /** Matched server-side against the name and the contact email. */
  readonly term: string;
  /** An `OrgStatus`, or `''` for any. */
  readonly status: string;
}

/** Everything the tenant registry's table renders for one query. */
export interface OrgsSnapshot {
  readonly rows: readonly OrgRow[];
  readonly totalElements: number;
  readonly totalPages: number;
  /**
   * The term these rows answer, echoed back.
   *
   * The page keeps the last good rows on screen while the next request is in flight, so "the term"
   * and "the rows" are briefly out of step — and the empty state has to know which of the two it is
   * describing, or a slow search shows "nothing matched" over the previous query's results.
   */
  readonly term: string;
}

/**
 * The organizations list.
 *
 * **One request.** The filtering is the server's — `POST org-manager/list` takes a term matched
 * against the name and the contact email plus a status, so the box narrows the whole registry rather
 * than the twenty rows on screen. What is still absent is a store count, a user count or a plan per
 * row: each would be a separate paged call per organization. See lessons.md, "Organizations — no
 * store count, user count or plan on the row".
 */
@Injectable({providedIn: 'root'})
export class OrganizationsApi {
  private readonly orgs = inject(OrgService);

  loadOrgs(query: OrgsQuery): Observable<OrgsSnapshot> {
    return this.orgs.search({term: query.term, status: query.status}, query.page, query.count).pipe(
      map((page) => ({
        rows: (page.content ?? []).map(toOrgRow),
        totalElements: page.totalElements,
        totalPages: page.totalPages,
        term: query.term,
      })),
    );
  }

  /** Creates an organization and its first administrator. Answers the created uaa account. */
  create(user: CreateOrgUser): Observable<void> {
    return this.orgs.create(user).pipe(map(() => undefined));
  }
}

