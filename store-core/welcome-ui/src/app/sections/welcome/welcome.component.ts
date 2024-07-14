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
  title: string = 'Make cool Landing pages with sApp';
  desc: string = 'Lorem ipsum dolor sit amet, consectetur adipisicing elit. Impedit nihil tenetur minus quidem est deserunt molestias accusamus harum ullam tempore debitis et, expedita, repellat delectus aspernatur neque itaque qui quod.';
  banner: string = 'img/welcome/welcome-mockup.png'
}
