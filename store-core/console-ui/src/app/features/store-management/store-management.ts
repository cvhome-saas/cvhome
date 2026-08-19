import {Component, computed, effect, inject, input} from '@angular/core';
import {Router} from '@angular/router';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {BusyOverlay} from '@shared/ui/busy-overlay/busy-overlay';
import {Icon} from '@shared/ui/icon/icon';
import {PageHeader} from '@shared/ui/page-header/page-header';
import {TabSwitcher, type TabItem} from '@shared/ui/tab-switcher/tab-switcher';
import {ToastService} from '@shared/ui/toast/toast';
import type {SettingsSectionKey} from '@models/store-settings';
import {BrandingSection} from './components/branding-section/branding-section';
import {DetailsSection} from './components/details-section/details-section';
import {DomainSection} from './components/domain-section/domain-section';
import {HomeSection} from './components/home-section/home-section';
import {PaymentsSection} from './components/payments-section/payments-section';
import {SettingsNav} from './components/settings-nav/settings-nav';
import {SliderSection} from './components/slider-section/slider-section';
import {SocialLinksSection} from './components/social-links-section/social-links-section';
import {SocialLoginSection} from './components/social-login-section/social-login-section';
import {StoreSettingsFacade} from './facades/store-settings.facade';

/**
 * The store's settings surface.
 *
 * Renders into `ConsoleShell`, which owns the banner, navigation rail and toolbar, so this
 * component is only its own content — a page header, the settings rail, and the one section
 * the route names.
 *
 * The section is a route param rather than component state, so a card is linkable, survives a
 * reload, and answers the back button. `withComponentInputBinding()` is on, so the param
 * arrives as an input.
 */
@Component({
  selector: 'app-store-management',
  imports: [
    BrandingSection,
    BusyOverlay,
    DetailsSection,
    DomainSection,
    HomeSection,
    Icon,
    PageHeader,
    PaymentsSection,
    SettingsNav,
    SliderSection,
    SocialLinksSection,
    SocialLoginSection,
    TabSwitcher,
    TranslocoDirective,
  ],
  templateUrl: './store-management.html',
  styleUrl: './store-management.css',
})
export class StoreManagement {
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);

  protected readonly facade = inject(StoreSettingsFacade);

  /** The `:section` route param. Bound by the router, not read from `ActivatedRoute`. */
  readonly section = input.required<SettingsSectionKey>();

  protected readonly isLoading = this.facade.isLoading;
  protected readonly isEmpty = this.facade.isEmpty;
  protected readonly error = this.facade.error;

  protected readonly settings = this.facade.settings;
  protected readonly sections = this.facade.sections;
  protected readonly context = this.facade.context;
  protected readonly canSave = this.facade.canSave;
  protected readonly isDirty = this.facade.isDirty;
  protected readonly isSaving = this.facade.isSaving;

  protected readonly forms = this.facade.form.controls;
  protected readonly languages = this.facade.languages;
  protected readonly activeLanguage = this.facade.activeLanguage;

  protected readonly domainStatus = this.facade.domainStatus;
  protected readonly customDomains = this.facade.customDomains;
  protected readonly customDomainRecord = this.facade.customDomainRecord;
  protected readonly subdomain = this.facade.subdomain;

  /**
   * The rail's sections as a tab track, for the narrow layout.
   *
   * Both are rendered and CSS decides which is visible, because a container query cannot be
   * read from TypeScript — and the page's width, not the viewport's, is what decides.
   */
  protected readonly tabs = computed<readonly TabItem[]>(() => {
    this.transloco.activeLang();
    return this.sections().map((section) => ({
      key: section.key,
      label: this.transloco.translate(section.shortLabelKey),
      badge: section.attention ? '!' : undefined,
      badgeTone: 'amber' as const,
    }));
  });

  constructor() {
    // The route is the source of truth; the facade mirrors it so the header can act on it.
    effect(() => this.facade.activeSection.set(this.section()));
  }

  protected pickSection(key: string): void {
    void this.router.navigate(['/store-management', key]);
  }

  protected previewStorefront(): void {
    this.toast.info(this.transloco.translate('storeSettings.previewNotAvailable'));
  }
}
