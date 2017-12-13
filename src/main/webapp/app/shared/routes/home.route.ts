import {Routes} from '@angular/router';

import {HomeComponent} from '../../home/home.component';

export const homeRoute: Routes = [
  {
    path: '',
    redirectTo: '/home',
    pathMatch: 'full'
  },
  {
    path: 'home',
    component: HomeComponent,
    data: {
    },
  }
];
