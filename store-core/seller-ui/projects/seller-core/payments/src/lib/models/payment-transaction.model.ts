export interface PaymentTransaction {
  internalRef: string;
  requestRef: string;
  amount: number;
  currency: {
    code: string;
  };
  paymentType: string;
  transactionDate: string;
  status: string;
  transactionNo: string;
}
