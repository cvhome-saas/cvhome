import {ErrorHandler, Injectable} from '@angular/core';
import {isApiError} from './api-error';

/**
 * The last resort, for whatever escapes a call site.
 *
 * Its job is diagnostic, not presentational: it does not toast. A toast here would double up with the one
 * the call site already showed in every case where the call site handled the error properly, and the cases
 * where it did not are bugs to fix rather than to paper over.
 *
 * This is the **only** place `detail` may be logged. `PAYMENT.INITIATE.REJECTED` arriving with
 * `providerCode: card_declined` is precisely what support asks about, and the `traceId` is what joins this
 * line to the server log for the same request.
 */
@Injectable()
export class GlobalErrorHandler implements ErrorHandler {

  handleError(error: unknown): void {
    if (isApiError(error)) {
      console.error('Unhandled API error', error.toLogContext(), error);
      return;
    }
    console.error('Unhandled error', error);
  }
}
