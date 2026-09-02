import type {Theme} from '@cvhome-saas/ui-kit/theme';
import type {Tone} from '@cvhome-saas/ui-kit';

// The union lives in `@models/ui` so a view model may name the tone it wants without importing
// upward into the UI tier; the resolvers below stay here, because they read the document.
export type {Tone} from '@cvhome-saas/ui-kit';

/** Tone to its slot in the `--chart-*` scale. `slate` is not a hue and has none. */
const CHART_SLOT: Readonly<Record<Exclude<Tone, 'slate'>, number>> = {
  green: 1,
  blue: 2,
  cyan: 3,
  amber: 4,
  red: 5,
  violet: 6,
};

/**
 * Resolves a tone to a concrete colour for canvas rendering.
 *
 * Charts draw to a canvas, so they cannot use the custom properties the rest of the UI
 * consumes — they need real values. Reading them back off the document keeps the charts on
 * the same palette as everything painted in CSS.
 */
export function toneColor(theme: Theme, tone: Tone): string {
  // The neutral member of the set is the chart track, not a hue — it reads as "the rest"
  // rather than as another category.
  return tone === 'slate'
    ? theme.color('--track')
    : theme.color(`--chart-${CHART_SLOT[tone]}`);
}
