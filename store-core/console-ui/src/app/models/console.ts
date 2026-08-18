import {IconName} from '@shared/ui/icon/icon-paths';
import type {Tone} from '@shared/ui/tone';

/**
 * The console chrome — navigation, stores, notifications and identity. Shared by every
 * page that renders inside the console shell, and independent of any one page's content.
 */

export interface NavigationItem {
  readonly labelKey: string;
  readonly icon: IconName;
  /** Absent while a section has no page yet — the item renders, but leads nowhere. */
  readonly route?: string;
  readonly badge?: string;
  readonly badgeTone?: Tone;
}

export interface NavigationSection {
  readonly groupKey: string;
  readonly items: readonly NavigationItem[];
}

export interface ConsoleStore {
  readonly id: string;
  readonly name: string;
}

/**
 * The stores a user can switch between, with the two preferences that order and open them.
 *
 * One payload because the list is meaningless without knowing which store is open and which
 * one login lands on — the eventual endpoint should answer all three in a single round trip.
 */
export interface StoreDirectory {
  readonly stores: readonly ConsoleStore[];
  /** Store the console opens on, marked with a pin in the rail. Null before the first one exists. */
  readonly defaultStoreId: string | null;
  /** Null while the account owns no store — the condition the first-run guards read. */
  readonly currentStoreId: string | null;
}

export interface ConsoleNotification {
  readonly id: string;
  readonly titleKey: string;
  readonly titleParams?: Record<string, string | number>;
  readonly detailKey: string;
  readonly detailParams?: Record<string, string | number>;
  readonly timeKey: string;
  readonly timeParams?: Record<string, string | number>;
  readonly icon: IconName;
  readonly tone: Tone;
  readonly unread: boolean;
}

export interface ConsoleUser {
  readonly name: string;
  readonly initials: string;
  readonly email: string;
}
