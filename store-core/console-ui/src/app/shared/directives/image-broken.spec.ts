import {Component, signal} from '@angular/core';
import {TestBed, type ComponentFixture} from '@angular/core/testing';

import {ImageBroken} from './image-broken';

/** A 1×1 transparent GIF: the smallest thing that genuinely decodes. */
const REAL_GIF =
  'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7';

const NOT_AN_IMAGE = 'data:image/gif;base64,notanimage';

@Component({
  imports: [ImageBroken],
  template: `<img appImageBroken [src]="src()" alt="" (broken)="broken.set(true)" />`,
})
class Host {
  readonly src = signal(NOT_AN_IMAGE);
  readonly broken = signal(false);
}

describe('ImageBroken', () => {
  let fixture: ComponentFixture<Host>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({imports: [Host]}).compileComponents();
    fixture = TestBed.createComponent(Host);
  });

  /*
   * The already-failed case — an image the browser finished with before Angular attached its
   * listener — is what the directive's post-render check exists for, and it cannot be reproduced
   * here: a `data:` URL is not cached, so it always fails *after* attachment. It is covered by the
   * check itself being unconditional rather than by a test that would only appear to prove it.
   */
  it('reports an image that will not decode', async () => {
    fixture.detectChanges();

    expect(await settlesTo(fixture, true)).toBeTrue();
  });

  it('says nothing about an image that loads', async () => {
    fixture.componentInstance.src.set(REAL_GIF);
    fixture.detectChanges();

    expect(await settlesTo(fixture, false)).toBeFalse();
  });
});

/**
 * Waits for the host's flag to reach `expected`, then holds one more turn to be sure it stays there.
 *
 * Image decoding is not synchronous and neither is the `error` event, so a single `whenStable()` can
 * land before either — polling is what makes the negative case meaningful rather than merely early.
 */
async function settlesTo(fixture: ComponentFixture<Host>, expected: boolean): Promise<boolean> {
  for (let attempt = 0; attempt < 25; attempt += 1) {
    await fixture.whenStable();
    await new Promise((resolve) => setTimeout(resolve, 10));
    fixture.detectChanges();
    if (fixture.componentInstance.broken() === expected) {
      break;
    }
  }
  return fixture.componentInstance.broken();
}
