import {
  Component,
  DestroyRef,
  ElementRef,
  Injector,
  afterNextRender,
  effect,
  inject,
  input,
  signal,
  viewChild,
} from '@angular/core';

import {echarts, type EChartsCoreOption} from './echarts-setup';

/**
 * Owns the ECharts instance: creation, option updates, resizing and teardown.
 *
 * Chart components compose this rather than touching ECharts themselves, so there is one
 * place that knows about the library's lifecycle.
 *
 * Rendering is browser-only. ECharts needs a real canvas, so on the server (this app
 * prerenders) the host renders an empty, correctly-sized box and the chart paints on
 * hydration — `afterNextRender` never runs during SSR.
 *
 * A canvas is opaque to assistive tech, so the host carries `role="img"` and the caller's
 * `ariaLabel`. Charts here are always accompanied by the same figures in real DOM.
 */
@Component({
  selector: 'app-echart',
  template: `<div class="echart-surface" #surface></div>`,
  styles: `
    :host {
      display: block;
      inline-size: 100%;
    }

    .echart-surface {
      inline-size: 100%;
      block-size: 100%;
    }
  `,
  host: {
    role: 'img',
    '[attr.aria-label]': 'ariaLabel()',
  },
})
export class EChart {
  private readonly surface = viewChild.required<ElementRef<HTMLDivElement>>('surface');
  private readonly injector = inject(Injector);
  private readonly destroyRef = inject(DestroyRef);

  readonly option = input.required<EChartsCoreOption>();
  readonly ariaLabel = input.required<string>();

  private chart: echarts.ECharts | null = null;
  private readonly ready = signal(false);

  constructor() {
    afterNextRender(() => this.create(), {injector: this.injector});

    // Re-applies whenever the caller's option changes, and once more when the instance
    // first becomes available.
    effect(() => {
      const option = this.option();
      if (this.ready() && this.chart) {
        this.chart.setOption(option, {notMerge: true});
      }
    });

    this.destroyRef.onDestroy(() => {
      this.observer?.disconnect();
      this.chart?.dispose();
      this.chart = null;
    });
  }

  private observer: ResizeObserver | null = null;

  private create(): void {
    const host = this.surface().nativeElement;
    this.chart = echarts.init(host, undefined, {
      renderer: 'canvas',
      // Without this ECharts measures a zero-height container on first paint.
      height: host.clientHeight || undefined,
    });
    this.chart.setOption(this.option(), {notMerge: true});
    this.ready.set(true);

    // ResizeObserver always fires once immediately on observe(), even though nothing has
    // resized. Left unguarded, that call lands a few milliseconds after setOption() above
    // and its resize() snaps straight to the final layout — cutting off the entrance
    // animation before it is ever visible. Only genuine later size changes (window resize,
    // sidebar collapse) should trigger a resize.
    let firstObservation = true;
    this.observer = new ResizeObserver(() => {
      if (firstObservation) {
        firstObservation = false;
        return;
      }
      this.chart?.resize();
    });
    this.observer.observe(host);
  }
}
