import {inject, Injectable} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import {LocaleService} from '@core/i18n/locale.service';
import {THEME} from '@core/theme/theme.provider';

export interface PdfExportRequest {
  /** The element to capture. */
  readonly element: HTMLElement;
  /** File name, without the `.pdf` extension. */
  readonly fileName: string;
  /** Printed at the top of the first page. */
  readonly title: string;
  /** Second line of the header — typically the period the data covers. */
  readonly subtitle?: string;
  /**
   * How the page is built around the capture.
   *
   * `report` (the default) is a landscape A4 with the console's own title band and a footer, for
   * a screen region — a dashboard, a table — being turned into a document.
   *
   * `document` is for a region that **already is** a document: an invoice laid out at page
   * proportions. It goes on a portrait A4, whole, scaled to fit one page, with no header, footer
   * or page numbers — the sheet has its own letterhead, and a second one above it would be the
   * console talking over the seller.
   */
  readonly layout?: 'report' | 'document';
}

/** A4 in points. A report is laid out landscape, a document portrait. */
const A4_LONG = 841.89;
const A4_SHORT = 595.28;

/** The report layout's page, which the header, footer and slicing are all written against. */
const PAGE_WIDTH = A4_LONG;
const PAGE_HEIGHT = A4_SHORT;
const MARGIN = 36;
const HEADER_HEIGHT = 58;
const FOOTER_HEIGHT = 24;

/** 2x keeps text crisp without producing an unreasonably large file. */
const CAPTURE_SCALE = 2;

/**
 * A safe stand-in for `--font-sans`, dropping the two variable-font entries.
 *
 * `modern-screenshot` embeds every `@font-face` the capture touches as base64 inside the
 * generated SVG, and Arabic content pulls in `Noto Sans Arabic Variable` — three
 * `unicode-range`-subsetted variable-axis `@font-face` blocks from
 * `@fontsource-variable/noto-sans-arabic`. Something in that specific shape breaks the
 * library's embedding silently: no error, no console warning, just a fully blank capture,
 * confirmed by capturing the same element with only the font swapped. The static fallbacks
 * later in the stack need no embedding at all — the browser's own generic `sans-serif`
 * resolves to a system Arabic font for Arabic glyphs, which is what this relies on.
 */
const CAPTURE_FONT_STACK = "'Inter', ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif";

/** A rendered line of header/footer text, sized and ready for `pdf.addImage`. */
interface TextImage {
  readonly url: string;
  readonly widthPt: number;
  readonly heightPt: number;
}

/** One widget's vertical extent within the captured region, in PDF points. */
interface Block {
  readonly top: number;
  readonly bottom: number;
}

/**
 * Renders a region of the page to a PDF.
 *
 * Capture goes through `modern-screenshot`, which draws the region into an SVG
 * `foreignObject` and lets the browser rasterise it. That matters here: html2canvas
 * reimplements CSS itself and throws on any colour function it does not recognise, and
 * Tailwind v4 emits `oklab()` — in the preflight `::placeholder` rule among others — so
 * exporting failed outright with "unsupported color function". Delegating to the browser
 * means anything it can render, we can capture.
 *
 * The header, footer and page numbers are text jsPDF draws itself rather than part of that
 * capture, and jsPDF's built-in fonts are WinAnsi standard fonts — they have no Arabic
 * glyphs. Asking them to draw Arabic doesn't fail, it silently reinterprets the string
 * through the wrong encoding and prints mojibake. Rather than embedding a font (jsPDF only
 * accepts TrueType, and the Arabic font this app ships is WOFF2), every line of header and
 * footer text is rendered to a small canvas with the browser's own text engine — the same
 * approach the body capture already relies on — and placed with `addImage`. That makes it
 * exactly as capable as the page itself: whatever the browser can shape, the PDF can show.
 *
 * Page breaks land between widgets rather than through one: a raw pixel-height cut has no
 * notion of where a card ends, and a KPI tile or panel sliced across two pages reads as
 * broken output, not a page break. `blocksOf` measures each widget's real position before
 * capture and `pageBreakFor` backs the cut off to the nearest gap.
 *
 * `jspdf` and the capture library are only needed when someone actually exports, so they
 * are imported dynamically on first use rather than bundled into the page that offers the
 * button. That also keeps them off the server: the import only ever runs from a click.
 */
