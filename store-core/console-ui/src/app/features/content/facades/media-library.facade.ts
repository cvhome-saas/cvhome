import {Injectable, computed, inject, linkedSignal, signal} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {TranslocoService} from '@jsverse/transloco';

import {ContentCache} from '@api/content/content-cache';
import {MediaService, type MediaQuery} from '@api/content/media.service';
import {ApiErrorService} from '@core/errors/api-error.service';
import {ApiError} from '@core/errors/api-error';
import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import type {MediaAsset, MediaFolder, MediaKind, PersistableMediaAsset} from '@models/content';
import {EMPTY_PAGE, type PageT} from '@models/page';
import {ToastService} from '@shared/ui/toast/toast';

export const MEDIA_PAGE_SIZE = 24;

export type MediaView = 'grid' | 'list';

/**
 * The media library's state: folders, the filtered page of assets, uploads in flight, and the asset
 * open in the detail drawer. Root-provided because the picker dialog (inside an editor) and the
 * media tab share it.
 */
@Injectable({providedIn: 'root'})
export class MediaLibraryFacade {
  private readonly api = inject(MediaService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);
  private readonly shell = inject(ConsoleShellFacade);
  private readonly cache = inject(ContentCache);

  readonly folder = signal<number | null>(null);
  readonly kind = signal<MediaKind | null>(null);
  readonly search = signal('');
  readonly unusedOnly = signal(false);
  readonly view = signal<MediaView>('grid');

  readonly pageIndex = linkedSignal<unknown, number>({
    source: () => [this.folder(), this.kind(), this.search(), this.unusedOnly(), this.shell.currentStoreId()],
    computation: () => 0,
  });

  /** Bumped by every write here, so the folder counts and the page reload together. */
  private readonly stamp = signal(0);

  private readonly foldersResource = rxResource({
    params: () => {
      this.stamp();
      this.cache.stamp();
      return this.shell.currentStoreId() ?? undefined;
    },
    stream: () => this.api.folders(),
  });

  readonly folders = computed<readonly MediaFolder[]>(() =>
    this.foldersResource.hasValue() ? this.foldersResource.value() : [],
  );

  private readonly assetsResource = rxResource({
    params: () => {
      this.stamp();
      this.cache.stamp();
      if (!this.shell.currentStoreId()) {
        return undefined;
      }
      const query: MediaQuery = {
        folder: this.folder(),
        kind: this.kind(),
        q: this.search(),
        used: this.unusedOnly() ? false : null,
        page: this.pageIndex(),
        count: MEDIA_PAGE_SIZE,
      };
      return query;
    },
    stream: ({params}) => this.api.list(params),
  });

  private readonly loaded = linkedSignal<PageT<MediaAsset> | undefined, PageT<MediaAsset> | undefined>({
    source: () => (this.assetsResource.hasValue() ? this.assetsResource.value() : undefined),
    computation: (incoming, previous) => incoming ?? previous?.value,
  });

  readonly isLoading = this.assetsResource.isLoading;
  readonly error = computed(() => this.assetsResource.error() as Error | undefined);
  readonly isEmpty = computed(() => this.loaded() === undefined);
  readonly page = computed<PageT<MediaAsset>>(() => this.loaded() ?? (EMPTY_PAGE as PageT<MediaAsset>));
  readonly assets = computed<readonly MediaAsset[]>(() => this.page().content);
  readonly filtered = computed(
    () => this.folder() !== null || this.kind() !== null || this.search().trim() !== '' || this.unusedOnly(),
  );

  readonly saving = signal(false);

  /* ------------------------------------------------------------------------ uploads ---- */

  readonly uploading = signal(false);
  readonly uploadPercent = signal(0);

