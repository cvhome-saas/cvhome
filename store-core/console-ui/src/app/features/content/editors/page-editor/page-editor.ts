import {Component, computed, effect, inject, input, signal, untracked} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {FormControl, FormGroup, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';
import {map} from 'rxjs';

import {ContentItemsService} from '@api/content/content-items.service';
import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import type {PageTemplate, PersistablePage, ReadablePage} from '@models/content';
import {ConsolePermissions} from '@shared/auth/console-permissions';
import {DateTimeField} from '@shared/ui/date-time-field/date-time-field';
import {FormField} from '@shared/ui/form-field/form-field';
import {Icon} from '@shared/ui/icon/icon';
import {Panel} from '@shared/ui/panel/panel';
import {Select, type SelectOption} from '@shared/ui/select/select';
import {Toggle} from '@shared/ui/toggle/toggle';
import {LocaleCopy, type CopyFields} from '../../components/locale-copy/locale-copy';
import {
  PublishChecklist,
  type ChecklistItem,
} from '../../components/publish-checklist/publish-checklist';
import {SeoBlock} from '../../components/seo-block/seo-block';
import {ContentEditorFacade} from '../../facades/content-editor.facade';
import {ContentHubFacade} from '../../facades/content-hub.facade';
import {EditorShell, type EditorCommand} from '../editor-shell/editor-shell';

const TEMPLATES: readonly PageTemplate[] = ['STANDARD', 'LANDING', 'CONTACT', 'FAQ_PAGE'];

const COPY: CopyFields = {
  titleKey: 'content.copy.pageTitle',
  bodyKey: 'content.copy.body',
  richBody: true,
};

/**
 * `New Page.dc.html`: template cards, the per-language title and body, URL & search, and a sidebar
 * with publish date, footer/menu toggles, parent page and the before-publishing checklist.
 *
 * Not built: the "Stores" picker (an item belongs to the store it was created in — the service is
 * store-scoped) and the storefront preview.
 * TODO(lessons.md): in-console storefront preview — see lessons.md, "Content — no in-console
 * storefront preview". The preview token endpoint exists; the console lacks the storefront host.
 */
@Component({
  selector: 'app-page-editor',
  imports: [
    DateTimeField,
    EditorShell,
    FormField,
    Icon,
    LocaleCopy,
    Panel,
    PublishChecklist,
    ReactiveFormsModule,
    Select,
    SeoBlock,
    Toggle,
    TranslocoDirective,
  ],
  providers: [ContentEditorFacade],
  templateUrl: './page-editor.html',
  styleUrls: ['../../../../shared/styles/field.css', './page-editor.css'],
})
export class PageEditor {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly transloco = inject(TranslocoService);
  private readonly items = inject(ContentItemsService);
  private readonly shell = inject(ConsoleShellFacade);
  private readonly permissions = inject(ConsolePermissions);
  protected readonly hub = inject(ContentHubFacade);
  protected readonly facade =
    inject<ContentEditorFacade<PersistablePage, ReadablePage>>(ContentEditorFacade);

  /** The route's `:id`; absent on `new`. */
  readonly id = input<string>();

  protected readonly templates = TEMPLATES;
  protected readonly copyFields = COPY;
  protected readonly deleteOpen = signal(false);
  protected readonly scheduleOpen = signal(false);
  protected readonly scheduleAt = signal('');
  protected readonly canManage = computed(() => this.permissions.canManageContent());

  protected readonly extra: FormGroup<{
    template: FormControl<PageTemplate>;
    parentId: FormControl<number | null>;
    showInFooter: FormControl<boolean>;
    linkToMenu: FormControl<boolean>;
  }> = this.fb.group({
    template: this.fb.control<PageTemplate>('STANDARD'),
    parentId: this.fb.control<number | null>(null),
    showInFooter: this.fb.control(false),
    linkToMenu: this.fb.control(false),
  });

  /** Other pages, as parent candidates. */
  private readonly pages = rxResource({
    params: () => this.shell.currentStoreId() ?? undefined,
    stream: () =>
      this.items
        .list('pages', {status: null, locale: null, state: null, q: ''}, {page: 0, count: 100})
        .pipe(map((page) => page.content)),
  });

  protected readonly parentOptions = computed<readonly SelectOption[]>(() => {
    this.transloco.activeLang();
    const self = this.facade.id();
    const rows = this.pages.hasValue() ? this.pages.value() : [];
    return [
      {value: '', label: this.transloco.translate('content.page.noParent')},
      ...rows
        .filter((row) => row.id !== self)
        .map((row) => ({value: String(row.id), label: row.title})),
    ];
  });

  constructor() {
    effect(() => {
      const raw = this.id();
      untracked(() =>
        this.facade.init('pages', raw ? Number(raw) : null, this.extra, (item) => {
          this.extra.reset({
            template: item.template ?? 'STANDARD',
            parentId: item.parentId ?? null,
            showInFooter: !!item.showInFooter,
            linkToMenu: !!item.linkToMenu,
          });
        }),
      );
    });
    effect(() => {
      const locales = this.hub.locales();
      this.facade.syncLanguages(locales.codes, locales.defaultCode);
    });
  }

  protected readonly heading = computed(() => {
    this.transloco.activeLang();
    return this.facade.isNew()
      ? this.transloco.translate('content.page.newTitle')
      : this.facade.title() || this.transloco.translate('content.page.editTitle');
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
    const titleAndBody =
      !!source &&
      source.controls.title.value.trim().length > 0 &&
      source.controls.body.value.trim().length > 0;
    const slug = this.facade.common?.controls.slug.valid ?? false;
    const meta = (source?.controls.metaDescription.value ?? '').length <= 160;
    const missing = this.hub.locales().codes.filter((code) => !this.facade.written().has(code));
    return [
      {
        key: 'copy',
        label: this.transloco.translate('content.checklist.titleAndBody'),
        ok: titleAndBody,
      },
      {key: 'slug', label: this.transloco.translate('content.checklist.slug'), ok: slug},
      {
        key: 'meta',
        label: this.transloco.translate('content.checklist.metaLength'),
        ok: meta,
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

  protected templateLabel(template: PageTemplate): string {
    return this.transloco.translate(`content.page.template.${template}.label`);
  }

  protected templateHint(template: PageTemplate): string {
    return this.transloco.translate(`content.page.template.${template}.hint`);
  }

  protected pickTemplate(template: PageTemplate): void {
    this.extra.controls.template.setValue(template);
    this.extra.controls.template.markAsDirty();
  }

  protected onParent(value: string): void {
    this.extra.controls.parentId.setValue(value === '' ? null : Number(value));
    this.extra.controls.parentId.markAsDirty();
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

  protected confirmSchedule(): void {
    const at = this.scheduleAt();
    this.scheduleOpen.set(false);
    if (at) {
      this.facade.transition('publish', at);
    }
  }
}
