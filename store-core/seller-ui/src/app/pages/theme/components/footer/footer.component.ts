import {Component} from '@angular/core';

@Component({
  selector: 'ngx-footer',
  standalone: true,
  template: `
    <span>
      Created with ♥ by <b><a href="mailto:{{email}}">{{ email }}</a></b> {{ year }}
    </span>
  `,
})
export class FooterComponent {
  email = "me@asrevo.com"
  year = 2025
}