  upload(files: readonly File[]): void {
    if (!files.length) {
      return;
    }
    this.uploading.set(true);
    this.uploadPercent.set(0);
    this.api.upload(files, this.folder()).subscribe({
      next: (event) => {
        if (event.kind === 'progress') {
          this.uploadPercent.set(event.percent);
        } else {
          this.uploading.set(false);
          this.stamp.update((v) => v + 1);
          this.cache.invalidate();
          this.toast.success(this.transloco.translate('content.media.uploaded', {count: event.assets.length}));
        }
      },
      error: (failure: unknown) => {
        this.uploading.set(false);
        this.apiErrors.notify(failure);
      },
    });
  }

  /* ------------------------------------------------------------------------- drawer ---- */

  readonly openAsset = signal<MediaAsset | null>(null);

  select(asset: MediaAsset | null): void {
    this.openAsset.set(asset);
    if (asset) {
      // the list row carries no usage list; the single read does
      this.api.get(asset.id).subscribe({next: (full) => this.openAsset.set(full)});
    }
  }

  patch(asset: MediaAsset, body: PersistableMediaAsset): void {
    this.saving.set(true);
    this.api.patch(asset.id, body).subscribe({
      next: (saved) => {
        this.saving.set(false);
        this.openAsset.set(saved);
        this.stamp.update((v) => v + 1);
        this.toast.success(this.transloco.translate('content.media.saved'));
      },
      error: (failure: unknown) => {
        this.saving.set(false);
        this.apiErrors.notify(failure);
      },
    });
  }

  /** An asset waiting on the delete confirmation; `force` once the server answered 409 referenced. */
  readonly pendingDelete = signal<{asset: MediaAsset; force: boolean} | null>(null);

  askDelete(asset: MediaAsset): void {
    this.pendingDelete.set({asset, force: false});
  }

  dismissDelete(): void {
    this.pendingDelete.set(null);
  }

  confirmDelete(): void {
    const pending = this.pendingDelete();
    this.pendingDelete.set(null);
    if (!pending) {
      return;
    }
    this.saving.set(true);
    this.api.delete(pending.asset.id, pending.force).subscribe({
      next: () => {
        this.saving.set(false);
        if (this.openAsset()?.id === pending.asset.id) {
          this.openAsset.set(null);
        }
        this.stamp.update((v) => v + 1);
        this.cache.invalidate();
        this.toast.success(this.transloco.translate('content.media.deleted', {name: pending.asset.filename}));
      },
      error: (failure: unknown) => {
        this.saving.set(false);
        // Still referenced: ask again, this time to force. The dialog says where it is used.
        if (failure instanceof ApiError && failure.code === 'MEDIA.REFERENCED') {
          this.pendingDelete.set({asset: pending.asset, force: true});
          return;
        }
        this.apiErrors.notify(failure);
      },
    });
  }

  /* ------------------------------------------------------------------------ folders ---- */

  createFolder(name: string): void {
    if (!name.trim()) {
      return;
    }
    this.saving.set(true);
    this.api.createFolder({name: name.trim()}).subscribe({
      next: (folder) => {
        this.saving.set(false);
        this.stamp.update((v) => v + 1);
        this.folder.set(folder.id ?? null);
      },
      error: (failure: unknown) => {
        this.saving.set(false);
        this.apiErrors.notify(failure);
      },
    });
  }

  /** Deletes the open folder; its files move to the first other folder, or stay unfiled when there is none. */
  deleteFolder(folder: MediaFolder): void {
    if (!folder.id) {
      return;
    }
    const target = this.folders().find((other) => other.id !== folder.id)?.id;
    this.saving.set(true);
    this.api.deleteFolder(folder.id, target).subscribe({
      next: () => {
        this.saving.set(false);
        if (this.folder() === folder.id) {
          this.folder.set(null);
        }
        this.stamp.update((v) => v + 1);
      },
      error: (failure: unknown) => {
        this.saving.set(false);
        this.apiErrors.notify(failure);
      },
    });
  }

  goToPage(page: number): void {
    this.pageIndex.set(page);
  }

  clearFilters(): void {
    this.folder.set(null);
    this.kind.set(null);
    this.search.set('');
    this.unusedOnly.set(false);
  }

  retry(): void {
    this.assetsResource.reload();
  }
}
