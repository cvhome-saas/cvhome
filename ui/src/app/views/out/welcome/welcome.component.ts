import {Component} from '@angular/core';
import {environment} from "../../../../environment";

@Component({
  selector: 'app-welcome',
  templateUrl: './welcome.component.html',
  styleUrls: ['./welcome.component.css']
})
export class WelcomeComponent {
  loginUrl: string;

  constructor() {
    this.loginUrl = environment.LOGIN_URL;
  }
}
