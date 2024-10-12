import {Component} from '@angular/core';
import {Router, RouterLink} from "@angular/router";
import {SignUpService} from "../../service/sign-up.service";
import {FormBuilder, ReactiveFormsModule, Validators} from "@angular/forms";

@Component({
  selector: 'app-sign-up-form',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule
  ],
  templateUrl: './sign-up-form.component.html',
  styleUrl: './sign-up-form.component.css'
})
export class SignUpFormComponent {
  title: string = 'Sign Up';
  desc: string = 'Fill all fields so we can get required info';
  userForm: any;

  constructor(private signUpService: SignUpService, private formBuilder: FormBuilder, private router: Router) {
    this.userForm = this.formBuilder.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      emailAddress: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
    })
  }

  signUp() {
    if (this.userForm?.valid) {
      this.signUpService.signUp({
        firstName: "ashraf",
        lastName: "ashraf",
        emailAddress: "ashraf@mail.com",
        password: "admin"
      }).subscribe(it => {
        this.router.navigate(['/redirect/internal?serviceName=store-ui&path=/oauth2/authorization/keycloak']);
      })

    }
  }
}
