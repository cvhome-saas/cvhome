import {signal} from '@angular/core';
import {TestBed, fakeAsync, tick} from '@angular/core/testing';
import {Observable, Subject, of, throwError} from 'rxjs';

import {snapshot, type Snapshot} from './snapshot';

interface Page {
  readonly rows: readonly string[];
}

describe('snapshot', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  const build = (
    params: () => number | undefined,
    stream: (id: number) => Observable<Page>,
  ): Snapshot<Page> =>
    TestBed.runInInjectionContext(() => snapshot<Page, number>(params, stream));

  it('starts empty and reports the first load', fakeAsync(() => {
    const id = signal<number>(1);
    const state = build(id, (value) => of({rows: [`row ${value}`]}));

    expect(state.isEmpty()).toBeTrue();
    TestBed.tick();
    tick();
    expect(state.value()).toEqual({rows: ['row 1']});
    expect(state.isEmpty()).toBeFalse();
    expect(state.error()).toBeUndefined();
  }));

  /*
   * The reason the `linkedSignal` is here. A bare resource drops to `undefined` while it reloads,
   * so a table blanks on every filter change and the page flashes its first-load placeholder
   * between two states that both had data.
   */
  it('holds the last good value while the next one is in flight', fakeAsync(() => {
    const id = signal(1);
    const answers = new Subject<Page>();
    const state = build(id, () => answers.asObservable() as never);

    TestBed.tick();
    answers.next({rows: ['first']});
    tick();
    expect(state.value()).toEqual({rows: ['first']});

    id.set(2);
    TestBed.tick();
    tick();

    expect(state.isLoading()).toBeTrue();
    expect(state.value()).withContext('previous page must stay on screen').toEqual({rows: ['first']});
    expect(state.isEmpty()).toBeFalse();
  }));

  /*
   * The gate. A resource keyed on several signals that settle at different times runs once per
   * signal: the product form managed eighteen requests to answer six questions because its params
   * read a route id and a store id that arrived in separate ticks.
   */
  it('does not run at all until every input it needs is present', fakeAsync(() => {
    const id = signal<number | undefined>(undefined);
    const stream = jasmine.createSpy('stream').and.returnValue(of({rows: []}));
    const state = build(() => id(), stream as never);

    TestBed.tick();
    tick();
    expect(stream).not.toHaveBeenCalled();
    expect(state.isEmpty()).toBeTrue();

    id.set(7);
    TestBed.tick();
    tick();
    expect(stream).toHaveBeenCalledTimes(1);
  }));

  it('surfaces a failure as an Error the page can offer a retry for', fakeAsync(() => {
    const state = build(
      () => 1,
      () => throwError(() => new Error('offline')) as never,
    );

    TestBed.tick();
    tick();
    expect(state.error()?.message).toBe('offline');
  }));
});
