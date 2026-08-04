import {Injectable, inject, signal} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {User} from '../../../shared/models/user';

@Injectable()
export class CreateNewUserFacade {
  private readonly activatedRoute = inject(ActivatedRoute);

  readonly store = signal<string>('');
  readonly user = signal<User | undefined>(undefined);

  init(): void {
    this.activatedRoute.params.subscribe((params) => {
      this.store.set(params['store'] || '');
    });
  }
}
