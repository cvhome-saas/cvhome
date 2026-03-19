import {Component} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {ShapeComponent} from '../componants/shape/shape.component';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  imports: [
    ReactiveFormsModule,
    ShapeComponent
  ],
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {

  contactForm = new FormGroup({
    username: new FormControl(''),
    password: new FormControl('')
  });

}
