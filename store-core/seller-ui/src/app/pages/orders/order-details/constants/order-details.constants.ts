export const ORDER_STATUS_LIST = [
  { name: 'ORDERED', id: 'ORDERED' },
  { name: 'PROCESSED', id: 'PROCESSED' },
  { name: 'DELIVERED', id: 'DELIVERED' },
  { name: 'REFUNDED', id: 'REFUNDED' },
  { name: 'CANCELED', id: 'CANCELED' }
];

export const LANGUAGES = [
  { code: 'en', name: 'English' },
  { code: 'fr', name: 'French' }
];

export enum OrderDialogType {
  Transaction = '1',
  Invoice = '2',
  History = '3',
}
