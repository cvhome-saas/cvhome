import {ChangeDetectorRef, Component, Input, OnInit} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {NbToastrService} from '@nebular/theme';
import {TranslateService} from '@ngx-translate/core';
import {ErrorService} from "../../shared/services/error.service";
import {OrgService} from "../services/org.service";

@Component({
  selector: 'ngx-create-new-org',
  standalone: false,
  templateUrl: './create-new-org.component.html',
  styleUrls: ['./create-new-org.component.scss']
})
export class CreateNewOrgComponent implements OnInit {
  form: FormGroup;
  @Input() title: string;
  pwdPattern = '^(?=[^A-Z]*[A-Z])(?=[^a-z]*[a-z])(?=[^0-9]*[0-9]).{6,12}$';
  emailPattern = '^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})$';
  subscriptionPlans: string[] = [];
  loader = false;

  constructor(
    private fb: FormBuilder,
    private orgService: OrgService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private toastr: NbToastrService,
    private translate: TranslateService,
    private errorService: ErrorService
  ) {
  }


  ngOnInit() {
    this.orgService.getSubscriptionPlans().subscribe({
      next: (it) => {
        this.subscriptionPlans = it;
        this.form.patchValue({
          "subscriptionPlan": this.subscriptionPlans[0]
        })
      },
      error: (err) => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      },
      complete: () => {

      }
    });

    this.createForm();
  }

  checkPasswords(group: FormGroup) { // here we have the 'passwords' group
    const password = group.get('user.password').value;
    const confirmPassword = group.get('user.repeatPassword').value;
    return password === confirmPassword ? null : {notSame: true}
  }


  save() {
    this.loader = true;
    this.orgService.createOrg(this.form.value).subscribe({
      next: (res) => {
        this.loader = false;
        this.toastr.success(this.translate.instant('ORG_FORM.ORG_CREATED'));
        this.goToBack();
      },
      error: (err) => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
        this.loader = false;
      }
    });
  }


  route(link) {
    this.router.navigate([link]);
  }

  goToBack() {
    this.router.navigate(['pages/org-management/org-list']);
  }


  private createForm() {
    this.form = this.fb.group({
      user: this.fb.group({
        firstName: ['', Validators.required],
        lastName: ['', Validators.required],
        emailAddress: new FormControl('', [Validators.required, Validators.email, Validators.pattern(this.emailPattern)]),
        password: new FormControl('', [Validators.required, Validators.pattern(this.pwdPattern)]),
        repeatPassword: new FormControl('', [Validators.required]),
      }),
      subscriptionPlan: new FormControl(this.subscriptionPlans[0], [Validators.required]),
    }, {validators: this.checkPasswords});
  }
}
