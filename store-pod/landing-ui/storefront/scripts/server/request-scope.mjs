/**
 * Each request's abort signal, reachable from the backend calls its render makes, and whether one of them gave up.
 *
 * A render went on for a shopper who had already left: in the 2026-09-14 spike landing-ui stayed at its CPU cap for
 * 53–75 s after the load had stopped, rendering pages whose clients had timed out. The signal here aborts when the
 * response's connection closes before the response has been sent, and libs/services' apiFetch hands it to every
 * server-side read, so the render's next backend call fails at once and the render ends. A render other shoppers are
 * waiting on (the page cache's shared render) is the exception: it is kept alive while any of them is still there.
 *
 * A read that was aborted or timed out and rendered without its section (apiFetch's orUndefined) marks the request
 * degraded, so the page cache does not keep a page with a hole in it for every shopper of the store.
 *
 * The state travels in an AsyncLocalStorage, which follows the render through every await. apiFetch reaches it through
 * global functions rather than an import, so the browser bundle that shares apiFetch never pulls in async_hooks.
 */
import {AsyncLocalStorage} from 'node:async_hooks';

/** The global apiFetch reads the signal through; the same symbol is declared in libs/services/src/http-utils.ts. */
export const REQUEST_SIGNAL = Symbol.for('cvhome.storefront.requestSignal');

/** The global apiFetch marks a degraded render through; declared in libs/services/src/http-utils.ts as well. */
export const REQUEST_DEGRADED = Symbol.for('cvhome.storefront.requestDegraded');

const scope = new AsyncLocalStorage();

const byResponse = new WeakMap();

/** Makes the current request's signal readable, and its render markable as degraded, by apiFetch. Called once, at start. */
export function installRequestSignal() {
    globalThis[REQUEST_SIGNAL] = () => scope.getStore()?.signal;
    globalThis[REQUEST_DEGRADED] = () => {
        const request = scope.getStore();
        if (request) {
            request.degraded = true;
        }
    };
}

/**
 * Runs {@code handle} with a signal that aborts if {@code res} is closed before it finished — the shopper went away —
 * unless {@code keepAlive} says other shoppers are still waiting on what it renders.
 */
export function withRequestSignal(res, handle, keepAlive = () => false) {
    const controller = new AbortController();
    const request = {signal: controller.signal, degraded: false};
    byResponse.set(res, request);
    res.once('close', () => {
        if (!res.writableFinished && !keepAlive()) {
            controller.abort(new Error('the client closed the connection before the response was sent'));
        }
    });
    return scope.run(request, handle);
}

/** Whether a backend read of the render behind {@code res} was aborted or timed out. */
export function isDegraded(res) {
    return byResponse.get(res)?.degraded === true;
}
