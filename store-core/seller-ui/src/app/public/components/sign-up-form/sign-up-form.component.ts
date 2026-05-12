import {Component, Inject, PLATFORM_ID} from '@angular/core';
import {SignUpService} from "../../service/sign-up.service";
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {ToastrService} from "ngx-toastr";
import {isPlatformBrowser} from '@angular/common';
import {Router} from "@angular/router";

@Component({
  selector: 'app-sign-up-form',
  standalone: true,
  imports: [
    ReactiveFormsModule

  ],
  templateUrl: './sign-up-form.component.html',
  styleUrl: './sign-up-form.component.css'
})
export class SignUpFormComponent {
  title: string = 'Sign Up';
  userForm: any;

  constructor(@Inject(PLATFORM_ID) private platformId: Object, private router: Router,
              private signUpService: SignUpService, private formBuilder: FormBuilder, private toastr: ToastrService) {
    this.userForm = this.formBuilder.group({
      user: this.formBuilder.group({
        firstName: ['', Validators.required],
        lastName: ['', Validators.required],
        emailAddress: ['', [Validators.required, Validators.email]],
        password: ['', Validators.required],
        repeatPassword: ['', Validators.required],
      }, {validators: this.checkPasswords}),
    })
  }

  checkPasswords(group: FormGroup) { // here we have the 'passwords' group
    // @ts-ignore
    const password = group.get('password').value;
    // @ts-ignore
    const confirmPassword = group.get('repeatPassword').value;
    return password === confirmPassword ? null : {notSame: true}
  }


  signUp() {
    if (this.userForm?.valid) {
      this.signUpService.signUp(this.userForm.value).subscribe({
        next: (it) => {
          this.toastr.success('Successfully Register!', 'You will be redirected to login page');
          this.doLazyRedirect();
        },
        error: (err) => {
          this.toastr.error('Failed to Register!', 'Please fill all required fields');
        }
      });
    }
  }

  doLazyRedirect() {
    setTimeout(() => {
      if (isPlatformBrowser(this.platformId)) {
        this.router.navigate(["/pages"])
      }
    }, 2000);
  }
}
