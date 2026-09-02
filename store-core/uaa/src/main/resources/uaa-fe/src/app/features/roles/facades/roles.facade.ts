import {Injectable, computed, inject, signal} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {TranslocoService} from '@jsverse/transloco';

import {ApiErrorService, snapshot} from '@cvhome-saas/ui-kit';
import {ToastService} from '@cvhome-saas/ui-kit/ui';
import {AdminRoleService, type RoleDto} from '@cvhome-saas/ui-kit/uaa';

export const PAGE_SIZE = 20;

/**
 * uaa's role registry.
 *
 * A role is a name and nothing else — `Role` is `{id, name}` and both request records carry the
 * single field — so this page is a list, a one-field form and a delete. There are no permissions to
 * edit: an authority *is* the role name, and what it grants is decided by the `@PreAuthorize` and
 * filter-chain rules in each service.
 *
 * **Renaming a role does not re-issue anyone's token.** A principal signed in under the old name
 * keeps it until their next login, which is uaa's behaviour and not something this console can
 * paper over; the page says so rather than implying the change is instant.
 */
@Injectable()
export class RolesFacade {
  private readonly roles = inject(AdminRoleService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);

  readonly busy = signal(false);
  readonly pageIndex = signal(0);

  /**
   * What the dialog is editing: a role, `'new'` while creating, or null when it is closed.
   *
   * One signal rather than a separate "dialog open" flag — the subject *is* the open state, and two
   * sources of that truth is how a form ends up showing one role while saving another.
   */
  readonly editing = signal<RoleDto | 'new' | null>(null);
  readonly deleting = signal<RoleDto | null>(null);

  readonly form = new FormGroup({
    name: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(80)],
    }),
  });

  private readonly page = snapshot(
    () => ({page: this.pageIndex(), count: PAGE_SIZE}),
    (query) => this.roles.list(query.page, query.count),
  );

  readonly isLoading = this.page.isLoading;
  readonly error = this.page.error;
  readonly isEmpty = this.page.isEmpty;
  readonly reload = () => this.page.reload();

  readonly rows = computed(() => this.page.value()?.content ?? []);
  readonly totalElements = computed(() => this.page.value()?.totalElements ?? 0);
  readonly totalPages = computed(() => this.page.value()?.totalPages ?? 0);

  goToPage(page: number): void {
    this.pageIndex.set(page);
  }

  startCreate(): void {
    this.form.reset({name: ''});
    this.editing.set('new');
  }

  /** Clicking a row opens the dialog on it. */
  select(role: RoleDto): void {
    this.form.reset({name: role.name});
    this.editing.set(role);
  }

  dismiss(): void {
    this.editing.set(null);
    this.deleting.set(null);
  }

  /**
   * Deleting closes the form and opens the confirmation, rather than stacking one modal on another.
   *
   * Two dialogs in the top layer at once leaves the operator looking at a form they can no longer
   * reach, and Escape then closes the wrong one. Cancelling the confirmation returns to the list —
   * acceptable here, because a role is a single field to retype.
   */
  askDelete(role: RoleDto): void {
    this.editing.set(null);
    this.deleting.set(role);
  }

  save(): void {
    const target = this.editing();
    if (!target || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const request = {name: this.form.controls.name.value.trim()};
    this.busy.set(true);
    const call =
      target === 'new' ? this.roles.create(request) : this.roles.update(target.id, request);
    call.subscribe({
      next: () => this.settled(target === 'new' ? 'roles.toast.created' : 'roles.toast.renamed', request.name),
      error: (failure: unknown) => this.failed(failure),
    });
  }

  confirmDelete(): void {
    const target = this.deleting();
    if (!target) {
      return;
    }
    this.busy.set(true);
    this.roles.delete(target.id).subscribe({
      next: () => this.settled('roles.toast.deleted', target.name),
      error: (failure: unknown) => this.failed(failure),
    });
  }

  private settled(key: string, name: string): void {
    this.busy.set(false);
    this.dismiss();
    this.toast.success(this.transloco.translate(key, {name}));
    this.page.reload();
  }

  /**
   * Server-side validation lands on the form, not only in a toast.
   *
   * `applyToForm` and `clearServerErrorsOnChange` belong together — without the second the field
   * stays invalid after the operator has fixed it — and `ApiErrorService` pairs them itself.
   */
  private failed(failure: unknown): void {
    this.busy.set(false);
    this.apiErrors.applyToForm(failure, this.form);
    this.apiErrors.notify(failure);
  }
}
