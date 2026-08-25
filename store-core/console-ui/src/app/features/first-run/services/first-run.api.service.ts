import {Injectable} from '@angular/core';
import {Observable, of} from 'rxjs';

import {
  FIRST_RUN_FEATURE,
  FIRST_RUN_GUIDES,
  FIRST_RUN_LIMITS,
  FIRST_RUN_NEXT_UP,
  FIRST_RUN_STEPS,
  FIRST_RUN_TRIAL_DAYS,
} from '../first-run.content';
import type {FirstRunSnapshot} from '@models/first-run';

/**
 * The getting-started page's content.
 *
 * One payload rather than four calls: the checklist, the guides, what unlocks next and the
 * plan allowances are all readings of the same onboarding state, and an endpoint that
 * answered them separately could contradict itself mid-render.
 *
 * **It is authored copy, not data, and no longer pretends otherwise.** This used to sit behind a
 * `delay(220)` "so the page's veil is exercised", which made a constant look like a round trip in
 * the network tab and in every spec that waited on it. The content lives in `first-run.content.ts`
 * beside the page that renders it; when there is an onboarding endpoint, this method is where it
 * goes, and the wait will be real.
 */
@Injectable({providedIn: 'root'})
export class FirstRunApi {
  loadSnapshot(): Observable<FirstRunSnapshot> {
    return of<FirstRunSnapshot>({
      steps: FIRST_RUN_STEPS,
      guides: FIRST_RUN_GUIDES,
      nextUp: FIRST_RUN_NEXT_UP,
      limits: FIRST_RUN_LIMITS,
      feature: FIRST_RUN_FEATURE,
      trialDays: FIRST_RUN_TRIAL_DAYS,
    });
  }
}
