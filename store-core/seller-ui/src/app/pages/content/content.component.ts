import {Component} from '@angular/core';
import {RouterOutlet} from '@angular/router';

@Component({
  standalone: true,
  imports: [RouterOutlet],
  selector: 'ngx-content-management',
  templateUrl: './content.component.html',
})
export class ContentComponent {
}
