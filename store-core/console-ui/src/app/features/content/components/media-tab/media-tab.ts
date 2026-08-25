import {Component, computed, inject, input, signal} from '@angular/core';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import {copyText} from '@core/platform/clipboard';
import type {ReferenceOption} from '@core/reference/reference-data.service';
import {MEDIA_KINDS, type MediaAsset, type MediaFolder, type MediaKind} from '@models/content';
import {Badge} from '@shared/ui/badge/badge';
import {BusyOverlay} from '@shared/ui/busy-overlay/busy-overlay';
import {ConfirmDialog} from '@shared/ui/confirm-dialog/confirm-dialog';
import {EmptyState} from '@shared/ui/empty-state/empty-state';
import {FileDropZone} from '@shared/ui/file-drop-zone/file-drop-zone';
import {Icon} from '@shared/ui/icon/icon';
import type {IconName} from '@shared/ui/icon/icon-paths';
import {ImagePreview} from '@shared/ui/image-preview/image-preview';
import {LoadError} from '@shared/ui/load-error/load-error';
import {Pagination} from '@shared/ui/pagination/pagination';
import {Panel} from '@shared/ui/panel/panel';
import {SearchBox} from '@shared/ui/search-box/search-box';
import {Select, type SelectOption} from '@shared/ui/select/select';
import {TagInput} from '@shared/ui/tag-input/tag-input';
import {TextField} from '@shared/ui/text-field/text-field';
import {ToastService} from '@shared/ui/toast/toast';
import {Toggle} from '@shared/ui/toggle/toggle';
import {MEDIA_PAGE_SIZE, MediaLibraryFacade} from '../../facades/media-library.facade';

const KIND_ICONS: Readonly<Record<MediaKind, IconName>> = {
  IMAGE: 'images',
  VIDEO: 'play',
  DOCUMENT: 'file',
  ARCHIVE: 'box',
  VECTOR: 'bookmark',
};

/** Bytes as KB / MB / GB, for the grid captions and the quota line. */
export function formatBytes(value: number, digits: (n: number) => string): string {
  if (value >= 1024 ** 3) {
    return `${digits(Math.round((value / 1024 ** 3) * 10) / 10)} GB`;
  }
  if (value >= 1024 ** 2) {
    return `${digits(Math.round((value / 1024 ** 2) * 10) / 10)} MB`;
  }
  return `${digits(Math.max(1, Math.round(value / 1024)))} KB`;
}

/**
 * The media library tab — folder chips, the uploader, grid/list of assets, and a detail drawer for
 * the selected one (alt text per language, title, tags, folder, copy URL, delete with the usage list
 * when the server says it is referenced).
 */
@Component({
  selector: 'app-media-tab',
  imports: [
    Badge,
    BusyOverlay,
    ConfirmDialog,
    EmptyState,
    FileDropZone,
    Icon,
    ImagePreview,
    LoadError,
    Pagination,
    Panel,
    SearchBox,
    Select,
    TagInput,
    TextField,
    Toggle,
    TranslocoDirective,
  ],
  templateUrl: './media-tab.html',
  styleUrl: './media-tab.css',
})
export class MediaTab {
  private readonly transloco = inject(TranslocoService);
  private readonly localeFormat = inject(TranslocoLocaleService);
  private readonly toast = inject(ToastService);
  protected readonly facade = inject(MediaLibraryFacade);

  readonly locales = input.required<readonly ReferenceOption[]>();
  readonly canManage = input(true);

  protected readonly pageSize = MEDIA_PAGE_SIZE;
  protected readonly newFolderName = signal('');
  protected readonly newFolderOpen = signal(false);
  protected readonly previewUrl = signal<string | null>(null);

  /** The drawer's draft metadata, edited locally until Save. */
  protected readonly draftTitle = signal('');
  protected readonly draftTags = signal<readonly string[]>([]);
  protected readonly draftAlt = signal<Record<string, string>>({});
  protected readonly draftFolder = signal<string>('');
  protected readonly altLocale = signal('');

  protected readonly kindOptions = computed<readonly SelectOption[]>(() => {
    this.transloco.activeLang();
    return [
      {value: '', label: this.transloco.translate('content.media.allTypes')},
      ...MEDIA_KINDS.map((kind) => ({
        value: kind,
        label: this.transloco.translate(`content.media.kind.${kind}`),
      })),
    ];
  });

