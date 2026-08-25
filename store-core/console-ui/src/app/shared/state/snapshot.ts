import {Signal, computed, linkedSignal} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {Observable} from 'rxjs';

/** What every page's load looks like, whatever it loaded. */
export interface Snapshot<T> {
  /** The most recent value, or the previous one while a new one is in flight. */
  readonly value: Signal<T | undefined>;
  readonly isLoading: Signal<boolean>;
  readonly error: Signal<Error | undefined>;
  /** Nothing has ever loaded — the first-load state, distinct from "loaded and empty". */
  readonly isEmpty: Signal<boolean>;
  /** Re-runs the current request. What the load-error's button calls. */
  readonly reload: () => void;
}

/**
 * A page's data, loaded, with the last good value kept while the next one arrives.
 *
 * Seven facades wrote this by hand and identically — `rxResource`, a `linkedSignal` holding the
 * previous value, `isLoading`, `error as Error | undefined`, `isEmpty`, `retry()` — each with a doc
 * comment saying it followed the one before it. Around thirty lines apiece for a pattern nobody
 * ever varied on purpose.
 *
 * Two behaviours are the reason it exists rather than each page calling `rxResource` directly:
 *
 * **The last good value.** A resource drops to `undefined` while it reloads, so a table would blank
 * on every filter change and a page would flash its first-load placeholder between two states that
 * both had data. `linkedSignal` holds the previous value until the next one lands, which is what
 * makes `isEmpty` mean "nothing has ever loaded" rather than "nothing right now" — and that is the
 * distinction the busy overlay reads.
 *
 * **`params` returning `undefined` means "not ready".** A resource keyed on several signals that
 * settle at different times runs once per signal: the product form managed eighteen requests to
 * answer six questions, two rounds cancelled mid-flight, because its params read a route id and a
 * store id that arrived in separate ticks (lessons.md). A gate is not the same as a key — return
 * `undefined` until every input a request genuinely needs is present, and the resource waits.
 *
 * ```ts
 * private readonly snapshot = snapshot(
 *   () => (this.active() ? this.query() : undefined),
 *   (query) => this.api.loadSnapshot(query),
 * );
 * readonly isLoading = this.snapshot.isLoading;
 * ```
 */
export function snapshot<T, P>(
  params: () => P | undefined,
  stream: (params: P) => Observable<T>,
): Snapshot<T> {
  // `P | undefined` is the resource's parameter type, not `P`: `undefined` is the "not ready" gate
  // documented above, and `rxResource` only skips the load when the params signal actually says so.
  const resource = rxResource<T, P | undefined>({
    params,
    stream: ({params: value}) => stream(value as P),
  });

  const value = linkedSignal<T | undefined, T | undefined>({
    source: () => (resource.hasValue() ? resource.value() : undefined),
    computation: (incoming, previous) => incoming ?? previous?.value,
  });

  return {
    value,
    isLoading: resource.isLoading,
    error: computed(() => resource.error() as Error | undefined),
    isEmpty: computed(() => value() === undefined),
    reload: () => resource.reload(),
  };
}
