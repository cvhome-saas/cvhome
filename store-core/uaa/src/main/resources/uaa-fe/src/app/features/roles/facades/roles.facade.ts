import {Injectable, computed, inject, signal} from '@angular/core';
import {toSignal} from '@angular/core/rxjs-interop';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {TranslocoService} from '@jsverse/transloco';
import {catchError, of} from 'rxjs';

import {ApiErrorService, snapshot} from '@cvhome-saas/ui-kit';
import {ToastService, type SelectOption} from '@cvhome-saas/ui-kit/ui';
import {
  AdminRoleService,
  type PermissionDto,
  type RoleDto,
  type RoleScope,
  type UpdateRoleRequest,
} from '@cvhome-saas/ui-kit/uaa';

export const PAGE_SIZE = 50;

export type RoleFilter = 'all' | 'system' | 'custom';

export const SCOPES: readonly RoleScope[] = ['REALM', 'ORGANIZATION', 'CLIENT'];

/** The catalogue, grouped the way the matrix draws it. */
export interface PermissionGroup {
  readonly key: PermissionDto['group'];
  readonly items: readonly PermissionDto[];
}

/**
 * uaa's role registry.
 *
 * A role is a name plus what it grants: a description, a scope, an optional parent whose
 * permissions it inherits, and a set of keys from the server's catalogue. The name is still the
 * authority — a system role keeps it, and cannot be deleted — so the form locks name and scope for
 * those and leaves everything else editable.
 *
 * **Renaming a role does not re-issue anyone's token.** A principal signed in under the old name
 * keeps it until their next login, which is uaa's behaviour and not something this console can
 * paper over; the page says so rather than implying the change is instant.
 *
 * Filters and search are client-side: uaa's list has neither, and a realm has tens of roles, not
 * thousands. The page size is set high enough that "All" is the whole table.
 */
@Injectable()
export class RolesFacade {
  private readonly roles = inject(AdminRoleService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);

  readonly busy = signal(false);
  readonly pageIndex = signal(0);
  readonly filter = signal<RoleFilter>('all');
  readonly query = signal('');

  /**
   * What the dialog is editing: a role, `'new'` while creating, or null when it is closed.
   *
   * One signal rather than a separate "dialog open" flag — the subject *is* the open state, and two
   * sources of that truth is how a form ends up showing one role while saving another.
   */
  readonly editing = signal<RoleDto | 'new' | null>(null);
  readonly deleting = signal<RoleDto | null>(null);

  /** The permissions ticked in the open dialog. A signal, not a form array: it is a set. */
  readonly draftPermissions = signal<readonly string[]>([]);

