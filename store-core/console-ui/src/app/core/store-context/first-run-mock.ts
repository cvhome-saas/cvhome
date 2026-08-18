import {DOCUMENT} from '@angular/common';
import {Injectable, inject} from '@angular/core';

import {BrowserStorage} from '@core/platform/browser-storage';

const FLAG_KEY = 'cvhome.console.mock.firstRun';
const FLAG_PARAM = 'firstRun';

/**
 * Simulates an account that owns no stores yet.
 *
 * First run is the one console state the fixtures cannot otherwise produce: both store
 * lists are hardcoded non-empty, so without a switch there is no way to reach the
 * getting-started page, the disabled rail, or the guards that lead to them. `?firstRun=1`
 * turns it on and it sticks, because the flow it gates spans several navigations.
 *
 * This is scaffolding. Once a stores endpoint exists an empty response is the real signal
 * and this service, along with the two fixture lists it empties, comes out.
 */
@Injectable({providedIn: 'root'})
export class FirstRunMock {
  private readonly storage = inject(BrowserStorage);
  private readonly document = inject(DOCUMENT);

  /**
   * Read once and cached: the query parameter is consumed on the first navigation, but the
   * answer has to stay stable for every later route change in the same session.
   */
  private readonly enabled = this.resolve();

  active(): boolean {
    return this.enabled;
  }

  /** Called once a store exists — the account is no longer in first run. */
  clear(): void {
    this.storage.removeItem(FLAG_KEY);
  }

  private resolve(): boolean {
    const search = this.document.defaultView?.location.search;
    if (search && new URLSearchParams(search).get(FLAG_PARAM) === '1') {
      this.storage.setItem(FLAG_KEY, 'true');
      return true;
    }
    return this.storage.getItem(FLAG_KEY) === 'true';
  }
}
