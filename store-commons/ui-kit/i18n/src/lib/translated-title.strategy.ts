import {DestroyRef, inject, Injectable} from '@angular/core';
import {Title} from '@angular/platform-browser';
import {RouterStateSnapshot, TitleStrategy} from '@angular/router';
import {TranslocoService} from '@jsverse/transloco';

import {routeData} from '@cvhome-saas/ui-kit';

/**
 * Route `title` is a static string resolved once at navigation — it cannot follow a
 * language change on its own. Routes carry `data.titleKey` instead (see `app.routes.ts`);
 * this strategy translates it and re-applies on every `langChanges$` emission, using the
 * last snapshot it saw.
 */
@Injectable({providedIn: 'root'})
export class TranslatedTitleStrategy extends TitleStrategy {
  private readonly title = inject(Title);
  private readonly transloco = inject(TranslocoService);
  private readonly destroyRef = inject(DestroyRef);

  private lastSnapshot: RouterStateSnapshot | undefined;

  constructor() {
    super();
    const subscription = this.transloco.langChanges$.subscribe(() => {
      if (this.lastSnapshot) {
        this.updateTitle(this.lastSnapshot);
      }
    });
    this.destroyRef.onDestroy(() => subscription.unsubscribe());
  }

  override updateTitle(snapshot: RouterStateSnapshot): void {
    this.lastSnapshot = snapshot;
    const titleKey = this.buildTitleKey(snapshot);
    if (titleKey) {
      this.title.setTitle(this.transloco.translate(titleKey));
    }
  }

  private buildTitleKey(snapshot: RouterStateSnapshot): string | undefined {
    let route = snapshot.root;
    let titleKey: string | undefined;
    while (route.firstChild) {
      route = route.firstChild;
      titleKey = routeData(route.data).titleKey ?? titleKey;
    }
    return titleKey ?? routeData(snapshot.root.data).titleKey;
  }
}
