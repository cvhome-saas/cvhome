import {Component, computed, inject, input} from '@angular/core';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {Badge} from '@shared/ui/badge/badge';
import {FileDrop} from '@shared/ui/file-drop/file-drop';
import {Icon} from '@shared/ui/icon/icon';
import {Panel} from '@shared/ui/panel/panel';
import {ToastService} from '@shared/ui/toast/toast';
import {SLIDER_CAPACITY, SLIDE_STATE_TONE, type SliderSlide} from '@models/store-settings';

/**
 * The home page carousel: one row per slide, in priority order.
 *
 * Reordering, editing and deleting a slide all need endpoints that do not exist yet, so each
 * says so. The rows are ordered by `priority`, which is what `ReadableSliderImage` carries —
 * position in the array is not the contract.
 */
@Component({
  selector: 'app-slider-section',
  imports: [Badge, FileDrop, Icon, Panel, TranslocoDirective],
  template: `
    <app-panel [title]="t('storeSettings.slider.title')" [subtitle]="subtitle(t)" *transloco="let t">
      <button panelAction class="primary-action" type="button" (click)="notSupported(t('storeSettings.slider.addingSlide'))">
        <app-icon name="plus" />
        {{ t('storeSettings.slider.addSlide') }}
      </button>

      <div class="section-body slides">
        @for (slide of ordered(); track slide.id) {
          <div class="slide">
            <!--
              A grab handle, not a button: dragging is what it is for, and there is nothing to
              drag to yet. It is titled so the reason is discoverable.
            -->
            <app-icon name="grip" class="handle" [title]="t('storeSettings.slider.reorderUnavailable')" />

            <span class="thumb" aria-hidden="true">
              @if (slide.url) {
                <img [src]="slide.url" [alt]="slide.name" />
              } @else {
                <app-icon name="images" />
              }
            </span>

            <div class="slide-copy">
              <strong>{{ slide.name }}</strong>
              <small>{{ slide.meta }}</small>
              <small class="slide-link">{{ t('storeSettings.slider.linksTo', {link: slide.link}) }}</small>
            </div>

            <app-badge [tone]="toneOf(slide)" shape="square">{{ t(stateKeyOf(slide)) }}</app-badge>

            <div class="slide-actions">
              <button
                class="icon-action"
                type="button"
                [attr.aria-label]="t('storeSettings.slider.editNamed', {name: slide.name})"
                (click)="notSupported(t('storeSettings.slider.editingSlide'))"
              >
                <app-icon name="pencil" />
              </button>
              <button
                class="icon-action danger"
                type="button"
                [attr.aria-label]="t('storeSettings.slider.deleteNamed', {name: slide.name})"
                (click)="notSupported(t('storeSettings.slider.deletingSlide'))"
              >
                <app-icon name="trash" />
              </button>
            </div>
          </div>
        }

        @if (hasRoom()) {
          <app-file-drop
            [label]="t('storeSettings.slider.dropImages')"
            [hint]="t('storeSettings.slider.hint', {capacity})"
            [multiple]="true"
            (filesSelected)="notSupported(t('storeSettings.slider.uploadingImages'))"
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
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);

  readonly slides = input.required<readonly SliderSlide[]>();

  protected readonly capacity = SLIDER_CAPACITY;

  protected readonly ordered = computed(() =>
    [...this.slides()].sort((left, right) => left.priority - right.priority),
  );

  protected readonly hasRoom = computed(() => this.slides().length < SLIDER_CAPACITY);

  protected subtitle(t: (key: string, params?: Record<string, unknown>) => string): string {
    return t('storeSettings.slider.subtitle', {count: this.slides().length, capacity: SLIDER_CAPACITY});
  }

  protected toneOf(slide: SliderSlide) {
    return SLIDE_STATE_TONE[slide.state];
  }

  protected stateKeyOf(slide: SliderSlide): string {
    return slide.state === 'LIVE' ? 'storeSettings.slider.live' : 'storeSettings.slider.scheduled';
  }

  protected notSupported(what: string): void {
    this.toast.info(this.transloco.translate('storeSettings.notAvailable', {what}));
  }
}
