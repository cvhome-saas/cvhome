/**
 * The one thing `start.mjs` imports: the page cache built from the environment, wired to the store it names, the
 * loopback revalidator and the metrics, with the effective policy logged once.
 */
import {createPageCache} from './page-cache.mjs';
import {describePolicy, loadPolicy} from './policy.mjs';
import {createStore} from './store.mjs';
import {loopbackRevalidator} from './revalidate.mjs';
import {createCacheMetrics} from './metrics.mjs';

/**
 * @param port     read on each refresh: the server is listening only after start
 * @param degraded from request-scope.mjs
 * @throws PolicyError when the environment's policy cannot be run with; the caller decides whether to start
 */
export function installPageCache({env = process.env, port, degraded, log = console, now = Date.now} = {}) {
    const policy = loadPolicy(env);
    const store = createStore(policy, {now});
    const metrics = createCacheMetrics({policy, store, log});
    const cache = createPageCache({policy, store, degraded, metrics, log, now,
        revalidate: port ? loopbackRevalidator(port) : undefined});
    log.info(`[storefront-cache] policy ${describePolicy(policy)}`);
    return {serve: cache.serve, snapshot: cache.snapshot, policy, store};
}
