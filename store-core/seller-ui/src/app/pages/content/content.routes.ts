import {Routes} from '@angular/router';
import {ContentComponent} from './content.component';
import {PageComponent} from './pages/page.component';
import {BoxesComponent} from './boxes/boxes.component';
import {AddPageComponent} from './pages/add-page.component';
import {AddBoxComponent} from './boxes/add-box.component';
import {FilesComponent} from './files/files.component';

export const CONTENT_ROUTES: Routes = [{
  path: '',
  component: ContentComponent,
  children: [
    {
      path: 'pages/list',
      component: PageComponent,
    },
    {
      path: 'pages/add',
      component: AddPageComponent,
    },
    {
      path: 'pages/edit/:code',
      component: AddPageComponent,
    },
    {
      path: 'boxes/list',
      component: BoxesComponent,
    },
    {
      path: 'boxes/add',
      component: AddBoxComponent,
    },
    {
      path: 'boxes/edit/:code',
      component: AddBoxComponent,
    },
    {
      path: 'files/list',
      component: FilesComponent,
    }],
}];
