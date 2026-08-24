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
    /*
     * Dependency direction: features -> layouts -> shared -> api -> core -> models.
     *
     * `api/` holds the HTTP tier ported from seller-core; only features may reach it, and it must
     * never reach back up. Enforced here because the mistake it prevents — a shared component
     * fetching its own data — is the one that made seller-ui's pages untestable.
     *
     * `models/` is the floor and gets its own block below: this one omitted `@shared` and `@core`,
     * and eight model files had quietly grown imports of `Tone`, `IconName`, `PageT`, `ConsoleLocale`
     * and — worst — `KpiDatum` from a *component file*, so a wire shape depended on a widget's
     * template contract. Those types now live in `@models/ui`, `@models/page` and `@models/locale`.
     */
    files: ['src/app/core/**/*.ts', 'src/app/shared/**/*.ts'],
    rules: {
      'no-restricted-imports': [
        'error',
        {
          patterns: [
            {group: ['@api/*'], message: 'core/ and shared/ must not depend on the api tier.'},
            {group: ['@features/*'], message: 'core/ and shared/ must not depend on features.'},
            {group: ['@layouts/*'], message: 'core/ and shared/ must not depend on layouts.'},
          ],
        },
      ],
    },
  },
  {
    // The floor. A model describes a shape; it may not know that anything renders it.
    files: ['src/app/models/**/*.ts'],
    rules: {
      'no-restricted-imports': [
        'error',
        {
          patterns: [
            {
              group: ['@api/*', '@features/*', '@layouts/*', '@shared/*', '@core/*'],
              message: 'models/ is the bottom tier and must import nothing but other models.',
            },
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
          ],
        },
      ],
    },
  },
  {
    /*
     * A layout is chrome every feature sits inside, so it cannot know about any particular one.
     * Not previously enforced; `layouts/billing/` was a feature's state living in the layout tier
     * and is now `layouts/console-shell/billing/`, which is the chrome that actually reads it.
     */
    files: ['src/app/layouts/**/*.ts'],
    rules: {
      'no-restricted-imports': [
        'error',
        {
          patterns: [
            {group: ['@features/*'], message: 'A layout must not depend on a feature.'},
          ],
        },
      ],
    },
  },
  {
    /*
     * One feature may not reach into another. Three did — create-store borrowed store management's
     * validators, the catalogue borrowed the product form's search, and the product form borrowed
     * the product list's cache stamp — and each was a thing that belonged one tier down. Anything
     * two features need is `@shared`, `@core` or `@api`; a feature's own files are relative
     * imports, so this pattern only ever matches a reach across the boundary.
     */
    files: ['src/app/features/**/*.ts'],
    rules: {
      'no-restricted-imports': [
        'error',
        {
          patterns: [
            {group: ['@features/*'], message: 'A feature must not import another feature. Move the shared part down a tier.'},
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
            'tone', 'icon', 'shape', 'fileName', 'slugPrefix', 'prefix', 'pathPrefix', 'loading',
            'inputmode', 'contentDir', 'basePath',
            'controlId', 'check', 'autocomplete', 'display', 'reserve',
            'd', 'preload', 'kind',
          ],
        },
      ],
    },
  },
  {
    /*
     * A spec's host template is a fixture, not an interface. The i18n rule exists to catch copy
     * that would reach an operator untranslated, and nothing here ever does — the literals are the
     * inputs under test, and binding them through Transloco would only hide what the case is
     * asserting. Matches the virtual filenames `processInlineTemplates` derives from a `.spec.ts`.
     */
    files: ['**/*.spec.ts', '**/*.spec.ts/**'],
    rules: {
      '@angular-eslint/template/i18n': 'off',
    },
  },
);
