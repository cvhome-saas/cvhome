import {Component, computed, effect, inject, input, signal, untracked} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {FormControl, FormGroup, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {FaqService} from '@api/content/faq.service';
import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import type {FaqGroup, PersistableFaq, ReadableFaq} from '@models/content';
import {ConsolePermissions} from '@shared/auth/console-permissions';
import {FormField, Icon, NumberField, Panel, TagInput, TextField, Toggle} from '@cvhome-saas/ui-kit/ui';
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
  titleKey: 'content.copy.question',
  bodyKey: 'content.copy.answer',
  richBody: true,
};

/**
 * `New FAQ Entry.dc.html`: question and answer per language, the group cards (with an inline "new
 * group"), position in the group, search keywords, the checkout-help toggle, an accordion preview.
 */
@Component({
  selector: 'app-faq-editor',
  imports: [
    EditorShell,
    FormField,
    Icon,
    LocaleCopy,
    NumberField,
    Panel,
    PublishChecklist,
    ReactiveFormsModule,
    ScheduleSheet,
    TagInput,
    TextField,
    Toggle,
    TranslocoDirective,
  ],
  providers: [ContentEditorFacade],
  templateUrl: './faq-editor.html',
  styleUrls: ['../../../../shared/styles/field.css', './faq-editor.css'],
})
export class FaqEditor {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly transloco = inject(TranslocoService);
  private readonly faqApi = inject(FaqService);
  private readonly shell = inject(ConsoleShellFacade);
  private readonly permissions = inject(ConsolePermissions);
  protected readonly hub = inject(ContentHubFacade);
  protected readonly facade =
    inject<ContentEditorFacade<PersistableFaq, ReadableFaq>>(ContentEditorFacade);

  readonly id = input<string>();

  protected readonly copyFields = COPY;
  protected readonly deleteOpen = signal(false);
  protected readonly scheduleOpen = signal(false);
  protected readonly scheduleAt = signal('');
  protected readonly newGroupOpen = signal(false);
  protected readonly newGroupName = signal('');
  protected readonly canManage = computed(() => this.permissions.canManageContent());

  protected readonly extra: FormGroup<{
    groupId: FormControl<number | null>;
    position: FormControl<number | null>;
    keywords: FormControl<readonly string[]>;
    showInCheckoutHelp: FormControl<boolean>;
  }> = this.fb.group({
    groupId: this.fb.control<number | null>(null),
    position: this.fb.control<number | null>(null),
    keywords: this.fb.control<readonly string[]>([]),
    showInCheckoutHelp: this.fb.control(false),
  });

  private readonly groupStamp = signal(0);

  private readonly groupsResource = rxResource({
    params: () => {
      this.groupStamp();
      return this.shell.currentStoreId() ?? undefined;
    },
    stream: () => this.faqApi.groups(),
  });

  protected readonly groups = computed<readonly FaqGroup[]>(() =>
    this.groupsResource.hasValue() ? this.groupsResource.value() : [],
  );

  constructor() {
    effect(() => {
      const raw = this.id();
      untracked(() =>
        this.facade.init('faq', raw ? Number(raw) : null, this.extra, (item) => {
          this.extra.reset({
            groupId: item.groupId ?? null,
            position: item.position ?? null,
            keywords: item.keywords ?? [],
            showInCheckoutHelp: !!item.showInCheckoutHelp,
          });
        }),
      );
    });
    effect(() => {
      const locales = this.hub.locales();
      this.facade.syncLanguages(locales.codes, locales.defaultCode);
    });
    // a new entry defaults to the first group once the groups arrive
    effect(() => {
      const first = this.groups()[0];
      if (
        first?.id !== undefined &&
        this.facade.isNew() &&
        this.extra.controls.groupId.value === null
      ) {
        this.extra.controls.groupId.setValue(first.id);
      }
    });
  }

  protected readonly heading = computed(() => {
    this.transloco.activeLang();
    return this.facade.isNew()
      ? this.transloco.translate('content.faq.newTitle')
      : this.facade.title() || this.transloco.translate('content.faq.editTitle');
  });

  protected readonly activeTranslation = computed(
    () =>
      this.facade.translationFor(this.facade.language()) ??
      Object.values(this.facade.translations())[0],
  );

  protected readonly checklist = computed<readonly ChecklistItem[]>(() => {
    this.transloco.activeLang();
    this.facade.written();
    const source = this.facade.sourceTranslation(this.hub.locales().defaultCode);
    const qa =
      !!source &&
      source.controls.title.value.trim().length > 0 &&
      source.controls.body.value.trim().length > 0;
    const missing = this.hub.locales().codes.filter((code) => !this.facade.written().has(code));
    return [
      {key: 'qa', label: this.transloco.translate('content.checklist.questionAndAnswer'), ok: qa},
      {
        key: 'group',
        label: this.transloco.translate('content.checklist.group'),
        ok: this.extra.controls.groupId.value !== null,
      },
      {
        key: 'keywords',
        label: this.transloco.translate('content.checklist.keywords'),
        ok: this.extra.controls.keywords.value.length > 0,
        soft: true,
      },
      {
        key: 'translations',
        label: missing.length
          ? this.transloco.translate('content.checklist.translationsMissing', {
              languages: missing.map((c) => c.toUpperCase()).join(', '),
            })
          : this.transloco.translate('content.checklist.translationsDone'),
        ok: missing.length === 0,
        soft: true,
      },
    ];
  });

  protected groupName(group: FaqGroup): string {
    const lang = this.transloco.getActiveLang();
    return group.names[lang] ?? Object.values(group.names)[0] ?? group.key;
  }

  protected pickGroup(group: FaqGroup): void {
    if (group.id === undefined) {
      return;
    }
    this.extra.controls.groupId.setValue(group.id);
    this.extra.controls.groupId.markAsDirty();
  }

  protected createGroup(): void {
    const name = this.newGroupName().trim();
    if (!name) {
      return;
    }
    const key =
      name
        .toLowerCase()
        .replace(/[^a-z0-9]+/g, '-')
        .replace(/^-+|-+$/g, '') || `group-${Date.now()}`;
    this.faqApi
      .createGroup({
        key,
        names: {[this.transloco.getActiveLang()]: name},
        position: this.groups().length,
      })
      .subscribe({
        next: (group) => {
          this.newGroupName.set('');
          this.newGroupOpen.set(false);
          this.groupStamp.update((v) => v + 1);
          if (group.id !== undefined) {
            this.extra.controls.groupId.setValue(group.id);
            this.extra.controls.groupId.markAsDirty();
          }
        },
      });
  }

  protected setKeywords(keywords: readonly string[]): void {
    this.extra.controls.keywords.setValue(keywords);
    this.extra.controls.keywords.markAsDirty();
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
