import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {ErrorComponent} from "./error/error.component";

const routes: Routes = [
  {path: 'err', component: ErrorComponent},
];

// configures NgModule imports and exports
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class OutRoutingModule {
}
