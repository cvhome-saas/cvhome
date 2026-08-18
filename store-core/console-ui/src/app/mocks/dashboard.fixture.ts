import {AttentionItem, Kpi, OrderStatus, Product} from '@models/dashboard';

export const DASHBOARD_KPIS: readonly Kpi[] = [
  {id: 'revenue', labelKey: 'dashboard.kpi.revenue', value: '$48,230', icon: 'dollar', tone: 'green', delta: '12.4%'},
  {id: 'orders', labelKey: 'dashboard.kpi.orders', value: '312', icon: 'shoppingCart', tone: 'blue', delta: '5.8%'},
  {id: 'pendingPayments', labelKey: 'dashboard.kpi.pendingPayments', value: '7', icon: 'creditCard', tone: 'amber', flagKey: 'dashboard.kpi.needsReview'},
  {id: 'lowStock', labelKey: 'dashboard.kpi.lowStock', value: '12', icon: 'box', tone: 'red', flagKey: 'dashboard.kpi.needsReview'},
];

export const DASHBOARD_ATTENTION: readonly AttentionItem[] = [
  {
    labelKey: 'dashboard.attention.paymentApprovals.label',
    detailKey: 'dashboard.attention.paymentApprovals.detail',
    count: '7',
    icon: 'creditCard',
    tone: 'amber',
  },
  {
    labelKey: 'dashboard.attention.unfulfilledOrders.label',
    detailKey: 'dashboard.attention.unfulfilledOrders.detail',
    count: '5',
    icon: 'clock',
    tone: 'red',
  },
  {
    labelKey: 'dashboard.attention.lowStockProducts.label',
    detailKey: 'dashboard.attention.lowStockProducts.detail',
    count: '12',
    icon: 'box',
    tone: 'violet',
  },
];

export const DASHBOARD_ORDER_STATUSES: readonly OrderStatus[] = [
  {labelKey: 'dashboard.orderStatus.ordered', value: 120, tone: 'green'},
  {labelKey: 'dashboard.orderStatus.processed', value: 84, tone: 'blue'},
  {labelKey: 'dashboard.orderStatus.delivered', value: 96, tone: 'cyan'},
  {labelKey: 'dashboard.orderStatus.refunded', value: 8, tone: 'amber'},
  {labelKey: 'dashboard.orderStatus.canceled', value: 4, tone: 'red'},
];

/** Product names are catalog data, not UI chrome — a real API supplies them already in the seller's language. */
export const DASHBOARD_PRODUCTS: readonly Product[] = [
  {name: 'Wireless Headphones', sales: 482},
  {name: 'Ergonomic Desk Chair', sales: 376},
  {name: 'USB-C Docking Station', sales: 294},
  {name: 'Mechanical Keyboard', sales: 218},
  {name: '4K Monitor 27"', sales: 161},
];
