import {DOCUMENT} from '@angular/common';
import {Injectable, inject, signal} from '@angular/core';

import {BrowserStorage} from '@cvhome-saas/ui-kit';
import {THEME} from './theme.provider';

export interface ThemeOption {
  readonly id: string;
  readonly labelKey: string;
  /** One line for the menu, describing what the operator is choosing. */
  readonly hintKey: string;
  readonly scheme: 'dark' | 'light';
}

/**
 * The themes on offer. Each `id` matches a `[data-theme]` block in src/styles, and the
 * first entry is the default — it is the one that also claims bare `:root`.
 *
 * Adding a theme is three steps: a stylesheet beside theme-forest.css (imported after it in
 * styles.css), an entry here, and the id in the `themes` array of the pre-paint script in
 * index.html — that array is an allow-list, so a theme missing from it is stored correctly and
 * then rejected on the next load, which reads as "my theme keeps resetting to Forest". Its
 * `shared.theme.<id>.*` keys go in every locale file.
 */
export const CONSOLE_THEMES: readonly ThemeOption[] = [
  {id: 'forest', labelKey: 'shared.theme.forest.label', hintKey: 'shared.theme.forest.hint', scheme: 'dark'},
  {id: 'midnight', labelKey: 'shared.theme.midnight.label', hintKey: 'shared.theme.midnight.hint', scheme: 'dark'},
  {id: 'daylight', labelKey: 'shared.theme.daylight.label', hintKey: 'shared.theme.daylight.hint', scheme: 'light'},
  {id: 'light', labelKey: 'shared.theme.light.label', hintKey: 'shared.theme.light.hint', scheme: 'light'},
];

const STORAGE_KEY = 'cvhome.console.theme';

/**
 * Owns which theme is active.
 *
 * Switching is one attribute write — the stylesheets carry every value — plus a nudge to
 * `THEME` so anything that read a token in TypeScript recomputes. There is no per-property
 * writing and nothing to keep in sync.
 *
 * The initial value is applied by an inline script in index.html, before first paint, so
 * this constructor only adopts what is already on the element. Doing it here instead would
 * mean a frame of Forest before the operator's real theme arrived.
 */
@Injectable({providedIn: 'root'})
export class ThemeService {
  private readonly document = inject(DOCUMENT);
  private readonly storage = inject(BrowserStorage);
  private readonly theme = inject(THEME);

  readonly themes = CONSOLE_THEMES;

  private readonly active = signal<ThemeOption>(this.resolve(this.stored()));

  /** The theme in effect. */
  readonly current = this.active.asReadonly();

  select(id: string): void {
    const option = this.resolve(id);
    if (option.id === this.active().id) {
      return;
    }
    this.document.documentElement.dataset['theme'] = option.id;
    this.storage.setItem(STORAGE_KEY, option.id);
    this.active.set(option);
    // The CSS has already changed; this tells the TypeScript readers to look again.
    this.theme.invalidate();
  }

  private stored(): string | null {
    // Prefer what the inline script actually applied, so the two cannot disagree.
    return this.document.documentElement.dataset['theme'] ?? this.storage.getItem(STORAGE_KEY);
  }

  private resolve(id: string | null): ThemeOption {
    return CONSOLE_THEMES.find((option) => option.id === id) ?? CONSOLE_THEMES[0];
  }
}
