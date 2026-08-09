import {Injectable, inject, signal} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {map, mergeMap} from 'rxjs/operators';
import {UserService} from 'seller-core';
import {ApiErrorService} from 'seller-core';
import {User} from 'seller-core';

@Injectable()
export class UserDetailsFacade {
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly userService = inject(UserService);
  private readonly apiErrors = inject(ApiErrorService);

  readonly user = signal<User>(null);
  readonly loadingInfo = signal<boolean>(false);

  init(): void {
    this.loadingInfo.set(true);
    this.activatedRoute.params
      .pipe(
        map(params => params['id']),
        mergeMap(id => this.userService.getUser(id))
      )
      .subscribe({
        next: (res) => {
          this.user.set(res);
          this.loadingInfo.set(false);
        },
        error: (err) => {
          this.loadingInfo.set(false);
          this.apiErrors.notify(err);
        }
      });
  }
}
