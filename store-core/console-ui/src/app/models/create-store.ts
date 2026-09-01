import type {IconName} from '@cvhome-saas/ui-kit';

export type CreateStorePhase = 'form' | 'running';

export interface StorePlanSpec {
  readonly key: string;
  readonly params?: Record<string, string | number>;
}

export interface StorePlanOption {
  readonly id: string;
  readonly labelKey: string;
  readonly monthlyPrice: number;
  readonly specs: readonly StorePlanSpec[];
}

/** One row of the provisioning checklist, in the order it runs. */
export interface ProvisioningTask {
  readonly id: string;
  readonly labelKey: string;
  readonly detailKey: string;
  readonly detailParams?: Record<string, string | number>;
}

/** One row of "what gets created" — the resources this run provisions. */
export interface ArtifactItem {
  readonly id: string;
  readonly labelKey: string;
  readonly detailKey: string;
  readonly detailParams?: Record<string, string | number>;
  readonly icon: IconName;
}

/** A follow-up action offered once the store is live. */
export interface NextStepLink {
  readonly id: string;
  readonly labelKey: string;
  readonly icon: IconName;
  /** Absent for actions the console does not support yet — those show a toast instead. */
  readonly route?: string;
}
