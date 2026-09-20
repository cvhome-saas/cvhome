/** Counters per state and class, for the stats route; the OpenTelemetry instruments join in the next commit. */
export function createCacheMetrics() {
    const counters = {};
    const byClass = {};
    return {
        record(state, cls) {
            counters[state] = (counters[state] ?? 0) + 1;
            const held = byClass[cls] ?? (byClass[cls] = {});
            held[state] = (held[state] ?? 0) + 1;
        },
        snapshot() {
            return {counters: {...counters}, byClass: structuredClone(byClass)};
        },
    };
}
