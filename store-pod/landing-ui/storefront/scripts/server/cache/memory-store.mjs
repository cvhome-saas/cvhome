/**
 * The in-process store: a Map in least-recently-used order, bounded by bytes overall and per class.
 *
 * `set` puts the entry last; `get` moves it last; when the bytes exceed the bound the first entries go. A class is
 * bounded to its share of the bytes so that, say, ten thousand product pages cannot push the home page out: when a
 * class is over its share, its own oldest entries go first. An entry past `ttlMs + staleMs` is dropped when read.
 */
export class MemoryStore {
    #entries = new Map();
    #bytes = 0;
    #bytesByClass = new Map();
    #maxBytes;
    #shares;
    #now;
    #listeners = [];

    /** @param shares the fraction of `maxBytes` each class may hold (`{home: 0.25}`); a class without one is unbounded */
    constructor({maxBytes, shares = {}, now = Date.now}) {
        this.#maxBytes = maxBytes;
        this.#shares = shares;
        this.#now = now;
    }

    async get(key) {
        const entry = this.#entries.get(key);
        if (!entry) {
            return undefined;
        }
        if (this.#now() - entry.storedAt >= entry.ttlMs + entry.staleMs) {
            this.#evict(key, 'expired');
            return undefined;
        }
        this.#entries.delete(key);
        this.#entries.set(key, entry);
        return entry;
    }

    async set(key, entry) {
        if (this.#entries.has(key)) {
            this.#evict(key, 'replaced');
        }
        this.#entries.set(key, entry);
        this.#bytes += entry.body.length;
        this.#bytesByClass.set(entry.cls, (this.#bytesByClass.get(entry.cls) ?? 0) + entry.body.length);
        const share = this.#shares[entry.cls];
        if (share !== undefined) {
            const cap = this.#maxBytes * share;
            for (const [oldest, held] of this.#entries) {
                if ((this.#bytesByClass.get(entry.cls) ?? 0) <= cap) {
                    break;
                }
                if (held.cls === entry.cls) {
                    this.#evict(oldest, 'class-share');
                }
            }
        }
        for (const oldest of this.#entries.keys()) {
            if (this.#bytes <= this.#maxBytes) {
                break;
            }
            this.#evict(oldest, 'bytes');
        }
    }

    async delete(key) {
        if (this.#entries.has(key)) {
            this.#evict(key, 'replaced');
        }
    }

    async clear() {
        for (const key of [...this.#entries.keys()]) {
            this.#evict(key, 'replaced');
        }
    }

    size() {
        return this.#entries.size;
    }

    bytes() {
        return this.#bytes;
    }

    /** Entries and bytes per class, for the stats and the gauges. */
    byClass() {
        const result = {};
        for (const entry of this.#entries.values()) {
            const held = result[entry.cls] ?? (result[entry.cls] = {entries: 0, bytes: 0});
            held.entries += 1;
            held.bytes += entry.body.length;
        }
        return result;
    }

    onEvict(listener) {
        this.#listeners.push(listener);
    }

    #evict(key, reason) {
        const entry = this.#entries.get(key);
        if (!entry) {
            return;
        }
        this.#entries.delete(key);
        this.#bytes -= entry.body.length;
        this.#bytesByClass.set(entry.cls, (this.#bytesByClass.get(entry.cls) ?? 0) - entry.body.length);
        for (const listener of this.#listeners) {
            listener(key, entry, reason);
        }
    }
}
