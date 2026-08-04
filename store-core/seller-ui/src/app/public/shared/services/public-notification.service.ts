import {Injectable, inject} from '@angular/core';
import {ToastrService} from 'ngx-toastr';

@Injectable({
  providedIn: 'root'
})
export class PublicNotificationService {
  private readonly toastr = inject(ToastrService);

  success(message: string, title?: string): void {
    this.toastr.success(message, title);
  }

  error(message: string, title?: string): void {
    this.toastr.error(message, title);
  }
}
