import {Injectable, isDevMode} from '@angular/core';
import {TranslocoMissingHandler, TranslocoMissingHandlerData} from '@jsverse/transloco';

/**
 * The enforcement half of "no literal strings": a missing key is a broken translation
 * pipeline, not a cosmetic gap, so development throws instead of silently falling back to
 * English. Production logs and returns the key — a visible placeholder beats a crash for a
 * seller mid-task.
 */
@Injectable({providedIn: 'root'})
export class StrictMissingHandler implements TranslocoMissingHandler {
  handle(key: string, data: TranslocoMissingHandlerData): string {
    if (isDevMode()) {
      throw new Error(`Missing translation key "${key}" for lang "${data.activeLang}"`);
    }
    console.error(`Missing translation key "${key}" for lang "${data.activeLang}"`);
    return key;
  }
}
