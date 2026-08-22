import {Observable, catchError, of} from 'rxjs';

/**
 * A leg of a wider load that is allowed to fail.
 *
 * The pattern store management named and four features then copied: every leg of a wide `forkJoin`
 * except the one that *is* the page is wrapped, because "a select that falls back to showing only
 * the current value is still a working page, whereas a failed `forkJoin` is a blank one".
 *
 * It was copied four times and drifted in the worst possible way — three services called it
 * `optional` and answered `null`, while store management's `optional` answered `[]` and its
 * `optionalOne` answered `null`. Two functions with one name and different contracts, which is why
 * these are named for what they return.
 *
 * **Wrap the legs, never the page.** A load whose *subject* fails must reach `error` so the page can
 * offer a retry; swallowing that would show an empty page as though the store genuinely had nothing.
 * Each call should carry a comment naming why that particular leg is allowed to fail.
 */
export function optionalOne<T>() {
  return (source: Observable<T>): Observable<T | null> => source.pipe(catchError(() => of(null)));
}

/** The same, for a leg whose absence is best expressed as "no options" rather than "unknown". */
export function optionalList<T>() {
  return (source: Observable<readonly T[]>): Observable<readonly T[]> =>
    source.pipe(catchError(() => of([] as readonly T[])));
}
