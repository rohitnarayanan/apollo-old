import {NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';

import {
  ErrorComponent,
  ProfileInfoService
} from './';

import {
  homeRoute,
  errorRoute
} from './routes';

const LAYOUT_ROUTES = [
  ...homeRoute,
  ...errorRoute
];

@NgModule({
  imports: [
    RouterModule.forRoot(LAYOUT_ROUTES, {useHash: true})
  ],
  exports: [
    RouterModule
  ],
  declarations: [
    ErrorComponent
  ]
})
export class awfRoutingModule {}
