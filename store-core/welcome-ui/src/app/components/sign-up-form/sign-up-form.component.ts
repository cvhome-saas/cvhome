import {Component, makeStateKey} from '@angular/core';
import {RouterLink} from "@angular/router";
import {SignUpService} from "../../service/sign-up.service";

const DOMAIN_KEY = makeStateKey<{ data: string }>("data")

@Component({
  selector: 'app-sign-up-form',
  standalone: true,
  imports: [
    RouterLink
  ],
  templateUrl: './sign-up-form.component.html',
  styleUrl: './sign-up-form.component.css'
})
export class SignUpFormComponent {
  title: string = 'Sign Up';
  desc: string = 'Fill all fields so we can get required info';
  signInUrl: string | undefined;

  constructor(private signUpService: SignUpService) {
  }

  signUp() {
    this.signUpService.signUp({
      name: "ashraf",
      email: "ashraf@mail.com",
      password: "admin",
      phone: "+20*********",
    }).subscribe(it => {
      console.log(it.status)
    })
  }
}
