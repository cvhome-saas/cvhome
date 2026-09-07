/**
 * What an operator asks the gateway for: who to act as, and why. The session then is that account — its
 * own org, stores and roles, whatever they are — so there is nothing else to say.
 *
 * A model rather than part of the api service so the dialog in `shared/` can name it — `shared/` may
 * not depend on the api tier, and the shape is the gateway's contract, not the client's.
 */
export interface StartImpersonation {
  readonly userId: string;
  readonly reason: string;
}
