import { Component } from '@angular/core';
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
  desc: string = 'There are many variations of passages of Lorem Ipsum available, but the majority have suffered alteration in some form, by injected humour, or randomised words which don\'t look even slightly believable. If you are going to use a passage of Lorem Ipsum.';
}
