/**
 * What an operator asks the gateway for: who to act as, in which store, how, and why.
 *
 * A model rather than part of the api service so the dialog in `shared/` can name it — `shared/` may
 * not depend on the api tier, and the shape is the gateway's contract, not the client's.
 */
export interface StartImpersonation {
  readonly userId: string;
  readonly storeId: string;
  readonly mode: 'read' | 'write';
  readonly reason: string;
}
