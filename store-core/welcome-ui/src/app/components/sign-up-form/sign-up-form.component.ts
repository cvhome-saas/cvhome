import {Component, Inject, makeStateKey, PLATFORM_ID, TransferState} from '@angular/core';
import {RouterLink} from "@angular/router";
import {isPlatformBrowser, isPlatformServer} from "@angular/common";

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
  storeSignIn: string|undefined;

  constructor(@Inject(PLATFORM_ID) private platformID: Object) {
    if (isPlatformServer(this.platformID)) { //<--- this block run on server
      console.log("this block runs only on server");
      const APP_SIGNIN_DOMAIN_ENV_VALUE = process.env['COM_ASREVO_CVHOME_APP_SIGNIN_DOMAIN'];
      if (APP_SIGNIN_DOMAIN_ENV_VALUE) {
        this.storeSignIn = APP_SIGNIN_DOMAIN_ENV_VALUE;
        console.log(this.storeSignIn)
      }
    }
  }
}
