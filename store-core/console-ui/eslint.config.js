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
            'aria-live', 'aria-relevant', 'stroke-linecap', 'stroke-linejoin',
            'animate.enter', 'animate.leave', 'method',
            'tone', 'icon', 'shape', 'fileName',
          ],
        },
      ],
    },
  },
);
