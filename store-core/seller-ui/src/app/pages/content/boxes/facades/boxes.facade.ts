import {DestroyRef, Injectable, inject, signal} from '@angular/core';
import {Router} from '@angular/router';
import {NbDialogService} from '@nebular/theme';
import {ShowcaseDialogComponent} from "../../../shared/components/showcase-dialog/showcase-dialog.component";
import {ApiErrorService} from 'seller-core';
import {NotificationService} from '../../../../core/notifications/notification.service';
import {ContentService} from "seller-core/content";
import {SelectedStoreService} from "seller-core";
import {TableStateService} from "seller-core";
import {StorePageRequest, PageT} from "seller-core";
import {PageEvent} from "@swimlane/ngx-datatable";
import {Observable, tap} from "rxjs";
import {map} from "rxjs/operators";
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ReadableContentBox} from "seller-core/content";

@Injectable()
export class BoxesFacade {
  private readonly contentService = inject(ContentService);
  private readonly router = inject(Router);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly notify = inject(NotificationService);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly dialogService = inject(NbDialogService);
  public readonly tableState = inject(TableStateService<ReadableContentBox, StorePageRequest>);

  readonly selectedStore = signal<string>('');

  private readonly recommendedCodes = ["meta-title", "meta-description", "agreement", "header-message"];

  init(destroyRef: DestroyRef): void {
    this.selectedStoreService.current()
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe({
        next: (store) => {
          this.selectedStore.set(store);
          if (store) {
            this.refresh();
          }
        },
        error: (err) => this.apiErrors.notify(err)
      });
  }

  onPageChange(event: PageEvent): void {
    this.tableState.patchParams({ page: event.offset });
    this.refresh();
  }

  private refresh(): void {
    const store = this.selectedStore();
    if (!store) return;

    const request: StorePageRequest = { ...this.tableState.params(), store };
    this.loadBoxes(request).subscribe(page => this.tableState.setPage(page));
  }

  loadBoxes(request: StorePageRequest): Observable<PageT<ReadableContentBox>> {
    this.tableState.setLoading(true);
    return this.contentService.getBoxes(request).pipe(
      map((it) => {
        const missingCodes = this.recommendedCodes.filter(code => !it.content.some((e) => e.code === code));
        const content: ReadableContentBox[] = [...it.content];
        missingCodes.forEach(code => {
          content.unshift({ code });
        });
        return {
          ...it,
          size: content.length,
          totalElements: content.length,
          content
        };
      }),
      tap({
        next: () => this.tableState.setLoading(false),
        error: (err) => {
          this.tableState.setLoading(false);
          this.apiErrors.notify(err);
        }
      })
    );
  }

  addBox(): void {
    this.router.navigate(['/pages/content/boxes/add']);
  }

  onEdit(event: ReadableContentBox): void {
    this.router.navigate(['/pages/content/boxes/edit', event.code]);
  }

  onCreate(event: ReadableContentBox): void {
    this.router.navigate(['/pages/content/boxes/add'], { queryParams: { code: event.code } });
  }

  onDelete(event: ReadableContentBox): void {
    this.dialogService.open(ShowcaseDialogComponent, {
      context: {
        title: 'Are you sure!',
        text: 'Do you really want to remove this entity?'
      },
    }).onClose.subscribe(res => {
      if (res) {
        this.contentService.deleteContent(event.id)
          .subscribe({
            next: () => {
              this.notify.success('CONTENT.BOX_DELETED');
              this.refresh();
            },
            error: (err) => {
              this.apiErrors.notify(err);
            }
          });
      }
    });
  }
}
