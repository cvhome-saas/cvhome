import type {Theme} from '@core/theme/theme.provider';

/**
 * The categorical vocabulary shared by badges, pills, KPI tiles and charts.
 *
 * Tone is never the only signal: every use is accompanied by a label, a value, or
 * position, per the design system's data-integrity rule.
 */
export type Tone = 'green' | 'blue' | 'cyan' | 'amber' | 'red' | 'violet' | 'slate';

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

/** The on-dark ink paired with a tone's wash, for text and icons. */
export function toneInk(theme: Theme, tone: Tone): string {
  return tone === 'slate'
    ? theme.color('--muted-foreground')
    : theme.color(`--chart-${CHART_SLOT[tone]}-foreground`);
}
