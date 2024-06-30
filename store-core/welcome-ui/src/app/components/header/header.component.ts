import {Component, Inject, makeStateKey, PLATFORM_ID, StateKey, TransferState} from '@angular/core';
import {RouterLink} from "@angular/router";
import {isPlatformServer} from "@angular/common";

const key:StateKey<string> = makeStateKey('storeSignIn');


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
  storeSignIn: string | undefined;


  constructor(@Inject(PLATFORM_ID) private platformID: Object,private transferState:TransferState) {
    if (isPlatformServer(this.platformID)) { //<--- this block run on server
      console.log("in server");
      this.storeSignIn = process.env['COM_ASREVO_CVHOME_APP_SIGNIN_DOMAIN'];
      console.log("setting this to state "+this.storeSignIn)
      this.transferState.set(key,this.storeSignIn)
      console.log("got from state in server "+this.transferState.get(key,undefined))
    } else {
      console.log("in web")
      console.log("got from state in web "+this.transferState.get(key,undefined))


    }
  }
}
