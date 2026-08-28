import {Component, signal} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';

import {Icon} from './icon';
import type {IconName} from './icon-paths';

@Component({
  imports: [Icon],
  template: `
    <div [attr.dir]="dir()">
      <app-icon [name]="name()" [flip]="flip()" [label]="label()" />
    </div>
  `,
})
class Host {
  readonly name = signal<IconName>('arrowRight');
  readonly flip = signal<boolean | undefined>(undefined);
  readonly label = signal<string | null>(null);
  readonly dir = signal('rtl');
}

describe('Icon', () => {
  let fixture: ComponentFixture<Host>;
  let host: Host;
  let element: HTMLElement;

  const svg = () => element.querySelector('svg') as SVGElement;
  const mirrored = () => svg().classList.contains('rtl-flip');

  beforeEach(async () => {
    await TestBed.configureTestingModule({imports: [Host]}).compileComponents();
    fixture = TestBed.createComponent(Host);
    host = fixture.componentInstance;
    fixture.detectChanges();
    element = fixture.nativeElement as HTMLElement;
  });

  describe('direction', () => {
    it('mirrors a glyph that points somewhere, without being asked', () => {
      // The whole point of the default: 52 call sites cannot each be relied on to know this, and
      // five of them did not — including both date pickers' month chevrons.
      for (const name of ['arrowRight', 'arrowLeft', 'chevronLeft', 'chevronRight'] as IconName[]) {
        host.name.set(name);
        fixture.detectChanges();
        expect(mirrored()).withContext(name).toBeTrue();
      }
    });

    it('leaves a glyph that does not point anywhere alone', () => {
      for (const name of ['chevronDown', 'search', 'trash', 'plus'] as IconName[]) {
        host.name.set(name);
        fixture.detectChanges();
        expect(mirrored()).withContext(name).toBeFalse();
      }
    });

    it('lets a call site overrule the default in both directions', () => {
      host.name.set('chevronRight');
      host.flip.set(false);
      fixture.detectChanges();
      expect(mirrored()).toBeFalse();

      host.name.set('search');
      host.flip.set(true);
      fixture.detectChanges();
      expect(mirrored()).toBeTrue();
    });
  });

  describe('accessible name', () => {
    it('is hidden from readers unless it is given one', () => {
      expect(svg().getAttribute('aria-hidden')).toBe('true');
      expect(svg().getAttribute('aria-label')).toBeNull();
    });

    it('is announced when it carries meaning of its own', () => {
      host.label.set('Translated in Arabic');
      fixture.detectChanges();
      expect(svg().getAttribute('aria-hidden')).toBeNull();
      expect(svg().getAttribute('aria-label')).toBe('Translated in Arabic');
    });
  });
});