@Injectable({providedIn: 'root'})
export class PdfExportService {
  private readonly theme = inject(THEME);
  private readonly locale = inject(LocaleService);
  private readonly transloco = inject(TranslocoService);
  private readonly localeFormat = inject(TranslocoLocaleService);

  async export(request: PdfExportRequest): Promise<void> {
    const [{domToCanvas}, {jsPDF}] = await Promise.all([
      import('modern-screenshot'),
      import('jspdf'),
    ]);

    const asDocument = request.layout === 'document';
    // A document paints its own paper; a report sits on the console's background.
    const background = asDocument ? '#ffffff' : this.theme.color('--background');
    const rtl = this.locale.currentLocale().dir === 'rtl';

    if (asDocument) {
      const canvas = await domToCanvas(request.element, {
        backgroundColor: background,
        scale: CAPTURE_SCALE,
        onCloneNode: (cloned) => {
          this.settleAnimations(cloned);
          this.stripVariableFonts(cloned);
        },
      });

      const pdf = new jsPDF({orientation: 'portrait', unit: 'pt', format: 'a4'});
      // Fit the whole sheet on one page: it was drawn as a page, so a page is what it should be.
      // Slicing it across two — which the report path would do, since the sheet is far taller than
      // a landscape body — is what made the download stop matching what the screen showed.
      const scale = Math.min(
        (A4_SHORT - MARGIN * 2) / canvas.width,
        (A4_LONG - MARGIN * 2) / canvas.height,
      );
      const width = canvas.width * scale;
      const height = canvas.height * scale;

      pdf.setFillColor(background);
      pdf.rect(0, 0, A4_SHORT, A4_LONG, 'F');
      pdf.addImage(
        canvas.toDataURL('image/png'),
        'PNG',
        (A4_SHORT - width) / 2,
        MARGIN,
        width,
        height,
        undefined,
        'FAST',
      );
      pdf.save(`${request.fileName}.pdf`);
      return;
    }

    const contentWidth = PAGE_WIDTH - MARGIN * 2;
    // Measured on the live element, before capture — the clone `domToCanvas` renders from
    // is torn down again once it resolves, so this is the only point layout is readable.
    const blocks = this.blocksOf(request.element, contentWidth);

    const canvas = await domToCanvas(request.element, {
      backgroundColor: background,
      scale: CAPTURE_SCALE,
      onCloneNode: (cloned) => {
        this.settleAnimations(cloned);
        this.stripVariableFonts(cloned);
      },
    });

    const pdf = new jsPDF({orientation: 'landscape', unit: 'pt', format: 'a4'});

    const scale = contentWidth / canvas.width;
    const totalHeight = canvas.height * scale;

    // Bottom of the printable area, above the footer line.
    const bodyBottom = PAGE_HEIGHT - MARGIN - FOOTER_HEIGHT;
    const firstPageBody = bodyBottom - (MARGIN + HEADER_HEIGHT);
    const laterPageBody = bodyBottom - MARGIN;

    let renderedHeight = 0;
    let page = 0;

    while (renderedHeight < totalHeight) {
      const isFirst = page === 0;
      if (!isFirst) {
        pdf.addPage();
      }

      this.paintBackground(pdf, background);

      const bodyTop = isFirst ? MARGIN + HEADER_HEIGHT : MARGIN;
      const bodyHeight = isFirst ? firstPageBody : laterPageBody;

      if (isFirst) {
        this.paintHeader(pdf, request, rtl);
      }

      const maxReach = Math.min(renderedHeight + bodyHeight, totalHeight);
      const cutAt = this.pageBreakFor(blocks, renderedHeight, maxReach);
      const sliceHeight = cutAt - renderedHeight;

      const slice = this.sliceOf(canvas, renderedHeight / scale, sliceHeight / scale, background);

      pdf.addImage(slice, 'PNG', MARGIN, bodyTop, contentWidth, sliceHeight, undefined, 'FAST');

      renderedHeight += sliceHeight;
      page += 1;
    }

    this.paintFooters(pdf, rtl);
    pdf.save(`${request.fileName}.pdf`);
  }

