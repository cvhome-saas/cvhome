import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ProfileComponent} from "./views/profile/profile.component";

const routes: Routes = [
    {path: '', component: ProfileComponent},
];

// configures NgModule imports and exports
@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class ProfileRoutingModule {
}
