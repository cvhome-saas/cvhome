/**
 * A code the server stores, with the name a reader sees. The code is the value; the label is prose.
 *
 * At the bottom tier because `app-locale-switcher` and `app-select` name it when they describe the
 * options they render, while the service that *fetches* reference data — country lists, currency
 * lists — is an application concern and stays in the consumer. The shape is the only part both
 * halves need to agree on.
 */
export interface ReferenceOption {
  readonly code: string;
  readonly label: string;
}
