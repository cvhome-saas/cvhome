import {Component, computed, effect, inject, input} from '@angular/core';
import {Router} from '@angular/router';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {BusyOverlay} from '@shared/ui/busy-overlay/busy-overlay';
import {Icon} from '@shared/ui/icon/icon';
import {PageHeader} from '@shared/ui/page-header/page-header';
import {Panel} from '@shared/ui/panel/panel';
import {ProgressTrack} from '@shared/ui/progress-track/progress-track';
import {Stepper} from '@shared/ui/stepper/stepper';
import {isProductStep, type ProductStep} from '@models/products';
import {EssentialsStep} from './components/essentials-step/essentials-step';
import {MediaStep} from './components/media-step/media-step';
import {OrganizeStep} from './components/organize-step/organize-step';
import {PricingStep} from './components/pricing-step/pricing-step';
import {ProductFormFacade} from './facades/product-form.facade';

/**
 * The product wizard: four steps, a readiness checklist and a translations panel.
 *
 * Replaces seller-ui's two-column form plus four router sub-tabs. The step is *not* in the URL,
 * unlike the catalogue's tab: a step is a position inside one editing session, and making it
 * linkable would invite an operator to bookmark step 3 of a product that has since been saved
 * twice. The product id is in the URL, which is the thing worth linking to.
 *
 * **`ProductFormFacade` is provided here, not in root.** Leaving `/products/new` and coming back
 * gives a genuinely new form rather than the last product's half-typed one.
 */
@Component({
  selector: 'app-product-form',
  imports: [
    BusyOverlay,
    EssentialsStep,
    Icon,
    MediaStep,
    OrganizeStep,
    PageHeader,
    Panel,
    PricingStep,
    ProgressTrack,
    Stepper,
    TranslocoDirective,
  ],
  providers: [ProductFormFacade],
  templateUrl: './product-form.html',
  styleUrl: './product-form.css',
})
export class ProductForm {
  private readonly router = inject(Router);
  private readonly transloco = inject(TranslocoService);

  protected readonly facade = inject(ProductFormFacade);

  /** The `:id` route param, or `undefined` on `/products/new`. */
  readonly id = input<string>();

  constructor() {
    /*
     * The route param, validated before it reaches a facade — Module 4's `/orders/abc` finding.
     * A non-numeric id is not a product, and sending it to the API would produce a 400 dressed up
     * as a page error rather than the not-found the URL actually describes.
     */
    effect(() => {
      const raw = this.id();
      if (raw === undefined) {
        this.facade.productId.set(null);
        this.facade.routeSettled.set(true);
        return;
      }
      const parsed = Number(raw);
      if (!Number.isInteger(parsed) || parsed <= 0) {
        this.router.navigate(['/products'], {replaceUrl: true});
        return;
      }
      this.facade.productId.set(parsed);
      /*
       * Set *after* the id, and never before: until this flips, the snapshot resource does not run.
       * Without it the resource fired once on construction with `id: null` and no store, again when
       * this effect landed, and a third time when the store directory resolved — three rounds of six
       * requests, two of them cancelled mid-flight, for one page.
       */
      this.facade.routeSettled.set(true);
    });

    // Filling a form is a write, so it cannot live in a `computed`. The facade no-ops once the
    // form matches the product on screen.
    effect(() => {
      this.facade.syncForm();
    });
  }

  protected readonly isNew = computed(() => this.facade.productId() === null);

  protected readonly title = computed(() => {
    this.transloco.activeLang();
    if (this.isNew()) {
      return this.transloco.translate('productForm.newTitle');
    }
    const name = this.facade
      .draft()
      .copy.find((copy) => copy.name.trim() !== '')?.name;
    return name ?? this.transloco.translate('productForm.title');
  });

  protected readonly context = computed(() => {
    this.transloco.activeLang();
    const draft = this.facade.draft();
    return this.transloco.translate(
      draft.visible ? 'productForm.contextLive' : 'productForm.contextDraft',
      {sku: draft.sku || '—', percent: this.facade.readyPercent()},
    );
  });

  protected onStep(key: string): void {
    if (isProductStep(key)) {
      this.facade.activeStep.set(key as ProductStep);
    }
  }

  protected cancel(): void {
    this.router.navigate(['/products']);
  }
}
