import {Inject, Injectable, makeStateKey, PLATFORM_ID, StateKey, TransferState} from '@angular/core';
import {isPlatformServer} from "@angular/common";

const SIGN_IN_URL_KEY: StateKey<string> = makeStateKey('SIGN_IN_URL_KEY');
const BACKEND_URL_KEY: StateKey<string> = makeStateKey('BACKEND_URL_KEY');

@Injectable({
  providedIn: 'root'
})
export class EvnLoaderService {
  signInUrl: string | undefined;
  backendUrl: string | undefined;

  constructor(@Inject(PLATFORM_ID) private platformID: Object, private transferState: TransferState) {
    if (isPlatformServer(this.platformID)) {
      this.transferState.set(SIGN_IN_URL_KEY, process.env['COM_ASREVO_CVHOME_APP_SIGN_IN_URL_KEY'])
      this.transferState.set(BACKEND_URL_KEY, process.env['COM_ASREVO_CVHOME_APP_BACKEND_URL_KEY'])
    }
    this.loadState();
  }

  loadState() {
    this.signInUrl = this.transferState.get(SIGN_IN_URL_KEY, undefined);
    this.backendUrl = this.transferState.get(BACKEND_URL_KEY, undefined);
  }

}
