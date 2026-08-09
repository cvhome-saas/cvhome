import {Injectable, inject, signal} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {User} from 'seller-core';

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
