import {NgModule} from '@angular/core';
import {RouterModule, Routes, UrlMatchResult, UrlSegment} from '@angular/router';
import {environment} from "../environment";
import {inGuard, outGuard} from "./service/auth-guard.service";


function matcher(pathIn: string[] = [], hostIn: string[] = [], pathNotIn: string[] = [], hostNotIn: string[] = []) {
  return (segments: UrlSegment[]): UrlMatchResult | null => {
    const host = window.location.hostname;
    const path = segments.length == 0 ? '' : segments[0].path

    let pathInPredicate = pathIn.length == 0 || new Set<string>(pathIn).has(path);
    let hostInPredicate = hostIn.length == 0 || new Set<string>(hostIn).has(host);
    let pathNotInPredicate = pathNotIn.length == 0 || !new Set<string>(pathNotIn).has(path);
    let hostNotInPredicate = hostNotIn.length == 0 || !new Set<string>(hostNotIn).has(host);
    let result = pathInPredicate && hostInPredicate && pathNotInPredicate && hostNotInPredicate;

    if (result) {
      return {
        consumed: segments
      };
    } else {
      return null;
    }
  }

}

const routes: Routes = [
  {
    matcher: matcher([], [], [], environment.ALL_BASE_DOMAINS),
    loadChildren: () => import('./views/blog/blog.module').then(m => m.BlogModule)
  },
  {
    path: 'in',
    canActivate: [inGuard()],
    loadChildren: () => import('./views/in/in.module').then(m => m.InModule)
  },
  {
    path: '',
    canActivate: [outGuard()],
    loadChildren: () => import('./views/out/out.module').then(m => m.OutModule)
  },
];

// configures NgModule imports and exports
@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
