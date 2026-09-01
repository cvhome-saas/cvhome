// @ts-check
const eslint = require('@eslint/js');
const tseslint = require('typescript-eslint');
const angular = require('angular-eslint');

module.exports = tseslint.config(
  {
    ignores: ['.angular/**', 'coverage/**', 'dist/**', 'out-tsc/**', 'node_modules/**'],
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
      '@typescript-eslint/no-unused-vars': [
        'error',
        {argsIgnorePattern: '^_', varsIgnorePattern: '^_', caughtErrorsIgnorePattern: '^_'},
      ],
      /*
       * `app`, not the `lib` an Angular library normally takes. These components were `app-*` in
       * console-ui and are used by name in ~150 templates there; a library prefix would rename every
       * one of them for no benefit, and the kit is consumed by this repo's apps rather than published.
       */
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
     * The boundary. console-ui's tier rules stop at its own edge, so this restates them from the
     * library's side: nothing here may reach back into a consumer's tiers.
     *
     * Belt and braces — such an import could not resolve anyway, because the kit's tsconfig has no
     * `paths` for those aliases. The rule exists so the failure reads as "the kit must not depend on
     * an app" rather than as a module-not-found three files away from the mistake.
     */
    files: ['**/*.ts'],
    rules: {
      'no-restricted-imports': [
        'error',
        {
          patterns: [
            {
              group: ['@api/*', '@features/*', '@layouts/*', '@shared/*', '@core/*', '@env/*'],
              message:
                'The kit is the floor. It must not import a consuming application’s tiers — take the value as an input or a token instead.',
            },
          ],
        },
      ],
    },
  },
  {
    files: ['**/*.html'],
    extends: [...angular.configs.templateRecommended, ...angular.configs.templateAccessibility],
  },
);
