import {
  ConsoleNotification,
  ConsoleStore,
  ConsoleUser,
  NavigationSection,
} from '@models/console';

export const CONSOLE_ORGANIZATION = 'ACME';

export const CONSOLE_USER: ConsoleUser = {
  name: 'Jordan Diaz',
  initials: 'JD',
  email: 'jordan@acmesupply.co',
};

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
      {labelKey: 'shell.nav.item.inventory', icon: 'box', badge: '12', badgeTone: 'amber'},
      {labelKey: 'shell.nav.item.orders', icon: 'shoppingCart', route: '/orders', badge: '5', badgeTone: 'red'},
      {labelKey: 'shell.nav.item.payments', icon: 'creditCard', badge: '7', badgeTone: 'green'},
    ],
  },
];

/** Ids match `SelectedStoreService`, so a switch here is the store the API layer sends. */
export const CONSOLE_STORES: readonly ConsoleStore[] = [
  {id: '65f023632bc46470c104b76f', name: 'Acme Supply Co.'},
  {id: '65f023632bc46470c104b75f', name: 'Acme Outlet - West'},
  {id: '65f023632bc46470c104b77f', name: 'Acme Wholesale'},
];

export const CONSOLE_DEFAULT_STORE_ID = CONSOLE_STORES[0].id;

export const CONSOLE_NOTIFICATIONS: readonly ConsoleNotification[] = [
  {
    id: 'order-10482',
    titleKey: 'shell.notifications.item.newOrder.title',
    titleParams: {orderId: '10482'},
    detailKey: 'shell.notifications.item.newOrder.detail',
    detailParams: {customer: 'Maya Chen', amount: '$1,240.00', count: 3},
    timeKey: 'shell.notifications.time.minutesAgo',
    timeParams: {count: 2},
    icon: 'shoppingCart',
    tone: 'green',
    unread: true,
  },
  {
    id: 'payment-awaiting',
    titleKey: 'shell.notifications.item.paymentAwaiting.title',
    detailKey: 'shell.notifications.item.paymentAwaiting.detail',
    detailParams: {amount: '$3,890.00'},
    timeKey: 'shell.notifications.time.minutesAgo',
    timeParams: {count: 18},
    icon: 'creditCard',
    tone: 'amber',
    unread: true,
  },
  {
    id: 'out-of-stock-wireless-headphones',
    titleKey: 'shell.notifications.item.outOfStock.title',
    titleParams: {product: 'Wireless Headphones'},
    detailKey: 'shell.notifications.item.outOfStock.detail',
    detailParams: {units: 0, orders: 12},
    timeKey: 'shell.notifications.time.hoursAgo',
    timeParams: {count: 1},
    icon: 'box',
    tone: 'red',
    unread: true,
  },
  {
    id: 'team-joined-priya-nair',
    titleKey: 'shell.notifications.item.teamJoined.title',
    titleParams: {name: 'Priya Nair'},
    detailKey: 'shell.notifications.item.teamJoined.detail',
    detailParams: {role: 'Store manager', store: 'Acme Outlet - West'},
    timeKey: 'shell.notifications.time.hoursAgo',
    timeParams: {count: 3},
    icon: 'userPlus',
    tone: 'blue',
    unread: true,
  },
  {
    id: 'refund-10416',
    titleKey: 'shell.notifications.item.refundIssued.title',
    titleParams: {orderId: '10416'},
    detailKey: 'shell.notifications.item.refundIssued.detail',
    detailParams: {amount: '$212.00'},
    timeKey: 'shell.notifications.time.yesterday',
    icon: 'undo',
    tone: 'slate',
    unread: false,
  },
];
