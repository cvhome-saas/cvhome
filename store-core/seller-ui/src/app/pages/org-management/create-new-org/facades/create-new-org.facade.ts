import {Injectable, inject, signal} from '@angular/core';
import {Router} from '@angular/router';
import {OrgService} from '../../services/org.service';
import {ErrorService} from '../../../shared/services/error.service';
import {CreateNewOrgFormService} from '../services/create-new-org-form.service';

@Injectable()
export class CreateNewOrgFacade {
  private readonly formService = inject(CreateNewOrgFormService);
  private readonly orgService = inject(OrgService);
  private readonly router = inject(Router);
  private readonly errorService = inject(ErrorService);

  readonly loader = signal<boolean>(false);
  readonly subscriptionPlans = signal<string[]>([]);

  get form() {
    return this.formService.form;
  }

  init(): void {
    this.orgService.getSubscriptionPlans().subscribe({
      next: (plans) => {
        this.subscriptionPlans.set(plans);
        this.formService.setSubscriptionPlans(plans);
      },
      error: (err) => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  save(): void {
    this.loader.set(true);
    this.orgService.createOrg(this.form.value).subscribe({
      next: () => {
        this.loader.set(false);
        this.errorService.success('ORG_FORM.ORG_CREATED');
        this.goToBack();
      },
      error: (err) => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
        this.loader.set(false);
      }
    });
  }

  goToBack(): void {
    this.router.navigate(['pages/org-management/org-list']);
  }
}
