import {Component} from '@angular/core';
import {ShapeComponent} from "../../components/shape/shape.component";

@Component({
  selector: 'app-welcome',
  standalone: true,
  imports: [
    ShapeComponent
  ],
  templateUrl: './welcome.component.html',
  styleUrl: './welcome.component.css'
})
export class WelcomeComponent {
  title = 'Make cool store easily with Cvhome';
  desc = 'Join our clients and try create your store with few clicks for free.';
}
