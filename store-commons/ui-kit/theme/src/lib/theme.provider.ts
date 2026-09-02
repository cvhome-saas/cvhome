import {DOCUMENT} from '@angular/common';
import {
  APP_INITIALIZER,
  InjectionToken,
  makeEnvironmentProviders,
  signal,
  type EnvironmentProviders,
} from '@angular/core';

/**
 * The design system, for the few places that need a resolved value in TypeScript rather
 * than in CSS — echarts and jsPDF both paint outside the DOM and cannot use a custom
 * property.
 *
 * The tokens themselves are authored in CSS (src/styles/theme*.css). This reads them back
 * off the document at runtime, so TypeScript and CSS cannot drift: there is one source of
 * truth and it is the stylesheet. Anything expressible in CSS should use the custom
 * properties or their Tailwind utilities instead of this.
 */
export interface Theme {
  /** The computed value of a token, verbatim — `'900px'`, `'0.875rem'`. */
  (token: string): string;
  /**
   * A token resolved to an sRGB string that canvas and jsPDF can parse. The palette is
   * authored in oklch, which jsPDF cannot read, so colours must come through here.
   */
  color(token: string): string;
  /**
   * Invalidates every value read so far. `ThemeService` calls this after swapping
   * `[data-theme]`; nothing else should need to.
   */
  invalidate(): void;
}

/**
 * Values for the tokens read from TypeScript, used when there is no document to read —
 * server-side rendering and unit tests. Kept deliberately short: every entry is a
 * duplicate of a value in theme-forest.css, so the list should not grow casually.
 */
const FALLBACK: Readonly<Record<string, string>> = {
  '--background': '#05140f',
  '--foreground': '#e6efeb',
  '--foreground-strong': '#ffffff',
  '--foreground-quiet': '#82978e',
  '--muted-foreground': '#98aca3',
  '--card': '#071a13',
  '--border': 'rgb(255 255 255 / 10%)',
  '--track': 'rgb(255 255 255 / 18%)',
  '--primary': '#10b981',
  '--chart-1': '#10b981',
  '--chart-2': '#3b82f6',
  '--chart-3': '#06b6d4',
  '--chart-4': '#f59e0b',
  '--chart-5': '#ef4444',
  '--chart-6': '#8b5cf6',
  '--chart-1-foreground': '#6ee7b7',
  '--chart-2-foreground': '#93c5fd',
  '--chart-3-foreground': '#67e8f9',
  '--chart-4-foreground': '#fcd34d',
  '--chart-5-foreground': '#fca5a5',
  '--chart-6-foreground': '#c4b5fd',
  '--breakpoint-lg': '900px',
  '--font-sans': "'Inter Variable', Inter, ui-sans-serif, system-ui, sans-serif",
};

const hex = (channel: number) => channel.toString(16).padStart(2, '0');

/**
 * Normalises any colour the browser understands — including the oklch the palette is
 * authored in — to `#rrggbb` or `rgba(...)`.
 *
 * This paints one pixel and reads it back rather than round-tripping through `fillStyle`:
 * the `fillStyle` getter preserves the colour function it was given, so oklch in is oklch
 * out, and jsPDF cannot parse that. A sampled pixel is always sRGB bytes.
 */
function toSrgb(value: string, document: Document): string {
  const canvas = document.createElement('canvas');
  canvas.width = canvas.height = 1;
  const context = canvas.getContext('2d', {willReadFrequently: true});
  if (!context) {
    return value;
  }
  context.clearRect(0, 0, 1, 1);
  context.fillStyle = value;
  context.fillRect(0, 0, 1, 1);
  const [r, g, b, a] = context.getImageData(0, 0, 1, 1).data;
  return a === 255
    ? `#${hex(r)}${hex(g)}${hex(b)}`
    : `rgba(${r}, ${g}, ${b}, ${(a / 255).toFixed(3)})`;
}

function createTheme(document: Document | null): Theme {
  const root = document?.documentElement;
  // Live declaration: once the [data-theme] attribute changes, this reports the new
  // values without being rebuilt.
  const computed = root && typeof getComputedStyle === 'function' ? getComputedStyle(root) : null;

  /**
   * Bumped on every theme change. Reading it here means any `computed()` that touches a
   * token — a chart's ECharts option, for instance — re-evaluates on a switch without the
   * component knowing that themes exist.
   */
  const revision = signal(0);

  const read = (token: string): string => {
    revision();
    return computed?.getPropertyValue(token).trim() || FALLBACK[token] || '';
  };

  const theme = ((token: string) => read(token)) as Theme;
  theme.color = (token: string) => {
    const value = read(token);
    return document ? toSrgb(value, document) : value;
  };
  theme.invalidate = () => revision.update((n) => n + 1);
  return theme;
}

/**
 * Resolves anywhere without setup — a chart dropped into a test or a standalone harness
 * gets the real palette rather than an NG0201. `provideTheme()` supplies the document-
 * backed reader once the app bootstraps.
 */
export const THEME = new InjectionToken<Theme>('THEME', {
  providedIn: 'root',
  factory: () => createTheme(typeof document === 'undefined' ? null : document),
});

/**
 * Registers the design system.
 *
 * On the normal path the tokens are already in the stylesheet, so this only wires up the
 * reader. Passing `overrides` — a map of custom properties, exactly as they appear in
 * theme-forest.css — additionally writes them onto the document root at bootstrap, which
 * is the hook for per-tenant theming. That path is browser-only by design; on the server
 * it is a no-op and the stylesheet's own values render instead.
 */
export function provideTheme(overrides?: Readonly<Record<string, string>>): EnvironmentProviders {
  const providers = [
    {
      provide: THEME,
      deps: [DOCUMENT],
      useFactory: (document: Document) => createTheme(document),
    },
  ];

  if (!overrides) {
    return makeEnvironmentProviders(providers);
  }

  return makeEnvironmentProviders([
    ...providers,
    {
      provide: APP_INITIALIZER,
      multi: true,
      deps: [DOCUMENT],
      useFactory: (document: Document) => () => {
        const root = document.documentElement;
        // `style` is absent on the server-side DOM shim; nothing to do there.
        if (!root?.style) {
          return;
        }
        for (const [name, value] of Object.entries(overrides)) {
          root.style.setProperty(name, value);
        }
      },
    },
  ]);
}
