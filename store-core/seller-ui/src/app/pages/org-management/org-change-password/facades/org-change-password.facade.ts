import {DestroyRef, Injectable, inject, signal} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {map, mergeMap} from 'rxjs/operators';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {OrgService} from '../../services/org.service';
import {ErrorService} from '../../../shared/services/error.service';
import {OrgChangePasswordFormService} from '../services/org-change-password-form.service';
import {Org} from '../../model/org';
import {ORG_SIDEMENU_LINKS} from '../../constants/org-management.constants';

@Injectable()
export class OrgChangePasswordFacade {
  private readonly formService = inject(OrgChangePasswordFormService);
  private readonly orgService = inject(OrgService);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly errorService = inject(ErrorService);

  readonly loader = signal<boolean>(false);
  readonly org = signal<Org | null>(null);
  readonly selectedItem = signal<string>('1');
  readonly sidemenuLinks = ORG_SIDEMENU_LINKS;

  get form() {
    return this.formService.form;
  }

  init(destroyRef: DestroyRef): void {
    this.activatedRoute.params.pipe(
      map(params => params['id']),
      mergeMap(id => this.orgService.getOrg(id)),
      takeUntilDestroyed(destroyRef)
    ).subscribe({
      next: (res) => {
        this.org.set(res);
      },
      error: (err) => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  save(): void {
    const currentOrg = this.org();
    if (!currentOrg) return;

    this.loader.set(true);
    const passwords = {
      password: this.form.value.newPassword
    };

    this.orgService.changeOrgPassword(currentOrg.id.id, passwords).subscribe({
      next: () => {
        this.errorService.success('ORG_FORM.PASSWORD_CHANGED_SUCCESSFULLY');
        this.loader.set(false);
      },
      error: (err) => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
        this.loader.set(false);
      }
    });
  }

  route(link: string): void {
    const currentOrg = this.org();
    if (currentOrg) {
      this.router.navigate([link.replace('{OrgId}', currentOrg.id.id)]);
    }
  }

  goToBack(): void {
    this.router.navigate(['pages/org-management/org-list']);
  }
}
