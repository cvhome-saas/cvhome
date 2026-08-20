import {Component, signal} from '@angular/core';
import {TestBed, type ComponentFixture} from '@angular/core/testing';

import {translocoTesting} from '@testing/transloco-testing';
import {ImagePicker, type ImageRules} from './image-picker';

const BANNER: ImageRules = {
  accept: 'image/jpeg,image/png',
  maxBytes: 5 * 1024 * 1024,
  minWidth: 1200,
  minHeight: 300,
  aspect: 4,
  aspectTolerance: 0.35,
};

/** A 1×1 transparent GIF: a real, decodable asset for the well to hold. */
const STORED =
  'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7';

@Component({
  imports: [ImagePicker],
  template: `
    <app-image-picker
      [label]="label"
      [rules]="rules"
      [url]="url()"
      [fileName]="fileName()"
      (picked)="picked.set($event)"
    />
  `,
})
class Host {
  readonly label = 'Banner';
  readonly rules = BANNER;
  readonly url = signal<string | null>(null);
  readonly fileName = signal<string | null>(null);
  readonly picked = signal<File | null>(null);
}

/**
 * The point of this component is what it *refuses*. The file input's `accept` is advisory in every
 * browser, so these are the checks standing between a mistyped asset and a multipart POST.
 */
describe('ImagePicker', () => {
  let fixture: ComponentFixture<Host>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Host, ...translocoTesting().imports],
      providers: [...translocoTesting().providers],
    }).compileComponents();
    fixture = TestBed.createComponent(Host);
    fixture.detectChanges();
  });

  it('takes an image that matches the slot', async () => {
    await choose(fixture, await png(1920, 480));

    expect(fixture.componentInstance.picked()).not.toBeNull();
    expect(error(fixture)).toBeNull();
  });

  it('refuses a file that is not one of the accepted formats', async () => {
    await choose(fixture, new File(['%PDF-1.4'], 'brochure.pdf', {type: 'application/pdf'}));

    expect(fixture.componentInstance.picked()).toBeNull();
    expect(error(fixture)).toContain('JPEG · PNG');
  });

  it('refuses a file heavier than the limit, and says how heavy it is', async () => {
    const heavy = new File([new Uint8Array(6 * 1024 * 1024)], 'huge.png', {type: 'image/png'});
    await choose(fixture, heavy);

    expect(fixture.componentInstance.picked()).toBeNull();
    expect(error(fixture)).toContain('6.0 MB');
  });

  it('refuses an image below the minimum, and quotes its actual size', async () => {
    await choose(fixture, await png(800, 200));

    expect(fixture.componentInstance.picked()).toBeNull();
    expect(error(fixture)).toContain('800 × 200');
  });

  it('refuses an image of the wrong shape even when it is big enough', async () => {
    // Comfortably past both minimums, but 1.6:1 where the slot wants 4:1.
    await choose(fixture, await png(1600, 1000));

    expect(fixture.componentInstance.picked()).toBeNull();
    expect(error(fixture)).toContain('wrong shape');
  });

  it('offers a closer look only when there is something to look at', async () => {
    expect(peek(fixture)).toBeNull();

    fixture.componentInstance.url.set(STORED);
    fixture.componentInstance.fileName.set('hero.png');
    fixture.detectChanges();
    await fixture.whenStable();

    expect(peek(fixture)).not.toBeNull();
    expect(peek(fixture)!.getAttribute('aria-label')).toContain('Banner');
  });

  it('opens the asset in a dialog and closes again', async () => {
    fixture.componentInstance.url.set(STORED);
    fixture.componentInstance.fileName.set('hero.png');
    fixture.detectChanges();
    await fixture.whenStable();

    peek(fixture)!.click();
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const dialog = fixture.nativeElement.querySelector('dialog') as HTMLDialogElement;
    expect(dialog.open).toBeTrue();
    expect(dialog.querySelector('.preview-image')?.getAttribute('src')).toBe(STORED);
    expect(dialog.textContent).toContain('hero.png');

    dialog.querySelector<HTMLButtonElement>('.preview-close')!.click();
    /*
     * The picture outlives the `close()` call on purpose — the dialog transitions out, and clearing
     * it immediately would fade an empty panel — so this waits past the exit rather than one turn.
     */
    for (let attempt = 0; attempt < 60 && dialog.querySelector('.preview-image'); attempt += 1) {
      await new Promise((resolve) => setTimeout(resolve, 20));
      fixture.detectChanges();
    }

    expect(dialog.open).toBeFalse();
    // Only then is it dropped, rather than left decoding behind a closed door.
    expect(dialog.querySelector('.preview-image')).toBeNull();
  });
});

function peek(fixture: ComponentFixture<Host>): HTMLButtonElement | null {
  return fixture.nativeElement.querySelector('.peek');
}

/**
 * Puts a file through the hidden input the way the browser would, and waits for the verdict.
 *
 * Polls rather than awaiting one turn: the dimension check decodes the image with
 * `createImageBitmap`, which resolves on its own schedule, so a single macrotask lands before the
 * answer exists and every refusal reads as an acceptance.
 */
async function choose(fixture: ComponentFixture<Host>, file: File): Promise<void> {
  const input = fixture.nativeElement.querySelector('input[type=file]') as HTMLInputElement;
  const transfer = new DataTransfer();
  transfer.items.add(file);
  input.files = transfer.files;
  input.dispatchEvent(new Event('change'));

  for (let attempt = 0; attempt < 40; attempt += 1) {
    await fixture.whenStable();
    await new Promise((resolve) => setTimeout(resolve, 10));
    fixture.detectChanges();
    if (error(fixture) || fixture.componentInstance.picked()) {
      return;
    }
  }
}

function error(fixture: ComponentFixture<Host>): string | null {
  return fixture.nativeElement.querySelector('.foot.error')?.textContent?.trim() ?? null;
}

/** A real PNG of the given size, so `createImageBitmap` has something to decode. */
async function png(width: number, height: number): Promise<File> {
  const canvas = document.createElement('canvas');
  canvas.width = width;
  canvas.height = height;
  canvas.getContext('2d')!.fillRect(0, 0, width, height);
  const blob = await new Promise<Blob | null>((resolve) => canvas.toBlob(resolve, 'image/png'));
  return new File([blob!], `${width}x${height}.png`, {type: 'image/png'});
}
