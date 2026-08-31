import {defineConfig, globalIgnores} from "eslint/config";
import nextVitals from "eslint-config-next/core-web-vitals";
import nextTs from "eslint-config-next/typescript";

const PHYSICAL_DIRECTION_CLASSES = /\b(?:[mp][lr]-|(?:-)?(?:left|right)-|text-(?:left|right)\b|rounded-[lr]-|border-[lr]-|rounded-[tb][lr]\b|(?:[mp][lr]|left|right)-\[)/;

// Root config: one ruleset for the storefront, the shared source libs and every theme (ESLint ignores files
// outside the config's directory, so this cannot live in storefront/).
const eslintConfig = defineConfig([
    {settings: {next: {rootDir: "storefront/"}}},
    ...nextVitals,
    ...nextTs,
    // `.next*` (not just `.next`): NEXT_DIST_DIR gives every lcl stack its own dev build dir (.next-<stack>)
    globalIgnores(["**/.next*/**", "**/out/**", "**/build/**", "**/next-env.d.ts", "templates-deprecated/**", "**/dist/**", "**/node_modules/**"]),
    {
        // Themes and shared UI are linted through the storefront so one config rules them all.
        files: ["themes/**/*.{ts,tsx}", "libs/ui/**/*.{ts,tsx}", "libs/theme/**/*.{ts,tsx}", "libs/i18n/**/*.{ts,tsx}"],
        rules: {
            "no-restricted-imports": ["error", {
                patterns: [{group: ["@/*"], message: "Themes and shared libs must not import the storefront shell (`@/…`). Use @store-front/* packages."}],
            }],
        },
    },
    {
        // RTL: logical properties only in theme code (ps-/pe-/ms-/me-/start-/end-/text-start/text-end/rounded-s-/e-).
        // libs/ui is exempt: Radix side variants (data-[side=left]) are direction-aware already.
        files: ["themes/**/*.{ts,tsx}"],
        rules: {
            "no-restricted-syntax": ["warn", {
                selector: `Literal[value=${PHYSICAL_DIRECTION_CLASSES.toString()}]`,
                message: "Physical direction class (left/right/pl/pr/ml/mr…) — use logical utilities (start/end, ps/pe, ms/me) so RTL locales flip correctly.",
            }, {
                selector: `TemplateElement[value.raw=${PHYSICAL_DIRECTION_CLASSES.toString()}]`,
                message: "Physical direction class in a template literal — use logical utilities (start/end, ps/pe, ms/me).",
            }],
        },
    },
]);

export default eslintConfig;
