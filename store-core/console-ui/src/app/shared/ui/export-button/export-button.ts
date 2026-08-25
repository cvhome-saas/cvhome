import {Component, ElementRef, inject, input, output, signal} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';

import {PdfExportService} from '@core/export/pdf-export.service';
import {Icon} from '@shared/ui/icon/icon';

/**
 * Exports a region of the page to PDF.
 *
 * Point it at the element to capture and give it a file name; it handles the capture, its
 * own busy state, and failure. Any page can drop one in:
 *
 * ```html
 * <app-export-button [target]="report()" fileName="orders" title="Orders"
 *                    [subtitle]="periodLabel()" />
 * …
 * <div #report> … </div>
 * ```
 *
 * Accepts the raw element or an `ElementRef`, so it works with both `viewChild` forms.
 */
@Component({
  selector: 'app-export-button',
  imports: [Icon],
  template: `
    <button
      class="export-button"
      type="button"
      [disabled]="disabled() || busy() || !resolvedTarget()"
      [attr.aria-busy]="busy()"
      (click)="run()"
    >
      @if (busy()) {
        <span class="export-spinner" aria-hidden="true"></span>
      } @else {
        <app-icon name="download" />
      }
      {{ busy() ? resolvedBusyLabel() : resolvedLabel() }}
    </button>

    <!-- Export is silent when it works, so failure needs announcing. -->
    <p class="sr-only" role="status" aria-live="polite">{{ announcement() }}</p>

    @if (error()) {
      <p class="export-error" role="alert">{{ error() }}</p>
    }
  `,
  styleUrl: './export-button.css',
})
export class ExportButton {
  private readonly pdf = inject(PdfExportService);
  private readonly transloco = inject(TranslocoService);

  /** The region to capture. Omit or pass undefined to keep the button disabled. */
  readonly target = input<ElementRef<HTMLElement> | HTMLElement | undefined>();
  /** File name, without the `.pdf` extension. */
  readonly fileName = input.required<string>();
  /** Printed at the top of the first page. */
  readonly title = input.required<string>();
  /** Second header line — typically the period the data covers. */
  readonly subtitle = input<string | undefined>();
  readonly label = input<string>();
  readonly busyLabel = input<string>();
  readonly disabled = input(false);

  readonly exported = output<void>();
  readonly failed = output<Error>();

  protected readonly busy = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly announcement = signal('');

  protected resolvedLabel(): string {
    return this.label() ?? this.transloco.translate('shared.exportButton.export');
  }

  protected resolvedBusyLabel(): string {
    return this.busyLabel() ?? this.transloco.translate('shared.exportButton.preparing');
  }

  protected resolvedTarget(): HTMLElement | undefined {
    const target = this.target();
    if (!target) {
      return undefined;
    }
    return target instanceof ElementRef ? target.nativeElement : target;
  }

  protected async run(): Promise<void> {
    const element = this.resolvedTarget();
    if (!element || this.busy()) {
      return;
    }

    this.busy.set(true);
    this.error.set(null);
    this.announcement.set(this.transloco.translate('shared.exportButton.preparingAnnouncement'));

    try {
      await this.pdf.export({
        element,
        fileName: this.fileName(),
        title: this.title(),
        subtitle: this.subtitle(),
      });
      this.announcement.set(this.transloco.translate('shared.exportButton.readyAnnouncement'));
      this.exported.emit();
    } catch (cause) {
      const failure = cause instanceof Error ? cause : new Error(this.transloco.translate('shared.exportButton.failedFallback'));
      this.error.set(failure.message);
      this.announcement.set(this.transloco.translate('shared.exportButton.failedAnnouncement', {message: failure.message}));
      this.failed.emit(failure);
    } finally {
      this.busy.set(false);
    }
  }
}
