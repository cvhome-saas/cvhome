import {TestBed, fakeAsync, tick} from '@angular/core/testing';

import {ToastService} from './toast';

describe('ToastService', () => {
  let service: ToastService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ToastService);
  });

  it('adds a message with a stable id', () => {
    service.success('Saved');
    expect(service.messages().length).toBe(1);
    expect(service.messages()[0].text).toBe('Saved');
    expect(service.messages()[0].id).toBeTruthy();
  });

  it('auto-dismisses success, info and warning toasts after their duration', fakeAsync(() => {
    service.success('Saved');
    service.info('Heads up');
    service.warning('Careful');
    expect(service.messages().length).toBe(3);

    tick(4500);
    expect(service.messages().map((m) => m.text)).toEqual(['Careful']);

    tick(1500);
    expect(service.messages().length).toBe(0);
  }));

  it('never auto-dismisses a danger toast', fakeAsync(() => {
    service.danger('Something broke');
    tick(60_000);
    expect(service.messages().length).toBe(1);

    service.dismiss(service.messages()[0].id);
    expect(service.messages().length).toBe(0);
  }));

  it('dismiss removes a specific toast by id and cancels its timer', fakeAsync(() => {
    service.success('First');
    service.success('Second');
    const [first, second] = service.messages();

    service.dismiss(first.id);
    expect(service.messages().map((m) => m.text)).toEqual(['Second']);

    // The cancelled timer for `first` must not throw or remove anything once it would
    // otherwise have fired.
    tick(4500);
    expect(service.messages().map((m) => m.text)).toEqual([]);
    void second;
  }));

  it('pause freezes the countdown and resume continues from where it left off', fakeAsync(() => {
    service.warning('Careful');
    const id = service.messages()[0].id;

    tick(4000);
    service.pause(id);

    // Paused: no amount of elapsed time should remove it.
    tick(10_000);
    expect(service.messages().length).toBe(1);

    service.resume(id);
    // 2000ms remained (6000ms duration - 4000ms already elapsed) when paused.
    tick(1999);
    expect(service.messages().length).toBe(1);
    tick(1);
    expect(service.messages().length).toBe(0);
  }));

  it('still raises a toast where crypto.randomUUID does not exist', () => {
    // Insecure origins — plain HTTP in development — do not expose it. It threw there, and every
    // toast in the app went silent, error reports included.
    const original = crypto.randomUUID;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (crypto as any).randomUUID = undefined;
    try {
      const service = TestBed.inject(ToastService);
      service.info('One');
      service.info('Two');

      expect(service.messages().length).toBe(2);
      // Distinct, or `@for`'s track would collapse them into one.
      expect(service.messages()[0].id).not.toBe(service.messages()[1].id);
    } finally {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      (crypto as any).randomUUID = original;
    }
  });
});
