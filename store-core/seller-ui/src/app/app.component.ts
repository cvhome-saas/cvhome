import { Component, inject } from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {NbIconLibraries} from "@nebular/theme";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  private iconLibraries = inject(NbIconLibraries);

  title = 'seller-ui';

  constructor() {
    this.iconLibraries.registerFontPack('font-awesome', {packClass: 'fa', iconClassPrefix: 'fa'});
    this.iconLibraries.setDefaultPack('font-awesome');
  }

}
