import {DestroyRef, Injectable, inject, signal} from '@angular/core';
import {Router} from '@angular/router';
import {NbDialogService} from '@nebular/theme';
import {ShowcaseDialogComponent} from "../../../shared/components/showcase-dialog/showcase-dialog.component";
import {ErrorService} from "../../../shared/services/error.service";
import {ContentService} from "../../services/content.service";
import {SelectedStoreService} from "../../../shared/services/selected-store.service";
import {TableStateService} from "../../../shared/table/table-state.service";
import {StorePageRequest, PageT} from "../../../shared/table/table.types";
import {PageEvent} from "@swimlane/ngx-datatable";
import {Observable, tap} from "rxjs";
import {map} from "rxjs/operators";
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ReadableContentPage} from "../../models/content.model";

@Injectable()
export class PagesFacade {
  private readonly contentService = inject(ContentService);
  private readonly router = inject(Router);
  private readonly errorService = inject(ErrorService);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly dialogService = inject(NbDialogService);
  public readonly tableState = inject(TableStateService<ReadableContentPage, StorePageRequest>);

  readonly selectedStore = signal<string>('');

  private readonly recommendedCodes = ["about-us", "contact-us", "faq", "location", "privacy", "terms"];

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
        error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
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
    this.loadPages(request).subscribe(page => this.tableState.setPage(page));
  }

  loadPages(request: StorePageRequest): Observable<PageT<ReadableContentPage>> {
    this.tableState.setLoading(true);
    return this.contentService.pages(request).pipe(
      map((it) => {
        const missingCodes = this.recommendedCodes.filter(code => !it.content.some((e) => e.code === code));
        const content: ReadableContentPage[] = [...it.content];
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
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      })
    );
  }

  addPages(): void {
    localStorage.setItem('contentpageid', '');
    this.router.navigate(['/pages/content/pages/add']);
  }

  onEdit(event: ReadableContentPage): void {
    this.router.navigate(['/pages/content/pages/edit/' + event.code]);
  }

  onCreate(event: ReadableContentPage): void {
    this.router.navigate(['/pages/content/pages/add'], { queryParams: { code: event.code } });
  }

  onDelete(event: ReadableContentPage): void {
    this.dialogService.open(ShowcaseDialogComponent, {
      context: {
        title: 'Are you sure!',
        text: 'Do you really want to remove this entity?'
      }
    }).onClose.subscribe(res => {
      if (res) {
        this.contentService.deleteContent(event.id)
          .subscribe({
            next: () => {
              this.errorService.success('Content page deleted successfully');
              this.refresh();
            },
            error: (err) => {
              this.errorService.error('ERROR.SYSTEM_ERROR', err);
            }
          });
      }
    });
  }
}
