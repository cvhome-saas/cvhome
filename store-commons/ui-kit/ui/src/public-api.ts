/*
 * @cvhome-saas/ui-kit/ui — the control catalogue.
 *
 * There are no raw `<input>`, `<select>`, `<textarea>` or checkbox elements in a consuming feature:
 * five stylesheets had each grown their own `.field`/`.control` vocabulary and drifted into two
 * paddings, two textarea heights and one shared invalid state. These are what a feature uses instead.
 *
 * Most components style themselves with semantic class names over `var(--token)` from
 * `@cvhome-saas/ui-kit/theme`, but several — badge, tag-input, tree, the chart legends — use real
 * Tailwind utilities. ng-packagr does not run Tailwind over component CSS, so those can only come
 * from the consumer's own build, which means a consumer must both import the token layer and add
 * `@source '../node_modules/@cvhome-saas/ui-kit';` to its stylesheet. Omitting it fails silently:
 * everything builds, and the utilities are simply absent.
 *
 * Selectors stay `app-*` rather than taking a library prefix: they are named that way in ~150
 * templates, and renaming them would have bought nothing.
 */
export * from './lib/action-list/action-list';
export * from './lib/action-menu/action-menu';
export * from './lib/autocomplete/autocomplete';
export * from './lib/badge/badge';
export * from './lib/busy-overlay/busy-overlay';
export * from './lib/checkbox/checkbox';
export * from './lib/confirm-dialog/confirm-dialog';
export * from './lib/copy-field/copy-field';
export * from './lib/data-table/data-table';
export * from './lib/data-table/table-row';
export * from './lib/date-picker/date-picker';
export * from './lib/date-range-picker/date-range-picker';
export * from './lib/date-time-field/date-time-field';
export * from './lib/duration-field/duration-field';
export * from './lib/empty-state/empty-state';
export * from './lib/file-drop-zone/file-drop-zone';
export * from './lib/form-field/field-error';
export * from './lib/form-dialog/form-dialog';
export * from './lib/form-field/form-field';
export * from './lib/icon/icon';
export * from './lib/icon/icon-paths';
export * from './lib/image-picker/image-picker';
export * from './lib/image-preview/image-preview';
export * from './lib/kpi-card/kpi-card';
export * from './lib/kpi-grid/kpi-grid';
export * from './lib/load-error/load-error';
export * from './lib/locale-switcher/locale-switcher';
export * from './lib/notice-bar/notice-bar';
export * from './lib/number-field/number-field';
export * from './lib/page-header/page-header';
export * from './lib/pagination/pagination';
export * from './lib/panel/panel';
export * from './lib/progress-track/progress-track';
export * from './lib/ranked-list/ranked-list';
export * from './lib/rich-text/rich-text';
export * from './lib/rich-text/rich-text-html';
export * from './lib/roles-dialog/roles-dialog';
export * from './lib/search-box/search-box';
export * from './lib/secret-field/secret-field';
export * from './lib/section-nav/section-nav';
export * from './lib/select/select';
export * from './lib/set-password-dialog/set-password-dialog';
export * from './lib/stepper/stepper';
export * from './lib/tab-switcher/tab-switcher';
export * from './lib/tag-input/tag-input';
export * from './lib/text-field/text-field';
export * from './lib/textarea-field/textarea-field';
export * from './lib/toast/toast';
export * from './lib/toast/toast-host';
export * from './lib/toggle/toggle';
export * from './lib/tone';
export * from './lib/tree/tree';
export * from './lib/video-dialog/video-dialog';
