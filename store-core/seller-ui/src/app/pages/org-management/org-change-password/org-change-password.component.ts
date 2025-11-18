import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ErrorService} from "../../../shared/services/error.service";
import {Org} from "../model/org";
import {map, mergeMap} from "rxjs/operators";
import {OrgService} from "../services/org.service";

@Component({
  selector: 'ngx-org-change-password',
  standalone: false,
  templateUrl: './org-change-password.component.html',
  styleUrls: ['./org-change-password.component.scss']
})
export class OrgChangePasswordComponent implements OnInit {
  form: FormGroup;
  loader = false;
  pwdPattern = '^(?=[^A-Z]*[A-Z])(?=[^a-z]*[a-z])(?=[^0-9]*[0-9]).{6,12}$';
  org: Org
  selectedItem = '1';
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

  route(link) {
    this.router.navigate([link.replace("{OrgId}", this.org.id.id)]);
  }


  goToBack() {
    this.router.navigate(['pages/org-management/org-list']);
  }


  constructor(
    private fb: FormBuilder,
    private router: Router,
    private errorService: ErrorService,
    private activatedRoute: ActivatedRoute,
    private orgService: OrgService,
  ) {
    this.createForm();
  }


  ngOnInit() {
    this.activatedRoute.params.pipe(map(it => it['id']), mergeMap(it => this.orgService.getOrg(it)))
      .subscribe(res => {
        this.org = res;
      }, err => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      });
  }

  checkPasswords(group: FormGroup) {
    const pass = group.controls.newPassword.value;
    const confirmPass = group.controls.confirmNewPassword.value;
    return pass === confirmPass ? null : {notSame: true};
  }

  save() {
    this.loader = true;
    const passwords = {
      password: this.form.value.newPassword
    };
    this.orgService.changeOrgPassword(this.org.id.id, passwords)
      .subscribe({
        next: (it) =>
          this.errorService.success('ORG_FORM.PASSWORD_CHANGED_SUCCESSFULLY'),
        error: (err) =>
          this.errorService.error('ERROR.SYSTEM_ERROR', err),
        complete: () => this.loader = false
      });

  }

  private createForm() {
    this.form = this.fb.group({
      newPassword: ['', [Validators.required, Validators.pattern(this.pwdPattern)]],
      confirmNewPassword: ['', [Validators.required]],
    }, {validator: this.checkPasswords});
  }

}
