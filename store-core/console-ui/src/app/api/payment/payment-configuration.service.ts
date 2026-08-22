/** Ported from seller-ui/projects/seller-core/stores/src/lib/services/store.service.ts. */
import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {
  PersistablePaymentConfiguration,
  ReadablePaymentConfiguration,
} from '@models/payment';

/**
 * Ported from seller-ui/projects/seller-core/stores/src/lib/services/store.service.ts
 * (`getSupportedPaymentTypes`, `getPaymentConfigs`, `savePaymentConfig`, `updatePaymentConfig`),
 * verified against `PaymentConfigurationController`.
 *
 * Which gateways the storefront can take money through, and what they are configured with.
 * Distinct from `@api/payment/payment.service.ts`, which reads *transactions* — this one is the
 * settings behind them.
 *
 * `DELETE /{paymentType}` exists and is deliberately not ported: it removes a gateway's stored
 * credentials outright, and nothing in the design offers that. Turning a gateway off is
 * `enabled: false`, which is reversible; deleting is not, and an unasked-for destructive control is
 * worse than a missing one.
 */
const PAYMENT_CONFIG_API_BASE = '/spg/payment/api/v1/private/payment-configuration';

@Injectable({providedIn: 'root'})
export class PaymentConfigurationService {
  private readonly crudService = inject(CrudService);

  /** Every `PaymentType` the platform knows, by name — `COD`, `MANUAL_TRANSFER`, `STRIPE`, `PAYPAL`. */
  supportedTypes(): Observable<string[]> {
    return this.crudService.get(`${PAYMENT_CONFIG_API_BASE}/supported-payment-types`);
  }

  /** Only the gateways this store has a row for. A type absent here has never been configured. */
  configs(): Observable<ReadablePaymentConfiguration[]> {
    return this.crudService.get(PAYMENT_CONFIG_API_BASE);
  }

  /**
   * Creates a gateway's configuration.
   *
   * Builds a fresh entity, so a field omitted here is written as null — which is why the console
   * uses this only for a gateway that has no row yet, and `update` for one that has.
   */
  create(config: PersistablePaymentConfiguration): Observable<void> {
    return this.crudService.post(PAYMENT_CONFIG_API_BASE, config);
  }

  /**
   * Updates a gateway's configuration. 404 when it has none — `create` is the call for that.
   *
   * Partial: `updateEntity` skips a `null` field and leaves the stored value in place, which is how
   * a secret is kept rather than rewritten. `enabled` is not optional and is always applied.
   */
  update(paymentType: string, config: PersistablePaymentConfiguration): Observable<void> {
    return this.crudService.put(`${PAYMENT_CONFIG_API_BASE}/${paymentType}`, config);
  }
}
