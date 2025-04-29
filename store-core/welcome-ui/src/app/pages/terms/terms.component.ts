import {Component} from '@angular/core';
import {ShapeComponent} from "../../components/shape/shape.component";

@Component({
  selector: 'app-terms',
  standalone: true,
  imports: [
    ShapeComponent
  ],
  templateUrl: './terms.component.html',
  styleUrl: './terms.component.css'
})
export class TermsComponent {

}
