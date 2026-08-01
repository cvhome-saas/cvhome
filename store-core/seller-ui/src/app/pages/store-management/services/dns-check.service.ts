// /home/ashraf-revo/IdeaProjects/cvhome-saas/cvhome/store-core/seller-ui/src/app/pages/store-management/services/dns-check.service.ts
import { Injectable, inject } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http'; // Removed HttpHeaders import
import {Observable, of} from 'rxjs';
import {catchError, map, tap} from 'rxjs/operators';

interface GoogleDnsResponse {
  Status: number; // 0 for NOERROR, 1 for FORMERR, 2 for SERVFAIL, 3 for NXDOMAIN, etc.
  TC: boolean; // Truncated
  RD: boolean; // Recursion Desired
  RA: boolean; // Recursion Available
  AD: boolean; // Authentic Data
  CD: boolean; // Checking Disabled
  Question: { name: string; type: number }[];
  Answer?: { name: string; type: number; TTL: number; data: string }[];
  Authority?: { name: string; type: number; TTL: number; data: string }[];
  // There can also be an "edns_client_subnet" field
}

@Injectable({
  providedIn: 'root'
})
export class DnsCheckService {
  private http = inject(HttpClient);

  private googleDohUrl = 'https://dns.google/resolve'; // Inject HttpClient

  /**
   * Checks if a custom domain's CNAME record points to the targetPodDomain using Google's Public DNS API.
   * @param customDomain The domain to check (e.g., "shop.mycompany.com")
   * @param targetPodDomain The expected CNAME target (e.g., "myalias-123.saas.com")
   */
  checkCustomDomainPointsTo(customDomain: string, targetPodDomain: string): Observable<boolean> {
    if (!customDomain || !targetPodDomain) {
      console.warn('[DnsCheckService] customDomain or targetPodDomain is empty. Cannot verify.');
      return of(false);
    }

    // We are looking for a CNAME record for the customDomain
    const params = new HttpParams()
      .set('name', customDomain)
      .set('type', 'CNAME') // Type 5 for CNAME
      .set('_cb', new Date().getTime().toString()); // Cache-busting parameter with current timestamp

    // REMOVED custom headers to prevent CORS preflight issues
    // const headers = new HttpHeaders({
    //   'Cache-Control': 'no-cache, no-store, must-revalidate, post-check=0, pre-check=0',
    //   'Pragma': 'no-cache',
    //   'Expires': '0'
    // });

    console.log(`[DnsCheckService] Querying Google DNS for CNAME: ${customDomain} (Cache buster: ${params.get('_cb')})`);

    // Make the request without the custom 'headers' option
    return this.http.get<GoogleDnsResponse>(this.googleDohUrl, {params}).pipe(
      tap(response => console.log(`[DnsCheckService] Google DNS Response for ${customDomain}:`, response)),
      map(response => {
        if (response && response.Status === 0 && response.Answer) { // Status 0 means NOERROR
          const normalizedTarget = targetPodDomain.endsWith('.') ? targetPodDomain : targetPodDomain + '.';
          const answers = response.Answer;

          for (const record of answers) {
            if (record.type === 5) { // Type 5 is CNAME
              const cnameValue = record.data.endsWith('.') ? record.data : record.data + '.';
              if (cnameValue === normalizedTarget) {
                console.log(`[DnsCheckService] VALID: ${customDomain} CNAMEs to ${targetPodDomain} (via ${record.data})`);
                return true;
              }
            }
          }
          console.log(`[DnsCheckService] INVALID: ${customDomain} has CNAME records, but none point to ${targetPodDomain}. Answers:`, answers.map(a => a.data));
          return false;
        } else if (response && response.Status === 3) { // NXDOMAIN
          console.log(`[DnsCheckService] INVALID: ${customDomain} does not exist (NXDOMAIN).`);
          return false;
        } else if (response && response.Status === 0 && !response.Answer) { // NOERROR but no answer
          console.log(`[DnsCheckService] INVALID: No CNAME record found for ${customDomain}.`);
          return false;
        }
        console.warn(`[DnsCheckService] Could not validate ${customDomain}. Status: ${response?.Status}`);
        return false;
      }),
      catchError(error => {
        console.error(`[DnsCheckService] Error querying Google DNS for ${customDomain}:`, error);
        return of(false);
      })
    );
  }
}