  /**
   * The vertical extent of every widget in the captured region, in PDF points.
   *
   * Descends one level into `.panel-row` — the dashboard's and orders page's side-by-side
   * pairs — so a break can land between two short panels sharing a row, not only between
   * rows. Everything else is treated as one atomic block: splitting a chart or a table
   * mid-height is exactly what this exists to avoid.
   */
  private blocksOf(root: HTMLElement, contentWidth: number): readonly Block[] {
    const rootRect = root.getBoundingClientRect();
    const toPt = rootRect.width > 0 ? contentWidth / rootRect.width : 1;
    const blocks: Block[] = [];

    const collect = (container: Element) => {
      for (const child of Array.from(container.children)) {
        if (!(child instanceof HTMLElement)) {
          continue;
        }
        if (child.classList.contains('panel-row')) {
          collect(child);
          continue;
        }
        const rect = child.getBoundingClientRect();
        if (rect.height === 0) {
          continue;
        }
        blocks.push({
          top: (rect.top - rootRect.top) * toPt,
          bottom: (rect.bottom - rootRect.top) * toPt,
        });
      }
    };
    collect(root);
    return blocks;
  }

  /**
   * Where a page should actually end.
   *
   * `maxReach` is how far the page could go by raw capacity. If that line falls inside a
   * block, and the block has room to move to the next page whole, the break backs off to
   * the block's top instead. A block taller than one page's capacity has nowhere to move
   * to — that one still gets cut, the same way the whole page did before this existed.
   */
  private pageBreakFor(blocks: readonly Block[], renderedHeight: number, maxReach: number): number {
    for (const block of blocks) {
      if (block.top < maxReach && block.bottom > maxReach) {
        if (block.top > renderedHeight + 0.5) {
          return block.top;
        }
        break;
      }
    }
    return maxReach;
  }

  /**
   * Freezes animations in the clone the capture renders from.
   *
   * The clone is a fresh subtree, so CSS animations restart at frame zero. Our entrance
   * animations begin at `opacity: 0` with `animation-fill-mode: both` and a delay, which
   * meant every widget was captured mid-delay — fully transparent, producing a page that
   * was correctly sized and completely empty. Clearing animation and transition drops the
   * backwards fill and leaves each element in its resting state.
   */
  private settleAnimations(cloned: Node): void {
    if (!(cloned instanceof HTMLElement)) {
      return;
    }
    // The hook receives the cloned root, so walk the subtree it owns.
    for (const element of [cloned, ...cloned.querySelectorAll<HTMLElement>('*')]) {
      element.style.setProperty('animation', 'none', 'important');
      element.style.setProperty('transition', 'none', 'important');
    }
  }

  /** See `CAPTURE_FONT_STACK` — swaps out the variable fonts that break the capture. */
  private stripVariableFonts(cloned: Node): void {
    if (!(cloned instanceof HTMLElement)) {
      return;
    }
    for (const element of [cloned, ...cloned.querySelectorAll<HTMLElement>('*')]) {
      element.style.setProperty('font-family', CAPTURE_FONT_STACK, 'important');
    }
  }

  /** Cuts a horizontal band out of the capture so tall regions can span pages. */
  private sliceOf(
    source: HTMLCanvasElement,
    top: number,
    height: number,
    background: string,
  ): string {
    const slice = document.createElement('canvas');
    slice.width = source.width;
    slice.height = Math.max(1, Math.round(height));

    const context = slice.getContext('2d');
    if (!context) {
      throw new Error(this.transloco.translate('shared.exportButton.canvasUnavailable'));
    }

    context.fillStyle = background;
    context.fillRect(0, 0, slice.width, slice.height);
    context.drawImage(source, 0, Math.round(top), source.width, slice.height, 0, 0, source.width, slice.height);

    return slice.toDataURL('image/png');
  }

