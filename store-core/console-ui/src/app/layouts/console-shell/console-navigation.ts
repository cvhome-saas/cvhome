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
  {
    /*
     * The other product. `.agents/plans/seller-ui-feature-inventory.md` §1 names the problem this
     * group answers — "two products are wearing one skin" — and the answer is a marked group and a
     * route guard rather than a second shell: a platform operator switching between the two halves
     * should not be switching applications.
     *
     * It leads because a super admin arriving at the console is here for the platform, not for
     * whichever tenant's store happens to sort first.
     */
    groupKey: 'shell.nav.group.platform',
    audience: 'platform',
    items: [
      {labelKey: 'shell.nav.item.platformHome', icon: 'chartLine', route: '/platform'},
      {labelKey: 'shell.nav.item.organizations', icon: 'building', route: '/platform/organizations'},
      {labelKey: 'shell.nav.item.pods', icon: 'server', route: '/platform/pods'},
      {labelKey: 'shell.nav.item.platformUsers', icon: 'shield', route: '/platform/users'},
      /*
       * `dollar` rather than `creditCard`: the plans entry one row below already owns that glyph,
       * and two identical icons adjacent in one rail is worse than either choice on its own.
       */
      {labelKey: 'shell.nav.item.platformBilling', icon: 'dollar', route: '/platform/billing'},
      {labelKey: 'shell.nav.item.plans', icon: 'creditCard', route: '/platform/plans'},
    ],
  },
  /*
   * Everything below is the merchant console, and every page in it is a reading of **one store** —
   * which is why the whole of it is marked rather than only the obviously store-scoped parts. A
   * platform operator has no store of their own to read: the list they are handed is a truncated
   * page of every tenant's, so the switcher is hidden and `?store=` resolves to a stranger's shop.
   * Offering these groups to them was offering nine pages that 403.
   */
  {
    groupKey: 'shell.nav.group.seller',
    audience: 'merchant',
    items: [{labelKey: 'shell.nav.item.home', icon: 'home', route: '/dashboard'}],
  },
  {
    groupKey: 'shell.nav.group.organization',
    audience: 'merchant',
    items: [
      {labelKey: 'shell.nav.item.storeManagement', icon: 'building', route: '/store-management'},
      // `/subscription`, not `/billing` — the gateway owns that prefix. See app.routes.ts.
      {labelKey: 'shell.nav.item.billing', icon: 'creditCard', route: '/subscription'},
      // Routed by Module 8. The team of the *open store*, which is what user-account/list answers.
      {labelKey: 'shell.nav.item.userManagement', icon: 'users', route: '/users'},
    ],
  },
  {
    groupKey: 'shell.nav.group.storefront',
    audience: 'merchant',
    items: [
      // Was routeless — this app's marker for "not built". Module 9 built it.
      {labelKey: 'shell.nav.item.customers', icon: 'user', route: '/customers'},
      {labelKey: 'shell.nav.item.contentManagement', icon: 'fileEdit'},
    ],
  },
  {
    groupKey: 'shell.nav.group.commerce',
    audience: 'merchant',
    items: [
      {labelKey: 'shell.nav.item.catalogue', icon: 'sitemap', route: '/catalogue'},
      // Was routeless — this app's marker for "not built". Module 6 built it.
      {labelKey: 'shell.nav.item.inventory', icon: 'box', route: '/products'},
      {labelKey: 'shell.nav.item.orders', icon: 'shoppingCart', route: '/orders'},
      // Was routeless — this app's marker for "not built". Module 7 built it.
      {labelKey: 'shell.nav.item.payments', icon: 'creditCard', route: '/payments'},
    ],
  },
];
