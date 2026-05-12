import {ChangeDetectorRef, Component, Input, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {map, mergeMap} from "rxjs/operators";
import {ErrorService} from "../../shared/services/error.service";
import {OrgService} from "../services/org.service";
import {Org} from "../model/org";
import {FormBuilder, FormControl, FormGroup} from "@angular/forms";
import {NbToastrService} from "@nebular/theme";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'ngx-update-org-details',
  standalone: false,
  templateUrl: './update-org-details.component.html',
  styleUrls: ['./update-org-details.component.scss']
})
export class UpdateOrgDetailsComponent implements OnInit {
  loadingInfo = false;
  form: FormGroup;
  @Input() title: string;
  subscriptionPlans: string[] = [];
  loader = false;
  org: Org
  selectedItem = '0';
  sidemenuLinks = [
    {
      id: '0',
      title: 'COMPONENTS.UPDATE_ORG',
      key: 'COMPONENTS.UPDATE_ORG',
      link: '/pages/org-management/org/{OrgId}'
    }, {
      id: '1',
      title: 'COMPONENTS.CHANGE_PASSWORD',
      key: 'COMPONENTS.CHANGE_PASSWORD',
      link: '/pages/org-management/org/{OrgId}/change-password'
    }, {
      id: '2',
      title: 'COMPONENTS.STORES',
      key: 'COMPONENTS.STORES',
      link: '/pages/org-management/org/{OrgId}/stores'
    }
  ];

  constructor(
    private activatedRoute: ActivatedRoute,
    private orgService: OrgService,
    private errorService: ErrorService,
    private fb: FormBuilder,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private toastr: NbToastrService,
    private translate: TranslateService,
  ) {
  }

  route(link) {
    this.router.navigate([link.replace("{OrgId}", this.org.id.id)]);
  }

  ngOnInit() {
    this.createForm();
    this.activatedRoute.params.pipe(map(it => it['id']), mergeMap(it => this.orgService.getOrg(it)))
      .subscribe(res => {
        this.loadingInfo = false;
        this.org = res;
        this.fillForm(res);
      }, err => {
        this.loadingInfo = false;
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      });

    this.orgService.getSubscriptionPlans().subscribe(it => {
      this.subscriptionPlans = it;
    });
  }


  fillForm(org: Org) {
    this.form.patchValue({
      email: org.email.email,
      subscriptionPlan: org.subscriptionPlan,
    });
    this.cdr.detectChanges();
  }

  save() {
    this.loader = true;
    const orgData = this.form.value;

    this.orgService.updateOrg(this.org.id.id, orgData)
      .subscribe({
        next: (res) => {
          this.toastr.success(this.translate.instant('ORG_FORM.ORG_UPDATED'));
          this.loader = false;
        },
        error: (err) => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
          this.loader = false;
        },
      });
  }


  goToBack() {
    this.router.navigate(['pages/org-management/org-list']);
  }

  private createForm() {
    this.form = this.fb.group({
      email: new FormControl({value: '', disabled: true}),
      subscriptionPlan: new FormControl(''),
    });
  }

}
