import {Component, computed, effect, inject, input, signal, untracked} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {
  FormControl,
  FormGroup,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import {PoliciesService} from '@api/content/policies.service';
import {ApiErrorService} from '@core/errors/api-error.service';
import {
  POLICY_TYPES,
  type PersistablePolicy,
  type PolicyType,
  type ReadablePolicy,
  type ReadablePolicyVersion,
} from '@models/content';
import {ConsolePermissions} from '@shared/auth/console-permissions';
import {Badge} from '@shared/ui/badge/badge';
import {DateTimeField} from '@shared/ui/date-time-field/date-time-field';
import {FormField} from '@shared/ui/form-field/form-field';
import {Icon} from '@shared/ui/icon/icon';
import {Panel} from '@shared/ui/panel/panel';
import {TextField} from '@shared/ui/text-field/text-field';
import {ToastService} from '@shared/ui/toast/toast';
import {Toggle} from '@shared/ui/toggle/toggle';
import {LocaleCopy, type CopyFields} from '../../components/locale-copy/locale-copy';
import {
  PublishChecklist,
  type ChecklistItem,
} from '../../components/publish-checklist/publish-checklist';
import {ScheduleSheet} from '../../components/schedule-sheet/schedule-sheet';
import {ContentEditorFacade} from '../../facades/content-editor.facade';
import {ContentHubFacade} from '../../facades/content-hub.facade';
import {EditorShell, type EditorCommand} from '../editor-shell/editor-shell';

const COPY: CopyFields = {
  titleKey: 'content.copy.heading',
  bodyKey: 'content.copy.policyBody',
  richBody: true,
};

/**
 * `New Policy.dc.html`: type cards with "Insert template", heading and body per language, where it
 * applies (jurisdiction, effective date, acceptance and notification toggles, display flags), the
 * version history (restore an old version's text onto the draft) and the checkout preview.
 * Publishing cuts an immutable version; the storefront and checkout read the live one.
 */
@Component({
  selector: 'app-policy-editor',
  imports: [
    Badge,
    DateTimeField,
    EditorShell,
    FormField,
    Icon,
    LocaleCopy,
    Panel,
    PublishChecklist,
    ReactiveFormsModule,
    ScheduleSheet,
    TextField,
    Toggle,
    TranslocoDirective,
  ],
  providers: [ContentEditorFacade],
  templateUrl: './policy-editor.html',
  styleUrls: ['../../../../shared/styles/field.css', './policy-editor.css'],
})
export class PolicyEditor {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly transloco = inject(TranslocoService);
  private readonly localeFormat = inject(TranslocoLocaleService);
  private readonly policiesApi = inject(PoliciesService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly permissions = inject(ConsolePermissions);
  private readonly route = inject(ActivatedRoute);
  protected readonly hub = inject(ContentHubFacade);
  protected readonly facade =
    inject<ContentEditorFacade<PersistablePolicy, ReadablePolicy>>(ContentEditorFacade);

  readonly id = input<string>();

  protected readonly types = POLICY_TYPES;
  protected readonly copyFields = COPY;
  protected readonly deleteOpen = signal(false);
  protected readonly scheduleOpen = signal(false);
  protected readonly scheduleAt = signal('');
  protected readonly templateBusy = signal(false);
  protected readonly canManage = computed(() => this.permissions.canManageContent());

  protected readonly extra: FormGroup<{
    policyType: FormControl<PolicyType>;
    jurisdiction: FormControl<string>;
    effectiveFrom: FormControl<string>;
    requiresAcceptance: FormControl<boolean>;
    notifyCustomers: FormControl<boolean>;
    showInFooter: FormControl<boolean>;
    showAtCheckout: FormControl<boolean>;
    showAtSignup: FormControl<boolean>;
  }> = this.fb.group({
    policyType: this.fb.control<PolicyType>('TERMS'),
    jurisdiction: this.fb.control('', {validators: [Validators.maxLength(60)]}),
    effectiveFrom: this.fb.control(''),
    requiresAcceptance: this.fb.control(false),
    notifyCustomers: this.fb.control(false),
    showInFooter: this.fb.control(true),
    showAtCheckout: this.fb.control(false),
    showAtSignup: this.fb.control(false),
  });

  constructor() {
    effect(() => {
      const raw = this.id();
      untracked(() =>
        this.facade.init('policies', raw ? Number(raw) : null, this.extra, (item) => {
          this.extra.reset({
            policyType: item.policyType,
            jurisdiction: item.jurisdiction ?? '',
            effectiveFrom: item.effectiveFrom ?? '',
            requiresAcceptance: !!item.requiresAcceptance,
            notifyCustomers: !!item.notifyCustomers,
            showInFooter: item.showInFooter !== false,
            showAtCheckout: !!item.showAtCheckout,
            showAtSignup: !!item.showAtSignup,
          });
        }),
      );
      const wanted = this.route.snapshot.queryParamMap.get('type') as PolicyType | null;
      if (!raw && wanted && (POLICY_TYPES as readonly string[]).includes(wanted)) {
        this.pickType(wanted);
      }
    });
    this.facade.setBodyMapper(() => {
      const v = this.extra.getRawValue();
      return {
        ...v,
        effectiveFrom: v.effectiveFrom || null,
        jurisdiction: v.jurisdiction.trim() || null,
      };
    });
    effect(() => {
      const locales = this.hub.locales();
      this.facade.syncLanguages(locales.codes, locales.defaultCode);
    });
  }

  protected readonly heading = computed(() => {
    this.transloco.activeLang();
    return this.facade.isNew()
      ? this.transloco.translate('content.policy.newTitle')
      : this.facade.title() || this.transloco.translate('content.policy.editTitle');
  });

  protected readonly versions = computed<readonly ReadablePolicyVersion[]>(
    () => this.facade.item()?.versions ?? [],
  );
  protected readonly liveVersion = computed(() => this.facade.item()?.liveVersion ?? 0);

  protected readonly activeTranslation = computed(
    () =>
      this.facade.translationFor(this.facade.language()) ??
      Object.values(this.facade.translations())[0],
  );

  protected readonly checklist = computed<readonly ChecklistItem[]>(() => {
    this.transloco.activeLang();
    this.facade.written();
    const source = this.facade.sourceTranslation(this.hub.locales().defaultCode);
    const text =
      !!source &&
      source.controls.title.value.trim().length > 0 &&
      source.controls.body.value.trim().length > 0;
    const missing = this.hub.locales().codes.filter((code) => !this.facade.written().has(code));
    return [
      {key: 'type', label: this.transloco.translate('content.checklist.policyType'), ok: true},
      {
        key: 'text',
        label: this.transloco.translate('content.checklist.headingAndBody'),
        ok: text,
      },
      {
        key: 'effective',
        label: this.transloco.translate('content.checklist.effectiveDate'),
        ok: !!this.extra.controls.effectiveFrom.value,
        soft: true,
      },
      {
        key: 'translations',
        label: missing.length
          ? this.transloco.translate('content.checklist.legalReview', {
              languages: missing.map((c) => c.toUpperCase()).join(', '),
            })
          : this.transloco.translate('content.checklist.translationsDone'),
        ok: missing.length === 0,
        soft: true,
      },
    ];
  });

  protected typeLabel(type: PolicyType): string {
    return this.transloco.translate(`content.policy.type.${type}.label`);
  }

  protected typeHint(type: PolicyType): string {
    return this.transloco.translate(`content.policy.type.${type}.hint`);
  }

  protected pickType(type: PolicyType): void {
    this.extra.controls.policyType.setValue(type);
    this.extra.controls.policyType.markAsDirty();
    if (!this.facade.common.controls.slug.value) {
      this.facade.common.controls.slug.setValue(type.toLowerCase());
      this.facade.common.controls.slug.markAsDirty();
    }
  }

  /** Starter text for the chosen type, poured into every language the template has. */
  protected insertTemplate(): void {
    this.templateBusy.set(true);
    this.policiesApi
      .template(
        this.extra.controls.policyType.value,
        this.extra.controls.jurisdiction.value || null,
      )
      .subscribe({
        next: (template) => {
          this.templateBusy.set(false);
          for (const t of template.translations) {
            const form = this.facade.translationFor(t.language);
            if (form) {
              form.patchValue({title: t.title ?? '', body: t.body ?? ''});
              form.markAsDirty();
            }
          }
          this.toast.success(this.transloco.translate('content.policy.templateInserted'));
        },
        error: (failure: unknown) => {
          this.templateBusy.set(false);
          this.apiErrors.notify(failure);
        },
      });
  }

  protected restoreVersion(version: ReadablePolicyVersion): void {
    const id = this.facade.id();
    if (id === null) {
      return;
    }
    this.policiesApi.restoreText(id, version.version).subscribe({
      next: () => {
        this.toast.success(
          this.transloco.translate('content.policy.versionRestored', {version: version.version}),
        );
        this.facade.reload();
      },
      error: (failure: unknown) => this.apiErrors.notify(failure),
    });
  }

  protected versionDate(version: ReadablePolicyVersion): string {
    return version.publishedAt
      ? this.localeFormat.localizeDate(version.publishedAt, undefined, {dateStyle: 'medium'})
      : '';
  }

  protected versionTone(version: ReadablePolicyVersion): 'green' | 'slate' | 'red' {
    return version.status === 'LIVE' ? 'green' : version.status === 'DRAFT' ? 'slate' : 'red';
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
