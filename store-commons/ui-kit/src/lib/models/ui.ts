/**
 * The presentational vocabulary a wire model is allowed to name.
 *
 * `models/` is the bottom tier and must import nothing — but eight of its files reached *upward*
 * into `@shared/ui` and `@core` for exactly three types: the tone a status maps to, the name of an
 * icon, and the shape a KPI tile takes. `models/orders.ts` imported `KpiDatum` from a component
 * file, which is a wire-shape module depending on a widget's template contract.
 *
 * The types themselves are fine to share — a view model naming the icon it wants is reasonable, and
 * the alternative is a mapper in every feature translating a string into an `IconName`. What was
 * wrong was the direction. They live here, at the bottom, where both tiers may read them.
 *
 * `IconName` is the *union of names*; the paths that draw them stay in `@shared/ui/icon/icon-paths`,
 * which is where they belong and which now checks itself against this list.
 */

/**
 * The categorical vocabulary shared by badges, pills, KPI tiles and charts.
 *
 * Tone is never the only signal: every use is accompanied by a label, a value, or position, per the
 * design system's data-integrity rule.
 */
export type Tone = 'green' | 'blue' | 'cyan' | 'amber' | 'red' | 'violet' | 'slate';

/** One metric, as consumed by `app-kpi-grid`. */
export interface KpiDatum {
  readonly label: string;
  readonly value: string;
  readonly icon: IconName;
  readonly tone?: Tone;
  /** Movement against the comparison period. Mutually exclusive with `flag`. */
  readonly delta?: string;
  readonly trend?: 'up' | 'down';
  /** A state rather than a change, e.g. "Needs review". */
  readonly flag?: string;
}

/**
 * Every glyph the console can draw.
 *
 * Authored as a list rather than derived from the path map, so that the map is checked against it
 * rather than defining it — a path added without a name here is a compile error, and a name here
 * with no path is too.
 */
export type IconName =
  'alertCircle' | 'arrowDown' | 'arrowLeft' | 'arrowRight' | 'arrowUp' | 'arrowUpRight' | 'bell'
  | 'bold' | 'bolt' | 'bookmark' | 'box' | 'building' | 'calendar' | 'chartLine' | 'check' |
  'checkCircle' | 'chevronDown' | 'chevronLeft' | 'chevronRight' | 'clock' | 'code' | 'cog' |
  'copy' | 'creditCard' | 'database' | 'desktop' | 'dollar' | 'download' | 'ellipsisH' |
  'ellipsisV' | 'envelope' | 'eraser' | 'externalLink' | 'eye' | 'eyeOff' | 'facebook' | 'file'
  | 'fileEdit' | 'filter' | 'github' | 'globe' | 'google' | 'grip' | 'heading2' | 'heading3' |
  'home' | 'images' | 'info' | 'instagram' | 'italic' | 'layoutGrid' | 'link' | 'list' |
  'listBullet' | 'listOrdered' | 'lock' | 'mapPin' | 'menu' | 'messageCircle' | 'minus' |
  'palette' | 'panelLeftClose' | 'panelLeftOpen' | 'pencil' | 'percent' | 'phone' | 'pin' |
  'play' | 'plus' | 'printer' | 'questionCircle' | 'receipt' | 'search' | 'send' | 'server' |
  'share' | 'shield' | 'shoppingCart' | 'signIn' | 'signOut' | 'sitemap' | 'sort' | 'sparkles' |
  'star' | 'tag' | 'tiktok' | 'trash' | 'truck' | 'undo' | 'upload' | 'user' | 'userPlus' |
  'users' | 'x' | 'xCircle' | 'xSocial';