  /**
   * Rasterises one line of text with the browser's own font shaping — the UI's actual
   * `--font-sans` stack, which is where the bundled Arabic weights live — rather than
   * jsPDF's built-in Latin-only fonts.
   *
   * `shapeRtl` controls the canvas's bidi paragraph direction and must reflect what the
   * *text itself* is, not which side of the page it lands on: a pure-digit string like a
   * page count has no strong-direction characters, and shaping it as RTL lets the
   * paragraph's own direction reorder those neutral characters — "1 / 2" becomes "2 / 1"
   * with nothing else about it changed. Anchoring which margin the line sits against is a
   * separate decision, made by the caller.
   */
  private textImage(text: string, fontSizePt: number, color: string, bold: boolean, shapeRtl: boolean): TextImage {
    const fontPx = fontSizePt * CAPTURE_SCALE;
    const fontFamily = this.theme('--font-sans');
    const font = `${bold ? 700 : 400} ${fontPx}px ${fontFamily}`;

    const probe = document.createElement('canvas').getContext('2d');
    if (!probe) {
      throw new Error(this.transloco.translate('shared.exportButton.canvasUnavailable'));
    }
    probe.font = font;
    const metrics = probe.measureText(text);
    const ascent = metrics.actualBoundingBoxAscent || fontPx * 0.8;
    const descent = metrics.actualBoundingBoxDescent || fontPx * 0.25;
    const widthPx = Math.max(1, Math.ceil(metrics.width) + 4);
    const heightPx = Math.max(1, Math.ceil(ascent + descent) + 4);

    const canvas = document.createElement('canvas');
    canvas.width = widthPx;
    canvas.height = heightPx;
    const context = canvas.getContext('2d');
    if (!context) {
      throw new Error(this.transloco.translate('shared.exportButton.canvasUnavailable'));
    }
    context.font = font;
    context.direction = shapeRtl ? 'rtl' : 'ltr';
    context.textAlign = shapeRtl ? 'right' : 'left';
    context.textBaseline = 'alphabetic';
    context.fillStyle = color;
    context.fillText(text, shapeRtl ? widthPx - 2 : 2, ascent + 2);

    return {
      url: canvas.toDataURL('image/png'),
      widthPt: widthPx / CAPTURE_SCALE,
      heightPt: heightPx / CAPTURE_SCALE,
    };
  }

  /** Draws a rendered text line, anchored to the given margin. */
  private drawText(pdf: import('jspdf').jsPDF, image: TextImage, baselineY: number, anchorRight: boolean): void {
    const x = anchorRight ? PAGE_WIDTH - MARGIN - image.widthPt : MARGIN;
    pdf.addImage(image.url, 'PNG', x, baselineY - image.heightPt + 2, image.widthPt, image.heightPt);
  }

  private paintBackground(pdf: import('jspdf').jsPDF, background: string): void {
    pdf.setFillColor(background);
    pdf.rect(0, 0, PAGE_WIDTH, PAGE_HEIGHT, 'F');
  }

  private paintHeader(pdf: import('jspdf').jsPDF, request: PdfExportRequest, rtl: boolean): void {
    const titleColor = this.theme.color('--foreground-strong');
    this.drawText(pdf, this.textImage(request.title, 18, titleColor, true, rtl), MARGIN + 18, rtl);

    if (request.subtitle) {
      const subtitleColor = this.theme.color('--muted-foreground');
      this.drawText(pdf, this.textImage(request.subtitle, 10, subtitleColor, false, rtl), MARGIN + 36, rtl);
    }

    pdf.setDrawColor(this.theme.color('--primary'));
    pdf.setLineWidth(1);
    pdf.line(MARGIN, MARGIN + 46, PAGE_WIDTH - MARGIN, MARGIN + 46);
  }

  private paintFooters(pdf: import('jspdf').jsPDF, rtl: boolean): void {
    const pages = pdf.getNumberOfPages();
    const stamp = this.localeFormat.localizeDate(new Date(), undefined, {dateStyle: 'medium', timeStyle: 'short'});
    const generatedLine = this.transloco.translate('shared.export.generatedAt', {date: stamp});
    const pageLineTemplate = 'shared.export.pageOfPages';
    const color = this.theme.color('--foreground-quiet');

    for (let page = 1; page <= pages; page++) {
      pdf.setPage(page);
      // The stamp sits at the reading-direction start, the page count at the end — the
      // same two corners `pdf.text`'s `align: 'right'` used to pin them to, just resolved
      // per direction instead of hardcoded to the left. The page count is pure digits, so
      // it always shapes left-to-right (see `textImage`'s `shapeRtl` note) even while its
      // anchor still flips with the document direction.
      this.drawText(pdf, this.textImage(generatedLine, 8, color, false, rtl), PAGE_HEIGHT - MARGIN / 2, rtl);
      const pageLine = this.transloco.translate(pageLineTemplate, {page, pages});
      this.drawText(pdf, this.textImage(pageLine, 8, color, false, false), PAGE_HEIGHT - MARGIN / 2, !rtl);
    }
  }
}
