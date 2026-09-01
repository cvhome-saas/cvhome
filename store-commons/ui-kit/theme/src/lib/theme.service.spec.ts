import {TestBed} from '@angular/core/testing';

import {CONSOLE_THEMES, ThemeService} from './theme.service';
import {THEME} from './theme.provider';

/**
 * Every token a theme is expected to define. Sourced from Forest, which is the base: a
 * theme that omits one silently inherits Forest's value, which on Daylight means a dark
 * colour on a light page. The coverage test below is the guard against that.
 */
const THEMED_TOKENS = [
  '--background',
  '--foreground',
  '--foreground-strong',
  '--foreground-subtle',
  '--foreground-quiet',
  '--card',
  '--card-foreground',
  '--popover',
  '--popover-foreground',
  '--primary',
  '--primary-foreground',
  '--primary-emphasis',
  '--primary-soft',
  '--primary-muted',
  '--secondary',
  '--secondary-foreground',
  '--muted',
  '--muted-foreground',
  '--accent',
  '--accent-foreground',
  '--accent-strong',
  '--accent-veil',
  '--destructive',
  '--destructive-foreground',
  '--border',
  '--border-soft',
  '--edge',
  '--edge-strong',
  '--input',
  '--ring',
  '--track',
  '--panel',
  '--header-veil',
  '--scrim',
  '--lift',
  '--lift-primary',
  '--lift-subtle',
  '--glow-color',
  '--seat-1',
  '--seat-2',
  '--seat-3',
  '--seat-3-foreground',
  ...[1, 2, 3, 4, 5, 6].flatMap((n) => [
    `--chart-${n}`,
    `--chart-${n}-foreground`,
    `--chart-${n}-wash`,
  ]),
];

/** Reads a token as the browser resolves it, with the given theme applied. */
function underTheme(id: string, token: string): string {
  const root = document.documentElement;
  const previous = root.dataset['theme'];
  root.dataset['theme'] = id;
  const value = getComputedStyle(root).getPropertyValue(token).trim();
  if (previous === undefined) {
    delete root.dataset['theme'];
  } else {
    root.dataset['theme'] = previous;
  }
  return value;
}

describe('themes', () => {
  for (const theme of CONSOLE_THEMES) {
    it(`${theme.id} defines every themed token`, () => {
      const missing = THEMED_TOKENS.filter((token) => !underTheme(theme.id, token));
      expect(missing).withContext(`unset under [data-theme="${theme.id}"]`).toEqual([]);
    });
  }

  it('gives each theme its own surface and accent', () => {
    // If a theme failed to override the base these would collide, which is exactly the
    // failure the coverage test above cannot see — a token present but inherited.
    const backgrounds = CONSOLE_THEMES.map((t) => underTheme(t.id, '--background'));
    const primaries = CONSOLE_THEMES.map((t) => underTheme(t.id, '--primary'));
    expect(new Set(backgrounds).size).toBe(CONSOLE_THEMES.length);
    expect(new Set(primaries).size).toBeGreaterThan(1);
  });

  it('keeps the categorical scale stable across themes', () => {
    // Chart hues are a data vocabulary, not brand: "amber means pending" must survive a
    // theme change. Only their inks may move, for contrast against the new background.
    const fills = CONSOLE_THEMES.map((t) => underTheme(t.id, '--chart-4'));
    expect(new Set(fills).size).toBe(1);
  });
});

describe('ThemeService', () => {
  let service: ThemeService;

  beforeEach(() => {
    localStorage.removeItem('cvhome.console.theme');
    delete document.documentElement.dataset['theme'];
    TestBed.configureTestingModule({});
    service = TestBed.inject(ThemeService);
  });

  afterEach(() => {
    localStorage.removeItem('cvhome.console.theme');
    delete document.documentElement.dataset['theme'];
  });

  it('falls back to the first theme when nothing is stored', () => {
    expect(service.current().id).toBe(CONSOLE_THEMES[0].id);
  });

  it('applies the selection to the document and persists it', () => {
    service.select('daylight');

    expect(service.current().id).toBe('daylight');
    expect(document.documentElement.dataset['theme']).toBe('daylight');
    expect(localStorage.getItem('cvhome.console.theme')).toBe('daylight');
  });

  it('ignores an unknown theme rather than applying a broken one', () => {
    service.select('chartreuse');

    expect(service.current().id).toBe(CONSOLE_THEMES[0].id);
  });

  it('invalidates the token readers so canvas consumers repaint', () => {
    const theme = TestBed.inject(THEME);
    const before = theme.color('--primary');

    service.select('midnight');

    expect(theme.color('--primary')).not.toBe(before);
  });
});
