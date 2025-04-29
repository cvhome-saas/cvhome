import {Component} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {NbIconLibraries} from '@nebular/theme';

@Component({
  selector: 'app-root',
  template: '<router-outlet/>',
  imports: [
    RouterOutlet
  ]
})
export class AppComponent {
  constructor(private iconLibraries: NbIconLibraries) {
    this.iconLibraries.registerFontPack('font-awesome', { packClass: 'fa', iconClassPrefix: 'fa' });
    this.iconLibraries.setDefaultPack('font-awesome');
  }
}
