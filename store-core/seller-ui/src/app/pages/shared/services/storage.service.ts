import {Injectable, inject} from '@angular/core';

import {UserService} from './user.service';
import {ErrorService} from "./error.service";

@Injectable({
  providedIn: 'root'
})
export class StorageService {
  private readonly userService = inject(UserService);
  private readonly errorService = inject(ErrorService);

  getUserId() {
    let userId = localStorage.getItem('userId');
    if (!userId) {
      this.userService.getUserProfile()
        .subscribe(res => {
          userId = res.id;
          localStorage.setItem('userId', userId);
        }, err => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        });
    }
    return userId;
  }

  getMerchant() {
    let merchant = localStorage.getItem('merchant');
    if (!merchant) {
      this.userService.getUser(this.getUserId())
        .subscribe(user => {
          merchant = user.merchant;
          localStorage.setItem('merchant', merchant);
        }, err => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        });
    }
    return merchant;
  }
}

