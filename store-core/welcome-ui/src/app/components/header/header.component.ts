import {Component} from '@angular/core';
import {RouterLink} from "@angular/router";
import {EvnLoaderService} from "../../service/evn-loader.service";

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    RouterLink
  ],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {
  signInUrl: string | undefined;


  constructor( private evnLoaderService: EvnLoaderService) {
    this.signInUrl = this.evnLoaderService.signInUrl;
  }
}
