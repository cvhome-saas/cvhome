import {Component, computed, effect, inject, input, signal, untracked} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {
  FormControl,
  FormGroup,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {PostCategoriesService} from '@api/content/post-categories.service';
import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import type {MediaAsset, PersistablePost, PostCategory, ReadablePost} from '@models/content';
import {ConsolePermissions} from '@shared/auth/console-permissions';
import {Checkbox} from '@shared/ui/checkbox/checkbox';
import {DateTimeField} from '@shared/ui/date-time-field/date-time-field';
import {FormField} from '@shared/ui/form-field/form-field';
import {Icon} from '@shared/ui/icon/icon';
import {Panel} from '@shared/ui/panel/panel';
import {TagInput} from '@shared/ui/tag-input/tag-input';
import {TextField} from '@shared/ui/text-field/text-field';
import {Toggle} from '@shared/ui/toggle/toggle';
import {LocaleCopy, type CopyFields} from '../../components/locale-copy/locale-copy';
import {MediaPickerDialog} from '../../components/media-picker/media-picker-dialog';
import {
  PublishChecklist,
  type ChecklistItem,
} from '../../components/publish-checklist/publish-checklist';
import {SeoBlock} from '../../components/seo-block/seo-block';
import {ContentEditorFacade} from '../../facades/content-editor.facade';
import {ContentHubFacade} from '../../facades/content-hub.facade';
import {EditorShell, type EditorCommand} from '../editor-shell/editor-shell';

const COPY: CopyFields = {
  titleKey: 'content.copy.postTitle',
  excerptKey: 'content.copy.excerpt',
  bodyKey: 'content.copy.body',
  richBody: true,
};

/**
 * A blog post: title/excerpt/body per language, hero image from the media library, categories, tags,
 * author and featured flag, URL & search, schedule, checklist.
 */
@Component({
  selector: 'app-post-editor',
  imports: [
    Checkbox,
    DateTimeField,
    EditorShell,
    FormField,
    Icon,
    LocaleCopy,
    MediaPickerDialog,
    Panel,
    PublishChecklist,
    ReactiveFormsModule,
    SeoBlock,
    TagInput,
    TextField,
    Toggle,
    TranslocoDirective,
  ],
  providers: [ContentEditorFacade],
  templateUrl: './post-editor.html',
  styleUrls: ['../../../../shared/styles/field.css', './post-editor.css'],
})
export class PostEditor {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly transloco = inject(TranslocoService);
  private readonly categoriesApi = inject(PostCategoriesService);
  private readonly shell = inject(ConsoleShellFacade);
  private readonly permissions = inject(ConsolePermissions);
  protected readonly hub = inject(ContentHubFacade);
  protected readonly facade =
    inject<ContentEditorFacade<PersistablePost, ReadablePost>>(ContentEditorFacade);

  readonly id = input<string>();

  protected readonly copyFields = COPY;
  protected readonly deleteOpen = signal(false);
  protected readonly scheduleOpen = signal(false);
  protected readonly scheduleAt = signal('');
  protected readonly pickerOpen = signal(false);
  protected readonly heroUrl = signal<string | null>(null);
  protected readonly canManage = computed(() => this.permissions.canManageContent());

  protected readonly extra: FormGroup<{
    heroMediaId: FormControl<number | null>;
    categoryIds: FormControl<readonly number[]>;
    tags: FormControl<readonly string[]>;
    authorName: FormControl<string>;
    featured: FormControl<boolean>;
  }> = this.fb.group({
    heroMediaId: this.fb.control<number | null>(null),
    categoryIds: this.fb.control<readonly number[]>([]),
    tags: this.fb.control<readonly string[]>([]),
    authorName: this.fb.control('', {validators: [Validators.maxLength(120)]}),
    featured: this.fb.control(false),
  });

  private readonly categories = rxResource({
    params: () => this.shell.currentStoreId() ?? undefined,
    stream: () => this.categoriesApi.list(),
  });

  protected readonly categoryOptions = computed<readonly PostCategory[]>(() =>
    this.categories.hasValue() ? this.categories.value() : [],
  );

  constructor() {
    effect(() => {
      const raw = this.id();
      untracked(() =>
        this.facade.init('posts', raw ? Number(raw) : null, this.extra, (item) => {
          this.extra.reset({
            heroMediaId: item.heroMediaId ?? null,
            categoryIds: item.categoryIds ?? [],
            tags: item.tags ?? [],
            authorName: item.authorName ?? '',
            featured: !!item.featured,
          });
          this.heroUrl.set(item.heroMediaUrl ?? null);
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
      ? this.transloco.translate('content.post.newTitle')
      : this.facade.title() || this.transloco.translate('content.post.editTitle');
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
    const missing = this.hub.locales().codes.filter((code) => !this.facade.written().has(code));
    return [
      {
        key: 'copy',
        label: this.transloco.translate('content.checklist.titleAndBody'),
        ok: titleAndBody,
      },
      {
        key: 'slug',
        label: this.transloco.translate('content.checklist.slug'),
        ok: this.facade.slugOk(),
      },
      {
        key: 'hero',
        label: this.transloco.translate('content.checklist.hero'),
        ok: this.extra.controls.heroMediaId.value !== null,
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

  protected categoryName(category: PostCategory): string {
    const lang = this.transloco.getActiveLang();
    return category.names[lang] ?? Object.values(category.names)[0] ?? category.slug;
  }

  protected categoryChecked(category: PostCategory): boolean {
    return category.id !== undefined && this.extra.controls.categoryIds.value.includes(category.id);
  }

  protected toggleCategory(category: PostCategory): void {
    if (category.id === undefined) {
      return;
    }
    const id = category.id;
    const current = this.extra.controls.categoryIds.value;
    this.extra.controls.categoryIds.setValue(
      current.includes(id) ? current.filter((c) => c !== id) : [...current, id],
    );
    this.extra.controls.categoryIds.markAsDirty();
  }

  protected setTags(tags: readonly string[]): void {
    this.extra.controls.tags.setValue(tags);
    this.extra.controls.tags.markAsDirty();
  }

  protected onHero(asset: MediaAsset): void {
    this.extra.controls.heroMediaId.setValue(asset.id);
    this.extra.controls.heroMediaId.markAsDirty();
    this.heroUrl.set(asset.url);
  }

  protected clearHero(): void {
    this.extra.controls.heroMediaId.setValue(null);
    this.extra.controls.heroMediaId.markAsDirty();
    this.heroUrl.set(null);
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
