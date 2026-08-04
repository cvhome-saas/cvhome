import {Routes} from '@angular/router';
import {provideEchartsCore} from 'ngx-echarts';
import {HomeComponent} from './home.component';

export const HOME_ROUTES: Routes = [
  {
    path: '',
    component: HomeComponent,
    providers: [
      provideEchartsCore({
        echarts: () => import('echarts'),
      }),
    ],
  },
];
