import {TestBed, fakeAsync, tick, discardPeriodicTasks} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {Observable, of, throwError} from 'rxjs';

import {ManagerStoreService} from '@api/tenancy/manager-store.service';
import {PodService} from '@api/pod-registry/pod.service';
import {ApiError} from '@core/errors/api-error';
import {NOTIFICATION_PORT} from '@core/errors/notification.port';
import {ConsoleApi} from '@layouts/console-shell/services/console.api.service';
import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import type {Pod} from '@models/pod';
import type {CreateStoreRequest, ManagerStore, ProvisioningState} from '@models/tenancy';
import {FakeConsoleApi} from '@testing/console-api.fake';
import {translocoTesting} from '@testing/transloco-testing';
import {CreateStoreFacade} from './facades/create-store.facade';

function store(state: ProvisioningState): ManagerStore {
  return {
    id: 'store-1',
    name: 'Acme Supply Co.',
    orgId: {id: 'org-1'},
    podId: {id: 'pod-1'},
    provisioningState: state,
    status: 'ACTIVE',
    billingStatus: null,
  };
}

class FakeManagerStoreService {
  taken = false;
  infoCalls = 0;
  /** The state each successive poll reports. The last entry repeats. */
  states: ProvisioningState[] = ['SUCCESSFULLY_PROVISIONING'];

  nameExists(): Observable<boolean> {
    return of(this.taken);
  }

  storeInfo(): Observable<ManagerStore> {
    const state = this.states[Math.min(this.infoCalls, this.states.length - 1)];
    this.infoCalls++;
    return of(store(state));
  }
}

class FakeConsoleApiWithCreate extends FakeConsoleApi {
  requests: CreateStoreRequest[] = [];
  createError: unknown = null;

  createStore(request: CreateStoreRequest): Observable<ManagerStore> {
    this.requests.push(request);
    if (this.createError) {
      return throwError(() => this.createError);
    }
    return of(store('IN_PROGRESS_PROVISIONING'));
  }
}

describe('CreateStoreFacade', () => {
  let stores: FakeManagerStoreService;
  let console_: FakeConsoleApiWithCreate;
  let pods: {list(): Observable<Pod[]>};
  let podList: Pod[];
  let facade: CreateStoreFacade;

  beforeEach(() => {
    localStorage.removeItem('cvhome.console.store');
    stores = new FakeManagerStoreService();
    console_ = new FakeConsoleApiWithCreate();
    podList = [];
    pods = {list: () => of(podList)};

    TestBed.configureTestingModule({
      imports: [...translocoTesting().imports],
      providers: [
        provideRouter([]),
        ...translocoTesting().providers,
        {provide: ManagerStoreService, useValue: stores},
        {provide: PodService, useValue: pods},
        {provide: ConsoleApi, useValue: console_},
        {provide: NOTIFICATION_PORT, useValue: {danger: () => undefined}},
      ],
    });
    TestBed.inject(ConsoleShellFacade);
    facade = TestBed.inject(CreateStoreFacade);
  });

  function fill(name = 'Acme Supply Co.'): void {
    facade.form.controls.name.setValue(name);
    facade.form.controls.name.updateValueAndValidity();
  }

  it('posts the name and the merchant fields tenancy forwards, and no pod when none was chosen', fakeAsync(() => {
    fill();
    tick(500);
    facade.start();
    tick();

    expect(console_.requests.length).toBe(1);
    expect(console_.requests[0]['name']).toBe('Acme Supply Co.');
    expect(console_.requests[0]['country']).toBe('DE');
    expect(console_.requests[0]['currency']).toBe('EUR');
    // The registry places the store when the operator has no pod to ask for.
    expect(console_.requests[0]['pod']).toBeUndefined();

    discardPeriodicTasks();
  }));

  it('sends a chosen pod as a preference', fakeAsync(() => {
    podList = [{name: 'EU Central', shortenPodId: 'eu-1', endpoint: {endpoint: 'https://eu', type: 'EXTERNAL'}, orgId: {id: 'org-1'}, id: {id: 'pod-9'}}];
    fill();
    tick(500);
    facade.form.controls.podId.setValue('pod-9');
    facade.start();
    tick();

    expect(console_.requests[0]['pod']).toEqual({id: 'pod-9'});

    discardPeriodicTasks();
  }));

  it('blocks a name the server already has', fakeAsync(() => {
    stores.taken = true;
    fill();
    tick(500);

    expect(facade.form.controls.name.hasError('nameTaken')).toBeTrue();

    facade.start();
    tick();
    expect(console_.requests).toEqual([]);
  }));

  it('polls until the server says the store is ready', fakeAsync(() => {
    stores.states = ['IN_PROGRESS_PROVISIONING', 'IN_PROGRESS_PROVISIONING', 'SUCCESSFULLY_PROVISIONING'];
    fill();
    tick(500);
    facade.start();
    tick();

    // Create answered, but the store is not ready — the page must not say it is.
    expect(facade.phase()).toBe('running');
    expect(facade.isDone()).toBeFalse();

    tick(2000);
    expect(facade.isDone()).toBeFalse();
    tick(2000);
    expect(facade.isDone()).toBeFalse();
    tick(2000);

    expect(facade.isDone()).toBeTrue();
    // Settled, so polling stops rather than hammering the endpoint forever.
    const calls = stores.infoCalls;
    tick(10000);
    expect(stores.infoCalls).toBe(calls);
  }));

  it('reports a provisioning failure, which the old timer could never reach', fakeAsync(() => {
    stores.states = ['FAILED_PROVISIONING'];
    fill();
    tick(500);
    facade.start();
    tick();
    tick(2000);

    expect(facade.hasFailed()).toBeTrue();
    expect(facade.isDone()).toBeFalse();

    tick(10000);
    // A failure is terminal too — no point re-reading a row that will not change.
    expect(stores.infoCalls).toBe(1);
  }));

  it('gives up after the timeout without claiming anything failed', fakeAsync(() => {
    stores.states = ['IN_PROGRESS_PROVISIONING'];
    fill();
    tick(500);
    facade.start();
    tick();
    tick(125_000);

    expect(facade.timedOut()).toBeTrue();
    expect(facade.hasFailed()).toBeFalse();
    expect(facade.isDone()).toBeFalse();

    discardPeriodicTasks();
  }));

  it('keeps polling through a transient read failure', fakeAsync(() => {
    let calls = 0;
    stores.storeInfo = () => {
      calls++;
      return calls === 1 ? throwError(() => new Error('blip')) : of(store('SUCCESSFULLY_PROVISIONING'));
    };
    fill();
    tick(500);
    facade.start();
    tick();
    tick(2000);
    tick(2000);

    expect(facade.isDone()).toBeTrue();
    discardPeriodicTasks();
  }));

  it('stays on the form when the create is refused', fakeAsync(() => {
    // What billing answers when the org is out of store allowance.
    console_.createError = new ApiError({
      code: 'BILLING.STORE_QUOTA.REFUSED',
      category: 'UNPROCESSABLE',
      status: 422,
    });
    fill();
    tick(500);
    facade.start();
    tick();

    expect(facade.phase()).toBe('form');
    expect(facade.submitting()).toBeFalse();
    expect(facade.store()).toBeNull();
  }));
});
