/**
 * Each request's abort signal, reachable from the backend calls its render makes.
 *
 * A render went on for a shopper who had already left: in the 2026-09-14 spike landing-ui stayed at its CPU cap for
 * 53–75 s after the load had stopped, rendering pages whose clients had timed out. The signal here aborts when the
 * response's connection closes before the response has been sent, and libs/services' apiFetch hands it to every
 * server-side fetch, so the render's next backend call fails at once and the render ends.
 *
 * The signal travels in an AsyncLocalStorage, which follows the render through every await. apiFetch reads it through
 * a global function rather than an import, so the browser bundle that shares apiFetch never pulls in async_hooks.
 */
import {AsyncLocalStorage} from 'node:async_hooks';

/** The global apiFetch reads; the same symbol is declared in libs/services/src/http-utils.ts. */
export const REQUEST_SIGNAL = Symbol.for('cvhome.storefront.requestSignal');

const scope = new AsyncLocalStorage();

/** Makes the current request's signal readable by apiFetch. Called once, at start. */
export function installRequestSignal() {
    globalThis[REQUEST_SIGNAL] = () => scope.getStore();
}

/**
 * Runs {@code handle} with a signal that aborts if {@code res} is closed before it finished — the shopper went away.
 */
export function withRequestSignal(res, handle) {
    const controller = new AbortController();
    res.once('close', () => {
        if (!res.writableFinished) {
            controller.abort(new Error('the client closed the connection before the response was sent'));
        }
    });
    return scope.run(controller.signal, handle);
}
