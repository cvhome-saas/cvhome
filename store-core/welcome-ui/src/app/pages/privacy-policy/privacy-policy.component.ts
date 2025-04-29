import {Component} from '@angular/core';
import {ShapeComponent} from "../../components/shape/shape.component";
import {SignUpFormComponent} from "../../components/sign-up-form/sign-up-form.component";

@Component({
  selector: 'app-privacy-policy',
  standalone: true,
  imports: [
    ShapeComponent,
    SignUpFormComponent
  ],
  templateUrl: './privacy-policy.component.html',
  styleUrl: './privacy-policy.component.css'
})
export class PrivacyPolicyComponent {

}
