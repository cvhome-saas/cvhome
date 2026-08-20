import {Component, computed, input, output, signal} from '@angular/core';
import {TranslocoDirective} from '@jsverse/transloco';

import {ImageBroken} from '@shared/directives/image-loaded';
import {ImagePicker, type ImageRules} from '@shared/ui/image-picker/image-picker';
import {Icon} from '@shared/ui/icon/icon';
import {Panel} from '@shared/ui/panel/panel';
import {SLIDER_CAPACITY, type SliderSlide} from '@models/store-settings';

/**
 * The home page carousel: one row per slide, in priority order.
 *
 * Reordering and deleting are real, and both are the same call — `PUT …/marketing/slider-images`
 * replaces the list outright, so "move this up" and "remove this" are both just a different list.
 * That is why the section emits the whole ordered list rather than an index or an id.
 *
 * What the design asks for and the record does not carry: a `LIVE`/`SCHEDULED` tag, a click-through
 * link, and a `1600×640 · 248 KB · JPG` line. `ReadableSliderImage` is a priority, a server-issued
 * name and a path — nothing else. Those blocks are gone rather than filled with plausible numbers.
 * TODO(lessons.md): see lessons.md, "Store management — slider images carry no schedule, link or
 * file metadata".
 *
 * The thumbnail is the pod's own path, which is not necessarily a URL this browser can reach — the
 * same gap the logo and the invoice hit. A slide whose image fails to load shows the placeholder
 * rather than a broken-image glyph. See lessons.md, "Orders — the store's logo URL is not reachable
 * from the browser".
 */
@Component({
  selector: 'app-slider-section',
  imports: [Icon, ImageBroken, ImagePicker, Panel, TranslocoDirective],
  template: `
    <app-panel [title]="t('storeSettings.slider.title')" [subtitle]="subtitle(t)" *transloco="let t">
      <div class="section-body slides">
        @for (slide of ordered(); track slide.name; let index = $index, count = $count) {
          <div class="slide">
            <span class="position" aria-hidden="true">{{ index + 1 }}</span>

            <span class="thumb" aria-hidden="true">
              @if (slide.url && !broken().has(slide.name)) {
                <img appImageBroken [src]="slide.url" alt="" (broken)="markBroken(slide.name)" />
              } @else {
                <app-icon name="images" />
              }
            </span>

            <div class="slide-copy">
              <strong>{{ t('storeSettings.slider.slideNumber', {position: index + 1}) }}</strong>
              <!--
                The stored name, whatever the pod made it — a filename on a seeded store, a generated
                id on an uploaded one. Shown quietly as the identifier it is, because it is the only
                thing that tells two slides apart.
              -->
              <small class="slide-ref" dir="ltr">{{ slide.name }}</small>
            </div>

            <div class="slide-actions">
              <button
                class="icon-action"
                type="button"
                [disabled]="index === 0 || busy()"
                [attr.aria-label]="t('storeSettings.slider.moveUp', {position: index + 1})"
                (click)="move(index, -1)"
              >
                <app-icon name="arrowUp" />
              </button>
              <button
                class="icon-action"
                type="button"
                [disabled]="index === count - 1 || busy()"
                [attr.aria-label]="t('storeSettings.slider.moveDown', {position: index + 1})"
                (click)="move(index, 1)"
              >
                <app-icon name="arrowDown" />
              </button>
              <button
                class="icon-action danger"
                type="button"
                [disabled]="busy()"
                [attr.aria-label]="t('storeSettings.slider.deleteNumbered', {position: index + 1})"
                (click)="remove(index)"
              >
                <app-icon name="trash" />
              </button>
            </div>
          </div>
        } @empty {
          <p class="slots-full">{{ t('storeSettings.slider.none') }}</p>
        }

        @if (hasRoom()) {
          <!--
            The same well the branding assets use, at the carousel's proportions. It carries the
            checks with it: a slide that is the wrong shape or too small for a hero never becomes a
            multipart POST, and the well itself reports the upload rather than a toast at the far
            corner of the page.
          -->
          <app-image-picker
            class="add-slide"
            [label]="t('storeSettings.slider.dropImages')"
            [rules]="slideRules"
            [busy]="uploading()"
            (picked)="picked.emit($event)"
          />
        } @else {
          <p class="slots-full">
            {{ t('storeSettings.slider.slotsFull', {capacity}) }}
          </p>
        }
      </div>
    </app-panel>
  `,
  styleUrls: ['../settings-card.css', './slider-section.css'],
})
export class SliderSection {
  readonly slides = input.required<readonly SliderSlide[]>();
  readonly uploading = input(false);
  readonly reordering = input(false);

  /** One slide to upload. */
  readonly picked = output<File>();
  /** The list the slider should become, already in the order it should be saved in. */
  readonly reordered = output<{slides: readonly SliderSlide[]; messageKey: string}>();

  protected readonly capacity = SLIDER_CAPACITY;

  /**
   * What a slide has to be.
   *
   * 1600×640 is the carousel's own frame, and 2.5:1 is the shape a hero crop has to be to survive
   * it. A portrait photograph dropped here would be cropped to a band across its middle, which is
   * the kind of thing nobody notices until it is on the storefront.
   */
  protected readonly slideRules: ImageRules = {
    accept: 'image/jpeg,image/png',
    maxBytes: 5 * 1024 * 1024,
    minWidth: 1200,
    minHeight: 480,
    aspect: 2.5,
    aspectTolerance: 0.35,
  };

  /** Slides whose image would not load. Keyed by name, so a reorder does not resurrect a broken one. */
  protected readonly broken = signal<ReadonlySet<string>>(new Set());

  protected readonly busy = computed(() => this.uploading() || this.reordering());

  protected readonly ordered = computed(() =>
    [...this.slides()].sort((left, right) => left.priority - right.priority),
  );

  /**
   * Whether another slide fits.
   *
   * `SLIDER_CAPACITY` is the console's own limit, not the server's — nothing in the pod caps the list.
   * Kept because a carousel of thirty is a broken storefront rather than a configured one, but it is a
   * house rule and the copy says "slots" rather than claiming the platform refuses more.
   */
  protected readonly hasRoom = computed(() => this.slides().length < SLIDER_CAPACITY);

  protected subtitle(t: (key: string, params?: Record<string, unknown>) => string): string {
    return t('storeSettings.slider.subtitle', {count: this.slides().length, capacity: SLIDER_CAPACITY});
  }

  /**
   * Swaps a slide with its neighbour.
   *
   * The whole list goes out, renumbered by the api service. Sending a pair of priorities instead
   * would leave the rest of the list unstated, and the endpoint does not merge — it replaces.
   */
  protected move(index: number, by: -1 | 1): void {
    const next = [...this.ordered()];
    const target = index + by;
    if (target < 0 || target >= next.length) {
      return;
    }
    [next[index], next[target]] = [next[target], next[index]];
    this.reordered.emit({slides: next, messageKey: 'storeSettings.slider.reordered'});
  }

  protected markBroken(name: string): void {
    this.broken.update((current) => new Set(current).add(name));
  }

  protected remove(index: number): void {
    const next = this.ordered().filter((_, position) => position !== index);
    this.reordered.emit({slides: next, messageKey: 'storeSettings.slider.deleted'});
  }

}
