import {Component} from '@angular/core';
import {ShapeComponent} from "../../components/shape/shape.component";

@Component({
  selector: 'app-privacy-policy',
  standalone: true,
  imports: [
    ShapeComponent,
  ],
  templateUrl: './privacy-policy.component.html',
  styleUrl: './privacy-policy.component.css'
})
export class PrivacyPolicyComponent {

}