  readonly form = new FormGroup({
    name: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(80), Validators.pattern(/^[A-Za-z][A-Za-z0-9_]{1,79}$/)],
    }),
    description: new FormControl('', {nonNullable: true, validators: [Validators.maxLength(255)]}),
    scope: new FormControl<RoleScope>('REALM', {nonNullable: true}),
    inheritsFromId: new FormControl('', {nonNullable: true}),
  });

  /**
   * The catalogue. Allowed to fail: without it the dialog still edits name, description, scope and
   * parent, and shows the role's keys as read-only text — where a failed `forkJoin` would blank the
   * whole page.
   */
  readonly catalogue = toSignal(this.roles.permissions().pipe(catchError(() => of([] as readonly PermissionDto[]))), {
    initialValue: [] as readonly PermissionDto[],
  });

  readonly groups = computed<readonly PermissionGroup[]>(() => {
    const order: PermissionDto['group'][] = ['IDENTITY', 'CLIENTS', 'IDENTITY_PROVIDERS', 'SYSTEM'];
    const all = this.catalogue();
    return order
      .map((key) => ({key, items: all.filter((p) => p.group === key)}))
      .filter((group) => group.items.length > 0);
  });

  private readonly page = snapshot(
    () => ({page: this.pageIndex(), count: PAGE_SIZE}),
    (query) => this.roles.list(query.page, query.count),
  );

  readonly isLoading = this.page.isLoading;
  readonly error = this.page.error;
  readonly isEmpty = this.page.isEmpty;
  readonly reload = () => this.page.reload();

  readonly all = computed(() => this.page.value()?.content ?? []);
  readonly totalElements = computed(() => this.page.value()?.totalElements ?? 0);
  readonly totalPages = computed(() => this.page.value()?.totalPages ?? 0);

  readonly systemCount = computed(() => this.all().filter((r) => r.systemRole).length);
  readonly customCount = computed(() => this.all().length - this.systemCount());

  /** The rows after the segment and the search box; both are applied here, never on the server. */
  readonly rows = computed(() => {
    const filter = this.filter();
    const q = this.query().trim().toLowerCase();
    return this.all().filter((role) => {
      if (filter === 'system' && !role.systemRole) {
        return false;
      }
      if (filter === 'custom' && role.systemRole) {
        return false;
      }
      return !q || role.name.toLowerCase().includes(q) || (role.description ?? '').toLowerCase().includes(q);
    });
  });

  /** Every role but the one being edited, as parent candidates. */
  readonly parentOptions = computed<readonly SelectOption[]>(() => {
    const target = this.editing();
    const self = target && target !== 'new' ? target.id : null;
    return this.all()
      .filter((role) => role.id !== self)
      .map((role) => ({value: role.id, label: role.name}));
  });

  readonly scopeOptions = computed<readonly SelectOption[]>(() =>
    SCOPES.map((scope) => ({value: scope, label: this.transloco.translate(`roles.scope.${scope}`)})),
  );

  /** What the draft would grant in total: the ticked keys plus the chosen parent's effective set. */
  readonly draftEffective = computed(() => {
    const parentId = this.form.controls.inheritsFromId.value;
    const parent = this.all().find((role) => role.id === parentId);
    return new Set([...(parent?.effectivePermissions ?? []), ...this.draftPermissions()]);
  });

  goToPage(page: number): void {
    this.pageIndex.set(page);
  }

  startCreate(): void {
    this.form.reset({name: '', description: '', scope: 'REALM', inheritsFromId: ''});
    this.form.controls.name.enable();
    this.form.controls.scope.enable();
    this.draftPermissions.set([]);
    this.editing.set('new');
  }

  /** Clicking a row opens the dialog on it. Name and scope are locked for a system role. */
  select(role: RoleDto): void {
    this.form.reset({
      name: role.name,
      description: role.description ?? '',
      scope: role.scope,
      inheritsFromId: role.inheritsFromId ?? '',
    });
    if (role.systemRole) {
      this.form.controls.name.disable();
      this.form.controls.scope.disable();
    } else {
      this.form.controls.name.enable();
      this.form.controls.scope.enable();
    }
    this.draftPermissions.set([...role.permissions]);
    this.editing.set(role);
  }

  dismiss(): void {
    this.editing.set(null);
    this.deleting.set(null);
  }

  togglePermission(key: string): void {
    this.draftPermissions.update((current) =>
      current.includes(key) ? current.filter((k) => k !== key) : [...current, key],
    );
    this.form.markAsDirty();
  }

  setGroup(group: PermissionGroup, on: boolean): void {
    const keys = group.items.map((p) => p.key);
    this.draftPermissions.update((current) => {
      const without = current.filter((k) => !keys.includes(k));
      return on ? [...without, ...keys] : without;
    });
    this.form.markAsDirty();
  }

  groupState(group: PermissionGroup): 'all' | 'some' | 'none' {
    const ticked = group.items.filter((p) => this.draftPermissions().includes(p.key)).length;
    return ticked === 0 ? 'none' : ticked === group.items.length ? 'all' : 'some';
  }

  /**
   * Deleting closes the form and opens the confirmation, rather than stacking one modal on another.
   *
   * Two dialogs in the top layer at once leaves the operator looking at a form they can no longer
   * reach, and Escape then closes the wrong one.
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
    const value = this.form.getRawValue();
    const name = value.name.trim().toUpperCase();
    const parent = value.inheritsFromId || null;
    this.busy.set(true);
    const call =
      target === 'new'
        ? this.roles.create({
            name,
            description: value.description.trim() || null,
            scope: value.scope,
            inheritsFromId: parent,
            permissions: this.draftPermissions(),
          })
        : this.roles.update(target.id, this.updateOf(target, name, value.description, value.scope, parent));
    call.subscribe({
      next: () => this.settled(target === 'new' ? 'roles.toast.created' : 'roles.toast.saved', name),
      error: (failure: unknown) => this.failed(failure),
    });
  }

  /** A system role never sends its name or scope: the server would refuse, and they did not change. */
  private updateOf(
    target: RoleDto,
    name: string,
    description: string,
    scope: RoleScope,
    parent: string | null,
  ): UpdateRoleRequest {
    const request: UpdateRoleRequest = {
      description: description.trim() || null,
      permissions: this.draftPermissions(),
      ...(parent ? {inheritsFromId: parent} : {clearInheritsFrom: true}),
    };
    return target.systemRole ? request : {...request, name, scope};
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
