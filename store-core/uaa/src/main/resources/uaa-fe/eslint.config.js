// @ts-check
const eslint = require('@eslint/js');
const tseslint = require('typescript-eslint');
const angular = require('angular-eslint');

module.exports = tseslint.config(
  {ignores: ['.angular/**', 'coverage/**', 'dist/**', 'out-tsc/**', 'node_modules/**']},
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
     * The same direction console-ui enforces, with the tiers this app actually has:
     * features -> layouts -> core -> @cvhome-saas/ui-kit. There is no `shared` tier and no `models`
     * tier here on purpose — everything two features would share is in the kit, and the day this app
     * grows its own is the day it has started re-growing what the kit exists to hold.
     */
    files: ['src/app/core/**/*.ts', 'src/app/layouts/**/*.ts'],
    rules: {
      'no-restricted-imports': [
        'error',
        {
          patterns: [
            {group: ['@features/*'], message: 'core/ and layouts/ must not depend on a feature.'},
          ],
        },
      ],
    },
  },
  {
    files: ['src/app/features/**/*.ts'],
    rules: {
      'no-restricted-imports': [
        'error',
        {
          patterns: [
            {
              group: ['@features/*'],
              message:
                'A feature must not import another feature. What two need belongs in @cvhome-saas/ui-kit.',
            },
          ],
        },
      ],
    },
  },
  {
    files: ['**/*.html'],
    ignores: ['src/index.html'],
    extends: [...angular.configs.templateRecommended, ...angular.configs.templateAccessibility],
  },
);
