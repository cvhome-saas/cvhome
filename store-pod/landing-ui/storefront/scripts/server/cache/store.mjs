/**
 * Where the page cache keeps its documents. One interface, so the middleware (`page-cache.mjs`) never knows whether
 * the copy it serves came from this process's memory or from a store every task shares.
 *
 * A `CacheStore`:
 * - `get(key)` → `Promise<Entry | undefined>`: the entry if the store still has it (past `ttlMs + staleMs` it is
 *   gone); whether it is fresh is the caller's to read from `storedAt` and `ttlMs`;
 * - `set(key, entry)` → `Promise<void>`: keeps the entry, evicting what it must; `entry.body` is a Buffer;
 * - `delete(key)`, `clear()` → `Promise<void>`;
 * - `size()`, `bytes()`: how many entries and how many body bytes are held, for the stats and the gauges;
 * - `onEvict(listener)`: `listener(key, entry, reason)` with reason `bytes`, `class-share`, `expired`, `replaced`.
 *
 * An `Entry` is `{status, headers, body, cls, storedAt, ttlMs, staleMs, locale?}`: what the shopper was sent, the
 * class it was kept under, when, and for how long it is fresh and then stale.
 *
 * `MemoryStore` (`memory-store.mjs`) is the one store today: each task keeps its own copy, bounded in bytes. A shared
 * store (Redis, Valkey) would implement the same interface with the key as the Redis key, the body as bytes, a key
 * TTL of `ttlMs + staleMs`, and `storedAt` inside the value so freshness is the same computation; only the
 * single-flight of a miss (`page-cache.mjs`) stays per process, which is fine: one render per task per cold page.
 */
import {PolicyError} from './policy.mjs';
import {MemoryStore} from './memory-store.mjs';

/** The store the policy names. */
export function createStore(policy, {now = Date.now} = {}) {
    if (policy.store === 'memory') {
        const shares = {};
        for (const [cls, rule] of policy.classes) {
            if (rule.enabled) {
                shares[cls] = rule.share;
            }
        }
        return new MemoryStore({maxBytes: policy.maxBytes, shares, now});
    }
    throw new PolicyError(`store ${JSON.stringify(policy.store)} is not one of: memory`);
}
