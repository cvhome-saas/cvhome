/**
 * The console's navigation — a map of this application, not data any service owns.
 *
 * This is the one thing in `mocks/` that is not standing in for a backend: an endpoint for it would
 * only let a service break the front end's routing. It stays here because the shell has always read it
 * from this file.
 *
 * TODO(lessons.md): the per-section badge counts the design shows were invented and have been removed —
 * see lessons.md, "Shell — no sidebar badge counts".
 */
import {NavigationSection} from '@models/console';

export const CONSOLE_NAVIGATION: readonly NavigationSection[] = [
  {groupKey: 'shell.nav.group.seller', items: [{labelKey: 'shell.nav.item.home', icon: 'home', route: '/dashboard'}]},
  {
    groupKey: 'shell.nav.group.organization',
    items: [
      {labelKey: 'shell.nav.item.storeManagement', icon: 'building', route: '/store-management'},
      {labelKey: 'shell.nav.item.userManagement', icon: 'users'},
    ],
  },
  {
    groupKey: 'shell.nav.group.storefront',
    items: [
      {labelKey: 'shell.nav.item.customers', icon: 'user'},
      {labelKey: 'shell.nav.item.contentManagement', icon: 'fileEdit'},
    ],
  },
  {
    groupKey: 'shell.nav.group.commerce',
    items: [
      {labelKey: 'shell.nav.item.inventory', icon: 'box'},
      {labelKey: 'shell.nav.item.orders', icon: 'shoppingCart', route: '/orders'},
      {labelKey: 'shell.nav.item.payments', icon: 'creditCard'},
    ],
  },
];
