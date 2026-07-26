import {Injectable, inject, signal} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {NbToastrService} from '@nebular/theme';
import {TranslateService} from '@ngx-translate/core';
import {map, mergeMap} from 'rxjs/operators';
import {OrgService} from '../../services/org.service';
import {ErrorService} from '../../../shared/services/error.service';
import {UpdateOrgDetailsFormService} from '../services/update-org-details-form.service';
import {Org} from '../../model/org';
import {ORG_SIDEMENU_LINKS} from '../../constants/org-management.constants';

@Injectable()
export class UpdateOrgDetailsFacade {
  private readonly formService = inject(UpdateOrgDetailsFormService);
  private readonly orgService = inject(OrgService);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly toastr = inject(NbToastrService);
  private readonly translate = inject(TranslateService);
  private readonly errorService = inject(ErrorService);

  readonly loader = signal<boolean>(false);
  readonly loadingInfo = signal<boolean>(false);
  readonly subscriptionPlans = signal<string[]>([]);
  readonly org = signal<Org | null>(null);
  readonly selectedItem = signal<string>('0');
  readonly sidemenuLinks = ORG_SIDEMENU_LINKS;

  get form() {
    return this.formService.form;
  }

  init(): void {
    this.activatedRoute.params.pipe(
      map(params => params['id']),
      mergeMap(id => this.orgService.getOrg(id))
    ).subscribe({
      next: (res) => {
        this.loadingInfo.set(false);
        this.org.set(res);
        this.formService.fillForm(res);
      },
      error: (err) => {
        this.loadingInfo.set(false);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });

    this.orgService.getSubscriptionPlans().subscribe({
      next: (plans) => this.subscriptionPlans.set(plans),
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
  }

  save(): void {
    const currentOrg = this.org();
    if (!currentOrg) return;

    this.loader.set(true);
    this.orgService.updateOrg(currentOrg.id.id, this.form.value).subscribe({
      next: () => {
        this.toastr.success(this.translate.instant('ORG_FORM.ORG_UPDATED'));
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
