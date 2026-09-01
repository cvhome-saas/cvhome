import {Component, signal} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';

import {BusyOverlay} from './busy-overlay';

@Component({
  imports: [BusyOverlay],
  template: `
    <app-busy-overlay [busy]="busy()" [reserve]="reserve()" label="Loading orders">
      @if (hasContent()) {
        <p class="rows">A row</p>
      }
    </app-busy-overlay>
  `,
})
class Host {
  readonly busy = signal(false);
  readonly reserve = signal<'none' | 'panel' | 'page'>('none');
  readonly hasContent = signal(true);
}

describe('BusyOverlay', () => {
  let fixture: ComponentFixture<Host>;
  let host: Host;
  let element: HTMLElement;

  const overlay = () => element.querySelector('app-busy-overlay') as HTMLElement;
  const content = () => element.querySelector('.busy-content') as HTMLElement;
  const height = () => Number.parseFloat(getComputedStyle(overlay()).minBlockSize);

  beforeEach(async () => {
    await TestBed.configureTestingModule({imports: [Host]}).compileComponents();
    fixture = TestBed.createComponent(Host);
    host = fixture.componentInstance;
    fixture.detectChanges();
    element = fixture.nativeElement as HTMLElement;
  });

  it('keeps the content on screen and takes it out of reach while busy', () => {
    expect(content().hasAttribute('inert')).toBeFalse();

    host.busy.set(true);
    fixture.detectChanges();

    expect(content().querySelector('.rows')).not.toBeNull();
    expect(content().hasAttribute('inert')).toBeTrue();
    expect(overlay().getAttribute('aria-busy')).toBe('true');
  });

  describe('the first-load reservation', () => {
    /*
     * Eight pages declared their own `.first-load` slab, at 26rem, 24rem and 60vh, so how far the
     * page jumped when data arrived depended on which page you were on.
     */
    it('holds no room open unless asked', () => {
      host.busy.set(true);
      fixture.detectChanges();
      expect(height() || 0).toBe(0);
    });

    it('holds a panel-sized slab open while a panel loads for the first time', () => {
      host.reserve.set('panel');
      host.busy.set(true);
      fixture.detectChanges();
      expect(height()).toBeGreaterThan(300);
    });

    it('lets go the moment the load finishes, so an empty result is not 60vh of nothing', () => {
      host.reserve.set('page');
      host.busy.set(true);
      fixture.detectChanges();
      const whileLoading = height();

      host.busy.set(false);
      fixture.detectChanges();

      expect(whileLoading).toBeGreaterThan(0);
      expect(height() || 0).toBe(0);
    });
  });
});
