import {Component, signal} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';

import {Panel} from './panel';

@Component({
  imports: [Panel],
  template: `
    <app-panel [title]="title()" [subtitle]="subtitle()" [meta]="meta()" [padded]="padded()">
      <button panelAction type="button">Add</button>
      <p class="body-content">Rows go here</p>
    </app-panel>
  `,
})
class Host {
  readonly title = signal('Top selling products');
  readonly subtitle = signal<string | null>(null);
  readonly meta = signal<string | null>(null);
  readonly padded = signal(false);
}

describe('Panel', () => {
  let fixture: ComponentFixture<Host>;
  let host: Host;
  let element: HTMLElement;

  const body = () => element.querySelector('.panel-body') as HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({imports: [Host]}).compileComponents();
    fixture = TestBed.createComponent(Host);
    host = fixture.componentInstance;
    fixture.detectChanges();
    element = fixture.nativeElement as HTMLElement;
  });

  it('gives the title a heading so a panel is reachable by structure', () => {
    const heading = element.querySelector('h2') as HTMLElement;
    expect(heading.textContent?.trim()).toBe('Top selling products');
  });

  it('omits the subtitle and meta lines when there are none', () => {
    expect(element.querySelector('.panel-subtitle')).toBeNull();
    expect(element.querySelector('.panel-meta')).toBeNull();

    host.subtitle.set('This month');
    host.meta.set('412');
    fixture.detectChanges();

    expect(element.querySelector('.panel-subtitle')?.textContent?.trim()).toBe('This month');
    expect(element.querySelector('.panel-meta')?.textContent?.trim()).toBe('412');
  });

  it('projects an action into the header, not the body', () => {
    const action = element.querySelector('.panel-head button');
    expect(action?.textContent?.trim()).toBe('Add');
    expect(body().querySelector('button')).toBeNull();
  });

  describe('the body interior', () => {
    /*
     * Measured rather than asserted by class name. `.panel-pad` existed three times, at three
     * different values, precisely because nobody could see what the panel itself did.
     */
    it('reaches the panel edges by default, for a table', () => {
      expect(getComputedStyle(body()).paddingLeft).toBe('0px');
    });

    it('takes the standard interior when asked', () => {
      host.padded.set(true);
      fixture.detectChanges();
      expect(getComputedStyle(body()).paddingLeft).not.toBe('0px');
    });
  });
});
