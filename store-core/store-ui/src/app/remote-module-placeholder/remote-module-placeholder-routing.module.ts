import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RemoteModulePlaceholderComponent } from './remote-module-placeholder.component';

const routes: Routes = [
  { path: '', component: RemoteModulePlaceholderComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class RemoteModulePlaceholderRoutingModule {}
