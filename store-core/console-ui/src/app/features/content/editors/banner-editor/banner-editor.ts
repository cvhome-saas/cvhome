import {Component, computed, effect, inject, input, signal, untracked} from '@angular/core';
import {toSignal} from '@angular/core/rxjs-interop';
import {
  FormControl,
  FormGroup,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {
  BANNER_PLACEMENTS,
  type BannerPlacement,
  type BannerTarget,
  type MediaAsset,
  type PersistableBanner,
  type ReadableBanner,
} from '@models/content';
import {ConsolePermissions} from '@shared/auth/console-permissions';
import {DateTimeField} from '@shared/ui/date-time-field/date-time-field';
import {FormField} from '@shared/ui/form-field/form-field';
import {Icon} from '@shared/ui/icon/icon';
import {Panel} from '@shared/ui/panel/panel';
import {Select, type SelectOption} from '@shared/ui/select/select';
import {TextField} from '@shared/ui/text-field/text-field';
import {Toggle} from '@shared/ui/toggle/toggle';
import {LocaleCopy, type CopyFields} from '../../components/locale-copy/locale-copy';
import {MediaPickerDialog} from '../../components/media-picker/media-picker-dialog';
import {
  PublishChecklist,
  type ChecklistItem,
} from '../../components/publish-checklist/publish-checklist';
import {ScheduleSheet} from '../../components/schedule-sheet/schedule-sheet';
import {ContentEditorFacade} from '../../facades/content-editor.facade';
import type {TranslationForm} from '../../services/content-editor-form.service';
import {ContentHubFacade} from '../../facades/content-hub.facade';
import {EditorShell, type EditorCommand} from '../editor-shell/editor-shell';

/** Every placement but the strip: copy laid over artwork. */
const COPY_ARTWORK: CopyFields = {
  titleKey: 'content.copy.headline',
  subtitleKey: 'content.copy.subtext',
  ctaLabelKey: 'content.copy.buttonLabel',
  altTextKey: 'content.copy.altText',
};

/**
 * The announcement strip. What the shopper reads is the body — rich text, so the message can link
 * out, which is the whole point of an announcement. The title is only the row label in the list, so
 * it says so; it is what the `header-message` box became, and the editor edits the same field the
 * storefront renders.
 */
const COPY_STRIP: CopyFields = {
  titleKey: 'content.copy.bannerLabel',
  titleHintKey: 'content.copy.bannerLabelHint',
  bodyKey: 'content.copy.message',
  richBody: true,
};

const SIZE_HINTS: Readonly<Record<BannerPlacement, string>> = {
  COLLECTION: '1600 × 420',
  STRIP: '',
};

type TargetKind = BannerTarget['kind'];

/**
 * `New Banner.dc.html`: placement cards with the recommended size, artwork from the library (desktop
 * and mobile), per-language headline / subtext / button / alt text, link target, schedule and
 * audience, a live preview and the checklist.
 *
 * "Audience" is only "logged-in visitors": the platform has no customer segments or country
 * targeting. See lessons.md.
 */
@Component({
  selector: 'app-banner-editor',
  imports: [
    DateTimeField,
    EditorShell,
    FormField,
    Icon,
    LocaleCopy,
    MediaPickerDialog,
    Panel,
    PublishChecklist,
    ReactiveFormsModule,
    ScheduleSheet,
    Select,
    TextField,
    Toggle,
    TranslocoDirective,
  ],
  providers: [ContentEditorFacade],
  templateUrl: './banner-editor.html',
  styleUrls: ['../../../../shared/styles/field.css', './banner-editor.css'],
})
export class BannerEditor {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly transloco = inject(TranslocoService);
  private readonly permissions = inject(ConsolePermissions);
  protected readonly hub = inject(ContentHubFacade);
  protected readonly facade =
    inject<ContentEditorFacade<PersistableBanner, ReadableBanner>>(ContentEditorFacade);

  readonly id = input<string>();

  protected readonly placements = BANNER_PLACEMENTS;
  protected readonly deleteOpen = signal(false);
  protected readonly scheduleOpen = signal(false);
  protected readonly scheduleAt = signal('');
  protected readonly picker = signal<'desktop' | 'mobile' | null>(null);
  protected readonly desktopUrl = signal<string | null>(null);
  protected readonly mobileUrl = signal<string | null>(null);
  protected readonly canManage = computed(() => this.permissions.canManageContent());

  protected readonly extra: FormGroup<{
    placement: FormControl<BannerPlacement>;
    startsAt: FormControl<string>;
    endsAt: FormControl<string>;
    targetKind: FormControl<TargetKind>;
    targetValue: FormControl<string>;
    desktopMediaId: FormControl<number | null>;
    mobileMediaId: FormControl<number | null>;
    mobileCrop: FormControl<string>;
    loggedInOnly: FormControl<boolean>;
  }> = this.fb.group({
    placement: this.fb.control<BannerPlacement>('COLLECTION'),
    startsAt: this.fb.control(''),
    endsAt: this.fb.control(''),
    targetKind: this.fb.control<TargetKind>('URL'),
    targetValue: this.fb.control('', {validators: [Validators.maxLength(255)]}),
    desktopMediaId: this.fb.control<number | null>(null),
    mobileMediaId: this.fb.control<number | null>(null),
    mobileCrop: this.fb.control('center'),
    loggedInOnly: this.fb.control(false),
  });

  constructor() {
    effect(() => {
      const raw = this.id();
      untracked(() =>
        this.facade.init('banners', raw ? Number(raw) : null, this.extra, (item) => {
          this.extra.reset({
            placement: item.placement,
            startsAt: item.startsAt ?? '',
            endsAt: item.endsAt ?? '',
            targetKind: item.target?.kind ?? 'URL',
            targetValue: item.target?.value ?? '',
            desktopMediaId: item.artwork?.desktopMediaId ?? null,
            mobileMediaId: item.artwork?.mobileMediaId ?? null,
            mobileCrop: item.artwork?.mobileCrop ?? 'center',
            loggedInOnly: !!item.loggedInOnly,
          });
          this.desktopUrl.set(item.desktopUrl ?? null);
          this.mobileUrl.set(item.mobileUrl ?? null);
        }),
      );
    });
    this.facade.setBodyMapper(() => this.body());
    effect(() => {
      const locales = this.hub.locales();
      this.facade.syncLanguages(locales.codes, locales.defaultCode);
    });
  }

  /** The editor's fields are flat; the wire shape nests target and artwork. */
  protected body(): Partial<PersistableBanner> {
    const v = this.extra.getRawValue();
    return {
      placement: v.placement,
      startsAt: v.startsAt || null,
      endsAt: v.endsAt || null,
      // The strip renders neither artwork nor a link, so it stores neither: leaving stale ids behind
      // would have the media usage index claim the strip still uses an image it never shows.
      target:
        v.placement !== 'STRIP' && v.targetValue.trim()
          ? {kind: v.targetKind, value: v.targetValue.trim()}
          : null,
      artwork:
        v.placement === 'STRIP'
          ? null
          : {
              desktopMediaId: v.desktopMediaId,
              mobileMediaId: v.mobileMediaId,
              mobileCrop: v.mobileCrop || null,
            },
      loggedInOnly: v.loggedInOnly,
    };
  }

  protected readonly heading = computed(() => {
    this.transloco.activeLang();
    return this.facade.isNew()
      ? this.transloco.translate('content.banner.newTitle')
      : this.facade.title() || this.transloco.translate('content.banner.editTitle');
  });

  /**
   * The chosen placement as a signal. Reading `extra.controls.placement.value` inside a `computed`
   * would cache the first answer forever — a form control is not reactive — so what depends on the
   * placement (the copy fields, the artwork panel, the preview) tracks its `valueChanges` instead.
   */
  private readonly placement = toSignal(this.extra.controls.placement.valueChanges, {
    initialValue: this.extra.controls.placement.value,
  });

  protected readonly sizeHint = computed(() => SIZE_HINTS[this.placement()]);
  protected readonly isStrip = computed(() => this.placement() === 'STRIP');
  protected readonly copyFields = computed<CopyFields>(() =>
    this.isStrip() ? COPY_STRIP : COPY_ARTWORK,
  );

  protected readonly targetKinds = computed<readonly SelectOption[]>(() => {
    this.transloco.activeLang();
    return (['COLLECTION', 'PRODUCT', 'PAGE', 'URL'] as TargetKind[]).map((kind) => ({
      value: kind,
      label: this.transloco.translate(`content.banner.targetKind.${kind}`),
    }));
  });

  /** Typed as optional on purpose: before the store's languages land there is no group to bind. */
  protected readonly activeTranslation = computed<TranslationForm | undefined>(
    () =>
      this.facade.translationFor(this.facade.language()) ??
      Object.values(this.facade.translations())[0],
  );

  protected readonly checklist = computed<readonly ChecklistItem[]>(() => {
    this.transloco.activeLang();
    this.facade.written();
    const source = this.facade.sourceTranslation(this.hub.locales().defaultCode);
    const missing = this.hub.locales().codes.filter((code) => !this.facade.written().has(code));
    const translations: ChecklistItem = {
      key: 'translations',
      label: missing.length
        ? this.transloco.translate('content.checklist.translationsMissing', {
            languages: missing.map((c) => c.toUpperCase()).join(', '),
          })
        : this.transloco.translate('content.checklist.translationsDone'),
      ok: missing.length === 0,
      soft: true,
    };
    const placement: ChecklistItem = {
      key: 'placement',
      label: this.transloco.translate('content.checklist.placement'),
      ok: true,
    };

    // The strip has no artwork, no headline over it and no link target of its own: the one thing it
    // must have is the message, which is also what the server's publish gate asks for.
    if (this.isStrip()) {
      // A strip locale counts as written when it has a message, not a label — the label is console-only.
      const unwritten = Object.entries(this.facade.translations())
        .filter(([, form]) => form.controls.body.value.trim().length === 0)
        .map(([code]) => code)
        .filter((code) => this.hub.locales().codes.includes(code));
      return [
        placement,
        {
          key: 'message',
          label: this.transloco.translate('content.checklist.message'),
          ok: (source?.controls.body.value.trim().length ?? 0) > 0,
        },
        {
          ...translations,
          label: unwritten.length
            ? this.transloco.translate('content.checklist.translationsMissing', {
                languages: unwritten.map((c) => c.toUpperCase()).join(', '),
              })
            : this.transloco.translate('content.checklist.translationsDone'),
          ok: unwritten.length === 0,
        },
      ];
    }

    const headline = !!source && source.controls.title.value.trim().length > 0;
    const target = this.extra.controls.targetValue.value.trim().length > 0;
    const artwork = this.extra.controls.desktopMediaId.value !== null;
    const alt = !artwork || (source?.controls.altText.value.trim().length ?? 0) > 0;
    return [
      placement,
      {
        key: 'copy',
        label: this.transloco.translate('content.checklist.headlineAndTarget'),
        ok: headline && target,
      },
      {key: 'artwork', label: this.transloco.translate('content.checklist.artwork'), ok: artwork},
      {key: 'alt', label: this.transloco.translate('content.checklist.altText'), ok: alt},
      translations,
    ];
  });

  protected placementLabel(placement: BannerPlacement): string {
    return this.transloco.translate(`content.banner.placement.${placement}.label`);
  }

  protected placementHint(placement: BannerPlacement): string {
    return this.transloco.translate(`content.banner.placement.${placement}.hint`);
  }

  protected pickPlacement(placement: BannerPlacement): void {
    this.extra.controls.placement.setValue(placement);
    this.extra.controls.placement.markAsDirty();
  }

  protected onArtwork(asset: MediaAsset): void {
    const which = this.picker();
    if (which === 'desktop') {
      this.extra.controls.desktopMediaId.setValue(asset.id);
      this.extra.controls.desktopMediaId.markAsDirty();
      this.desktopUrl.set(asset.url);
    } else if (which === 'mobile') {
      this.extra.controls.mobileMediaId.setValue(asset.id);
      this.extra.controls.mobileMediaId.markAsDirty();
      this.mobileUrl.set(asset.url);
    }
  }

  protected clearArtwork(which: 'desktop' | 'mobile'): void {
    const control =
      which === 'desktop' ? this.extra.controls.desktopMediaId : this.extra.controls.mobileMediaId;
    control.setValue(null);
    control.markAsDirty();
    (which === 'desktop' ? this.desktopUrl : this.mobileUrl).set(null);
  }

  protected onTargetKind(value: string): void {
    this.extra.controls.targetKind.setValue(value as TargetKind);
    this.extra.controls.targetKind.markAsDirty();
  }

  protected onCommand(command: string): void {
    switch (command as EditorCommand) {
      case 'schedule':
        this.scheduleAt.set('');
        this.scheduleOpen.set(true);
        return;
      case 'delete':
        this.deleteOpen.set(true);
        return;
      default:
        this.facade.transition(command as Exclude<EditorCommand, 'schedule' | 'delete'>);
    }
  }

  protected confirmSchedule(at: string): void {
    if (at) {
      this.facade.transition('publish', at);
    }
  }
}
