import {Component} from '@angular/core';
import {ShapeComponent} from "../../components/shape/shape.component";
import {SignUpFormComponent} from "../../components/sign-up-form/sign-up-form.component";

@Component({
  selector: 'app-sign-up',
  standalone: true,
  imports: [
    ShapeComponent,
    SignUpFormComponent
  ],
  templateUrl: './sign-up.component.html',
  styleUrl: './sign-up.component.css'
})
export class SignUpComponent {
  title: string = 'Create an account!';
  desc: string = 'Its just a few steps and you will be one of our amazing clients.';
}
