import type {IconName} from '@cvhome-saas/ui-kit';

/**
 * The getting-started page: what an operator sees before their first store exists.
 *
 * Like every other model here, it carries translation keys rather than copy — the facade
 * translates, so a language switch re-renders without refetching.
 */

/** One row of the setup checklist, in the order it has to happen. */
export interface SetupStep {
  readonly id: string;
  readonly labelKey: string;
  readonly metaKey: string;
}

/** A short walkthrough offered beside the checklist. */
export interface GuideVideo {
  readonly id: string;
  readonly titleKey: string;
  readonly durationKey: string;
  readonly sectionKey: string;
  /**
   * Where this guide lives in the docs site, relative to `DOCS_BASE_URL`.
   *
   * These rows were buttons that raised a "not available yet" toast. The written guides exist —
   * they are on the documentation site — so each row is a link out to its page rather than a
   * placeholder for a video nobody has recorded.
   */
  readonly docPath: string;
}

/** A capability that unlocks once the store is provisioned. */
export interface NextUpCard {
  readonly id: string;
  readonly titleKey: string;
  readonly copyKey: string;
  readonly icon: IconName;
}

/** One usage meter on the plan panel. `used` and `cap` are display strings — "0 GB", "1 GB". */
export interface PlanLimit {
  readonly id: string;
  readonly labelKey: string;
  readonly used: string;
  readonly cap: string;
  /** Percentage of the allowance consumed, 0–100. */
  readonly pct: number;
  readonly noteKey: string;
  readonly noteParams?: Record<string, string | number>;
}

/** The feature walkthrough the page leads with. */
export interface FeatureVideo {
  readonly titleKey: string;
  readonly copyKey: string;
  readonly durationKey: string;
  /**
   * The clip itself. Any URL a `<video>` element can load.
   *
   * Null means no walkthrough has been published yet, and the player says so rather than opening
   * on a black rectangle — which is what a missing source looks like otherwise.
   */
  readonly src: string | null;
  /** A still shown before playback starts. Optional; the player falls back to the first frame. */
  readonly poster: string | null;
}

export interface FirstRunSnapshot {
  readonly steps: readonly SetupStep[];
  readonly guides: readonly GuideVideo[];
  readonly nextUp: readonly NextUpCard[];
  readonly limits: readonly PlanLimit[];
  readonly feature: FeatureVideo;
  /** Days remaining on the trial once it is started. */
  readonly trialDays: number;
}
