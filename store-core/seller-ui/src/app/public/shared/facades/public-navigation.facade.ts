import {Injectable, inject} from '@angular/core';
import {Router} from '@angular/router';

@Injectable()
export class PublicNavigationFacade {
  private readonly router = inject(Router);

  goHome(): void {
    this.router.navigate(['/']);
  }
}
