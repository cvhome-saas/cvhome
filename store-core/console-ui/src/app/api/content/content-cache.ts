/** Console-native; not a port from seller-core. */
import {Injectable, signal} from '@angular/core';

/**
 * One stamp every content list and the hub summary key their resource on. A write anywhere in the
 * Content module bumps it, so a list that is open somewhere else reloads rather than showing a row
 * that was just published, moved or deleted in an editor.
 */
@Injectable({providedIn: 'root'})
export class ContentCache {
  readonly stamp = signal(0);

  invalidate(): void {
    this.stamp.update((value) => value + 1);
  }
}
