export const ACTIONABLE_PAYMENT_STATUSES = ['PENDING', 'PROCESSING', 'WAITING_VERIFICATION', 'AUTHORIZED'];

export const EMPTY_PAYMENT_FILTER = {
  status: '',
  paymentType: '',
  requestRef: '',
  internalRef: '',
  transactionDateFrom: '',
  transactionDateTo: ''
};
