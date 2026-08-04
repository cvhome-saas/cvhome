// @ts-check
const eslint = require("@eslint/js");
const { defineConfig } = require("eslint/config");
const tseslint = require("typescript-eslint");
const angular = require("angular-eslint");

module.exports = defineConfig([
  {
    files: ["**/*.ts"],
    extends: [
      eslint.configs.recommended,
      tseslint.configs.recommended,
      tseslint.configs.stylistic,
      angular.configs.tsRecommended,
    ],
    processor: angular.processInlineTemplates,
    rules: {
      // Every toast goes through NotificationService, and every backend error message through
      // ApiErrorService. Injecting Nebular's toastr directly is how the old ErrorService came to build
      // error copy of its own, and how 240 call sites ended up showing "System Error" for everything.
      // The wrapper itself is excluded below.
      "no-restricted-imports": [
        "error",
        {
          paths: [
            {
              name: "ngx-toastr",
              message:
                "ngx-toastr is not wired up in this app (no ToastrModule.forRoot), and injecting it throws at runtime. Use NotificationService.",
            },
          ],
        },
      ],
      "@typescript-eslint/no-unused-vars": [
        "error",
        {
          argsIgnorePattern: "^_",
          varsIgnorePattern: "^_",
          caughtErrorsIgnorePattern: "^_",
        },
      ],
      "@angular-eslint/directive-selector": [
        "error",
        {
          type: "attribute",
          prefix: ["ngx", "app"],
          style: "camelCase",
        },
      ],
      "@angular-eslint/component-selector": [
        "error",
        {
          type: "element",
          prefix: ["ngx", "app"],
          style: "kebab-case",
        },
      ],
    },
  },
  {
    // The one file allowed to reach for Nebular's toastr; everything else goes through it.
    files: ["src/app/core/notifications/notification.service.ts"],
    rules: {
      "no-restricted-syntax": "off",
    },
  },
  {
    files: ["**/*.ts"],
    ignores: ["src/app/core/notifications/notification.service.ts"],
    rules: {
      "no-restricted-syntax": [
        "error",
        {
          selector: "CallExpression[callee.name='inject'][arguments.0.name='NbToastrService']",
          message: "Inject NotificationService instead of NbToastrService, so all toast copy has one owner.",
        },
      ],
    },
  },
  {
    files: ["**/*.html"],
    extends: [
      angular.configs.templateRecommended,
      angular.configs.templateAccessibility,
    ],
    rules: {},
  }
]);
