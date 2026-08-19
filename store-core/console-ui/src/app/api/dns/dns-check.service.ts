import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable, inject} from '@angular/core';
import {Observable, map} from 'rxjs';

/** RFC 8484 record types, as numbers, which is how DoH answers name them. */
const CNAME = 5;

/** `Status` in the DoH JSON answer: 0 is NOERROR, 3 is NXDOMAIN. */
const NOERROR = 0;
const NXDOMAIN = 3;

/** What `https://dns.google/resolve` answers with. Only the fields the check reads are declared. */
interface DohAnswer {
  readonly name: string;
  readonly type: number;
  readonly TTL: number;
  readonly data: string;
}

interface DohResponse {
  readonly Status: number;
  readonly Answer?: readonly DohAnswer[];
}

/** What a lookup found. `unresolved` is "we could not tell", which is not the same as "wrong". */
export type CnameOutcome = 'points-here' | 'points-elsewhere' | 'no-record' | 'no-such-domain';

/**
 * Ported from seller-ui/projects/seller-core/stores/src/lib/services/dns-check.service.ts.
 *
 * Whether a custom domain's CNAME points at this store's pod.
 *
 * **This is not a platform check.** It is a query against Google's public DNS-over-HTTPS resolver, made
 * by the operator's own browser, and nothing server-side ever confirms the record. A pass here means
 * one public resolver saw the CNAME a moment ago — not that the platform has accepted the domain, and
 * not that every resolver agrees yet. Carried over because it is what seller-ui does and it is genuinely
 * useful as a pre-flight, but see lessons.md, "Store management — DNS verification is a browser-side
 * check, not a platform one".
 *
 * Uses `HttpClient` directly rather than `CrudService`: this is a third-party host, so it must not be
 * given the console's base URL, and it must not carry the `?store=&pod=` request context — which
 * `CrudService` stamps onto every call and which would leak the tenant's ids to Google.
 *
 * A failed lookup **throws** rather than resolving to a negative. Collapsing the two made "your DNS is
 * wrong" and "we could not check" the same answer, and told operators to fix records that were fine —
 * seller-core's own comment records that this was deliberate there too.
 */
@Injectable({providedIn: 'root'})
export class DnsCheckService {
  private readonly http = inject(HttpClient);

  private readonly resolver = 'https://dns.google/resolve';

  checkCname(domain: string, target: string): Observable<CnameOutcome> {
    const params = new HttpParams()
      .set('name', domain)
      .set('type', 'CNAME')
      // DoH answers are cacheable and a re-check after fixing a record has to see the new one.
      .set('_cb', Date.now().toString());

    return this.http
      .get<DohResponse>(this.resolver, {params})
      .pipe(map((response) => this.outcomeOf(response, target)));
  }

  private outcomeOf(response: DohResponse, target: string): CnameOutcome {
    if (response.Status === NXDOMAIN) {
      return 'no-such-domain';
    }
    const records = (response.Answer ?? []).filter((answer) => answer.type === CNAME);
    if (response.Status !== NOERROR || records.length === 0) {
      return 'no-record';
    }
    // DNS names are compared with the root dot present and without regard to case.
    const wanted = rooted(target).toLowerCase();
    return records.some((record) => rooted(record.data).toLowerCase() === wanted)
      ? 'points-here'
      : 'points-elsewhere';
  }
}

function rooted(name: string): string {
  return name.endsWith('.') ? name : `${name}.`;
}
