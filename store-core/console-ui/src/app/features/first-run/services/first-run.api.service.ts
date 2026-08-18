import {Injectable} from '@angular/core';
import {Observable, delay, of} from 'rxjs';

import {
  FIRST_RUN_FEATURE,
  FIRST_RUN_GUIDES,
  FIRST_RUN_LIMITS,
  FIRST_RUN_NEXT_UP,
  FIRST_RUN_STEPS,
  FIRST_RUN_TRIAL_DAYS,
} from '@mocks/first-run.fixture';
import type {FirstRunSnapshot} from '@models/first-run';

/** Round trip the onboarding payload pretends to take, so the page's veil is exercised. */
const LATENCY_MS = 220;

/**
 * The getting-started page's content.
 *
 * One payload rather than four calls: the checklist, the guides, what unlocks next and the
 * plan allowances are all readings of the same onboarding state, and an endpoint that
 * answered them separately could contradict itself mid-render.
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
    }).pipe(delay(LATENCY_MS));
  }
}
