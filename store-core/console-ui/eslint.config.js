// @ts-check
const eslint = require('@eslint/js');
const tseslint = require('typescript-eslint');
const angular = require('angular-eslint');

module.exports = tseslint.config(
  {
    ignores: ['.angular/**', 'coverage/**', 'dist/**', 'node_modules/**'],
  },
  {
    files: ['**/*.ts'],
    extends: [
      eslint.configs.recommended,
      ...tseslint.configs.recommended,
      ...angular.configs.tsRecommended,
    ],
    processor: angular.processInlineTemplates,
    rules: {
      // `_name` is how this codebase spells "this parameter exists to match a signature, not to be
      // read" — the test fakes mirror the real services so a drift in their parameters is a compile
      // error rather than a silently weaker spec.
      '@typescript-eslint/no-unused-vars': [
        'error',
        {argsIgnorePattern: '^_', varsIgnorePattern: '^_', caughtErrorsIgnorePattern: '^_'},
      ],
      '@angular-eslint/directive-selector': [
        'error',
        {type: 'attribute', prefix: 'app', style: 'camelCase'},
      ],
      '@angular-eslint/component-selector': [
        'error',
        {type: 'element', prefix: 'app', style: 'kebab-case'},
      ],
    },
  },
  {
    // Dependency direction: features -> layouts -> shared -> api -> core -> models.
    // `api/` holds the HTTP tier ported from seller-core; only features may reach it, and it must never
    // reach back up. Enforced here because the tier is new and the mistake it prevents — a shared
    // component fetching its own data — is the one that made seller-ui's pages untestable.
    files: ['src/app/core/**/*.ts', 'src/app/shared/**/*.ts', 'src/app/models/**/*.ts'],
    rules: {
      'no-restricted-imports': [
        'error',
        {
          patterns: [
            {group: ['@api/*'], message: 'core/, shared/ and models/ must not depend on the api tier.'},
            {group: ['@features/*'], message: 'core/, shared/ and models/ must not depend on features.'},
            {group: ['@layouts/*'], message: 'core/, shared/ and models/ must not depend on layouts.'},
            {group: ['@mocks/*'], message: 'Fixtures are for features and specs, never for core/ or shared/.'},
          ],
        },
      ],
    },
  },
  {
    files: ['src/app/api/**/*.ts'],
    rules: {
      'no-restricted-imports': [
        'error',
        {
          patterns: [
            {group: ['@features/*', '@layouts/*', '@shared/*'], message: 'The api tier must not depend on the UI.'},
            {group: ['@mocks/*'], message: 'The api tier talks to the backend; it never reads a fixture.'},
          ],
        },
      ],
    },
  },
  {
    files: ['**/*.html'],
    // src/index.html is the static host page rendered before Angular boots — it isn't a
    // component template, so Transloco bindings don't apply there.
    ignores: ['src/index.html'],
    extends: [...angular.configs.templateRecommended, ...angular.configs.templateAccessibility],
    rules: {
      // Used purely as a literal-text detector: every finding gets fixed with a
      // `transloco` binding, never Angular's own `i18n` attribute, since the app runs on
      // runtime translation, not $localize/XLIFF. A fixed node stops being flagged either
      // way, because `{{ t('key') }}` is a bound expression, not static text.
      '@angular-eslint/template/i18n': [
        'error',
        {
          checkId: false,
          checkText: true,
          checkAttributes: true,
          ignoreAttributes: [
            'class', 'id', 'style', 'href', 'src', 'routerLink', 'formControlName',
            'autocomplete', 'type', 'target', 'rel', 'name', 'for', 'role', 'data-label',
            'data-date', 'data-theme-preview', 'panelId', 'rows', 'minlength', 'accept',
            // ARIA/SVG mechanics and component-internal enum inputs — never display text.
            'aria-labelledby', 'aria-controls', 'aria-haspopup', 'aria-current',
            'aria-live', 'aria-relevant', 'scope', 'stroke-linecap', 'stroke-linejoin',
            'animate.enter', 'animate.leave', 'method',
            'tone', 'icon', 'shape', 'fileName', 'slugPrefix', 'basePath',
            'd', 'preload', 'kind',
          ],
        },
      ],
    },
  },
);