  protected readonly folderOptions = computed<readonly SelectOption[]>(() => {
    this.transloco.activeLang();
    return [
      {value: '', label: this.transloco.translate('content.media.noFolder')},
      ...this.facade.folders().map((folder) => ({value: String(folder.id), label: folder.name})),
    ];
  });

  protected readonly localeSelect = computed<readonly SelectOption[]>(() =>
    this.locales().map((locale) => ({value: locale.code, label: locale.label})),
  );

  /** The store's file count, from the hub summary (folder counts miss unfiled assets). */
  readonly totalFiles = input(0);

  protected kindIcon(asset: MediaAsset): IconName {
    return KIND_ICONS[asset.kind];
  }

  protected meta(asset: MediaAsset): string {
    const size = formatBytes(asset.bytes, (n) => this.localeFormat.localizeNumber(n, 'decimal'));
    return asset.width && asset.height ? `${asset.width}×${asset.height} · ${size}` : size;
  }

  protected usedLabel(asset: MediaAsset): string {
    this.transloco.activeLang();
    return asset.usageCount
      ? this.transloco.translate('content.media.usedOn', {count: asset.usageCount})
      : this.transloco.translate('content.media.unused');
  }

  protected date(asset: MediaAsset): string {
    return asset.uploadedAt
      ? this.localeFormat.localizeDate(asset.uploadedAt, undefined, {dateStyle: 'medium'})
      : '';
  }

  protected pickFolder(folder: MediaFolder | null): void {
    this.facade.folder.set(folder?.id ?? null);
  }

  protected onKind(value: string): void {
    this.facade.kind.set(value === '' ? null : (value as MediaKind));
  }

  protected onFiles(files: readonly File[]): void {
    this.facade.upload(files);
  }

  protected tooLarge = (name: string): string =>
    this.transloco.translate('content.media.tooLarge', {name});

  protected openAsset(asset: MediaAsset): void {
    this.facade.select(asset);
    this.draftTitle.set(asset.title ?? '');
    this.draftTags.set(asset.tags ?? []);
    this.draftAlt.set({...(asset.altTexts ?? {})});
    this.draftFolder.set(asset.folderId ? String(asset.folderId) : '');
    this.altLocale.set(this.locales()[0]?.code ?? 'en');
  }

  protected closeAsset(): void {
    this.facade.select(null);
  }

  protected setAlt(value: string): void {
    this.draftAlt.update((current) => ({...current, [this.altLocale()]: value}));
  }

  protected currentAlt(): string {
    return this.draftAlt()[this.altLocale()] ?? '';
  }

  protected saveAsset(asset: MediaAsset): void {
    this.facade.patch(asset, {
      title: this.draftTitle().trim() || null,
      tags: this.draftTags(),
      altTexts: this.draftAlt(),
      folderId: this.draftFolder() === '' ? null : Number(this.draftFolder()),
    });
  }

  protected async copyUrl(asset: MediaAsset): Promise<void> {
    const ok = await copyText(asset.url);
    if (ok) {
      this.toast.success(this.transloco.translate('content.media.urlCopied'));
    }
  }

  protected createFolder(): void {
    this.facade.createFolder(this.newFolderName());
    this.newFolderName.set('');
    this.newFolderOpen.set(false);
  }

  protected deleteTitle(): string {
    const pending = this.facade.pendingDelete();
    if (!pending) {
      return '';
    }
    return this.transloco.translate(
      pending.force ? 'content.media.delete.forceTitle' : 'content.media.delete.title',
      {
        name: pending.asset.filename,
      },
    );
  }

  protected deleteMessage(): string {
    const pending = this.facade.pendingDelete();
    if (!pending) {
      return '';
    }
    if (pending.force) {
      const uses = (this.facade.openAsset()?.usage ?? [])
        .map((use) => use.itemTitle ?? `#${use.itemId}`)
        .slice(0, 5)
        .join(', ');
      return this.transloco.translate('content.media.delete.forceMessage', {uses});
    }
    return this.transloco.translate('content.media.delete.message');
  }
}
